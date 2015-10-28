/*=============================================================================#
 # Copyright (c) 2005-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.core.runtime;

import static de.walware.statet.nico.core.runtime.IToolEventHandler.REPORT_STATUS_DATA_KEY;
import static de.walware.statet.nico.core.runtime.IToolEventHandler.REPORT_STATUS_EVENT_ID;
import static de.walware.statet.nico.core.runtime.IToolEventHandler.SCHEDULE_QUIT_EVENT_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.osgi.util.NLS;

import de.walware.jcommons.collections.ImCollections;

import de.walware.ecommons.FastList;
import de.walware.ecommons.IDisposable;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolCommandHandler;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.NicoCoreMessages;
import de.walware.statet.nico.internal.core.Messages;
import de.walware.statet.nico.internal.core.NicoPlugin;
import de.walware.statet.nico.internal.core.RunnableProgressMonitor;


/**
 * Controller for a long running tight integrated tool.
 * <p>
 * Usage: This class is intend to be subclass. Subclasses are responsible for the
 * life cycle of the tool (<code>startTool()</code>, <code>terminateTool()</code>.
 * Subclasses should provide an interface which can be used by IToolRunnables
 * to access the features of the tool. E.g. provide an abstract implementation of
 * IToolRunnable with the necessary methods (in protected scope).</p>
 * 
 * Methods with protected visibility and marked with an L at the end their names must be called only
 * within the lifecycle thread.
 */
public abstract class ToolController implements IConsoleService {
	
	
	/**
	 * Listens for changes of the status of a controller.
	 * 
	 * Use this only if it's really necessary, otherwise listen to
	 * debug events of the ToolProcess.
	 */
	public static interface IToolStatusListener {
		
		void controllerStatusRequested(ToolStatus currentStatus, ToolStatus requestedStatus, List<DebugEvent> eventCollection);
		
		void controllerStatusRequestCanceled(ToolStatus currentStatus, ToolStatus requestedStatus, List<DebugEvent> eventCollection);
		
		/**
		 * Should be fast!
		 * 
		 * This method is called in the tool lifecycle thread
		 * and blocks the queue.
		 * 
		 * @param oldStatus
		 * @param newStatus
		 * @param eventCollection a collection, you can add you own debug events to.
		 */
		void controllerStatusChanged(ToolStatus oldStatus, ToolStatus newStatus, List<DebugEvent> eventCollection);
		
//		void controllerBusyChanged(boolean isBusy, final List<DebugEvent> eventCollection);
		
	}
	
	
	private static NullProgressMonitor fgProgressMonitorDummy = new NullProgressMonitor();
	
	
	protected abstract class ControllerSystemRunnable implements ISystemRunnable {
		
		
		private final String fTypeId;
		private final String fLabel;
		
		
		public ControllerSystemRunnable(final String typeId, final String label) {
			fTypeId = typeId;
			fLabel = label;
		}
		
		
		@Override
		public String getTypeId() {
			return fTypeId;
		}
		
