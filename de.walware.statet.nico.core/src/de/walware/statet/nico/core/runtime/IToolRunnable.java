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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * Runnables part which can communicate with the tool software.
 * The are added to the queue and runned, when it is one's turn.
 */
public interface IToolRunnable {
	
	
	/**
	 * Total work of progress monitors.
	 * Value = {@value}
	 */
	public static final int TOTAL_WORK = 10000;
	
	
	/**
	 * Unique id of the runnable type (not instance).
	 * 
	 * @return the id
	 */
	public String getTypeId();
	
	/**
	 * Return the submit type of this entry. The same runnable should
	 * always return the same type.
	 * 
	 * @return the type
	 */
	public SubmitType getSubmitType();
	
	/**
	 * Return a label for this runnable, used by the UI.
	 * 
	 * @return the label
	 */
	public String getLabel();
	
	/**
	 * Is called when the state of the runnable has changed
	 */
	public void changed(int event, ToolProcess process);
	
	/**
	 * This method is called by the tool controller, when it is one's turn.
	 * <p>
	 * This method is running in the tool-thread and blocks the thread,
	 * until <code>run</code> is finished. So you have exclusive access to
	 * the tool inside this method.
	 * <p>
	 * Don't call this method on another place.
	 * <p>
	 * The monitor is already setup with main label of getLabel() and total
	 * work of {@link #TOTAL_WORK}.
	 * 
	 * @param adapter your interface to the tool
	 * @param monitor a progress monitor (you can check for cancel)
	 * @throws InterruptedException if action was cancelled
	 * @throws CoreException if error occured
	 */
	public void run(IToolRunnableControllerAdapter adapter, IProgressMonitor monitor)
			throws InterruptedException, CoreException;
	
}
