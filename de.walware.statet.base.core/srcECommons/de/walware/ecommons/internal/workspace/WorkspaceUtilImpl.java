/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.internal.workspace;

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
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import de.walware.ecommons.FileUtil;


/**
 * Impl of FileUtil for Eclipse workspace files.
 */
public class WorkspaceUtilImpl extends FileUtil {
	
	private static final InputStream EMPTY_INPUT = new ByteArrayInputStream(new byte[0]);
	private static final String LABEL_2_WORKSPACE = "' ("+Messages.FileType_Workspace_name+")"; //$NON-NLS-1$ //$NON-NLS-2$
	
	
	private IFile fFile;
	
	
	public WorkspaceUtilImpl(final IFile file) {
		fFile = file;
	}
	
	
	@Override
	public String getFileLabel() {
		return "'"+fFile.getFullPath().makeAbsolute().toString()+LABEL_2_WORKSPACE;  //$NON-NLS-1$
	}
	
	@Override
	public long getTimeStamp(final IProgressMonitor monitor) throws CoreException {
		final long stamp = fFile.getLocalTimeStamp();
		monitor.done();
		return stamp;
	}
	
	
	@Override
	public ReadTextFileOperation createReadTextFileOp(final ReaderAction action) {
		return new ReadTextFileOperation() {
			
			@Override
			protected FileInput getInput(final IProgressMonitor monitor) throws CoreException, IOException {
				try {
					final InputStream raw = fFile.getContents(true);
					return new FileInput(raw, fFile.getCharset(false));
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
			public void doOperation(final IProgressMonitor monitor) throws CoreException, OperationCanceledException {
				runAsWorkspaceRunnable(monitor, null);
			}
			
		};
	}
	
	@Override
	public WriteTextFileOperation createWriteTextFileOp(final String content) {
		return new WriteTextFileOperation() {
			
			@Override
			public void doOperation(final IProgressMonitor monitor) throws CoreException, OperationCanceledException {
				final ISchedulingRule rule = (fFile.exists()) ? 
						fFile.getWorkspace().getRuleFactory().modifyRule(fFile) :
						fFile.getWorkspace().getRuleFactory().createRule(fFile);
				runAsWorkspaceRunnable(monitor, rule);
			}
			
			@Override
			protected void writeImpl(final IProgressMonitor monitor) throws CoreException, UnsupportedEncodingException {
				final boolean exists = fFile.exists();
				if (exists && ((fMode & EFS.APPEND) != 0)) {
					if (fForceCharset) {
						fFile.setCharset(fCharset, new SubProgressMonitor(monitor, 20));
					}
					else {
						fCharset = fFile.getCharset(true);
						monitor.worked(20);
					}
						
					fFile.appendContents(new ByteArrayInputStream(content.getBytes(fCharset)),
							(IFile.FORCE | IFile.KEEP_HISTORY),
							new SubProgressMonitor(monitor, 80));
				}
				else {
					if (exists && ((fMode & EFS.OVERWRITE) != 0)) {
						fFile.setContents(EMPTY_INPUT, IFile.FORCE | IFile.KEEP_HISTORY,
								new SubProgressMonitor(monitor, 15));
					}
					else {
						fFile.create(EMPTY_INPUT, IFile.FORCE, new SubProgressMonitor(monitor, 15));
					}
					if (fForceCharset || !fCharset.equals(fFile.getCharset(true))) {
						fFile.setCharset(fCharset, new SubProgressMonitor(monitor, 5));
					} else {
						monitor.worked(5);
					}
					fFile.setContents(new ByteArrayInputStream(content.getBytes(fCharset)),
							IFile.NONE, new SubProgressMonitor(monitor, 80));
				}
			}
			
		};
	}
	
}
