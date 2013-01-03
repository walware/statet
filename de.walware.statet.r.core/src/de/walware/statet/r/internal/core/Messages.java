/*******************************************************************************
 * Copyright (c) 2005-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String Builder_error_OnStartup_message;
	public static String Builder_error_MultipleErrors_message;
	public static String Builder_error_UnsupportedEncoding_message;
	public static String Builder_error_IOReadingFile_message;
	
	public static String REnvConfiguration_DefaultLib_label;
	public static String REnvConfiguration_SiteLibs_label;
	public static String REnvConfiguration_OtherLibs_label;
	public static String REnvConfiguration_UserLibs_label;
	public static String REnvConfiguration_Validation_error_Removed_message;
	public static String REnvConfiguration_Validation_error_InvalidRHome_message;
	
	public static String REnvManager_status_NotAny_label;
	public static String REnvManager_status_NoDefault_label;
	public static String REnvManager_error_Dispose_message;
	public static String REnvManager_error_Saving_message;
	public static String REnvManager_error_Accessing_message;
	
	public static String RProject_ConfigureTask_label;
	
	public static String RPkgName_Validation_error_Empty_message;
	public static String RPkgName_Validation_error_InvalidFirstChar_message;
	public static String RPkgName_Validation_error_InvalidChar_message;
	public static String RPkgName_Validation_error_InvalidNoAscii_message;
	public static String RPkgName_Validation_error_InvalidDotAtEnd_message;
	public static String RPkgName_Validation_error_InvalidSingleChar_message;
	public static String RPkgName_Validation_error_IncompleteTranslation_message;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
