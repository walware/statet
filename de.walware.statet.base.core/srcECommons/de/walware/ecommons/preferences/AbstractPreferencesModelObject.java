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

package de.walware.ecommons.preferences;

import java.util.HashMap;
import java.util.Map;

import de.walware.ecommons.AbstractSettingsModelObject;


/**
 * Extends settings object with preference related features
 * (e.g. load, save and listen to changes).
 */
public abstract class AbstractPreferencesModelObject extends AbstractSettingsModelObject {
	
	
	protected AbstractPreferencesModelObject() {
	}
	
	
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
	
}
