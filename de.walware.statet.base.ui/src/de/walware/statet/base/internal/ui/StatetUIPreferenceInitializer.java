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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.RGB;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.text.ui.settings.AssistPreferences;
import de.walware.ecommons.text.ui.settings.DecorationPreferences;

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
		
		// EditorPreferences
		final DecorationPreferences decoPrefs = IStatetUIPreferenceConstants.EDITING_DECO_PREFERENCES;
		PreferencesUtil.setPrefValue(defaults, decoPrefs.getMatchingBracketsEnabled(), Boolean.TRUE);
		PreferencesUtil.setPrefValue(defaults, decoPrefs.getMatchingBracketsColor(), new RGB(192, 192, 192));
		
		final AssistPreferences assistPrefs = IStatetUIPreferenceConstants.EDITING_ASSIST_PREFERENCES;
		PreferencesUtil.setPrefValue(defaults, assistPrefs.getAutoActivationEnabledPref(), Boolean.TRUE);
		PreferencesUtil.setPrefValue(defaults, assistPrefs.getAutoActivationDelayPref(), 200);
		PreferencesUtil.setPrefValue(defaults, assistPrefs.getAutoInsertSinglePref(), Boolean.FALSE);
		PreferencesUtil.setPrefValue(defaults, assistPrefs.getAutoInsertPrefixPref(), Boolean.FALSE);
		PreferencesUtil.setPrefValue(defaults, assistPrefs.getProposalsBackgroundPref(), new RGB(243, 247, 255));
		PreferencesUtil.setPrefValue(defaults, assistPrefs.getProposalsForegroundPref(), new RGB(0, 0, 0));
		PreferencesUtil.setPrefValue(defaults, assistPrefs.getInformationBackgroundPref(), new RGB(255, 255, 255));
		PreferencesUtil.setPrefValue(defaults, assistPrefs.getInformationForegroundPref(), new RGB(0, 0, 0));
		
//		store.setDefault(EDITOROUTLINE_SORT, false);
//		store.setDefault(EDITOROUTLINE_LINKWITHEDITOR, true);
	}
	
}
