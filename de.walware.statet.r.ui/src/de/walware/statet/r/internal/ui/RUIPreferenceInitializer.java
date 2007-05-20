/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.spelling.SpellingService;

import de.walware.eclipsecommons.preferences.PreferencesUtil;

import de.walware.statet.r.internal.debug.ui.RDebugPreferenceConstants;
import de.walware.statet.r.ui.RUIPreferenceConstants;
import de.walware.statet.r.ui.editors.REditorOptions;


public class RUIPreferenceInitializer extends AbstractPreferenceInitializer {


	public void initializeDefaultPreferences() {
		IPreferenceStore store = RUIPlugin.getDefault().getPreferenceStore();
		EditorsUI.useAnnotationsPreferencePage(store);
		EditorsUI.useQuickDiffPreferencePage(store);
		RUIPreferenceConstants.initializeDefaultValues(store);
		
		DefaultScope defaultScope = new DefaultScope();
		PreferencesUtil.setPrefValue(defaultScope, REditorOptions.PREF_SMARTINSERT_ASDEFAULT, true);
		store.setValue(SpellingService.PREFERENCE_SPELLING_ENABLED, true);
		PreferencesUtil.setPrefValue(defaultScope, REditorOptions.PREF_SPELLCHECKING_ENABLED, false);
		
		RDebugPreferenceConstants.initializeDefaultValues(defaultScope);
	}

}