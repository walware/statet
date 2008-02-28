/*******************************************************************************
 * Copyright (c) 2006-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;

import de.walware.eclipsecommons.internal.fileutil.EFSUtilImpl;
import de.walware.eclipsecommons.internal.fileutil.FileUtilProvider;
import de.walware.eclipsecommons.internal.fileutil.WorkspaceUtilImpl;
import de.walware.eclipsecommons.internal.fileutil.FileUtilProvider.FileInput;

import de.walware.statet.base.core.StatetCore;


/**
 * Utilities to work with files.
 */
public class FileUtil {
	
	
	public static String UTF_8 = "UTF-8"; //$NON-NLS-1$
	public static String UTF_16_BE = "UTF-16BE"; //$NON-NLS-1$
	public static String UTF_16_LE = "UTF-16LE"; //$NON-NLS-1$
	
	private static EFSUtilImpl EFS_UTIL = new EFSUtilImpl();
	private static WorkspaceUtilImpl WORKSPACE_UTIL = new WorkspaceUtilImpl();
	
	
/*-- Local files --*/
	public static IFileStore getLocalFileStore(final String s) throws CoreException {
		
		if (s.length() > 0) {
			final IFileSystem localFS = EFS.getLocalFileSystem();
			if (s.startsWith(EFS.SCHEME_FILE)) {
				return localFS.getStore(URI.create(s));
			}
			final IPath path = Path.fromOSString(s);
			if (path.isUNC()) {
				return localFS.getStore(URIUtil.toURI(path));
			}
			if (path.isAbsolute()) {
				final String device = path.getDevice();
				if (device == null || device.length() <= 2) {
					return localFS.getStore(URIUtil.toURI(path));
				}
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, StatetCore.PLUGIN_ID, "No local filesystem resource."));
	}
	
	public static IFileStore expandToLocalFileStore(final String location, final String child) throws CoreException {
		
		final IStringVariableManager variables = VariablesPlugin.getDefault().getStringVariableManager();
		final String expanded = variables.performStringSubstitution(location);
		final IFileStore localFileStore = getLocalFileStore(expanded);
		if (child != null) {
			return localFileStore.getChild(child);
		}
		return localFileStore;
	}
	
	public static IPath expandToLocalPath(final String location, final String child) throws CoreException {
		
		final IFileStore fileStore = expandToLocalFileStore(location, child);
		return URIUtil.toPath(fileStore.toURI());
	}
	
	
/*-- File Operations --*/
	public static abstract class AbstractFileOperation {
		
		protected int fMode = EFS.NONE;
		
		protected String fCharset = UTF_8;
		protected boolean fForceCharset = false;
		
		protected AbstractFileOperation() {
		}
		
		public void setFileOperationMode(final int mode) {
			fMode = mode;
		}
		
		public void setCharset(final String charset, final boolean forceCharset) {
			fCharset = charset;
			fForceCharset = forceCharset;
		}
		
		protected abstract String getFileLabel();
		
		public void doOperation(final IProgressMonitor monitor) throws CoreException, OperationCanceledException {
			runInEnv(monitor);
		}
		
		protected abstract void runInEnv(IProgressMonitor monitor) throws CoreException, OperationCanceledException;
		
