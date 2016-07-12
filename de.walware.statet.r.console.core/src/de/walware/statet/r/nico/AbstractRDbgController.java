/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.nico;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Version;

import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.core.model.IWorkspaceSourceUnit;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;

import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolStatus;
import de.walware.statet.nico.core.runtime.ToolStreamProxy;

import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.data.defaultImpl.RReferenceImpl;
import de.walware.rj.server.dbg.CallStack;
import de.walware.rj.server.dbg.CtrlReport;
import de.walware.rj.server.dbg.DbgEnablement;
import de.walware.rj.server.dbg.DbgFilterState;
import de.walware.rj.server.dbg.DbgRequest;
import de.walware.rj.server.dbg.ElementTracepointInstallationRequest;
import de.walware.rj.server.dbg.Frame;
import de.walware.rj.server.dbg.FrameContext;
import de.walware.rj.server.dbg.FrameRef;
import de.walware.rj.server.dbg.SetDebugReport;
import de.walware.rj.server.dbg.SetDebugRequest;
import de.walware.rj.server.dbg.SrcfileData;
import de.walware.rj.server.dbg.TracepointEvent;
import de.walware.rj.server.dbg.TracepointInstallationReport;
import de.walware.rj.server.dbg.TracepointInstallationRequest;
import de.walware.rj.server.dbg.TracepointStatesUpdate;

import de.walware.statet.r.console.core.AbstractRController;
import de.walware.statet.r.console.core.ContinuePrompt;
import de.walware.statet.r.console.core.IRDataAdapter;
import de.walware.statet.r.console.core.RDbg;
import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.tool.IRConsoleService;
import de.walware.statet.r.internal.console.core.RConsoleCorePlugin;


/**
 * For implementations supporting debug features
 */
public abstract class AbstractRDbgController extends AbstractRController
		implements IRDataAdapter, ICombinedRDataAdapter {
	
	
	public interface IRControllerTracepointAdapter {
		
		
		boolean matchScriptBreakpoint(IRModelSrcref srcref, IProgressMonitor monitor);
		
		ElementTracepointInstallationRequest getElementTracepoints(SrcfileData srcfile,
				IRModelSrcref su, IProgressMonitor monitor );
		
		ElementTracepointInstallationRequest prepareFileElementTracepoints(SrcfileData srcfile,
				IRSourceUnit su, IProgressMonitor monitor );
		
		void finishFileElementTracepoints(SrcfileData srcfileData, IRSourceUnit su,
				ElementTracepointInstallationRequest request, IProgressMonitor monitor );
		
		void installElementTracepoints(ElementTracepointInstallationRequest request,
				IProgressMonitor monitor );
		
		Object toEclipseData(TracepointEvent hit);
		
		
	}
	
	
	private static final int TOPLEVELBROWSER_ENABLE_COMMANDS= 1;
	private static final int TOPLEVELBROWSER_CHECK_SUSPENDED= 3;
	private static final int TOPLEVELBROWSER_CHECK_SUBMIT= 4;
	
	
	protected static final RReference TOPLEVEL_ENV_FRAME= new RReferenceImpl(0, RObject.TYPE_ENV, null);
	
	
	private IRControllerTracepointAdapter breakpointAdapter;
	
	private CallStack callStack;
	private int callStackStamp;
	
	private RReference globalEnv;
	
	private boolean suspendScheduled;
	private final IToolRunnable suspendRunnable= new ControllerSystemRunnable(SUSPEND_TYPE_ID,
			"Suspend") {
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			switch (event) {
			case MOVING_FROM:
				return false;
			case REMOVING_FROM:
			case BEING_ABANDONED:
			case FINISHING_OK:
			case FINISHING_ERROR:
			case FINISHING_CANCEL:
				synchronized (AbstractRDbgController.this.suspendRunnable) {
					AbstractRDbgController.this.suspendScheduled= false;
				}
				break;
			default:
				break;
			}
			return true;
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			if (getStatusL() == ToolStatus.STARTED_SUSPENDED
					|| (getHotTasksState() <= 1 && (AbstractRDbgController.this.fCurrentPrompt.meta & META_PROMPT_SUSPENDED) != 0) ) {
				return;
			}
//			if (!canSuspend(monitor)) {
//				scheduleControllerRunnable(this);
//				return;
//			}
			if (AbstractRDbgController.this.topLevelBrowserAction == 0) {
				AbstractRDbgController.this.topLevelBrowserAction= TOPLEVELBROWSER_CHECK_SUBMIT;
			}
			doRequestSuspend(monitor);
		}
		
	};
	
	private boolean topLevelBrowserEnabled;
	private int topLevelBrowserAction;
	private final ISystemRunnable fTopLevelBrowserRunnable= new ControllerSystemRunnable(
			"r/debug", "Debugging") { //$NON-NLS-1$
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			if (getCurrentLevelL() == 0) {
				if ((AbstractRDbgController.this.fCurrentPrompt.meta & META_PROMPT_SUSPENDED) != 0) {
					setSuspended(getBrowserLevel(AbstractRDbgController.this.fCurrentPrompt.text), 0, null);
				}
				else if ((AbstractRDbgController.this.fCurrentPrompt.meta & META_PROMPT_DEFAULT) != 0) {
					switch (AbstractRDbgController.this.topLevelBrowserAction) {
					case TOPLEVELBROWSER_ENABLE_COMMANDS:
						initTopLevelBrowser(monitor);
						break;
					case TOPLEVELBROWSER_CHECK_SUSPENDED:
						if (getQueue().size() > 0) {
							AbstractRDbgController.this.topLevelBrowserAction= TOPLEVELBROWSER_CHECK_SUBMIT;
							break;
						}
						setDebugBrowser(TOPLEVEL_ENV_FRAME, false, false, monitor);
						//$FALL-THROUGH$
					default:
						removePostControllerRunnable(AbstractRDbgController.this.fTopLevelBrowserRunnable);
					}
				}
			}
		}
		
	};
	
