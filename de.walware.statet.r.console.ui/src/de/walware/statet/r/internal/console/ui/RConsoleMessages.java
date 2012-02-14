/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.console.ui;

import org.eclipse.osgi.util.NLS;


public class RConsoleMessages extends NLS {
	
	
	public static String RConsole_MainTab_name;
	public static String RConsole_MainTab_LaunchType_label;
	public static String RConsole_MainTab_RunHelp_label;
	public static String RConsole_MainTab_error_CannotRunHelp_message;
	public static String RConsole_MainTab_error_WhileRunningHelp_message;
	public static String RConsole_MainTab_WorkingDir_label;
	public static String RConsole_MainTab_ConsoleOptions_label;
	public static String RConsole_MainTab_ConsoleOptions_Pin_label;
	
	public static String RConsole_OptionsTab_name;
	
	public static String JavaJRE_RCompatibility_error_DifferentBits_message;
	public static String JavaJRE_Tab_VMArguments_label;
	
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
	
	public static String LaunchDelegate_CheckingRegistry_subtask;
	public static String LaunchDelegate_StartREngine_subtask;
	public static String LaunchDelegate_WaitForR_subtask;
	public static String LaunchDelegate_error_MissingAddress_message;
	public static String LaunchDelegate_error_InvalidAddress_message;
	public static String LaunchDelegate_error_InvalidUnsupportedConsoleEncoding_message;
	
	public static String AdjustWidth_label;
	public static String AdjustWidth_mnemonic;
	public static String AdjustWidth_task;
	
	public static String ChangeWorkingDir_Task_label;
	public static String ChangeWorkingDir_Action_label;
	public static String ChangeWorkingDir_SelectDialog_message;
	public static String ChangeWorkingDir_SelectDialog_title;
	public static String ChangeWorkingDir_Resource_label;
	public static String ChangeWorkingDir_error_ResolvingFailed_message;
	
	public static String REnvIndex_Check_task;
	public static String REnvIndex_Check_NoIndex_message;
	public static String REnvIndex_Check_Changed_singular_message;
	public static String REnvIndex_Check_Changed_plural_message;
	public static String REnvIndex_Check_error_message;
	public static String REnvIndex_CheckDialog_title;
	public static String REnvIndex_CheckDialog_Remember_label;
	public static String REnvIndex_CheckDialog_RememberGlobally_label;
	public static String REnvIndex_CheckDialog_RememberSession_label;
	
	public static String REnvIndex_Update_task;
	public static String REnvIndex_Update_Started_message;
	
	
	static {
		NLS.initializeMessages(RConsoleMessages.class.getName(), RConsoleMessages.class);
	}
	private RConsoleMessages() {}
	
}
