/*******************************************************************************
 * Copyright (c) 2005-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.editors.text.EditorsUI;

import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssistComputerRegistry;
import de.walware.ecommons.ltk.ui.sourceediting.assist.InfoHoverRegistry;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.Preference.BooleanPref;
import de.walware.ecommons.preferences.PreferencesUtil;

import de.walware.statet.nico.core.NicoPreferenceNodes;

import de.walware.statet.r.core.rhelp.IRHelpManager;
import de.walware.statet.r.internal.ui.editors.DefaultRFoldingPreferences;
import de.walware.statet.r.internal.ui.rhelp.RHelpPreferences;
import de.walware.statet.r.launching.RRunDebugPreferenceConstants;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.RUIPreferenceConstants;
import de.walware.statet.r.ui.editors.REditorBuild;
import de.walware.statet.r.ui.editors.REditorOptions;


public class RUIPreferenceInitializer extends AbstractPreferenceInitializer {
	
	
	public static final String REDITOR_NODE = RUI.PLUGIN_ID + "/editor.r/options"; //$NON-NLS-1$
	public static final String RCONSOLE_NODE = RUI.PLUGIN_ID + '/'+NicoPreferenceNodes.SCOPE_QUALIFIER+ "/editor.r/options"; // NicoPreferenceNodes.createScopeQualifier(REDITOR_NODE); //$NON-NLS-1$
	
	public static final String REDITOR_ASSIST_GROUP_ID = "r/r.editor/assist"; //$NON-NLS-1$
	public static final String RCONSOLE_ASSIST_GROUP_ID = "r/r.console/assist"; //$NON-NLS-1$
	
	public static final String REDITOR_HOVER_GROUP_ID = "r/r.editor/hover"; //$NON-NLS-1$
	
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
		
		final DefaultScope scope = new DefaultScope();
		final Map<Preference<?>, Object> map = new HashMap<Preference<?>, Object>();
		RUIPreferenceConstants.initializeDefaultValues(scope);
		
		new REditorOptions(0).deliverToPreferencesMap(map);
		
		PreferencesUtil.setPrefValue(scope, CONSOLE_SMARTINSERT_CLOSECURLY_ENABLED, false);
		PreferencesUtil.setPrefValue(scope, CONSOLE_SMARTINSERT_CLOSEROUND_ENABLED, true);
		PreferencesUtil.setPrefValue(scope, CONSOLE_SMARTINSERT_CLOSESQUARE_ENABLED, true);
		PreferencesUtil.setPrefValue(scope, CONSOLE_SMARTINSERT_CLOSESPECIAL_ENABLED, true);
		PreferencesUtil.setPrefValue(scope, CONSOLE_SMARTINSERT_CLOSESTRINGS_ENABLED, true);
		
		PreferencesUtil.setPrefValue(scope, REditorOptions.PREF_FOLDING_ENABLED, true);
		PreferencesUtil.setPrefValue(scope, REditorOptions.PREF_MARKOCCURRENCES_ENABLED, true);
		PreferencesUtil.setPrefValue(scope, REditorBuild.PROBLEMCHECKING_ENABLED_PREF, true);
		PreferencesUtil.setPrefValue(scope, REditorOptions.PREF_SPELLCHECKING_ENABLED, false);
		DefaultRFoldingPreferences.initializeDefaultValues(scope);
		
		scope.getNode(REDITOR_NODE).put(ContentAssistComputerRegistry.CIRCLING_ORDERED, "r-elements:false,templates:true,r-elements.runtime:true"); //$NON-NLS-1$
		scope.getNode(REDITOR_NODE).put(ContentAssistComputerRegistry.DEFAULT_DISABLED, "r-elements.runtime"); //$NON-NLS-1$
		scope.getNode(RCONSOLE_NODE).put(ContentAssistComputerRegistry.CIRCLING_ORDERED, "r-elements:false,templates:true"); //$NON-NLS-1$
		
		scope.getNode(REDITOR_NODE).put(InfoHoverRegistry.TYPE_SETTINGS,
				"de.walware.statet.r.contentInfoHover.RCombinedHover:true;," + //$NON-NLS-1$
				"de.walware.statet.r.contentInfoHover.RHelpHover:false;," + //$NON-NLS-1$
				"de.walware.statet.r.contentInfoHover.RDebugHover:true;M2"); //$NON-NLS-1$
		
		final IEclipsePreferences rHelp = scope.getNode(RHelpPreferences.RHELP_QUALIFIER);
		rHelp.put(RHelpPreferences.HOMEPAGE_URL_KEY, IRHelpManager.PORTABLE_DEFAULT_RENV_BROWSE_URL);
		rHelp.putBoolean(RHelpPreferences.SEARCH_REUSE_PAGE_ENABLED_KEY, true);
		rHelp.putInt(RHelpPreferences.SEARCH_PREVIEW_FRAGMENTS_MAX_KEY, 10);
		
		RRunDebugPreferenceConstants.initializeDefaultValues(scope);
		
		PreferencesUtil.setPrefValues(scope, map);
	}
	
}
