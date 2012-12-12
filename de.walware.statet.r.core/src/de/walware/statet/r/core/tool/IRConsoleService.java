/*******************************************************************************
 * Copyright (c) 2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.tool;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.rj.services.RService;


public interface IRConsoleService extends RService {
	
	
	boolean acceptNewConsoleCommand();
	
	/**
	 * Submits the text to the tool console.
	 * 
	 * @param input the text to submit
	 * @param monitor the progress monitor of the current run (or a child)
	 * @throws CoreException if an error occurred or the operation was canceled
	 */
	void submitToConsole(String input, IProgressMonitor monitor)
			throws CoreException;
	
	void briefAboutChange(int o);
	
}
