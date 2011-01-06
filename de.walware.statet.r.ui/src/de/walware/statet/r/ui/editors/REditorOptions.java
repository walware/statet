/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import java.util.Map;

import de.walware.ecommons.preferences.AbstractPreferencesModelObject;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.Preference.BooleanPref;

import de.walware.statet.r.internal.ui.RUIPreferenceInitializer;


/**
 * 
 */
public class REditorOptions extends AbstractPreferencesModelObject {
	// Default values see RUIPreferenceInitializer
	
	public static final String GROUP_ID = "r/r.editor/options"; //$NON-NLS-1$
	
	
	public static final BooleanPref PREF_SMARTINSERT_BYDEFAULT_ENABLED = new BooleanPref(
			RUIPreferenceInitializer.REDITOR_NODE, "smartinsert.as_default.enabled"); //$NON-NLS-1$
	
	public static final BooleanPref PREF_SMARTINSERT_ONPASTE_ENABLED = new BooleanPref(
			RUIPreferenceInitializer.REDITOR_NODE, "smartinsert.on_paste.enabled"); //$NON-NLS-1$
	
	public static final BooleanPref PREF_SMARTINSERT_CLOSECURLY_ENABLED = new BooleanPref(
			RUIPreferenceInitializer.REDITOR_NODE, "smartinsert.close_curlybrackets.enabled"); //$NON-NLS-1$
	public static final BooleanPref PREF_SMARTINSERT_CLOSEROUND_ENABLED = new BooleanPref(
			RUIPreferenceInitializer.REDITOR_NODE, "smartinsert.close_roundbrackets.enabled"); //$NON-NLS-1$
	public static final BooleanPref PREF_SMARTINSERT_CLOSESQUARE_ENABLED = new BooleanPref(
			RUIPreferenceInitializer.REDITOR_NODE, "smartinsert.close_squarebrackets.enabled"); //$NON-NLS-1$
	public static final BooleanPref PREF_SMARTINSERT_CLOSESPECIAL_ENABLED = new BooleanPref(
			RUIPreferenceInitializer.REDITOR_NODE, "smartinsert.close_specialpercent.enabled"); //$NON-NLS-1$
	public static final BooleanPref PREF_SMARTINSERT_CLOSESTRINGS_ENABLED = new BooleanPref(
			RUIPreferenceInitializer.REDITOR_NODE, "smartinsert.close_strings.enabled"); //$NON-NLS-1$
	
	
	public static final BooleanPref PREF_PROBLEMCHECKING_ENABLED = new BooleanPref(
			RUIPreferenceInitializer.REDITOR_NODE, "problemchecking.enabled"); //$NON-NLS-1$
	
	public static final BooleanPref PREF_SPELLCHECKING_ENABLED = new BooleanPref(
			RUIPreferenceInitializer.REDITOR_NODE, "spellcheck.enabled"); //$NON-NLS-1$
	
	// not in group
	public static final BooleanPref PREF_FOLDING_ENABLED = new BooleanPref(
			RUIPreferenceInitializer.REDITOR_NODE, "folding.enabled"); //$NON-NLS-1$
	
	public static final BooleanPref PREF_MARKOCCURRENCES_ENABLED = new BooleanPref(
			RUIPreferenceInitializer.REDITOR_NODE, "markoccurrences.enabled"); //$NON-NLS-1$
	
	
	private boolean fIsSmartByDefaultEnabled;
	private boolean fIsSmartCurlyBracketsEnabled;
	private boolean fIsSmartRoundBracketsEnabled;
	private boolean fIsSmartSquareBracketsEnabled;
	private boolean fIsSmartSpecialPercentEnabled;
	private boolean fIsSmartStringsEnabled;
	private boolean fIsSmartPasteEnabled;
	
	
	public REditorOptions(final int mode) {
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
	public void load(final IPreferenceAccess prefs) {
		fIsSmartByDefaultEnabled = prefs.getPreferenceValue(PREF_SMARTINSERT_BYDEFAULT_ENABLED);
		fIsSmartCurlyBracketsEnabled = prefs.getPreferenceValue(PREF_SMARTINSERT_CLOSECURLY_ENABLED);
		fIsSmartRoundBracketsEnabled = prefs.getPreferenceValue(PREF_SMARTINSERT_CLOSEROUND_ENABLED);
		fIsSmartSquareBracketsEnabled = prefs.getPreferenceValue(PREF_SMARTINSERT_CLOSESQUARE_ENABLED);
		fIsSmartSpecialPercentEnabled = prefs.getPreferenceValue(PREF_SMARTINSERT_CLOSESPECIAL_ENABLED);
		fIsSmartStringsEnabled = prefs.getPreferenceValue(PREF_SMARTINSERT_CLOSESTRINGS_ENABLED);
		fIsSmartPasteEnabled = prefs.getPreferenceValue(PREF_SMARTINSERT_ONPASTE_ENABLED);
	}
	
	@Override
	public Map<Preference, Object> deliverToPreferencesMap(final Map<Preference, Object> map) {
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
