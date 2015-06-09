/*=============================================================================#
 # Copyright (c) 2008-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

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
import de.walware.ecommons.collections.ConstArrayList;
import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.io.FileValidator;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.variables.core.VariableText;
import de.walware.ecommons.variables.core.VariableText.LocationProcessor;

import net.sourceforge.texlipse.TexPathConfig;
import net.sourceforge.texlipse.Texlipse;
import net.sourceforge.texlipse.builder.AbstractBuilder;
import net.sourceforge.texlipse.builder.Builder;
import net.sourceforge.texlipse.builder.TexlipseBuilder;
import net.sourceforge.texlipse.viewer.ViewerConfiguration;

import de.walware.statet.nico.core.runtime.IRequireSynch;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolWorkspace;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;

import de.walware.rj.services.RServiceControlExtension;

import de.walware.statet.r.console.core.IRBasicAdapter;
import de.walware.statet.r.console.core.RConsoleTool;
import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.internal.sweave.Messages;
import de.walware.statet.r.internal.sweave.SweavePlugin;


class RweaveTexTool implements IProcess {
	
	
	public static final int NO= 0;
	public static final int AUTO= 1;
	public static final int EXPLICITE= 2;
	
	private static final int TICKS_PREPARER= 5;
	private static final int TICKS_RWEAVE= 30;
	private static final int TICKS_TEX= 30;
	private static final int TICKS_OPEN_TEX= 5;
	private static final int TICKS_OPEN_OUTPUT= 20;
	private static final int TICKS_REST= 10;
	
	
	public static final List<String> SWEAVE_FOLDER_VARNAMES= new ConstArrayList<>(
			VARNAME_SWEAVE_FILE );
	public static final List<String> SWEAVE_COMMAND_VARNAMES= new ConstArrayList<>(
			VARNAME_SWEAVE_FILE, VARNAME_LATEX_FILE, VARNAME_OUTPUT_FILE );
	public static final List<String> OUTPUT_DIR_VARNAMES= new ConstArrayList<>(
			VARNAME_SWEAVE_FILE, VARNAME_LATEX_FILE );
	public static final List<String> TEX_COMMAND_VARNAMES= new ConstArrayList<>(
			VARNAME_SWEAVE_FILE, VARNAME_LATEX_FILE, VARNAME_OUTPUT_FILE );
	
	
	private class R implements IToolRunnable {
		
		
		public static final int TASK_FINISHED= 1;
		public static final int TASK_PREPARE_TEX= 2;
		
		private int task= 0;
		
		
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
			return NLS.bind(Messages.RweaveTexProcessing_Sweave_Task_label, RweaveTexTool.this.fSweaveFile.getName());
		}
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			switch (event) {
			case REMOVING_FROM:
			case BEING_ABANDONED:
				RweaveTexTool.this.fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, -1,
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
			final IRBasicAdapter r= (IRBasicAdapter) service;
			RweaveTexTool.this.fProgress2= monitor;
			Callable<Boolean> cancel= null;
			if (r instanceof RServiceControlExtension) {
				cancel= new Callable<Boolean>() {
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
				
				final ToolWorkspace workspace= r.getWorkspaceData();
				if (RweaveTexTool.this.fWorkingFolder == null) {
					r.refreshWorkspaceData(0, monitor);
					updatePathInformations(r.getWorkspaceData());
				}
				else {
					String path= workspace.toToolPath(RweaveTexTool.this.fWorkingFolder);
					path= RUtil.escapeBackslash(path);
					r.submitToConsole("setwd(\""+path+"\")", monitor);
					r.refreshWorkspaceData(0, monitor);
					if (!RweaveTexTool.this.fWorkingFolder.equals(workspace.getWorkspaceDir())) {
						RweaveTexTool.this.fStatus.add(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID,
								"Failed to set the R working directory." ));
					}
				}
				
				if (checkExit(0)) {
					return;
				}
				
				final LocationProcessor processor= new LocationProcessor() {
					@Override
					public String process(String path) throws CoreException {
						final IFileStore store= FileUtil.getFileStore(path);
						path= workspace.toToolPath(store);
						path= RUtil.escapeBackslash(path);
						return path;
					}
				};
				
				if (RweaveTexTool.this.fRunSweave && RweaveTexTool.this.fSweaveType == SWEAVE_TYPE_RCONSOLE) {
					monitor.subTask("Sweave"); //$NON-NLS-1$
					final SubMonitor progress= RweaveTexTool.this.fProgress.newChild(TICKS_RWEAVE);
					progress.beginTask(Messages.RweaveTexProcessing_Sweave_InConsole_label, 100);
					
					try {
						RweaveTexTool.this.fSweaveRCommands.set(VARNAME_SWEAVE_FILE, RweaveTexTool.this.fSweaveFile.getFullPath().toString());
						RweaveTexTool.this.fSweaveRCommands.set(VARNAME_LATEX_FILE, RweaveTexTool.this.fTexFile.getFullPath().toString());
						RweaveTexTool.this.fSweaveRCommands.set(VARNAME_OUTPUT_FILE, RweaveTexTool.this.fTexPathConfig.getOutputFile().getFullPath().toString());
						RweaveTexTool.this.fSweaveRCommands.performFinalStringSubstitution(processor);
					}
					catch (final NullPointerException e) {
						throw new CoreException(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID,
								Messages.RweaveTexProcessing_Sweave_error_ResourceVariable_message));
					}
					catch (final CoreException e) {
						throw new CoreException(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID,
								Messages.RweaveTexProcessing_Sweave_error_ResourceVariable_message + ' ' + e.getLocalizedMessage()));
					}
					final String[] commands= RUtil.LINE_SEPARATOR_PATTERN.split(RweaveTexTool.this.fSweaveRCommands.getText());
					for (int i= 0; i < commands.length; i++) {
						r.submitToConsole(commands[i], monitor);
					}
					if (r instanceof IRequireSynch) {
						((IRequireSynch) r).synch(monitor);
					}
				}
				
				if (RweaveTexTool.this.fRunTex && RweaveTexTool.this.fTexType == BUILDTEX_TYPE_RCONSOLE) {
					monitor.subTask("LaTeX"); //$NON-NLS-1$
					if (checkExit(0)) {
						return;
					}
					
					waitTask(TASK_PREPARE_TEX);
					if (checkExit(0) || this.task < 0) {
						return;
					}
					
					final SubMonitor progress= RweaveTexTool.this.fProgress.newChild(TICKS_TEX);
					progress.beginTask(Messages.RweaveTexProcessing_Tex_label, 100);
					
					try {
						RweaveTexTool.this.fTexRCommands.set(VARNAME_SWEAVE_FILE, RweaveTexTool.this.fSweaveFile.getFullPath().toString());
						RweaveTexTool.this.fTexRCommands.set(VARNAME_LATEX_FILE, RweaveTexTool.this.fTexFile.getFullPath().toString());
						RweaveTexTool.this.fTexRCommands.set(VARNAME_OUTPUT_FILE, RweaveTexTool.this.fTexPathConfig.getOutputFile().getFullPath().toString());
						RweaveTexTool.this.fTexRCommands.performFinalStringSubstitution(processor);
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
					
					Texlipse.getViewerManager().closeDocInViewer(RweaveTexTool.this.fTexPathConfig);
					
					final String[] commands= RUtil.LINE_SEPARATOR_PATTERN.split(RweaveTexTool.this.fTexRCommands.getText());
					for (int i= 0; i < commands.length; i++) {
						r.submitToConsole(commands[i], monitor);
						progress.setWorkRemaining(90-80/commands.length*(i+1));
					}
					if (r instanceof IRequireSynch) {
						((IRequireSynch) r).synch(monitor);
					}
				}
			}
			catch (final CoreException e) {
				RweaveTexTool.this.fStatus.add(e.getStatus());
				throw e;
			}
			finally {
				if (cancel != null) {
					((RServiceControlExtension) r).removeCancelHandler(cancel);
					cancel= null;
				}
				continueAfterR();
				RweaveTexTool.this.fProgress2= null;
			}
			
		}
		
		private void updatePathInformations(final ToolWorkspace workspace) {
			final IFileStore wd= workspace.getWorkspaceDir();
			final IStatus status= setWorkingDir(wd, null, true);
			if (status.getSeverity() > IStatus.OK) {
				RweaveTexTool.this.fStatus.add(status);
			}
		}
		
		private synchronized void waitTask(final int task) {
			this.task= task;
			while (this.task == task) {
				notifyAll();
				try {
					this.wait();
				}
				catch (final InterruptedException e) {
				}
			}
		}
		
		private synchronized void continueAfterR() {
			this.task= TASK_FINISHED;
			notifyAll();
		}
		
	}
	
	private final String fProfileName;
	private final String fName;
	private final MultiStatus fStatus;
	private final IWorkbenchPage fWorkbenchPage;
	private final ILaunch fLaunch;
	private Thread fWorkerThread;
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
	int fTexOpenEditor= 0;
	private int fTexType;
	private Builder fTexBuilder;
	private VariableText fTexRCommands;
	private TexPathConfig fTexPathConfig;
	
	int fRunPreview;
	ViewerConfiguration fPreviewConfig;
	
	private int fExitValue= 0;
	private Map<String, String> fAttributes;
	
	
	public RweaveTexTool(final String name, final ILaunch launch, final IWorkbenchPage workbenchPage, final IFile file) {
		this.fProfileName= name;
		this.fName= NLS.bind(Messages.RweaveTexProcessing_label, name, file.getName());
		this.fWorkbenchPage= workbenchPage;
		this.fLaunch= launch;
		this.fSweaveFile= file;
		this.fStatus= new MultiStatus(SweavePlugin.PLUGIN_ID, ICommonStatusConstants.LAUNCHING,
				NLS.bind(Messages.RweaveTexProcessing_Status_label, this.fSweaveFile.getName()), null);
	}
	
	
	public String getProfileName() {
		return this.fProfileName;
	}
	
	public IFile getSweaveFile() {
		return this.fSweaveFile;
	}
	
	public void setWorkingDir(final VariableText wd) throws CoreException {
		wd.performInitialStringSubstitution(true);
		wd.set(VARNAME_SWEAVE_FILE, this.fSweaveFile.getFullPath().toString());
		wd.performFinalStringSubstitution(null);
		
		final FileValidator validator= new FileValidator(false);
		validator.setResourceLabel("Sweave Working / Output Folder");
		validator.setOnFile(IStatus.ERROR);
		validator.setOnExisting(IStatus.OK);
		validator.setOnNotExisting(IStatus.ERROR);
		validator.setRequireWorkspace(true, true);
		{	final IStatus status= validator.validate(wd.getText());
			if (!status.isOK()) {
				throw new CoreException(status);
			}
		}
		{	final IStatus status= setWorkingDir(null, (IContainer) validator.getWorkspaceResource(), true);
			if (!status.isOK()) {
				throw new CoreException(status);
			}
		}
	}
	
	public void setSweave(final VariableText rCommands) throws CoreException {
		if (this.fSweaveType > 0 || rCommands == null) {
			throw new IllegalArgumentException();
		}
		this.fSweaveType= SWEAVE_TYPE_RCONSOLE;
		this.fSweaveRCommands= rCommands;
		this.fSweaveRCommands.performInitialStringSubstitution(true);
	}
	
	public void setSweave(final ILaunchConfiguration rCmd) {
		if (this.fSweaveType > 0 || rCmd == null) {
			throw new IllegalArgumentException();
		}
		this.fSweaveType= SWEAVE_TYPE_RCMD;
		this.fSweaveConfig= rCmd;
	}
	
	public void setOutput(final VariableText directory, final String format) throws CoreException {
		this.fOutputDir= directory;
		this.fOutputDir.performInitialStringSubstitution(true);
		this.fOutputFormat= format;
		
		if (this.fWorkingFolder != null && !this.fOutputInitialized) {
			final IStatus status= initOutputDir();
			if (!status.isOK()) {
				throw new CoreException(status);
			}
		}
	}
	
	public void setBuildTex(final VariableText commands) throws CoreException {
		if (this.fTexType > 0 || commands == null) {
			throw new IllegalArgumentException();
		}
		this.fTexType= BUILDTEX_TYPE_RCONSOLE;
		this.fTexRCommands= commands;
		this.fTexRCommands.performInitialStringSubstitution(true);
	}
	
	public void setBuildTex(final Builder texBuilder) {
		if (this.fTexType > 0 || texBuilder == null) {
			throw new IllegalArgumentException();
		}
		this.fTexType= BUILDTEX_TYPE_ECLIPSE;
		this.fTexBuilder= texBuilder;
	}
	
	
	public IStatus setWorkingDir(final IFileStore efsFolder, final IContainer workspaceFolder, final boolean synch) {
		this.fWorkingFolder= efsFolder;
		this.fWorkingFolderInWorkspace= workspaceFolder;
		if (synch) {
			if (this.fWorkingFolder == null && this.fWorkingFolderInWorkspace != null) {
				this.fWorkingFolder= EFS.getLocalFileSystem().getStore(this.fWorkingFolderInWorkspace.getLocation());
			}
			else if (this.fWorkingFolder != null && this.fWorkingFolderInWorkspace == null) {
				final IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
				final IContainer[] found= root.findContainersForLocationURI(this.fWorkingFolder.toURI());
				for (int i= 0; i < found.length; i++) {
					if (found[i].getType() == IResource.PROJECT || found[i].getType() == IResource.FOLDER) {
						this.fWorkingFolderInWorkspace= found[i];
						break;
					}
				}
			}
		}
		if (this.fWorkingFolderInWorkspace == null) {
			this.fExitValue= 11;
			return new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, -1,
					Messages.RweaveTexProcessing_Tex_error_MustBeInWorkspace_message, null);
		}
		
		if (this.fBaseFileName == null) {
			this.fBaseFileName= this.fSweaveFile.getName();
			final int idx= this.fBaseFileName.lastIndexOf('.');
			if (idx >= 0) {
				this.fBaseFileName= this.fBaseFileName.substring(0, idx);
			}
		}
			
		if (this.fTexFileExtension == null) {
			this.fTexFileExtension= "tex";  //$NON-NLS-1$
		}
		this.fTexFile= this.fWorkingFolderInWorkspace.getFile(new Path(this.fBaseFileName + '.' + this.fTexFileExtension));
		
		if (this.fOutputDir != null && !this.fOutputInitialized) {
			return initOutputDir();
		}
		return Status.OK_STATUS;
	}
	
	private IStatus initOutputDir() {
		this.fOutputInitialized= true;
		final String texFilePath= this.fTexFile.getFullPath().toString();
		this.fOutputDir.set(VARNAME_SWEAVE_FILE, this.fSweaveFile.getFullPath().toString());
		this.fOutputDir.set(VARNAME_LATEX_FILE, texFilePath);
		
		// 21x
		if (this.fOutputFormat == null) {
			return new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, -1,
					Messages.RweaveTexProcessing_Tex_error_BuilderNotConfigured_message, null );
		}
		final IContainer outputDir;
		try {
			outputDir= TexPathConfig.resolveDirectory(this.fOutputDir.getText(), this.fTexFile, this.fSweaveFile);
		}
		catch (final CoreException e) {
			return new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, -1,
					Messages.RweaveTexProcessing_Tex_error_OutputDir_message, e );
		}
		this.fTexPathConfig= new TexPathConfig(this.fTexFile, outputDir, this.fOutputFormat);
		
		return Status.OK_STATUS;
	}
	
	public IFileStore getWorkingDirectory() {
		return this.fWorkingFolder;
	}
	
	
	void run(final IProgressMonitor monitor) {
		this.fProgress= SubMonitor.convert(monitor);
		this.fWorkerThread= Thread.currentThread();
		
		try {
			this.fProgress.beginTask('\'' + this.fProfileName + '\'', calculateTicks());
			if (checkExit(0)) {
				return;
			}
			
			doWeave();
			if (checkExit(0)) {
				return;
			}
			
			if (!this.fOutputInitialized) {
				final IStatus status= initOutputDir();
				if (!status.isOK()) {
					this.fStatus.add(status);
				}
			}
			if (checkExit(0)) {
				return;
			}
			
			if (this.fRunTex && this.fTexType == BUILDTEX_TYPE_RCONSOLE) {
				finallyTex(this.fProgress.newChild(1));
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
			
			doOpenOutput();
			
			this.fProgress.done();
		}
//		catch (final CoreException e) {
//			fStatus.add(e.getStatus());
//		}
		catch (final Throwable e) {
			this.fStatus.add(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, ICommonStatusConstants.LAUNCHING,
					Messages.RweaveTexProcessing_error_UnexpectedError_message, e));
		}
		finally {
			exit();
			this.fProgress= null;
			DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {
				new DebugEvent(this, DebugEvent.TERMINATE),
			});
		}
	}
	
	private int calculateTicks() {
		int sum= 0;
		if (this.fRunSweave) {
			sum += TICKS_RWEAVE;
		}
		else {
			sum += TICKS_RWEAVE/10;
		}
		if (this.fTexOpenEditor >= TexTab.OPEN_ALWAYS) {
			sum += TICKS_OPEN_TEX;
		}
		if (this.fRunTex) {
			sum += TICKS_TEX;
		}
		if (this.fRunPreview > NO) {
			sum += TICKS_OPEN_OUTPUT;
		}
		sum += TICKS_REST;
		
		return sum;
	}
	
	
	private ISchedulingRule beginSchedulingRule(final ISchedulingRule rule, final IProgressMonitor monitor) {
		try {
			Job.getJobManager().beginRule(rule, monitor);
			return rule;
		}
		catch (final OperationCanceledException e) {
			this.fProgress.setCanceled(true);
			return null;
		}
	}
	
	private void endSchedulingRule(final ISchedulingRule rule) {
		if (rule != null) {
			Job.getJobManager().endRule(rule);
		}
	}
	
	
	private void exit() {
		if (this.fStatus.getSeverity() == 0) {
			return;
		}
		this.fStatus.add(new Status(IStatus.INFO, SweavePlugin.PLUGIN_ID, "Exit code= " + this.fExitValue)); 
		if (this.fStatus.getSeverity() == IStatus.ERROR) {
			StatusManager.getManager().handle(this.fStatus, StatusManager.LOG | StatusManager.SHOW);
			return;
		}
		StatusManager.getManager().handle(this.fStatus, StatusManager.LOG);
	}
	
	
	private void doWeave() { // 1xx
		if (this.fSweaveType == SWEAVE_TYPE_RCONSOLE || this.fTexType == BUILDTEX_TYPE_RCONSOLE) { // 11x
			if (!(this.fRunSweave || this.fRunTex) && this.fWorkingFolder != null) {
				return;
			}
			try {
	//			RCodeLaunchRegistry.runRCodeDirect(RUtil.LINE_SEPARATOR_PATTERN.split(fSweaveCommands), false);
				final ToolProcess rProcess= NicoUI.getToolRegistry().getActiveToolSession(this.fWorkbenchPage).getProcess();
				if (rProcess == null) {
					NicoUITools.accessTool(RConsoleTool.TYPE, rProcess); // throws CoreException
				}
				
				final R rTask= new R();
				if (this.fRunSweave || this.fRunTex) {
					this.fProgress.worked(TICKS_PREPARER);
					
					final IStatus submitStatus= rProcess.getQueue().add(rTask);
					if (submitStatus.getSeverity() > IStatus.OK) {
						this.fStatus.add(submitStatus);
						if (checkExit(112)) {
							return;
						}
					}
					RTASK: while (true) {
						synchronized (rTask) {
							boolean ok= false;
							try {
								rTask.notifyAll();
								if (rTask.task != R.TASK_FINISHED && checkExit(0)) {
									rTask.task= -1;
									// removing runnable sets the cancel status
									rProcess.getQueue().remove(rTask);
								}
								switch (rTask.task) {
								case R.TASK_FINISHED:
									ok= true;
									break RTASK;
								case R.TASK_PREPARE_TEX:
									doPrepareTex();
									ok= true;
									rTask.task= 0;
									break;
								default:
									ok= true;
								}
								rTask.wait(100);
							}
							catch (final InterruptedException e) {
								// continue loop, monitor is checked
							}
							finally {
								if (!ok) {
									rTask.task= -1;
								}
							}
						}
					}
					if (checkExit(113)) {
						return;
					}
				}
				else if (this.fWorkingFolder == null) { // we need the working directory
					final SubMonitor progress= this.fProgress.newChild(TICKS_RWEAVE/10);
					rTask.updatePathInformations(rProcess.getWorkspaceData());
					progress.done();
				}
			}
			catch (final CoreException e) {
				abort(e, 110);
				return;
			}
		}
		else if (this.fSweaveConfig != null) { // 12x
			if (!this.fRunSweave && this.fWorkingFolder != null) {
				return;
			}
			try {
				if (this.fRunSweave) {
					final SubMonitor monitor= this.fProgress.newChild(TICKS_RWEAVE);
					monitor.beginTask(Messages.RweaveTexProcessing_Sweave_RCmd_label, 100);
					final ILaunchConfigurationDelegate delegate= RweaveTexLaunchDelegate.getRunDelegate(this.fSweaveConfig);
					delegate.launch(this.fSweaveConfig, ILaunchManager.RUN_MODE, this.fLaunch, monitor.newChild(75));
					final IProcess[] processes= this.fLaunch.getProcesses();
					if (processes.length == 0) {
						throw new IllegalStateException();
					}
					final IProcess sweaveProcess= processes[processes.length-1];
					if (!sweaveProcess.isTerminated()) {
						throw new IllegalStateException();
					}
					final int exitValue= sweaveProcess.getExitValue();
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
		final ISchedulingRule rule= beginSchedulingRule(this.fTexFile.getParent(), this.fProgress.newChild(1));
		try {
			if ((this.fRunSweave || this.fRunTex) && this.fTexFile.exists() && this.fTexFile.getType() == IResource.FILE) {
				try {
					this.fTexFile.deleteMarkers(TexlipseBuilder.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
					this.fTexFile.deleteMarkers(TexlipseBuilder.LAYOUT_WARNING_TYPE, true, IResource.DEPTH_INFINITE);
				}
				catch (final CoreException e) {}
			}
			this.fProgress.worked(1);
			refreshDir(this.fTexFile, this.fProgress.newChild(1));
			if (checkExit(195)) {
				return;
			}
			
			final boolean exists= this.fTexFile.exists() && this.fTexFile.getType() == IResource.FILE;
			if (this.fRunTex && !exists) {
				this.fExitValue= 199;
				this.fStatus.add(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, -1,
						NLS.bind(Messages.RweaveTexProcessing_Tex_error_NotFound_message, this.fTexFile.getFullPath().toString()), null));
				return;
			}
			
			if ((this.fRunSweave || this.fRunTex) && exists && this.fTexOpenEditor == TexTab.OPEN_ALWAYS) {
				openEditor(this.fTexFile);
				this.fProgress.worked(TICKS_OPEN_TEX);
			}
		}
		finally {
			endSchedulingRule(rule);
		}
	}
	
	private void doProcessTex() { // 2xx
		if (this.fRunTex && this.fTexType == RweaveTexLaunchDelegate.BUILDTEX_TYPE_ECLIPSE) {
			final SubMonitor progress= this.fProgress.newChild(TICKS_TEX);
			this.fProgress.beginTask(Messages.RweaveTexProcessing_Tex_label, 100);
			Texlipse.getViewerManager().closeDocInViewer(this.fTexPathConfig);
			try {
				this.fTexBuilder.reset(progress.newChild(60, SubMonitor.SUPPRESS_SUBTASK));
				this.fTexBuilder.build(this.fTexPathConfig);
				AbstractBuilder.checkOutput(this.fTexPathConfig, new SubProgressMonitor(progress, 10));
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
		
		if (this.fStatus.getSeverity() < IStatus.ERROR) {
			try { // 28x
				if (this.fRunTex && this.fTexOpenEditor > TexTab.OPEN_ALWAYS && this.fTexFile.findMaxProblemSeverity(IMarker.PROBLEM, true, IResource.DEPTH_ZERO) >= this.fTexOpenEditor) {
					openEditor(this.fTexFile);
					this.fProgress.worked(TICKS_OPEN_TEX);
				}
			}
			catch (final CoreException e) {
				abort(e, 280);
				return;
			}
		}
	}
	
	private void finallyTex(final SubMonitor progress) {
		refreshDir(this.fTexPathConfig.getOutputFile(), progress.isCanceled() ? null : progress.newChild(5));
		if (!this.fWorkingFolderInWorkspace.equals(this.fTexPathConfig.getOutputFile().getParent())) {
			final Job job= new Job("Refresh after TeX build") {
				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					return refreshDir(RweaveTexTool.this.fWorkingFolderInWorkspace, progress.isCanceled() ? null : progress.newChild(5));
				}
			};
			job.setSystem(true);
			final IResourceRuleFactory ruleFactory= this.fWorkingFolderInWorkspace.getWorkspace().getRuleFactory();
			job.setRule(ruleFactory.refreshRule(this.fWorkingFolderInWorkspace));
		}
	}
	
	private void doOpenOutput() { // 3xx
		if (this.fRunPreview > NO) {
			final SubMonitor progress= this.fProgress.newChild(TICKS_OPEN_OUTPUT);
			progress.setWorkRemaining(100);
			if (!this.fTexPathConfig.getOutputFile().exists()) {
				abort((this.fRunPreview == EXPLICITE) ? IStatus.ERROR : IStatus.INFO,
						NLS.bind(Messages.RweaveTexProcessing_Output_error_NotFound_message, this.fTexPathConfig.getOutputFile().getFullPath().toString()), null,
						301);
				return;
			}
			try {
				if (this.fRunPreview == AUTO && this.fTexFile.findMaxProblemSeverity(IMarker.PROBLEM, true, IResource.DEPTH_ZERO) >= IMarker.SEVERITY_ERROR) {
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
			if (this.fPreviewConfig != null) {
				Texlipse.getViewerManager().openDocInViewer(this.fTexPathConfig, this.fPreviewConfig);
			}
			else {
				openEditor(this.fTexPathConfig.getOutputFile());
			}
			progress.done();
//			final ILaunchConfigurationDelegate delegate= getRunDelegate(fPreviewConfig);
//			delegate.launch(fPreviewConfig, ILaunchManager.RUN_MODE, fLaunch, new SubProgressMonitor(fMonitor, 10));
		}
	}
	
	private boolean checkExit(final int code) {
		if (this.fStatus.getSeverity() >= IStatus.ERROR) {
			if (code != 0 && this.fExitValue == 0) {
				this.fExitValue= code;
			}
			return true;
		}
		if (this.fProgress.isCanceled()) {
			final IProgressMonitor p2= this.fProgress2;
			if (p2 != null && !p2.isCanceled()) {
				p2.setCanceled(true);
			}
			if (this.fStatus.getSeverity() < IStatus.CANCEL) { 
				this.fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, Messages.RweaveTexProcessing_info_Canceled_message));
			}
			return true;
		}
		else {
			final IProgressMonitor p2= this.fProgress2;
			if (p2 != null && p2.isCanceled()) {
				this.fProgress.setCanceled(true);
				if (this.fStatus.getSeverity() < IStatus.CANCEL) { 
					this.fStatus.add(new Status(IStatus.CANCEL, SweavePlugin.PLUGIN_ID, Messages.RweaveTexProcessing_info_Canceled_message));
				}
				return true;
			}
		}
		return false;
	}
	
	private void abort(final CoreException e, final int exitCode) {
		final IStatus status= e.getStatus();
		if (status.getSeverity() == IStatus.CANCEL) {
			this.fStatus.add(status);
		}
		else {
			abort(status.getSeverity(), status.getMessage(), e, exitCode);
		}
	}
	
	private void abort(final int severity, final String message, final Throwable cause, final int exitValue) {
		this.fStatus.add(new Status(severity, SweavePlugin.PLUGIN_ID, -1, message, cause));
		this.fExitValue= exitValue;
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
					IDE.openEditor(RweaveTexTool.this.fWorkbenchPage, file);
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
		if (this.fAttributes == null) {
			initAttributes();
		}
		this.fAttributes.put(key, value);
	}
	
	private synchronized void initAttributes() {
		if (this.fAttributes == null) {
			this.fAttributes= new HashMap<String, String>();
		}
	}
	
	@Override
	public String getAttribute(final String key) {
		if (this.fAttributes != null) {
			return this.fAttributes.get(key);
		}
		return null;
	}
	
	@Override
	public int getExitValue() throws DebugException {
		if (!isTerminated()) {
			throw new DebugException(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, -1,
					"The process has not yet terminated.", null)); 
		}
		return this.fExitValue;
	}
	
	@Override
	public String getLabel() {
		return this.fName;
	}
	
	@Override
	public ILaunch getLaunch() {
		return this.fLaunch;
	}
	
	public IStatus getStatus() {
		return this.fStatus;
	}
	
	@Override
	public IStreamsProxy getStreamsProxy() {
		return null;
	}
	
	@Override
	public boolean canTerminate() {
		return (this.fProgress != null);
	}
	
	@Override
	public void terminate() throws DebugException {
		{	final IProgressMonitor monitor= this.fProgress2;
			if (monitor != null) {
				monitor.setCanceled(true);
			}
		}
		{	final IProgressMonitor monitor= this.fProgress;
			if (monitor != null) {
				monitor.setCanceled(true);
			}
		}
		{	final Thread thread= this.fWorkerThread;
			if (thread != null) {
				thread.interrupt();
			}
		}
	}
	
	@Override
	public boolean isTerminated() {
		return (this.fProgress == null);
	}
	
}
