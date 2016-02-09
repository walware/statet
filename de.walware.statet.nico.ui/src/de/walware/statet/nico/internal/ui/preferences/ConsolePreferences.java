/*=============================================================================#
 # Copyright (c) 2006-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.ui.preferences;

import static de.walware.ecommons.text.ui.presentation.ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
import static de.walware.ecommons.text.ui.presentation.ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
import static de.walware.ecommons.text.ui.presentation.ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
import static de.walware.ecommons.text.ui.presentation.ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
import static de.walware.ecommons.text.ui.presentation.ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;

import java.util.EnumSet;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.core.Preference;
import de.walware.ecommons.preferences.core.Preference.EnumSetPref;

import de.walware.workbench.ui.IWaThemeConstants;
import de.walware.workbench.ui.util.ThemeUtil;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.ui.NicoUIPreferences;


public class ConsolePreferences extends AbstractPreferenceInitializer {
	
	
	public static final String GROUP_ID= "nico.console"; //$NON-NLS-1$
	
	public static final String OUTPUT_TEXTSTYLE_GROUP_ID= "nico.console/output/textstyle"; //$NON-NLS-1$
	
	
	public static final String KEY_HISTORYNAVIGATION_SUBMIT_TYPES= "HistoryNavigation.SubmitTypes.include"; //$NON-NLS-1$
	public static final Preference<EnumSet<SubmitType>> HISTORYNAVIGATION_SUBMIT_TYPES_PREF= new EnumSetPref<>(
			NicoUIPreferences.QUALIFIER, KEY_HISTORYNAVIGATION_SUBMIT_TYPES, SubmitType.class);
	
	
	@Override
	public void initializeDefaultPreferences() {
		final IScopeContext scope= DefaultScope.INSTANCE;
		final IEclipsePreferences outputPrefs= scope.getNode(NicoUIPreferences.OUTPUT_QUALIFIER);
		final ThemeUtil theme= new ThemeUtil();
		
		PreferencesUtil.setPrefValue(scope, NicoUIPreferences.OUTPUT_CHARLIMIT_PREF, 1000000);
		
		outputPrefs.put(NicoUIPreferences.OUTPUT_INFO_STREAM_ROOT_KEY + TEXTSTYLE_COLOR_SUFFIX,
				theme.getColorPrefValue(IWaThemeConstants.CONSOLE_INFO_COLOR) );
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_INFO_STREAM_ROOT_KEY + TEXTSTYLE_BOLD_SUFFIX, false);
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_INFO_STREAM_ROOT_KEY + TEXTSTYLE_ITALIC_SUFFIX, false);
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_INFO_STREAM_ROOT_KEY + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_INFO_STREAM_ROOT_KEY + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		outputPrefs.put(NicoUIPreferences.OUTPUT_STD_INPUT_STREAM_ROOT_KEY + TEXTSTYLE_COLOR_SUFFIX,
				theme.getColorPrefValue(IWaThemeConstants.CONSOLE_INPUT_COLOR) );
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_STD_INPUT_STREAM_ROOT_KEY + TEXTSTYLE_BOLD_SUFFIX, false);
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_STD_INPUT_STREAM_ROOT_KEY + TEXTSTYLE_ITALIC_SUFFIX, false);
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_STD_INPUT_STREAM_ROOT_KEY + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_STD_INPUT_STREAM_ROOT_KEY + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		outputPrefs.put(NicoUIPreferences.OUTPUT_STD_OUTPUT_ROOT_KEY + TEXTSTYLE_COLOR_SUFFIX,
				theme.getColorPrefValue(IWaThemeConstants.CONSOLE_OUTPUT_COLOR) );
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_STD_OUTPUT_ROOT_KEY + TEXTSTYLE_BOLD_SUFFIX, false);
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_STD_OUTPUT_ROOT_KEY + TEXTSTYLE_ITALIC_SUFFIX, false);
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_STD_OUTPUT_ROOT_KEY + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_STD_OUTPUT_ROOT_KEY + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		outputPrefs.put(NicoUIPreferences.OUTPUT_SYSTEM_OUTPUT_STREAM_ROOT_KEY + TEXTSTYLE_COLOR_SUFFIX,
				theme.getColorPrefValue(IWaThemeConstants.CONSOLE_2ND_OUTPUT_COLOR) );
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_SYSTEM_OUTPUT_STREAM_ROOT_KEY + TEXTSTYLE_BOLD_SUFFIX, false);
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_SYSTEM_OUTPUT_STREAM_ROOT_KEY + TEXTSTYLE_ITALIC_SUFFIX, false);
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_SYSTEM_OUTPUT_STREAM_ROOT_KEY + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_SYSTEM_OUTPUT_STREAM_ROOT_KEY + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		outputPrefs.put(NicoUIPreferences.OUTPUT_STD_ERROR_STREAM_ROOT_KEY + TEXTSTYLE_COLOR_SUFFIX,
				theme.getColorPrefValue(IWaThemeConstants.CONSOLE_ERROR_COLOR) );
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_STD_ERROR_STREAM_ROOT_KEY + TEXTSTYLE_BOLD_SUFFIX, false);
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_STD_ERROR_STREAM_ROOT_KEY + TEXTSTYLE_ITALIC_SUFFIX, false);
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_STD_ERROR_STREAM_ROOT_KEY + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		outputPrefs.putBoolean(NicoUIPreferences.OUTPUT_STD_ERROR_STREAM_ROOT_KEY + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		outputPrefs.put(NicoUIPreferences.OUTPUT_OTHER_TASKS_BACKGROUND_COLOR_KEY,
				theme.getColorPrefValue("de.walware.workbench.themes.CodeRawBackgroundColor") ); //$NON-NLS-1$
		
		PreferencesUtil.setPrefValue(scope, NicoUIPreferences.OUTPUT_FILTER_SUBMITTYPES_INCLUDE_PREF,
				EnumSet.allOf(SubmitType.class) );
		
		PreferencesUtil.setPrefValue(scope, HISTORYNAVIGATION_SUBMIT_TYPES_PREF,
				EnumSet.of(SubmitType.CONSOLE) );
	}
	
}
