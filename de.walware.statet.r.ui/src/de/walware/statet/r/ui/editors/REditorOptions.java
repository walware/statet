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

import de.walware.eclipsecommons.preferences.Preference.BooleanPref;

import de.walware.statet.r.ui.RUI;


/**
 *
 */
public class REditorOptions {
	// Default values see RUIPreferenceInitializer

	public static final String CONTEXT_ID = "r.editor/options";
	
	
	private static final String NODE = RUI.PLUGIN_ID + "/editor.r/options"; //$NON-NLS-1$
	public static final BooleanPref PREF_SMARTINSERT_ASDEFAULT = new BooleanPref(
			NODE, "smartinsert.as_default.enabled"); //$NON-NLS-1$

	public static final BooleanPref PREF_SPELLCHECKING_ENABLED = new BooleanPref(
			NODE, "spellcheck.enabled"); //$NON-NLS-1$

}
