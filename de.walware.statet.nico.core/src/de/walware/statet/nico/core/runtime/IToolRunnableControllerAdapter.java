/*******************************************************************************
 * Copyright (c) 2006-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
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
 * This is the interface of the controller to the {@link IToolRunnable runnables}.
 * 
 * <ul>
 *     <li>The methods may only be used in the tool thread, usually inside the <code>run</code>
 *         method of the runnables.</li>
 * </ul>
 */
public interface IToolRunnableControllerAdapter {
	
	
	public static final int META_NONE = 0;
	public static final int META_HISTORY_DONTADD = 1 << 0;
	public static final int META_PROMPT_DEFAULT = 1 << 1;
	
	
	/**
	 * Returns the controller of this tool.
	 * 
	 * @return the tool controller
	 */
	ToolController getController();
	
	/**
	 * Returns the process handler of this tool.
	 * 
	 * A tool returns always the same process instance.
	 * 
	 * @return the tool process
	 */
	ToolProcess getProcess();
	
	/**
	 * Returns the currently running tool runnable.
	 * 
	 * @return the current tool runnable
	 */
	IToolRunnable getCurrentRunnable();
	
	/**
	 * Returns the workspace data of this tool.
	 * 
	 * A tool returns always the same workspace data instance.
	 * 
	 * @return the tool workspace
	 */
	ToolWorkspace getWorkspaceData();
	
	/**
	 * Refreshes the workspace data of this tool.
	 * 
	 * This method can be used to ensure that the workspace data is up-to-date.
	 * 
	 * @param options
	 * @param monitor the progress monitor of the current run (or a child)
	 * @throws CoreException if an error occurred or the operation was canceled
	 */
	void refreshWorkspaceData(int options, IProgressMonitor monitor)
			throws CoreException;
	
	
	/*---- Console ----*/
	
	/**
	 * Returns if the current prompt is the default prompt.
	 * 
	 * @return <code>true</code> if it is the default prompt, otherwise <code>false</code>
	 */
	boolean isDefaultPrompt();
	
	/**
	 * Returns the current prompt.
	 * 
	 * @return the current prompt
	 */
	Prompt getPrompt();
	
	/**
	 * Submits the text to the tool console.
	 * 
	 * @param input the text to submit
	 * @param monitor the progress monitor of the current run (or a child)
	 * @throws CoreException if an error occurred or the operation was canceled
	 * @throws InterruptedException
	 */
	void submitToConsole(String input, IProgressMonitor monitor)
			throws CoreException;
	
	
	/*---- Status ----*/
	
	/**
	 * Reports a status to the user
	 * 
	 * @param status the status to handle
	 * @param monitor the progress monitor of the current run (or a child)
	 */
	void handleStatus(final IStatus status, IProgressMonitor monitor);
	
}
