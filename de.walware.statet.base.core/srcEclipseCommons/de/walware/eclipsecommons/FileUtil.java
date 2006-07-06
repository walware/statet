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

package de.walware.eclipsecommons;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.jobs.ISchedulingRule;


/**
 * 
 */
public class FileUtil {

	
	private static InputStream EMPTY_INPUT = new ByteArrayInputStream(new byte[0]);
	public static String UTF_8 = "UTF-8";
	public static String UTF_16_BE = "UTF-16BE";
	public static String UTF_16_LE = "UTF-16LE";

	
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
	
	public static WriteTextFileOperation createWriteTextFileOp(final String content, final IFileStore file, final String pluginID) {
		
		IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(file.toURI().getPath()));
		if (ifile != null) {
			return (createWriteTextFileOp(content, ifile, pluginID));
		}
		return new WriteTextFileOperation(pluginID) {
			@Override
			protected String getFileLabel() {
				return FileUtil.getFileLabel(file);
			}

			@Override
			protected void writeImpl(IProgressMonitor monitor) throws CoreException, IOException {
				Writer out = null;
				try {
					boolean exists = file.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 5)).exists();
					if (exists && (fMode & (EFS.OVERWRITE & EFS.APPEND)) == 0) {
						throw new CoreException(new Status(IStatus.ERROR, pluginID, 0,
								"The file already exists.", null));
					}
					if (exists && (fMode & EFS.APPEND) != 0 && !fForceCharset) {
						try {
							InputStream raw = file.openInputStream(EFS.NONE, new SubProgressMonitor(monitor, 5));
							FileInput fi = new FileInput(raw, null);
							fi.close();
							if (fi.fDefaultEncoding != null) {
								fCharset = fi.fDefaultEncoding;
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
	public static WriteTextFileOperation createWriteTextFileOp(final String content, final IFile file, String pluginID) {
		
		return new WriteTextFileOperation(pluginID) {
			@Override
			protected String getFileLabel() {
				return FileUtil.getFileLabel(file);
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
	public static WriteTextFileOperation createWriteTextFileOp(String content, Object file, String pluginID) {
		
		if (file instanceof IFile) {
			return createWriteTextFileOp(content, (IFile) file, pluginID); 
		}
		else if (file instanceof IFileStore) {
			return createWriteTextFileOp(content, (IFileStore) file, pluginID);
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
			return createReadTextFileOp(action, (IFile) file, pluginID); 
		}
		else if (file instanceof IFileStore) {
			return createReadTextFileOp(action, (IFileStore) file, pluginID);
		}
		throw new IllegalArgumentException("Unknown file object.");
	}
	
	public static ReadTextFileOperation createReadTextFileOp(final ReaderAction action, final IFile file, 
			String pluginID) {
		
		return new ReadTextFileOperation(pluginID) {
			@Override
			protected String getFileLabel() {
				return FileUtil.getFileLabel(file);
			}

			@Override
			protected FileInput getInput(IProgressMonitor monitor) throws CoreException, IOException {
				try {
					InputStream raw = file.getContents(true);
					return new FileInput(raw, file);
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
	
	public static ReadTextFileOperation createReadTextFileOp(final ReaderAction action, final IFileStore file,
			String pluginID) { 
		
		IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(file.toURI().getPath()));
		if (ifile != null) {
			return createReadTextFileOp(action, ifile, pluginID);
		}

		return new ReadTextFileOperation(pluginID) {
			@Override
			protected String getFileLabel() {
				return FileUtil.getFileLabel(file);
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
	
	
	private static class FileInput implements Closeable {
		
		private String fEncoding;
		private String fDefaultEncoding;
		private InputStream fStream;
		
		FileInput(InputStream input, IFile ifile) throws IOException, CoreException {
			
			fStream = input;
			read(input);
			if (ifile != null) {
				String explicit = ifile.getCharset(false);
				if (explicit != null) {
					fDefaultEncoding = explicit;
				}
			}
			fEncoding = (fDefaultEncoding != null) ? fDefaultEncoding : UTF_8;
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
					fDefaultEncoding = UTF_8;
				}
				else if (startsWith(bytes, IContentDescription.BOM_UTF_16BE)) {
					next = IContentDescription.BOM_UTF_16BE.length;
					fDefaultEncoding = UTF_16_BE;
				}
				else if (startsWith(bytes, IContentDescription.BOM_UTF_16LE)) {
					next = IContentDescription.BOM_UTF_16LE.length;
					fDefaultEncoding = UTF_16_LE; 
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
		
		public Reader getReader() throws UnsupportedEncodingException {
			
			return new InputStreamReader(fStream, fEncoding);
		}
		
	}
	
	private static void saveClose(Closeable stream) {
		
		if (stream != null) {
			try {
				stream.close();
			}
			catch (IOException e) {
				;
			}
		}
	}
	
	private static String getFileLabel(IFile ifile) {
		
		return "'"+ifile.getFullPath().makeRelative().toString()+"' (workspace)";
	}
	
	private static String getFileLabel(IFileStore file) {
		
		IFileSystem system = file.getFileSystem();
		if (system.equals(EFS.getLocalFileSystem())) {
			return "'"+file.toString()+"' (local file)";
		}
		return "'"+file.toURI().toString()+"'";

	}
	
}
