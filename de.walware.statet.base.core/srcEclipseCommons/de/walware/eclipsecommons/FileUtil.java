/*******************************************************************************
 * Copyright (c) 2006-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;

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
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;

import de.walware.statet.base.core.StatetCore;

import de.walware.eclipsecommons.internal.fileutil.EFSUtilImpl;
import de.walware.eclipsecommons.internal.fileutil.WorkspaceUtilImpl;


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
	public static IFileStore getLocalFileStore(String s) throws CoreException {
		
		if (s.length() > 0) {
			IFileSystem localFS = EFS.getLocalFileSystem();
			if (s.startsWith(EFS.SCHEME_FILE)) {
				return localFS.getStore(URIUtil.toURI(s.substring(localFS.getScheme().length())));
			}
			IPath path = Path.fromOSString(s);
			if (path.isUNC()) {
				return localFS.getStore(URIUtil.toURI(path));
			}
			if (path.isAbsolute()) {
				String device = path.getDevice();
				if (device == null || device.length() <= 2) {
					return localFS.getStore(URIUtil.toURI(path));
				}
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, StatetCore.PLUGIN_ID, "No local filesystem resource."));
	}
	
	public static IFileStore expandToLocalFileStore(String location, String child) throws CoreException {
		
		IStringVariableManager variables = VariablesPlugin.getDefault().getStringVariableManager();
		String expanded = variables.performStringSubstitution(location);
		IFileStore localFileStore = getLocalFileStore(expanded);
		if (child != null) {
			return localFileStore.getChild(child);
		}
		return localFileStore;
	}
	
	public static IPath expandToLocalPath(String location, String child) throws CoreException {
		
		IFileStore fileStore = expandToLocalFileStore(location, child);
		return URIUtil.toPath(fileStore.toURI());
	}
	
	
/*-- File Operations --*/
	public static abstract class AbstractFileOperation {
		
		protected String fPluginID;
		protected int fMode = EFS.NONE;
		
		protected String fCharset = UTF_8;
		protected boolean fForceCharset = false;
		
		protected AbstractFileOperation(String pluginID) {
			
			fPluginID = pluginID;
		}
		
		public void setFileOperationMode(int mode) {
			
			fMode = mode;
		}
		
		public void setCharset(String charset, boolean forceCharset) {
			
			fCharset = charset;
			fForceCharset = forceCharset;
		}
		
		protected abstract String getFileLabel();
		
		public void doOperation(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
			
			runInEnv(monitor);
		}
		
		protected abstract void runInEnv(IProgressMonitor monitor) throws CoreException, OperationCanceledException;

		protected void runAsWorkspaceRunnable(IProgressMonitor monitor, IResource scope) throws CoreException, OperationCanceledException {
			
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			ISchedulingRule rule = workspace.getRuleFactory().createRule(scope);
			IWorkspaceRunnable workspaceRunner = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					runInEnv(monitor);
				}
			};
			workspace.run(workspaceRunner, rule, IWorkspace.AVOID_UPDATE, monitor);
		}
	}
	
	public static abstract class WriteTextFileOperation extends AbstractFileOperation {
		
		protected WriteTextFileOperation(String pluginID) {
			
			super(pluginID);
		}

		protected abstract void writeImpl(IProgressMonitor monitor) throws CoreException, UnsupportedEncodingException, IOException;

		@Override
		protected void runInEnv(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
			
			try {
				monitor.beginTask("Writing to "+getFileLabel(), 100);
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}

				writeImpl(monitor);
			}
			catch (UnsupportedEncodingException e) {
				throw new CoreException(new Status(IStatus.ERROR, fPluginID, 0,
						"The selected charset is unsupported on your system.", e));
			}
			catch (IOException e) {
				throw new CoreException(new Status(Status.ERROR, fPluginID, 0,
						"Error while writing to file.", e));
			}
			finally {
				monitor.done();
			}
		}
	}
	
	public static WriteTextFileOperation createWriteTextFileOp(String content, Object file, String pluginID) {
		
		if (file instanceof IFile) {
			return WORKSPACE_UTIL.createWriteTextFileOp(content, (IFile) file, pluginID); 
		}
		else if (file instanceof IFileStore) {
			IFileStore efsFile = (IFileStore) file;
			IFile iFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(efsFile.toURI().getPath()));
			if (iFile != null) {
				return WORKSPACE_UTIL.createWriteTextFileOp(content, iFile, pluginID);
			}
			return EFS_UTIL.createWriteTextFileOp(content, (IFileStore) file, pluginID);
		}
		throw new IllegalArgumentException("Unknown file object.");
	}


	public static interface ReaderAction {
		
		void run(BufferedReader reader, IProgressMonitor monitor) throws IOException;
	}
	
	public static abstract class ReadTextFileOperation extends AbstractFileOperation {
		
		protected abstract FileInput getInput(IProgressMonitor monitor) throws CoreException, IOException;
		protected abstract ReaderAction getAction();
		
		public ReadTextFileOperation(String pluginID) {
			
			super(pluginID);
		}

		@Override
		protected void runInEnv(IProgressMonitor monitor) throws CoreException {
			
			FileInput fi = null;
			BufferedReader reader = null;
			try {
				monitor.beginTask(null, 100);
				String fileLabel = getFileLabel();
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
			} catch (UnsupportedEncodingException e) {
				throw new CoreException(new Status(IStatus.ERROR, fPluginID, 0,
						"The selected charset is unsupported on your system.", e));
			} catch (IOException e) {
				throw new CoreException(new Status(Status.ERROR, fPluginID, 0,
						"Error while reading the file.", e));
			}
			finally {
				saveClose(reader);
				saveClose(fi);
				monitor.done();
			}
		}
	}
	
	/**
	 * @param file
	 * @return
	 * @throws CoreException 
	 */
	public static ReadTextFileOperation createReadTextFileOp(final ReaderAction action, Object file, 
			String pluginID) { 
		
		if (file instanceof IFile) {
			return WORKSPACE_UTIL.createReadTextFileOp(action, (IFile) file, pluginID); 
		}
		else if (file instanceof IFileStore) {
			IFileStore efsFile = (IFileStore) file;
			IFile iFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(efsFile.toURI().getPath()));
			if (iFile != null) {
				return WORKSPACE_UTIL.createReadTextFileOp(action, iFile, pluginID);
			}
			return EFS_UTIL.createReadTextFileOp(action, efsFile, pluginID);
		}
		throw new IllegalArgumentException("Unknown file object.");
	}
	
	
	protected static class FileInput implements Closeable {
		
		private String fEncoding;
		private String fDefaultEncoding;
		private InputStream fStream;
		
		public FileInput(InputStream input, String expliciteCharsetHint) throws IOException, CoreException {
			
			fStream = input;
			if (expliciteCharsetHint != null) {
				fDefaultEncoding = expliciteCharsetHint;
			}
			else {
				read(input);
			}
			fEncoding = (fDefaultEncoding != null) ? fDefaultEncoding : FileUtil.UTF_8;
		}
		
		void read(InputStream input) throws IOException {
			
			try {
				int n = 3;
				byte[] bytes = new byte[n];
				int readed = input.read(bytes, 0, n);
				if (readed == 0) {
					return;
				}
				int next = 0;
				if (startsWith(bytes, IContentDescription.BOM_UTF_8)) {
					next = IContentDescription.BOM_UTF_8.length;
					fDefaultEncoding = FileUtil.UTF_8;
				}
				else if (startsWith(bytes, IContentDescription.BOM_UTF_16BE)) {
					next = IContentDescription.BOM_UTF_16BE.length;
					fDefaultEncoding = FileUtil.UTF_16_BE;
				}
				else if (startsWith(bytes, IContentDescription.BOM_UTF_16LE)) {
					next = IContentDescription.BOM_UTF_16LE.length;
					fDefaultEncoding = FileUtil.UTF_16_LE; 
				}
				if (readed-next > 0) {
					fStream = new SequenceInputStream(new ByteArrayInputStream(
							bytes, next, readed-next), input);
				}
			}
			catch (IOException e) {
				saveClose(input);
				throw e;
			}
		}
		
		private boolean startsWith(byte[] array, byte[] start) {
			
			for (int i = 0; i < start.length; i++) {
				if (array[i] != start[i]) {
					return false;
				}
			}
			return true;
		}
		
		public void setEncoding(String encoding, boolean force) {
			
			if (encoding == null && fDefaultEncoding != null) {
				fEncoding = fDefaultEncoding;
			}
			if (force || fDefaultEncoding == null) {
				fEncoding = encoding;
			}
		}
		
		public void close() throws IOException {
			
			if (fStream != null) {
				fStream.close();
			}
		}
		
		public String getDefaultCharset() {
			
			return fDefaultEncoding;
		}
		
		public Reader getReader() throws UnsupportedEncodingException {
			
			return new InputStreamReader(fStream, fEncoding);
		}

		
	}
	
	protected static void saveClose(Closeable stream) {
		
		if (stream != null) {
			try {
				stream.close();
			}
			catch (IOException e) {
				;
			}
		}
	}
}
