/*******************************************************************************
 * Copyright (c) 2005-2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.osgi.util.NLS;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.NicoCoreMessages;
import de.walware.statet.nico.core.internal.Messages;
import de.walware.statet.nico.core.internal.NicoPlugin;


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
	 * Runtime status. Use this only in runtime classes.
	 */
	public static enum ToolStatus {
		STARTING,
		STARTED_IDLING,
		STARTED_PROCESSING,
		STARTED_PAUSED,
		STARTED_SUSPENDED,
//		STARTED_CUSTOM,
		TERMINATED;
	}
	
	/**
	 * Listens for changes of the status of a controller.
	 * 
	 * Use this only if it's really necessary, otherwise listen to 
	 * debug events of the ToolProcess. 
	 */
	public static interface IToolStatusListener {
		
		void controllerStatusRequested(ToolStatus currentStatus, ToolStatus requestedStatus, List<DebugEvent> eventCollection);
		
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
		
		public void submitToConsole(String input, IProgressMonitor monitor) throws InterruptedException {
			
			doBeforeSubmit(input);
			Prompt prompt = doSubmit(input, monitor);
			setPrompt(prompt);
		}
		
		protected void doBeforeSubmit(String input) {
			
			SubmitType type = fCurrentRunnable.getType();
			fInfoStream.append(fPrompt.text, type, fPrompt.meta);
			fInputStream.append(input, type, 
					(fPrompt.meta & IToolRunnableControllerAdapter.META_HISTORY_DONTADD) );
			fInputStream.append(fWorkspaceData.getLineSeparator(), type, 
					IToolRunnableControllerAdapter.META_HISTORY_DONTADD);
		}
		
		protected abstract Prompt doSubmit(String input, IProgressMonitor monitor) throws InterruptedException;
	}
	
	/**
	 * Default implementation of a runnable which can be used for
	 * {@link ToolController#createCommandRunnable(String, SubmitType)}.
	 * 
	 * Usage: This class is intend to be subclassed.
	 */
	public static class ConsoleCommandRunnable extends PlatformObject implements IToolRunnable {
		
		protected final String fText;
		protected final SubmitType fType;
		
		protected ConsoleCommandRunnable(String text, SubmitType type) {
			
			assert (text != null);
			assert (type != null);
			
			fText = text;
			fType = type;
		}

		public boolean needsProgressMonitor() {

			return false;
		}
		
		public void run(IToolRunnableControllerAdapter tools, IProgressMonitor monitor) throws InterruptedException {
			
			tools.submitToConsole(fText, monitor);
		}
		
		public void finish() {
		}
		
		public String getLabel() {
			
			return fText;
		}
		
		public SubmitType getType() {
			
			return fType;
		}
	}
	
	
	private ToolStreamProxy fStreams;
	protected ToolStreamMonitor fInputStream;
	protected ToolStreamMonitor fInfoStream;
	protected ToolStreamMonitor fDefaultOutputStream;
	protected ToolStreamMonitor fErrorOutputStream;
	
	protected final ToolProcess fProcess;
	private Queue fQueue;

	protected IToolRunnable fCurrentRunnable;
	private IProgressMonitor fRunnableProgressMonitor = new NullProgressMonitor();
	 
	private Thread fControllerThread;
	private ToolStatus fStatus = ToolStatus.STARTING;
	private IToolStatusListener[] fToolStatusListeners;
	private List<DebugEvent> fEventCollector = new LinkedList<DebugEvent>();
	private boolean fPauseRequested = false;
	private boolean fTerminateRequested = false;
	private boolean fTerminateForced = false;
	private boolean fIgnoreRequests = false;

	protected WorkspaceType fWorkspaceData;
	protected RunnableAdapterType fRunnableAdapter;
	
	
	
	protected ToolController(ToolProcess process) {
		
		fProcess = process;
		
		fStreams = new ToolStreamProxy();
		fInputStream = fStreams.getInputStreamMonitor();
		fInfoStream = fStreams.getInfoStreamMonitor();
		fDefaultOutputStream = fStreams.getOutputStreamMonitor();
		fErrorOutputStream = fStreams.getErrorStreamMonitor();
		
		fQueue = new Queue();
		fToolStatusListeners = new IToolStatusListener[] { fProcess };
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
	
	protected ToolStatus getStatus() {
		
		return fStatus;
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
		
		try {
			fControllerThread = Thread.currentThread();
			startTool();
			synchronized (fQueue) {
				loopChangeStatus(ToolStatus.STARTED_IDLING);
			}
			loop();
		}
		finally {
			synchronized (fQueue) {
				loopChangeStatus(ToolStatus.TERMINATED);
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
	 */
	public void pause(boolean doPause) {
		
		synchronized (fQueue) {
			if (doPause) {
				if (fStatus == ToolStatus.STARTED_PROCESSING || fStatus == ToolStatus.STARTED_IDLING) {
					if (!fPauseRequested) {
						fPauseRequested = true;
						statusRequested(ToolStatus.STARTED_PAUSED);
					}
					resume(); // so we can switch to pause status
				}
				return;
			}
			else { // !doPause
				if (fPauseRequested || fStatus == ToolStatus.STARTED_PAUSED) {
					fPauseRequested = false;
					if (fStatus == ToolStatus.STARTED_PAUSED) {
						resume();
					}
				}
				return;
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
	public boolean cancel() {
		
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
	public void terminate() {
		
		synchronized (fQueue) {
			if (fStatus != ToolStatus.TERMINATED) {
				if (!fTerminateRequested) {
					fTerminateRequested = true;
				}
				statusRequested(ToolStatus.TERMINATED);
				resume();
			}
		}
	}
	
	/**
	 * Cancels requests to termate the controller.
	 */
	public void cancelTermination() {
		
		synchronized(fQueue) {
			if (fStatus != ToolStatus.TERMINATED) {
				if (fTerminateRequested) {
					fTerminateRequested = false;
					fTerminateForced = false;
//					statusRequested(ToolStatus.);
				}
			}
		}
	}
	
	public void terminateForced(IProgressMonitor monitor) throws DebugException, InterruptedException {
		
		if (monitor.isCanceled()) {
			throw new InterruptedException();
		}
		fTerminateForced = true;
		terminate();
		cancel();

		Exception exception = null;
		try {
			for (int i = 5; i < 10; i++) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
				if (!fControllerThread.isAlive()) {
					synchronized (fQueue) {
						terminateTool(true);
						if (fStatus != ToolStatus.TERMINATED) {
							loopChangeStatus(ToolStatus.TERMINATED);
						}
					}
					return;
				}
				
				interruptTool(i);
				
				if (!fTerminateForced || monitor.isCanceled()) {
					throw new InterruptedException();
				}
			}
		}
		catch (UnsupportedOperationException e) {
			exception = e;
		}
		throw new DebugException(new Status(
				IStatus.ERROR, NicoCore.PLUGIN_ID, NicoCore.STATUSCODE_RUNTIME_ERROR, 
				NLS.bind(Messages.Runtime_error_TerminationFailed_message, fProcess.getToolLabel(true)), exception));
	}
		
	/**
	 * Should be only called inside synchronized(fQueue) blocks.
	 * 
	 * @param newStatus
	 */
	private void statusRequested(ToolStatus requestedStatus) {
		
		for (IToolStatusListener listener : fToolStatusListeners) {
			listener.controllerStatusRequested(fStatus, requestedStatus, fEventCollector);
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
	private void loopChangeStatus(ToolStatus newStatus) {
		
		ToolStatus oldStatus = fStatus;
		if (oldStatus == newStatus)
			return;
		fStatus = newStatus;
	
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
			monitor.beginTask(NicoCoreMessages.SubmitTask_label, 3); //$NON-NLS-1$
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
	public boolean submit(IToolRunnable<IToolRunnableControllerAdapter>[] tasks) {
		
		assert (tasks != null);
		
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
	public boolean runOnIdle(IToolRunnable<IToolRunnableControllerAdapter> task) {
		
		assert (task != null);
		
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
			try {
				 while (loopRunTask()) {}
			}
			catch (Exception e) {
				if (e instanceof InterruptedException) {
					fQueue.internalFinished(fCurrentRunnable, Queue.CANCEL);
					if (!isToolAlive()) {
						terminate();
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
			
			synchronized (fQueue) {
				fQueue.internalCheck();
				
				if (fTerminateRequested) {
					fTerminateRequested = false;
					if (terminateTool(fTerminateForced) || fTerminateForced) { // termination can be canceled
						loopChangeStatus(ToolStatus.TERMINATED);
						return;
					}
					continue;
				}
				if (fPauseRequested) {
					loopChangeStatus(ToolStatus.STARTED_PAUSED);
					loopWait();
					continue;
				}
				if (fQueue.internalIsEmpty()) {
					loopChangeStatus(ToolStatus.STARTED_IDLING);
					loopWait();
					continue;
				}
			}
		}
	}
	
	private boolean loopRunTask() throws InterruptedException {
		
		synchronized (fQueue) {
			if (fQueue.internalIsEmpty() 
					|| (!fIgnoreRequests && (fPauseRequested || fTerminateRequested))) {
				return false;
			}
			fIgnoreRequests = false;
			fCurrentRunnable = fQueue.internalPoll();
			fRunnableProgressMonitor.setCanceled(false);
			loopChangeStatus(ToolStatus.STARTED_PROCESSING);
		}

		// muss nicht synchronisiert werden, da Zugriff nur durch einen Thread
		fCurrentRunnable.run(fRunnableAdapter, fRunnableProgressMonitor);
		fQueue.internalFinished(fCurrentRunnable, Queue.OK);
		fCurrentRunnable = null;
		return true;
	} 

	private void loopWait() {
		
		try {
			fQueue.wait();
		} catch (InterruptedException e) { 
		}
	}
	
	private void resume() {
		
		synchronized (fQueue) {
			fQueue.notifyAll();
		}
	}
	
	
	protected void handleRunnableError(IStatus status) {
		
		NicoPlugin.log(status);
	}
	
	/**
	 * Implement here special functionality to start the tool.
	 * 
	 * The method is called automatically in the tool lifecycle thread.
	 * 
	 * @throws CoreException with details, if start fails.
	 */
	protected abstract void startTool() throws CoreException;
	
	/**
	 * Implement here special commands to terminate the tool.
	 * 
	 * If force is <code>true</code>, the method is can be called async.
	 * Otherwise it is called automatically in the tool lifecycle thread.
	 * 
	 * @param force if <code>true</code>, try to terminate in all cases. The answer is ignored.
	 * 
	 * @return <code>true</code> if successfully terminated, otherwise <code>false</code>.
	 */
	protected abstract boolean terminateTool(boolean forced);
	
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
