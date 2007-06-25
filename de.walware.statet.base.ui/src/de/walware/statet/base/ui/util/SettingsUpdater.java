/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui.util;

import java.util.Set;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;

import de.walware.eclipsecommons.preferences.SettingsChangeNotifier.ChangeListener;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.core.StatetCore;


/**
 * Util for settings changes in UI components.
 * Propagates changes to handler in UI thread and disposes automatically.
 */
public class SettingsUpdater implements ChangeListener {
	
	
	private ISettingsChangedHandler fHandler;
	private Control fControl;
	private DisposeListener fDisposeListener;
	private String[] fInterestingContexts;
	
	
	public SettingsUpdater(ISettingsChangedHandler handler, Control control) {
		this(handler, control, null);
	}
	
	public SettingsUpdater(ISettingsChangedHandler handler, Control control, String[] contexts) {
		fHandler = handler;
		fControl = control;
		setInterestingContexts(contexts);
		StatetCore.getSettingsChangeNotifier().addChangeListener(this);
		fDisposeListener = new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		};
		fControl.addDisposeListener(fDisposeListener);
	}

	public void setInterestingContexts(String[] contexts) {
		fInterestingContexts = contexts;
	}
	
	public void settingsChanged(Set<String> contexts) {
		if (fInterestingContexts == null) {
			runUpdate(contexts);
			return;
		}
		for (String id : fInterestingContexts) {
			if (contexts.contains(id)) {
				runUpdate(contexts);
				return;
			}
		}
	}
	
	private void runUpdate(final Set<String> contexts) {
		UIAccess.getDisplay().syncExec(new Runnable() {
			public void run() {
				if (UIAccess.isOkToUse(fControl)) {
					fHandler.handleSettingsChanged(contexts, null);
				}
			}
		});
	}
	
	public void dispose() {
		fControl.removeDisposeListener(fDisposeListener);
		StatetCore.getSettingsChangeNotifier().removeChangeListener(this);
	}
}