		@Override
		public String getLabel() {
			return fLabel;
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return (tool == getTool());
		}
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			if (event == MOVING_FROM) {
				return false;
			}
			return true;
		}
		
	}
	
	/**
	 * Default implementation of a runnable which can be used for
	 * {@link ToolController#createCommandRunnable(String, SubmitType)}.
	 * 
	 * Usage: This class is intend to be subclassed.
	 */
	public abstract static class ConsoleCommandRunnable implements IConsoleRunnable {
		
		public static final String TYPE_ID = "common/console/input"; //$NON-NLS-1$
		
		protected final String fText;
		protected String fLabel;
		protected final SubmitType fType;
		
		protected ConsoleCommandRunnable(final String text, final SubmitType type) {
			assert (text != null);
			assert (type != null);
			
			fText = text;
			fType = type;
		}
		
		@Override
		public String getTypeId() {
			return TYPE_ID;
		}
		
		@Override
		public SubmitType getSubmitType() {
			return fType;
		}
		
		public String getCommand() {
			return fText;
		}
		
		@Override
		public String getLabel() {
			if (fLabel == null) {
				fLabel = fText.trim();
			}
			return fLabel;
		}
		
		@Override
		public boolean changed(final int event, final ITool process) {
			return true;
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			((IConsoleService) service).submitToConsole(fText, monitor);
		}
		
	}
	
	protected class StartRunnable implements IConsoleRunnable {
		
		public StartRunnable() {
		}
		
		@Override
		public String getTypeId() {
			return START_TYPE_ID;
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return (tool == getTool());
		}
		
		@Override
		public SubmitType getSubmitType() {
			return SubmitType.CONSOLE;
		}
		
		@Override
		public String getLabel() {
			return Messages.ToolController_CommonStartTask_label;
		}
		
		@Override
		public boolean changed(final int event, final ITool process) {
			if ((event & MASK_EVENT_GROUP) == REMOVING_EVENT_GROUP) {
				return false;
			}
			return true;
		}
		
		@Override
		public void run(final IToolService s,
				final IProgressMonitor monitor) throws CoreException {
		}
		
	};
	
	private class SuspendedInsertRunnable extends ControllerSystemRunnable {
		
		private final int fLevel;
		
		public SuspendedInsertRunnable(final int level) {
			super(SUSPENDED_INSERT_TYPE_ID, "Suspended [" + level + "]");
			fLevel = level;
		}
		
		@Override
		public boolean changed(final int event, final ITool process) {
			switch (event) {
			case REMOVING_FROM:
			case MOVING_FROM:
				return false;
			}
			return true;
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
		}
		
	}
	
	private class SuspendedUpdateRunnable extends ControllerSystemRunnable {
		
		public SuspendedUpdateRunnable() {
			super(SUSPENDED_INSERT_TYPE_ID, "Update Debug Context");
		}
		
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			if (event == MOVING_FROM) {
				return false;
			}
			return true;
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			final IToolRunnable[] runnables = fSuspendUpdateRunnables.toArray();
			for (final IToolRunnable runnable : runnables) {
				try {
					runnable.run(service, monitor);
				}
				catch (final CoreException e) {
					final IStatus status = (e instanceof CoreException) ? e.getStatus() : null;
					if (status != null && (status.getSeverity() == IStatus.CANCEL || status.getSeverity() <= IStatus.INFO)) {
						// ignore
					}
					else {
						NicoPlugin.logError(-1, NLS.bind(
								"An error occurred when running suspend task ''{0}''.", //$NON-NLS-1$
								runnable.getLabel() ), e);
					}
					if (isTerminated()) {
						return;
					}
				}
			}
		}
		
	}
	
	protected abstract class SuspendResumeRunnable extends ControllerSystemRunnable {
		
		private int detail;
		
		public SuspendResumeRunnable(final String id, final String label, final int detail) {
			super(id, label);
			this.detail= detail;
		}
		
		public SubmitType getSubmitType() {
			return SubmitType.OTHER;
		}
		
		protected void setDetail(final int detail) {
			this.detail= detail;
		}
		
		@Override
		public boolean changed(final int event, final ITool process) {
			switch (event) {
			case MOVING_FROM:
				return false;
			case REMOVING_FROM:
			case BEING_ABANDONED:
				if (fSuspendExitRunnable == this) {
					fSuspendExitRunnable = null;
				}
				break;
			default:
				break;
			}
			return true;
		}
		
		@Override
		public void run(final IToolService adapter,
				final IProgressMonitor monitor) throws CoreException {
			fSuspendExitRunnable = this;
			setSuspended(fSuspendedLowerLevel, 0, null);
		}
		
		protected boolean canExec(final IProgressMonitor monitor) throws CoreException {
			return true;
		}
		
		protected abstract void doExec(final IProgressMonitor monitor) throws CoreException;
		
		protected void submitToConsole(final String print, final String send,
				final IProgressMonitor monitor) throws CoreException {
			final IToolRunnable savedCurrentRunnable = fCurrentRunnable;
			setCurrentRunnable(this);
			try {
				fCurrentInput= print;
				doBeforeSubmitL();
			}
			finally {
				setCurrentRunnable(savedCurrentRunnable);
			}
			if (send != null) {
				fCurrentInput= send;
				doSubmitL(monitor);
			}
		}
		
	}
	
	protected class QuitRunnable extends SuspendResumeRunnable {
		
		public QuitRunnable() {
			super(ToolController.QUIT_TYPE_ID, "Quit", DebugEvent.CLIENT_REQUEST);
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			super.run(service, monitor);
			if (!fIsTerminated) {
				try {
					((ToolController) service).doQuitL(monitor);
				}
				catch (final CoreException e) {
					if (!fIsTerminated) {
						handleStatus(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, 0,
								"An error occured when running quit command.", e), monitor);
					}
				}
			}
		}
		
		@Override
		protected void doExec(final IProgressMonitor monitor) throws CoreException {
		}
		
	}
	
	
	public static final String START_TYPE_ID = "common/start"; //$NON-NLS-1$
	public static final String QUIT_TYPE_ID = "common/quit"; //$NON-NLS-1$
	
	public static final String SUSPENDED_INSERT_TYPE_ID = "common/debug/suspended.insert"; //$NON-NLS-1$
	public static final String SUSPENDED_UPDATE_TYPE_ID = "common/debug/suspended.update"; //$NON-NLS-1$
	public static final String SUSPEND_TYPE_ID = "common/debug/suspend"; //$NON-NLS-1$
	public static final String RESUME_TYPE_ID = "common/debug/resume"; //$NON-NLS-1$
	public static final String STEP_INTO_TYPE_ID = "common/debug/step.in"; //$NON-NLS-1$
	public static final String STEP_OVER_TYPE_ID = "common/debug/step.over"; //$NON-NLS-1$
	public static final String STEP_RETURN_TYPE_ID = "common/debug/step.return"; //$NON-NLS-1$
	
	public static final int CANCEL_CURRENT = 	0x00;
	public static final int CANCEL_ALL = 		0x01;
	public static final int CANCEL_PAUSE = 		0x10;
	
	protected static final int SUSPENDED_TOPLEVEL = 0x1;
	protected static final int SUSPENDED_DEEPLEVEL = 0x2;
	
	private ToolStreamProxy streams;
	
	protected final ToolProcess fProcess;
	protected final Queue fQueue;
	
	private IToolRunnable fCurrentRunnable;
	private SubmitType fCurrentSubmitType;
	private final List<IToolRunnable> fControllerRunnables = new ArrayList<IToolRunnable>();
	private IToolRunnable fPostControllerRunnable;
	private RunnableProgressMonitor fRunnableProgressMonitor;
	
	private Thread fControllerThread;
	private ToolStatus fStatus = ToolStatus.STARTING;
	private ToolStatus fStatusPrevious;
	private final FastList<IToolStatusListener> fToolStatusListeners = new FastList<IToolStatusListener>(IToolStatusListener.class, FastList.IDENTITY);
	private final List<DebugEvent> fEventCollector = new LinkedList<DebugEvent>();
	private int fInternalTask;
	private boolean fPauseRequested;
	private boolean fTerminateForced;
	private volatile boolean fIsTerminated;
	private boolean fHotModeDeferred;
	private boolean fHotMode;
	private boolean fHotModeNested = true;
	private final IProgressMonitor fHotModeMonitor = new NullProgressMonitor();
	
	private int fSuspendedRequestLevel;
	private int fLoopCurrentLevel; // only within loop
	private int fSuspendedRunLevel; // also when running exit/continue suspended
	private int fSuspendedLowerLevel;
	private final FastList<IToolRunnable> fSuspendUpdateRunnables = new FastList<IToolRunnable>(IToolRunnable.class);
	private SuspendResumeRunnable fSuspendExitRunnable;
	private int fSuspendEnterDetail;
	private Object fSuspendEnterData;
	private int fSuspendExitDetail;
	
	protected ToolWorkspace fWorkspaceData;
	
	private final Map<String, IToolCommandHandler> fActionHandlers = new HashMap<String, IToolCommandHandler>();
	
	// RunnableAdapter proxy for tool lifecycle thread
	protected String fCurrentInput;
	protected Prompt fCurrentPrompt;
	protected Prompt fDefaultPrompt;
	protected String fLineSeparator;
	
	protected final FastList<IDisposable> fDisposables = new FastList<IDisposable>(IDisposable.class);
	
	
	protected ToolController(final ToolProcess process, final Map<String, Object> initData) {
		fProcess = process;
		fProcess.fInitData = initData;
		
		streams = new ToolStreamProxy();
		
		fQueue = process.getQueue();
		fToolStatusListeners.add(fProcess);
		
		fStatus = ToolStatus.STARTING;
		fRunnableProgressMonitor = new RunnableProgressMonitor(Messages.Progress_Starting_label);
		fCurrentPrompt = Prompt.NONE;
	}
	
	
	public final void addCommandHandler(final String commandId, final IToolCommandHandler handler) {
		fActionHandlers.put(commandId, handler);
	}
	
	public final IToolCommandHandler getCommandHandler(final String commandId) {
		return fActionHandlers.get(commandId);
	}
	
	/**
	 * Adds a tool status listener.
	 * 
	 * @param listener
	 */
	public final void addToolStatusListener(final IToolStatusListener listener) {
		fToolStatusListeners.add(listener);
	}
	
	/**
	 * Removes the tool status listener.
	 * 
	 * @param listener
	 */
	public final void removeToolStatusListener(final IToolStatusListener listener) {
		fToolStatusListeners.remove(listener);
	}
	
	
	protected void setStartupTimestamp(final long timestamp) {
		fProcess.setStartupTimestamp(timestamp);
	}
	
	protected void setStartupWD(final String wd) {
		fProcess.setStartupWD(wd);
	}
	
	protected void addDisposable(final IDisposable disposable) {
		fDisposables.add(disposable);
	}
	protected final Queue getQueue() {
		return fQueue;
	}
	
	public final ToolStatus getStatus() {
		synchronized (fQueue) {
			return fStatus;
		}
	}
	
	protected final ToolStatus getStatusL() {
		return fStatus;
	}
	
	public final IProgressInfo getProgressInfo() {
		return fRunnableProgressMonitor;
	}
	
	protected final Thread getControllerThread() {
		return fControllerThread;
	}
	
	
	public final ToolStreamProxy getStreams() {
		return streams;
	}
	
	@Override
	public ToolProcess getTool() {
		return fProcess;
	}
	
	
	protected Map<String, Object> getInitData() {
		return fProcess.fInitData;
	}
	
	
	/**
	 * Runs the tool.
	 * 
	 * This method should be called only in a thread explicit for this tool process.
	 * The thread exits this method, if the tool is terminated.
	 */
	public final void run() throws CoreException {
		assert (fStatus == ToolStatus.STARTING);
		try {
			fControllerThread = Thread.currentThread();
			setCurrentRunnable(createStartRunnable());
			startToolL(fRunnableProgressMonitor);
			setCurrentRunnable(null);
			synchronized (fQueue) {
				loopChangeStatus((fControllerRunnables.isEmpty()) ?
						ToolStatus.STARTED_IDLING : ToolStatus.STARTED_PROCESSING, null);
			}
			loop();
		}
		finally {
			synchronized (fQueue) {
				if (!fIsTerminated) {
					fIsTerminated = true;
				}
				loopChangeStatus(ToolStatus.TERMINATED, null);
				fQueue.notifyAll();
			}
			clear();
			fControllerThread = null;
		}
	}
	
	/**
	 * Set/unsets the tool in pause mode.
	 * <p>
	 * This implementation don't stop the current calculation, but no new
	 * runnables were started.
	 * 
	 * @param doPause <code>true</code> to switch in pause mode, <code>false</code>
	 * 		to continue the calculation.
	 * @return isPaused()
	 */
	public final boolean pause(final boolean doPause) {
		synchronized (fQueue) {
			if (doPause) {
				if (fStatus.isRunning() || fStatus.isWaiting()) {
					if (!fPauseRequested) {
						fPauseRequested = true;
						statusRequested(ToolStatus.STARTED_PAUSED, true);
					}
					fQueue.notifyAll(); // so we can switch to pause status
				}
				return true;
			}
			else { // !doPause
				if (fStatus == ToolStatus.STARTED_PAUSED) {
					fPauseRequested = false;
					fQueue.notifyAll();
					return true;
				}
				else if (fPauseRequested) {
					fPauseRequested = false;
					statusRequested(ToolStatus.STARTED_PAUSED, false);
					return false;
				}
				return false;
			}
		}
	}
	
	/**
	 * Checks, whether the controller is paused.
	 * It is paused if the tool has the pause status or if the pause status is requested while
	 * the tool is still processing.
	 * 
	 * @return <code>true</code> if paused, otherwise <code>false</code>.
	 */
	public final boolean isPaused() {
		synchronized (fQueue) {
			return (fPauseRequested || fStatus == ToolStatus.STARTED_PAUSED);
		}
	}
	
	public final int getHotTasksState() {
		synchronized (fQueue) {
			if (fHotMode) {
				return (fHotModeNested) ? 2 : 1;
			}
			return 0;
		}
	}
	
	protected final boolean isInHotModeL() {
		return fHotMode;
	}
	
	/**
	 * Returns whether the tool is suspended.
	 * It is suspended if the tool status is {@link ToolStatus#STARTED_SUSPENDED suspended},
	 * but also if it is processing or paused within the suspended mode of the tool (e.g.
	 * evaluations to inspect the objects).
	 * 
	 * @return <code>true</code> if suspended, otherwise <code>false</code>
	 */
	public final boolean isSuspended() {
		synchronized (fQueue) {
			return (fSuspendedRequestLevel > 0 || fLoopCurrentLevel > 0);
		}
	}
	
	/**
	 * {@link #isSuspended()}
	 */
	protected final boolean isSuspendedL() {
		return (fSuspendedRequestLevel > 0 || fLoopCurrentLevel > 0);
	}
	
	/**
	 * Returns the current value of the task counter. The counter is increas
	 * before running a task.
	 *
	 * @return the counter value
	 */
	public int getCounter() {
		return fQueue.counter;
	}
	
	/**
	 * Tries to apply "cancel".
	 * 
	 * The return value signalises if the command was applicable and/or
	 * was succesful (depends on implementation).
	 * 
	 * @return hint about success.
	 */
	public final boolean cancelTask(final int options) {
		synchronized (fQueue) {
			if ((options & CANCEL_ALL) != 0) {
				final List<IToolRunnable> list = fQueue.internalGetList();
				fQueue.remove(list.toArray(new IToolRunnable[list.size()]));
			}
			if ((options & CANCEL_PAUSE) != 0) {
				pause(true);
			}
			fRunnableProgressMonitor.setCanceled(true);
			beginInternalTask();
			
			if (fSuspendedRequestLevel  > fLoopCurrentLevel) {
				setSuspended(fLoopCurrentLevel, 0, null);
				fSuspendExitDetail = DebugEvent.RESUME;
			}
			else if (fLoopCurrentLevel > fSuspendedLowerLevel) {
				setSuspended(fSuspendedLowerLevel, 0, null);
				fSuspendExitDetail = DebugEvent.RESUME;
			}
		}
		
		try {
			interruptTool();
			return true;
		}
		catch (final UnsupportedOperationException e) {
			return false;
		}
		finally {
			synchronized (fQueue) {
				scheduleControllerRunnable(createCancelPostRunnable(options));
				endInternalTask();
			}
		}
	}
	
	/**
	 * Requests to terminate the controller/process asynchronously.
	 * 
	 * Same as ToolProcess.terminate()
	 * The tool will shutdown (after the next runnable) usually.
	 */
	public final void scheduleQuit() {
		synchronized (fQueue) {
			if (fStatus == ToolStatus.TERMINATED) {
				return;
			}
			beginInternalTask();
		}
		
		// ask handler
		boolean schedule = true;
		try {
			final IToolCommandHandler handler = fActionHandlers.get(SCHEDULE_QUIT_EVENT_ID);
			if (handler != null) {
				final Map<String, Object> data = new HashMap<String, Object>();
				data.put("scheduledQuitTasks", getQuitTasks()); //$NON-NLS-1$
				final IStatus status = executeHandler(SCHEDULE_QUIT_EVENT_ID, handler, data, new NullProgressMonitor());
				if (status != null && !status.isOK()) {
					schedule = false;
				}
			}
		}
		finally {
			synchronized (fQueue) {
				if (fStatus != ToolStatus.TERMINATED) {
					if (schedule) {
						final IToolRunnable runnable = createQuitRunnable();
						fQueue.add(runnable);
					}
				}
				endInternalTask();
			}
		}
	}
	
	protected void setTracks(final List<? extends ITrack> tracks) {
		fProcess.setTracks(tracks);
	}
	
	protected final void beginInternalTask() {
		fInternalTask++;
	}
	
	protected final void endInternalTask() {
		fInternalTask--;
		if (fControllerRunnables.size() > 0 || fInternalTask == 0) {
			fQueue.notifyAll();
		}
	}
	
	protected abstract IToolRunnable createStartRunnable();
	
	/**
	 * Creates a runnable to which can quit the tool.
	 * The type should be QUIT_TYPE_ID.
	 * @return
	 */
	protected QuitRunnable createQuitRunnable() {
		return new QuitRunnable();
	}
	
	protected IToolRunnable createCancelPostRunnable(final int options) {
		return null;
	}
	
	/**
	 * Cancels requests to terminate the controller.
	 */
	public final void cancelQuit() {
		synchronized(fQueue) {
			fQueue.remove(getQuitTasks());
			
			if (fStatus == ToolStatus.TERMINATED) {
				return;
			}
		}
		
		// cancel task should not be synch
		final IToolRunnable current = fCurrentRunnable;
		if (current != null && current.getTypeId() == QUIT_TYPE_ID) {
			cancelTask(0);
		}
	}
	
	private final IToolRunnable[] getQuitTasks() {
		final List<IToolRunnable> quit = new ArrayList<IToolRunnable>();
		final IToolRunnable current = fCurrentRunnable;
		if (current != null && current.getTypeId() == QUIT_TYPE_ID) {
			quit.add(current);
		}
		final List<IToolRunnable> list = fQueue.internalGetCurrentList();
		for (final IToolRunnable runnable : list) {
			if (runnable.getTypeId() == QUIT_TYPE_ID) {
				quit.add(runnable);
			}
		}
		return quit.toArray(new IToolRunnable[quit.size()]);
	}
	
	public final void kill(final IProgressMonitor monitor) throws CoreException {
		final Thread thread = getControllerThread();
		killTool(monitor);
		if (thread != null) {
			for (int i = 0; i < 3; i++) {
				if (isTerminated()) {
					return;
				}
				synchronized (fQueue) {
					fQueue.notifyAll();
					try {
						fQueue.wait(10);
					}
					catch (final Exception e) {}
				}
				thread.interrupt();
			}
		}
		if (!isTerminated()) {
			markAsTerminated();
		}
	}
	
	/**
	 * Should be only called inside synchronized(fQueue) blocks.
	 * 
	 * @param newStatus
	 */
	private final void statusRequested(final ToolStatus requestedStatus, final boolean on) {
		final IToolStatusListener[] listeners = fToolStatusListeners.toArray();
		if (on) {
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].controllerStatusRequested(fStatus, requestedStatus, fEventCollector);
			}
		}
		else {
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].controllerStatusRequestCanceled(fStatus, requestedStatus, fEventCollector);
			}
		}
		
		final DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(fEventCollector.toArray(new DebugEvent[fEventCollector.size()]));
		}
		fEventCollector.clear();
	}
	
	/**
	 * Should be only called inside synchronized(fQueue) blocks.
	 * 
	 * @param newStatus
	 */
	private final void loopChangeStatus(final ToolStatus newStatus, RunnableProgressMonitor newMonitor) {
		if (fStatus != newStatus && newMonitor == null) {
			newMonitor = new RunnableProgressMonitor(newStatus.getMarkedLabel());
		}
		
		// update progress info
		if (newMonitor != null) {
			fRunnableProgressMonitor = newMonitor;
		}
		
		// update status
		if (fStatus == newStatus) {
			return;
		}
		
		if (newStatus == ToolStatus.STARTED_PROCESSING
				&& (fStatus != ToolStatus.STARTED_PAUSED || fStatusPrevious != ToolStatus.STARTED_PROCESSING)) {
			fQueue.counterNext= ++fQueue.counter;
			fQueue.internalResetIdle();
		}
		
		fStatusPrevious = fStatus;
		fStatus = newStatus;
		
		// send debug events
		final IToolStatusListener[] listeners = fToolStatusListeners.toArray();
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].controllerStatusChanged(fStatusPrevious, newStatus, fEventCollector);
		}
		final DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(fEventCollector.toArray(new DebugEvent[fEventCollector.size()]));
		}
		fEventCollector.clear();
	}
	
