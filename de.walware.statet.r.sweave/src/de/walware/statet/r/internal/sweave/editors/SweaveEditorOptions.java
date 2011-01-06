/*******************************************************************************
 * Copyright (c) 2008-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.editors;

import de.walware.ecommons.preferences.Preference.BooleanPref;

import de.walware.statet.r.internal.sweave.SweavePlugin;


/**
 * 
 */
public class SweaveEditorOptions {
	
	
	public static final String GROUP_ID = "editor.sweave/options"; //$NON-NLS-1$
	
	public static final String SWEAVEEDITOR_NODE = SweavePlugin.PLUGIN_ID + "/editor.sweave/options"; //$NON-NLS-1$
	
	
	public static final BooleanPref PREF_SPELLCHECKING_ENABLED = new BooleanPref(
			SWEAVEEDITOR_NODE, "spellcheck.enabled"); //$NON-NLS-1$
	
}
