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

package de.walware.statet.nico.ui.internal.preferences;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

import de.walware.eclipsecommon.preferences.ICombinedPreferenceStore;
import de.walware.eclipsecommon.preferences.IPreferenceAccess;
import de.walware.eclipsecommon.preferences.Preference;
import de.walware.eclipsecommon.preferences.PreferencesUtil;
import de.walware.eclipsecommon.preferences.Preference.BooleanPref;
import de.walware.eclipsecommon.preferences.Preference.EnumSetPref;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.ui.NicoUIPreferenceNodes;


/**
 *
 */
public class ConsolePreferences {

	public static final String INPUT_COLOR = "input.color"; //$NON-NLS-1$
	public static final String INFO_COLOR = "info.color"; //$NON-NLS-1$
	public static final String OUTPUT_COLOR = "output.color"; //$NON-NLS-1$
	public static final String ERROR_COLOR = "error.color"; //$NON-NLS-1$
	
	public static final String KEY_FILTER_SUBMIT_TYPES = "filter.submit_types"; //$NON-NLS-1$
	public static final String KEY_FILTER_SHOW_ALL_ERRORS = "filter.show_all_errors"; //$NON-NLS-1$

	private static final EnumSetPref<SubmitType> PREF_FILTER_SUBMIT_TYPES = new EnumSetPref<SubmitType>(
			NicoUIPreferenceNodes.CAT_CONSOLE_QUALIFIER, KEY_FILTER_SUBMIT_TYPES, SubmitType.class);
	private static final BooleanPref PREF_FILTER_SHOW_ALL_ERRORS = new BooleanPref(
			NicoUIPreferenceNodes.CAT_CONSOLE_QUALIFIER, KEY_FILTER_SHOW_ALL_ERRORS); 

	
	public static ICombinedPreferenceStore getStore() {
		
		return PreferencesUtil.createCombindedPreferenceStore(
				PreferencesUtil.getInstancePrefs(),
				NicoUIPreferenceNodes.CAT_CONSOLE_QUALIFIER);
	}
	
	public static void initializeDefaults(Map<Preference, Object> map) {
		
		IPreferenceStore store = getStore();
		PreferenceConverter.setDefault(store, INPUT_COLOR, new RGB(73, 177, 117));
		PreferenceConverter.setDefault(store, INFO_COLOR, new RGB(0, 0, 127));
		PreferenceConverter.setDefault(store, OUTPUT_COLOR, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(store, ERROR_COLOR, new RGB(255, 0, 0));
		
		new FilterPreferences().addPreferencesToMap(map);
	}
	
	public static class FilterPreferences {
		
		private EnumSet<SubmitType> fSubmitTypes;
		private boolean fShowAllErrors;
		
		
		/**
		 * Creates preferences with default values.
		 */
		public FilterPreferences() {
			
			setup(	SubmitType.getDefaultSet(),
					false
			);
		}
		
		public FilterPreferences(IPreferenceAccess prefs) {
			
			load(prefs);
		}
		
		protected void setup(EnumSet<SubmitType> selectedTypes, boolean showAllErrors) {
			
			fSubmitTypes = selectedTypes;
			fShowAllErrors = showAllErrors;
		}
		
		protected void load(IPreferenceAccess prefs) {

			EnumSet<SubmitType> selectedTypes = prefs.getPreferenceValue(PREF_FILTER_SUBMIT_TYPES);
			boolean showAllErrors = prefs.getPreferenceValue(PREF_FILTER_SHOW_ALL_ERRORS);
			
			setup(selectedTypes, showAllErrors);
		}
		
		/**
		 * Allows to save the preferences. 
		 * 
		 * <p>Note: Intended to usage in preference/property page only.</p>
		 */
		public Map<Preference, Object> addPreferencesToMap(Map<Preference, Object> map) {
			
			map.put(PREF_FILTER_SUBMIT_TYPES, fSubmitTypes);
			map.put(PREF_FILTER_SHOW_ALL_ERRORS, fShowAllErrors);
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
		
		public boolean showAllErrors() {
			
			return fShowAllErrors;
		}
		
		
		@Override
		public boolean equals(Object obj) {
			
			if (obj == null || !(obj instanceof FilterPreferences)) {
				return false;
			}
			
			FilterPreferences other = (FilterPreferences) obj;
			return (fSubmitTypes.equals(other.fSubmitTypes)
						&& (fShowAllErrors == other.fShowAllErrors)
			);
		}
	}
}
