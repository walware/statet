/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.ui.actions;

import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.statet.nico.core.runtime.Queue;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.util.IToolProvider;
import de.walware.statet.nico.ui.NicoUI;
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
	
	@Override
	public void updateElement(final UIElement element, final Map parameters) {
		element.setChecked(fIsChecked);
	}
	
	@Override
	public boolean handleToolChanged() {
		final ToolProcess tool = getTool();
		final boolean wasChecked = fIsChecked;
		fIsChecked = (tool != null) ?
				tool.getQueue().isRequested(Queue.PAUSED_STATE) :
				false;
		
		setBaseEnabled(evaluateEnabled());
		return (wasChecked != fIsChecked);
	}
	
	@Override
	public void handleDebugEvents(final DebugEvent[] events) {
		if (getState() != S_ONAIR) {
			return;
		}
		boolean update = false;
		final ToolProcess tool = getTool();
		if (tool == null) {
			return;
		}
		ITER_EVENTS: for (final DebugEvent event : events) {
			if (event.getSource() == tool) {
				if (event.getKind() == DebugEvent.TERMINATE) {
					synchronized (this) {
						if (getTool() != tool) {
							return;
						}
						setBaseEnabled(false);
					}
					break;
				}
				continue ITER_EVENTS;
			}
			if (event.getSource() == tool.getQueue()) {
				Boolean checked= null;
				if (Queue.isStateRequest(event)) {
					final Queue.StateDelta delta= (Queue.StateDelta) event.getData();
					if (delta.newState == Queue.PAUSED_STATE) {
						checked= Boolean.TRUE;
					}
					else {
						checked= Boolean.FALSE;
					}
				}
				else if (Queue.isStateChange(event)) {
					final Queue.StateDelta delta= (Queue.StateDelta) event.getData();
					if (delta.newState == Queue.PAUSED_STATE) {
						checked= Boolean.TRUE;
					}
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
				continue ITER_EVENTS;
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
			final boolean wasChecked= fIsChecked;
			if (tool != null) {
				final boolean success= (!wasChecked) ?
						tool.getQueue().pause() : tool.getQueue().resume();
				if (success) {
					fIsChecked= !wasChecked;
				}
			}
			else {
				fIsChecked= false;
			}
			update = (wasChecked != fIsChecked);
		}
		
		if (update) {
			doRefresh();
		}
		return null;
	}
	
}
