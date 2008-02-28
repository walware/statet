/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.internal.fileutil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import de.walware.eclipsecommons.ICommonStatusConstants;
import de.walware.eclipsecommons.FileUtil.ReadTextFileOperation;
import de.walware.eclipsecommons.FileUtil.ReaderAction;
import de.walware.eclipsecommons.FileUtil.WriteTextFileOperation;

import de.walware.statet.base.core.StatetCore;


/**
 * 
 */
public class EFSUtilImpl extends FileUtilProvider {
	
	
	@Override
	public ReadTextFileOperation createReadTextFileOp(final ReaderAction action, final Object file) {
		final IFileStore fileStore = (IFileStore) file;
		return new ReadTextFileOperation() {
			@Override
			protected String getFileLabel() {
				return EFSUtilImpl.this.getFileLabel0(fileStore);
			}
			
			@Override
			protected FileInput getInput(final IProgressMonitor monitor) throws CoreException, IOException {
				try {
					final InputStream raw = fileStore.openInputStream(EFS.NONE, monitor);
					return new FileInput(raw, null);
				}
				finally {
					monitor.done();
				}
			}
			@Override
			protected ReaderAction getAction() {
				return action;
			}
		};
	}
	
	@Override
	public WriteTextFileOperation createWriteTextFileOp(final String content, final Object file) {
		final IFileStore fileStore = (IFileStore) file;
		return new WriteTextFileOperation() {
			@Override
			protected String getFileLabel() {
				return EFSUtilImpl.this.getFileLabel0(fileStore);
			}
			
			@Override
			protected void writeImpl(final IProgressMonitor monitor) throws CoreException, IOException {
				Writer out = null;
				try {
					final boolean exists = fileStore.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 5)).exists();
					if (exists && (fMode & (EFS.OVERWRITE | EFS.APPEND)) == 0) {
						throw new CoreException(new Status(IStatus.ERROR, StatetCore.PLUGIN_ID, ICommonStatusConstants.IO_ERROR,
								"The file already exists.", null));
					}
					if (exists && (fMode & EFS.APPEND) != 0 && !fForceCharset) {
						try {
							final InputStream raw = fileStore.openInputStream(EFS.NONE, new SubProgressMonitor(monitor, 5));
							final FileInput fi = new FileInput(raw, null);
							fi.close();
							final String defaultCharset = fi.getDefaultCharset();
							if (defaultCharset != null) {
								fCharset = defaultCharset;
							}
						}
						catch (final IOException e) { }
						finally {
							monitor.worked(5);
						}
					}
					else {
						monitor.worked(10);
					}
					out = new OutputStreamWriter(fileStore.openOutputStream(fMode, new SubProgressMonitor(monitor, 5)), fCharset);
					
					out.write(content);
					monitor.worked(75);
					out.flush();
				}
				finally {
					saveClose(out);
				}
			}
		};
	}
	
	private String getFileLabel0(final IFileStore file) {
		final IFileSystem system = file.getFileSystem();
		if (system.equals(EFS.getLocalFileSystem())) {
			return "'"+file.toString()+"' (local file)";
		}
		return "'"+file.toURI().toString()+"'";
	
	}
	
	@Override
	public long getTimeStamp(final Object file, final IProgressMonitor monitor) throws CoreException {
		final IFileStore fileStore = (IFileStore) file;
		return fileStore.fetchInfo(EFS.NONE, monitor).getLastModified();
	}
	
}
