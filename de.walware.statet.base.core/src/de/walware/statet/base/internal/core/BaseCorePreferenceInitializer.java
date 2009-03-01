/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.internal.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;

import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.PreferencesUtil;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;


/**
 * 
 */
public class BaseCorePreferenceInitializer extends AbstractPreferenceInitializer {
	
	
	@Override
	public void initializeDefaultPreferences() {
		final DefaultScope scope = new DefaultScope();
		
		final Map<Preference, Object> defaults = new HashMap<Preference, Object>();
		
		new TaskTagsPreferences().addPreferencesToMap(defaults);
		
		for (final Entry<Preference, Object> entry : defaults.entrySet()) {
			PreferencesUtil.setPrefValue(scope, entry.getKey(), entry.getValue());
		}
	}
	
}
