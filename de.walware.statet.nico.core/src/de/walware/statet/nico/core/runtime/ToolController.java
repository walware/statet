/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.osgi.util.NLS;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.NicoCoreMessages;
import de.walware.statet.nico.internal.core.Messages;
import de.walware.statet.nico.internal.core.NicoPlugin;
import de.walware.statet.nico.internal.core.RunnableProgressMonitor;


/**
 * Controller for a long running tight integrated tool.
 * <p>
 * Usage: This class is intend to be subclass. Subclasses are responsible for the
 * lifecicle of the tool (<code>startTool()</code>, <code>terminateTool()</code>.
 * Subclasses should provide an interface which can be used by IToolRunnables
 * to access the features of the tool. E.g. provide an abstract implementation of
 * IToolRunnable with the necessary methods (in protected scope).</p>
 */
public abstract class ToolController<
		RunnableAdapterType extends IToolRunnableControllerAdapter,
		WorkspaceType extends ToolWorkspace> {

	
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
	}
	

	private static NullProgressMonitor fgProgressMonitorDummy = new NullProgressMonitor();
	
	
	protected abstract class RunnableAdapter implements IToolRunnableControllerAdapter {
		
//		 Proxy for tool lifecycle thread
		protected Prompt fPrompt;
		protected Prompt fDefaultPrompt;
		protected String fLineSeparator;
		
		
		protected RunnableAdapter() {
			fPrompt = fDefaultPrompt = fWorkspaceData.getDefaultPrompt();
			fLineSeparator = fWorkspaceData.getLineSeparator();
		}
		

		public ToolController getController() {
			return ToolController.this;
		}
				
		public ToolWorkspace getWorkspaceData() {
			return fWorkspaceData;
		}
		
		public void setPrompt(Prompt prompt) {
			fPrompt = prompt;
			fWorkspaceData.setCurrentPrompt(prompt, fStatus);
		}
		
		public void setDefaultPromptText(String text) {
			fDefaultPrompt = new Prompt(text, IToolRunnableControllerAdapter.META_PROMPT_DEFAULT);
			fWorkspaceData.setDefaultPrompt(fDefaultPrompt);
		}
		
		public void setLineSeparator(String newSeparator) {
			fLineSeparator = newSeparator;
			fWorkspaceData.setLineSeparator(newSeparator);
		}
		
		public void setWorkspaceDir(IFileStore directory) {
			fWorkspaceData.setWorkspaceDir(directory);
		}
		
		public void submitToConsole(String input, IProgressMonitor monitor)
				throws InterruptedException, CoreException {
			doBeforeSubmit(input);
			Prompt prompt = doSubmit(input, monitor);
			setPrompt(prompt);
		}
		
		protected void doBeforeSubmit(String input) {
			SubmitType type = fCurrentRunnable.getSubmitType();
			fInfoStream.append(fPrompt.text, type,
					fPrompt.meta);
			fInputStream.append(input, type,
					(fPrompt.meta & IToolRunnableControllerAdapter.META_HISTORY_DONTADD) );
			fInputStream.append(fWorkspaceData.getLineSeparator(), type,
					IToolRunnableControllerAdapter.META_HISTORY_DONTADD);
		}
		
		protected abstract Prompt doSubmit(String input, IProgressMonitor monitor)
				throws InterruptedException, CoreException;
	}
	
	/**
	 * Default implementation of a runnable which can be used for
	 * {@link ToolController#createCommandRunnable(String, SubmitType)}.
	 * 
	 * Usage: This class is intend to be subclassed.
	 */
	public static class ConsoleCommandRunnable<T extends IToolRunnableControllerAdapter>
			extends PlatformObject implements IToolRunnable {
		
		public static final String TYPE_ID = "console.text";
		
		protected final String fText;
		protected String fLabel;
		protected final SubmitType fType;
		
		protected ConsoleCommandRunnable(String text, SubmitType type) {
			assert (text != null);
			assert (type != null);
			
			fText = text;
			fType = type;
		}

		public void run(IToolRunnableControllerAdapter tools, IProgressMonitor monitor)
				throws InterruptedException, CoreException {
			tools.submitToConsole(fText, monitor);
		}
		
		public String getCommand() {
			return fText;
		}
		
		public String getTypeId() {
			return TYPE_ID;
		}
		
		public String getLabel() {
			if (fLabel == null) {
				fLabel = fText.trim();
			}
			return fLabel;
		}
		
		public SubmitType getSubmitType() {
			return fType;
		}
	}
	
	
	public static final String QUIT_TYPE_ID = "common/quit"; //$NON-NLS-1$
	
	public static final String LOGIN_EVENT_ID = "common/login"; //$NON-NLS-1$
	public static final String SCHEDULE_QUIT_EVENT_ID = "common/scheduleQuit"; //$NON-NLS-1$
	
	
	private ToolStreamProxy fStreams;
	protected ToolStreamMonitor fInputStream;
	protected ToolStreamMonitor fInfoStream;
	protected ToolStreamMonitor fDefaultOutputStream;
	protected ToolStreamMonitor fErrorOutputStream;
	
	protected final ToolProcess fProcess;
	private Queue fQueue;

	protected IToolRunnable<RunnableAdapterType> fCurrentRunnable;
	private RunnableProgressMonitor fRunnableProgressMonitor;
	 
	private Thread fControllerThread;
	private ToolStatus fStatus = ToolStatus.STARTING;
	private IToolStatusListener[] fToolStatusListeners;
	private List<DebugEvent> fEventCollector = new LinkedList<DebugEvent>();
	private int fInternalTask = 0;
	private boolean fPauseRequested = false;
	private boolean fTerminateForced = false;
	private volatile boolean fIsTerminated = false;
	private boolean fIgnoreRequests = false;

	protected WorkspaceType fWorkspaceData;
	protected RunnableAdapterType fRunnableAdapter;
	
	private Map<String, IToolEventHandler> fHandlers = new HashMap<String, IToolEventHandler>();
	
	
	protected ToolController(ToolProcess process) {
		fProcess = process;
		
		fStreams = new ToolStreamProxy();
		fInputStream = fStreams.getInputStreamMonitor();
		fInfoStream = fStreams.getInfoStreamMonitor();
		fDefaultOutputStream = fStreams.getOutputStreamMonitor();
		fErrorOutputStream = fStreams.getErrorStreamMonitor();
		
		fQueue = new Queue();
		fToolStatusListeners = new IToolStatusListener[] { fProcess };
		
		fStatus = ToolStatus.STARTING;
		fRunnableProgressMonitor = new RunnableProgressMonitor(Messages.Progress_Starting_label);
	}
	
	
	public void addEventHandler(String eventId, IToolEventHandler handler) {
		fHandlers.put(eventId, handler);
	}
	
	public IToolEventHandler getEventHandler(String eventId) {
		return fHandlers.get(eventId);
	}
	
	/**
	 * Adds a tool status listener.
	 * 
	 * It's only allowed to do this in the tool lifecycle thread
	 * (not checked)!
	 * 
	 * @param listener
	 */
	protected void addToolStatusListener(IToolStatusListener listener) {
		IToolStatusListener[] list = new IToolStatusListener[fToolStatusListeners.length+1];
		System.arraycopy(fToolStatusListeners, 0, list, 0, fToolStatusListeners.length);
		list[fToolStatusListeners.length] = listener;
		fToolStatusListeners = list;
	}
	
	protected Queue getQueue() {
		return fQueue;
	}
	
	public ToolStatus getStatus() {
		return fStatus;
	}
	
	public IProgressInfo getProgressInfo() {
		return fRunnableProgressMonitor;
	}
	
	protected Thread getControllerThread() {
		return fControllerThread;
	}
	
	
	public ToolStreamProxy getStreams() {
		return fStreams;
	}
	
	public ToolProcess getProcess() {
		return fProcess;
	}
	
	
	/**
	 * Runs the tool.
	 * 
	 * This method should be called only in a thread explicit for this tool process.
	 * The thread exits this method, if the tool is terminated.
	 */
	public void run() throws CoreException {
		assert (fStatus == ToolStatus.STARTING);
		try {
			fControllerThread = Thread.currentThread();
			startTool(fRunnableProgressMonitor);
			synchronized (fQueue) {
				loopChangeStatus(ToolStatus.STARTED_IDLING, null);
			}
			loop();
		}
		catch (InterruptedException e) { // start interrupted
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
	public boolean pause(boolean doPause) {
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
	public boolean cancelTask() {
		synchronized (fQueue) {
			if (fStatus != ToolStatus.STARTED_PROCESSING || fCurrentRunnable == null) {
				return false;
			}
			fRunnableProgressMonitor.setCanceled(true);
			try {
				interruptTool(0);
			}
			catch (UnsupportedOperationException e) {
				return false;
			}
			return true;
		}
	}
	
	/**
	 * Checks, wether the controller is paused.
	 * Note that <code>true</code> is also returned, if a pause is requested
	 * but a runnable is still in process.
	 * 
	 * @return <code>true</code> if pause is requested or in pause, otherwise <code>false</code>.
	 */
	public boolean isPaused() {
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
	public void scheduleQuit() {
		synchronized (fQueue) {
			if (fStatus == ToolStatus.TERMINATED) {
				return;
			}
			fInternalTask++;
		}
		
		// ask handler
		boolean schedule = true;
		IToolEventHandler handler = fHandlers.get(SCHEDULE_QUIT_EVENT_ID);
		if (handler != null) {
			int answer = handler.handle(fRunnableAdapter, getQuitTasks());
			if (answer != IToolEventHandler.OK) {
				schedule = false;
			}
		}
		
		synchronized (fQueue) {
			fInternalTask--;
			if (fStatus != ToolStatus.TERMINATED) {
				if (schedule) {
					IToolRunnable runnable = createQuitRunnable();
					submit(runnable);
				}
			}
			fQueue.notifyAll();
		}
	}
	
	/**
	 * Creates a runnable to which can quit the tool.
	 * The type should be QUIT_TYPE_ID.
	 * @return
	 */
	protected abstract IToolRunnable createQuitRunnable();
	
	/**
	 * Cancels requests to termate the controller.
	 */
	public void cancelQuit() {
		if (fStatus != ToolStatus.TERMINATED) {
			IToolRunnable current = fCurrentRunnable;
			if (current != null && current.getTypeId() == QUIT_TYPE_ID) {
				cancelTask();
			}
		}		
		synchronized(fQueue) {
			fQueue.removeElements(getQuitTasks());
		}
	}
	
	private IToolRunnable[] getQuitTasks() {
		List<IToolRunnable> quit = new ArrayList<IToolRunnable>();
		IToolRunnable current = fCurrentRunnable;
		if (current != null && current.getTypeId() == QUIT_TYPE_ID) {
			quit.add(current);
		}
		List<IToolRunnable> list = fQueue.internalGetList();
		for (IToolRunnable runnable : list) {
			if (runnable.getTypeId().equals(QUIT_TYPE_ID)) {
				quit.add(runnable);
			}
		}
		return quit.toArray(new IToolRunnable[quit.size()]);
	}
	
	public void kill(IProgressMonitor monitor) throws CoreException {
		killTool(monitor);
		resume();
	}
	
	/**
	 * Should be only called inside synchronized(fQueue) blocks.
	 * 
	 * @param newStatus
	 */
	private void statusRequested(ToolStatus requestedStatus, boolean on) {
		if (on) {
			for (IToolStatusListener listener : fToolStatusListeners) {
				listener.controllerStatusRequested(fStatus, requestedStatus, fEventCollector);
			}
		}
		else {
			for (IToolStatusListener listener : fToolStatusListeners) {
				listener.controllerStatusRequestCanceled(fStatus, requestedStatus, fEventCollector);
			}
		}
		
		DebugPlugin manager = DebugPlugin.getDefault();
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
	private void loopChangeStatus(ToolStatus newStatus, RunnableProgressMonitor newMonitor) {
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
		ToolStatus oldStatus = fStatus;
		fStatus = newStatus;
	
		// send debug events
		for (IToolStatusListener listener : fToolStatusListeners) {
			listener.controllerStatusChanged(oldStatus, newStatus, fEventCollector);
		}
		DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(fEventCollector.toArray(new DebugEvent[fEventCollector.size()]));
		}
		fEventCollector.clear();
	}
	
//	public boolean isStarted() {
//
//		switch (fStatus) {
//		case STARTED_PROCESSING:
//		case STARTED_IDLING:
//		case STARTED_PAUSED:
//			return true;
//
//		default:
//			return false;
//		}
//	}
//
//	public boolean isTerminated() {
//
//		return (fStatus == ToolStatus.TERMINATED);
//	}
	
	/**
	 * Version for one single text line.
	 * @see #submit(String[], SubmitType)
	 * 
	 * @param text a single text line.
	 * @param type type of this submittal.
	 * @return <code>true</code>, if adding commands to queue was successful,
	 * 		otherwise <code>false</code>.
	 */
	public boolean submit (String text, SubmitType type) {
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
	 * 		otherwise <code>false</code>.
	 * 
	 * @throws InterruptedException if action is interupted via monitor.
	 */
	public boolean submit(String[] text, SubmitType type, IProgressMonitor monitor) {
		try {
			monitor.beginTask(NicoCoreMessages.SubmitTask_label, 3);
			assert (text != null);

			synchronized (fQueue) {
				if (acceptSubmit()) {
					if (monitor.isCanceled()) {
						return false;
					}
					monitor.worked(1);
					
					IToolRunnable[] runs = new IToolRunnable[text.length];
					for (int i = 0; i < text.length; i++) {
						runs[i] = createCommandRunnable(text[i], type);
					}
	
					if (monitor.isCanceled()) {
						return false;
					}
					monitor.worked(1);
	
					doSubmit(runs);
					return true;
				}
				else {
					return false;
				}
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
	 * 		otherwise <code>false</code>.
	 */
	public boolean submit(String[] text, SubmitType type) {
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
	protected IToolRunnable createCommandRunnable(String command, SubmitType type) {
		return new ConsoleCommandRunnable(command, type);
	}
	
	/**
	 * Submits the runnable ("task") for the tool.
	 * <p>
	 * The runnable will be added to the queue and will be runned, if it's its turn.
	 * 
	 * @param task the runnable.
	 * @return <code>true</code>, if adding task to queue was successful,
	 * 		otherwise <code>false</code>.
	 */
	public boolean submit(IToolRunnable task) {
		return submit(new IToolRunnable[] { task });
	}

	/**
	 * Submits the runnables ("task") for the tool.
	 * <p>
	 * The runnables will be added en block to the queue and will be runned, if it's its turn.
	 * 
	 * @param tasks runnables.
	 * @return <code>true</code>, if adding task to queue was successful,
	 * 		otherwise <code>false</code>.
	 */
	public boolean submit(IToolRunnable[] tasks) {
		synchronized (fQueue) {
			if (acceptSubmit()) {
				doSubmit(tasks);
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	/**
	 * Runs the runnable ("task"), if the tool is currently on idle.
	 * 
	 * Note: The runnable is always executed asynchronious.
	 * 
	 * @param task the runnable
	 * @return <code>true</code>, if adding task to queue was successful,
	 * 		otherwise <code>false</code>.
	 */
	public boolean runOnIdle(IToolRunnable task) {
		synchronized (fQueue) {
			if (acceptSubmit() && fStatus == ToolStatus.STARTED_IDLING) {
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
	private boolean acceptSubmit() {
		if (fStatus == ToolStatus.TERMINATED) {
			return false;
		}
		return true;
	}
	
	/**
	 * Note: call only inside synchronized(fQueue) block
	 * @param task
	 */
	private void doSubmit(IToolRunnable[] tasks) {
		if (fStatus == ToolStatus.STARTED_IDLING) {
			fQueue.internalAdd(tasks, true);
			resume();
		}
		else {
			fQueue.internalAdd(tasks, false);
		}
	}
	
	
	private void loop() {
		while (true) {
			while (loopRunTask()) {}
			
			synchronized (fQueue) {
				fQueue.internalCheck();
				
				if (fInternalTask > 0) {
					loopWait();
					continue;
				}
				if (fIsTerminated) {
					loopChangeStatus(ToolStatus.TERMINATED, null);
					return;
				}
				if (fPauseRequested) {
					loopChangeStatus(ToolStatus.STARTED_PAUSED, null);
					loopWait();
					continue;
				}
				if (fQueue.internalIsEmpty()) {
					loopChangeStatus(ToolStatus.STARTED_IDLING, null);
					loopWait();
					continue;
				}
			}
		}
	}
	
	private boolean loopRunTask() {
		synchronized (fQueue) {
			if (fIsTerminated || fInternalTask > 0 || fQueue.internalIsEmpty()
					|| (!fIgnoreRequests && fPauseRequested)) {
				return false;
			}
			fIgnoreRequests = false;
			fCurrentRunnable = fQueue.internalPoll();
			loopChangeStatus(ToolStatus.STARTED_PROCESSING,
					new RunnableProgressMonitor(fCurrentRunnable));
		}

		try {
			// muss nicht synchronisiert werden, da Zugriff nur durch einen Thread
			fCurrentRunnable.run(fRunnableAdapter, fRunnableProgressMonitor);
			fQueue.internalFinished(fCurrentRunnable, Queue.OK);
		}
		catch (Exception e) {
			if (e instanceof InterruptedException) {
				fQueue.internalFinished(fCurrentRunnable, Queue.CANCEL);
				if (!isToolAlive()) {
					finishTool();
				}
			}
			else {
				fQueue.internalFinished(fCurrentRunnable, Queue.ERROR);
				handleRunnableError(new Status(
					IStatus.ERROR,
					NicoCore.PLUGIN_ID,
					NicoPlugin.EXTERNAL_ERROR,
					NLS.bind(Messages.ToolRunnable_error_RuntimeError_message,
							new Object[] { fProcess.getToolLabel(true), fCurrentRunnable.getLabel() }),
					e));
			}
		}
		finally {
			fRunnableProgressMonitor.done();
		}
		
		return true;
	}
	
	private void loopWait() {
		try {
			fQueue.wait();
		} catch (InterruptedException e) {
		}
	}
	
	protected void resume() {
		synchronized (fQueue) {
			fQueue.notifyAll();
		}
	}

	protected void markAsTerminated() {
		if (isToolAlive()) {
			NicoPlugin.logError(NicoCore.STATUSCODE_RUNTIME_ERROR, "Illegal state: tool marked as terminated but still alive.", null); //$NON-NLS-1$
		}
		fIsTerminated = true;
	}
	
	protected void handleRunnableError(IStatus status) {
		NicoPlugin.log(status);
	}
	
	/**
	 * Implement here special functionality to start the tool.
	 * 
	 * The method is called automatically in the tool lifecycle thread.
	 * 
	 * @param monitor a progress monitor
	 * 
	 * @throws InterruptedException if start was cancelled.
	 * @throws CoreException with details, if start fails.
	 */
	protected abstract void startTool(IProgressMonitor monitor)
			throws InterruptedException, CoreException;
	
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
	protected void interruptTool(int hardness) throws UnsupportedOperationException {
		getControllerThread().interrupt();
	}

	protected void finishTool() {
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
	}
	
}
