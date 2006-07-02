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


/**
 * Runnables part which can communicate with the software. 
 * The are added to the queue and runned, when it is one's turn.
 *
 * A runnable depends on an adapter type. It is a good idea, if 
 * this is an interface and not an implementation.
 */
public interface IToolRunnable<T extends IToolRunnableControllerAdapter> {

	/**
	 * This method is called by the Tool-thread, when it is one's turn.
	 * <p>
	 * This method is running in the Tool-thread and blocks the thread, 
	 * until <code>run</code> its finished. So you have exlusive access to 
	 * the Tool inside this method.
	 * <p>
	 * Don't call this method on another place.
	 */
	public void run(T tools);
	
	/**
	 * Return a label for this runnable, used by the UI.
	 * @return the label
	 */
	public String getLabel();
	
	/**
	 * Return the submit type of this entry. The same runnable should
	 * always return the same type.
	 * @return
	 */
	public SubmitType getType();
}
