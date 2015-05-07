/*=============================================================================#
 # Copyright (c) 2011-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.cmd.ui.launching;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String MainTab_name;
	public static String MainTab_Cmd_label;
	public static String RCmd_CmdBuild_name;
	public static String RCmd_CmdCheck_name;
	public static String RCmd_CmdInstall_name;
	public static String RCmd_CmdRd2dvi_name;
	public static String RCmd_CmdRd2txt_name;
	public static String RCmd_CmdRdconv_name;
	public static String RCmd_CmdRemove_name;
	public static String RCmd_CmdSd2Rd_name;
	public static String RCmd_CmdRoxygen_name;
	public static String RCmd_CmdSweave_name;
	public static String RCmd_CmdOther_name;
	public static String MainTab_error_MissingCMD_message;
	public static String MainTab_RunHelp_label;
	public static String MainTab_error_CannotRunHelp_message;
	public static String MainTab_error_WhileRunningHelp_message;
	public static String MainTab_Resource_PackageDir_label;
	public static String MainTab_Resource_PackageDirOrArchive_label;
	public static String MainTab_Resource_Doc_label;
	public static String MainTab_Resource_DocOrDir_label;
	public static String MainTab_Resource_Other_label;
	public static String MainTab_WorkingDir_label;
	
	public static String RCmd_LaunchDelegate_Running_label;
	public static String RCmd_LaunchDelegate_error_StartingExec;
	public static String RCmd_LaunchDelegate_error_ProcessHandle;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
