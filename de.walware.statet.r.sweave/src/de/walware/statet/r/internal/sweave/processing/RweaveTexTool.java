/*******************************************************************************
 * Copyright (c) 2008-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.processing;

import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.BUILDTEX_TYPE_ECLIPSE;
import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.BUILDTEX_TYPE_RCONSOLE;
import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.SWEAVE_TYPE_RCMD;
import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.SWEAVE_TYPE_RCONSOLE;
import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.VARNAME_LATEX_FILE;
import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.VARNAME_OUTPUT_FILE;
import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.VARNAME_SWEAVE_FILE;

import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
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

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.variables.core.VariableText;
import de.walware.ecommons.variables.core.VariableText.LocationProcessor;

import de.walware.statet.nico.core.runtime.IRequireSynch;
import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.Queue;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolWorkspace;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;

import net.sourceforge.texlipse.TexPathConfig;
import net.sourceforge.texlipse.Texlipse;
import net.sourceforge.texlipse.builder.AbstractBuilder;
import net.sourceforge.texlipse.builder.Builder;
import net.sourceforge.texlipse.builder.TexlipseBuilder;
import net.sourceforge.texlipse.viewer.ViewerConfiguration;

import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.internal.sweave.Messages;
import de.walware.statet.r.internal.sweave.SweavePlugin;
import de.walware.statet.r.nico.RTool;


class RweaveTexTool implements Runnable, IProcess {
	
	
	public static final int NO = 0;
	public static final int AUTO = 1;
	public static final int EXPLICITE = 2;
	
	private static final int TICKS_PREPARER = 5;
	private static final int TICKS_RWEAVE = 30;
	private static final int TICKS_TEX = 30;
	private static final int TICKS_OPEN_TEX = 5;
	private static final int TICKS_OPEN_OUTPUT = 20;
	private static final int TICKS_REST = 10;
	
	
	private class R implements IToolRunnable {
		
		
		private boolean finished = false;
		
		
		R() {
		}
		
		public void changed(final int event, final ToolProcess process) {
			if (event == Queue.ENTRIES_DELETE || event == Queue.ENTRIES_ABANDONED) {
				fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, -1,
						Messages.RweaveTexProcessing_Sweave_Task_info_Canceled_message, null));
				continueAfterR();
			}
		}
		
		public String getLabel() {
			return NLS.bind(Messages.RweaveTexProcessing_Sweave_Task_label, fSweaveFile.getName());
		}
		
		public SubmitType getSubmitType() {
			return SubmitType.TOOLS;
		}
		
		public String getTypeId() {
			return "r/sweave/commands"; //$NON-NLS-1$
		}
		
		public void run(final IToolRunnableControllerAdapter r, final IProgressMonitor monitor)
				throws InterruptedException, CoreException {
			try {
				final ToolWorkspace workspace = r.getWorkspaceData();
				r.refreshWorkspaceData(0, monitor);
				updatePathInformations(r.getWorkspaceData());
				if (fStatus.getSeverity() >= IStatus.ERROR) {
					return;
				}
				if (beginSchedulingRule(monitor)) {
					final LocationProcessor processor = new LocationProcessor() {
						public String process(String path) throws CoreException {
							final IFileStore store = FileUtil.getFileStore(path);
							path = workspace.toToolPath(store);
							path = RUtil.escapeBackslash(path);
							return path;
						}
					};
					
					if (fRunSweave && fSweaveType == SWEAVE_TYPE_RCONSOLE) {
						monitor.subTask("Sweave"); //$NON-NLS-1$
						final SubMonitor progress = fProgress.newChild(TICKS_RWEAVE);
						progress.beginTask(Messages.RweaveTexProcessing_Sweave_InConsole_label, 100);
						
						try {
							fSweaveRCommands.performFinalStringSubstitution(processor);
						}
						catch (final NullPointerException e) {
							throw new CoreException(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID,
									Messages.RweaveTexProcessing_Sweave_error_ResourceVariable_message));
						}
						catch (final CoreException e) {
							throw new CoreException(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID,
									Messages.RweaveTexProcessing_Sweave_error_ResourceVariable_message + ' ' + e.getLocalizedMessage()));
						}
						final String[] commands = RUtil.LINE_SEPARATOR_PATTERN.split(fSweaveRCommands.getText());
						for (int i = 0; i < commands.length; i++) {
							r.submitToConsole(commands[i], monitor);
						}
						if (r instanceof IRequireSynch) {
							((IRequireSynch) r).synch(monitor);
						}
					}
					
					if (fRunTex && fTexType == BUILDTEX_TYPE_RCONSOLE) {
						monitor.subTask("LaTeX"); //$NON-NLS-1$
						
						doPrepareTex();
						if (monitor.isCanceled() || fProgress.isCanceled()) {
							fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, Messages.RweaveTexProcessing_info_Canceled_message));
						}
						if (fStatus.getSeverity() >= IStatus.ERROR) {
							return;
						}
						
						final SubMonitor progress = fProgress.newChild(TICKS_TEX);
						progress.beginTask(Messages.RweaveTexProcessing_Tex_label, 100);
						
						try {
							fTexRCommands.performFinalStringSubstitution(processor);
							progress.setWorkRemaining(90);
						}
						catch (final NullPointerException e) {
							throw new CoreException(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID,
									Messages.RweaveTexProcessing_Tex_error_ResourceVariable_message));
						}
						catch (final CoreException e) {
							throw new CoreException(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID,
									Messages.RweaveTexProcessing_Tex_error_ResourceVariable_message + ' ' + e.getLocalizedMessage()));
						}
						
						Texlipse.getViewerManager().closeDocInViewer(fTexPathConfig);
						
						final String[] commands = RUtil.LINE_SEPARATOR_PATTERN.split(fTexRCommands.getText());
						try {
							for (int i = 0; i < commands.length; i++) {
								r.submitToConsole(commands[i], monitor);
								progress.setWorkRemaining(90-80/commands.length*(i+1));
							}
							if (r instanceof IRequireSynch) {
								((IRequireSynch) r).synch(monitor);
							}
						}
						finally {
							finallyTex(progress);
						}
					}
				}
			}
			catch (final CoreException e) {
				fStatus.add(e.getStatus());
				throw e;
			}
			finally {
				if (fSchedulingRule != null) {
					Job.getJobManager().transferRule(fSchedulingRule, fThread);
				}
				finished = true;
				continueAfterR();
			}
			
		}
		
		public void updatePathInformations(final ToolWorkspace workspace) {
			final IFileStore wd = workspace.getWorkspaceDir();
			final IStatus status = setWorkingDir(wd, null, true);
			if (status.getSeverity() > IStatus.OK) {
				fStatus.add(status);
			}
		}
		
	}
	
	private final String fProfileName;
	private final String fName;
	private final MultiStatus fStatus;
	private final IWorkbenchPage fWorkbenchPage;
	private final ILaunch fLaunch;
	private Thread fThread;
	private ISchedulingRule fSchedulingRule;
	private SubMonitor fProgress;
	
	private final IFile fSweaveFile;
	private IContainer fWorkingFolderInWorkspace;
	private IFileStore fWorkingFolder;
	private String fBaseFileName;
	private String fTexFileExtension;
	
	boolean fRunSweave;
	private int fSweaveType;
	private VariableText fSweaveRCommands;
	private ILaunchConfiguration fSweaveConfig;
	
	private String fOutputFormat;
	private VariableText fOutputDir;
	
	boolean fRunTex;
	private IFile fTexFile;
	int fTexOpenEditor = 0;
	private int fTexType;
	private Builder fTexBuilder;
	private VariableText fTexRCommands;
	private TexPathConfig fTexPathConfig;
	
	int fRunPreview;
	ViewerConfiguration fPreviewConfig;
	
	private int fExitValue = 0;
	private Map<String, String> fAttributes;
	
	
	public RweaveTexTool(final String name, final ILaunch launch, final IWorkbenchPage workbenchPage, final IFile file) {
		fProfileName = name;
		fName = NLS.bind(Messages.RweaveTexProcessing_label, name, file.getName());
		fWorkbenchPage = workbenchPage;
		fLaunch = launch;
		fSweaveFile = file;
		fStatus = new MultiStatus(SweavePlugin.PLUGIN_ID, ICommonStatusConstants.LAUNCHING,
				NLS.bind(Messages.RweaveTexProcessing_Status_label, fSweaveFile.getName()), null);
	}
	
	public void setOutput(final VariableText directory, final String format) throws CoreException {
		fOutputDir = directory;
		fOutputDir.performInitialStringSubstitution(true);
		fOutputDir.set(VARNAME_SWEAVE_FILE, fSweaveFile.getFullPath().toString());
		fOutputFormat = format;
	}
	
	public void setSweave(final VariableText rCommands) throws CoreException {
		if (fSweaveType > 0 || rCommands == null) {
			throw new IllegalArgumentException();
		}
		fSweaveType = SWEAVE_TYPE_RCONSOLE;
		fSweaveRCommands = rCommands;
		fSweaveRCommands.performInitialStringSubstitution(true);
		fSweaveRCommands.set(VARNAME_SWEAVE_FILE, fSweaveFile.getFullPath().toString());
	}
	
	public void setSweave(final ILaunchConfiguration rCmd) {
		if (fSweaveType > 0 || rCmd == null) {
			throw new IllegalArgumentException();
		}
		fSweaveType = SWEAVE_TYPE_RCMD;
		fSweaveConfig = rCmd;
	}
	
	public void setBuildTex(final VariableText commands) throws CoreException {
		if (fTexType > 0 || commands == null) {
			throw new IllegalArgumentException();
		}
		fTexType = BUILDTEX_TYPE_RCONSOLE;
		fTexRCommands = commands;
		fTexRCommands.performInitialStringSubstitution(true);
		fTexRCommands.set(VARNAME_SWEAVE_FILE, fSweaveFile.getFullPath().toString());
	}
	
	public void setBuildTex(final Builder texBuilder) {
		if (fTexType > 0 || texBuilder == null) {
			throw new IllegalArgumentException();
		}
		fTexType = BUILDTEX_TYPE_ECLIPSE;
		fTexBuilder = texBuilder;
	}
	
//	public Thread createThread() {
//		final Thread thread = new Thread(this, fName);
//		thread.setDaemon(true);
//		fThread = thread;
//		return thread;
//	}
	
	public Job createJob() {
		final Job job = new Job(fName) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				fProgress = SubMonitor.convert(monitor);
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
				}
				catch (final DebugException e) {
				}
			}
		};
		job.setPriority(Job.BUILD);
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
					Messages.RweaveTexProcessing_Tex_error_MustBeInWorkspace_message, null);
		}
		
		if (fBaseFileName == null) {
			fBaseFileName = fSweaveFile.getName();
			final int idx = fBaseFileName.lastIndexOf('.');
			if (idx >= 0) {
				fBaseFileName = fBaseFileName.substring(0, idx);
			}
		}
			
		if (fTexFileExtension == null) {
			fTexFileExtension = "tex";  //$NON-NLS-1$
		}
		fTexFile = fWorkingFolderInWorkspace.getFile(new Path(fBaseFileName + '.' + fTexFileExtension));
		
		final String texFilePath = fTexFile.getFullPath().toString();
		fOutputDir.set(VARNAME_LATEX_FILE, texFilePath);
		
		// 21x
		if (fOutputFormat == null) {
			return new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, -1,
					Messages.RweaveTexProcessing_Tex_error_BuilderNotConfigured_message, null);
		}
		final IContainer outputDir;
		try {
			outputDir = TexPathConfig.resolveDirectory(fOutputDir.getText(), fTexFile, fSweaveFile);
		}
		catch (final CoreException e) {
			return new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, -1,
					Messages.RweaveTexProcessing_Tex_error_OutputDir_message, null);
		}
		fTexPathConfig = new TexPathConfig(fTexFile, outputDir, fOutputFormat);
		
		if (fSweaveRCommands != null) {
			fSweaveRCommands.set(VARNAME_LATEX_FILE, texFilePath);
			fSweaveRCommands.set(VARNAME_OUTPUT_FILE, fTexPathConfig.getOutputFile().getFullPath().toString());
		}
		if (fTexRCommands != null) {
			fTexRCommands.set(VARNAME_LATEX_FILE, texFilePath);
			fTexRCommands.set(VARNAME_OUTPUT_FILE, fTexPathConfig.getOutputFile().getFullPath().toString());
		}
		
		return Status.OK_STATUS;
	}
	
	public void run() {
		try {
			fProgress.beginTask('\'' + fProfileName + '\'', calculateTicks());
			if (fProgress.isCanceled()) {
				fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, Messages.RweaveTexProcessing_info_Canceled_message));
				return;
			}
			
			doWeave();
			if (fProgress.isCanceled()) {
				fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, Messages.RweaveTexProcessing_info_Canceled_message));
			}
			if (fStatus.getSeverity() >= IStatus.ERROR) {
				return;
			}
			
			if (!(fRunTex && fTexType == BUILDTEX_TYPE_RCONSOLE)) {
				doPrepareTex();
				if (fProgress.isCanceled()) {
					fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, Messages.RweaveTexProcessing_info_Canceled_message));
				}
				if (fStatus.getSeverity() >= IStatus.ERROR) {
					return;
				}
				
				doProcessTex();
				if (fProgress.isCanceled()) {
					fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, Messages.RweaveTexProcessing_info_Canceled_message));
				}
				if (fStatus.getSeverity() >= IStatus.ERROR) {
					return;
				}
			}
			
			doOpenOutput();
			if (fStatus.getSeverity() >= IStatus.ERROR) {
				return;
			}
			
			fProgress.done();
		}
//		catch (final CoreException e) {
//			fStatus.add(e.getStatus());
//		}
		catch (final Throwable e) {
			fStatus.add(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, ICommonStatusConstants.LAUNCHING,
					Messages.RweaveTexProcessing_error_UnexpectedError_message, e));
		}
		finally {
			endSchedulingRule();
			exit();
			fProgress = null;
			DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {
				new DebugEvent(this, DebugEvent.TERMINATE),
			});
		}
	}
	
	private int calculateTicks() {
		int sum = 0;
		if (fRunSweave) {
			sum += TICKS_RWEAVE;
		}
		else {
			sum += TICKS_RWEAVE/10;
		}
		if (fTexOpenEditor >= TexTab.OPEN_ALWAYS) {
			sum += TICKS_OPEN_TEX;
		}
		if (fRunTex) {
			sum += TICKS_TEX;
		}
		if (fRunPreview > NO) {
			sum += TICKS_OPEN_OUTPUT;
		}
		sum += TICKS_REST;
		
		return sum;
	}
	
	private boolean beginSchedulingRule(final IProgressMonitor monitor) {
		if (fSchedulingRule != null) {
			return true;
		}
		try {
			final ISchedulingRule rule = MultiRule.combine(
					fWorkingFolderInWorkspace.getProject(), 
					fTexPathConfig.getOutputFile().getParent());
			Job.getJobManager().beginRule(rule, monitor);
			fSchedulingRule = rule;
			return true;
		}
		catch (final OperationCanceledException e) {
			fProgress.setCanceled(true);
		}
		return false;
	}
	
	private void endSchedulingRule() {
		final ISchedulingRule rule = fSchedulingRule;
		if (rule != null) {
			fSchedulingRule = null;
			Job.getJobManager().endRule(rule);
		}
	}
	
	private void exit() {
		if (fStatus.getSeverity() == 0) {
			return;
		}
		fStatus.add(new Status(IStatus.INFO, SweavePlugin.PLUGIN_ID, "Exit code = " + fExitValue)); 
		if (fStatus.getSeverity() == IStatus.ERROR) {
			StatusManager.getManager().handle(fStatus, StatusManager.LOG | StatusManager.SHOW);
			return;
		}
		StatusManager.getManager().handle(fStatus, StatusManager.LOG);
	}
	
	
	private void doWeave() { // 1xx
		if (fSweaveType == SWEAVE_TYPE_RCONSOLE || fTexType == BUILDTEX_TYPE_RCONSOLE) { // 11x
			try {
	//			RCodeLaunchRegistry.runRCodeDirect(RUtil.LINE_SEPARATOR_PATTERN.split(fSweaveCommands), false);
				final ToolProcess rProcess = NicoUI.getToolRegistry().getActiveToolSession(fWorkbenchPage).getProcess();
				if (rProcess == null) {
					NicoUITools.accessTool(RTool.TYPE, rProcess); // throws CoreException
				}
				
				final R rTask = new R();
				if (fRunSweave || fRunTex) {
					final ToolController rController = NicoUITools.accessController(RTool.TYPE, rProcess);
					fProgress.worked(TICKS_PREPARER);
					
					final IStatus submitStatus = rController.submit(rTask);
					if (submitStatus.getSeverity() > IStatus.OK) {
						fStatus.add(submitStatus);
						if (submitStatus.getSeverity() >= IStatus.ERROR) {
							abort(null, 112);
							return;
						}
					}
					while (true) {
						synchronized (this) {
							try {
								if (fProgress.isCanceled()) {
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
						abort(null, 113);
						return;
					}
				}
				else { // we need the working directory
					final SubMonitor progress = fProgress.newChild(TICKS_RWEAVE/10);
					rTask.updatePathInformations(rProcess.getWorkspaceData());
					progress.done();
				}
			}
			catch (final CoreException e) {
				abort(e.getStatus(), 110);
				return;
			}
		}
		else if (fSweaveConfig != null) { // 12x
			try {
				if (fRunSweave) {
					final SubMonitor monitor = fProgress.newChild(TICKS_RWEAVE);
					monitor.beginTask(Messages.RweaveTexProcessing_Sweave_RCmd_label, 100);
					if (!beginSchedulingRule(monitor)) {
						return;
					}
					final ILaunchConfigurationDelegate delegate = RweaveTexLaunchDelegate.getRunDelegate(fSweaveConfig);
					delegate.launch(fSweaveConfig, ILaunchManager.RUN_MODE, fLaunch, monitor.newChild(75));
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
						abort(IStatus.CANCEL, NLS.bind(Messages.RweaveTexProcessing_Sweave_RCmd_error_Found_message, exitValue), null,
								121);
						return;
					}
					monitor.done();
				}
			}
			catch (final CoreException e) {
				abort(e.getStatus(), 120);
				return;
			}
		}
	}
	
	private void continueAfterR() {
		synchronized (this) {
			notifyAll();
		}
	}
	
	private void doPrepareTex() {
		if ((fRunSweave || fRunTex) && fTexFile.exists() && fTexFile.getType() == IResource.FILE) {
			try {
				fTexFile.deleteMarkers(TexlipseBuilder.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
				fTexFile.deleteMarkers(TexlipseBuilder.LAYOUT_WARNING_TYPE, true, IResource.DEPTH_INFINITE);
			}
			catch (final CoreException e) {}
		}
		fProgress.worked(1);
		refreshDir(fWorkingFolderInWorkspace, fProgress.newChild(1));
		if (fProgress.isCanceled()) {
			fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, Messages.RweaveTexProcessing_info_Canceled_message));
		}
		if (fStatus.getSeverity() >= IStatus.ERROR) {
			return;
		}
		if (fRunTex && !fTexFile.exists()) {
			fExitValue = 199;
			fStatus.add(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, -1,
					NLS.bind(Messages.RweaveTexProcessing_Tex_error_NotFound_message, fTexFile.getFullPath().toString()), null));
			return;
		}
		
		if (fTexOpenEditor == TexTab.OPEN_ALWAYS) {
			openEditor(fTexFile);
			fProgress.worked(TICKS_OPEN_TEX);
		}
	}
	
	private void doProcessTex() { // 2xx
		if (fRunTex && fTexType == RweaveTexLaunchDelegate.BUILDTEX_TYPE_ECLIPSE) {
			final SubMonitor progress = fProgress.newChild(TICKS_TEX);
			fProgress.beginTask(Messages.RweaveTexProcessing_Tex_label, 100);
			if (!beginSchedulingRule(progress)) {
				return;
			}
			Texlipse.getViewerManager().closeDocInViewer(fTexPathConfig);
			try {
				fTexBuilder.reset(progress.newChild(60, SubMonitor.SUPPRESS_SUBTASK));
				fTexBuilder.build(fTexPathConfig);
				AbstractBuilder.checkOutput(fTexPathConfig, new SubProgressMonitor(progress, 10));
			}
			catch (final OperationCanceledException e) {
				abort(IStatus.CANCEL, Messages.RweaveTexProcessing_info_Canceled_message, e,
						211);
				return;
			}
			catch (final CoreException e) {
				abort(e.getStatus(), 210);
				return;
			}
			finally {
				finallyTex(progress);
			}
			progress.done();
		}
		
		if (fStatus.getSeverity() < IStatus.ERROR) {
			try { // 28x
				if (fRunTex && fTexOpenEditor > TexTab.OPEN_ALWAYS && fTexFile.findMaxProblemSeverity(IMarker.PROBLEM, true, IResource.DEPTH_ZERO) >= fTexOpenEditor) {
					openEditor(fTexFile);
					fProgress.worked(TICKS_OPEN_TEX);
				}
			}
			catch (final CoreException e) {
				abort(e.getStatus(), 280);
				return;
			}
		}
	}
	
	private void finallyTex(final SubMonitor progress) {
		refreshDir(fWorkingFolderInWorkspace, progress.isCanceled() ? null : progress.newChild(5));
		if (!fWorkingFolderInWorkspace.equals(fTexPathConfig.getOutputFile().getParent())) {
			refreshDir(fTexPathConfig.getOutputFile(), progress.isCanceled() ? null : progress.newChild(5));
		}
	}
	
	private void doOpenOutput() { // 3xx
		if (fRunPreview > NO) {
			final SubMonitor progress = fProgress.newChild(TICKS_OPEN_OUTPUT);
			progress.setWorkRemaining(100);
			if (!fTexPathConfig.getOutputFile().exists()) {
				abort((fRunPreview == EXPLICITE) ? IStatus.ERROR : IStatus.INFO,
						NLS.bind(Messages.RweaveTexProcessing_Output_error_NotFound_message, fTexPathConfig.getOutputFile().getFullPath().toString()), null,
						301);
				return;
			}
			try {
				if (fRunPreview == AUTO && fTexFile.findMaxProblemSeverity(IMarker.PROBLEM, true, IResource.DEPTH_ZERO) >= IMarker.SEVERITY_ERROR) {
					abort(IStatus.CANCEL, Messages.RweaveTexProcessing_Output_info_SkipBecauseTex_message, null,
							302);
					return;
				}
			}
			catch (final CoreException e) {
				abort(e.getStatus(), 303);
				return;
			}
			progress.worked(10);
			if (fPreviewConfig != null) {
				Texlipse.getViewerManager().openDocInViewer(fTexPathConfig, fPreviewConfig);
			}
			else {
				openEditor(fTexPathConfig.getOutputFile());
			}
			progress.done();
//			final ILaunchConfigurationDelegate delegate = getRunDelegate(fPreviewConfig);
//			delegate.launch(fPreviewConfig, ILaunchManager.RUN_MODE, fLaunch, new SubProgressMonitor(fMonitor, 10));
		}
	}
	
	private void abort(final int severity, final String message, final Throwable cause, final int exitValue) {
		abort(new Status(severity, SweavePlugin.PLUGIN_ID, -1, message, cause), exitValue);
	}
	
	private void abort(final IStatus status, final int exitValue) {
		if (status != null) {
			fStatus.add(status);
		}
		fExitValue = exitValue;
	}
	
	private void refreshDir(final IResource resource, final IProgressMonitor monitor) {
		try {
			resource.refreshLocal(IResource.DEPTH_ONE, monitor);
		}
		catch (final OperationCanceledException e) {
			fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, -1,
					Messages.RweaveTexProcessing_info_Canceled_message, e));
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
				}
				catch (final PartInitException e) {
					SweavePlugin.logError(-1, "An error occured when opening the document.", e); 
				}
			}
		});
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
					"The process has not yet terminated.", null)); 
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
		return (fProgress != null);
	}
	
	public void terminate() throws DebugException {
		final IProgressMonitor monitor = fProgress;
		final Thread thread = fThread;
		if (monitor != null) {
			monitor.setCanceled(true);
		}
		if (thread != null) {
			thread.interrupt();
		}
	}
	
	public boolean isTerminated() {
		return (fProgress == null);
	}
	
}
