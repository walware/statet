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

import de.walware.eclipsecommons.FileUtil.ReadTextFileOperation;
import de.walware.eclipsecommons.FileUtil.ReaderAction;
import de.walware.eclipsecommons.FileUtil.WriteTextFileOperation;


/**
 *
 */
public class WorkspaceUtilImpl extends FileUtilProvider {

	private static InputStream EMPTY_INPUT = new ByteArrayInputStream(new byte[0]);

	@Override
	public ReadTextFileOperation createReadTextFileOp(final ReaderAction action, Object file) {
		final IFile wsFile = (IFile) file;
		return new ReadTextFileOperation() {
			@Override
			protected String getFileLabel() {
				return WorkspaceUtilImpl.this.getFileLabel0(wsFile);
			}
			@Override
			protected FileInput getInput(IProgressMonitor monitor) throws CoreException, IOException {
				try {
					InputStream raw = wsFile.getContents(true);
					return new FileInput(raw, wsFile.getCharset(false));
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
				runAsWorkspaceRunnable(monitor, wsFile);
			}
		};
	}

	@Override
	public WriteTextFileOperation createWriteTextFileOp(final String content, Object file) {
		final IFile wsFile = (IFile) file;
		return new WriteTextFileOperation() {
			@Override
			protected String getFileLabel() {
				return WorkspaceUtilImpl.this.getFileLabel0(wsFile);
			}
	
			@Override
			public void doOperation(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
				runAsWorkspaceRunnable(monitor, wsFile);
			}
			@Override
			protected void writeImpl(IProgressMonitor monitor) throws CoreException, UnsupportedEncodingException {
				boolean exists = wsFile.exists();
				if (exists && ((fMode & EFS.APPEND) != 0)) {
					if (fForceCharset) {
						wsFile.setCharset(fCharset, new SubProgressMonitor(monitor, 20));
					}
					else {
						fCharset = wsFile.getCharset();
						monitor.worked(20);
					}
						
					wsFile.appendContents(new ByteArrayInputStream(content.getBytes(fCharset)),
							(IFile.FORCE | IFile.KEEP_HISTORY),
							new SubProgressMonitor(monitor, 80));
				}
				else {
					if (exists && ((fMode & EFS.OVERWRITE) != 0)) {
						wsFile.setContents(EMPTY_INPUT, IFile.FORCE | IFile.KEEP_HISTORY,
								new SubProgressMonitor(monitor, 15));
					}
					else {
						wsFile.create(EMPTY_INPUT, IFile.FORCE, new SubProgressMonitor(monitor, 15));
					}
					if (fForceCharset || !fCharset.equals(wsFile.getCharset(true))) {
						wsFile.setCharset(fCharset, new SubProgressMonitor(monitor, 5));
					} else {
						monitor.worked(5);
					}
					wsFile.setContents(new ByteArrayInputStream(content.getBytes(fCharset)),
							IFile.NONE, new SubProgressMonitor(monitor, 80));
				}
			}
		};
	}

	@Override
	public long getTimeStamp(Object file, IProgressMonitor monitor) throws CoreException {
		final IFile wsFile = (IFile) file;
		final long stamp = wsFile.getLocalTimeStamp();
		monitor.done();
		return stamp;
	}
	
	private String getFileLabel0(IFile ifile) {
		return "'"+ifile.getFullPath().makeRelative().toString()+"' (workspace)";
	}

}
