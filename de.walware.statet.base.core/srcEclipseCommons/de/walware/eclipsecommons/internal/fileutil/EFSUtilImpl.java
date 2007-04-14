/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
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

import de.walware.eclipsecommons.FileUtil;


/**
 *
 */
public class EFSUtilImpl extends FileUtil {

	public ReadTextFileOperation createReadTextFileOp(final ReaderAction action, final IFileStore file,
			String pluginID) { 
		
		return new ReadTextFileOperation(pluginID) {
			@Override
			protected String getFileLabel() {
				return EFSUtilImpl.this.getFileLabel(file);
			}
			
			@Override
			protected FileInput getInput(IProgressMonitor monitor) throws CoreException, IOException {
				try {
					InputStream raw = file.openInputStream(EFS.NONE, monitor);
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

	public WriteTextFileOperation createWriteTextFileOp(final String content, final IFileStore file, final String pluginID) {
		
		return new WriteTextFileOperation(pluginID) {
			@Override
			protected String getFileLabel() {
				return EFSUtilImpl.this.getFileLabel(file);
			}
	
			@Override
			protected void writeImpl(IProgressMonitor monitor) throws CoreException, IOException {
				Writer out = null;
				try {
					boolean exists = file.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 5)).exists();
					if (exists && (fMode & (EFS.OVERWRITE | EFS.APPEND)) == 0) {
						throw new CoreException(new Status(IStatus.ERROR, pluginID, 0,
								"The file already exists.", null));
					}
					if (exists && (fMode & EFS.APPEND) != 0 && !fForceCharset) {
						try {
							InputStream raw = file.openInputStream(EFS.NONE, new SubProgressMonitor(monitor, 5));
							FileInput fi = new FileInput(raw, null);
							fi.close();
							String defaultCharset = fi.getDefaultCharset();
							if (defaultCharset != null) {
								fCharset = defaultCharset;
							}
						}
						catch (IOException e) { }
						finally {
							monitor.worked(5);
						}
					}
					else {
						monitor.worked(10);
					}
					out = new OutputStreamWriter(file.openOutputStream(fMode, new SubProgressMonitor(monitor, 5)), fCharset);
	
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

	private String getFileLabel(IFileStore file) {
		
		IFileSystem system = file.getFileSystem();
		if (system.equals(EFS.getLocalFileSystem())) {
			return "'"+file.toString()+"' (local file)";
		}
		return "'"+file.toURI().toString()+"'";
	
	}
	
}
