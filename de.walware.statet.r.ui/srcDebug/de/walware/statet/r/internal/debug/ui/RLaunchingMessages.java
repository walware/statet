/*******************************************************************************
 * Copyright (c) 2005-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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
	public static String RunCode_info_SyntaxError_message;
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
	public static String REnv_Tab_REnvConfig_label;
	public static String REnv_Tab_title;
	public static String REnv_Tab_WorkingDir_label;
	
	// Console
	public static String TextConsoleConnector_error_NoConsole_message;
	public static String TextConsoleConnector_error_Other_message;
	
	public static String RErrorLineTracker_error_GetFile_message;
	public static String RErrorLineTracker_error_OpeningFile_message;
	public static String RErrorLineTracker_File_name;
	
	
	static {
		NLS.initializeMessages(RLaunchingMessages.class.getName(), RLaunchingMessages.class);
	}
	private RLaunchingMessages() {}
	
}
