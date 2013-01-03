/*******************************************************************************
 * Copyright (c) 2008-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
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
import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.io.FileValidator;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.variables.core.VariableText;
import de.walware.ecommons.variables.core.VariableText.LocationProcessor;

import de.walware.statet.nico.core.runtime.IRequireSynch;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolWorkspace;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;

import de.walware.rj.services.RServiceControlExtension;
import net.sourceforge.texlipse.TexPathConfig;
import net.sourceforge.texlipse.Texlipse;
import net.sourceforge.texlipse.builder.AbstractBuilder;
import net.sourceforge.texlipse.builder.Builder;
import net.sourceforge.texlipse.builder.TexlipseBuilder;
import net.sourceforge.texlipse.viewer.ViewerConfiguration;

import de.walware.statet.r.console.core.IRBasicAdapter;
import de.walware.statet.r.console.core.RConsoleTool;
import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.internal.sweave.Messages;
import de.walware.statet.r.internal.sweave.SweavePlugin;


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
	
	
	public static final List<String> SWEAVE_FOLDER_VARNAMES = new ConstList<String>(
			VARNAME_SWEAVE_FILE );
	public static final List<String> SWEAVE_COMMAND_VARNAMES = new ConstList<String>(
			VARNAME_SWEAVE_FILE, VARNAME_LATEX_FILE, VARNAME_OUTPUT_FILE );
	public static final List<String> OUTPUT_DIR_VARNAMES = new ConstList<String>(
			VARNAME_SWEAVE_FILE, VARNAME_LATEX_FILE );
	public static final List<String> TEX_COMMAND_VARNAMES = new ConstList<String>(
			VARNAME_SWEAVE_FILE, VARNAME_LATEX_FILE, VARNAME_OUTPUT_FILE );
	
	
	private class R implements IToolRunnable {
		
		
		public static final int TASK_FINISHED = 1;
		public static final int TASK_PREPARE_TEX = 2;
		
		private int task = 0;
		
		
		R() {
		}
		
		@Override
		public String getTypeId() {
			return "r/sweave/commands"; //$NON-NLS-1$
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return (tool.isProvidingFeatureSet(RConsoleTool.R_BASIC_FEATURESET_ID));
		}
		
		@Override
		public String getLabel() {
			return NLS.bind(Messages.RweaveTexProcessing_Sweave_Task_label, fSweaveFile.getName());
		}
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			switch (event) {
			case REMOVING_FROM:
			case BEING_ABANDONED:
				fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, -1,
						Messages.RweaveTexProcessing_Sweave_Task_info_Canceled_message, null));
				continueAfterR();
				break;
			// finishing handled in run
			}
			return true;
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			final IRBasicAdapter r = (IRBasicAdapter) service;
			fProgress2 = monitor;
			boolean newRule = false;
			Callable<Boolean> cancel = null;
			if (r instanceof RServiceControlExtension) {
				cancel = new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						terminate();
						return Boolean.FALSE;
					}
				};
				((RServiceControlExtension) r).addCancelHandler(cancel);
			}
			try {
				if (checkExit(0)) {
					return;
				}
				
				final ToolWorkspace workspace = r.getWorkspaceData();
				if (fWorkingFolder == null) {
					r.refreshWorkspaceData(0, monitor);
					updatePathInformations(r.getWorkspaceData());
				}
				else {
					String path = workspace.toToolPath(fWorkingFolder);
					path = RUtil.escapeBackslash(path);
					r.submitToConsole("setwd(\""+path+"\")", monitor);
					r.refreshWorkspaceData(0, monitor);
					if (!fWorkingFolder.equals(workspace.getWorkspaceDir())) {
						fStatus.add(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID,
								"Failed to set the R working directory." ));
					}
				}
				
				if (checkExit(0)) {
					return;
				}
				if (fSchedulingRule == null) {
					if (beginSchedulingRule(monitor)) {
						newRule = true;
					}
				}
				if (checkExit(0)) {
					return;
				}
				
				final LocationProcessor processor = new LocationProcessor() {
					@Override
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
						fSweaveRCommands.set(VARNAME_SWEAVE_FILE, fSweaveFile.getFullPath().toString());
						fSweaveRCommands.set(VARNAME_LATEX_FILE, fTexFile.getFullPath().toString());
						fSweaveRCommands.set(VARNAME_OUTPUT_FILE, fTexPathConfig.getOutputFile().getFullPath().toString());
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
					if (checkExit(0)) {
						return;
					}
					
					waitTask(TASK_PREPARE_TEX);
					if (checkExit(0) || this.task < 0) {
						return;
					}
					
					final SubMonitor progress = fProgress.newChild(TICKS_TEX);
					progress.beginTask(Messages.RweaveTexProcessing_Tex_label, 100);
					
					try {
						fTexRCommands.set(VARNAME_SWEAVE_FILE, fSweaveFile.getFullPath().toString());
						fTexRCommands.set(VARNAME_LATEX_FILE, fTexFile.getFullPath().toString());
						fTexRCommands.set(VARNAME_OUTPUT_FILE, fTexPathConfig.getOutputFile().getFullPath().toString());
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
					for (int i = 0; i < commands.length; i++) {
						r.submitToConsole(commands[i], monitor);
						progress.setWorkRemaining(90-80/commands.length*(i+1));
					}
					if (r instanceof IRequireSynch) {
						((IRequireSynch) r).synch(monitor);
					}
				}
			}
			catch (final CoreException e) {
				fStatus.add(e.getStatus());
				throw e;
			}
			finally {
				if (cancel != null) {
					((RServiceControlExtension) r).removeCancelHandler(cancel);
					cancel = null;
				}
				if (newRule && fSchedulingRule != null) {
					Job.getJobManager().transferRule(fSchedulingRule, fThread);
				}
				continueAfterR();
				fProgress2 = null;
			}
			
		}
		
		private void updatePathInformations(final ToolWorkspace workspace) {
			final IFileStore wd = workspace.getWorkspaceDir();
			final IStatus status = setWorkingDir(wd, null, true);
			if (status.getSeverity() > IStatus.OK) {
				fStatus.add(status);
			}
		}
		
		private synchronized void waitTask(final int task) {
			this.task = task;
			while (this.task == task) {
				this.notifyAll();
				try {
					this.wait();
				}
				catch (final InterruptedException e) {
				}
			}
		}
		
		private synchronized void continueAfterR() {
			this.task = TASK_FINISHED;
			this.notifyAll();
		}
		
	}
	
	private final String fProfileName;
	private final String fName;
	private final MultiStatus fStatus;
	private final IWorkbenchPage fWorkbenchPage;
	private final ILaunch fLaunch;
	private Thread fThread;
	private final boolean fUseSchedulingRule = true;
	private ISchedulingRule fSchedulingRule;
	private SubMonitor fProgress;
	private IProgressMonitor fProgress2;
	
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
	private boolean fOutputInitialized;
	
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
	
	
	public void setWorkingDir(final VariableText wd) throws CoreException {
		wd.performInitialStringSubstitution(true);
		wd.set(VARNAME_SWEAVE_FILE, fSweaveFile.getFullPath().toString());
		wd.performFinalStringSubstitution(null);
		
		final FileValidator validator = new FileValidator(false);
		validator.setResourceLabel("Sweave Working / Output Folder");
		validator.setOnFile(IStatus.ERROR);
		validator.setOnExisting(IStatus.OK);
		validator.setOnNotExisting(IStatus.ERROR);
		validator.setRequireWorkspace(true, true);
		{	final IStatus status = validator.validate(wd.getText());
			if (!status.isOK()) {
				throw new CoreException(status);
			}
		}
		{	final IStatus status = setWorkingDir(null, (IContainer) validator.getWorkspaceResource(), true);
			if (!status.isOK()) {
				throw new CoreException(status);
			}
		}
	}
	
	public void setSweave(final VariableText rCommands) throws CoreException {
		if (fSweaveType > 0 || rCommands == null) {
			throw new IllegalArgumentException();
		}
		fSweaveType = SWEAVE_TYPE_RCONSOLE;
		fSweaveRCommands = rCommands;
		fSweaveRCommands.performInitialStringSubstitution(true);
	}
	
	public void setSweave(final ILaunchConfiguration rCmd) {
		if (fSweaveType > 0 || rCmd == null) {
			throw new IllegalArgumentException();
		}
		fSweaveType = SWEAVE_TYPE_RCMD;
		fSweaveConfig = rCmd;
	}
	
	public void setOutput(final VariableText directory, final String format) throws CoreException {
		fOutputDir = directory;
		fOutputDir.performInitialStringSubstitution(true);
		fOutputFormat = format;
		
		if (fWorkingFolder != null && !fOutputInitialized) {
			final IStatus status = initOutputDir();
			if (!status.isOK()) {
				throw new CoreException(status);
			}
		}
	}
	
	public void setBuildTex(final VariableText commands) throws CoreException {
		if (fTexType > 0 || commands == null) {
			throw new IllegalArgumentException();
		}
		fTexType = BUILDTEX_TYPE_RCONSOLE;
		fTexRCommands = commands;
		fTexRCommands.performInitialStringSubstitution(true);
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
		if (fUseSchedulingRule && (fSweaveType > 0 || fTexType > 0)
				&& fWorkingFolderInWorkspace != null && fTexPathConfig != null) {
			fSchedulingRule = createRule();
			job.setRule(fSchedulingRule);
		}
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
				for (int i = 0; i < found.length; i++) {
					if (found[i].getType() == IResource.PROJECT || found[i].getType() == IResource.FOLDER) {
						fWorkingFolderInWorkspace = found[i];
						break;
					}
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
		
		if (fOutputDir != null && !fOutputInitialized) {
			return initOutputDir();
		}
		return Status.OK_STATUS;
	}
	
	private IStatus initOutputDir() {
		fOutputInitialized = true;
		final String texFilePath = fTexFile.getFullPath().toString();
		fOutputDir.set(VARNAME_SWEAVE_FILE, fSweaveFile.getFullPath().toString());
		fOutputDir.set(VARNAME_LATEX_FILE, texFilePath);
		
		// 21x
		if (fOutputFormat == null) {
			return new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, -1,
					Messages.RweaveTexProcessing_Tex_error_BuilderNotConfigured_message, null );
		}
		final IContainer outputDir;
		try {
			outputDir = TexPathConfig.resolveDirectory(fOutputDir.getText(), fTexFile, fSweaveFile);
		}
		catch (final CoreException e) {
			return new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, -1,
					Messages.RweaveTexProcessing_Tex_error_OutputDir_message, null );
		}
		fTexPathConfig = new TexPathConfig(fTexFile, outputDir, fOutputFormat);
		
		return Status.OK_STATUS;
	}
	
	public IFileStore getWorkingDirectory() {
		return fWorkingFolder;
	}
	
	
	@Override
	public void run() {
		try {
			fProgress.beginTask('\'' + fProfileName + '\'', calculateTicks());
			if (checkExit(0)) {
				return;
			}
			
			doWeave();
			if (checkExit(0)) {
				return;
			}
			
			if (!fOutputInitialized) {
				final IStatus status = initOutputDir();
				if (!status.isOK()) {
					fStatus.add(status);
				}
			}
			if (checkExit(0)) {
				return;
			}
			
			if (fRunTex && fTexType == BUILDTEX_TYPE_RCONSOLE) {
				finallyTex(fProgress.newChild(1));
			}
			else {
				doPrepareTex();
				if (checkExit(0)) {
					return;
				}
				
				doProcessTex();
				if (checkExit(0)) {
					return;
				}
			}
			
			endSchedulingRule();
			doOpenOutput();
			
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
	
	private ISchedulingRule createRule() {
		final IResourceRuleFactory factory = fWorkingFolderInWorkspace.getWorkspace().getRuleFactory();
		return MultiRule.combine(new ISchedulingRule[] {
				fWorkingFolderInWorkspace,
				factory.refreshRule(fTexPathConfig.getTexFile()),
				factory.refreshRule(fTexPathConfig.getOutputFile()),
		});
	}
	
	private boolean beginSchedulingRule(final IProgressMonitor monitor) {
		if (!fUseSchedulingRule || fSchedulingRule != null) {
			return true;
		}
		try {
			final ISchedulingRule rule = createRule();
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
			if (Job.getJobManager().currentJob() == null) {
				Job.getJobManager().endRule(rule);
			}
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
			if (!(fRunSweave || fRunTex) && fWorkingFolder != null) {
				return;
			}
			try {
	//			RCodeLaunchRegistry.runRCodeDirect(RUtil.LINE_SEPARATOR_PATTERN.split(fSweaveCommands), false);
				final ToolProcess rProcess = NicoUI.getToolRegistry().getActiveToolSession(fWorkbenchPage).getProcess();
				if (rProcess == null) {
					NicoUITools.accessTool(RConsoleTool.TYPE, rProcess); // throws CoreException
				}
				
				final R rTask = new R();
				if (fRunSweave || fRunTex) {
					fProgress.worked(TICKS_PREPARER);
					
					final IStatus submitStatus = rProcess.getQueue().add(rTask);
					if (submitStatus.getSeverity() > IStatus.OK) {
						fStatus.add(submitStatus);
						if (checkExit(112)) {
							return;
						}
					}
					RTASK: while (true) {
						synchronized (rTask) {
							boolean ok = false;
							try {
								rTask.notifyAll();
								if (rTask.task != R.TASK_FINISHED && checkExit(0)) {
									rTask.task = -1;
									// removing runnable sets the cancel status
									rProcess.getQueue().remove(rTask);
								}
								switch (rTask.task) {
								case R.TASK_FINISHED:
									ok = true;
									break RTASK;
								case R.TASK_PREPARE_TEX:
									doPrepareTex();
									ok = true;
									rTask.task = 0;
									break;
								default:
									ok = true;
								}
								rTask.wait(100);
							}
							catch (final InterruptedException e) {
								// continue loop, monitor is checked
							}
							finally {
								if (!ok) {
									rTask.task = -1;
								}
							}
						}
					}
					if (checkExit(113)) {
						return;
					}
				}
				else if (fWorkingFolder == null) { // we need the working directory
					final SubMonitor progress = fProgress.newChild(TICKS_RWEAVE/10);
					rTask.updatePathInformations(rProcess.getWorkspaceData());
					progress.done();
				}
			}
			catch (final CoreException e) {
				abort(e, 110);
				return;
			}
		}
		else if (fSweaveConfig != null) { // 12x
			if (!fRunSweave && fWorkingFolder != null) {
				return;
			}
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
				abort(e, 120);
				return;
			}
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
		refreshDir(fTexFile, fProgress.newChild(1));
		if (checkExit(195)) {
			return;
		}
		if (fRunTex && !fTexFile.exists()) {
			fExitValue = 199;
			fStatus.add(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, -1,
					NLS.bind(Messages.RweaveTexProcessing_Tex_error_NotFound_message, fTexFile.getFullPath().toString()), null));
			return;
		}
		
		if ((fRunSweave || fRunTex) && fTexFile.exists() && fTexFile.getType() == IResource.FILE
				&& fTexOpenEditor == TexTab.OPEN_ALWAYS) {
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
				abort(e, 210);
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
				abort(e, 280);
				return;
			}
		}
	}
	
	private void finallyTex(final SubMonitor progress) {
		refreshDir(fTexPathConfig.getOutputFile(), progress.isCanceled() ? null : progress.newChild(5));
		if (!fWorkingFolderInWorkspace.equals(fTexPathConfig.getOutputFile().getParent())) {
			final Job job = new Job("Refresh after TeX build") {
				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					return refreshDir(fWorkingFolderInWorkspace, progress.isCanceled() ? null : progress.newChild(5));
				}
			};
			job.setSystem(true);
			final IResourceRuleFactory ruleFactory = fWorkingFolderInWorkspace.getWorkspace().getRuleFactory();
			job.setRule(ruleFactory.refreshRule(fWorkingFolderInWorkspace));
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
				abort(e, 303);
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
	
	private boolean checkExit(final int code) {
		if (fStatus.getSeverity() >= IStatus.ERROR) {
			if (code != 0 && fExitValue == 0) {
				fExitValue = code;
			}
			return true;
		}
		if (fProgress.isCanceled()) {
			final IProgressMonitor p2 = fProgress2;
			if (p2 != null && !p2.isCanceled()) {
				p2.setCanceled(true);
			}
			if (fStatus.getSeverity() < IStatus.CANCEL) { 
				fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, Messages.RweaveTexProcessing_info_Canceled_message));
			}
			return true;
		}
		else {
			final IProgressMonitor p2 = fProgress2;
			if (p2 != null && p2.isCanceled()) {
				fProgress.setCanceled(true);
				if (fStatus.getSeverity() < IStatus.CANCEL) { 
					fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, Messages.RweaveTexProcessing_info_Canceled_message));
				}
				return true;
			}
		}
		return false;
	}
	
	private void abort(final CoreException e, final int exitCode) {
		final IStatus status = e.getStatus();
		if (status.getSeverity() == IStatus.CANCEL) {
			fStatus.add(status);
		}
		else {
			abort(status.getSeverity(), status.getMessage(), e, exitCode);
		}
	}
	
	private void abort(final int severity, final String message, final Throwable cause, final int exitValue) {
		fStatus.add(new Status(severity, SweavePlugin.PLUGIN_ID, -1, message, cause));
		fExitValue = exitValue;
	}
	
	
	private IStatus refreshDir(final IResource resource, final IProgressMonitor monitor) {
		try {
			resource.refreshLocal(IResource.DEPTH_ONE, monitor);
			return Status.OK_STATUS;
		}
		catch (final OperationCanceledException e) {
			return new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, -1,
					Messages.RweaveTexProcessing_info_Canceled_message, e);
		}
		catch (final CoreException e) {
			return e.getStatus();
		}
	}
	
	private void openEditor(final IFile file) {
		UIAccess.getDisplay().syncExec(new Runnable() {
			@Override
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
	
	@Override
	public Object getAdapter(final Class adapter) {
		return null;
	}
	
	@Override
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
	
	@Override
	public String getAttribute(final String key) {
		if (fAttributes != null) {
			return fAttributes.get(key);
		}
		return null;
	}
	
	@Override
	public int getExitValue() throws DebugException {
		if (!isTerminated()) {
			throw new DebugException(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, -1,
					"The process has not yet terminated.", null)); 
		}
		return fExitValue;
	}
	
	@Override
	public String getLabel() {
		return fName;
	}
	
	@Override
	public ILaunch getLaunch() {
		return fLaunch;
	}
	
	@Override
	public IStreamsProxy getStreamsProxy() {
		return null;
	}
	
	@Override
	public boolean canTerminate() {
		return (fProgress != null);
	}
	
	@Override
	public void terminate() throws DebugException {
		{	final IProgressMonitor monitor = fProgress2;
			if (monitor != null) {
				monitor.setCanceled(true);
			}
		}
		{	final IProgressMonitor monitor = fProgress;
			if (monitor != null) {
				monitor.setCanceled(true);
			}
		}
		{	final Thread thread = fThread;
			if (thread != null) {
				thread.interrupt();
			}
		}
	}
	
	@Override
	public boolean isTerminated() {
		return (fProgress == null);
	}
	
}
