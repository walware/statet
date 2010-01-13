/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.preferences;

import java.util.Set;
import java.util.concurrent.locks.Lock;


/**
 * Keeps a preferences model up-to-date listening to changes of in the specified settings group.
 * 
 * The listener be disposed, if no longer required.
 */
public class PreferencesManageListener implements SettingsChangeNotifier.ManageListener {
	
	
	private AbstractPreferencesModelObject fModel;
	private IPreferenceAccess fPrefAccess;
	private String[] fGroupIds;
	
	
	/**
	 * Creates a new listener for a single group id.
	 */
	public PreferencesManageListener(final AbstractPreferencesModelObject model, final IPreferenceAccess prefs, final String groupId) {
		this(model, prefs, new String[] { groupId });
	}
	
	/**
	 * Creates a new listener for multiple group ids.
	 */
	public PreferencesManageListener(final AbstractPreferencesModelObject model, final IPreferenceAccess prefs, final String[] groupIds) {
		fModel = model;
		fPrefAccess = prefs;
		fGroupIds = groupIds;
		final Lock lock = fModel.getWriteLock();
		lock.lock();
		try {
			PreferencesUtil.getSettingsChangeNotifier().addManageListener(this);
			fModel.load(fPrefAccess);
		}
		finally {
			lock.unlock();
		}
	}
	
	public void beforeSettingsChangeNotification(final Set<String> groupIds) {
		for (final String id : fGroupIds) {
			if (groupIds.contains(id)) {
				final Lock lock = fModel.getWriteLock();
				lock.lock();
				try {
					fModel.load(fPrefAccess);
				}
				finally {
					lock.unlock();
				}
				return;
			}
		}
	}
	
	public void afterSettingsChangeNotification(final Set<String> groupIds) {
		fModel.resetDirty();
	}
	
	public void dispose() {
		PreferencesUtil.getSettingsChangeNotifier().removeManageListener(this);
		fModel = null;
		fPrefAccess = null;
	}
	
}