//	protected final void loopBusyChanged(final boolean isBusy) {
//		final IToolStatusListener[] listeners = getToolStatusListeners();
//		for (int i = 0; i < listeners.length; i++) {
//			listeners[i].controllerBusyChanged(isBusy, fEventCollector);
//		}
//		final DebugPlugin manager = DebugPlugin.getDefault();
//		if (manager != null && !fEventCollector.isEmpty()) {
//			manager.fireDebugEventSet(fEventCollector.toArray(new DebugEvent[fEventCollector.size()]));
//		}
//		fEventCollector.clear();
//	}
	
//	public final boolean isStarted() {
//		switch (fStatus) {
//		case STARTED_PROCESSING:
//		case STARTED_IDLING:
//		case STARTED_PAUSED:
//			return true;
//		default:
//			return false;
//		}
//	}
//	
//	public final boolean isTerminated() {
//		return (fStatus == ToolStatus.TERMINATED);
//	}
	
	/**
	 * Only for internal short tasks.
	 */
	protected final void scheduleControllerRunnable(final IToolRunnable runnable) {
		synchronized (fQueue) {
			if (!fControllerRunnables.contains(runnable)) {
				fControllerRunnables.add(runnable);
			}
			if (fStatus != ToolStatus.STARTED_PROCESSING) {
				fQueue.notifyAll();
			}
		}
	}
	
	protected final void addPostControllerRunnable(final IToolRunnable runnable) {
		fPostControllerRunnable = runnable;
	}
	
	protected final void removePostControllerRunnable(final IToolRunnable runnable) {
		if (fPostControllerRunnable == runnable) {
			fPostControllerRunnable = null;
		}
	}
	
	/**
	 * Version for one single text line.
	 * @see #submit(List, SubmitType)
	 * 
	 * @param text a single text line.
	 * @param type type of this submittal.
	 * @return <code>true</code>, if adding commands to queue was successful,
	 * 		otherwise <code>false</code>.
	 */
	public final IStatus submit(final String text, final SubmitType type) {
		return submit(Collections.singletonList(text), type);
	}
	
	/**
	 * Submits one or multiple text lines to the tool.
	 * The texts will be treated as usual commands with console output.
	 * 
	 * @param text array with text lines.
	 * @param type type of this submittal.
	 * @param monitor a monitor for cancel, will not be changed.
	 * @return <code>true</code>, if adding commands to queue was successful,
	 *     otherwise <code>false</code>.
	 */
	public final IStatus submit(final List<String> text, final SubmitType type, final IProgressMonitor monitor) {
		try {
			monitor.beginTask(NicoCoreMessages.SubmitTask_label, 2);
			assert (text != null);
			
			final IToolRunnable[] runs = new IToolRunnable[text.size()];
			for (int i = 0; i < text.size(); i++) {
				runs[i] = createCommandRunnable(text.get(i), type);
			}
			
			if (monitor.isCanceled()) {
				return new Status(IStatus.CANCEL, NicoCore.PLUGIN_ID, -1,
						Messages.ToolController_SubmitCancelled_message, null);
			}
			monitor.worked(1);
			
			return fQueue.add(ImCollections.newList(runs));
		}
		finally {
			monitor.done();
		}
	}
	
	/**
	 * Submits one or multiple text lines to the tool.
	 * The texts will be treated as usual commands with console output.
	 * 
	 * @param text array with text lines.
	 * @param type type of this submittal.
	 * @return <code>true</code>, if adding commands to queue was successful,
	 *     otherwise <code>false</code>.
	 */
	public final IStatus submit(final List<String> text, final SubmitType type) {
		return submit(text, type, fgProgressMonitorDummy);
	}
	
	/**
	 * Implement this method to create a runnable for text commands
	 * (e.g. from console or editor).
	 * 
	 * The runnable should commit this commands to the tool
	 * and print command and results to the console.
	 * Default implementations creates a {@link ConsoleCommandRunnable}.
	 * 
	 * @param command text command
	 * @param type type of this submission
	 * @return runnable for this command
	 */
	public abstract IToolRunnable createCommandRunnable(final String command, final SubmitType type);
	
	
	private final void loop() {
		if (fHotModeDeferred) {
			fHotModeDeferred = false;
			scheduleHotMode();
		}
		boolean enterSuspended = false;
		
		while (true) {
			if (enterSuspended) {
				enterSuspended = false;
				runSuspendedLoopL(SUSPENDED_TOPLEVEL);
			}
			else {
				loopRunTask();
			}
			
			synchronized (fQueue) { // if interrupted run loop, all states are checked
				fQueue.internalCheck();
				
				if (fInternalTask > 0) {
					try {
						fQueue.wait();
					}
					catch (final InterruptedException e) {}
					continue;
				}
				if (fIsTerminated) {
					fProcess.setExitValue(finishToolL());
					loopChangeStatus(ToolStatus.TERMINATED, null);
					return;
				}
				if (fControllerRunnables.size() > 0) {
					continue;
				}
				if (fSuspendedRequestLevel > 0) {
					enterSuspended = true;
					continue;
				}
				if (fPauseRequested) {
					loopChangeStatus(ToolStatus.STARTED_PAUSED, null);
					try {
						fQueue.wait();
					}
					catch (final InterruptedException e) {}
					continue;
				}
				if (fQueue.internalNext() < 0) {
					loopChangeStatus(ToolStatus.STARTED_IDLING, null);
					try {
						fQueue.wait();
					}
					catch (final InterruptedException e) {}
					continue;
				}
			}
		}
	}
	
	private final void loopSuspended(final int level) {
		boolean enterSuspended = false;
		
		while (true) {
			if (enterSuspended) {
				enterSuspended = false;
				runSuspendedLoopL(SUSPENDED_TOPLEVEL);
			}
			else {
				loopRunTask();
			}
			
			synchronized (fQueue) { // if interrupted run loop, all states are checked
				fQueue.internalCheck();
				
				if (fInternalTask > 0) {
					try {
						fQueue.wait();
					}
					catch (final InterruptedException e) {}
					continue;
				}
				if (fIsTerminated) {
					return;
				}
				if (fSuspendedRequestLevel < level) {
					return;
				}
				if (fControllerRunnables.size() > 0) {
					continue;
				}
				if (fSuspendedRequestLevel > level) {
					enterSuspended = true;
					continue;
				}
				if (fPauseRequested) {
					loopChangeStatus(ToolStatus.STARTED_PAUSED, null);
					try {
						fQueue.wait();
					}
					catch (final InterruptedException e) {}
					continue;
				}
				if (fQueue.internalNext() < 0) {
					loopChangeStatus(ToolStatus.STARTED_SUSPENDED, null);
					try {
						fQueue.wait();
					}
					catch (final InterruptedException e) {}
					continue;
				}
			}
		}
	}
	
	private final void loopRunTask() {
		while (true) {
			final int type;
			final IToolRunnable savedCurrentRunnable = fCurrentRunnable;
			synchronized (fQueue) {
				if (fControllerRunnables.size() > 0) {
					type = Queue.RUN_RESERVED;
					setCurrentRunnable(fControllerRunnables.remove(0));
				}
				else if (fLoopCurrentLevel != fSuspendedRequestLevel || fIsTerminated
						|| fInternalTask > 0 || fPauseRequested) {
					return;
				}
				else {
					type = fQueue.internalNext();
					switch (type) {
					case Queue.RUN_HOT:
						break;
					case Queue.RUN_OTHER:
					case Queue.RUN_DEFAULT:
						setCurrentRunnable(fQueue.internalPoll());
						break;
					default:
						return;
					}
				}
				if (type != Queue.RUN_HOT) {
					if (fLoopCurrentLevel > 0) {
						if (type != Queue.RUN_RESERVED
								&& (fCurrentRunnable instanceof ConsoleCommandRunnable)
								&& !runConsoleCommandInSuspend(((ConsoleCommandRunnable) fCurrentRunnable).fText) ) {
							fQueue.counterNext= fQueue.counter--;
							try {
								fQueue.internalFinished(fCurrentRunnable, IToolRunnable.FINISHING_CANCEL);
							}
							finally {
								setCurrentRunnable(savedCurrentRunnable);
							}
							return;
						}
						fSuspendExitDetail = (fCurrentRunnable instanceof ToolController.SuspendResumeRunnable) ?
								((ToolController.SuspendResumeRunnable) fCurrentRunnable).detail :
								DebugEvent.EVALUATION;
					}
					loopChangeStatus(ToolStatus.STARTED_PROCESSING,
							new RunnableProgressMonitor(fCurrentRunnable));
				}
			}
			switch (type) {
			case Queue.RUN_RESERVED:
				try {
					fCurrentRunnable.run(this, fRunnableProgressMonitor);
					safeRunnableChanged(fCurrentRunnable, IToolRunnable.FINISHING_OK);
					continue;
				}
				catch (final Throwable e) {
					final IStatus status = (e instanceof CoreException) ? ((CoreException) e).getStatus() : null;
					if (status != null && (status.getSeverity() == IStatus.CANCEL || status.getSeverity() <= IStatus.INFO)) {
						safeRunnableChanged(fCurrentRunnable, IToolRunnable.FINISHING_CANCEL);
						// ignore
					}
					else {
						NicoPlugin.logError(-1, NLS.bind(
								"An Error occurred when running internal controller task ''{0}''.", //$NON-NLS-1$
								fCurrentRunnable.getLabel() ), e);
						safeRunnableChanged(fCurrentRunnable, IToolRunnable.FINISHING_ERROR);
					}
					
					if (!isToolAlive()) {
						markAsTerminated();
					}
					return;
				}
				finally {
					setCurrentRunnable(savedCurrentRunnable);
					fCurrentSubmitType = null;
					fRunnableProgressMonitor.done();
				}
			case Queue.RUN_HOT:
				try {
					fHotModeNested = false;
					if (!initilizeHotMode()) {
						if (!isToolAlive()) {
							markAsTerminated();
						}
					}
					continue;
				}
				finally {
					fHotModeNested = true;
				}
			case Queue.RUN_OTHER:
			case Queue.RUN_DEFAULT:
				try {
					fCurrentRunnable.run(this, fRunnableProgressMonitor);
					fQueue.internalFinished(fCurrentRunnable, IToolRunnable.FINISHING_OK);
					safeRunnableChanged(fCurrentRunnable, IToolRunnable.FINISHING_OK);
					continue;
				}
				catch (final Throwable e) {
					IStatus status = (e instanceof CoreException) ? ((CoreException) e).getStatus() : null;
					if (status != null && (
							status.getSeverity() == IStatus.CANCEL || status.getSeverity() <= IStatus.INFO)) {
						fQueue.internalFinished(fCurrentRunnable, IToolRunnable.FINISHING_CANCEL);
						safeRunnableChanged(fCurrentRunnable, IToolRunnable.FINISHING_CANCEL);
					}
					else {
						fQueue.internalFinished(fCurrentRunnable, IToolRunnable.FINISHING_ERROR);
						safeRunnableChanged(fCurrentRunnable, IToolRunnable.FINISHING_ERROR);
						status = new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, NicoPlugin.EXTERNAL_ERROR,
								NLS.bind(Messages.ToolRunnable_error_RuntimeError_message,
										new Object[] { fProcess.getLabel(ITool.LONG_LABEL), fCurrentRunnable.getLabel() }),
								e);
						if (type == Queue.RUN_DEFAULT) {
							handleStatus(status, fRunnableProgressMonitor);
						}
						else {
							NicoPlugin.log(status);
						}
					}
					
					if (!isToolAlive()) {
						markAsTerminated();
					}
					return;
				}
				finally {
					if (fPostControllerRunnable != null) {
						synchronized (fQueue) {
							fControllerRunnables.remove(fPostControllerRunnable);
							fControllerRunnables.add(fPostControllerRunnable);
						}
					}
					setCurrentRunnable(savedCurrentRunnable);
					fRunnableProgressMonitor.done();
				}
			}
		}
	}
	
	private void safeRunnableChanged(final IToolRunnable runnable, final int event) {
		try {
			runnable.changed(event, fProcess);
		}
		catch (final Throwable e) {
			NicoPlugin.log(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, NicoPlugin.EXTERNAL_ERROR,
					NLS.bind(Messages.ToolRunnable_error_RuntimeError_message,
							new Object[] { fProcess.getLabel(ITool.LONG_LABEL), runnable.getLabel() }),
					e ));
		}
	}
	
	protected final void scheduleHotMode() {
		final ToolStatus status;
		synchronized (this) {
			status = fStatus;
		}
		switch (status) {
		case TERMINATED:
			return;
		case STARTING:
			fHotModeDeferred = true;
			return;
		default:
			requestHotMode((Thread.currentThread() != fControllerThread));
			return;
		}
	}
	
	protected void requestHotMode(final boolean async) {
	}
	
	protected boolean initilizeHotMode() {
		return true;
	}
	
	protected final void runHotModeLoop() {
		while (true) {
			IToolRunnable runnable;
			synchronized (fQueue) {
				if (fIsTerminated || (runnable = fQueue.internalPollHot()) == null) {
					if (!fIsTerminated && !fHotModeNested) {
						try {
							fQueue.wait(100);
						}
						catch (final InterruptedException e) {
						}
						if (fIsTerminated || (runnable = fQueue.internalPollHot()) == null) {
							fHotMode = false;
							return;
						}
					}
					else {
						fHotMode = false;
						return;
					}
				}
				fHotMode = true;
				fHotModeMonitor.setCanceled(false);
			}
			try {
				runnable.run(this, fHotModeMonitor);
				safeRunnableChanged(runnable, IToolRunnable.FINISHING_OK);
				continue;
			}
			catch (final Throwable e) {
				final IStatus status = (e instanceof CoreException) ? ((CoreException) e).getStatus() : null;
				if (status != null && (status.getSeverity() == IStatus.CANCEL || status.getSeverity() <= IStatus.INFO)) {
					safeRunnableChanged(runnable, IToolRunnable.FINISHING_CANCEL);
					// ignore
				}
				else {
					safeRunnableChanged(runnable, IToolRunnable.FINISHING_ERROR);
					NicoPlugin.logError(-1, "An Error occurred when running hot task.", e); // //$NON-NLS-1$
				}
				
				if (!isToolAlive()) {
					markAsTerminated();
				}
			}
		}
	}
	
	
	protected int setSuspended(final int level, final int enterDetail, final Object enterData) {
		fSuspendedRequestLevel = level;
		fSuspendEnterDetail = enterDetail;
		fSuspendEnterData = enterData;
		return (level - fSuspendedRunLevel);
	}
	
	public void addSuspendUpdateRunnable(final IToolRunnable runnable) {
		fSuspendUpdateRunnables.add(runnable);
	}
	
	protected boolean runConsoleCommandInSuspend(final String input) {
		return true;
	}
	
	protected void scheduleSuspendExitRunnable(final SuspendResumeRunnable runnable) throws CoreException {
		synchronized (fQueue) {
			if (fLoopCurrentLevel == 0) {
				return;
			}
			if (fSuspendExitRunnable != null) {
				fQueue.remove(fSuspendExitRunnable);
				fControllerRunnables.remove(fSuspendExitRunnable);
			}
			fSuspendExitRunnable = runnable;
			if (Thread.currentThread() == fControllerThread) {
				runnable.run(this, null);
			}
			else {
				scheduleControllerRunnable(runnable);
			}
		}
	}
	
	protected void runSuspendedLoopL(final int o) {
		IToolRunnable insertMarker = null;
		IToolRunnable updater = null;
		
		final IToolRunnable savedCurrentRunnable = fCurrentRunnable;
		final RunnableProgressMonitor savedProgressMonitor = fRunnableProgressMonitor;
		final ToolStatus savedStatus = fStatus;
		
		final int savedLower = fSuspendedLowerLevel;
		final int savedLevel = fSuspendedLowerLevel = fSuspendedRunLevel;
		try {
			while (true) {
				final int thisLevel;
				synchronized (fQueue) {
					thisLevel = fSuspendedRequestLevel;
					if (thisLevel <= savedLevel) {
						setSuspended(fSuspendedRequestLevel, 0, null);
						return;
					}
					if (fLoopCurrentLevel != thisLevel || insertMarker == null) {
						fLoopCurrentLevel = fSuspendedRunLevel = thisLevel;
						
						insertMarker = new SuspendedInsertRunnable(thisLevel);
						fQueue.internalAddInsert(insertMarker);
						if (savedLevel == 0 && updater == null) {
							updater = new SuspendedUpdateRunnable();
							fQueue.addOnIdle(updater, 6000);
						}
					}
					fSuspendExitDetail = DebugEvent.UNSPECIFIED;
				}
				
				// run suspended loop
				doRunSuspendedLoopL(o, thisLevel);
				
				// resume main runnable
				final SuspendResumeRunnable runnable;
				synchronized (fQueue) {
					fSuspendExitDetail = DebugEvent.UNSPECIFIED;
					fQueue.counterNext= ++fQueue.counter + 1;
					if (fIsTerminated) {
						setSuspended(0, 0, null);
						return;
					}
					fLoopCurrentLevel = savedLevel;
					fSuspendEnterDetail = DebugEvent.UNSPECIFIED;
					
					fQueue.internalRemoveInsert(insertMarker);
					insertMarker = null;
					
					if (thisLevel <= fSuspendedRequestLevel) {
						continue;
					}
					
					runnable = fSuspendExitRunnable;
					if (runnable != null) {
						fSuspendExitRunnable = null;
						fQueue.remove(runnable);
					}
				}
				
				if (runnable != null) { // resume with runnable
					try {
						setCurrentRunnable((savedCurrentRunnable != null) ?
								savedCurrentRunnable : runnable);
						if (runnable.canExec(savedProgressMonitor)) { // exec resume
							synchronized (fQueue) {
								fSuspendExitDetail = runnable.detail;
								loopChangeStatus(ToolStatus.STARTED_PROCESSING, savedProgressMonitor);
							}
							runnable.doExec(savedProgressMonitor);
						}
						else { // cancel resume
							synchronized (fQueue) {
								fSuspendedRequestLevel = thisLevel;
							}
						}
					}
					catch (final Exception e) {
						NicoPlugin.log(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, 0,
								"An error occurred when executing debug command.", e)); //$NON-NLS-1$
					}
				}
				else { // resume without runnable
					fSuspendExitDetail = DebugEvent.UNSPECIFIED;
					if (savedCurrentRunnable != null) {
						synchronized (fQueue) {
							loopChangeStatus(ToolStatus.STARTED_PROCESSING, savedProgressMonitor);
						}
					}
				}
			}
		}
		finally {
			fSuspendedLowerLevel = savedLower;
			fSuspendedRunLevel = savedLevel;
			setCurrentRunnable(savedCurrentRunnable);
			
			synchronized (fQueue) {
				loopChangeStatus(savedStatus, savedProgressMonitor);
				
				// if not exit normally
				if (fLoopCurrentLevel != savedLevel) {
					fLoopCurrentLevel = savedLevel;
					fSuspendEnterDetail = DebugEvent.UNSPECIFIED;
				}
				if (updater != null) {
					fQueue.removeOnIdle(updater);
				}
				if (insertMarker != null) {
					fQueue.internalRemoveInsert(insertMarker);
				}
				
				fSuspendExitRunnable = null;
				setSuspended(fSuspendedRequestLevel, 0, null);
			}
		}
	}
	
	protected void doRunSuspendedLoopL(final int o, final int level) {
		loopSuspended(level);
	}
	
	protected int getCurrentLevelL() {
		return fLoopCurrentLevel;
	}
	
	protected int getRequestedLevelL() {
		return fSuspendedRequestLevel;
	}
	
	public int getSuspendEnterDetail() {
		return fSuspendEnterDetail;
	}
	
	public Object getSuspendEnterData() {
		return fSuspendEnterData;
	}
	
	public int getSuspendExitDetail() {
		return fSuspendExitDetail;
	}
	
	protected final void markAsTerminated() {
		if (isToolAlive()) {
			NicoPlugin.logError(NicoCore.STATUSCODE_RUNTIME_ERROR, "Illegal state: tool marked as terminated but still alive.", null); //$NON-NLS-1$
		}
		fIsTerminated = true;
	}
	
	protected final boolean isTerminated() {
		return fIsTerminated;
	}
	
	
	/**
	 * Implement here special functionality to start the tool.
	 * 
	 * The method is called automatically in the tool lifecycle thread.
	 * 
	 * @param monitor a progress monitor
	 * 
	 * @throws CoreException with details, if start fails.
	 */
	protected abstract void startToolL(IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Implement here special commands to kill the tool.
	 * 
	 * The method is can be called async.
	 * 
	 * @param a progress monitor
	 */
	protected abstract void killTool(IProgressMonitor monitor);
	
	/**
	 * Checks if the tool is still alive.
	 * 
	 * @return <code>true</code> if ok, otherwise <code>false</code>.
	 */
	protected abstract boolean isToolAlive();
	
	/**
	 * Interrupts the tool. This methods can be called async.
	 */
	protected void interruptTool() throws UnsupportedOperationException {
		final Thread thread = getControllerThread();
		if (thread != null) {
			thread.interrupt();
		}
	}
	
	/**
	 * Is called, after termination is detected.
	 * 
	 * @return exit code
	 */
	protected int finishToolL() {
		return 0;
	}
	
	/**
	 * Implement here special commands to deallocate resources.
	 * 
	 * Call super!
	 * The method is called automatically in the tool lifecycle thread
	 * after the tool is terminated.
	 */
	protected void clear() {
		streams.dispose();
		streams = null;
		
		final IDisposable[] disposables = fDisposables.toArray();
		for (final IDisposable disposable : disposables) {
			try {
				disposable.dispose();
			}
			catch (final Exception e) {
				NicoPlugin.log(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, -1, "An unexepected exception is thrown when disposing a controller extension.", e));
			}
		}
		fDisposables.clear();
	}
	
	@Override
	public void handleStatus(final IStatus status, final IProgressMonitor monitor) {
		if (status == null || status.getSeverity() == IStatus.OK) {
			return;
		}
		final IToolCommandHandler handler = getCommandHandler(REPORT_STATUS_EVENT_ID);
		if (handler != null) {
			final Map<String, Object> data = Collections.singletonMap(REPORT_STATUS_DATA_KEY, (Object) status);
			final IStatus reportStatus = executeHandler(REPORT_STATUS_EVENT_ID, handler, data, monitor);
			if (reportStatus != null && reportStatus.isOK()) {
				return;
			}
		}
		if (status.getSeverity() > IStatus.INFO) {
			NicoPlugin.log(status);
		}
	}
	
	protected IStatus executeHandler(final String commandID, final IToolCommandHandler handler,
			final Map<String, Object> data, final IProgressMonitor monitor) {
		try {
			return handler.execute(commandID, this, data, monitor);
		}
		catch (final Exception e) {
			NicoPlugin.log(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID,
					NLS.bind("An error occurred when executing tool command ''{0}''.", commandID)));
			return null;
		}
	}
	
	
