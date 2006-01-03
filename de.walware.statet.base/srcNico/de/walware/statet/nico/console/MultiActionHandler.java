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

package de.walware.statet.nico.console;

import java.util.ArrayList;
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
import org.eclipse.ui.texteditor.IUpdate;


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
	
	private Widget fActiveWidget;
	private List<Widget> fKnownWidgets = new ArrayList<Widget>();
	private Map<String, ActionWrapper> fActions = new HashMap<String, ActionWrapper>();
	
	private ActionWrapper getActionWrapper(String id) {
		
		ActionWrapper wrapper = fActions.get(id);
		if (wrapper == null) {
			wrapper = new ActionWrapper();
			fActions.put(id, wrapper);
		}
		return wrapper;
	}
	
	public void addAction(Widget widget, String globalId, Action action) {
		
		ActionWrapper wrapper = getActionWrapper(globalId);
		wrapper.fWidgetActionMap.put(widget, action);
		
		if (!fKnownWidgets.contains(widget)) {
			widget.addListener(SWT.FocusIn, this);
			widget.addListener(SWT.FocusOut, this);
			fKnownWidgets.add(widget);
		}
	}
	
	public void registerGlobalActions(IActionBars bars) {
		
		for (String id : fActions.keySet()) {
			bars.setGlobalActionHandler(id, fActions.get(id));
		}
	}

	public void handleEvent(Event event) {
		
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

	public void selectionChanged(SelectionChangedEvent event) {

		updateEnabledState(); 
	}
	
	void updateEnabledState() {
		
		for (ActionWrapper wrapper : fActions.values()) {
			wrapper.update();
		}
	}

	public void dispose() {
		
		fActiveWidget = null;
		fActions.clear();
		fKnownWidgets.clear();
	}
}
