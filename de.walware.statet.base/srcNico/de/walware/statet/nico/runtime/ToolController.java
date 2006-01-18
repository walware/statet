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

package de.walware.statet.nico.runtime;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import de.walware.statet.nico.Messages;
import de.walware.statet.ui.util.ExceptionHandler;


/**
 * Controller for a long running tight integrated tool.
 */
public class ToolController {

	
	public static enum ToolStatus {
		STARTING,
		STARTED_IDLE,
		STARTED_CALCULATING,
		STARTED_PAUSED,
		TERMINATED;
	}
	
	protected class SimpleRunnable implements IToolRunnable {
		
		protected final String fText;
		protected final SubmitType fType;
		
		protected SimpleRunnable(String text, SubmitType type) {
			
			assert (text != null);
			assert (type != null);
			
			this.fText = text;
			this.fType = type;;
		}

		public void run() {
			
			doOnCommandRun(fText, fType);
		}

		public String getLabel() {
			
			return fText;
		}
		
//		public SubmitType getType() {
//			
//			return fType;
//		}
	}
	
	private static NullProgressMonitor fgProgressMonitorDummy = new NullProgressMonitor(); 
	
	private LinkedList<IToolRunnable> fQueue;
	
	private ToolStreamProxy fStreams;
	protected ToolStreamMonitor fInputStream;
	protected ToolStreamMonitor fDefaultOutputStream;
	protected ToolStreamMonitor fErrorOutputStream;
	
	protected final ToolProcess fProcess;
	private History fHistory;
	 
	private ToolStatus fStatus = ToolStatus.STARTING;
	private boolean fPauseRequested = false;
	private boolean fTerminateRequested = false;
	
	
	public ToolController(ToolProcess process) {
		
		fProcess = process;
		fQueue = new LinkedList<IToolRunnable>();
		
		fStreams = new ToolStreamProxy();
		fInputStream = fStreams.getInputStreamMonitor();
		fDefaultOutputStream = fStreams.getOutputStreamMonitor();
		fErrorOutputStream = fStreams.getErrorStreamMonitor();
		
		fHistory = new History(fInputStream);
	}
	
	/**
	 * Returns the history of this tool instance. There is one history per controller.
	 * @return the history
	 */
	public History getHistory() {
		
		return fHistory;
	}
	
	public ToolStreamProxy getStreams() {
		
		return fStreams;
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
					doResume(); // so we can switch to pause status
				}
				return;
			}
			else { // !doPause
				if (fPauseRequested) {
					fPauseRequested = false;
				}
				if (fStatus == ToolStatus.STARTED_PAUSED) {
					doResume();
				}
				return;
			}
		}
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
		
		assert (text != null);
		monitor.beginTask("Submitting to queue.", 3);

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
				
				monitor.done();
				return true;
			}
			else {
				return false;
			}
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
	 * Overwrite this method to create a runnable for text commands 
	 * (e.g. from console or editor).
	 * 
	 * The runnable should commit this commands to the tool 
	 * and print command and results to the console.
	 * You can extends <code>SimpleRunnable</code> or create a 
	 * completely new implementation.
	 *
	 * @param command text command
	 * @param type type of this submission
	 * @return runnable for this command
	 */
	protected IToolRunnable createCommandRunnable(String command, SubmitType type) {
		
		return new SimpleRunnable(command, type);
	}
	
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
		
		fQueue.addAll(Arrays.asList(tasks));
		if (fStatus == ToolStatus.STARTED_IDLE) {
			doResume();
		}
	}
	
	
	private void loop() {
		
		 while (true) {
			while (doRunTask()) {}
			
			synchronized (fQueue) {
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
				if (fQueue.isEmpty()) {
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
			if (fQueue.isEmpty() || fPauseRequested || fTerminateRequested) {
				return false;
			}
			e = fQueue.poll();
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
	
	
	public String createSubmitMessage() {
		
		return NLS.bind(Messages.SubmitTask_name, fProcess.getLabel());
	}
	
	public void runSubmitInBackground(IRunnableWithProgress runnable, Shell shell) {
		
		try {
			// would busycursor or job be better?
			PlatformUI.getWorkbench().getProgressService().run(true, true, runnable);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, shell, 
					NLS.bind(Messages.Submit_error_message, fProcess.getLabel()) 
			);
		} catch (InterruptedException e) {
			// something to do?
		}
	}
}
