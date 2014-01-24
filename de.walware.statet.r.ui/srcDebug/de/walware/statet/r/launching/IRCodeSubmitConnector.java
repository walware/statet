/*=============================================================================#
 # Copyright (c) 2005-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.launching;

import java.util.List;

import org.eclipse.core.runtime.CoreException;


public interface IRCodeSubmitConnector {
	
	
	/**
	 * Submit commands to R.
	 * 
	 * @param lines array with commands
	 * @param gotoConsole if <code>true</code>, switch focus console, else does not change the focus.
	 * @return <code>false</code>, if not successful, otherwise <code>true</code> (hint)
	 * 
	 * @throws CoreException if a technical error occured
	 */
	boolean submit(List<String> lines, boolean gotoConsole) throws CoreException;
	
	void gotoConsole() throws CoreException;
	
}
