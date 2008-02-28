/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.core.preferences;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.preferences.Preference.BooleanPref;
import de.walware.eclipsecommons.preferences.Preference.EnumSetPref;
import de.walware.eclipsecommons.preferences.Preference.IntPref;

import de.walware.statet.nico.core.NicoPreferenceNodes;
import de.walware.statet.nico.core.runtime.SubmitType;


/**
 * 
 */
public class HistoryPreferences {
	
	
	private static final String KEY_FILTER_SUBMIT_TYPES = "filter.submit_types"; //$NON-NLS-1$
	private static final String KEY_FILTER_COMMENTS = "filter.comments"; //$NON-NLS-1$
	private static final String KEY_LIMIT_COUNT = "limit.count"; //$NON-NLS-1$
	
	private static final EnumSetPref<SubmitType> PREF_FILTER_SUBMIT_TYPES = new EnumSetPref<SubmitType>(
			NicoPreferenceNodes.CAT_HISTORY_QUALIFIER, KEY_FILTER_SUBMIT_TYPES, SubmitType.class);
	private static final BooleanPref PREF_FILTER_COMMENTS = new BooleanPref(
			NicoPreferenceNodes.CAT_HISTORY_QUALIFIER, KEY_FILTER_COMMENTS);
	private static final IntPref PREF_LIMIT_COUNT = new IntPref(
			NicoPreferenceNodes.CAT_HISTORY_QUALIFIER, KEY_LIMIT_COUNT);
	
	
	private EnumSet<SubmitType> fSubmitTypes;
	private boolean fFilterComments;
	private int fLimitCount;
	
	
	/**
	 * Creates preferences with default values.
	 */
	public HistoryPreferences() {
		setup(	EnumSet.of(SubmitType.CONSOLE, SubmitType.EDITOR),
				true,
				100
		);
	}
	
	public HistoryPreferences(final IPreferenceAccess prefs) {
		load(prefs);
	}
	
	protected void setup(final EnumSet<SubmitType> selectedTypes, final boolean filterComments, final int limitCount) {
		fSubmitTypes = selectedTypes;
		fFilterComments = filterComments;
		fLimitCount = limitCount; 
	}
	
	protected void load(final IPreferenceAccess prefs) {
		final EnumSet<SubmitType> selectedTypes = prefs.getPreferenceValue(PREF_FILTER_SUBMIT_TYPES);
		final boolean filterComments = prefs.getPreferenceValue(PREF_FILTER_COMMENTS);
		final int limitCount = prefs.getPreferenceValue(PREF_LIMIT_COUNT);
		
		setup(selectedTypes, filterComments, limitCount);
	}
	
	
	/**
	 * Allows to save the preferences. 
	 * 
	 * <p>Note: Intended to usage in preference/property page only.</p>
	 */
	public Map<Preference, Object> addPreferencesToMap(final Map<Preference, Object> map) {
		map.put(PREF_FILTER_SUBMIT_TYPES, fSubmitTypes);
		map.put(PREF_FILTER_COMMENTS, fFilterComments);
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
	
	
	public EnumSet<SubmitType> getSelectedTypes() {
		return fSubmitTypes;
	}
	
	public boolean filterComments() {
		return fFilterComments;
	}
	
	public int getLimitCount() {
		return fLimitCount;
	}
	
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof HistoryPreferences)) {
			return false;
		}
		
		final HistoryPreferences other = (HistoryPreferences) obj;
		return (fSubmitTypes.equals(other.fSubmitTypes)
					&& (fFilterComments == other.fFilterComments)
					&& (fLimitCount == other.fLimitCount)
		);
	}
	
}
