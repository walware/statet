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
	
	
	public SettingsUpdater(ISettingsChangedHandler handler, Control control) {
		fHandler = handler;
		fControl = control;
		StatetCore.getSettingsChangeNotifier().addChangeListener(this);
		fDisposeListener = new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		};
		fControl.addDisposeListener(fDisposeListener);
	}

	
	public void settingsChanged(final Set<String> contexts) {
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
