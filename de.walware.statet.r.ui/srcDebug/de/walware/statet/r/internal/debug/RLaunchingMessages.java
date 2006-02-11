/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug;

import org.eclipse.osgi.util.NLS;


public class RLaunchingMessages extends NLS {
	
	private static final String BUNDLE_NAME = RLaunchingMessages.class.getName();

	public static String RLaunchPulldown_Item_label;

	public static String RLaunch_error_description;

	public static String RSelectionLaunch_error_message;
	public static String RScriptLaunch_error_message;

	
	public static String RConsoleLaunchDelegate_WorkbenchClosing_title;
	public static String RConsoleLaunchDelegate_WorkbenchClosing_message;
	public static String RConsoleLaunchDelegate_WorkbenchClosing_button_Continue;
	public static String RConsoleLaunchDelegate_WorkbenchClosing_button_Cancel;
	public static String RConsoleLaunchDelegate_Running;
	public static String RConsoleLaunchDelegate_error_ProcessHandle;

	
	public static String Tab_error_ReadingConfiguration_message;

	public static String MainTab_name;
	public static String MainTab_Location_Browse_Workspace;
	public static String MainTab_Location_Browse_FileSystem;
	public static String MainTab_Location_Variables;
	public static String MainTab_WorkingDirectory;
	public static String MainTab_WorkingDirectory_Browse_Workspace;
	public static String MainTab_WorkingDirectory_Browse_FileSystem;
	public static String MainTab_WorkingDirectory_Variables;
	public static String MainTab_SelectWorkingDirectory_message;
	public static String MainTab_Arguments;
	public static String MainTab_Arguments_Variables;
	public static String MainTab_Arguments_Note;

	public static String RCmdMainTab_Location;
	public static String RCmdMainTab_ROptions;
	public static String RCmdMainTab_SelectRExecutable;

	public static String MainTab_error_LocationCannotBeEmpty_message;
	public static String MainTab_error_LocationDoesNotExist_message;
	public static String MainTab_error_LocationSpecifiedIsNotAFile_message;
	public static String RCmdMainTab_info_SpecifyLocation_message;
	public static String MainTab_error_WorkingDirectoryDoesNotExistOrIsInvalid_message;
	public static String MainTab_error_WorkingDirectoryNotADirectory;

	
	public static String ROptionsSelectionDialog_title;
	public static String ROptionsSelectionDialog_message;
	public static String SelectionDialog_Argument;
	public static String SelectionDialog_Description;



	
	static {
		NLS.initializeMessages(BUNDLE_NAME, RLaunchingMessages.class);
	}
	
}
