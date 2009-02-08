/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.core.preferences;

import java.util.Set;
import java.util.concurrent.locks.Lock;

import de.walware.ecommons.preferences.AbstractPreferencesModelObject;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.SettingsChangeNotifier;

import de.walware.statet.base.core.StatetCore;


/**
 * Keeps a preferences model up-to-date listening to changes of in the specified settings group.
 * 
 * The listener be disposed, if no longer required.
 */
public class PreferencesManageListener implements SettingsChangeNotifier.ManageListener {
	
	
	private AbstractPreferencesModelObject fModel;
	private IPreferenceAccess fPrefAccess;
	private String fGroupId;
	
	
	/**
	 * Creates a new listener.
	 */
	public PreferencesManageListener(final AbstractPreferencesModelObject model, final IPreferenceAccess prefs, final String groupId) {
		fModel = model;
		fPrefAccess = prefs;
		fGroupId = groupId;
		final Lock lock = fModel.getWriteLock();
		lock.lock();
		try {
			StatetCore.getSettingsChangeNotifier().addManageListener(this);
			fModel.load(fPrefAccess);
		}
		finally {
			lock.unlock();
		}
	}
	
	public void beforeSettingsChangeNotification(final Set<String> groupIds) {
		if (groupIds.contains(fGroupId)) {
			final Lock lock = fModel.getWriteLock();
			lock.lock();
			try {
				fModel.load(fPrefAccess);
			}
			finally {
				lock.unlock();
			}
		}
	}
	
	public void afterSettingsChangeNotification(final Set<String> groupIds) {
		fModel.resetDirty();
	}
	
	public void dispose() {
		StatetCore.getSettingsChangeNotifier().removeManageListener(this);
		fModel = null;
		fPrefAccess = null;
	}
	
}
