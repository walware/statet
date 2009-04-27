/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
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
	
	public static String RunCode_error_NoRSession_message;
	public static String RunCode_error_RSessionTerminated_message;
	public static String RunCode_error_WhenAnalyzingAndCollecting_message;
	public static String RunCode_TextSelection_label;
	public static String RunCode_OtherSelection_label;
	public static String RunCode_GotoConsole_affix;
	public static String RunCode_error_RuntimeError_message;
	public static String RunCode_info_NotSupported_message;
	public static String RunCode_error_NoConnector_message;
	
	public static String RunCodeAndPasteOutput_error_Unspecific_status;
	public static String RunCodeAndPasteOutput_error_WhenPasting_message;
	public static String RunCodeAndPasteOutput_info_WriteProtected_status;
	public static String RunCodeAndPasteOutput_RTask_label;
	// Launch shortcuts
	public static String RSelectionLaunch_error_message;
	public static String RScriptLaunch_error_message;
	public static String RFunctionLaunch_error_message;
	public static String RCommandLaunch_error_message;
	public static String RSpecifiedLaunch_error_message;
	public static String RCodeLaunch_SubmitCode_task;
	public static String RCodeLaunch_UpdateStructure_task;
	
	// Launch configurations (dialogs/delegates)
	public static String RMI_status_RegistryAlreadyStarted_message;
	public static String RMI_status_RegistryStartFailed_message;
	public static String RMI_status_RegistryStartFailedWithExitValue_message;
	public static String RMI_status_RegistryStopFailedNotFound_message;
	
	public static String LaunchDelegate_error_StartingExec;
	public static String LaunchDelegate_error_ProcessHandle;
	public static String LaunchDelegate_error_InvalidUnsupportedConsoleEncoding_message;
	public static String RJLaunchDelegate_StartR_subtask;
	public static String RJLaunchDelegate_WaitForR_subtask;
	public static String RJLaunchDelegate_error_MissingAddress_message;
	public static String RJLaunchDelegate_error_InvalidAddress_message;
	
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
	public static String RConsole_MainTab_ConsoleOptions_label;
	public static String RConsole_MainTab_ConsoleOptions_Pin_label;
	
	public static String RConsole_OptionsTab_name;
	
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
	public static String RCmd_CmdRoxygen_name;
	public static String RCmd_CmdSweave_name;
	public static String RCmd_MainTab_error_MissingCMD_message;
	public static String RCmd_MainTab_RunHelp_label;
	public static String RCmd_MainTab_error_CannotRunHelp_message;
	public static String RCmd_MainTab_error_WhileRunningHelp_message;
	public static String RCmd_Resource_Doc_label;
	public static String RCmd_Resource_Other_label;
	public static String RCmd_Resource_Package_label;
	public static String RCmd_LaunchDelegate_Running_label;
	
	public static String JavaJRE_RCompatibility_error_DifferentBits_message;
	public static String JavaJRE_Tab_VMArguments_label;
	
	// Console
	public static String TextConsoleConnector_error_NoConsole_message;
	public static String TextConsoleConnector_error_Other_message;
	
	public static String RErrorLineTracker_error_GetFile_message;
	public static String RErrorLineTracker_error_OpeningFile_message;
	public static String RErrorLineTracker_File_name;
	
	public static String RRemoteConsoleSelectionDialog_title;
	public static String RRemoteConsoleSelectionDialog_message;
	public static String RRemoteConsoleSelectionDialog_Hostname_label;
	public static String RRemoteConsoleSelectionDialog_Update_label;
	public static String RRemoteConsoleSelectionDialog_Table_UserOrEngine_label;
	public static String RRemoteConsoleSelectionDialog_Table_Host_label;
	public static String RRemoteConsoleSelectionDialog_task_Connecting_message;
	public static String RRemoteConsoleSelectionDialog_task_Gathering_message;
	public static String RRemoteConsoleSelectionDialog_task_Resolving_message;
	public static String RRemoteConsoleSelectionDialog_info_ListRestored_message;
	public static String RRemoteConsoleSelectionDialog_error_ConnectionFailed_message;
	
	
	static {
		NLS.initializeMessages(RLaunchingMessages.class.getName(), RLaunchingMessages.class);
	}
	private RLaunchingMessages() {}
	
}
