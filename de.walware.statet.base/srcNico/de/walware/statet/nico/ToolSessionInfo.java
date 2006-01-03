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

package de.walware.statet.nico;

import org.eclipse.ui.IViewPart;

import de.walware.statet.nico.console.NIConsole;
import de.walware.statet.nico.runtime.ToolController;


public class ToolSessionInfo {

	
	ToolController fController;
	NIConsole fConsole;
	IViewPart fSource;
	
	
	public void setConsole(NIConsole console) {
		
		fConsole = console;
	}

	public NIConsole getConsole() {
		
		return fConsole;
	}
	
	public void setController(ToolController controller) {
		
		fController = controller;
	}
	
	public ToolController getController() {
		
		return fController;
	}
	
	public void setSource(IViewPart source) {
		
		fSource = source;
	}
	
	public IViewPart getSource() {
		
		return fSource;
	}
	
	
	@Override
	public String toString() {
		
		StringBuilder s = new StringBuilder();
		s.append("Controller: ").append(fController != null ? fController.getName() : "<null>").append('\n');
		s.append("Source: ").append(fSource != null ? fSource.getTitle() : "<null>").append('\n');
		s.append("Console: ").append(fConsole != null ? fConsole.getName() : "<null>").append('\n');
		return s.toString();
	}
}
