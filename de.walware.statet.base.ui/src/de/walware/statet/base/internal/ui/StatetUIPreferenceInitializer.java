/*******************************************************************************
 * Copyright (c) 2005-2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.internal.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

import de.walware.statet.base.ui.IStatetUIPreferenceConstants;


public class StatetUIPreferenceInitializer extends AbstractPreferenceInitializer {


	@Override
	public void initializeDefaultPreferences() {

		IPreferenceStore store = StatetUIPlugin.getDefault().getPreferenceStore();
		initializeTextEditiongPreferences(store);
	}

	
	private static void initializeTextEditiongPreferences(IPreferenceStore store) {
//		// set the default values from ExtendedTextEditor
//		store.setValue(AbstractDecoratedTextEditorPreferenceConstants.USE_QUICK_DIFF_PREFERENCE_PAGE, true);
		
		// EditorPreferences
		store.setDefault(IStatetUIPreferenceConstants.EDITOR_MATCHING_BRACKETS, true);
		PreferenceConverter.setDefault(store, IStatetUIPreferenceConstants.
				EDITOR_MATCHING_BRACKETS_COLOR, new RGB(192, 192, 192));

		store.setDefault(IStatetUIPreferenceConstants.CODEASSIST_AUTOACTIVATION, true);
		store.setDefault(IStatetUIPreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY, 200);
		store.setDefault(IStatetUIPreferenceConstants.CODEASSIST_AUTOINSERT, true);
		PreferenceConverter.setDefault(store, IStatetUIPreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND, new RGB(243, 247, 255));
		PreferenceConverter.setDefault(store, IStatetUIPreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(store, IStatetUIPreferenceConstants.CODEASSIST_PARAMETERS_BACKGROUND, new RGB(255, 255, 255));
		PreferenceConverter.setDefault(store, IStatetUIPreferenceConstants.CODEASSIST_PARAMETERS_FOREGROUND, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(store, IStatetUIPreferenceConstants.CODEASSIST_REPLACEMENT_BACKGROUND, new RGB(255, 255, 0));
		PreferenceConverter.setDefault(store, IStatetUIPreferenceConstants.CODEASSIST_REPLACEMENT_FOREGROUND, new RGB(255, 0, 0));
		
//		store.setDefault(EDITOROUTLINE_SORT, false);
//		store.setDefault(EDITOROUTLINE_LINKWITHEDITOR, true);
	}
	
}