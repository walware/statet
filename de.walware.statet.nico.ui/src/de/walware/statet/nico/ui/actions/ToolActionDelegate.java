/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.actions;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchPage;

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.nico.ui.IToolRegistryListener;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.ToolSessionUIData;


/**
 * TODO: Implementation not finished!
 */
public class ToolActionDelegate implements IActionDelegate, IActionDelegate2, IToolRegistryListener, IDebugEventSetListener {
	
	
	private ToolAction fAction;
	
	
	protected ToolActionDelegate() {
	}
	
	
	public void init(final IAction action) {
		assert (action != null);
		
		fAction = (ToolAction) action;
		final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(true);
		NicoUI.getToolRegistry().addListener(this, page);
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	
	public void selectionChanged(final IAction action, final ISelection selection) {
	}
	
	public void dispose() {
	}
	
	
	public synchronized void toolSessionActivated(final ToolSessionUIData informations) {
		
		fAction.setTool(informations.getProcess());
	}
	
	public void toolSessionClosed(final ToolSessionUIData informations) {
	}
	
	public void handleDebugEvents(final DebugEvent[] events) {
		
		for (final DebugEvent event : events) {
			if (event.getKind() == DebugEvent.TERMINATE) {
				synchronized (this) {
					if (event.getSource() == fAction.getTool()) {
						fAction.handleToolTerminated();
					}
				}
			}
		}
	}
	
	
	public void run(final IAction action) {
	}
	
	public void runWithEvent(final IAction action, final Event event) {
	}
	
}
