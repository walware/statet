/*=============================================================================#
 # Copyright (c) 2007-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.editors;

import static de.walware.statet.r.internal.ui.RUIPreferenceInitializer.REDITOR_NODE;

import java.util.Map;

import de.walware.ecommons.ltk.ui.sourceediting.ISmartInsertSettings;
import de.walware.ecommons.preferences.AbstractPreferencesModelObject;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.Preference.BooleanPref;
import de.walware.ecommons.preferences.Preference.EnumPref;


public class REditorOptions extends AbstractPreferencesModelObject
		implements ISmartInsertSettings {
	// Default values see RUIPreferenceInitializer
	
	public static final String GROUP_ID = "r/r.editor/options"; //$NON-NLS-1$
	
	
	public static final String SMARTINSERT_GROUP_ID = "r/r.editor/smartinsert"; //$NON-NLS-1$
	
	public static final BooleanPref SMARTINSERT_BYDEFAULT_ENABLED_PREF = new BooleanPref(
			REDITOR_NODE, "smartinsert.as_default.enabled"); //$NON-NLS-1$
	
	public static final BooleanPref SMARTINSERT_ONPASTE_ENABLED_PREF = new BooleanPref(
			REDITOR_NODE, "smartinsert.on_paste.enabled"); //$NON-NLS-1$
	
	public static final Preference<TabAction> SMARTINSERT_TAB_ACTION_PREF = new EnumPref<TabAction>(
			REDITOR_NODE, "SmartInsert.Tab.action", TabAction.class); //$NON-NLS-1$
	
	public static final BooleanPref SMARTINSERT_CLOSECURLY_ENABLED_PREF = new BooleanPref(
			REDITOR_NODE, "smartinsert.close_curlybrackets.enabled"); //$NON-NLS-1$
	public static final BooleanPref SMARTINSERT_CLOSEROUND_ENABLED_PREF = new BooleanPref(
			REDITOR_NODE, "smartinsert.close_roundbrackets.enabled"); //$NON-NLS-1$
	public static final BooleanPref SMARTINSERT_CLOSESQUARE_ENABLED_PREF = new BooleanPref(
			REDITOR_NODE, "smartinsert.close_squarebrackets.enabled"); //$NON-NLS-1$
	public static final BooleanPref SMARTINSERT_CLOSESPECIAL_ENABLED_PREF = new BooleanPref(
			REDITOR_NODE, "smartinsert.close_specialpercent.enabled"); //$NON-NLS-1$
	public static final BooleanPref SMARTINSERT_CLOSESTRINGS_ENABLED_PREF = new BooleanPref(
			REDITOR_NODE, "smartinsert.close_strings.enabled"); //$NON-NLS-1$
	
	
	public static final BooleanPref PREF_SPELLCHECKING_ENABLED = new BooleanPref(
			REDITOR_NODE, "spellcheck.enabled"); //$NON-NLS-1$
	
	// not in group
	public static final BooleanPref FOLDING_ENABLED_PREF = new BooleanPref(
			REDITOR_NODE, "folding.enabled"); //$NON-NLS-1$
	
	public static final String FOLDING_SHARED_GROUP_ID = "r/r.editor/folding.shared"; //$NON-NLS-1$
	
	public static final BooleanPref FOLDING_RESTORE_STATE_ENABLED_PREF = new BooleanPref(
			REDITOR_NODE, "Folding.RestoreState.enabled"); //$NON-NLS-1$
	
	public static final BooleanPref PREF_MARKOCCURRENCES_ENABLED = new BooleanPref(
			REDITOR_NODE, "markoccurrences.enabled"); //$NON-NLS-1$
	
	
	private boolean fIsSmartByDefaultEnabled;
	private TabAction fSmartTabAction;
	private boolean fIsSmartCurlyBracketsEnabled;
	private boolean fIsSmartRoundBracketsEnabled;
	private boolean fIsSmartSquareBracketsEnabled;
	private boolean fIsSmartSpecialPercentEnabled;
	private boolean fIsSmartStringsEnabled;
	private boolean fIsSmartPasteEnabled;
	
	
	public REditorOptions(final int mode) {
		if (mode >= 1) {
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
		fSmartTabAction = TabAction.INSERT_INDENT_LEVEL;
		fIsSmartCurlyBracketsEnabled = true;
		fIsSmartRoundBracketsEnabled = true;
		fIsSmartSquareBracketsEnabled = true;
		fIsSmartSpecialPercentEnabled = true;
		fIsSmartStringsEnabled = true;
		fIsSmartPasteEnabled = true;
	}
	
	@Override
	public void load(final IPreferenceAccess prefs) {
		fIsSmartByDefaultEnabled = prefs.getPreferenceValue(SMARTINSERT_BYDEFAULT_ENABLED_PREF);
		fSmartTabAction = prefs.getPreferenceValue(SMARTINSERT_TAB_ACTION_PREF);
		fIsSmartCurlyBracketsEnabled = prefs.getPreferenceValue(SMARTINSERT_CLOSECURLY_ENABLED_PREF);
		fIsSmartRoundBracketsEnabled = prefs.getPreferenceValue(SMARTINSERT_CLOSEROUND_ENABLED_PREF);
		fIsSmartSquareBracketsEnabled = prefs.getPreferenceValue(SMARTINSERT_CLOSESQUARE_ENABLED_PREF);
		fIsSmartSpecialPercentEnabled = prefs.getPreferenceValue(SMARTINSERT_CLOSESPECIAL_ENABLED_PREF);
		fIsSmartStringsEnabled = prefs.getPreferenceValue(SMARTINSERT_CLOSESTRINGS_ENABLED_PREF);
		fIsSmartPasteEnabled = prefs.getPreferenceValue(SMARTINSERT_ONPASTE_ENABLED_PREF);
	}
	
	@Override
	public Map<Preference<?>, Object> deliverToPreferencesMap(final Map<Preference<?>, Object> map) {
		map.put(SMARTINSERT_BYDEFAULT_ENABLED_PREF, fIsSmartByDefaultEnabled);
		map.put(SMARTINSERT_TAB_ACTION_PREF, fSmartTabAction);
		map.put(SMARTINSERT_CLOSECURLY_ENABLED_PREF, fIsSmartCurlyBracketsEnabled);
		map.put(SMARTINSERT_CLOSEROUND_ENABLED_PREF, fIsSmartRoundBracketsEnabled);
		map.put(SMARTINSERT_CLOSESQUARE_ENABLED_PREF, fIsSmartSquareBracketsEnabled);
		map.put(SMARTINSERT_CLOSESPECIAL_ENABLED_PREF, fIsSmartSpecialPercentEnabled);
		map.put(SMARTINSERT_CLOSESTRINGS_ENABLED_PREF, fIsSmartStringsEnabled);
		map.put(SMARTINSERT_ONPASTE_ENABLED_PREF, fIsSmartPasteEnabled);
		return map;
	}
	
	
	public boolean isSmartModeByDefaultEnabled() {
		return fIsSmartByDefaultEnabled;
	}
	public TabAction getSmartTabAction() {
		return fSmartTabAction;
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
