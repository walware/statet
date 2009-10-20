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

package de.walware.statet.nico.internal.core.preferences;

import java.util.HashMap;
import java.util.Map;

import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.Preference.IntPref;

import de.walware.statet.nico.core.NicoPreferenceNodes;


/**
 * 
 */
public class HistoryPreferences {
	
	
	private static final String KEY_LIMIT_COUNT = "limit.count"; //$NON-NLS-1$
	
	private static final IntPref PREF_LIMIT_COUNT = new IntPref(
			NicoPreferenceNodes.CAT_HISTORY_QUALIFIER, KEY_LIMIT_COUNT);
	
	
	private int fLimitCount;
	
	
	/**
	 * Creates preferences with default values.
	 */
	public HistoryPreferences() {
		setup(10000);
	}
	
	public HistoryPreferences(final IPreferenceAccess prefs) {
		load(prefs);
	}
	
	protected void setup(final int limitCount) {
		fLimitCount = limitCount; 
	}
	
	protected void load(final IPreferenceAccess prefs) {
		final int limitCount = prefs.getPreferenceValue(PREF_LIMIT_COUNT);
		
		setup(limitCount);
	}
	
	
	/**
	 * Allows to save the preferences. 
	 * 
	 * <p>Note: Intended to usage in preference/property page only.</p>
	 */
	public Map<Preference, Object> addPreferencesToMap(final Map<Preference, Object> map) {
		map.put(PREF_LIMIT_COUNT, fLimitCount);
		return map;
	}
	
	/**
	 * Allows to save the preferences. 
	 * 
	 * <p>Note: Intended to usage in preference/property page only.</p>
	 */
	public Map<Preference, Object> getPreferencesMap() {
		return addPreferencesToMap(new HashMap<Preference, Object>(2));
	}
	
	
	public int getLimitCount() {
		return fLimitCount;
	}
	
	
	@Override
	public int hashCode() {
		return fLimitCount;
	}
	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof HistoryPreferences)) {
			return false;
		}
		
		final HistoryPreferences other = (HistoryPreferences) obj;
		return (fLimitCount == other.fLimitCount);
	}
	
}
