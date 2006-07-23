/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.internal.fileutil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import de.walware.eclipsecommons.FileUtil;


/**
 *
 */
public class WorkspaceUtilImpl extends FileUtil {

	private static InputStream EMPTY_INPUT = new ByteArrayInputStream(new byte[0]);

	
	public ReadTextFileOperation createReadTextFileOp(final ReaderAction action, final IFile file, 
			String pluginID) {
		
		return new ReadTextFileOperation(pluginID) {
			@Override
			protected String getFileLabel() {
				return WorkspaceUtilImpl.this.getFileLabel(file);
			}
	
			@Override
			protected FileInput getInput(IProgressMonitor monitor) throws CoreException, IOException {
				try {
					InputStream raw = file.getContents(true);
					return new FileInput(raw, file.getCharset(false));
				}
				finally {
					monitor.done();
				}
			}
			@Override
			protected ReaderAction getAction() {
				return action;
			}
	
			@Override
			public void doOperation(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
				runAsWorkspaceRunnable(monitor, file);
			}			
		};
	}

	public WriteTextFileOperation createWriteTextFileOp(final String content, final IFile file, String pluginID) {
		
		return new WriteTextFileOperation(pluginID) {
			@Override
			protected String getFileLabel() {
				return WorkspaceUtilImpl.this.getFileLabel(file);
			}
	
			@Override
			public void doOperation(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
				runAsWorkspaceRunnable(monitor, file);
			}
			@Override
			protected void writeImpl(IProgressMonitor monitor) throws CoreException, UnsupportedEncodingException {
				boolean exists = file.exists();
				if (exists && ((fMode & EFS.APPEND) != 0)) {
					if (fForceCharset) {
						file.setCharset(fCharset, new SubProgressMonitor(monitor, 20));
					}
					else {
						fCharset = file.getCharset();
						monitor.worked(20);
					}
						
					file.appendContents(new ByteArrayInputStream(content.getBytes(fCharset)), 
							(IFile.FORCE | IFile.KEEP_HISTORY), 
							new SubProgressMonitor(monitor, 80));
				}
				else {
					if (exists && ((fMode & EFS.OVERWRITE) != 0)) {
						file.setContents(EMPTY_INPUT, IFile.FORCE | IFile.KEEP_HISTORY, 
								new SubProgressMonitor(monitor, 15));
					}
					else {
						file.create(EMPTY_INPUT, IFile.FORCE, new SubProgressMonitor(monitor, 15));
					}
					if (fForceCharset || !fCharset.equals(file.getCharset(true))) {
						file.setCharset(fCharset, new SubProgressMonitor(monitor, 5));
					} else {
						monitor.worked(5);
					}
					file.setContents(new ByteArrayInputStream(content.getBytes(fCharset)), 
							IFile.NONE, new SubProgressMonitor(monitor, 80));
				}
			}
		};
	}

	private String getFileLabel(IFile ifile) {
		
		return "'"+ifile.getFullPath().makeRelative().toString()+"' (workspace)";
	}

}
