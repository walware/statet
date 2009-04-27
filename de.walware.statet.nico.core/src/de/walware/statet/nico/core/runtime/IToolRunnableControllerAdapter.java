/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;


/**
 * This is the interface of the controller to the runnables.
 * The runnable gets the adapter with the call of its run method.
 * 
 * It is intended to extends this interface for more particular
 * needs. The concrete interface can be specified in implementations
 * of ToolController and IToolRunnable.
 * 
 * For implementations:
 * The lifecycle is the same as the ToolController (not ToolProcess!).
 * The methods must only be used in the tool lifecycle thread.
 */
public interface IToolRunnableControllerAdapter {
	
	
	public static final int META_NONE = 0;
	public static final int META_HISTORY_DONTADD = 1 << 0;
	public static final int META_PROMPT_DEFAULT = 1 << 1;
	
	
	/**
	 * Returns the controller the runnable is currently running in
	 * @return the tool controller
	 */
	public ToolController getController();
	
	/**
	 * Returns the process handler of controller the runnable is currently running in
	 * @return the tool process
	 */
	public ToolProcess getProcess();
	
	/**
	 * Returns the workspace data of controller the runnable is currently running in
	 * @return the tool workspace
	 */
	public ToolWorkspace getWorkspaceData();
	
	
	public void refreshWorkspaceData(IProgressMonitor monitor) 
			throws CoreException;
	
	/**
	 * Submits the text to the tool console
	 * 
	 * @param input the text to submit
	 * @param monitor the progress monitor of the current run (or a child)
	 * @throws CoreException
	 * @throws InterruptedException
	 */
	public void submitToConsole(String input, IProgressMonitor monitor)
			throws CoreException, InterruptedException;
	
	/**
	 * Reports a status to the user
	 * 
	 * @param status the status to handle
	 * @param monitor the progress monitor of the current run (or a child)
	 */
	public void handleStatus(final IStatus status, IProgressMonitor monitor);
	
}
