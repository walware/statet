/*=============================================================================#
 # Copyright (c) 2005-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.base.ui;

import de.walware.ecommons.text.ui.settings.AssistPreferences;
import de.walware.ecommons.text.ui.settings.DecorationPreferences;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * Common Preference constants used in Statet UI preference store. 
 */
public interface IStatetUIPreferenceConstants {
	
	
	public final static String CAT_EDITOR_OPTIONS_QUALIFIER = StatetUIPlugin.PLUGIN_ID + "/editors/options"; //$NON-NLS-1$
	
	
	public final static DecorationPreferences EDITING_DECO_PREFERENCES = new DecorationPreferences(StatetUIPlugin.PLUGIN_ID);
	
	public final static AssistPreferences EDITING_ASSIST_PREFERENCES = new AssistPreferences(IStatetUIPreferenceConstants.CAT_EDITOR_OPTIONS_QUALIFIER, "statet/editors/assist.ui");
	
	
//	/**
//	 * A named preference that controls whether the ouline page should sort its elements.
//	 * <p>
//	 * Value is of type <code>Boolean</code>.
//	 */
//	public static final String EDITOROUTLINE_SORT = ID+"editor_outline.sort";
//	/**
//	 * A named preference that controls whether the ouline page links its selection to the active editor.
//	 * <p>
//	 * Value is of type <code>Boolean</code>.
//	 */
//	public static final String EDITOROUTLINE_LINKWITHEDITOR = ID+"editor_outline.link_with_editor";
	
}
