/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui.sourceeditors;

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
	
	public static String FoldingMenu_label;
	public static String CodeFolding_Enable_label;
	public static String CodeFolding_Enable_mnemonic;
	
	
	static {
		NLS.initializeMessages(EditorMessages.class.getName(), EditorMessages.class);
	}
	
	private static ResourceBundle fgCompatibilityBundle = ResourceBundle.getBundle(EditorMessages.class.getName());
	public static ResourceBundle getCompatibilityBundle() {
		return fgCompatibilityBundle;
	}
	
	private EditorMessages() {}
	
}
