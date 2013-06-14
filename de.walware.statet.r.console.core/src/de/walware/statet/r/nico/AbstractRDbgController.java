/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.IWorkspaceSourceUnit;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;

import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolStatus;

import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.data.defaultImpl.RReferenceImpl;
import de.walware.rj.server.dbg.CallStack;
import de.walware.rj.server.dbg.CallStack.Frame;
import de.walware.rj.server.dbg.DbgEnablement;
import de.walware.rj.server.dbg.DbgFilterState;
import de.walware.rj.server.dbg.ElementTracepointInstallationReport;
import de.walware.rj.server.dbg.ElementTracepointInstallationRequest;
import de.walware.rj.server.dbg.FrameContext;
import de.walware.rj.server.dbg.SetDebugReport;
import de.walware.rj.server.dbg.SetDebugRequest;
import de.walware.rj.server.dbg.SrcfileData;
import de.walware.rj.server.dbg.TracepointEvent;
import de.walware.rj.server.dbg.TracepointStatesUpdate;

import de.walware.statet.r.console.core.ContinuePrompt;
import de.walware.statet.r.console.core.IRDataAdapter;
import de.walware.statet.r.console.core.RDbg;
import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.internal.console.core.RConsoleCorePlugin;


/**
 * For implementations supporting debug features
 */
public abstract class AbstractRDbgController extends AbstractRController implements IRDataAdapter {
	
	
	public interface IRControllerTracepointAdapter {
		
		
		boolean matchScriptBreakpoint(IRModelSrcref srcref, IProgressMonitor monitor);
		
		ElementTracepointInstallationRequest getElementTracepoints(SrcfileData srcfile,
				IRModelSrcref su, IProgressMonitor monitor );
		
		ElementTracepointInstallationRequest prepareFileElementTracepoints(SrcfileData srcfile,
				IRSourceUnit su, IProgressMonitor monitor );
		
		void installElementTracepoints(ElementTracepointInstallationRequest request,
				IProgressMonitor monitor );
		
		Object toEclipseData(TracepointEvent hit);
		
	}
	
	
	private static final int TOPLEVELBROWSER_ENABLE_COMMANDS = 1;
	private static final int TOPLEVELBROWSER_CHECK_SUSPENDED = 3;
	private static final int TOPLEVELBROWSER_CHECK_SUBMIT = 4;
	
	
	protected static final RReference TOPLEVEL_ENV_FRAME = new RReferenceImpl(0, RObject.TYPE_ENV, null);
	
	
	protected boolean fIsDebugEnabled;
	protected IRControllerTracepointAdapter fBreakpointAdapter;
	
	private CallStack fCallStack;
	private int fCallStackStamp;
	
	private RReference fGlobalEnv;
	
	private boolean fSuspendScheduled;
	private final IToolRunnable fSuspendRunnable = new ControllerSystemRunnable(SUSPEND_TYPE_ID,
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
				synchronized (fSuspendRunnable) {
					fSuspendScheduled = false;
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
					|| (getHotTasksState() <= 1 && (fCurrentPrompt.meta & META_PROMPT_SUSPENDED) != 0) ) {
				return;
			}
//			if (!canSuspend(monitor)) {
//				scheduleControllerRunnable(this);
//				return;
//			}
			if (fTopLevelBrowserAction == 0) {
				fTopLevelBrowserAction = TOPLEVELBROWSER_CHECK_SUBMIT;
			}
			doRequestSuspend(monitor);
		}
		
	};
	
	private boolean fTopLevelBrowserEnabled;
	private int fTopLevelBrowserAction;
	private final IToolRunnable fTopLevelBrowserRunnable = new ControllerSystemRunnable(
			"r/debug", "Debugging") { //$NON-NLS-1$
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			if (getCurrentLevelL() == 0) {
				if ((fCurrentPrompt.meta & META_PROMPT_SUSPENDED) != 0) {
					setSuspended(getBrowserLevel(fCurrentPrompt.text), 0, null);
				}
				else if ((fCurrentPrompt.meta & META_PROMPT_DEFAULT) != 0) {
					switch (fTopLevelBrowserAction) {
					case TOPLEVELBROWSER_ENABLE_COMMANDS:
						initTopLevelBrowser(monitor);
						break;
					case TOPLEVELBROWSER_CHECK_SUSPENDED:
						if (fQueue.size() > 0) {
							fTopLevelBrowserAction = TOPLEVELBROWSER_CHECK_SUBMIT;
							break;
						}
						setDebugBrowser(TOPLEVEL_ENV_FRAME, false, false, monitor);
						//$FALL-THROUGH$
					default:
						removePostControllerRunnable(fTopLevelBrowserRunnable);
					}
				}
			}
		}
		
	};
	
