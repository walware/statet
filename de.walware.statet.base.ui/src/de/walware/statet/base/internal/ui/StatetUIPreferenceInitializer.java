/*******************************************************************************
 * Copyright (c) 2005-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.internal.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.text.ui.settings.AssistPreferences;
import de.walware.ecommons.text.ui.settings.DecorationPreferences;

import de.walware.workbench.ui.IWaThemeConstants;
import de.walware.workbench.ui.util.ThemeUtil;

import de.walware.statet.base.ui.IStatetUIPreferenceConstants;


public class StatetUIPreferenceInitializer extends AbstractPreferenceInitializer {
	
	
	@Override
	public void initializeDefaultPreferences() {
		
		final IPreferenceStore store = StatetUIPlugin.getDefault().getPreferenceStore();
		initializeTextEditiongPreferences(store);
	}
	
	
	private static void initializeTextEditiongPreferences(final IPreferenceStore store) {
//		// set the default values from ExtendedTextEditor
//		store.setValue(AbstractDecoratedTextEditorPreferenceConstants.USE_QUICK_DIFF_PREFERENCE_PAGE, true);
		final DefaultScope defaults = new DefaultScope();
		final IEclipsePreferences prefs = defaults.getNode(StatetUIPlugin.PLUGIN_ID);
		final ThemeUtil theme = new ThemeUtil();
		
		// EditorPreferences
		prefs.putBoolean(DecorationPreferences.MATCHING_BRACKET_ENABLED_KEY, true);
		prefs.put(DecorationPreferences.MATCHING_BRACKET_COLOR_KEY, theme.getColorPrefValue(IWaThemeConstants.MATCHING_BRACKET_COLOR));
		
		{	final IEclipsePreferences node = defaults.getNode(IStatetUIPreferenceConstants.CAT_EDITOR_OPTIONS_QUALIFIER);
			final AssistPreferences assistPrefs = IStatetUIPreferenceConstants.EDITING_ASSIST_PREFERENCES;
			PreferencesUtil.setPrefValue(defaults, assistPrefs.getAutoActivationEnabledPref(), Boolean.TRUE);
			PreferencesUtil.setPrefValue(defaults, assistPrefs.getAutoActivationDelayPref(), 200);
			PreferencesUtil.setPrefValue(defaults, assistPrefs.getAutoInsertSinglePref(), Boolean.FALSE);
			PreferencesUtil.setPrefValue(defaults, assistPrefs.getAutoInsertPrefixPref(), Boolean.FALSE);
			node.put("Parameters.background", theme.getColorPrefValue(IWaThemeConstants.INFORMATION_BACKGROUND_COLOR)); //$NON-NLS-1$
			node.put("Parameters.foreground", theme.getColorPrefValue(IWaThemeConstants.INFORMATION_COLOR)); //$NON-NLS-1$
		}
//		store.setDefault(EDITOROUTLINE_SORT, false);
//		store.setDefault(EDITOROUTLINE_LINKWITHEDITOR, true);
	}
	
}
