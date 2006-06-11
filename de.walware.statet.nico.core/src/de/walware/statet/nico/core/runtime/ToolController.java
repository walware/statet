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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;


/**
 * Controller for a long running tight integrated tool.
 * <p>
 * Usage: This class is intend to be subclass. Subclasses are responsible for the
 * lifecicle of the tool (<code>startTool()</code>, <code>terminateTool()</code>.
 * Subclasses should provide an interface which can be used by IToolRunnables
 * to access the features of the tool. E.g. provide an abstract implementation of 
 * IToolRunnable with the necessary methods (in protected scope).</p>
 */
public abstract class ToolController {

	
	static enum ToolStatus {
		STARTING,
		STARTED_IDLE,
		STARTED_CALCULATING,
		STARTED_PAUSED,
		TERMINATED;
	}
	
	
	private static NullProgressMonitor fgProgressMonitorDummy = new NullProgressMonitor(); 
	
	
	/**
	 * Note: if you want to be notified, if the controller really is in pause status,
	 * listen to debug events {@link ToolProcess#STATUS_QUEUE_PAUSE}.
	 */
	public interface IPauseRequestListener {
		
		void pauseRequested();
		void unpauseRequested();
	}
	
	/**
	 * Default implementation of a runnable which can be used for
	 * {@link ToolController#createCommandRunnable(String, SubmitType)}.
	 * 
	 * Usage: This class is intend to be subclassed.
	 */
	public abstract class SimpleRunnable extends PlatformObject implements IToolRunnable {
		
		protected final String fText;
		protected final SubmitType fType;
		
		protected SimpleRunnable(String text, SubmitType type) {
			
			assert (text != null);
			assert (type != null);
			
			fText = text;
			fType = type;;
		}

		public void run() {
			
			doOnCommandRun(fText, fType);
		}
		
		public String getLabel() {
			
			return fText;
		}
	}
	
	
	private ToolStreamProxy fStreams;
	protected ToolStreamMonitor fInputStream;
	protected ToolStreamMonitor fDefaultOutputStream;
	protected ToolStreamMonitor fErrorOutputStream;
	
	protected final ToolProcess fProcess;
	private Queue fQueue;
	 