		protected void runAsWorkspaceRunnable(final IProgressMonitor monitor, final IResource scope) throws CoreException, OperationCanceledException {
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			final ISchedulingRule rule = workspace.getRuleFactory().createRule(scope);
			final IWorkspaceRunnable workspaceRunner = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					runInEnv(monitor);
				}
			};
			workspace.run(workspaceRunner, rule, IWorkspace.AVOID_UPDATE, monitor);
		}
	}
	
	public static abstract class WriteTextFileOperation extends AbstractFileOperation {
		
		protected WriteTextFileOperation() {
			super();
		}
		
		protected abstract void writeImpl(IProgressMonitor monitor) throws CoreException, UnsupportedEncodingException, IOException;
		
		@Override
		protected void runInEnv(final IProgressMonitor monitor) throws CoreException, OperationCanceledException {
			try {
				monitor.beginTask("Writing to "+getFileLabel(), 100);
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				
				writeImpl(monitor);
			}
			catch (final UnsupportedEncodingException e) {
				throw new CoreException(new Status(IStatus.ERROR, StatetCore.PLUGIN_ID, ICommonStatusConstants.IO_ERROR,
						"The selected charset is unsupported on your system.", e));
			}
			catch (final IOException e) {
				throw new CoreException(new Status(Status.ERROR, StatetCore.PLUGIN_ID, ICommonStatusConstants.IO_ERROR,
						"Error while writing to file.", e));
			}
			finally {
				monitor.done();
			}
		}
	}
	
	public static WriteTextFileOperation createWriteTextFileOp(final String content, final Object file) {
		if (file instanceof IFile) {
			return WORKSPACE_UTIL.createWriteTextFileOp(content, file);
		}
		else if (file instanceof IFileStore) {
			final IFileStore efsFile = (IFileStore) file;
			final IFile iFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(efsFile.toURI().getPath()));
			if (iFile != null) {
				return WORKSPACE_UTIL.createWriteTextFileOp(content, iFile);
			}
			return EFS_UTIL.createWriteTextFileOp(content, efsFile);
		}
		throw new IllegalArgumentException("Unknown file object.");
	}
	
	
	public static interface ReaderAction {
		
		void run(BufferedReader reader, IProgressMonitor monitor) throws IOException, CoreException;
		
	}
	
	public static abstract class ReadTextFileOperation extends AbstractFileOperation {
		
		protected abstract FileInput getInput(IProgressMonitor monitor) throws CoreException, IOException;
		protected abstract ReaderAction getAction();
		
		public ReadTextFileOperation() {
			super();
		}
		
		@Override
		protected void runInEnv(final IProgressMonitor monitor) throws CoreException {
			FileInput fi = null;
			BufferedReader reader = null;
			try {
				monitor.beginTask(null, 100);
				final String fileLabel = getFileLabel();
				monitor.subTask("Opening "+fileLabel+"...");
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				
				fi = getInput(new SubProgressMonitor(monitor, 10));
				fi.setEncoding(fCharset, fForceCharset);
				
				reader = new BufferedReader(fi.getReader());
				monitor.worked(5);
				monitor.subTask("Reading "+fileLabel+"...");
				getAction().run(reader, new SubProgressMonitor(monitor, 80));
			} catch (final UnsupportedEncodingException e) {
				throw new CoreException(new Status(IStatus.ERROR, StatetCore.PLUGIN_ID, ICommonStatusConstants.IO_ERROR,
						"The selected charset is unsupported on your system.", e));
			} catch (final IOException e) {
				throw new CoreException(new Status(Status.ERROR, StatetCore.PLUGIN_ID, ICommonStatusConstants.IO_ERROR,
						"Error while reading the file.", e));
			}
			finally {
				FileUtilProvider.saveClose(reader);
				FileUtilProvider.saveClose(fi);
				monitor.done();
			}
		}
	}
	
	/**
	 * @param file
	 * @return
	 * @throws CoreException
	 */
	public static ReadTextFileOperation createReadTextFileOp(final ReaderAction action, final Object file) {
		if (file instanceof IFile) {
			return WORKSPACE_UTIL.createReadTextFileOp(action, file);
		}
		else if (file instanceof IFileStore) {
			final IFileStore efsFile = (IFileStore) file;
			final IFile iFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(efsFile.toURI().getPath()));
			if (iFile != null) {
				return WORKSPACE_UTIL.createReadTextFileOp(action, iFile);
			}
			return EFS_UTIL.createReadTextFileOp(action, efsFile);
		}
		throw new IllegalArgumentException("Unknown file object.");
	}
	
	
	public static long getTimeStamp(final Object file, final IProgressMonitor monitor) throws CoreException {
		if (file instanceof IFile) {
			return WORKSPACE_UTIL.getTimeStamp(file, monitor);
		}
		else if (file instanceof IFileStore) {
			return EFS_UTIL.getTimeStamp(file, monitor);
		}
		throw new IllegalArgumentException("Unknown file object.");
	}
	
}
