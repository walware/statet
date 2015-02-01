/*=============================================================================#
 # Copyright (c) 2006-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;

import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.PreferencesUtil;

import de.walware.statet.nico.core.NicoPreferenceNodes;
import de.walware.statet.nico.internal.core.preferences.HistoryPreferences;


public class NicoPreferenceInitializer extends AbstractPreferenceInitializer {
	
	
	@Override
	public void initializeDefaultPreferences() {
		final DefaultScope scope = new DefaultScope();
		final Map<Preference<?>, Object> map = new HashMap<Preference<?>, Object>();
		
		new HistoryPreferences().addPreferencesToMap(map);
		map.put(NicoPreferenceNodes.KEY_DEFAULT_TIMEOUT, 15000);
		
		PreferencesUtil.setPrefValues(scope, map);
	}
	
}
