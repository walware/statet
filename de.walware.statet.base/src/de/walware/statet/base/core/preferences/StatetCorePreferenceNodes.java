/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.core.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.preferences.DefaultScope;

import de.walware.eclipsecommon.preferences.Preference;
import de.walware.eclipsecommon.preferences.PreferencesUtil;

import de.walware.statet.base.StatetPlugin;


public class StatetCorePreferenceNodes {

	
	public static final String CORE_QUALIFIER = StatetPlugin.ID + "/core";
	
	public static final String CAT_MANAGMENT_QUALIFIER = CORE_QUALIFIER + "/managment";
	
	
	/**
	 * Initializes the default values.
	 */
	public static void initializeDefaultValues(DefaultScope scope) {

		Map<Preference, Object> defaults = new HashMap<Preference, Object>();
		
		new TaskTagsPreferences().addPreferencesToMap(defaults);
		
		for (Preference<Object> unit : defaults.keySet()) {
			PreferencesUtil.setPrefValue(scope, unit, defaults.get(unit));
		}
	}
	
}