//	private int fStepFilterAction;
//	private final IToolRunnable fStepFilterRunnable= new IToolRunnable() {
//		
//		public String getTypeId() {
//			return "r/debug/";
//		}
//		
//		public SubmitType getSubmitType() {
//			return SubmitType.OTHER;
//		}
//		
//		public String getLabel() {
//			return "Step Filter";
//		}
//		
//		public boolean changed(final int event, final ToolProcess process) {
//			return true;
//		}
//		
//		public void run(final IToolRunnableControllerAdapter adapter,
//				final IProgressMonitor monitor) throws CoreException {
//			if (DebugPlugin.isUseStepFilters()
//					&& (fCurrentPrompt.meta & META_PROMPT_SUSPENDED) != 0) {
//				if (fStepFilterAction == STEPFILTER_STEP1) {
//					fStepFilterAction= 0;
//					debugStepOver(true);
//				}
//			}
//		}
//		
//	};
	
	private String lastSrcfile;
	private String lastSrcfilePath;
	
	private TracepointEvent breakpointHit;
	
	
	/**
	 * 
	 * @param process the R process the controller belongs to
	 * @param initData the initialization data
	 * @param enableDebug if debug features should be enabled
	 */
	public AbstractRDbgController(final RProcess process, final Map<String, Object> initData) {
		super(process, initData);
	}
	
	
	public void initDebug(final IRControllerTracepointAdapter breakpointAdapter) {
		if (breakpointAdapter == null) {
			throw new NullPointerException("breakpointAdapter");
		}
		setDebugEnabled(true);
		this.breakpointAdapter= breakpointAdapter;
		
		class LoadCallstackRunnable extends ControllerSystemRunnable implements ISystemRunnable {
			
			public LoadCallstackRunnable() {
				super("r/callstack", "Load Callstack"); //$NON-NLS-1$
			}
			
			@Override
			public void run(final IToolService service,
					final IProgressMonitor monitor) throws CoreException {
				getCallStack(monitor);
			}
			
		}
		addSuspendUpdateRunnable(new LoadCallstackRunnable());
		addToolStatusListener(new IToolStatusListener() {
			@Override
			public void controllerStatusChanged(final ToolStatus oldStatus, final ToolStatus newStatus,
					final List<DebugEvent> eventCollection) {
				switch (newStatus) {
				case STARTED_IDLING:
				case STARTED_PROCESSING:
				case TERMINATED:
					AbstractRDbgController.this.callStack= null;
				}
			}
		});
	}
	
	protected final void setCurrentPromptL(final String text, final boolean addToHistory) {
		final TracepointEvent hit= this.breakpointHit;
		this.breakpointHit= null;
		if (this.defaultPromptText.equals(text)) {
			if (isDebugEnabled() && getRequestedLevelL() != 0) {
				setSuspended(0, 0, null);
			}
			if (addToHistory) {
				setCurrentPromptL(this.fDefaultPrompt);
				return;
			}
			setCurrentPromptL(new Prompt(this.defaultPromptText, META_HISTORY_DONTADD | META_PROMPT_DEFAULT));
			return;
		}
		else if (this.continuePromptText.equals(text)) {
			setCurrentPromptL(new ContinuePrompt(
					this.fCurrentPrompt, this.fCurrentInput+this.fLineSeparator, this.continuePromptText,
					addToHistory ? 0 : META_HISTORY_DONTADD));
			return;
		}
		else if (text != null) {
			if (isDebugEnabled() && text.startsWith("Browse[") && text.endsWith("]> ")) { //$NON-NLS-1$ //$NON-NLS-2$
				this.callStack= null;
				setSuspended(getBrowserLevel(text),
						(hit != null) ? DebugEvent.BREAKPOINT : 0,
						(hit != null) ? this.breakpointAdapter.toEclipseData(hit) : null );
				setCurrentPromptL(new Prompt(text, addToHistory ? (META_PROMPT_SUSPENDED) :
						(META_PROMPT_SUSPENDED | META_HISTORY_DONTADD)));
				return;
			}
			setCurrentPromptL(new Prompt(text, addToHistory ? 0 : META_HISTORY_DONTADD));
			return;
		}
		else { // TODO log warning / exception?
			setCurrentPromptL(new Prompt("", addToHistory ? 0 : META_HISTORY_DONTADD)); //$NON-NLS-1$
			return;
		}
	}
	
	private int getBrowserLevel(final String prompt) {
		return Integer.parseInt(prompt.substring(7, prompt.indexOf(']')));
	}
	
	@Override
	protected boolean runConsoleCommandInSuspend(final String input) {
		final ToolStreamProxy streams= getStreams();
		
		if ((getPrompt().meta & META_PROMPT_SUSPENDED) != 0) {
			final String trimmed= input.trim();
			if (trimmed.isEmpty()) {
				streams.getOutputStreamMonitor().append(this.fLineSeparator, SubmitType.TOOLS, 0);
				// revert counter?
				return false;
			}
			else if (trimmed.length() == 1) {
				try {
					switch(trimmed.charAt(0)) {
					case 'Q':
						debugCancel();
						return false;
					case 'c':
						if (exec(new DbgRequest.Resume())) {
							return false;
						}
						break;
					case 'n':
						if (exec(new DbgRequest.StepOver())) {
							return false;
						}
						break;
					case 's':
						if (exec(new DbgRequest.StepInto())) {
							return false;
						}
						break;
					default:
						break;
					}
				}
				catch (final CoreException e) {
					RConsoleCorePlugin.log(new Status(IStatus.INFO, RConsoleCorePlugin.PLUGIN_ID, 0,
							"An error occurred when executing debug request in the R engine.", e ));
					
					return false;
				}
			}
		}
		return true;
	}
	
	
	public void debugSuspend() {
		synchronized (this.suspendRunnable) {
			if (this.suspendScheduled) {
				return;
			}
			this.suspendScheduled= true;
		}
		getQueue().addHot(this.suspendRunnable);
	}
	
	public boolean exec(final DbgRequest request) throws CoreException {
		
		class DbgRequestResumeRunnable<R extends DbgRequest> extends SuspendResumeRunnable {
			
			
			public DbgRequestResumeRunnable(final String id, final String label) {
				super(id, label, RDbg.getResumeEventDetail(request.getOp()));
			}
			
			
			protected DbgRequest check(final R request, final IProgressMonitor monitor) {
				return request;
			}
			
			@Override
			public void run(final IToolService adapter,
					final IProgressMonitor monitor) throws CoreException {
				if ((getPrompt().meta & META_PROMPT_SUSPENDED) == 0) {
					return;
				}
				final DbgRequest checkedRequest= check((R) request, monitor);
				if (checkedRequest == null) {
					return;
				}
				final CtrlReport report= AbstractRDbgController.this.doExec(checkedRequest, monitor);
				if (!report.isEngineSuspended()) {
					super.run(adapter, monitor);
					submitToConsole(getResumeRCommand(report.getOp()), null, monitor);
					setDetail(RDbg.getResumeEventDetail(report.getOp()));
				}
			}
			
			@Override
			protected void doExec(final IProgressMonitor monitor) throws CoreException {
				briefChanged(IRConsoleService.AUTO_CHANGE);
			}
			
			protected String getResumeRCommand(final byte op) {
				switch (op) {
				case DbgRequest.RESUME:
					return "c"; //$NON-NLS-1$
				case DbgRequest.STEP_INTO:
					return "s"; //$NON-NLS-1$
				case DbgRequest.STEP_OVER:
					return "n"; //$NON-NLS-1$
				case DbgRequest.STEP_RETURN:
				default:
					return "c"; //$NON-NLS-1$
				}
			}
			
		}
		
		switch (request.getOp()) {
		case DbgRequest.RESUME:
			scheduleSuspendExitRunnable(new DbgRequestResumeRunnable<DbgRequest.Resume>(
					RESUME_TYPE_ID, "Resume" ) {
				
				@Override
				protected void doExec(final IProgressMonitor monitor) throws CoreException {
					super.doExec(monitor);
					AbstractRDbgController.this.topLevelBrowserEnabled= false;
				}
				
			});
			return true;
		case DbgRequest.STEP_OVER:
			scheduleSuspendExitRunnable(new DbgRequestResumeRunnable<DbgRequest.StepOver>(
					STEP_OVER_TYPE_ID, "Step Over" ));
			return true;
		case DbgRequest.STEP_INTO:
			if ((getPlatform().getRVersion().compareTo(new Version(3, 1, 0)) < 0)) {
				return false;
			}
			scheduleSuspendExitRunnable(new DbgRequestResumeRunnable<DbgRequest.StepInto>(
					STEP_INTO_TYPE_ID, "Step Into" ));
			return true;
		case DbgRequest.STEP_RETURN:
			scheduleSuspendExitRunnable(new DbgRequestResumeRunnable<DbgRequest.StepReturn>(
					STEP_RETURN_TYPE_ID, "Step Return" ) {
				
				private Frame targetFrame;
				
				@Override
				protected DbgRequest check(final DbgRequest.StepReturn request, final IProgressMonitor monitor) {
					final CallStack callStack= getCallStack(monitor);
					if (request.getTarget() instanceof FrameRef.ByPosition) {
						final int targetPosition= ((FrameRef.ByPosition) request.getTarget()).getPosition();
						final int n= callStack.getFrames().size();
						if (targetPosition >= 0 && targetPosition < n - 1) {
							this.targetFrame= callStack.getFrames().get(targetPosition);
						}
					}
					else if (request.getTarget() instanceof FrameRef.ByHandle) {
						final long targetHandle= ((FrameRef.ByHandle) request.getTarget()).getHandle();
						this.targetFrame= callStack.findFrame(targetHandle);
					}
					return (this.targetFrame != null) ?
							new DbgRequest.StepReturn(new FrameRef.ByHandle(this.targetFrame.getHandle())) :
							null;
				}
				
				@Override
				protected void doExec(final IProgressMonitor monitor) throws CoreException {
					super.doExec(monitor);
//					if (targetPosition == 0) {
//						fTopLevelBrowserAction= TOPLEVELBROWSER_ENABLE_COMMANDS;
//					}
				}
				
			});
			return true;
		default:
			throw new UnsupportedOperationException(request.toString());
		}
	}
	
	public void debugStepInto(final int position, final String fRefCode) throws CoreException {
		scheduleSuspendExitRunnable(new SuspendResumeRunnable(STEP_INTO_TYPE_ID,
				"Step Into", DebugEvent.STEP_OVER) {
			@Override
			protected boolean canExec(final IProgressMonitor monitor) throws CoreException {
				if ((getPrompt().meta & META_PROMPT_SUSPENDED) != 0) {
					final CallStack stack= getCallStack(monitor);
					if (stack != null) {
						final int n= stack.getFrames().size();
						if (n == 0 || position > n) {
							return false;
						}
						final int pos= (position >= 0) ? position : stack.getFrames().size() - 1;
						try {
							final SetDebugReport report= AbstractRDbgController.this.doExec(
									new SetDebugRequest(pos, fRefCode, true, true), monitor);
							return (report != null);
						}
						catch (final Exception e) {
							RConsoleCorePlugin.log(new Status(IStatus.INFO, RConsoleCorePlugin.PLUGIN_ID, 0,
									"A problem occurred when stepping into the specified function call: " +
									"Could not prepare debug for '"+fRefCode+"'.", e ));
							return false;
						}
					}
				}
				return false;
			}
			@Override
			protected void doExec(final IProgressMonitor monitor) throws CoreException {
				briefChanged(IRConsoleService.AUTO_CHANGE);
				submitToConsole("c", "c", monitor); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		});
	}
	
	public void debugCancel() throws CoreException {
		scheduleSuspendExitRunnable(new SuspendResumeRunnable(RESUME_TYPE_ID,
				"Cancel", DebugEvent.CLIENT_REQUEST) {
			@Override
			protected boolean canExec(final IProgressMonitor monitor) throws CoreException {
				return ((getPrompt().meta & META_PROMPT_SUSPENDED) != 0);
			}
			@Override
			protected void doExec(final IProgressMonitor monitor) throws CoreException {
				briefChanged(IRConsoleService.AUTO_CHANGE);
				AbstractRDbgController.this.topLevelBrowserEnabled= false;
				AbstractRDbgController.this.topLevelBrowserAction= TOPLEVELBROWSER_CHECK_SUBMIT;
				submitToConsole("Q", "Q", monitor); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}
	
	@Override
	protected QuitRunnable createQuitRunnable() {
		return new QuitRunnable() {
			@Override
			protected void doExec(final IProgressMonitor monitor) throws CoreException {
				if ((getPrompt().meta & META_PROMPT_SUSPENDED) != 0) {
					briefChanged(IRConsoleService.AUTO_CHANGE);
					AbstractRDbgController.this.topLevelBrowserEnabled= false;
					AbstractRDbgController.this.topLevelBrowserAction= TOPLEVELBROWSER_CHECK_SUBMIT;
					submitToConsole("Q", "Q", monitor); //$NON-NLS-1$ //$NON-NLS-2$
				}
//				getQueue().add(createQuitRunnable());
			}
		};
	}
	
	@Override
	protected void doQuitL(final IProgressMonitor monitor) throws CoreException {
		if ((getPrompt().meta & META_PROMPT_SUSPENDED) == 0) {
			super.doQuitL(monitor);
		}
	}
	
	@Override
	protected void runSuspendedLoopL(final int o) {
		if (this.topLevelBrowserAction == TOPLEVELBROWSER_CHECK_SUBMIT) {
			this.topLevelBrowserAction= 0;
		}
		removePostControllerRunnable(this.fTopLevelBrowserRunnable);
		
		super.runSuspendedLoopL(o);
		
		if (getCurrentLevelL() == 0) {
			if (this.topLevelBrowserAction == 0) {
				this.topLevelBrowserAction= TOPLEVELBROWSER_CHECK_SUBMIT;
			}
			addPostControllerRunnable(this.fTopLevelBrowserRunnable);
		}
	}
	
	
	public CallStack getCallStack(final IProgressMonitor monitor) {
		if (this.callStack == null || this.callStackStamp != getChangeStamp()) {
			this.callStackStamp= getChangeStamp();
			try {
				this.callStack= doEvalCallStack(monitor);
			}
			catch (final Exception e) {
				this.callStack= null;
				RConsoleCorePlugin.log(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID, 0,
						"An error occurred when loading the R call stack.", e ));
			}
		}
		return this.callStack;
	}
	
	public FrameContext evalFrameContext(final int position, final IProgressMonitor monitor) throws CoreException {
		try {
			return doEvalFrameContext(position, monitor);
		}
		catch (final Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID, 0,
					NLS.bind("An error occurred when loading detail of R stack frame {0}.", position),
					e ));
		}
	}
	
	public abstract void exec(DbgEnablement request) throws CoreException;
	
	public abstract void exec(DbgFilterState request) throws CoreException;
	
	public abstract void exec(TracepointStatesUpdate request) throws CoreException;
	
	public abstract void exec(TracepointStatesUpdate request,
			IProgressMonitor monitor) throws CoreException;
	
	public abstract TracepointInstallationReport exec(
			TracepointInstallationRequest request,
			IProgressMonitor monitor) throws CoreException;
	
	@Override
	public Set<Long> getLazyEnvironments(final IProgressMonitor monitor) {
		Set<Long> list= super.getLazyEnvironments(monitor);
		if (isSuspendedL()) {
			final CallStack stack= getCallStack(monitor);
			if (stack != null) {
				final List<? extends Frame> frames= stack.getFrames();
				if (list == null) {
					list= new HashSet<>(frames.size());
				}
				for (int i= 0; i < frames.size(); i++) {
					final long handle= frames.get(i).getHandle();
					if (handle != 0) {
						list.add(Long.valueOf(handle));
					}
				}
			}
		}
		return list;
	}
	
	protected CallStack doEvalCallStack(final IProgressMonitor monitor) throws CoreException {
		return null;
	}
	
	protected FrameContext doEvalFrameContext(final int position,
			final IProgressMonitor monitor) throws Exception {
		return null;
	}
	
	
	public void initTopLevelBrowser(final IProgressMonitor monitor) throws CoreException {
		if ((this.fCurrentPrompt.meta & META_PROMPT_DEFAULT) == 0) {
			return;
		}
		if (this.topLevelBrowserAction != TOPLEVELBROWSER_CHECK_SUSPENDED) {
			this.topLevelBrowserAction= TOPLEVELBROWSER_CHECK_SUBMIT;
		}
		setDebugBrowser(TOPLEVEL_ENV_FRAME, true, false, monitor);
//		fStepFilterAction= STEPFILTER_STEP1;
//		submitCommandToConsole(new String[] { "browser(skipCalls= 3L)" }, null, monitor);
	}
	
	private void checkInit(final IProgressMonitor monitor) throws CoreException {
		if (this.globalEnv == null) {
			try {
				this.globalEnv= RDataUtil.checkRReference(
						evalData(".GlobalEnv", null, 0, DEPTH_REFERENCE, monitor) ); //$NON-NLS-1$
			}
			catch (final UnexpectedRDataException e) {
				throw new CoreException(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID, 0,
						"Init debug data failed.", e));
			}
		}
		
	}
	
	protected boolean setDebugBrowser(final RReference environment, final boolean enable,
			final boolean temp,
			final IProgressMonitor monitor) throws CoreException {
		checkInit(monitor);
		if (environment == TOPLEVEL_ENV_FRAME || environment.getHandle() == this.globalEnv.getHandle()) {
			this.topLevelBrowserEnabled= enable;
			if (enable) {
				if (this.topLevelBrowserAction == 0) {
					this.topLevelBrowserAction= TOPLEVELBROWSER_CHECK_SUBMIT;
				}
			}
			else {
				if (this.topLevelBrowserAction != TOPLEVELBROWSER_ENABLE_COMMANDS) {
					this.topLevelBrowserAction= 0;
				}
			}
		}
		try {
			final SetDebugReport report= doExec(
					new SetDebugRequest(environment.getHandle(), enable, temp), monitor );
			return (report != null && report.isChanged());
		}
		catch (final CoreException e) {
			throw e;
		}
		catch (final Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID, -1,
					"An error occurred when changing the R debug browser state.", e));
		}
	}
	
	
	protected SetDebugReport doExec(final SetDebugRequest request,
			final IProgressMonitor monitor) throws CoreException {
		return null;
	}
	
	protected boolean canSuspend(final IProgressMonitor monitor) {
		return true;
	}
	
	protected void doRequestSuspend(final IProgressMonitor monitor) throws CoreException {
	}
	
	protected void handle(final TracepointEvent event) {
		if (event.getKind() == TracepointEvent.KIND_ABOUT_TO_HIT) {
			this.breakpointHit= event;
		}
	}
	
	protected CtrlReport doExec(final DbgRequest request,
			final IProgressMonitor monitor) throws CoreException {
		return null;
	}
	
	
	@Override
	public boolean acceptNewConsoleCommand() {
		return ((this.fCurrentPrompt.meta & (META_PROMPT_DEFAULT | META_PROMPT_SUSPENDED)) != 0);
	}
	
	@Override
	public void submitToConsole(final String input,
			final IProgressMonitor monitor) throws CoreException {
		if (input.indexOf('\n') >= 0) {
			// TODO progress
			final String[] lines= RUtil.LINE_SEPARATOR_PATTERN.split(input);
			for (int i= 0; i < lines.length; i++) {
				if (this.topLevelBrowserAction != 0 && getCurrentLevelL() == 0 && (this.fCurrentPrompt.meta & META_PROMPT_DEFAULT) != 0) {
					this.topLevelBrowserAction= 0;
					setDebugBrowser(TOPLEVEL_ENV_FRAME, false, false, monitor);
				}
				super.submitToConsole(lines[i], monitor);
			}
			return;
		}
		
		if (this.topLevelBrowserAction != 0 && getCurrentLevelL() == 0 && (this.fCurrentPrompt.meta & META_PROMPT_DEFAULT) != 0) {
			this.topLevelBrowserAction= 0;
			setDebugBrowser(TOPLEVEL_ENV_FRAME, false, false, monitor);
		}
		super.submitToConsole(input, monitor);
	}
	
	@Override
	public void submitCommandToConsole(final String[] lines, final IRSrcref srcref,
			final IProgressMonitor monitor) throws CoreException {
		if (isDebugEnabled() && getCurrentLevelL() == 0
				&& (this.fCurrentPrompt.meta & META_PROMPT_DEFAULT) != 0) {
			if (this.topLevelBrowserAction == TOPLEVELBROWSER_ENABLE_COMMANDS) {
				initTopLevelBrowser(monitor);
			}
			else if (!this.topLevelBrowserEnabled && srcref instanceof IRModelSrcref
					&& this.breakpointAdapter.matchScriptBreakpoint((IRModelSrcref) srcref, monitor) ) {
				initTopLevelBrowser(monitor);
			}
		}
		
		final SrcfileData srcfile= getSrcfile(srcref, monitor);
		
		doSubmitCommandL(lines, srcfile, srcref, monitor);
		
		if (isDebugEnabled() && srcfile != null && srcref instanceof IRModelSrcref) {
			final ElementTracepointInstallationRequest breakpointsRequest= this.breakpointAdapter
					.getElementTracepoints(srcfile, (IRModelSrcref) srcref, monitor );
			if (breakpointsRequest != null) {
				this.breakpointAdapter.installElementTracepoints(breakpointsRequest, monitor);
			}
		}
	}
	
	protected void doSubmitCommandL(final String[] lines, final SrcfileData srcfile,
			final IRSrcref srcref,
			final IProgressMonitor monitor) throws CoreException {
		super.submitCommandToConsole(lines, srcref, monitor);
	}
	
	@Override
	public void submitFileCommandToConsole(final String[] lines, final ISourceUnit su,
			final IProgressMonitor monitor) throws CoreException {
		if (su == null) {
			super.submitFileCommandToConsole(lines, null, monitor);
			return;
		}
		SrcfileData srcfile= getSrcfile(su, monitor);
		final ElementTracepointInstallationRequest breakpointsRequest =
				(isDebugEnabled() && su instanceof IRWorkspaceSourceUnit) ?
						this.breakpointAdapter.prepareFileElementTracepoints(srcfile, (IRSourceUnit) su, monitor ) :
						null;
		try {
			doSubmitFileCommandToConsole(lines, srcfile, su, monitor);
		}
		finally {
			if (breakpointsRequest != null) {
				if (srcfile.getTimestamp() != getRTimestamp((IRWorkspaceSourceUnit) su, monitor)) {
					srcfile= null;
				}
				this.breakpointAdapter.finishFileElementTracepoints(srcfile, (IRSourceUnit) su,
						breakpointsRequest, monitor );
			}
		}
	}
	
	public void doSubmitFileCommandToConsole(final String[] lines,
			final SrcfileData srcfile, final ISourceUnit su,
			final IProgressMonitor monitor) throws CoreException {
		super.submitFileCommandToConsole(lines, su, monitor);
	}
	
	protected SrcfileData getSrcfile(final IRSrcref srcref,
			final IProgressMonitor monitor) throws CoreException {
		if (srcref instanceof IRModelSrcref) {
			return getSrcfile(((IRModelSrcref) srcref).getFile(), monitor);
		}
		return null;
	}
	
	private long getRTimestamp(final IWorkspaceSourceUnit su, final IProgressMonitor monitor) {
		return (su.getWorkingContext() == LTK.PERSISTENCE_CONTEXT) ?
				su.getResource().getLocalTimeStamp()/1000 :
				RDbg.getTimestamp(su, monitor);
	}
	
	protected SrcfileData getSrcfile(final ISourceUnit su,
			final IProgressMonitor monitor) throws CoreException {
		String fileName= null;
		if (su != null && su.getResource() != null) {
			URI uri= null;
			final FileUtil fileUtil= FileUtil.getFileUtil(su.getResource());
			if (fileUtil != null) {
				uri= fileUtil.getURI();
			}
			if (uri != null) {
				fileName= uri.toString();
				try {
					final IFileStore store= EFS.getStore(uri);
					if (store != null) {
						fileName= getWorkspaceData().toToolPath(store);
					}
				}
				catch (final CoreException e) {
				}
			}
			if (fileName == null) {
				return null;
			}
			if (su instanceof IWorkspaceSourceUnit) {
				final IWorkspaceSourceUnit wsu= (IWorkspaceSourceUnit) su;
				final IPath path= wsu.getResource().getFullPath();
				
				prepareSrcfile(fileName, path, monitor);
				
				return new SrcfileData(
						(this.lastSrcfile == fileName) ? this.lastSrcfilePath : path.toPortableString(),
						fileName, getRTimestamp(wsu, monitor) );
			}
			else {
				return new SrcfileData(null, fileName, RDbg.getTimestamp(su, monitor));
			}
		}
		return null;
	}
	
	private void prepareSrcfile(final String srcfile, final IPath path,
			final IProgressMonitor monitor) {
		try {
			if (srcfile == null || path == null) {
				return;
			}
			final String statetPath= path.toPortableString();
			if (!srcfile.equals(this.lastSrcfile) || !statetPath.equals(this.lastSrcfilePath) ) {
				doPrepareSrcfile(srcfile, statetPath, monitor);
			}
			this.lastSrcfile= srcfile;
			this.lastSrcfilePath= statetPath;
		}
		catch (final Exception e) {
			this.lastSrcfile= null;
			this.lastSrcfilePath= null;
			RConsoleCorePlugin.log(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID, 0,
					"An error occurred when preparing srcfile information in R." , e ));
		}
	}
	
	protected void doPrepareSrcfile(final String srcfile, final String statetPath,
			final IProgressMonitor monitor) throws Exception {
	}
	
}
