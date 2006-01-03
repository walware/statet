/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
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
import java.util.LinkedList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
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
	
	private String fName;
	
	private LinkedList<IToolRunnable> fQueue;
	
	private ToolStreamProxy fStreams;
	protected ToolStreamMonitor fInputStream;
	protected ToolStreamMonitor fDefaultOutputStream;
	protected ToolStreamMonitor fErrorOutputStream;
	
	private History fHistory;
	 
	private volatile ToolStatus fStatus = ToolStatus.STARTING; // volatile, because the access in is..() are not synchronized
	private ListenerList fStatusListeners = new ListenerList();
	private boolean fPauseRequested = false;
	private boolean fTerminateRequested = false;
	
	
	public ToolController(String name) {
		
		fName = name;
		
		fQueue = new LinkedList<IToolRunnable>();
		
		fStreams = new ToolStreamProxy();
		fInputStream = fStreams.getInputStreamMonitor();
		fDefaultOutputStream = fStreams.getOutputStreamMonitor();
		fErrorOutputStream = fStreams.getErrorStreamMonitor();
		
		fHistory = new History(fInputStream);
	}
	
	
	/**
	 * Returns the (unique) label of this tool instance.
	 * @return label for usage in the GUI
	 */
	public String getName() {
		
		return fName;
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
	 * Adds a listener, which is notified, if the status of this controller is changed.
	 * 
	 * @param listener listener to add.
	 */
	public void addStatusListener(IStatusListener listener) {
		
		fStatusListeners.add(listener);
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
		}
		catch (CoreException e) {
			synchronized (fQueue) {
				setStatus(ToolStatus.TERMINATED);
			}
			throw e;
		}
		
		loop();
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
					setStatus(ToolStatus.STARTED_IDLE);
					doResume();
				}
				return;
			}
		}
	}
	
	public void terminate() {
		
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
	
		Object[] listeners = fStatusListeners.getListeners();
		for (Object obj : listeners) {
			((IStatusListener) obj).statusChanged(oldStatus, newStatus);
		}
	}
	
	public boolean isStarted() {
		
		switch (fStatus) {
		case STARTED_CALCULATING:
		case STARTED_IDLE:
		case STARTED_PAUSED:
			return true;

		default:
			return false;
		}
	}
	
	public boolean isTerminated() {
		
		return (fStatus == ToolStatus.TERMINATED);
	}
	
	/**
	 * Version for one single text line.
	 * @see #submit(String[], SubmitType)
	 * 
	 * @param text a single text line.
	 * @param type type of this submittal.
	 */
	public void submit (String text, SubmitType type) {
		
		submit(new String[] { text }, type);
	}
	
	/**
	 * Submit one or multiple text lines to the tool.
	 * The texts will be treated as usual commands with console output.
	 * 
	 * @param text array with text lines.
	 * @param type type of this submittal.
	 * @param monitor a monitor for cancel, will not be changed.
	 * 
	 * @throws InterruptedException if action is interupted via monitor.
	 */
	public void submit(String[] text, SubmitType type, IProgressMonitor monitor) throws InterruptedException {
		
		synchronized (fQueue) {
			if (monitor.isCanceled())
				throw new InterruptedException();
			
			submit(text, type);
		}
	}
	
	/**
	 * Submit one or multiple text lines to the tool.
	 * The texts will be treated as usual commands with console output.
	 * 
	 * @param text array with text lines.
	 * @param type type of this submittal.
	 */
	public void submit(String[] text, SubmitType type) {
		
		assert (text != null);
		// texts are checked in runnable constructor.
		
		synchronized (fQueue) {
			for (String s : text) {
				doSubmit(createCommandRunnable(s, type));
			}
		}
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
	 * @param task the runnable
	 */
	public void submit(IToolRunnable task) {
		
		assert (task != null);
		
		synchronized (fQueue) {
			doSubmit(task);
		}
	}
	
	/**
	 * Note: call only inside synchronized(fQueue) block
	 * @param task
	 */
	private void doSubmit(IToolRunnable task) {
		
		fQueue.add(task);
		if (fStatus == ToolStatus.STARTED_IDLE) {
			doResume();
		}
	}
	
	
	private void loop() {
		
		 while (true) {
			while (doRunTask()) {}
			
			synchronized (fQueue) {
				switch (fStatus) {

				case TERMINATED:
					return;
					
				case STARTED_CALCULATING:
				case STARTED_IDLE:
					if (fTerminateRequested) {
						fTerminateRequested = false;
						if (terminateTool()) { // termination can be canceled
							setStatus(ToolStatus.TERMINATED);
						}
						break;
					}
					if (fPauseRequested) {
						fPauseRequested = false;
						setStatus(ToolStatus.STARTED_PAUSED);
						doWait();
						break;
					}
					if (fQueue.isEmpty()) {
						setStatus(ToolStatus.STARTED_IDLE);
						doWait();
						break;
					}
					break;

				case STARTED_PAUSED:
					doWait();
					break;
					
				default:
					// Not expected
					break;
				}
			}
		 }
	}
	
	private boolean doRunTask() {
		
		IToolRunnable e = null;
		synchronized (fQueue) {
			if (fQueue.isEmpty() || fPauseRequested || fTerminateRequested
					|| (fStatus != ToolStatus.STARTED_IDLE && fStatus != ToolStatus.STARTED_CALCULATING) ) {
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
	
	
	public void runSubmitInBackground(IRunnableWithProgress runnable, Shell shell) {
		
		try {
			// would busycursor or job be better?
			PlatformUI.getWorkbench().getProgressService().run(true, true, runnable);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, shell, 
					NLS.bind(Messages.Submit_error_message, getName()) 
			);
		} catch (InterruptedException e) {
			// something to do?
		}
	}
}
