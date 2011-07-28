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

package de.walware.statet.r.debug.core.breakpoints;

import org.eclipse.core.runtime.CoreException;


public interface IRMethodBreakpoint extends IRLineBreakpoint {
	
	
	/**
	 * Returns whether this breakpoint causes execution to suspend on entry to methods.
	 * 
	 * @return whether suspend on entry is enabled
	 * @exception CoreException if unable to access the property from this breakpoint's underlying marker
	 */
	boolean isEntry() throws CoreException;
	
	/**
	 * Returns whether this breakpoint causes execution to suspend on exit of methods.
	 * 
	 * @return whether suspend on exit is enabled
	 * @exception CoreException if unable to access the property from this breakpoint's underlying marker
	 */
	boolean isExit() throws CoreException;	
	
	/**
	 * Sets whether this breakpoint causes execution to suspend on entry to methods.
	 * 
	 * @param enabled whether this breakpoint causes execution to suspend on entry to methods
	 * @exception CoreException if unable to set the property on this breakpoint's underlying marker
	 */
	void setEntry(boolean enabled) throws CoreException;	
	
	/**
	 * Sets whether this breakpoint causes execution to suspend on exit of methods.
	 * 
	 * @param enabled whether this breakpoint causes execution to suspend on exit of methods
	 * @exception CoreException if unable to set the property on this breakpoint's underlying marker
	 */
	void setExit(boolean enabled) throws CoreException;	
	
}
