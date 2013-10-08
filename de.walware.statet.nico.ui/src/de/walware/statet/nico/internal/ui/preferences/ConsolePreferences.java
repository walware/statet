/*******************************************************************************
 * Copyright (c) 2006-2013 Stephan Wahlbrink (www.walware.de/goto/opensource)
 * and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui.preferences;

import static de.walware.ecommons.text.ui.presentation.ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
import static de.walware.ecommons.text.ui.presentation.ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
import static de.walware.ecommons.text.ui.presentation.ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
import static de.walware.ecommons.text.ui.presentation.ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
import static de.walware.ecommons.text.ui.presentation.ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.preference.IPreferenceStore;

import de.walware.ecommons.ltk.ui.util.CombinedPreferenceStore;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.Preference.BooleanPref;
import de.walware.ecommons.preferences.Preference.EnumSetPref;
import de.walware.ecommons.preferences.Preference.IntPref;
import de.walware.ecommons.preferences.PreferencesUtil;

import de.walware.workbench.ui.IWaThemeConstants;
import de.walware.workbench.ui.util.ThemeUtil;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.ui.NicoUIPreferenceNodes;


public class ConsolePreferences extends AbstractPreferenceInitializer {
	
	
	public static final String GROUP_ID= "nico.console"; //$NON-NLS-1$
	
	public static final String KEY_FILTER_SUBMIT_TYPES= "Output.Filter.SubmitTypes.include"; //$NON-NLS-1$
	public static final String KEY_FILTER_SHOW_ALL_ERRORS= "Output.Filter.ShowAllErrors.enable"; //$NON-NLS-1$
	
	public static final String KEY_CHARLIMIT= "Output.CharLimit.num"; //$NON-NLS-1$
	
	
	public static final String OUTPUT_TEXTSTYLE_GROUP_ID= "nico.console/output/textstyle"; //$NON-NLS-1$
	
	public static final String OUTPUT_INPUT_ROOT_KEY= "Output.Input"; //$NON-NLS-1$
	public static final String OUTPUT_INFO_ROOT_KEY= "Output.Info"; //$NON-NLS-1$
	public static final String OUTPUT_STANDARD_OUTPUT_ROOT_KEY= "Output.Output"; //$NON-NLS-1$
	public static final String OUTPUT_STANDARD_ERROR_ROOT_KEY= "Output.Error"; //$NON-NLS-1$
	public static final String OUTPUT_SYSTEM_OUTPUT_ROOT_KEY= "Output.SystemOutput"; //$NON-NLS-1$
	
	
	public static final String KEY_HISTORYNAVIGATION_SUBMIT_TYPES= "HistoryNavigation.SubmitTypes.include"; //$NON-NLS-1$
	
	public static final Preference<EnumSet<SubmitType>> PREF_FILTER_SUBMIT_TYPES= new EnumSetPref<SubmitType>(
			NicoUIPreferenceNodes.CAT_CONSOLE_QUALIFIER, KEY_FILTER_SUBMIT_TYPES, SubmitType.class);
	public static final Preference<Boolean> PREF_FILTER_SHOW_ALL_ERRORS= new BooleanPref(
			NicoUIPreferenceNodes.CAT_CONSOLE_QUALIFIER, KEY_FILTER_SHOW_ALL_ERRORS);
	
	public static final Preference<Integer> PREF_CHARLIMIT= new IntPref(
			NicoUIPreferenceNodes.CAT_CONSOLE_QUALIFIER, KEY_CHARLIMIT);
	
	public static final Preference<EnumSet<SubmitType>> PREF_HISTORYNAVIGATION_SUBMIT_TYPES= new EnumSetPref<SubmitType>(
			NicoUIPreferenceNodes.CAT_CONSOLE_QUALIFIER, KEY_HISTORYNAVIGATION_SUBMIT_TYPES, SubmitType.class);
	
	
	public static IPreferenceStore getStore() {
		return CombinedPreferenceStore.createStore(
				PreferencesUtil.getInstancePrefs(),
				NicoUIPreferenceNodes.CAT_CONSOLE_QUALIFIER);
	}
	
	
	public static class FilterPreferences {
		
		
		private EnumSet<SubmitType> submitTypes;
		private boolean showAllErrors;
		
		
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
			this.submitTypes= selectedTypes;
			this.showAllErrors= showAllErrors;
		}
		
		protected void load(final IPreferenceAccess prefs) {
			final EnumSet<SubmitType> selectedTypes= prefs.getPreferenceValue(PREF_FILTER_SUBMIT_TYPES);
			final boolean showAllErrors= prefs.getPreferenceValue(PREF_FILTER_SHOW_ALL_ERRORS);
			
			setup(selectedTypes, showAllErrors);
		}
		
		
		/**
		 * Allows to save the preferences. 
		 * 
		 * <p>Note: Intended to usage in preference/property page only.</p>
		 */
		public Map<Preference<?>, Object> addPreferencesToMap(final Map<Preference<?>, Object> map) {
			map.put(PREF_FILTER_SUBMIT_TYPES, this.submitTypes);
			map.put(PREF_FILTER_SHOW_ALL_ERRORS, this.showAllErrors);
			return map;
		}
		
		/**
		 * Allows to save the preferences. 
		 * 
		 * <p>Note: Intended to usage in preference/property page only.</p>
		 */
		public Map<Preference<?>, Object> getPreferencesMap() {
			return addPreferencesToMap(new HashMap<Preference<?>, Object>(2));
		}
		
		public EnumSet<SubmitType> getSelectedTypes() {
			return this.submitTypes;
		}
		
		public boolean showAllErrors() {
			return this.showAllErrors;
		}
		
		
		@Override
		public int hashCode() {
			return this.submitTypes.size();
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (obj == null || !(obj instanceof FilterPreferences)) {
				return false;
			}
			
			final FilterPreferences other= (FilterPreferences) obj;
			return (this.submitTypes.equals(other.submitTypes)
						&& (this.showAllErrors == other.showAllErrors)
			);
		}
		
	}
	
	
	@Override
	public void initializeDefaultPreferences() {
		final IScopeContext scope= DefaultScope.INSTANCE;
		final IEclipsePreferences consolePrefs= scope.getNode(NicoUIPreferenceNodes.CAT_CONSOLE_QUALIFIER);
		final ThemeUtil theme= new ThemeUtil();
		
		PreferencesUtil.setPrefValue(scope, PREF_FILTER_SUBMIT_TYPES, SubmitType.getDefaultSet());
		PreferencesUtil.setPrefValue(scope, PREF_FILTER_SHOW_ALL_ERRORS, false);
		
		PreferencesUtil.setPrefValue(scope, PREF_CHARLIMIT, 500000);
		
		
		consolePrefs.put(OUTPUT_INPUT_ROOT_KEY + TEXTSTYLE_COLOR_SUFFIX,
				theme.getColorPrefValue(IWaThemeConstants.CONSOLE_INPUT_COLOR) );
		consolePrefs.putBoolean(OUTPUT_INPUT_ROOT_KEY + TEXTSTYLE_BOLD_SUFFIX, false);
		consolePrefs.putBoolean(OUTPUT_INPUT_ROOT_KEY + TEXTSTYLE_ITALIC_SUFFIX, false);
		consolePrefs.putBoolean(OUTPUT_INPUT_ROOT_KEY + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		consolePrefs.putBoolean(OUTPUT_INPUT_ROOT_KEY + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		consolePrefs.put(OUTPUT_INFO_ROOT_KEY + TEXTSTYLE_COLOR_SUFFIX,
				theme.getColorPrefValue(IWaThemeConstants.CONSOLE_INFO_COLOR) );
		consolePrefs.putBoolean(OUTPUT_INFO_ROOT_KEY + TEXTSTYLE_BOLD_SUFFIX, false);
		consolePrefs.putBoolean(OUTPUT_INFO_ROOT_KEY + TEXTSTYLE_ITALIC_SUFFIX, false);
		consolePrefs.putBoolean(OUTPUT_INFO_ROOT_KEY + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		consolePrefs.putBoolean(OUTPUT_INFO_ROOT_KEY + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		consolePrefs.put(OUTPUT_STANDARD_OUTPUT_ROOT_KEY + TEXTSTYLE_COLOR_SUFFIX,
				theme.getColorPrefValue(IWaThemeConstants.CONSOLE_OUTPUT_COLOR) );
		consolePrefs.putBoolean(OUTPUT_STANDARD_OUTPUT_ROOT_KEY + TEXTSTYLE_BOLD_SUFFIX, false);
		consolePrefs.putBoolean(OUTPUT_STANDARD_OUTPUT_ROOT_KEY + TEXTSTYLE_ITALIC_SUFFIX, false);
		consolePrefs.putBoolean(OUTPUT_STANDARD_OUTPUT_ROOT_KEY + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		consolePrefs.putBoolean(OUTPUT_STANDARD_OUTPUT_ROOT_KEY + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		consolePrefs.put(OUTPUT_SYSTEM_OUTPUT_ROOT_KEY + TEXTSTYLE_COLOR_SUFFIX,
				theme.getColorPrefValue(IWaThemeConstants.CONSOLE_2ND_OUTPUT_COLOR) );
		consolePrefs.putBoolean(OUTPUT_SYSTEM_OUTPUT_ROOT_KEY + TEXTSTYLE_BOLD_SUFFIX, false);
		consolePrefs.putBoolean(OUTPUT_SYSTEM_OUTPUT_ROOT_KEY + TEXTSTYLE_ITALIC_SUFFIX, false);
		consolePrefs.putBoolean(OUTPUT_SYSTEM_OUTPUT_ROOT_KEY + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		consolePrefs.putBoolean(OUTPUT_SYSTEM_OUTPUT_ROOT_KEY + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		consolePrefs.put(OUTPUT_STANDARD_ERROR_ROOT_KEY + TEXTSTYLE_COLOR_SUFFIX,
				theme.getColorPrefValue(IWaThemeConstants.CONSOLE_ERROR_COLOR) );
		consolePrefs.putBoolean(OUTPUT_STANDARD_ERROR_ROOT_KEY + TEXTSTYLE_BOLD_SUFFIX, false);
		consolePrefs.putBoolean(OUTPUT_STANDARD_ERROR_ROOT_KEY + TEXTSTYLE_ITALIC_SUFFIX, false);
		consolePrefs.putBoolean(OUTPUT_STANDARD_ERROR_ROOT_KEY + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		consolePrefs.putBoolean(OUTPUT_STANDARD_ERROR_ROOT_KEY + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		
		PreferencesUtil.setPrefValue(scope, PREF_HISTORYNAVIGATION_SUBMIT_TYPES, EnumSet.of(SubmitType.CONSOLE));
	}
	
}
