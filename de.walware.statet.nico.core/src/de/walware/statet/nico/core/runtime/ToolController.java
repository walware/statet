/*=============================================================================#
 # Copyright (c) 2005-2016 Stephan Wahlbrink (WalWare.de) and others.
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

import de.walware.jcommons.collections.CopyOnWriteList;
import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.FastList;
import de.walware.ecommons.IDisposable;
import de.walware.ecommons.ts.ISystemReadRunnable;
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
	
	
	private static NullProgressMonitor fgProgressMonitorDummy= new NullProgressMonitor();
	
	
	protected abstract class ControllerSystemRunnable implements ISystemRunnable {
		
		
		private final String fTypeId;
		private final String fLabel;
		
		
		public ControllerSystemRunnable(final String typeId, final String label) {
			this.fTypeId= typeId;
			this.fLabel= label;
		}
		
		
		@Override
		public String getTypeId() {
			return this.fTypeId;
		}
		
		@Override
		public String getLabel() {
			return this.fLabel;
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
		
		public static final String TYPE_ID= "common/console/input"; //$NON-NLS-1$
		
		protected final String fText;
		protected String fLabel;
		protected final SubmitType fType;
		
		protected ConsoleCommandRunnable(final String text, final SubmitType type) {
			assert (text != null);
			assert (type != null);
			
			this.fText= text;
			this.fType= type;
		}
		
		@Override
		public String getTypeId() {
			return TYPE_ID;
		}
		
		@Override
		public SubmitType getSubmitType() {
			return this.fType;
		}
		
		public String getCommand() {
			return this.fText;
		}
		
		@Override
		public String getLabel() {
			if (this.fLabel == null) {
				this.fLabel= this.fText.trim();
			}
			return this.fLabel;
		}
		
		@Override
		public boolean changed(final int event, final ITool process) {
			return true;
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			((IConsoleService) service).submitToConsole(this.fText, monitor);
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
	
	private class SuspendedInsertRunnable extends ControllerSystemRunnable implements ISystemReadRunnable {
		
		private final int level;
		
		public SuspendedInsertRunnable(final int level) {
			super(SUSPENDED_INSERT_TYPE_ID, "Suspended [" + level + "]");
			this.level= level;
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
	
	private class SuspendedUpdateRunnable extends ControllerSystemRunnable implements ISystemReadRunnable {
		
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
			final ImList<ISystemReadRunnable> runnables= ToolController.this.suspendUpdateRunnables.toList();
			for (final ISystemReadRunnable runnable : runnables) {
				try {
					runnable.run(service, monitor);
				}
				catch (final CoreException e) {
					final IStatus status= (e instanceof CoreException) ? e.getStatus() : null;
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
				if (ToolController.this.suspendExitRunnable == this) {
					ToolController.this.suspendExitRunnable= null;
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
			ToolController.this.suspendExitRunnable= this;
			setSuspended(ToolController.this.suspendedLowerLevel, 0, null);
		}
		
		protected boolean canExec(final IProgressMonitor monitor) throws CoreException {
			return true;
		}
		
		protected abstract void doExec(final IProgressMonitor monitor) throws CoreException;
		
		protected void submitToConsole(final String print, final String send,
				final IProgressMonitor monitor) throws CoreException {
			final IToolRunnable savedCurrentRunnable= ToolController.this.currentRunnable;
			setCurrentRunnable(this);
			try {
				ToolController.this.fCurrentInput= print;
				doBeforeSubmitL();
			}
			finally {
				setCurrentRunnable(savedCurrentRunnable);
			}
			if (send != null) {
				ToolController.this.fCurrentInput= send;
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
			if (!ToolController.this.isTerminated) {
				try {
					((ToolController) service).doQuitL(monitor);
				}
				catch (final CoreException e) {
					if (!ToolController.this.isTerminated) {
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
	
	
	public static final String START_TYPE_ID= "common/start"; //$NON-NLS-1$
	public static final String QUIT_TYPE_ID= "common/quit"; //$NON-NLS-1$
	
	public static final String SUSPENDED_INSERT_TYPE_ID= "common/debug/suspended.insert"; //$NON-NLS-1$
	public static final String SUSPENDED_UPDATE_TYPE_ID= "common/debug/suspended.update"; //$NON-NLS-1$
	public static final String SUSPEND_TYPE_ID= "common/debug/suspend"; //$NON-NLS-1$
	public static final String RESUME_TYPE_ID= "common/debug/resume"; //$NON-NLS-1$
	public static final String STEP_INTO_TYPE_ID= "common/debug/step.in"; //$NON-NLS-1$
	public static final String STEP_OVER_TYPE_ID= "common/debug/step.over"; //$NON-NLS-1$
	public static final String STEP_RETURN_TYPE_ID= "common/debug/step.return"; //$NON-NLS-1$
	
	public static final int CANCEL_CURRENT= 	0x00;
	public static final int CANCEL_ALL= 		0x01;
	public static final int CANCEL_PAUSE= 		0x10;
	
	protected static final int SUSPENDED_TOPLEVEL= 0x1;
	protected static final int SUSPENDED_DEEPLEVEL= 0x2;
	
	private ToolStreamProxy streams;
	
	private final ToolProcess process;
	protected final Queue fQueue;
	
	private int counter= 1;
	private int counterNext= this.counter + 1;
	
	private IToolRunnable currentRunnable;
	private SubmitType currentSubmitType;
	private final List<ISystemRunnable> controllerRunnables= new ArrayList<>();
	private ISystemRunnable postControllerRunnable;
	private RunnableProgressMonitor runnableProgressMonitor;
	
	private Thread controllerThread;
	private ToolStatus status= ToolStatus.STARTING;
	private ToolStatus statusPrevious;
	private final FastList<IToolStatusListener> toolStatusListeners= new FastList<>(IToolStatusListener.class, FastList.IDENTITY);
	private final List<DebugEvent> eventCollector= new LinkedList<>();
	private int internalTask;
	private boolean pauseRequested;
	private boolean terminateForced;
	private volatile boolean isTerminated;
	
	private boolean hotModeDeferred;
	private boolean hotMode;
	private boolean hotModeNested= true;
	private final IProgressMonitor hotModeMonitor= new NullProgressMonitor();
	
	private int suspendedRequestLevel;
	private int loopCurrentLevel; // only within loop
	private int suspendedRunLevel; // also when running exit/continue suspended
	private int suspendedLowerLevel;
	private final CopyOnWriteList<ISystemReadRunnable> suspendUpdateRunnables= new CopyOnWriteList<>();
	private SuspendResumeRunnable suspendExitRunnable;
	private int suspendEnterDetail;
	private Object suspendEnterData;
	private int suspendExitDetail;
	
	private ToolWorkspace workspaceData;
	
	private final Map<String, IToolCommandHandler> actionHandlers= new HashMap<>();
	
	// RunnableAdapter proxy for tool lifecycle thread
	protected String fCurrentInput;
	protected Prompt fCurrentPrompt;
	protected Prompt fDefaultPrompt;
	protected String fLineSeparator;
	
	protected final FastList<IDisposable> fDisposables= new FastList<>(IDisposable.class);
	
	
	protected ToolController(final ToolProcess process, final Map<String, Object> initData) {
		this.process= process;
		this.process.fInitData= initData;
		
		this.streams= new ToolStreamProxy();
		
		this.fQueue= process.getQueue();
		this.toolStatusListeners.add(this.process);
		
		this.status= ToolStatus.STARTING;
		this.runnableProgressMonitor= new RunnableProgressMonitor(Messages.Progress_Starting_label);
		this.fCurrentPrompt= Prompt.NONE;
	}
	
	
	protected void setWorksapceData(final ToolWorkspace workspaceData) {
		this.workspaceData= workspaceData;
	}
	
	
	public final void addCommandHandler(final String commandId, final IToolCommandHandler handler) {
		this.actionHandlers.put(commandId, handler);
	}
	
	public final IToolCommandHandler getCommandHandler(final String commandId) {
		return this.actionHandlers.get(commandId);
	}
	
	/**
	 * Adds a tool status listener.
	 * 
	 * @param listener
	 */
	public final void addToolStatusListener(final IToolStatusListener listener) {
		this.toolStatusListeners.add(listener);
	}
	
	/**
	 * Removes the tool status listener.
	 * 
	 * @param listener
	 */
	public final void removeToolStatusListener(final IToolStatusListener listener) {
		this.toolStatusListeners.remove(listener);
	}
	
	
	protected void setStartupTimestamp(final long timestamp) {
		this.process.setStartupTimestamp(timestamp);
	}
	
	protected void setStartupWD(final String wd) {
		this.process.setStartupWD(wd);
	}
	
	protected void addDisposable(final IDisposable disposable) {
		this.fDisposables.add(disposable);
	}
	
	protected final Queue getQueue() {
		return this.fQueue;
	}
	
	public final ToolStatus getStatus() {
		synchronized (this.fQueue) {
			return this.status;
		}
	}
	
	protected final ToolStatus getStatusL() {
		return this.status;
	}
	
	public final IProgressInfo getProgressInfo() {
		return this.runnableProgressMonitor;
	}
	
	protected final Thread getControllerThread() {
		return this.controllerThread;
	}
	
	
	public final ToolStreamProxy getStreams() {
		return this.streams;
	}
	
	@Override
	public ToolProcess getTool() {
		return this.process;
	}
	
	
	protected Map<String, Object> getInitData() {
		return this.process.fInitData;
	}
	
	
	/**
	 * Runs the tool.
	 * 
	 * This method should be called only in a thread explicit for this tool process.
	 * The thread exits this method, if the tool is terminated.
	 */
	public final void run() throws CoreException {
		assert (this.status == ToolStatus.STARTING);
		try {
			this.controllerThread= Thread.currentThread();
			setCurrentRunnable(createStartRunnable());
			startToolL(this.runnableProgressMonitor);
			setCurrentRunnable(null);
			synchronized (this.fQueue) {
				loopChangeStatus((this.controllerRunnables.isEmpty()) ?
						ToolStatus.STARTED_IDLING : ToolStatus.STARTED_PROCESSING, null);
			}
			loop();
		}
		finally {
			synchronized (this.fQueue) {
				if (!this.isTerminated) {
					this.isTerminated= true;
				}
				loopChangeStatus(ToolStatus.TERMINATED, null);
				this.fQueue.notifyAll();
			}
			clear();
			this.controllerThread= null;
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
		synchronized (this.fQueue) {
			if (doPause) {
				if (this.status.isRunning() || this.status.isWaiting()) {
					if (!this.pauseRequested) {
						this.pauseRequested= true;
						statusRequested(ToolStatus.STARTED_PAUSED, true);
					}
					this.fQueue.notifyAll(); // so we can switch to pause status
				}
				return true;
			}
			else { // !doPause
				if (this.status == ToolStatus.STARTED_PAUSED) {
					this.pauseRequested= false;
					this.fQueue.notifyAll();
					return true;
				}
				else if (this.pauseRequested) {
					this.pauseRequested= false;
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
		synchronized (this.fQueue) {
			return (this.pauseRequested || this.status == ToolStatus.STARTED_PAUSED);
		}
	}
	
	public final int getHotTasksState() {
		synchronized (this.fQueue) {
			if (this.hotMode) {
				return (this.hotModeNested) ? 2 : 1;
			}
			return 0;
		}
	}
	
	protected final boolean isInHotModeL() {
		return this.hotMode;
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
		synchronized (this.fQueue) {
			return (this.suspendedRequestLevel > 0 || this.loopCurrentLevel > 0);
		}
	}
	
	/**
	 * {@link #isSuspended()}
	 */
	protected final boolean isSuspendedL() {
		return (this.suspendedRequestLevel > 0 || this.loopCurrentLevel > 0);
	}
	
	/**
	 * Returns the current value of the task counter. The counter is increas
	 * before running a task.
	 *
	 * @return the counter value
	 */
	public int getCounter() {
		return this.counter;
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
		synchronized (this.fQueue) {
			if ((options & CANCEL_ALL) != 0) {
				final List<IToolRunnable> list= this.fQueue.internalGetList();
				this.fQueue.remove(list.toArray(new IToolRunnable[list.size()]));
			}
			if ((options & CANCEL_PAUSE) != 0) {
				pause(true);
			}
			this.runnableProgressMonitor.setCanceled(true);
			beginInternalTask();
			
			if (this.suspendedRequestLevel  > this.loopCurrentLevel) {
				setSuspended(this.loopCurrentLevel, 0, null);
				this.suspendExitDetail= DebugEvent.RESUME;
			}
			else if (this.loopCurrentLevel > this.suspendedLowerLevel) {
				setSuspended(this.suspendedLowerLevel, 0, null);
				this.suspendExitDetail= DebugEvent.RESUME;
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
			synchronized (this.fQueue) {
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
		synchronized (this.fQueue) {
			if (this.status == ToolStatus.TERMINATED) {
				return;
			}
			beginInternalTask();
		}
		
		// ask handler
		boolean schedule= true;
		try {
			final IToolCommandHandler handler= this.actionHandlers.get(SCHEDULE_QUIT_EVENT_ID);
			if (handler != null) {
				final Map<String, Object> data= new HashMap<>();
				data.put("scheduledQuitTasks", getQuitTasks()); //$NON-NLS-1$
				final IStatus status= executeHandler(SCHEDULE_QUIT_EVENT_ID, handler, data, new NullProgressMonitor());
				if (status != null && !status.isOK()) {
					schedule= false;
				}
			}
		}
		finally {
			synchronized (this.fQueue) {
				if (this.status != ToolStatus.TERMINATED) {
					if (schedule) {
						final IToolRunnable runnable= createQuitRunnable();
						this.fQueue.add(runnable);
					}
				}
				endInternalTask();
			}
		}
	}
	
	protected void setTracks(final List<? extends ITrack> tracks) {
		this.process.setTracks(tracks);
	}
	
	protected final void beginInternalTask() {
		this.internalTask++;
	}
	
	protected final void endInternalTask() {
		this.internalTask--;
		if (this.controllerRunnables.size() > 0 || this.internalTask == 0) {
			this.fQueue.notifyAll();
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
	
	protected ISystemRunnable createCancelPostRunnable(final int options) {
		return null;
	}
	
	/**
	 * Cancels requests to terminate the controller.
	 */
	public final void cancelQuit() {
		synchronized(this.fQueue) {
			this.fQueue.remove(getQuitTasks());
			
			if (this.status == ToolStatus.TERMINATED) {
				return;
			}
		}
		
		// cancel task should not be synch
		final IToolRunnable current= this.currentRunnable;
		if (current != null && current.getTypeId() == QUIT_TYPE_ID) {
			cancelTask(0);
		}
	}
	
	private final IToolRunnable[] getQuitTasks() {
		final List<IToolRunnable> quit= new ArrayList<>();
		final IToolRunnable current= this.currentRunnable;
		if (current != null && current.getTypeId() == QUIT_TYPE_ID) {
			quit.add(current);
		}
		final List<IToolRunnable> list= this.fQueue.internalGetCurrentList();
		for (final IToolRunnable runnable : list) {
			if (runnable.getTypeId() == QUIT_TYPE_ID) {
				quit.add(runnable);
			}
		}
		return quit.toArray(new IToolRunnable[quit.size()]);
	}
	
	public final void kill(final IProgressMonitor monitor) throws CoreException {
		final Thread thread= getControllerThread();
		killTool(monitor);
		if (thread != null) {
			for (int i= 0; i < 3; i++) {
				if (isTerminated()) {
					return;
				}
				synchronized (this.fQueue) {
					this.fQueue.notifyAll();
					try {
						this.fQueue.wait(10);
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
		final IToolStatusListener[] listeners= this.toolStatusListeners.toArray();
		if (on) {
			for (int i= 0; i < listeners.length; i++) {
				listeners[i].controllerStatusRequested(this.status, requestedStatus, this.eventCollector);
			}
		}
		else {
			for (int i= 0; i < listeners.length; i++) {
				listeners[i].controllerStatusRequestCanceled(this.status, requestedStatus, this.eventCollector);
			}
		}
		
		final DebugPlugin manager= DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(this.eventCollector.toArray(new DebugEvent[this.eventCollector.size()]));
		}
		this.eventCollector.clear();
	}
	
	/**
	 * Should be only called inside synchronized(fQueue) blocks.
	 * 
	 * @param newStatus
	 */
	private final void loopChangeStatus(final ToolStatus newStatus, RunnableProgressMonitor newMonitor) {
		if (this.status != newStatus && newMonitor == null) {
			newMonitor= new RunnableProgressMonitor(newStatus.getMarkedLabel());
		}
		
		// update progress info
		if (newMonitor != null) {
			this.runnableProgressMonitor= newMonitor;
		}
		
		// update status
		if (this.status == newStatus) {
			return;
		}
		
		if (newStatus == ToolStatus.STARTED_PROCESSING
				&& (this.status != ToolStatus.STARTED_PAUSED || this.statusPrevious != ToolStatus.STARTED_PROCESSING)) {
			this.counterNext= ++this.counter;
			this.fQueue.internalResetIdle();
		}
		
		this.statusPrevious= this.status;
		this.status= newStatus;
		
		// send debug events
		final IToolStatusListener[] listeners= this.toolStatusListeners.toArray();
		for (int i= 0; i < listeners.length; i++) {
			listeners[i].controllerStatusChanged(this.statusPrevious, newStatus, this.eventCollector);
		}
		final DebugPlugin manager= DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(this.eventCollector.toArray(new DebugEvent[this.eventCollector.size()]));
		}
		this.eventCollector.clear();
	}
	
//	protected final void loopBusyChanged(final boolean isBusy) {
//		final IToolStatusListener[] listeners= getToolStatusListeners();
//		for (int i= 0; i < listeners.length; i++) {
//			listeners[i].controllerBusyChanged(isBusy, fEventCollector);
//		}
//		final DebugPlugin manager= DebugPlugin.getDefault();
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
	protected final void scheduleControllerRunnable(final ISystemRunnable runnable) {
		synchronized (this.fQueue) {
			if (!this.controllerRunnables.contains(runnable)) {
				this.controllerRunnables.add(runnable);
			}
			if (this.status != ToolStatus.STARTED_PROCESSING) {
				this.fQueue.notifyAll();
			}
		}
	}
	
	protected final void addPostControllerRunnable(final ISystemRunnable runnable) {
		this.postControllerRunnable= runnable;
	}
	
	protected final void removePostControllerRunnable(final IToolRunnable runnable) {
		if (this.postControllerRunnable == runnable) {
			this.postControllerRunnable= null;
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
			
			final IToolRunnable[] runs= new IToolRunnable[text.size()];
			for (int i= 0; i < text.size(); i++) {
				runs[i]= createCommandRunnable(text.get(i), type);
			}
			
			if (monitor.isCanceled()) {
				return new Status(IStatus.CANCEL, NicoCore.PLUGIN_ID, -1,
						Messages.ToolController_SubmitCancelled_message, null);
			}
			monitor.worked(1);
			
			return this.fQueue.add(ImCollections.newList(runs));
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
	
	
	private void incrementCounter() {
		this.counter= this.counterNext++;
	}
	
	private void incrementCounterForce() {
		this.counterNext= ++this.counter + 1;
	}
	
	private final void loop() {
		if (this.hotModeDeferred) {
			this.hotModeDeferred= false;
			scheduleHotMode();
		}
		boolean enterSuspended= false;
		
		while (true) {
			if (enterSuspended) {
				enterSuspended= false;
				runSuspendedLoopL(SUSPENDED_TOPLEVEL);
			}
			else {
				loopRunTask();
			}
			
			synchronized (this.fQueue) { // if interrupted run loop, all states are checked
				this.fQueue.internalCheck();
				
				if (this.internalTask > 0) {
					try {
						this.fQueue.wait();
					}
					catch (final InterruptedException e) {}
					continue;
				}
				if (this.isTerminated) {
					this.process.setExitValue(finishToolL());
					loopChangeStatus(ToolStatus.TERMINATED, null);
					return;
				}
				if (this.controllerRunnables.size() > 0) {
					continue;
				}
				if (this.suspendedRequestLevel > 0) {
					enterSuspended= true;
					continue;
				}
				if (this.pauseRequested) {
					loopChangeStatus(ToolStatus.STARTED_PAUSED, null);
					try {
						this.fQueue.wait();
					}
					catch (final InterruptedException e) {}
					continue;
				}
				if (this.fQueue.internalNext() < 0) {
					loopChangeStatus(ToolStatus.STARTED_IDLING, null);
					try {
						this.fQueue.wait();
					}
					catch (final InterruptedException e) {}
					continue;
				}
			}
		}
	}
	
	private final void loopSuspended(final int level) {
		boolean enterSuspended= false;
		
		while (true) {
			if (enterSuspended) {
				enterSuspended= false;
				runSuspendedLoopL(SUSPENDED_TOPLEVEL);
			}
			else {
				loopRunTask();
			}
			
			synchronized (this.fQueue) { // if interrupted run loop, all states are checked
				this.fQueue.internalCheck();
				
				if (this.internalTask > 0) {
					try {
						this.fQueue.wait();
					}
					catch (final InterruptedException e) {}
					continue;
				}
				if (this.isTerminated) {
					return;
				}
				if (this.suspendedRequestLevel < level) {
					return;
				}
				if (this.controllerRunnables.size() > 0) {
					continue;
				}
				if (this.suspendedRequestLevel > level) {
					enterSuspended= true;
					continue;
				}
				if (this.pauseRequested) {
					loopChangeStatus(ToolStatus.STARTED_PAUSED, null);
					try {
						this.fQueue.wait();
					}
					catch (final InterruptedException e) {}
					continue;
				}
				if (this.fQueue.internalNext() < 0) {
					loopChangeStatus(ToolStatus.STARTED_SUSPENDED, null);
					try {
						this.fQueue.wait();
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
			final IToolRunnable savedCurrentRunnable= this.currentRunnable;
			synchronized (this.fQueue) {
				if (this.controllerRunnables.size() > 0) {
					type= Queue.RUN_RESERVED;
					setCurrentRunnable(this.controllerRunnables.remove(0));
				}
				else if (this.loopCurrentLevel != this.suspendedRequestLevel || this.isTerminated
						|| this.internalTask > 0 || this.pauseRequested) {
					return;
				}
				else {
					type= this.fQueue.internalNext();
					switch (type) {
					case Queue.RUN_HOT:
						break;
					case Queue.RUN_OTHER:
					case Queue.RUN_DEFAULT:
						setCurrentRunnable(this.fQueue.internalPoll());
						break;
					default:
						return;
					}
				}
				if (type != Queue.RUN_HOT) {
					if (this.loopCurrentLevel > 0) {
						if (type != Queue.RUN_RESERVED
								&& (this.currentRunnable instanceof ConsoleCommandRunnable)
								&& !runConsoleCommandInSuspend(((ConsoleCommandRunnable) this.currentRunnable).fText) ) {
//							this.counterNext= this.counter--;
							try {
								this.fQueue.internalFinished(this.currentRunnable, IToolRunnable.FINISHING_CANCEL);
							}
							finally {
								setCurrentRunnable(savedCurrentRunnable);
							}
							return;
						}
						this.suspendExitDetail= (this.currentRunnable instanceof ToolController.SuspendResumeRunnable) ?
								((ToolController.SuspendResumeRunnable) this.currentRunnable).detail :
								DebugEvent.EVALUATION;
					}
					loopChangeStatus(ToolStatus.STARTED_PROCESSING,
							new RunnableProgressMonitor(this.currentRunnable));
				}
			}
			switch (type) {
			case Queue.RUN_RESERVED:
				try {
					this.currentRunnable.run(this, this.runnableProgressMonitor);
					safeRunnableChanged(this.currentRunnable, IToolRunnable.FINISHING_OK);
					continue;
				}
				catch (final Throwable e) {
					final IStatus status= (e instanceof CoreException) ? ((CoreException) e).getStatus() : null;
					if (status != null && (status.getSeverity() == IStatus.CANCEL || status.getSeverity() <= IStatus.INFO)) {
						safeRunnableChanged(this.currentRunnable, IToolRunnable.FINISHING_CANCEL);
						// ignore
					}
					else {
						NicoPlugin.logError(-1, NLS.bind(
								"An Error occurred when running internal controller task ''{0}''.", //$NON-NLS-1$
								this.currentRunnable.getLabel() ), e);
						safeRunnableChanged(this.currentRunnable, IToolRunnable.FINISHING_ERROR);
					}
					
					if (!isToolAlive()) {
						markAsTerminated();
					}
					return;
				}
				finally {
					setCurrentRunnable(savedCurrentRunnable);
					this.currentSubmitType= null;
					this.runnableProgressMonitor.done();
				}
			case Queue.RUN_HOT:
				try {
					this.hotModeNested= false;
					if (!initilizeHotMode()) {
						if (!isToolAlive()) {
							markAsTerminated();
						}
					}
					continue;
				}
				finally {
					this.hotModeNested= true;
				}
			case Queue.RUN_OTHER:
			case Queue.RUN_DEFAULT:
				try {
					this.currentRunnable.run(this, this.runnableProgressMonitor);
					this.fQueue.internalFinished(this.currentRunnable, IToolRunnable.FINISHING_OK);
					safeRunnableChanged(this.currentRunnable, IToolRunnable.FINISHING_OK);
					continue;
				}
				catch (final Throwable e) {
					IStatus status= (e instanceof CoreException) ? ((CoreException) e).getStatus() : null;
					if (status != null && (
							status.getSeverity() == IStatus.CANCEL || status.getSeverity() <= IStatus.INFO)) {
						this.fQueue.internalFinished(this.currentRunnable, IToolRunnable.FINISHING_CANCEL);
						safeRunnableChanged(this.currentRunnable, IToolRunnable.FINISHING_CANCEL);
					}
					else {
						this.fQueue.internalFinished(this.currentRunnable, IToolRunnable.FINISHING_ERROR);
						safeRunnableChanged(this.currentRunnable, IToolRunnable.FINISHING_ERROR);
						status= new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, NicoPlugin.EXTERNAL_ERROR,
								NLS.bind(Messages.ToolRunnable_error_RuntimeError_message,
										new Object[] { this.process.getLabel(ITool.LONG_LABEL), this.currentRunnable.getLabel() }),
								e);
						if (type == Queue.RUN_DEFAULT) {
							handleStatus(status, this.runnableProgressMonitor);
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
					if (!(this.currentRunnable instanceof ISystemReadRunnable)) {
						incrementCounter();
					}
					if (this.postControllerRunnable != null) {
						synchronized (this.fQueue) {
							this.controllerRunnables.remove(this.postControllerRunnable);
							this.controllerRunnables.add(this.postControllerRunnable);
						}
					}
					setCurrentRunnable(savedCurrentRunnable);
					this.runnableProgressMonitor.done();
				}
			}
		}
	}
	
	private void safeRunnableChanged(final IToolRunnable runnable, final int event) {
		try {
			runnable.changed(event, this.process);
		}
		catch (final Throwable e) {
			NicoPlugin.log(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, NicoPlugin.EXTERNAL_ERROR,
					NLS.bind(Messages.ToolRunnable_error_RuntimeError_message,
							new Object[] { this.process.getLabel(ITool.LONG_LABEL), runnable.getLabel() }),
					e ));
		}
	}
	
	protected final void scheduleHotMode() {
		final ToolStatus status;
		synchronized (this) {
			status= this.status;
		}
		switch (status) {
		case TERMINATED:
			return;
		case STARTING:
			this.hotModeDeferred= true;
			return;
		default:
			requestHotMode((Thread.currentThread() != this.controllerThread));
			return;
		}
	}
	
	protected void requestHotMode(final boolean async) {
	}
	
	protected boolean initilizeHotMode() {
		return true;
	}
	
	private final IToolRunnable pollHotRunnable() {
		IToolRunnable runnable= null;
		if (!this.isTerminated) {
			runnable= this.fQueue.internalPollHot();
			if (runnable == null && !this.hotModeNested) {
				try {
					this.fQueue.wait(100);
				}
				catch (final InterruptedException e) {
				}
				if (!this.isTerminated) {
					runnable= this.fQueue.internalPollHot();
				}
			}
		}
		return runnable;
	}
	
	protected final void runHotModeLoop() {
		while (true) {
			final IToolRunnable runnable;
			synchronized (this.fQueue) {
				runnable= pollHotRunnable();
				if (runnable == null) {
					this.hotMode= false;
					return;
				}
				if (!this.hotMode) {
					this.hotMode= true;
					if (this.hotModeNested && !(this.currentRunnable instanceof ISystemReadRunnable)) {
						incrementCounter();
					}
				}
				this.hotModeMonitor.setCanceled(false);
			}
			try {
				runnable.run(this, this.hotModeMonitor);
				safeRunnableChanged(runnable, IToolRunnable.FINISHING_OK);
				continue;
			}
			catch (final Throwable e) {
				final IStatus status= (e instanceof CoreException) ? ((CoreException) e).getStatus() : null;
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
			finally {
				if (!(runnable instanceof ISystemReadRunnable)) {
					incrementCounter();
				}
			}
		}
	}
	
	
	protected int setSuspended(final int level, final int enterDetail, final Object enterData) {
		this.suspendedRequestLevel= level;
		this.suspendEnterDetail= enterDetail;
		this.suspendEnterData= enterData;
		return (level - this.suspendedRunLevel);
	}
	
	public void addSuspendUpdateRunnable(final ISystemReadRunnable runnable) {
		this.suspendUpdateRunnables.add(runnable);
	}
	
	protected boolean runConsoleCommandInSuspend(final String input) {
		return true;
	}
	
	protected void scheduleSuspendExitRunnable(final SuspendResumeRunnable runnable) throws CoreException {
		synchronized (this.fQueue) {
			if (this.loopCurrentLevel == 0) {
				return;
			}
			if (this.suspendExitRunnable != null) {
				this.fQueue.remove(this.suspendExitRunnable);
				this.controllerRunnables.remove(this.suspendExitRunnable);
			}
			this.suspendExitRunnable= runnable;
			if (Thread.currentThread() == this.controllerThread) {
				runnable.run(this, null);
			}
			else {
				scheduleControllerRunnable(runnable);
			}
		}
	}
	
	protected void runSuspendedLoopL(final int o) {
		IToolRunnable insertMarker= null;
		ISystemReadRunnable updater= null;
		
		final IToolRunnable savedCurrentRunnable= this.currentRunnable;
		final RunnableProgressMonitor savedProgressMonitor= this.runnableProgressMonitor;
		final ToolStatus savedStatus= this.status;
		
		final int savedLower= this.suspendedLowerLevel;
		final int savedLevel= this.suspendedLowerLevel= this.suspendedRunLevel;
		try {
			while (true) {
				final int thisLevel;
				synchronized (this.fQueue) {
					thisLevel= this.suspendedRequestLevel;
					if (thisLevel <= savedLevel) {
						setSuspended(this.suspendedRequestLevel, 0, null);
						return;
					}
					if (this.loopCurrentLevel != thisLevel || insertMarker == null) {
						this.loopCurrentLevel= this.suspendedRunLevel= thisLevel;
						
						insertMarker= new SuspendedInsertRunnable(thisLevel);
						this.fQueue.internalAddInsert(insertMarker);
						if (savedLevel == 0 && updater == null) {
							updater= new SuspendedUpdateRunnable();
							this.fQueue.addOnIdle(updater, 6000);
						}
					}
					this.suspendExitDetail= DebugEvent.UNSPECIFIED;
				}
				
				// run suspended loop
				doRunSuspendedLoopL(o, thisLevel);
				
				// resume main runnable
				final SuspendResumeRunnable runnable;
				synchronized (this.fQueue) {
					this.suspendExitDetail= DebugEvent.UNSPECIFIED;
					incrementCounterForce();
					if (this.isTerminated) {
						setSuspended(0, 0, null);
						return;
					}
					this.loopCurrentLevel= savedLevel;
					this.suspendEnterDetail= DebugEvent.UNSPECIFIED;
					
					this.fQueue.internalRemoveInsert(insertMarker);
					insertMarker= null;
					
					if (thisLevel <= this.suspendedRequestLevel) {
						continue;
					}
					
					runnable= this.suspendExitRunnable;
					if (runnable != null) {
						this.suspendExitRunnable= null;
						this.fQueue.remove(runnable);
					}
				}
				
				if (runnable != null) { // resume with runnable
					try {
						setCurrentRunnable((savedCurrentRunnable != null) ?
								savedCurrentRunnable : runnable);
						if (runnable.canExec(savedProgressMonitor)) { // exec resume
							synchronized (this.fQueue) {
								this.suspendExitDetail= runnable.detail;
								loopChangeStatus(ToolStatus.STARTED_PROCESSING, savedProgressMonitor);
							}
							runnable.doExec(savedProgressMonitor);
						}
						else { // cancel resume
							synchronized (this.fQueue) {
								this.suspendedRequestLevel= thisLevel;
							}
						}
					}
					catch (final Exception e) {
						NicoPlugin.log(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, 0,
								"An error occurred when executing debug command.", e)); //$NON-NLS-1$
					}
				}
				else { // resume without runnable
					this.suspendExitDetail= DebugEvent.UNSPECIFIED;
					if (savedCurrentRunnable != null) {
						synchronized (this.fQueue) {
							loopChangeStatus(ToolStatus.STARTED_PROCESSING, savedProgressMonitor);
						}
					}
				}
			}
		}
		finally {
			this.suspendedLowerLevel= savedLower;
			this.suspendedRunLevel= savedLevel;
			setCurrentRunnable(savedCurrentRunnable);
			
			synchronized (this.fQueue) {
				loopChangeStatus(savedStatus, savedProgressMonitor);
				
				// if not exit normally
				if (this.loopCurrentLevel != savedLevel) {
					this.loopCurrentLevel= savedLevel;
					this.suspendEnterDetail= DebugEvent.UNSPECIFIED;
				}
				if (updater != null) {
					this.fQueue.removeOnIdle(updater);
				}
				if (insertMarker != null) {
					this.fQueue.internalRemoveInsert(insertMarker);
				}
				
				this.suspendExitRunnable= null;
				setSuspended(this.suspendedRequestLevel, 0, null);
			}
		}
	}
	
	protected void doRunSuspendedLoopL(final int o, final int level) {
		loopSuspended(level);
	}
	
	protected int getCurrentLevelL() {
		return this.loopCurrentLevel;
	}
	
	protected int getRequestedLevelL() {
		return this.suspendedRequestLevel;
	}
	
	public int getSuspendEnterDetail() {
		return this.suspendEnterDetail;
	}
	
	public Object getSuspendEnterData() {
		return this.suspendEnterData;
	}
	
	public int getSuspendExitDetail() {
		return this.suspendExitDetail;
	}
	
	protected final void markAsTerminated() {
		if (isToolAlive()) {
			NicoPlugin.logError(NicoCore.STATUSCODE_RUNTIME_ERROR, "Illegal state: tool marked as terminated but still alive.", null); //$NON-NLS-1$
		}
		this.isTerminated= true;
	}
	
	protected final boolean isTerminated() {
		return this.isTerminated;
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
		final Thread thread= getControllerThread();
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
		this.streams.dispose();
		this.streams= null;
		
		final IDisposable[] disposables= this.fDisposables.toArray();
		for (final IDisposable disposable : disposables) {
			try {
				disposable.dispose();
			}
			catch (final Exception e) {
				NicoPlugin.log(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, -1, "An unexepected exception is thrown when disposing a controller extension.", e));
			}
		}
		this.fDisposables.clear();
	}
	
	@Override
	public void handleStatus(final IStatus status, final IProgressMonitor monitor) {
		if (status == null || status.getSeverity() == IStatus.OK) {
			return;
		}
		final IToolCommandHandler handler= getCommandHandler(REPORT_STATUS_EVENT_ID);
		if (handler != null) {
			final Map<String, Object> data= Collections.singletonMap(REPORT_STATUS_DATA_KEY, (Object) status);
			final IStatus reportStatus= executeHandler(REPORT_STATUS_EVENT_ID, handler, data, monitor);
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
		this.fCurrentPrompt= this.fDefaultPrompt= this.workspaceData.getDefaultPrompt();
		this.fLineSeparator= this.workspaceData.getLineSeparator();
	}
	
	
	@Override
	public final ToolController getController() {
		return this;
	}
	
	
	private void setCurrentRunnable(final IToolRunnable runnable) {
		this.currentRunnable= runnable;
		this.currentSubmitType= getSubmitTypeL(runnable);
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
		return this.currentRunnable;
	}
	
	public SubmitType getCurrentSubmitType() {
		return this.currentSubmitType;
	}
	
	
	public String getProperty(final String key) {
		return null;
	}
	
	@Override
	public final void refreshWorkspaceData(final int options, final IProgressMonitor monitor) throws CoreException {
		this.workspaceData.controlRefresh(options, this, monitor);
	}
	
	@Override
	public ToolWorkspace getWorkspaceData() {
		return this.workspaceData;
	}
	
	@Override
	public boolean isDefaultPrompt() {
		return (this.fDefaultPrompt == this.fCurrentPrompt); 
	}
	
	@Override
	public Prompt getPrompt() {
		return this.fCurrentPrompt;
	}
	
	protected void setCurrentPromptL(final Prompt prompt) {
		this.fCurrentPrompt= prompt;
		this.workspaceData.controlSetCurrentPrompt(prompt, this.status);
	}
	
	protected void setDefaultPromptTextL(final String text) {
		this.fDefaultPrompt= new Prompt(text, IConsoleService.META_PROMPT_DEFAULT);
		this.workspaceData.controlSetDefaultPrompt(this.fDefaultPrompt);
	}
	
	protected void setLineSeparatorL(final String newSeparator) {
		this.fLineSeparator= newSeparator;
		this.workspaceData.controlSetLineSeparator(newSeparator);
	}
	
	protected void setFileSeparatorL(final char newSeparator) {
		this.workspaceData.controlSetFileSeparator(newSeparator);
	}
	
	protected void setWorkspaceDirL(final IFileStore directory) {
		this.workspaceData.controlSetWorkspaceDir(directory);
	}
	
	protected void setRemoteWorkspaceDirL(final IPath directory) {
		this.workspaceData.controlSetRemoteWorkspaceDir(directory);
	}
	
	@Override
	public void submitToConsole(final String input,
			final IProgressMonitor monitor) throws CoreException {
		this.fCurrentInput= input;
		doBeforeSubmitL();
		doSubmitL(monitor);
	}
	
	protected void doBeforeSubmitL() {
		final ToolStreamProxy streams= getStreams();
		final SubmitType submitType= getCurrentSubmitType();
		
		streams.getInfoStreamMonitor().append(this.fCurrentPrompt.text, submitType,
				this.fCurrentPrompt.meta);
		streams.getInputStreamMonitor().append(this.fCurrentInput, submitType,
				(this.fCurrentPrompt.meta & IConsoleService.META_HISTORY_DONTADD) );
		streams.getInputStreamMonitor().append(this.workspaceData.getLineSeparator(), submitType,
				IConsoleService.META_HISTORY_DONTADD);
	}
	
	protected abstract void doSubmitL(IProgressMonitor monitor) throws CoreException;
	
	protected CoreException cancelTask() {
		return new CoreException(new Status(IStatus.CANCEL, NicoCore.PLUGIN_ID, -1,
				Messages.ToolRunnable_error_RuntimeError_message, null));
	}
	
	protected abstract void doQuitL(IProgressMonitor monitor) throws CoreException;
	
}
