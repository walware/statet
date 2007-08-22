/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import java.util.Map;

import de.walware.eclipsecommons.preferences.AbstractPreferencesModelObject;
import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.preferences.Preference.BooleanPref;

import de.walware.statet.r.ui.RUI;


/**
 *
 */
public class REditorOptions extends AbstractPreferencesModelObject {
	// Default values see RUIPreferenceInitializer

	public static final String CONTEXT_ID = "r.editor/options"; //$NON-NLS-1$
	
	
	public static final String NODE = RUI.PLUGIN_ID + "/editor.r/options"; //$NON-NLS-1$

	public static final BooleanPref PREF_SMARTINSERT_BYDEFAULT_ENABLED = new BooleanPref(
			NODE, "smartinsert.as_default.enabled"); //$NON-NLS-1$

	public static final BooleanPref PREF_SMARTINSERT_ONPASTE_ENABLED = new BooleanPref(
			NODE, "smartinsert.on_paste.enabled"); //$NON-NLS-1$

	public static final BooleanPref PREF_SMARTINSERT_CLOSECURLY_ENABLED = new BooleanPref(
			NODE, "smartinsert.close_curlybrackets.enabled"); //$NON-NLS-1$
	public static final BooleanPref PREF_SMARTINSERT_CLOSEROUND_ENABLED = new BooleanPref(
			NODE, "smartinsert.close_roundbrackets.enabled"); //$NON-NLS-1$
	public static final BooleanPref PREF_SMARTINSERT_CLOSESQUARE_ENABLED = new BooleanPref(
			NODE, "smartinsert.close_squarebrackets.enabled"); //$NON-NLS-1$
	public static final BooleanPref PREF_SMARTINSERT_CLOSESPECIAL_ENABLED = new BooleanPref(
			NODE, "smartinsert.close_specialpercent.enabled"); //$NON-NLS-1$
	public static final BooleanPref PREF_SMARTINSERT_CLOSESTRINGS_ENABLED = new BooleanPref(
			NODE, "smartinsert.close_strings.enabled"); //$NON-NLS-1$

	public static final BooleanPref PREF_FOLDING_ASDEFAULT_ENABLED = new BooleanPref(
			NODE, "folding.enable_as_default.enabled"); //$NON-NLS-1$

	public static final BooleanPref PREF_SPELLCHECKING_ENABLED = new BooleanPref(
			NODE, "spellcheck.enabled"); //$NON-NLS-1$

	

	private boolean fIsSmartByDefaultEnabled;
	private boolean fIsSmartCurlyBracketsEnabled;
	private boolean fIsSmartRoundBracketsEnabled;
	private boolean fIsSmartSquareBracketsEnabled;
	private boolean fIsSmartSpecialPercentEnabled;
	private boolean fIsSmartStringsEnabled;
	private boolean fIsSmartPasteEnabled;
	
	
	public REditorOptions(int mode) {
		if (mode == 1) {
			installLock();
		}
		loadDefaults();
	}
	
	@Override
	public String[] getNodeQualifiers() {
		return new String[0];
	}

	@Override
	public void loadDefaults() {
		fIsSmartByDefaultEnabled = true;
		fIsSmartCurlyBracketsEnabled = true;
		fIsSmartRoundBracketsEnabled = true;
		fIsSmartSquareBracketsEnabled = true;
		fIsSmartSpecialPercentEnabled = true;
		fIsSmartStringsEnabled = true;
		fIsSmartPasteEnabled = true;
	}

	@Override
	public void load(IPreferenceAccess prefs) {
		fIsSmartByDefaultEnabled = prefs.getPreferenceValue(PREF_SMARTINSERT_BYDEFAULT_ENABLED);
		fIsSmartCurlyBracketsEnabled = prefs.getPreferenceValue(PREF_SMARTINSERT_CLOSECURLY_ENABLED);
		fIsSmartRoundBracketsEnabled = prefs.getPreferenceValue(PREF_SMARTINSERT_CLOSEROUND_ENABLED);
		fIsSmartSquareBracketsEnabled = prefs.getPreferenceValue(PREF_SMARTINSERT_CLOSESQUARE_ENABLED);
		fIsSmartSpecialPercentEnabled = prefs.getPreferenceValue(PREF_SMARTINSERT_CLOSESPECIAL_ENABLED);
		fIsSmartStringsEnabled = prefs.getPreferenceValue(PREF_SMARTINSERT_CLOSESTRINGS_ENABLED);
		fIsSmartPasteEnabled = prefs.getPreferenceValue(PREF_SMARTINSERT_ONPASTE_ENABLED);
	}

	@Override
	public Map<Preference, Object> deliverToPreferencesMap(Map<Preference, Object> map) {
		map.put(PREF_SMARTINSERT_BYDEFAULT_ENABLED, fIsSmartByDefaultEnabled);
		map.put(PREF_SMARTINSERT_CLOSECURLY_ENABLED, fIsSmartCurlyBracketsEnabled);
		map.put(PREF_SMARTINSERT_CLOSEROUND_ENABLED, fIsSmartRoundBracketsEnabled);
		map.put(PREF_SMARTINSERT_CLOSESQUARE_ENABLED, fIsSmartSquareBracketsEnabled);
		map.put(PREF_SMARTINSERT_CLOSESPECIAL_ENABLED, fIsSmartSpecialPercentEnabled);
		map.put(PREF_SMARTINSERT_CLOSESTRINGS_ENABLED, fIsSmartStringsEnabled);
		map.put(PREF_SMARTINSERT_ONPASTE_ENABLED, fIsSmartPasteEnabled);
		return map;
	}
	
	
	public boolean isSmartModeByDefaultEnabled() {
		return fIsSmartByDefaultEnabled;
	}
	public boolean isSmartPasteEnabled() {
		return fIsSmartPasteEnabled;
	}
	public boolean isSmartCurlyBracketsEnabled() {
		return fIsSmartCurlyBracketsEnabled;
	}
	public boolean isSmartRoundBracketsEnabled() {
		return fIsSmartRoundBracketsEnabled;
	}
	public boolean isSmartSquareBracketsEnabled() {
		return fIsSmartSquareBracketsEnabled;
	}
	public boolean isSmartSpecialPercentEnabled() {
		return fIsSmartSpecialPercentEnabled;
	}
	public boolean isSmartStringsEnabled() {
		return fIsSmartStringsEnabled;
	}
	
}
