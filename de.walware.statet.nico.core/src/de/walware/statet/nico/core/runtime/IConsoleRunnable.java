/*******************************************************************************
 * Copyright (c) 2005-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import de.walware.ecommons.ts.IToolRunnable;


/**
 * Runnables for classic console input.
 */
public interface IConsoleRunnable extends IToolRunnable {
	
	
	/**
	 * Return the submit type of this entry. The same runnable should
	 * always return the same type.
	 * 
	 * @return the type
	 */
	SubmitType getSubmitType();
	
}