//-- RunnableAdapter - access only in main loop
	
	protected void initRunnableAdapterL() {
		fCurrentPrompt = fDefaultPrompt = fWorkspaceData.getDefaultPrompt();
		fLineSeparator = fWorkspaceData.getLineSeparator();
	}
	
	
	@Override
	public final ToolController getController() {
		return this;
	}
	
	
	private void setCurrentRunnable(final IToolRunnable runnable) {
		fCurrentRunnable = runnable;
		fCurrentSubmitType = getSubmitTypeL(runnable);
	}
	
	protected SubmitType getSubmitTypeL(final IToolRunnable runnable) {
		if (runnable instanceof IConsoleRunnable) {
			return ((IConsoleRunnable) runnable).getSubmitType();
		}
		else if (runnable instanceof ISystemRunnable) {
			return SubmitType.OTHER;
		}
		else {
			return SubmitType.TOOLS;
		}
	}
	
	@Override
	public IToolRunnable getCurrentRunnable() {
		return fCurrentRunnable;
	}
	
	public SubmitType getCurrentSubmitType() {
		return fCurrentSubmitType;
	}
	
	
	public String getProperty(final String key) {
		return null;
	}
	
	@Override
	public final void refreshWorkspaceData(final int options, final IProgressMonitor monitor) throws CoreException {
		fWorkspaceData.controlRefresh(options, this, monitor);
	}
	
	@Override
	public ToolWorkspace getWorkspaceData() {
		return fWorkspaceData;
	}
	
	@Override
	public boolean isDefaultPrompt() {
		return (fDefaultPrompt == fCurrentPrompt); 
	}
	
	@Override
	public Prompt getPrompt() {
		return fCurrentPrompt;
	}
	
	protected void setCurrentPromptL(final Prompt prompt) {
		fCurrentPrompt = prompt;
		fWorkspaceData.controlSetCurrentPrompt(prompt, fStatus);
	}
	
	protected void setDefaultPromptTextL(final String text) {
		fDefaultPrompt = new Prompt(text, IConsoleService.META_PROMPT_DEFAULT);
		fWorkspaceData.controlSetDefaultPrompt(fDefaultPrompt);
	}
	
	protected void setLineSeparatorL(final String newSeparator) {
		fLineSeparator = newSeparator;
		fWorkspaceData.controlSetLineSeparator(newSeparator);
	}
	
	protected void setFileSeparatorL(final char newSeparator) {
		fWorkspaceData.controlSetFileSeparator(newSeparator);
	}
	
	protected void setWorkspaceDirL(final IFileStore directory) {
		fWorkspaceData.controlSetWorkspaceDir(directory);
	}
	
	protected void setRemoteWorkspaceDirL(final IPath directory) {
		fWorkspaceData.controlSetRemoteWorkspaceDir(directory);
	}
	
	@Override
	public void submitToConsole(final String input,
			final IProgressMonitor monitor) throws CoreException {
		fCurrentInput = input;
		doBeforeSubmitL();
		doSubmitL(monitor);
	}
	
	protected void doBeforeSubmitL() {
		final ToolStreamProxy streams = getStreams();
		final SubmitType submitType = getCurrentSubmitType();
		
		streams.getInfoStreamMonitor().append(fCurrentPrompt.text, submitType,
				fCurrentPrompt.meta);
		streams.getInputStreamMonitor().append(fCurrentInput, submitType,
				(fCurrentPrompt.meta & IConsoleService.META_HISTORY_DONTADD) );
		streams.getInputStreamMonitor().append(fWorkspaceData.getLineSeparator(), submitType,
				IConsoleService.META_HISTORY_DONTADD);
	}
	
	protected abstract void doSubmitL(IProgressMonitor monitor) throws CoreException;
	
	protected CoreException cancelTask() {
		return new CoreException(new Status(IStatus.CANCEL, NicoCore.PLUGIN_ID, -1,
				Messages.ToolRunnable_error_RuntimeError_message, null));
	}
	
	protected abstract void doQuitL(IProgressMonitor monitor) throws CoreException;
	
}
