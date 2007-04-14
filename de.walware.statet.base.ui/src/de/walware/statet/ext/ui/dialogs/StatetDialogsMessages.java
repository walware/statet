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

package de.walware.statet.ext.ui.dialogs;

import org.eclipse.osgi.util.NLS;


public class StatetDialogsMessages extends NLS {
	
	private static final String BUNDLE_NAME = StatetDialogsMessages.class.getName();


	public static String Resources_File;

	
	public static String ExpandAllAction_label;
	public static String ExpandAllAction_description;
	public static String ExpandAllAction_tooltip;

	public static String CollapseAllAction_label;
	public static String CollapseAllAction_description;
	public static String CollapseAllAction_tooltip;

	public static String FilterFavouredContainersAction_label;
	public static String FilterFavouredContainersAction_description;
	public static String FilterFavouredContainersAction_tooltip;
	
	
	public static String ContainerSelectionControl_label_EnterOrSelectFolder;
	public static String ContainerSelectionControl_label_SelectFolder;

	public static String ContainerSelectionControl_error_FolderEmpty;
	public static String ContainerSelectionControl_error_ProjectNotExists;
	public static String ContainerSelectionControl_error_PathOccupied;

	
	static {
		NLS.initializeMessages(BUNDLE_NAME, StatetDialogsMessages.class);
	}




	
}
