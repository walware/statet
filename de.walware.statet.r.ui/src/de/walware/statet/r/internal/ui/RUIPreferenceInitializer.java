/*******************************************************************************
 * Copyright (c) 2005-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.editors.text.EditorsUI;

import de.walware.ecommons.ltk.ui.sourceediting.ContentAssistComputerRegistry;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.Preference.BooleanPref;

import de.walware.statet.nico.core.NicoPreferenceNodes;

import de.walware.statet.r.internal.debug.ui.RDebugPreferenceConstants;
import de.walware.statet.r.internal.ui.editors.DefaultRFoldingPreferences;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.RUIPreferenceConstants;
import de.walware.statet.r.ui.editors.REditorOptions;


public class RUIPreferenceInitializer extends AbstractPreferenceInitializer {
	
	
	public static final String REDITOR_NODE = RUI.PLUGIN_ID + "/editor.r/options"; //$NON-NLS-1$
	public static final String RCONSOLE_NODE = RUI.PLUGIN_ID + '/'+NicoPreferenceNodes.SCOPE_QUALIFIER+ "/editor.r/options"; // NicoPreferenceNodes.createScopeQualifier(REDITOR_NODE); //$NON-NLS-1$
	
	public static final String REDITOR_ASSIST_GROUP_ID = "r/r.editor/assist";
	public static final String RCONSOLE_ASSIST_GROUP_ID = "r/r.console/assist";
	
	public static final BooleanPref CONSOLE_SMARTINSERT_CLOSECURLY_ENABLED = new BooleanPref(
			RCONSOLE_NODE, "smartinsert.close_curlybrackets.enabled"); //$NON-NLS-1$
	public static final BooleanPref CONSOLE_SMARTINSERT_CLOSEROUND_ENABLED = new BooleanPref(
			RCONSOLE_NODE, "smartinsert.close_roundbrackets.enabled"); //$NON-NLS-1$
	public static final BooleanPref CONSOLE_SMARTINSERT_CLOSESQUARE_ENABLED = new BooleanPref(
			RCONSOLE_NODE, "smartinsert.close_squarebrackets.enabled"); //$NON-NLS-1$
	public static final BooleanPref CONSOLE_SMARTINSERT_CLOSESPECIAL_ENABLED = new BooleanPref(
			RCONSOLE_NODE, "smartinsert.close_specialpercent.enabled"); //$NON-NLS-1$
	public static final BooleanPref CONSOLE_SMARTINSERT_CLOSESTRINGS_ENABLED = new BooleanPref(
			RCONSOLE_NODE, "smartinsert.close_strings.enabled"); //$NON-NLS-1$
	
	
	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = RUIPlugin.getDefault().getPreferenceStore();
		EditorsUI.useAnnotationsPreferencePage(store);
		EditorsUI.useQuickDiffPreferencePage(store);
		RUIPreferenceConstants.initializeDefaultValues(store);
		
		final DefaultScope defaultScope = new DefaultScope();
		final Map<Preference, Object> defaults = new HashMap<Preference, Object>();
		new REditorOptions(0).deliverToPreferencesMap(defaults);
		PreferencesUtil.setPrefValues(defaultScope, defaults);
		
		PreferencesUtil.setPrefValue(defaultScope, CONSOLE_SMARTINSERT_CLOSECURLY_ENABLED, false);
		PreferencesUtil.setPrefValue(defaultScope, CONSOLE_SMARTINSERT_CLOSEROUND_ENABLED, true);
		PreferencesUtil.setPrefValue(defaultScope, CONSOLE_SMARTINSERT_CLOSESQUARE_ENABLED, true);
		PreferencesUtil.setPrefValue(defaultScope, CONSOLE_SMARTINSERT_CLOSESPECIAL_ENABLED, true);
		PreferencesUtil.setPrefValue(defaultScope, CONSOLE_SMARTINSERT_CLOSESTRINGS_ENABLED, true);
		
		PreferencesUtil.setPrefValue(defaultScope, REditorOptions.PREF_PROBLEMCHECKING_ENABLED, true);
		PreferencesUtil.setPrefValue(defaultScope, REditorOptions.PREF_SPELLCHECKING_ENABLED, false);
		PreferencesUtil.setPrefValue(defaultScope, REditorOptions.PREF_MARKOCCURRENCES_ENABLED, true);
		PreferencesUtil.setPrefValue(defaultScope, REditorOptions.PREF_FOLDING_ENABLED, true);
		DefaultRFoldingPreferences.initializeDefaultValues(defaultScope);
		
		defaultScope.getNode(REDITOR_NODE).put(ContentAssistComputerRegistry.CIRCLING_ORDERED, "r-elements:false,templates:true,r-elements.runtime:true"); //$NON-NLS-1$
		defaultScope.getNode(REDITOR_NODE).put(ContentAssistComputerRegistry.DEFAULT_DISABLED, "r-elements.runtime"); //$NON-NLS-1$
		defaultScope.getNode(RCONSOLE_NODE).put(ContentAssistComputerRegistry.CIRCLING_ORDERED, "r-elements:false,templates:true"); //$NON-NLS-1$
		
		RDebugPreferenceConstants.initializeDefaultValues(defaultScope);
	}
	
}
