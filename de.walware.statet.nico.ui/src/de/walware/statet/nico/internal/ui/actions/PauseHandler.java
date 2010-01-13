/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui.actions;

import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.util.IToolProvider;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.actions.ToolRetargetableHandler;


/**
 * Handler to toggle pause state of engine
 */
public class PauseHandler extends ToolRetargetableHandler implements IElementUpdater,
		IDebugEventSetListener {
	
	
	private boolean fIsChecked;
	
	private final ElementUpdater fUpdater = new ElementUpdater(NicoUI.PAUSE_COMMAND_ID);
	
	
	public PauseHandler(final IToolProvider toolProvider, final IServiceLocator serviceLocator) {
		super(toolProvider, serviceLocator);
		init();
	}
	
	@Override
	protected void init() {
		DebugPlugin.getDefault().addDebugEventListener(this);
		super.init();
	}
	
	@Override
	public void dispose() {
		final DebugPlugin debugManager = DebugPlugin.getDefault();
		if (debugManager != null) {
			debugManager.removeDebugEventListener(this);
		}
		
		super.dispose();
	}
	
	
	@Override
	protected void doRefresh() {
		if (getState() == S_ONAIR) {
			fUpdater.schedule();
		}
	}
	
	public void updateElement(final UIElement element, final Map parameters) {
		element.setChecked(fIsChecked);
	}
	
	@Override
	public boolean handleToolChanged() {
		final ToolProcess tool = getTool();
		final boolean wasChecked = fIsChecked;
		final ToolController controller = (tool != null) ? tool.getController() : null;
		fIsChecked = (controller != null && controller.isPaused());
		
		setBaseEnabled(evaluateEnabled());
		return (wasChecked != fIsChecked);
	}
	
	public void handleDebugEvents(final DebugEvent[] events) {
		if (getState() != S_ONAIR) {
			return;
		}
		boolean update = false;
		final ToolProcess tool = getTool();
		for (final DebugEvent event : events) {
			if (event.getSource() == tool) {
				switch (event.getKind()) {
				case DebugEvent.MODEL_SPECIFIC:
					Boolean checked = null;
					final int detail = event.getDetail();
					switch (detail) {
					case ToolProcess.REQUEST_PAUSE:
					case ToolProcess.STATUS_PAUSE:
						checked = Boolean.TRUE;
						break;
					case ToolProcess.REQUEST_PAUSE_CANCELED:
						checked = Boolean.FALSE;
						break;
					default:
						if ((detail & ToolProcess.STATUS) == ToolProcess.STATUS) { // status other than QUEUE_PAUSE
							checked = Boolean.FALSE;
						}
						break;
					}
					if (checked != null) {
						synchronized (this) {
							if (getState() != S_ONAIR || getTool() != tool) {
								return;
							}
							final boolean wasChecked = fIsChecked;
							fIsChecked = checked;
							
							update = (wasChecked != fIsChecked);
						}
					}
					break;
				case DebugEvent.TERMINATE:
					synchronized (this) {
						if (getTool() != tool) {
							return;
						}
						setBaseEnabled(false);
					}
					break;
				}
			}
		}
		
		if (update) {
			doRefresh();
		}
	}
	
	@Override
	protected Object doExecute(final ExecutionEvent event) {
		boolean update = false;
		synchronized (this) {
			final ToolProcess tool = getCheckedTool();
			final ToolController controller;
			final boolean wasChecked;
			try {
				controller = NicoUITools.accessController(null, tool);
				wasChecked = fIsChecked;
			}
			catch (final CoreException e) {
				StatusManager.getManager().handle(e.getStatus(), StatusManager.SHOW | StatusManager.LOG);
				return null;
			}
			
			fIsChecked = controller.pause(!wasChecked);
			update = (wasChecked != fIsChecked);
		}
		
		if (update) {
			doRefresh();
		}
		return null;
	}
	
}
