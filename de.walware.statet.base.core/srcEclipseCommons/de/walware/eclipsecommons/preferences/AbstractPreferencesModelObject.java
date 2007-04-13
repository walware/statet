/*******************************************************************************
 * Copyright (c) 2007 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

import de.walware.eclipsecommons.AbstractSettingsModelObject;


/**
 * Extends settings object with preference related features
 * (e.g. load, save and listen to changes).
 */
public abstract class AbstractPreferencesModelObject extends AbstractSettingsModelObject {
	
	
	private IPreferenceAccess fPrefsListenTo;
	private IPreferenceChangeListener fPrefChangeListener;
	
	
	/**
	 * Returns the qualifier of all nodes, this model have preferences from.
	 * @return
	 */
	public abstract String[] getNodeQualifiers();
	
	/**
	 * Loads the default settings. 
	 */
	public abstract void loadDefaults();
	
	/**
	 * Loads the settings from preferences.
	 */
	public abstract void load(IPreferenceAccess prefs);
	
	/**
	 * Adds all preferences (definition and settings) to the map.
	 * 
	 * @param map
	 * @return the same map.
	 */
	public abstract Map<Preference, Object> deliverToPreferencesMap(Map<Preference, Object> map);

	/**
	 * Return map with all preferences (definition and settings)
	 * 
	 * @return new created map.
	 */
	public Map<Preference, Object> toPreferencesMap() {
		
		return deliverToPreferencesMap(new HashMap<Preference, Object>());
	}
	
	/**
	 * Loads the settings from preferences and keep them up to date.
	 */
	public void listen(IPreferenceAccess prefs) {
		
		String[] qualifiers = getNodeQualifiers();
		// dispose existing connection
		if (fPrefsListenTo != null) {
			for (String qualifier : qualifiers) {
				IEclipsePreferences[] nodes = fPrefsListenTo.getPreferenceNodes(qualifier);
				for (IEclipsePreferences node : nodes) {
					node.removePreferenceChangeListener(fPrefChangeListener);
				}
			}
		}
		fPrefsListenTo = prefs;
		if (fPrefsListenTo == null) {
			return;
		}
		if (fPrefChangeListener == null) {
			fPrefChangeListener = new IPreferenceChangeListener() {
				public void preferenceChange(PreferenceChangeEvent event) {
					onPreferenceChange(fPrefsListenTo, event);
				}
			};
		}
		// create new connection
		for (String qualifier : qualifiers) {
			IEclipsePreferences[] nodes = fPrefsListenTo.getPreferenceNodes(qualifier);
			for (IEclipsePreferences node : nodes) {
				node.addPreferenceChangeListener(fPrefChangeListener);
			}
		}
		load(fPrefsListenTo);
	}
	
	protected void onPreferenceChange(IPreferenceAccess prefs, PreferenceChangeEvent event) {
		
		load(prefs);
	}
	
	public void dispose() {
		
		listen(null);
		fPrefChangeListener = null;
	}
		
}
