/*******************************************************************************
 * Copyright (c) 2005-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.preferences;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String RInteraction_description;
	public static String RInteraction_RConnector;
	public static String RInteraction_RConnector_Description_label;
	
	public static String RInteraction_FileCommands_label;
	
	public static String REnv_REnvList_label;
	public static String REnv_LocationColumn_name;
	public static String REnv_NameColumn_name;
	public static String REnv_warning_NoDefaultConfiguration_message;
	public static String REnv_Detail_AddDialog_title;
	public static String REnv_Detail_Edit_Dialog_title;
	public static String REnv_Detail_Name_label;
	public static String REnv_Detail_Name_error_Duplicate_message;
	public static String REnv_Detail_Name_error_InvalidChar_message;
	public static String REnv_Detail_Name_error_Missing_message;
	public static String REnv_Detail_Location_label;
	public static String REnv_Detail_Location_error_NoRHome_message;
	public static String REnv_Detail_Location_FindAuto_label;
	public static String REnv_Detail_Location_FindAuto_Failed_message;
	public static String REnv_Detail_Bits_label;
	public static String REnv_Detail_Libraries_label;
	public static String REnv_Detail_LibraryLocation_label;
	public static String REnv_Detail_DetectSettings_label;
	public static String REnv_Detail_DetectSettings_task;
	public static String REnv_Detail_DetectSettings_error_message;
	public static String REnv_Detail_DetectSettings_error_Unexpected_message;
	public static String REnv_SystemRHome_name;
	public static String REnv_error_Saving_message;
	
	public static String RIntegrationExt_description;
	public static String RIntegrationExt_LocalRMI_label;
	public static String RIntegrationExt_LocalRMI_RegistryAction_Start_label;
	public static String RIntegrationExt_LocalRMI_RegistryAction_Stop_label;
	public static String RIntegrationExt_LocalRMI_RegistryAutostart_label;
	public static String RIntegrationExt_LocalRMI_RegistryPort_error_Invalid_message;
	public static String RIntegrationExt_LocalRMI_RegistryPort_label;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
