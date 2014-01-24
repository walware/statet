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

package de.walware.statet.nico.ui.console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.texteditor.IUpdate;


/**
 * Manages activation of actions for different controls
 * (based on FocusIn/FocusOut event) in a view.
 * 
 * For compatibility with old action framework.
 */
public class MultiActionHandler implements Listener, ISelectionChangedListener {
	
	
	private class ActionWrapper extends Action {
		
		private final Map<Widget, IAction> fWidgetActionMap = new HashMap<Widget, IAction>();
		
		ActionWrapper() {
		}
		
		public void update() {
			boolean enabled = false;
			if (fActiveWidget != null) {
				final IAction action = fWidgetActionMap.get(fActiveWidget);
				if (action != null) {
					if (action instanceof IUpdate) {
						((IUpdate) action).update();
					}
					enabled = action.isEnabled();
				}
			}
			setEnabled(enabled);
		}
		
		@Override
		public void runWithEvent(final Event event) {
			if (fActiveWidget != null) {
				final IAction action = fWidgetActionMap.get(fActiveWidget);
				if (action != null) {
					action.runWithEvent(event);
				}
			}
		}
	}
	
	
	private Widget fActiveWidget;
	private final List<Widget> fKnownWidgets = new ArrayList<Widget>();
	private final Map<String, ActionWrapper> fActions = new HashMap<String, ActionWrapper>();
	
	
	MultiActionHandler() {
	}
	
	
	private ActionWrapper getActionWrapper(final String id) {
		ActionWrapper wrapper = fActions.get(id);
		if (wrapper == null) {
			wrapper = new ActionWrapper();
			fActions.put(id, wrapper);
		}
		return wrapper;
	}
	
	public void addGlobalAction(final Widget widget, final String globalId, final IAction action) {
		final ActionWrapper wrapper = getActionWrapper(globalId);
		wrapper.fWidgetActionMap.put(widget, action);
		
		addWidget(widget);
	}
	public void addCommandAction(final Widget widget, final String commandId, final IAction action) {
	}
	
	private void addWidget(final Widget widget) {
		if (!fKnownWidgets.contains(widget)) {
			widget.addListener(SWT.FocusIn, this);
			widget.addListener(SWT.FocusOut, this);
			fKnownWidgets.add(widget);
		}
	}
	
	public void registerActions(final IActionBars bars) {
		for (final String id : fActions.keySet()) {
			bars.setGlobalActionHandler(id, fActions.get(id));
		}
	}
	
	@Override
	public void handleEvent(final Event event) {
		switch (event.type) {
		case SWT.FocusIn:
			fActiveWidget = event.widget;
			updateEnabledState();
			break;
		case SWT.FocusOut:
			fActiveWidget = null;
			updateEnabledState();
			break;
			
		default:
			break;
		}
	}
	
	@Override
	public void selectionChanged(final SelectionChangedEvent event) {
		updateEnabledState(); 
	}
	
	void updateEnabledState() {
		for (final ActionWrapper wrapper : fActions.values()) {
			wrapper.update();
		}
	}
	public void dispose() {
		fActiveWidget = null;
		fActions.clear();
		fKnownWidgets.clear();
	}
	
}
