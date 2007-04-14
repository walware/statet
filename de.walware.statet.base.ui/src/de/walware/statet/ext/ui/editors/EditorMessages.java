/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.editors;

import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;


public class EditorMessages extends NLS {
	

	public static String GotoMatchingBracketAction_label;
	public static String GotoMatchingBracketAction_tooltip;
	public static String GotoMatchingBracketAction_description;
	public static String GotoMatchingBracketAction_error_BracketOutsideSelectedElement;
	public static String GotoMatchingBracketAction_error_InvalidSelection;
	public static String GotoMatchingBracketAction_error_NoMatchingBracket;

	public static String ToggleCommentAction_error;

	
	private static final String BUNDLE_NAME = EditorMessages.class.getName();

	static {
		NLS.initializeMessages(BUNDLE_NAME, EditorMessages.class);
	}
	
	
	private static ResourceBundle fgCompatibilityBundle = ResourceBundle.getBundle(BUNDLE_NAME);

	public static ResourceBundle getCompatibilityBundle() {
		return fgCompatibilityBundle;
	}
}
