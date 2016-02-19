/*=============================================================================#
 # Copyright (c) 2005-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.base.internal.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.text.ui.settings.AssistPreferences;

import de.walware.statet.base.ui.IStatetUIPreferenceConstants;


public class StatetUIPreferenceInitializer extends AbstractPreferenceInitializer {
	
	
	public StatetUIPreferenceInitializer() {
	}
	
	
	@Override
	public void initializeDefaultPreferences() {
//		// set the default values from ExtendedTextEditor
//		store.setValue(AbstractDecoratedTextEditorPreferenceConstants.USE_QUICK_DIFF_PREFERENCE_PAGE, true);
		final IScopeContext context= DefaultScope.INSTANCE;
		
		{	final AssistPreferences assistPrefs= IStatetUIPreferenceConstants.EDITING_ASSIST_PREFERENCES;
			PreferencesUtil.setPrefValue(context, assistPrefs.getAutoActivationEnabledPref(), Boolean.TRUE);
			PreferencesUtil.setPrefValue(context, assistPrefs.getAutoInsertSinglePref(), Boolean.FALSE);
			PreferencesUtil.setPrefValue(context, assistPrefs.getAutoInsertPrefixPref(), Boolean.FALSE);
		}
//		store.setDefault(EDITOROUTLINE_SORT, false);
//		store.setDefault(EDITOROUTLINE_LINKWITHEDITOR, true);
	}
	
}
