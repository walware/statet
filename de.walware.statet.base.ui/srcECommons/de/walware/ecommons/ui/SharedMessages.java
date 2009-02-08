/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui;

import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;


public class SharedMessages extends NLS {
	
	
	public static String ToggleSortAction_name;
	public static String ToggleSortAction_tooltip;
	
	public static String ToggleScrollLockAction_name;
	public static String ToggleScrollLockAction_tooltip;
	
	public static String UndoAction_name;
	public static String UndoAction_tooltip;
	public static String RedoAction_name;
	public static String RedoAction_tooltip;
	
	public static String DeleteAction_name;
	public static String DeleteAction_tooltip;
	
	public static String CutAction_name;
	public static String CutAction_tooltip;
	
	public static String CopyAction_name;
	public static String CopyAction_tooltip;
	
	public static String PasteAction_name;
	public static String PasteAction_tooltip;
	
	public static String SelectAllAction_name;
	public static String SelectAllAction_tooltip;
	
	public static String FindReplaceAction_name;
	public static String FindReplaceAction_tooltip;
	
	public static String CollectionEditing_AddItem_label;
	public static String CollectionEditing_EditItem_label;
	public static String CollectionEditing_RemoveItem_label;
	public static String CollectionEditing_DefaultItem_label;
	
	public static String Note_label;
	
	
	static {
		NLS.initializeMessages(SharedMessages.class.getName(), SharedMessages.class);
	}
	
	private static ResourceBundle fgCompatibilityBundle = ResourceBundle.getBundle(SharedMessages.class.getName());
	public static ResourceBundle getCompatibilityBundle() {
		return fgCompatibilityBundle;
	}
	
	private SharedMessages() {}
	
}
