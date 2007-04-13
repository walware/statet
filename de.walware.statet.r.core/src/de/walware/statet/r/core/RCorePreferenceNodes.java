/*******************************************************************************
 * Copyright (c) 2007 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core;


/**
 * Preference Nodes for the preferences of 'StatET R Core' plug-in
 */
public class RCorePreferenceNodes {
	
	
	public static final String CAT_R_CODESTYLE_QUALIFIER = RCore.PLUGIN_ID + "/codestyle/r";
	
	/**
	 * Qualifier of node with preferences required to show R code corresponding the style settings.
	 * 
	 * It is a subset of settings in {@link #CAT_R_CODESTYLE_QUALIFIER},
	 * compatible with Eclipse TextEditor {@link org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants}.
	 */
	public static final String CAT_R_CODESTYLE_PRESENTATION_QUALIFIER = CAT_R_CODESTYLE_QUALIFIER + "/presentation"; 

}
