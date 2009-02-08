/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.launching;

import org.eclipse.core.runtime.CoreException;


public interface IRCodeLaunchConnector {
	
	
	/**
	 * Submit commands to R.
	 * 
	 * @param rCommands array with commands
	 * @param gotoConsole if <code>true</code>, switch focus console, else does not change the focus.
	 * @return <code>false</code>, if not successful, otherwise <code>true</code> (hint)
	 * 
	 * @throws CoreException if a technical error occured
	 */
	boolean submit(String[] rCommands, boolean gotoConsole) throws CoreException;
	
	void gotoConsole() throws CoreException;
	
}
