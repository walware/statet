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

package de.walware.statet.base.core.preferences;

import java.util.Set;
import java.util.concurrent.locks.Lock;

import de.walware.eclipsecommons.preferences.AbstractPreferencesModelObject;
import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.SettingsChangeNotifier;

import de.walware.statet.base.core.StatetCore;


/**
 * Keeps a preferences model uptodate listening to changes of in specified settings context.
 * 
 * The listener be disposed, if no longer required.
 */
public class PreferencesManageListener implements SettingsChangeNotifier.ManageListener {

	private AbstractPreferencesModelObject fModel;
	private IPreferenceAccess fPrefAccess;
	private String fContext;
	
	
	/**
	 * Creates a new listener.
	 */
	public PreferencesManageListener(AbstractPreferencesModelObject model, IPreferenceAccess prefs, String context) {
		fModel = model;
		fPrefAccess = prefs;
		fContext = context;
		Lock lock = fModel.getWriteLock();
		lock.lock();
		try {
			StatetCore.getSettingsChangeNotifier().addManageListener(this);
			fModel.load(fPrefAccess);
		}
		finally {
			lock.unlock();
		}
	}
	
	public void beforeSettingsChangeNotification(Set<String> contexts) {
		if (contexts.contains(fContext)) {
			Lock lock = fModel.getWriteLock();
			lock.lock();
			try {
				fModel.load(fPrefAccess);
			}
			finally {
				lock.unlock();
			}
		}
	}
	
	public void afterSettingsChangeNotification(Set<String> contexts) {
		fModel.resetDirty();
	}
	
	public void dispose() {
		StatetCore.getSettingsChangeNotifier().removeManageListener(this);
		fModel = null;
		fPrefAccess = null;
	}
	
}
