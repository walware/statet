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

package de.walware.statet.nico.ui;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;

import de.walware.ecommons.ts.ITool;

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
		final StringBuilder sb = new StringBuilder(getClass().getName());
		sb.append("\n\t"); //$NON-NLS-1$
		sb.append("process= ").append(fProcess != null ? fProcess.getLabel(ITool.LONG_LABEL) : "<null>"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("\n\t"); //$NON-NLS-1$
		sb.append("source= ").append(fSource != null ? fSource.getTitle() : "<null>"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("\n\t"); //$NON-NLS-1$
		sb.append("console= ").append(fConsole != null ? fConsole.getName() : "<null>"); //$NON-NLS-1$ //$NON-NLS-2$
		return sb.toString();
	}
	
}
