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

package de.walware.statet.nico.internal.ui.preferences;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.Preference.BooleanPref;
import de.walware.ecommons.preferences.Preference.EnumSetPref;
import de.walware.ecommons.ui.preferences.CombinedPreferenceStore;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.ui.NicoUIPreferenceNodes;


/**
 * 
 */
public class ConsolePreferences extends AbstractPreferenceInitializer {
	
	
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
	
	
	public static IPreferenceStore getStore() {
		return CombinedPreferenceStore.createStore(
				PreferencesUtil.getInstancePrefs(),
				NicoUIPreferenceNodes.CAT_CONSOLE_QUALIFIER);
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
		
		public FilterPreferences(final IPreferenceAccess prefs) {
			load(prefs);
		}
		
		protected void setup(final EnumSet<SubmitType> selectedTypes, final boolean showAllErrors) {
			fSubmitTypes = selectedTypes;
			fShowAllErrors = showAllErrors;
		}
		
		protected void load(final IPreferenceAccess prefs) {
			final EnumSet<SubmitType> selectedTypes = prefs.getPreferenceValue(PREF_FILTER_SUBMIT_TYPES);
			final boolean showAllErrors = prefs.getPreferenceValue(PREF_FILTER_SHOW_ALL_ERRORS);
			
			setup(selectedTypes, showAllErrors);
		}
		
		
		/**
		 * Allows to save the preferences. 
		 * 
		 * <p>Note: Intended to usage in preference/property page only.</p>
		 */
		public Map<Preference, Object> addPreferencesToMap(final Map<Preference, Object> map) {
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
		public boolean equals(final Object obj) {
			if (obj == null || !(obj instanceof FilterPreferences)) {
				return false;
			}
			
			final FilterPreferences other = (FilterPreferences) obj;
			return (fSubmitTypes.equals(other.fSubmitTypes)
						&& (fShowAllErrors == other.fShowAllErrors)
			);
		}
		
	}
	
	
	@Override
	public void initializeDefaultPreferences() {
		final DefaultScope defaultScope = new DefaultScope();
		
		final IPreferenceStore store = getStore();
		PreferenceConverter.setDefault(store, INPUT_COLOR, new RGB(31, 167, 111));
		PreferenceConverter.setDefault(store, INFO_COLOR, new RGB(31, 79, 175));
		PreferenceConverter.setDefault(store, OUTPUT_COLOR, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(store, ERROR_COLOR, new RGB(255, 0, 8));
		
		PreferencesUtil.setPrefValue(defaultScope, PREF_FILTER_SUBMIT_TYPES, SubmitType.getDefaultSet());
		PreferencesUtil.setPrefValue(defaultScope, PREF_FILTER_SHOW_ALL_ERRORS, false);
	}
	
}
