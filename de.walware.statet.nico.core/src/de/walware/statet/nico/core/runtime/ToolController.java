/*******************************************************************************
 * Copyright (c) 2005-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import static de.walware.statet.nico.core.runtime.IToolEventHandler.REPORT_STATUS_DATA_KEY;
import static de.walware.statet.nico.core.runtime.IToolEventHandler.REPORT_STATUS_EVENT_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ConstList;
import de.walware.ecommons.FastList;
import de.walware.ecommons.IDisposable;

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
 */
public abstract class ToolController<WorkspaceType extends ToolWorkspace>
		implements IToolRunnableControllerAdapter, IAdaptable {
	
	
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
	
	
	/**
	 * Default implementation of a runnable which can be used for
	 * {@link ToolController#createCommandRunnable(String, SubmitType)}.
	 * 
	 * Usage: This class is intend to be subclassed.
	 */
	public static class ConsoleCommandRunnable extends PlatformObject implements IToolRunnable {
		
		public static final String TYPE_ID = "console.text"; //$NON-NLS-1$
		
		protected final String fText;
		protected String fLabel;
		protected final SubmitType fType;
		
		protected ConsoleCommandRunnable(final String text, final SubmitType type) {
			assert (text != null);
			assert (type != null);
			
			fText = text;
			fType = type;
		}
		
		public String getTypeId() {
			return TYPE_ID;
		}
		
		public void changed(final int event, final ToolProcess process) {
		}
		
		public SubmitType getSubmitType() {
			return fType;
		}
		
		public void run(final IToolRunnableControllerAdapter adapter, final IProgressMonitor monitor)
				throws InterruptedException, CoreException {
			adapter.submitToConsole(fText, monitor);
		}
		
		public String getCommand() {
			return fText;
		}
		
		public String getLabel() {
			if (fLabel == null) {
				fLabel = fText.trim();
			}
			return fLabel;
		}
		
	}
	
	protected class StartRunnable implements IToolRunnable {
		
		public StartRunnable() {
		}
		
		public String getTypeId() {
			return START_TYPE_ID;
		}
		
		public SubmitType getSubmitType() {
			return SubmitType.CONSOLE;
		}
		
		public String getLabel() {
			return Messages.ToolController_CommonStartTask_label;
		}
		
		public void changed(final int event, final ToolProcess process) {
		}
		
		public void run(final IToolRunnableControllerAdapter tools, final IProgressMonitor monitor)
				throws InterruptedException, CoreException {
		}
		
	};
	
	
	public static final String START_TYPE_ID = "common/start"; //$NON-NLS-1$
	public static final String QUIT_TYPE_ID = "common/quit"; //$NON-NLS-1$
	
	public static final int CANCEL_CURRENT = 	0x00;
	public static final int CANCEL_ALL = 		0x01;
	public static final int CANCEL_PAUSE = 		0x10;
	
	private ToolStreamProxy fStreams;
	protected ToolStreamMonitor fInputStream;
	protected ToolStreamMonitor fInfoStream;
	protected ToolStreamMonitor fDefaultOutputStream;
	protected ToolStreamMonitor fErrorOutputStream;
	
	protected final ToolProcess fProcess;
	protected final Queue fQueue;
	
	protected IToolRunnable fCurrentRunnable;
	protected IToolRunnable fLastPublicRunnable;
	private IToolRunnable fControllerRunnable;
	private RunnableProgressMonitor fRunnableProgressMonitor;
	
	private Thread fControllerThread;
	private ToolStatus fStatus = ToolStatus.STARTING;
	private final FastList<IToolStatusListener> fToolStatusListeners;
	private List<DebugEvent> fEventCollector = new LinkedList<DebugEvent>();
	private int fInternalTask = 0;
	private boolean fPauseRequested = false;
	private boolean fTerminateForced = false;
	private volatile boolean fIsTerminated = false;
	private boolean fIgnoreRequests = false;
	
	protected WorkspaceType fWorkspaceData;
	
	private Map<String, IToolEventHandler> fHandlers = new HashMap<String, IToolEventHandler>();
	
	// RunnableAdapter proxy for tool lifecycle thread
	protected String fCurrentInput;
	protected Prompt fCurrentPrompt;
	protected Prompt fDefaultPrompt;
	protected String fLineSeparator;
	
	protected final FastList<IDisposable> fDisposables = new FastList<IDisposable>(IDisposable.class);
	
	
	protected ToolController(final ToolProcess process, final Map<String, Object> initData) {
		fProcess = process;
		fProcess.fInitData = initData;
		
		fStreams = new ToolStreamProxy();
		fInputStream = fStreams.getInputStreamMonitor();
		fInfoStream = fStreams.getInfoStreamMonitor();
		fDefaultOutputStream = fStreams.getOutputStreamMonitor();
		fErrorOutputStream = fStreams.getErrorStreamMonitor();
		
		fQueue = process.getQueue();
		fToolStatusListeners = new FastList<IToolStatusListener>(IToolStatusListener.class, FastList.IDENTITY);
		fToolStatusListeners.add(fProcess);
		
		fStatus = ToolStatus.STARTING;
		fRunnableProgressMonitor = new RunnableProgressMonitor(Messages.Progress_Starting_label);
		fCurrentPrompt = Prompt.NONE;
	}
	
	
	public final void addEventHandler(final String eventId, final IToolEventHandler handler) {
		fHandlers.put(eventId, handler);
	}
	
	public final IToolEventHandler getEventHandler(final String eventId) {
		return fHandlers.get(eventId);
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
		return fStatus;
	}
	
	public final IProgressInfo getProgressInfo() {
		return fRunnableProgressMonitor;
	}
	
	protected final Thread getControllerThread() {
		return fControllerThread;
	}
	
	
	public final ToolStreamProxy getStreams() {
		return fStreams;
	}
	
	public final ToolProcess getProcess() {
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
			fCurrentRunnable = createStartRunnable();
			startTool(fRunnableProgressMonitor);
			synchronized (fQueue) {
				loopChangeStatus((fControllerRunnable != null) ? ToolStatus.STARTED_PROCESSING : ToolStatus.STARTED_IDLING, null);
			}
			loop();
		}
		catch (final CoreException e) {
			if (e.getStatus().getSeverity() != IStatus.CANCEL) {
				throw e;
			}
		}
		finally {
			synchronized (fQueue) {
				loopChangeStatus(ToolStatus.TERMINATED, null);
			}
			clear();
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
				if (fStatus == ToolStatus.STARTED_PROCESSING || fStatus == ToolStatus.STARTED_IDLING) {
					if (!fPauseRequested) {
						fPauseRequested = true;
						statusRequested(ToolStatus.STARTED_PAUSED, true);
					}
					resume(); // so we can switch to pause status
				}
				return true;
			}
			else { // !doPause
				if (fStatus == ToolStatus.STARTED_PAUSED) {
					fPauseRequested = false;
					resume();
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
				fQueue.removeElements(list.toArray(new IToolRunnable[list.size()]));
			}
			if ((options & CANCEL_PAUSE) != 0) {
				pause(true);
			}
			fRunnableProgressMonitor.setCanceled(true);
			beginInternalTask();
		}
		
		try {
			interruptTool(0);
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
	 * Checks, wether the controller is paused.
	 * Note that <code>true</code> is also returned, if a pause is requested
	 * but a runnable is still in process.
	 * 
	 * @return <code>true</code> if pause is requested or in pause, otherwise <code>false</code>.
	 */
	public final boolean isPaused() {
		synchronized (fQueue) {
			return (fPauseRequested || fStatus == ToolStatus.STARTED_PAUSED);
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
			final IToolEventHandler handler = fHandlers.get(IToolEventHandler.SCHEDULE_QUIT_EVENT_ID);
			if (handler != null) {
				final Map<String, Object> data = new HashMap<String, Object>();
				data.put("scheduledQuitTasks", getQuitTasks()); //$NON-NLS-1$
				final IStatus answer = handler.handle(IToolEventHandler.SCHEDULE_QUIT_EVENT_ID, this, data, new NullProgressMonitor());
				if (!answer.isOK()) {
					schedule = false;
				}
			}
		}
		finally {
			synchronized (fQueue) {
				if (fStatus != ToolStatus.TERMINATED) {
					if (schedule) {
						final IToolRunnable runnable = createQuitRunnable();
						submit(runnable);
					}
				}
				endInternalTask();
			}
		}
	}
	
	protected void setTracks(List<? extends ITrack> tracks) {
		if (!(tracks instanceof ConstList<?>)) {
			tracks = new ConstList<ITrack>(tracks);
		}
		fProcess.setTracks(tracks);
	}
	
	protected final void beginInternalTask() {
		fInternalTask++;
	}
	
	protected final void endInternalTask() {
		fInternalTask--;
		if (fControllerRunnable != null || fInternalTask == 0) {
			fQueue.notifyAll();
		}
	}
	
	protected abstract IToolRunnable createStartRunnable();
	
	/**
	 * Creates a runnable to which can quit the tool.
	 * The type should be QUIT_TYPE_ID.
	 * @return
	 */
	protected abstract IToolRunnable createQuitRunnable();
	
	protected IToolRunnable createCancelPostRunnable(final int options) {
		return null;
	}
	
	/**
	 * Cancels requests to terminate the controller.
	 */
	public final void cancelQuit() {
		synchronized(fQueue) {
			fQueue.removeElements(getQuitTasks());
		}
		if (fStatus != ToolStatus.TERMINATED) {
			// cancel task should not be synch
			final IToolRunnable current = fLastPublicRunnable;
			if (current != null && current.getTypeId() == QUIT_TYPE_ID) {
				cancelTask(0);
			}
		}
	}
	
	private final IToolRunnable[] getQuitTasks() {
		final List<IToolRunnable> quit = new ArrayList<IToolRunnable>();
		final IToolRunnable current = fLastPublicRunnable;
		if (current != null && current.getTypeId() == QUIT_TYPE_ID) {
			quit.add(current);
		}
		final List<IToolRunnable> list = fQueue.internalGetList();
		for (final IToolRunnable runnable : list) {
			if (runnable.getTypeId() == QUIT_TYPE_ID) {
				quit.add(runnable);
			}
		}
		return quit.toArray(new IToolRunnable[quit.size()]);
	}
	
	public final void kill(final IProgressMonitor monitor) throws CoreException {
		killTool(monitor);
		resume();
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
		final ToolStatus oldStatus = fStatus;
		fStatus = newStatus;
		
		// send debug events
		final IToolStatusListener[] listeners = fToolStatusListeners.toArray();
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].controllerStatusChanged(oldStatus, newStatus, fEventCollector);
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
	 * You must have the lock for the queue.
	 */
	protected final void scheduleControllerRunnable(final IToolRunnable runnable) {
		synchronized (fQueue) {
			if (fControllerRunnable != null) {
				NicoPlugin.log(new Status(IStatus.WARNING, NicoCore.PLUGIN_ID, 
						NLS.bind("Scheduled controller task ''{0}'' was overwritten by ''{1}''.",  //$NON-NLS-1$
								fControllerRunnable.getLabel(), runnable.getLabel())));
			}
			fControllerRunnable = runnable;
			if (fStatus != ToolStatus.STARTED_PROCESSING) {
				fQueue.notifyAll();
			}
		}
	}
	
	/**
	 * Version for one single text line.
	 * @see #submit(String[], SubmitType)
	 * 
	 * @param text a single text line.
	 * @param type type of this submittal.
	 * @return <code>true</code>, if adding commands to queue was successful,
	 * 		otherwise <code>false</code>.
	 */
	public final IStatus submit(final String text, final SubmitType type) {
		return submit(new String[] { text }, type);
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
	public final IStatus submit(final String[] text, final SubmitType type, final IProgressMonitor monitor) {
		try {
			monitor.beginTask(NicoCoreMessages.SubmitTask_label, 3);
			assert (text != null);
			
			synchronized (fQueue) {
				final IStatus status = acceptSubmit();
				if (monitor.isCanceled()) {
					return new Status(IStatus.CANCEL, NicoCore.PLUGIN_ID, -1,
							Messages.ToolController_SubmitCancelled_message, null);
				}
				monitor.worked(1);
				
				if (status.getSeverity() < IStatus.ERROR) {
					final IToolRunnable[] runs = new IToolRunnable[text.length];
					for (int i = 0; i < text.length; i++) {
						runs[i] = createCommandRunnable(text[i], type);
					}
					
					if (monitor.isCanceled()) {
						return new Status(IStatus.CANCEL, NicoCore.PLUGIN_ID, -1,
								Messages.ToolController_SubmitCancelled_message, null);
					}
					monitor.worked(1);
					
					doSubmit(runs);
				}
				return status;
			}
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
	public final IStatus submit(final String[] text, final SubmitType type) {
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
	public IToolRunnable createCommandRunnable(final String command, final SubmitType type) {
		return new ConsoleCommandRunnable(command, type);
	}
	
	/**
	 * Submits the runnable ("task") for the tool.
	 * <p>
	 * The runnable will be added to the queue and will be runned, if it's its turn.
	 * 
	 * @param task the runnable.
	 * @return <code>true</code>, if adding task to queue was successful,
	 *     otherwise <code>false</code>.
	 */
	public final IStatus submit(final IToolRunnable task) {
		return submit(new IToolRunnable[] { task });
	}
	
	/**
	 * Submits the runnables ("task") for the tool.
	 * <p>
	 * The runnables will be added en block to the queue and will be runned, if it's its turn.
	 * 
	 * @param tasks runnables.
	 * @return <code>true</code>, if adding task to queue was successful,
	 *     otherwise <code>false</code>.
	 */
	public final IStatus submit(final IToolRunnable[] tasks) {
		synchronized (fQueue) {
			final IStatus status = acceptSubmit();
			if (status.getSeverity() < IStatus.ERROR) {
				doSubmit(tasks);
			}
			return status;
		}
	}
	
	/**
	 * Runs the runnable ("task"), if the tool is currently on idle.
	 * 
	 * Note: The runnable is always executed asynchronious.
	 * 
	 * @param task the runnable
	 * @return <code>true</code>, if adding task to queue was successful,
	 *     otherwise <code>false</code>.
	 */
	public final boolean runOnIdle(final IToolRunnable task) {
		synchronized (fQueue) {
			if (fStatus == ToolStatus.STARTED_IDLING) {
//				acceptSubmit();
				fIgnoreRequests = true;
				doSubmit(new IToolRunnable[] { task });
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	/**
	 * Should be checked before <code>doSubmit(task)</code>
	 * 
	 * Note: call only inside synchronized(fQueue) block
	 * @return if submit is allowed
	 */
	private final IStatus acceptSubmit() {
		if (fStatus == ToolStatus.TERMINATED) {
			return new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, -1,
					NLS.bind(Messages.ToolController_ToolTerminated_message, getProcess().getToolLabel(false)), null);
		}
		return Status.OK_STATUS;
	}
	
	/**
	 * Note: call only inside synchronized(fQueue) block
	 * @param task
	 */
	private final void doSubmit(final IToolRunnable[] tasks) {
		if (fStatus == ToolStatus.STARTED_IDLING) {
			fQueue.internalAdd(tasks, true);
			resume();
		}
		else {
			fQueue.internalAdd(tasks, false);
		}
	}
	
	
	private final void loop() {
		while (true) {
			loopRunTask();
			
			synchronized (fQueue) {
				fQueue.internalCheck();
				
				if (fInternalTask > 0) {
					try {
						fQueue.wait();
					} catch (final InterruptedException e) {}
					continue;
				}
				if (fIsTerminated) {
					fProcess.fExitValue = finishTool();
					loopChangeStatus(ToolStatus.TERMINATED, null);
					return;
				}
				if (fControllerRunnable != null) {
					continue;
				}
				if (fPauseRequested) {
					loopChangeStatus(ToolStatus.STARTED_PAUSED, null);
					try {
						fQueue.wait();
					} catch (final InterruptedException e) {}
					continue;
				}
				if (fQueue.internalIsEmpty()) {
					loopChangeStatus(ToolStatus.STARTED_IDLING, null);
					try {
						fQueue.wait();
					} catch (final InterruptedException e) {}
					continue;
				}
			}
		}
	}
	
	private final void loopRunTask() {
		while (true) {
			final int isPublic;
			synchronized (fQueue) {
				if (fControllerRunnable != null) {
					fCurrentRunnable = fControllerRunnable;
					fControllerRunnable = null;
					isPublic = 0;
				}
				else {
					if (fIsTerminated || fInternalTask > 0 || fQueue.internalIsEmpty()
							|| (!fIgnoreRequests && fPauseRequested)) {
						return;
					}
					fIgnoreRequests = false;
					fCurrentRunnable = fQueue.internalPoll();
					if (fCurrentRunnable.getSubmitType() == SubmitType.OTHER) {
						isPublic = 1;
					}
					else {
						isPublic = 2;
						fLastPublicRunnable = fCurrentRunnable;
					}
				}
				loopChangeStatus(ToolStatus.STARTED_PROCESSING,
						new RunnableProgressMonitor(fCurrentRunnable));
			}
			if (isPublic >= 1) {
				try {
					// muss nicht synchronisiert werden, da Zugriff nur durch einen Thread
					fCurrentRunnable.run(this, fRunnableProgressMonitor);
					fQueue.internalFinished(fCurrentRunnable, Queue.OK);
				}
				catch (final Throwable e) {
					IStatus status = (e instanceof CoreException) ? ((CoreException) e).getStatus() : null;
					if (status != null && (
							status.getSeverity() == IStatus.CANCEL || status.getSeverity() <= IStatus.INFO)) {
						fQueue.internalFinished(fCurrentRunnable, Queue.CANCEL);
					}
					else {
						fQueue.internalFinished(fCurrentRunnable, Queue.ERROR);
						status = new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, NicoPlugin.EXTERNAL_ERROR,
								NLS.bind(Messages.ToolRunnable_error_RuntimeError_message,
										new Object[] { fProcess.getToolLabel(true), fCurrentRunnable.getLabel() }),
								e);
						if (isPublic >= 2) {
							handleStatus(status, fRunnableProgressMonitor);
						}
						else {
							NicoPlugin.getDefault().getLog().log(status);
						}
					}
					
					if (!isToolAlive()) {
						markAsTerminated();
					}
					return;
				}
				finally {
					fRunnableProgressMonitor.done();
				}
			}
			else {
				try {
					fCurrentRunnable.run(this, fRunnableProgressMonitor);
				}
				catch (final Throwable e) {
					final IStatus status = (e instanceof CoreException) ? ((CoreException) e).getStatus() : null;
					if (status != null && (status.getSeverity() == IStatus.CANCEL || status.getSeverity() <= IStatus.INFO)) {
						// ignore
					}
					else {
						NicoPlugin.logError(-1, "An Error occurred when running internal controller task.", e); // //$NON-NLS-1$
					}
					
					if (!isToolAlive()) {
						markAsTerminated();
					}
					return;
				}
			}
		}
	}
	
	protected final void resume() {
		synchronized (fQueue) {
			fQueue.notifyAll();
		}
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
	protected abstract void startTool(IProgressMonitor monitor) throws CoreException;
	
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
	 * Interrupts the tool. This methods is called async.
	 * 
	 * Predefined degree of hardness:
	 *   0    - while cancelation
	 *   5..9 - while forced termination
	 * 
	 * @param hardness degree of hardness
	 */
	protected void interruptTool(final int hardness) throws UnsupportedOperationException {
		getControllerThread().interrupt();
	}
	
	/**
	 * Is called, after termination is detected.
	 * 
	 * @return exit code
	 */
	protected int finishTool() {
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
		fStreams.dispose();
		fStreams = null;
		fInputStream = null;
		fInfoStream = null;
		fDefaultOutputStream = null;
		fErrorOutputStream = null;
		
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
	
	public void handleStatus(final IStatus status, final IProgressMonitor monitor) {
		if (status == null || status.getSeverity() == IStatus.OK) {
			return;
		}
		final IToolEventHandler eventHandler = getEventHandler(REPORT_STATUS_EVENT_ID);
		if (eventHandler != null) {
			final Map<String, Object> data = Collections.singletonMap(REPORT_STATUS_DATA_KEY, (Object) status);
			eventHandler.handle(REPORT_STATUS_EVENT_ID, this, data, monitor);
		}
		else {
			if (status.getSeverity() > IStatus.INFO) {
				NicoPlugin.getDefault().getLog().log(status);
			}
		}
	}
	
	
//-- RunnableAdapter - access only in main loop
	
	protected void initRunnableAdapter() {
		fCurrentPrompt = fDefaultPrompt = fWorkspaceData.getDefaultPrompt();
		fLineSeparator = fWorkspaceData.getLineSeparator();
	}
	
	
	public final ToolController getController() {
		return this;
	}
	
	public final void refreshWorkspaceData(final int options, final IProgressMonitor monitor) throws CoreException {
		fWorkspaceData.controlRefresh(options, this, monitor);
	}
	
	public ToolWorkspace getWorkspaceData() {
		return fWorkspaceData;
	}
	
	public IToolRunnable getCurrentRunnable() {
		return fCurrentRunnable;
	}
	
	public void setCurrentPrompt(final Prompt prompt) {
		fCurrentPrompt = prompt;
		fWorkspaceData.controlSetCurrentPrompt(prompt, fStatus);
	}
	
	public void setDefaultPromptText(final String text) {
		fDefaultPrompt = new Prompt(text, IToolRunnableControllerAdapter.META_PROMPT_DEFAULT);
		fWorkspaceData.controlSetDefaultPrompt(fDefaultPrompt);
	}
	
	public void setLineSeparator(final String newSeparator) {
		fLineSeparator = newSeparator;
		fWorkspaceData.controlSetLineSeparator(newSeparator);
	}
	
	public void setWorkspaceDir(final IFileStore directory) {
		fWorkspaceData.controlSetWorkspaceDir(directory);
	}
	
	public void setRemoteWorkspaceDir(final IPath directory) {
		fWorkspaceData.controlSetRemoteWorkspaceDir(directory);
	}
	
	public void submitToConsole(final String input, final IProgressMonitor monitor)
			throws CoreException {
		fCurrentInput = input;
		doBeforeSubmit();
		doSubmit(monitor);
	}
	
	protected void doBeforeSubmit() {
		final SubmitType type = fCurrentRunnable.getSubmitType();
		fInfoStream.append(fCurrentPrompt.text, type,
				fCurrentPrompt.meta);
		fInputStream.append(fCurrentInput, type,
				(fCurrentPrompt.meta & IToolRunnableControllerAdapter.META_HISTORY_DONTADD) );
		fInputStream.append(fWorkspaceData.getLineSeparator(), type,
				IToolRunnableControllerAdapter.META_HISTORY_DONTADD);
	}
	
	protected CoreException cancelTask() {
		return new CoreException(new Status(IStatus.CANCEL, NicoCore.PLUGIN_ID, -1,
				Messages.ToolRunnable_error_RuntimeError_message, null));
	}
	
	protected abstract void doSubmit(IProgressMonitor monitor)
			throws CoreException;
	
}
