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

package de.walware.eclipsecommons.ui;

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
	
	
	public static String BrowseWorkspace_button_name;
	public static String BrowseFilesystem_button_name;
	public static String BrowseVariables_button_name;
	
	
	public static String CollectionEditing_AddItem_label;
	public static String CollectionEditing_EditItem_label;
	public static String CollectionEditing_RemoveItem_label;
	public static String CollectionEditing_DefaultItem_label;
	
	
	private static final String BUNDLE_NAME = SharedMessages.class.getName();
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, SharedMessages.class);
	}

	
	private static ResourceBundle fgCompatibilityBundle = ResourceBundle.getBundle(BUNDLE_NAME);

	public static ResourceBundle getCompatibilityBundle() {
		return fgCompatibilityBundle;
	}
	
}