	private ToolStatus fStatus = ToolStatus.STARTING;
	private boolean fPauseRequested = false;
	private ListenerList fPauseRequestListeners = new ListenerList(ListenerList.IDENTITY);
	private boolean fTerminateRequested = false;
	
	
	protected ToolController(ToolProcess process) {
		
		fProcess = process;
		
		fStreams = new ToolStreamProxy();
		fInputStream = fStreams.getInputStreamMonitor();
		fDefaultOutputStream = fStreams.getOutputStreamMonitor();
		fErrorOutputStream = fStreams.getErrorStreamMonitor();
		
		fQueue = new Queue(process);
	}
	
	
	Queue getQueue() {

		return fQueue;
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
			startTool();
			synchronized (fQueue) {
				setStatus(ToolStatus.STARTED_IDLE);
			}
			loop();
		}
		catch (CoreException e) {
			synchronized (fQueue) {
				setStatus(ToolStatus.TERMINATED);
			}
			throw e;
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
				if (fStatus == ToolStatus.STARTED_CALCULATING || fStatus == ToolStatus.STARTED_IDLE) {
					fPauseRequested = true;
					for (Object obj : fPauseRequestListeners.getListeners()) {
						((IPauseRequestListener) obj).pauseRequested();
					}
					doResume(); // so we can switch to pause status
				}
				return;
			}
			else { // !doPause
				if (fPauseRequested || fStatus == ToolStatus.STARTED_PAUSED) {
					for (Object obj : fPauseRequestListeners.getListeners()) {
						((IPauseRequestListener) obj).unpauseRequested();
					}
					if (fPauseRequested) {
						fPauseRequested = false;
					}
					if (fStatus == ToolStatus.STARTED_PAUSED) {
						doResume();
					}
				}
				return;
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
	public boolean isPaused() {
		
		synchronized (fQueue) {
			return (fPauseRequested || fStatus == ToolStatus.STARTED_PAUSED);
		}
	}
	
	public void addPauseRequestListener(IPauseRequestListener listener) {
		
		fPauseRequestListeners.add(listener);
	}
	
	public void removePauseRequestListener(IPauseRequestListener listener) {
		
		fPauseRequestListeners.remove(listener);
	}
	
	void terminate() {
		
		synchronized (fQueue) {
			if (fStatus != ToolStatus.TERMINATED) {
				fTerminateRequested = true;
				doResume();
			}
		}
	}
	
	/**
	 * Should be only called inside synchronized(fQueue) blocks.
	 * 
	 * @param newStatus
	 */
	protected void setStatus(ToolStatus newStatus) {
		
		ToolStatus oldStatus = fStatus;
		if (oldStatus == newStatus)
			return;
		fStatus = newStatus;
	
		fProcess.controllerStatusChanged(oldStatus, newStatus);
	}
	
//	public boolean isStarted() {
//		
//		switch (fStatus) {
//		case STARTED_CALCULATING:
//		case STARTED_IDLE:
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
	 * Submit one or multiple text lines to the tool.
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
			monitor.beginTask("Submitting to queue.", 3);
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
	 * Submit one or multiple text lines to the tool.
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
	 * You can extends <code>SimpleRunnable</code> or create a 
	 * completely new implementation.
	 * @see SimpleRunnable
	 *
	 * @param command text command
	 * @param type type of this submission
	 * @return runnable for this command
	 * 
	 */
	protected abstract IToolRunnable createCommandRunnable(String command, SubmitType type);
	
	/**
	 * Submit the runnable ("task") for the tool.
	 * <p>
	 * The runnable will be added to the queue and will be runned, if it's its turn.
	 * 
	 * @param task the runnable.
	 * @return <code>true</code>, if adding task to queue was successful, 
	 * 		otherwise <code>false</code>. 
	 */
	public boolean submit(IToolRunnable[] task) {
		
		assert (task != null);
		
		synchronized (fQueue) {
			if (acceptSubmit()) {
				doSubmit(task);
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
		
		if (fStatus == ToolStatus.STARTED_IDLE) {
			fQueue.internalAdd(tasks, true);
			doResume();
		}
		else {
			fQueue.internalAdd(tasks, false);
		}
	}
	
	
	private void loop() {
		
		 while (true) {
			while (doRunTask()) {}
			
			synchronized (fQueue) {
				fQueue.internalCheckCache();
				
				if (fTerminateRequested) {
					fTerminateRequested = false;
					if (terminateTool()) { // termination can be canceled
						setStatus(ToolStatus.TERMINATED);
						return;
					}
					continue;
				}
				if (fPauseRequested) {
					setStatus(ToolStatus.STARTED_PAUSED);
					doWait();
					continue;
				}
				if (fQueue.internalIsEmpty()) {
					setStatus(ToolStatus.STARTED_IDLE);
					doWait();
					continue;
				}
			}
		}
	}
	
	private boolean doRunTask() {
		
		IToolRunnable e = null;
		synchronized (fQueue) {
			if (fQueue.internalIsEmpty() || fPauseRequested || fTerminateRequested) {
				return false;
			}
			e = fQueue.internalPoll();
			setStatus(ToolStatus.STARTED_CALCULATING);
		}

		// muss nicht synchronisiert werden, da Zugriff nur durch einen Thread
		e.run();
		return true;
	} 

	private void doWait() {
		
		try {
			fQueue.wait();
		} catch (InterruptedException e) { 
		}
	}
	
	private void doResume() {
		
		synchronized (fQueue) {
			fQueue.notifyAll();
		}
	}
	
	/**
	 * Implement here special functionality to start the tool.
	 * 
	 * @throws CoreException with details, if start fails.
	 */
	protected void startTool() throws CoreException {
		
	}
	
	/**
	 * Implement here special commands to terminate the tool.
	 * 
	 * @return <code>true</code> if successfully terminated, otherwise <code>false</code>.
	 */
	protected boolean terminateTool() {
		
		return true;
	}
	
	protected void doOnCommandRun(String command, SubmitType type) {

		fInputStream.append(command, type);
	}
	
}
