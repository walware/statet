/*=============================================================================#
 # Copyright (c) 2008-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.core.runtime.preferences.IScopeContext;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.core.Preference.BooleanPref;
import de.walware.ecommons.preferences.core.Preference.IntPref;

import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.editors.RDefaultFoldingProvider;


/**
 * Preferences for {@link RDefaultFoldingProvider}
 */
public class DefaultRFoldingPreferences {
	
	public static final String GROUP_ID = "r.editor/folding/default"; //$NON-NLS-1$
	
	public static final String NODE = RUI.PLUGIN_ID + "/editor.r/folding.default"; //$NON-NLS-1$
	
	public static final BooleanPref PREF_OTHERBLOCKS_ENABLED = new BooleanPref(
			DefaultRFoldingPreferences.NODE, "other_blocks.enabled"); //$NON-NLS-1$
	public static final IntPref PREF_MINLINES_NUM = new IntPref(
			DefaultRFoldingPreferences.NODE, "min_lines.num"); //$NON-NLS-1$
	
	public static final BooleanPref PREF_ROXYGEN_ENABLED = new BooleanPref(
			DefaultRFoldingPreferences.NODE, "roxygen.enabled"); //$NON-NLS-1$
	public static final IntPref PREF_ROXYGEN_MINLINES_NUM = new IntPref(
			DefaultRFoldingPreferences.NODE, "roxygen.min_lines.num"); //$NON-NLS-1$
	public static final BooleanPref PREF_ROXYGEN_COLLAPSE_INITIALLY_ENABLED = new BooleanPref(
			DefaultRFoldingPreferences.NODE, "roxygen.collapse_initially.enabled"); //$NON-NLS-1$
	
	
	/**
	 * Initializes the default values.
	 */
	public static void initializeDefaultValues(final IScopeContext context) {
		PreferencesUtil.setPrefValue(context, DefaultRFoldingPreferences.PREF_MINLINES_NUM, 4);
		PreferencesUtil.setPrefValue(context, DefaultRFoldingPreferences.PREF_OTHERBLOCKS_ENABLED, false);
		PreferencesUtil.setPrefValue(context, DefaultRFoldingPreferences.PREF_ROXYGEN_ENABLED, true);
		PreferencesUtil.setPrefValue(context, DefaultRFoldingPreferences.PREF_ROXYGEN_MINLINES_NUM, 2);
		PreferencesUtil.setPrefValue(context, DefaultRFoldingPreferences.PREF_ROXYGEN_COLLAPSE_INITIALLY_ENABLED, false);
	}
	
}
