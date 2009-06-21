/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk.internal.ui.refactoring;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.Preference.BooleanPref;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


public class ECommonsRefactoring {
	
	
	public static final BooleanPref PREF_SAVE_ALL_EDITORS = new BooleanPref(StatetUIPlugin.PLUGIN_ID + "/Refactoring", "SaveAll.enabled");
	
	
	public static boolean getSaveAllEditors() {
		return PreferencesUtil.getInstancePrefs().getPreferenceValue(PREF_SAVE_ALL_EDITORS);
	}
	
	public static void setSaveAllEditors(final boolean save) {
		PreferencesUtil.setPrefValue(PreferencesUtil.getInstancePrefs().getPreferenceContexts()[0], PREF_SAVE_ALL_EDITORS, save);
	}
	
}
