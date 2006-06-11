/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui;

import org.eclipse.ui.IViewPart;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.console.NIConsole;


public class ToolSessionInfo {

	
	ToolProcess fProcess;
	NIConsole fConsole;
	IViewPart fSource;
	
	
	public NIConsole getConsole() {
		
		return fConsole;
	}
	
	public ToolProcess getProcess() {
		
		return fProcess;
	}
	
	public IViewPart getSource() {
		
		return fSource;
	}
	
	
	@Override
	public String toString() {
		
		StringBuilder s = new StringBuilder();
		s.append("Process: ").append(fProcess != null ? fProcess.getLabel() : "<null>").append('\n');
		s.append("Source: ").append(fSource != null ? fSource.getTitle() : "<null>").append('\n');
		s.append("Console: ").append(fConsole != null ? fConsole.getName() : "<null>").append('\n');
		return s.toString();
	}
}
