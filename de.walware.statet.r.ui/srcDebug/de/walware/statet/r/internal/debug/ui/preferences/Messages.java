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

package de.walware.statet.r.internal.debug.ui.preferences;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	

	public static String RInteraction_description;
	public static String RInteraction_RConnector;
	public static String RInteraction_RConnector_Description_label;

	public static String REnv_REnvList_label;
	public static String REnv_LocationColumn_name;
	public static String REnv_NameColumn_name;
	public static String REnv_Copy_label;
	public static String REnv_warning_NoDefaultConfiguration_message;
	public static String REnv_Detail_AddDialog_title;
	public static String REnv_Detail_Edit_Dialog_title;
	public static String REnv_Detail_Name_label;
	public static String REnv_Detail_Name_error_Duplicate_message;
	public static String REnv_Detail_Name_error_InvalidChar_message;
	public static String REnv_Detail_Name_error_Missing_message;
	public static String REnv_Detail_Location_label;
	public static String REnv_Detail_Location_error_NoRHome_message;
	public static String REnv_Detail_FindAuto_label;
	public static String REnv_Detail_FindAuto_Failed_message;

	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	
}
