/*******************************************************************************
 * Copyright (c) 2005-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.console.NIConsole;


public class ToolSessionUIData {
	
	
	private final ToolProcess fProcess;
	private final NIConsole fConsole;
	private final IWorkbenchPage fPage;
	private final IViewPart fSource;
	
	
	public ToolSessionUIData(final ToolProcess process, final NIConsole console, 
			final IWorkbenchPage page, final IViewPart source) {
		fProcess = process;
		fConsole = console;
		fPage = page;
		fSource = source;
	}
	
	public NIConsole getConsole() {
		return fConsole;
	}
	
	public ToolProcess getProcess() {
		return fProcess;
	}
	
	public IWorkbenchPage getPage() {
		return fPage;
	}
	
	public IViewPart getSource() {
		return fSource;
	}
	
	
	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder("["); //$NON-NLS-1$
		s.append("Process: ").append(fProcess != null ? fProcess.getToolLabel(true) : "<null>").append(", "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		s.append("Source: ").append(fSource != null ? fSource.getTitle() : "<null>").append(", "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		s.append("Console: ").append(fConsole != null ? fConsole.getName() : "<null>").append("]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return s.toString();
	}
	
}
