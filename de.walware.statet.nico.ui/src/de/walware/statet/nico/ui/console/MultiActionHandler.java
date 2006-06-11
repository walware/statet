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

package de.walware.statet.nico.ui.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.texteditor.IUpdate;


/**
 * Manages activation of actions and keybiding contexts for different controls
 * (based on FocusIn/FocusOut event) in a view.
 */
public class MultiActionHandler implements Listener, ISelectionChangedListener {

	
	private class ActionWrapper extends Action {
		
		private Map<Widget, Action> fWidgetActionMap = new HashMap<Widget, Action>();
		
		ActionWrapper() {
		}
		
		public void update() {
			
			boolean enabled = false;
			
			if (fActiveWidget != null) {
				Action action = fWidgetActionMap.get(fActiveWidget);
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
		public void runWithEvent(Event event) {
			
			if (fActiveWidget != null) {
				Action action = fWidgetActionMap.get(fActiveWidget);
				if (action != null) {
					action.runWithEvent(event);
				}
			}
		}
	}
	
	private IViewSite fViewSite;
	private Widget fActiveWidget;
	private List<Widget> fKnownWidgets = new ArrayList<Widget>();
	private Map<String, ActionWrapper> fActions = new HashMap<String, ActionWrapper>();
	private Map<Widget, String> fScopes = new HashMap<Widget, String>();
	
	
	MultiActionHandler(IViewSite viewSite) {
		
		fViewSite = viewSite;
	}
	
	
	private ActionWrapper getActionWrapper(String id) {
		
		ActionWrapper wrapper = fActions.get(id);
		if (wrapper == null) {
			wrapper = new ActionWrapper();
			fActions.put(id, wrapper);
		}
		return wrapper;
	}
	
	public void addGlobalAction(Widget widget, String globalId, Action action) {
		
		ActionWrapper wrapper = getActionWrapper(globalId);
		wrapper.fWidgetActionMap.put(widget, action);
		
		addWidget(widget);
	}
	
	public void addKeybindingScope(Widget widget, String scope) {
		
		fScopes.put(widget, scope);
		
		addWidget(widget);
	}
	
	private void addWidget(Widget widget) {
		
		if (!fKnownWidgets.contains(widget)) {
			widget.addListener(SWT.FocusIn, this);
			widget.addListener(SWT.FocusOut, this);
			fKnownWidgets.add(widget);
		}
	}
	
	public void registerActions(IActionBars bars) {
		
		for (String id : fActions.keySet()) {
			bars.setGlobalActionHandler(id, fActions.get(id));
		}
	}

	public void handleEvent(Event event) {
		
		switch (event.type) {
		case SWT.FocusIn:
			fActiveWidget = event.widget;
			addScope(fActiveWidget);
			updateEnabledState();
			break;

		case SWT.FocusOut:
			removeScope(fActiveWidget);
			fActiveWidget = null;
			updateEnabledState();
			break;
			
		default:
			break;
		}
	}

	public void selectionChanged(SelectionChangedEvent event) {

		updateEnabledState(); 
	}
	
	void updateEnabledState() {
		
		for (ActionWrapper wrapper : fActions.values()) {
			wrapper.update();
		}
	}
	
	private void addScope(Widget widget) {
		
		String scope = fScopes.get(widget);
		IKeyBindingService keys = fViewSite.getKeyBindingService();
		if (scope == null || keys == null) {
			return;
		}

		String[] scopes = keys.getScopes();
		List<String> scopeList = Arrays.asList(scopes);
		if (!scopeList.contains(scope)) {
			int length = scopes.length;
			String[] newScopes = new String[length+1];
			System.arraycopy(scopes, 0, newScopes, 0, length);
			newScopes[length] = scope;
			keys.setScopes(newScopes);
		}
	}
	
	private void removeScope(Widget widget) {
		
		String scope = fScopes.get(widget);
		IKeyBindingService keys = fViewSite.getKeyBindingService();
		if (scope == null || keys == null) {
			return;
		}

		String[] scopes = keys.getScopes();
		List<String> scopeList = Arrays.asList(scopes);
		int idx = scopeList.indexOf(scope);
		if (idx >= 0) {
			int length = scopes.length;
			String[] newScopes = new String[length-1];
			System.arraycopy(scopes, 0, newScopes, 0, idx);
			System.arraycopy(scopes, idx+1, newScopes, idx, length-idx-1);
			keys.setScopes(newScopes);
		}
	}

	public void dispose() {
		
		fActiveWidget = null;
		fActions.clear();
		fKnownWidgets.clear();
	}

}