//	private int fStepFilterAction;
//	private final IToolRunnable fStepFilterRunnable = new IToolRunnable() {
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
//					fStepFilterAction = 0;
//					debugStepOver(true);
//				}
//			}
//		}
//		
//	};
	
	private String fLastSrcfile;
	private String fLastSrcfilePath;
	
	private TracepointEvent fBreakpointHit;
	
	
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
		fIsDebugEnabled = true;
		fBreakpointAdapter = breakpointAdapter;
		
		addSuspendUpdateRunnable(new ControllerSystemRunnable("r/callstack", "Load Callstack") { //$NON-NLS-1$
			@Override
			public void run(final IToolService service,
					final IProgressMonitor monitor) throws CoreException {
				getCallStack(monitor);
			}
		});
		addToolStatusListener(new IToolStatusListener() {
			@Override
			public void controllerStatusRequested(final ToolStatus currentStatus, final ToolStatus requestedStatus,
					final List<DebugEvent> eventCollection) {
			}
			@Override
			public void controllerStatusRequestCanceled(final ToolStatus currentStatus, final ToolStatus requestedStatus,
					final List<DebugEvent> eventCollection) {
			}
			@Override
			public void controllerStatusChanged(final ToolStatus oldStatus, final ToolStatus newStatus,
					final List<DebugEvent> eventCollection) {
				switch (newStatus) {
				case STARTED_IDLING:
				case STARTED_PROCESSING:
				case TERMINATED:
					fCallStack = null;
				}
			}
		});
	}
	
	protected final void setCurrentPromptL(final String text, final boolean addToHistory) {
		final TracepointEvent hit = fBreakpointHit;
		fBreakpointHit = null;
		if (fDefaultPromptText.equals(text)) {
			if (fIsDebugEnabled && getRequestedLevelL() != 0) {
				setSuspended(0, 0, null);
			}
			if (addToHistory) {
				setCurrentPromptL(fDefaultPrompt);
				return;
			}
			setCurrentPromptL(new Prompt(fDefaultPromptText, META_HISTORY_DONTADD | META_PROMPT_DEFAULT));
			return;
		}
		else if (fContinuePromptText.equals(text)) {
			setCurrentPromptL(new ContinuePrompt(
					fCurrentPrompt, fCurrentInput+fLineSeparator, fContinuePromptText,
					addToHistory ? 0 : META_HISTORY_DONTADD));
			return;
		}
		else if (text != null) {
			if (fIsDebugEnabled && text.startsWith("Browse[") && text.endsWith("]> ")) { //$NON-NLS-1$ //$NON-NLS-2$
				fCallStack = null;
				setSuspended(getBrowserLevel(text), (hit != null) ? DebugEvent.BREAKPOINT : 0,
						fBreakpointAdapter.toEclipseData(hit));
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
		if ((getPrompt().meta & META_PROMPT_SUSPENDED) != 0) {
			final String trimmed = input.trim();
			final char c;
			if (trimmed.isEmpty()) {
				fDefaultOutputStream.append(fLineSeparator, SubmitType.OTHER, 0);
				// revert counter?
				return false;
			}
			else if (trimmed.length() == 1) {
				c = trimmed.charAt(0);
			}
			else {
				c = 0;
			}
			switch (c) {
			case 'c':
				debugResume();
				return false;
			case 'Q':
				debugCancel();
				return false;
			case 'n':
				debugStepOver();
				return false;
			}
		}
		return true;
	}
	
	
	public void debugSuspend() {
		synchronized (fSuspendRunnable) {
			if (fSuspendScheduled) {
				return;
			}
			fSuspendScheduled = true;
		}
		fQueue.addHot(fSuspendRunnable);
	}
	
	public void debugResume() {
		scheduleSuspendExitRunnable(new SuspendResumeRunnable(RESUME_TYPE_ID, 
				"Resume", DebugEvent.CLIENT_REQUEST) {
			@Override
			protected boolean canExec(final IProgressMonitor monitor) throws CoreException {
				return ((getPrompt().meta & META_PROMPT_SUSPENDED) != 0);
			}
			@Override
			protected void doExec(final IProgressMonitor monitor) throws CoreException {
				fChanged |= RWorkspace.REFRESH_AUTO;
				fTopLevelBrowserEnabled = false;
				submitToConsole("c", "c", monitor); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}
	
	public void debugStepOver() {
		debugStepOver(false);
	}
	
	public void debugStepInto(final int position, final String fRefCode) {
		scheduleSuspendExitRunnable(new SuspendResumeRunnable(STEP_INTO_TYPE_ID,
				"Step Into", DebugEvent.STEP_OVER) {
			@Override
			protected boolean canExec(final IProgressMonitor monitor) throws CoreException {
				if ((getPrompt().meta & META_PROMPT_SUSPENDED) != 0) {
					final CallStack stack = getCallStack(monitor);
					if (stack != null) {
						final int n = stack.getFrames().size();
						if (n == 0 || position > n) {
							return false;
						}
						final int pos = (position >= 0) ? position : stack.getFrames().size() - 1;
						try {
							final SetDebugReport report = AbstractRDbgController.this.doExec(
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
				fChanged |= RWorkspace.REFRESH_AUTO;
				submitToConsole("c", "c", monitor); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		});
	}
	
	public void debugStepOver(final boolean filter) {
		scheduleSuspendExitRunnable(new SuspendResumeRunnable(STEP_OVER_TYPE_ID,
				"Step Over", DebugEvent.STEP_OVER) {
			@Override
			protected boolean canExec(final IProgressMonitor monitor) throws CoreException {
				if ((getPrompt().meta & META_PROMPT_SUSPENDED) != 0) {
					final CallStack stack = getCallStack(monitor);
					return (stack != null);
				}
				return false;
			}
			@Override
			protected void doExec(final IProgressMonitor monitor) throws CoreException {
				final CallStack stack = getCallStack(monitor);
				fChanged |= RWorkspace.REFRESH_AUTO;
//				if (debug.stack.length == 1
//						|| (debug.stack.length >= 4 && debug.stack[3].isTopLevelCommand()) ) {
//					// already suspended
//					// fTopLevelBrowser = TOPLEVELBROWSER_ENABLE_COMMANDS;
//					submitToConsole("n", (filter || fQueue.size() > 0) ? "n" : "c", monitor);
//				}
//				else {
				submitToConsole("n", "n", monitor); //$NON-NLS-1$ //$NON-NLS-2$
//				}
				return;
			}
		});
	}
	
	public void debugStepToFrame(final int toPosition) {
		scheduleSuspendExitRunnable(new SuspendResumeRunnable(STEP_RETURN_TYPE_ID,
				"Step Return", DebugEvent.STEP_RETURN) {
			@Override
			protected boolean canExec(final IProgressMonitor monitor) throws CoreException {
				if ((getPrompt().meta & META_PROMPT_SUSPENDED) != 0) {
					final CallStack stack = getCallStack(monitor);
					final int n = stack.getFrames().size();
					return (toPosition >= 0 && toPosition < n - 1 );
				}
				return false;
			}
			@Override
			protected void doExec(final IProgressMonitor monitor) throws CoreException {
				final CallStack stack = getCallStack(monitor);
				fChanged |= RWorkspace.REFRESH_AUTO;
				fTopLevelBrowserEnabled = false;
				if (toPosition == 0 || stack.getFrames().get(toPosition).isTopLevelCommand()) {
					fTopLevelBrowserAction = TOPLEVELBROWSER_ENABLE_COMMANDS;
					submitToConsole("c", "c", monitor); //$NON-NLS-1$ //$NON-NLS-2$
				}
				else {
					final int n = stack.getFrames().size();
					final int relPos = n - 1 - toPosition;
					submitToConsole("c", "browserSetDebug(n="+(relPos)+"L); c", monitor); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		});
	}
	
	public void debugCancel() {
		scheduleSuspendExitRunnable(new SuspendResumeRunnable(RESUME_TYPE_ID,
				"Cancel", DebugEvent.CLIENT_REQUEST) {
			@Override
			protected boolean canExec(final IProgressMonitor monitor) throws CoreException {
				return ((getPrompt().meta & META_PROMPT_SUSPENDED) != 0);
			}
			@Override
			protected void doExec(final IProgressMonitor monitor) throws CoreException {
				fChanged |= RWorkspace.REFRESH_AUTO;
				fTopLevelBrowserEnabled = false;
				fTopLevelBrowserAction = TOPLEVELBROWSER_CHECK_SUBMIT;
				submitToConsole("Q", "Q", monitor);
			}
		});
	}
	
	@Override
	protected QuitRunnable createQuitRunnable() {
		return new QuitRunnable() {
			@Override
			protected void doExec(final IProgressMonitor monitor) throws CoreException {
				if ((getPrompt().meta & META_PROMPT_SUSPENDED) != 0) {
					fChanged |= RWorkspace.REFRESH_AUTO;
					fTopLevelBrowserEnabled = false;
					fTopLevelBrowserAction = TOPLEVELBROWSER_CHECK_SUBMIT;
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
		if (fTopLevelBrowserAction == TOPLEVELBROWSER_CHECK_SUBMIT) {
			fTopLevelBrowserAction = 0;
		}
		removePostControllerRunnable(fTopLevelBrowserRunnable);
		
		super.runSuspendedLoopL(o);
		
		if (getCurrentLevelL() == 0) {
			if (fTopLevelBrowserAction == 0) {
				fTopLevelBrowserAction = TOPLEVELBROWSER_CHECK_SUBMIT;
			}
			addPostControllerRunnable(fTopLevelBrowserRunnable);
		}
	}
	
	
	public CallStack getCallStack(final IProgressMonitor monitor) {
		if (fCallStack == null || fCallStackStamp != getCounter()) {
			fCallStackStamp = getCounter();
			try {
				fCallStack = doEvalCallStack(monitor);
			}
			catch (final Exception e) {
				fCallStack = null;
				RConsoleCorePlugin.log(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID, 0,
						"An error occurred when loading the R call stack.", e ));
			}
		}
		return fCallStack;
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
	
	public abstract ElementTracepointInstallationReport exec(
			ElementTracepointInstallationRequest request,
			IProgressMonitor monitor) throws CoreException;
	
	@Override
	public Set<Long> getLazyEnvironments(final IProgressMonitor monitor) {
		Set<Long> list = super.getLazyEnvironments(monitor);
		if (isSuspendedL()) {
			final CallStack stack = getCallStack(monitor);
			if (stack != null) {
				final List<? extends Frame> frames = stack.getFrames();
				if (list == null) {
					list = new HashSet<Long>(frames.size());
				}
				for (int i = 0; i < frames.size(); i++) {
					final long handle = frames.get(i).getHandle();
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
		if ((fCurrentPrompt.meta & META_PROMPT_DEFAULT) == 0) {
			return;
		}
		if (fTopLevelBrowserAction != TOPLEVELBROWSER_CHECK_SUSPENDED) {
			fTopLevelBrowserAction = TOPLEVELBROWSER_CHECK_SUBMIT;
		}
		setDebugBrowser(TOPLEVEL_ENV_FRAME, true, false, monitor);
//		fStepFilterAction = STEPFILTER_STEP1;
//		submitCommandToConsole(new String[] { "browser(skipCalls= 3L)" }, null, monitor);
	}
	
	private void checkInit(final IProgressMonitor monitor) throws CoreException {
		if (fGlobalEnv == null) {
			try {
				fGlobalEnv = RDataUtil.checkRReference(
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
		if (environment == TOPLEVEL_ENV_FRAME || environment.getHandle() == fGlobalEnv.getHandle()) {
			fTopLevelBrowserEnabled = enable;
			if (enable) {
				if (fTopLevelBrowserAction == 0) {
					fTopLevelBrowserAction = TOPLEVELBROWSER_CHECK_SUBMIT;
				}
			}
			else {
				if (fTopLevelBrowserAction != TOPLEVELBROWSER_ENABLE_COMMANDS) {
					fTopLevelBrowserAction = 0;
				}
			}
		}
		try {
			final SetDebugReport report = doExec(
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
			fBreakpointHit = event;
		}
	}
	
	@Override
	public boolean acceptNewConsoleCommand() {
		return ((fCurrentPrompt.meta & (META_PROMPT_DEFAULT | META_PROMPT_SUSPENDED)) != 0);
	}
	
	@Override
	public void submitToConsole(final String input,
			final IProgressMonitor monitor) throws CoreException {
		if (input.indexOf('\n') >= 0) {
			// TODO progress
			final String[] lines = RUtil.LINE_SEPARATOR_PATTERN.split(input);
			for (int i = 0; i < lines.length; i++) {
				if (fTopLevelBrowserAction != 0 && getCurrentLevelL() == 0 && (fCurrentPrompt.meta & META_PROMPT_DEFAULT) != 0) {
					fTopLevelBrowserAction = 0;
					setDebugBrowser(TOPLEVEL_ENV_FRAME, false, false, monitor);
				}
				super.submitToConsole(lines[i], monitor);
			}
			return;
		}
		
		if (fTopLevelBrowserAction != 0 && getCurrentLevelL() == 0 && (fCurrentPrompt.meta & META_PROMPT_DEFAULT) != 0) {
			fTopLevelBrowserAction = 0;
			setDebugBrowser(TOPLEVEL_ENV_FRAME, false, false, monitor);
		}
		super.submitToConsole(input, monitor);
	}
	
	@Override
	public void submitCommandToConsole(final String[] lines, final IRSrcref srcref,
			final IProgressMonitor monitor) throws CoreException {
		if (fIsDebugEnabled && getCurrentLevelL() == 0
				&& (fCurrentPrompt.meta & META_PROMPT_DEFAULT) != 0) {
			if (fTopLevelBrowserAction == TOPLEVELBROWSER_ENABLE_COMMANDS) {
				initTopLevelBrowser(monitor);
			}
			else if (!fTopLevelBrowserEnabled && srcref instanceof IRModelSrcref
					&& fBreakpointAdapter.matchScriptBreakpoint((IRModelSrcref) srcref, monitor) ) {
				initTopLevelBrowser(monitor);
			}
		}
		
		final SrcfileData srcfile = getSrcfile(srcref, monitor);
		
		doSubmitCommandL(lines, srcfile, srcref, monitor);
		
		if (fIsDebugEnabled && srcfile != null && srcref instanceof IRModelSrcref) {
			final ElementTracepointInstallationRequest breakpointsRequest = fBreakpointAdapter
					.getElementTracepoints(srcfile, (IRModelSrcref) srcref, monitor );
			if (breakpointsRequest != null) {
				fBreakpointAdapter.installElementTracepoints(breakpointsRequest, monitor);
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
		final SrcfileData srcfile = getSrcfile(su, monitor);
		final ElementTracepointInstallationRequest breakpointsRequest =
				(fIsDebugEnabled && su instanceof IRWorkspaceSourceUnit) ?
						fBreakpointAdapter.prepareFileElementTracepoints(srcfile, (IRSourceUnit) su, monitor ) :
						null;
		
		doSubmitFileCommandToConsole(lines, srcfile, su, monitor);
		
		if (breakpointsRequest != null) {
			fBreakpointAdapter.installElementTracepoints(breakpointsRequest, monitor);
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
	
	protected SrcfileData getSrcfile(final ISourceUnit su,
			final IProgressMonitor monitor) throws CoreException {
		String fileName = null;
		if (su != null && su.getResource() != null) {
			URI uri = null;
			final FileUtil fileUtil = FileUtil.getFileUtil(su.getResource());
			if (fileUtil != null) {
				uri = fileUtil.getURI();
			}
			if (uri != null) {
				fileName = uri.toString();
				try {
					final IFileStore store = EFS.getStore(uri);
					if (store != null) {
						fileName = getWorkspaceData().toToolPath(store);
					}
				}
				catch (final CoreException e) {
				}
			}
			if (fileName == null) {
				return null;
			}
			if (su instanceof IWorkspaceSourceUnit) {
				final IResource resource = ((IWorkspaceSourceUnit) su).getResource();
				final IPath path = resource.getFullPath();
				
				prepareSrcfile(fileName, path, monitor);
				
				return new SrcfileData(
						(fLastSrcfile == fileName) ? fLastSrcfilePath : path.toPortableString(),
						fileName,
						(su.getWorkingContext() == LTK.PERSISTENCE_CONTEXT) ?
								resource.getLocalTimeStamp()/1000 :
								RDbg.getTimestamp(su, monitor) );
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
			final String statetPath = path.toPortableString();
			if (!srcfile.equals(fLastSrcfile) || !statetPath.equals(fLastSrcfilePath) ) {
				doPrepareSrcfile(srcfile, statetPath, monitor);
			}
			fLastSrcfile = srcfile;
			fLastSrcfilePath = statetPath;
		}
		catch (final Exception e) {
			fLastSrcfile = null;
			fLastSrcfilePath = null;
			RConsoleCorePlugin.log(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID, 0,
					"An error occurred when preparing srcfile information in R." , e ));
		}
	}
	
	protected void doPrepareSrcfile(final String srcfile, final String statetPath,
			final IProgressMonitor monitor) throws Exception {
	}
	
}
