/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.tool;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.rj.eclient.IRToolService;
import de.walware.rj.services.RService;


public interface IRConsoleService extends IRToolService, RService {
	
	
	int AUTO_CHANGE=                    0b0_0000_0000_0000_0001;
	
//	int DATA_CHANGE=                    0b0_0000_0000_0000_1000;
	
	int PACKAGE_CHANGE=                 0b0_0000_0000_0001_0000;
	
	
	boolean acceptNewConsoleCommand();
	
	/**
	 * Submits the text to the tool console.
	 * 
	 * @param input the text to submit
	 * @param monitor the progress monitor of the current run (or a child)
	 * @throws CoreException if an error occurred or the operation was canceled
	 */
	void submitToConsole(String input,
			IProgressMonitor monitor) throws CoreException;
	
	void briefAboutToChange();
	void briefChanged(int flags);
	
}
