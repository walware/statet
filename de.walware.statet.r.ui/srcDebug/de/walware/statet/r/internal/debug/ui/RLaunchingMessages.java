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

package de.walware.statet.r.internal.debug.ui;

import org.eclipse.osgi.util.NLS;


public class RLaunchingMessages extends NLS {
	
	public static String Run_error_NoRSession_message;
	public static String Run_error_RSessionTerminated_message;
	public static String Run_error_RuntimeError_message;
	
	public static String RunAndPasteLaunch_error_Unspecific_status;
	public static String RunAndPasteLaunch_error_WhenPasting_message;
	public static String RunAndPasteLaunch_info_WriteProtected_status;
	public static String RunAndPasteLaunch_RTask_label;
	// Launch shortcuts
	public static String RLaunchPulldown_Item_label;
	public static String RSelectionLaunch_error_message;
	public static String RScriptLaunch_error_message;
	public static String RFunctionLaunch_error_message;
	public static String RCommandLaunch_error_message;
	public static String RSpecifiedLaunch_error_message;
	public static String RCodeLaunch_SubmitCode_task;
	public static String RCodeLaunch_UpdateStructure_task;
	
	// Launch configurations (dialogs/delegates)
	public static String LaunchDelegate_error_StartingExec;
	public static String LaunchDelegate_error_ProcessHandle;
	
	public static String REnv_Tab_REnvConfig_label;
	public static String REnv_Tab_title;
	public static String REnv_Tab_WorkingDir_label;
	public static String REnv_Runtime_error_CouldNotFound_message;
	public static String REnv_Runtime_error_Invalid_message;
	
	public static String RConsole_MainTab_name;
	public static String RConsole_MainTab_LaunchType_label;
	public static String RConsole_MainTab_RunHelp_label;
	public static String RConsole_MainTab_error_CannotRunHelp_message;
	public static String RConsole_MainTab_error_WhileRunningHelp_message;
	
	public static String RCmd_MainTab_name;
	public static String RCmd_MainTab_Cmd_label;
	public static String RCmd_CmdBuild_name;
	public static String RCmd_CmdCheck_name;
	public static String RCmd_CmdInstall_name;
	public static String RCmd_CmdOther_name;
	public static String RCmd_CmdRd2dvi_name;
	public static String RCmd_CmdRd2txt_name;
	public static String RCmd_CmdRdconv_name;
	public static String RCmd_CmdRemove_name;
	public static String RCmd_CmdSd2Rd_name;
	public static String RCmd_CmdSweave_name;
	public static String RCmd_MainTab_error_MissingCMD_message;
	public static String RCmd_MainTab_RunHelp_label;
	public static String RCmd_MainTab_error_CannotRunHelp_message;
	public static String RCmd_MainTab_error_WhileRunningHelp_message;
	public static String RCmd_Resource_Doc_label;
	public static String RCmd_Resource_Other_label;
	public static String RCmd_Resource_Package_label;
	public static String RCmd_LaunchDelegate_Running_label;
	
	// Console
	public static String TextConsoleConnector_error_NoConsole_message;
	public static String TextConsoleConnector_error_Other_message;
	
	public static String RErrorLineTracker_error_GetFile_message;
	public static String RErrorLineTracker_error_OpeningFile_message;
	public static String RErrorLineTracker_File_name;
	
	
	static {
		NLS.initializeMessages(RLaunchingMessages.class.getName(), RLaunchingMessages.class);
	}
	
}
