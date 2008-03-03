/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.processing;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.texlipse.TexPathConfig;
import net.sourceforge.texlipse.Texlipse;
import net.sourceforge.texlipse.builder.Builder;
import net.sourceforge.texlipse.builder.BuilderRegistry;
import net.sourceforge.texlipse.builder.TexlipseBuilder;
import net.sourceforge.texlipse.viewer.ViewerConfiguration;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.eclipsecommons.ICommonStatusConstants;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.IRequireSynch;
import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.Queue;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolWorkspace;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.r.internal.sweave.Messages;
import de.walware.statet.r.internal.sweave.SweavePlugin;
import de.walware.statet.r.nico.IBasicRAdapter;


class RweaveTexTool implements Runnable, IProcess {
	
	
	public static final int NO = 0;
	public static final int AUTO = 1;
	public static final int EXPLICITE = 2;
	
	
	private class R implements IToolRunnable<IBasicRAdapter> {
		
		
		boolean finished = false;
		
		
		public void changed(final int event) {
			if (event == Queue.ENTRIES_DELETE || event == Queue.ENTRIES_ABANDONED) {
				fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, -1,
						Messages.RweaveTexCreation_Sweave_Task_info_Canceled_message, null));
				continueAfterR();
			}
		}
		
		public String getLabel() {
			return NLS.bind(Messages.RweaveTexCreation_Sweave_Task_label, fSweaveFile.getName());
		}
		
		public SubmitType getSubmitType() {
			return SubmitType.TOOLS;
		}
		
		public String getTypeId() {
			return "r/sweave/commands"; //$NON-NLS-1$
		}
		
		public void run(final IBasicRAdapter tools, final IProgressMonitor monitor)
				throws InterruptedException, CoreException {
			try {
				tools.submitToConsole(fSweaveCommands, monitor);
				if (tools instanceof IRequireSynch) {
					((IRequireSynch) tools).synch(monitor);
				}
				// TODO look for output= parameter
				final ToolWorkspace workspace = tools.getWorkspaceData();
				workspace.refresh(monitor);
				final IStatus status = setWorkingDir(workspace.getWorkspaceDir(), null, true);
				if (status.getSeverity() > IStatus.OK) {
					fStatus.add(status);
				}
			}
			catch (final CoreException e) {
				fStatus.add(e.getStatus());
				throw e;
			}
			finally {
				finished = true;
				continueAfterR();
			}
			
		}
		
	}
	
	private String fProfileName;
	private String fName;
	private MultiStatus fStatus;
	private IProgressMonitor fMonitor;
	private Thread fThread;
	private IWorkbenchPage fWorkbenchPage;
	private ILaunch fLaunch;
	
	private IFile fSweaveFile;
	private IContainer fWorkingFolderInWorkspace;
	private IFileStore fWorkingFolder;
	
	boolean fRunSweave;
	String fSweaveCommands;
	ILaunchConfiguration fSweaveConfig;
	
	boolean fRunTex;
	private IFile fTexFile;
	String fConfiguredOutputDir;
	int fTexOpenEditor = 0;
	int fTexBuilderId = -1;
	private TexPathConfig fTexPathConfig;
	
	int fRunPreview;
	ViewerConfiguration fPreviewConfig;
	
	private int fExitValue = 0;
	private Map<String, String> fAttributes;
	
	
	public RweaveTexTool(final String name, final ILaunch launch, final IWorkbenchPage workbenchPage, final IFile file) {
		fProfileName = name;
		fName = NLS.bind(Messages.RweaveTexCreation_label, name, file.getName());
		fWorkbenchPage = workbenchPage;
		fLaunch = launch;
		fSweaveFile = file;
		fMonitor = new NullProgressMonitor();
		fStatus = new MultiStatus(SweavePlugin.PLUGIN_ID, ICommonStatusConstants.LAUNCHING,
				NLS.bind(Messages.RweaveTexCreation_Status_label, fSweaveFile.getName()), null);
	}
	
	public Thread createThread() {
		final Thread thread = new Thread(this, fName);
		thread.setDaemon(true);
		fThread = thread;
		return thread;
	}
	
	public Job createJob() {
		final Job job = new Job(fName) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				fMonitor = monitor;
				fThread = Thread.currentThread();
				RweaveTexTool.this.run();
				if (fStatus.getSeverity() == IStatus.CANCEL) {
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
			
			@Override
			protected void canceling() {
				try {
					RweaveTexTool.this.terminate();
				} catch (final DebugException e) {
				}
			}
		};
		return job;
	}
	
	
	public IStatus setWorkingDir(final IFileStore efsFolder, final IContainer workspaceFolder, final boolean synch) {
		fWorkingFolder = efsFolder;
		fWorkingFolderInWorkspace = workspaceFolder;
		if (synch) {
			if (fWorkingFolder == null && fWorkingFolderInWorkspace != null) {
				fWorkingFolder = EFS.getLocalFileSystem().getStore(fWorkingFolderInWorkspace.getLocation());
			}
			else if (fWorkingFolder != null && fWorkingFolderInWorkspace == null) {
				final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				final IContainer[] found = root.findContainersForLocationURI(fWorkingFolder.toURI());
				if (found.length > 0) {
					fWorkingFolderInWorkspace = found[0];
				}
			}
		}
		if (fWorkingFolderInWorkspace == null) {
			fExitValue = 11;
			return new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, -1,
					Messages.RweaveTexCreation_Tex_error_MustBeInWorkspace_message, null);
		}
		return Status.OK_STATUS;
	}
	
	public void run() {
		try {
			fMonitor.beginTask('\'' + fProfileName + '\'', 1000);
			if (fMonitor.isCanceled()) {
				fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, Messages.RweaveTexCreation_info_Canceled_message));
				return;
			}
			
			doWeave(new SubProgressMonitor(fMonitor, 300));
			if (fMonitor.isCanceled()) {
				fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, Messages.RweaveTexCreation_info_Canceled_message));
			}
			if (fStatus.getSeverity() >= IStatus.ERROR) {
				return;
			}
			
			// Prepare TeX processing
			String baseName = fSweaveFile.getName();
			final int idx = baseName.lastIndexOf('.');
			if (idx >= 0) {
				baseName = baseName.substring(0, idx);
			}
			final String texFileName = baseName + ".tex"; //$NON-NLS-1$
			fTexFile = fWorkingFolderInWorkspace.getFile(new Path(texFileName));
			if (fTexFile.exists() && fTexFile.getType() == IResource.FILE) {
				fTexFile.deleteMarkers(TexlipseBuilder.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
				fTexFile.deleteMarkers(TexlipseBuilder.LAYOUT_WARNING_TYPE, true, IResource.DEPTH_INFINITE);
			}
			fMonitor.worked(5);
			refreshDir(fWorkingFolderInWorkspace, new SubProgressMonitor(fMonitor, 5));
			if (fMonitor.isCanceled()) {
				fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, Messages.RweaveTexCreation_info_Canceled_message));
			}
			if (fStatus.getSeverity() >= IStatus.ERROR) {
				return;
			}
			if (!fTexFile.exists()) {
				fExitValue = 199;
				fStatus.add(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, -1,
						NLS.bind(Messages.RweaveTexCreation_Tex_error_NotFound_message, fTexFile.getFullPath().toString()), null));
				return;
			}
			
			doProcessTex(baseName, new SubProgressMonitor(fMonitor, 300));
			if (fMonitor.isCanceled()) {
				fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, Messages.RweaveTexCreation_info_Canceled_message));
			}
			if (fStatus.getSeverity() >= IStatus.ERROR) {
				return;
			}
			
			doOpenOutput(new SubProgressMonitor(fMonitor, 100));
			if (fStatus.getSeverity() >= IStatus.ERROR) {
				return;
			}
		}
		catch (final CoreException e) {
			fStatus.add(e.getStatus());
		}
		catch (final Throwable e) {
			fStatus.add(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, ICommonStatusConstants.LAUNCHING,
					Messages.RweaveTexCreation_error_UnexpectedError_message, e));
		}
		finally {
			exit();
			fMonitor.done();
			fMonitor = null;
			DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {
				new DebugEvent(this, DebugEvent.TERMINATE),
			});
		}
	}
	
	private void doWeave(final IProgressMonitor monitor) { // 1xx
		monitor.beginTask("", 100); //$NON-NLS-1$
		if (fSweaveCommands != null) { // 11x
			try {
	//			RCodeLaunchRegistry.runRCodeDirect(RUtil.LINE_SEPARATOR_PATTERN.split(fSweaveCommands), false);
				final ToolProcess rProcess = NicoUI.getToolRegistry().getActiveToolSession(fWorkbenchPage).getProcess();
				if (rProcess == null) {
					NicoUITools.accessTool("R", rProcess); // throws CoreException //$NON-NLS-1$
				}
				
				if (fRunSweave) {
					monitor.subTask(Messages.RweaveTexCreation_Sweave_InConsole_label);
					final ToolController rController = NicoUITools.accessTool("R", rProcess); //$NON-NLS-1$
					final R rTask = new R();
					monitor.worked(10);
					final IStatus submitStatus = rController.submit(rTask);
					if (submitStatus.getSeverity() > IStatus.OK) {
						fStatus.add(submitStatus);
						if (submitStatus.getSeverity() >= IStatus.ERROR) {
							abort(null, 112, monitor);
							return;
						}
					}
					while (true) {
						synchronized (this) {
							try {
								if (monitor.isCanceled()) {
									rController.getProcess().getQueue().removeElements(new IToolRunnable[] { rTask });
									break;
								}
								if (rTask.finished) {
									break;
								}
								wait();
							}
							catch (final InterruptedException e) {
								Thread.interrupted();
							}
						}
					}
					if (fStatus.getSeverity() >= IStatus.ERROR) {
						abort(null, 113, monitor);
						return;
					}
				}
				else { // we need the working directory
					final IStatus status = setWorkingDir(rProcess.getWorkspaceData().getWorkspaceDir(), null, true);
					if (status.getSeverity() > IStatus.OK) {
						fStatus.add(status);
					}
				}
			}
			catch (final CoreException e) {
				abort(e.getStatus(), 110, monitor);
				return;
			}
		}
		else if (fSweaveConfig != null) { // 12x
			try {
				if (fRunSweave) {
					monitor.subTask(Messages.RweaveTexCreation_Sweave_RCmd_label);
					final ILaunchConfigurationDelegate delegate = RweaveTexCreationDelegate.getRunDelegate(fSweaveConfig);
					delegate.launch(fSweaveConfig, ILaunchManager.RUN_MODE, fLaunch, new SubProgressMonitor(monitor, 30));
					monitor.worked(60);
					final IProcess[] processes = fLaunch.getProcesses();
					if (processes.length == 0) {
						throw new IllegalStateException();
					}
					final IProcess sweaveProcess = processes[processes.length-1];
					if (!sweaveProcess.isTerminated()) {
						throw new IllegalStateException();
					}
					final int exitValue = sweaveProcess.getExitValue();
					if (exitValue != 0) {
						abort(IStatus.CANCEL, NLS.bind(Messages.RweaveTexCreation_Sweave_RCmd_error_Found_message, exitValue), null,
								121, monitor);
						return;
					}
				}
			}
			catch (final CoreException e) {
				abort(e.getStatus(), 120, monitor);
				return;
			}
		}
		monitor.done();
	}
	
	private void continueAfterR() {
		synchronized (this) {
			notifyAll();
		}
	}
	
	private void doProcessTex(final String baseName, final IProgressMonitor monitor) { // 2xx
		monitor.beginTask("", 100); //$NON-NLS-1$
		if (fTexOpenEditor == TexTab.OPEN_ALWAYS) {
			openEditor(fTexFile);
			monitor.worked(5);
		}
		
		final Builder builder = BuilderRegistry.get(fTexBuilderId);
		if (builder != null) { // 21x
			final IContainer outputDir;
			try {
				outputDir = TexPathConfig.resolveDirectory(fConfiguredOutputDir, fTexFile, fSweaveFile);
			}
			catch (final CoreException e) {
				abort(IStatus.ERROR, Messages.RweaveTexCreation_Tex_error_OutputDir_message, e, 
						201, monitor);
				return;
			}
			fTexPathConfig = new TexPathConfig(fTexFile, outputDir, builder.getOutputFormat());
			if (fRunTex) {
				monitor.subTask(Messages.RweaveTexCreation_Tex_label);
				Texlipse.getViewerManager().closeDocInViewer(fTexPathConfig);
				try {
					builder.reset(new SubProgressMonitor(monitor, 60, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
					builder.build(fTexPathConfig);
				}
				catch (final OperationCanceledException e) {
					abort(IStatus.CANCEL, Messages.RweaveTexCreation_info_Canceled_message, e,
							211, monitor);
					return;
				}
				catch (final CoreException e) {
					abort(e.getStatus(), 210, monitor);
					return;
				}
				finally {
					refreshDir(fWorkingFolderInWorkspace, monitor.isCanceled() ? null : new SubProgressMonitor(monitor, 5));
					if (!fWorkingFolderInWorkspace.equals(outputDir)) {
						refreshDir(outputDir, monitor.isCanceled() ? null : new SubProgressMonitor(monitor, 5));
					}
				}
			}
		}
		else {
			fStatus.add(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, -1,
					Messages.RweaveTexCreation_Tex_error_BuilderNotConfigured_message, null));
		}
		
		if (fStatus.getSeverity() < IStatus.ERROR) {
			try { // 28x
				if (fRunTex && fTexOpenEditor > TexTab.OPEN_ALWAYS && fTexFile.findMaxProblemSeverity(IMarker.PROBLEM, true, IResource.DEPTH_ZERO) >= fTexOpenEditor) {
					openEditor(fTexFile);
					monitor.worked(5);
				}
			}
			catch (final CoreException e) {
				abort(e.getStatus(), 280, monitor);
				return;
			}
		}
		
		monitor.done();
	}
	
	private void doOpenOutput(final IProgressMonitor monitor) { // 3xx
		if (fRunPreview > NO) {
			if (!fTexPathConfig.getOutputFile().exists()) {
				abort((fRunPreview == EXPLICITE) ? IStatus.ERROR : IStatus.INFO,
						NLS.bind(Messages.RweaveTexCreation_Output_error_NotFound_message, fTexPathConfig.getOutputFile().getFullPath().toString()), null,
						301, monitor);
				return;
			}
			try {
				if (fRunPreview == AUTO && fTexFile.findMaxProblemSeverity(IMarker.PROBLEM, true, IResource.DEPTH_ZERO) >= IMarker.SEVERITY_ERROR) {
					abort(IStatus.CANCEL, Messages.RweaveTexCreation_Output_info_SkipBecauseTex_message, null,
							302, monitor);
					return;
				}
			} catch (final CoreException e) {
				abort(e.getStatus(), 303, monitor);
				return;
			}
			if (fPreviewConfig != null) {
				Texlipse.getViewerManager().openDocInViewer(fTexPathConfig, fPreviewConfig);
			}
			else {
				openEditor(fTexPathConfig.getOutputFile());
			}
			monitor.done();
//			final ILaunchConfigurationDelegate delegate = getRunDelegate(fPreviewConfig);
//			delegate.launch(fPreviewConfig, ILaunchManager.RUN_MODE, fLaunch, new SubProgressMonitor(fMonitor, 10));
		}
	}
	
	private void abort(final int severity, final String message, final Throwable cause, final int exitValue, final IProgressMonitor monitor) {
		abort(new Status(severity, SweavePlugin.PLUGIN_ID, -1, message, cause), exitValue, monitor);
	}
	
	private void abort(final IStatus status, final int exitValue, final IProgressMonitor monitor) {
		if (status != null) {
			fStatus.add(status);
		}
		fExitValue = exitValue;
		monitor.done();
	}
	
	private void refreshDir(final IResource resource, final IProgressMonitor monitor) {
		try {
			resource.refreshLocal(IResource.DEPTH_ONE, monitor);
		}
		catch (final OperationCanceledException e) {
			fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, -1,
					Messages.RweaveTexCreation_info_Canceled_message, e));
		}
		catch (final CoreException e) {
			fStatus.add(e.getStatus());
		}
	}
	
	private void openEditor(final IFile file) {
		UIAccess.getDisplay().syncExec(new Runnable() {
			public void run() {
				try {
					IDE.openEditor(fWorkbenchPage, file);
				} catch (final PartInitException e) {
					SweavePlugin.logError(-1, "An error occured when opening the document.", e); //$NON-NLS-1$
				}
			}
		});
	}
	
	private void exit() {
		if (fStatus.getSeverity() == 0) {
			return;
		}
		fStatus.add(new Status(IStatus.INFO, SweavePlugin.PLUGIN_ID, "Exit code = " + fExitValue)); //$NON-NLS-1$
		if (fStatus.getSeverity() == IStatus.ERROR) {
			StatusManager.getManager().handle(fStatus, StatusManager.LOG | StatusManager.SHOW);
			return;
		}
		StatusManager.getManager().handle(fStatus, StatusManager.LOG);
	}
	
	
	public Object getAdapter(final Class adapter) {
		return null;
	}
	
	public void setAttribute(final String key, final String value) {
		if (fAttributes == null) {
			initAttributes();
		}
		fAttributes.put(key, value);
	}
	
	private synchronized void initAttributes() {
		if (fAttributes == null) {
			fAttributes = new HashMap<String, String>();
		}
	}
	
	public String getAttribute(final String key) {
		if (fAttributes != null) {
			return fAttributes.get(key);
		}
		return null;
	}
	
	public int getExitValue() throws DebugException {
		if (!isTerminated()) {
			throw new DebugException(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, -1,
					"The process has not yet terminated.", null)); //$NON-NLS-1$
		}
		return fExitValue;
	}
	
	public String getLabel() {
		return fName;
	}
	
	public ILaunch getLaunch() {
		return fLaunch;
	}
	
	public IStreamsProxy getStreamsProxy() {
		return null;
	}
	
	public boolean canTerminate() {
		return (fMonitor != null);
	}
	
	public void terminate() throws DebugException {
		final IProgressMonitor monitor = fMonitor;
		final Thread thread = fThread;
		if (monitor != null) {
			monitor.setCanceled(true);
		}
		if (thread != null) {
			thread.interrupt();
		}
	}
	
	public boolean isTerminated() {
		return (fMonitor == null);
	}
	
}
