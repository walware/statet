/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.debug.core;

import org.eclipse.debug.core.model.IStackFrame;


/**
 * Represents an R stack frame in the Eclipse debug model for R.
 */
public interface IRStackFrame extends IStackFrame {
	
	
	/**
	 * Informal filename
	 * @return filename or <code>null</code>, if not available
	 */
	String getInfoFileName();
	
	/**
	 * Informal line number
	 * @return linenumber or <code>-1</code>, if not available
	 */
	int getInfoLineNumber();
	
	/**
	 * Position index of the frame in R (one-based like in R)
	 */
	int getPosition();
	
}
