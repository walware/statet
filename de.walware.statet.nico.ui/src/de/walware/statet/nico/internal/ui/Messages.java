/*=============================================================================#
 # Copyright (c) 2006-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.ui;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String LoadSaveHistoryPage_File_label;
	public static String LoadSaveHistoryPage_Encoding_label;
	public static String LoadHistoryPage_title;
	public static String LoadHistoryPage_description;
	public static String SaveHistoryPage_title;
	public static String SaveHistoryPage_description;
	public static String SaveHistoryPage_Options_label;
	public static String SaveHistoryPage_AppendToFile_label;
	public static String SaveHistoryPage_OverwriteExisting_label;
	
	public static String FilterEmptyAction_name;
	public static String FilterEmptyAction_tooltip;
	public static String HistorySearch_Pattern_tooltip;
	
	public static String Console_SubmitButton_label;
	public static String Console_error_UnexpectedException_message;
	
	public static String CancelAction_name;
	public static String CancelAction_tooltip;
	public static String ShowToolDescription_name;
	public static String ShowToolDescription_tooltip;
	public static String ShowProgress_name;
	public static String ShowProgress_tooltip;
	
	public static String TerminateToolAction_error_message;
	public static String TerminatingMonitor_title;
	public static String TerminatingMonitor_message;
	public static String TerminatingMonitor_CancelButton_label;
	public static String TerminatingMonitor_ForceButton_label;
	public static String TerminatingMonitor_Force_error_message;
	public static String TerminatingMonitor_WaitButton_label;
	
	public static String Tracking_Name_label;
	public static String Tracking_Name_error_Missing_message;
	public static String Tracking_Content_label;
	public static String Tracking_InfoStream_label;
	public static String Tracking_InputStream_label;
	public static String Tracking_InputStream_OnlyHistory_label;
	public static String Tracking_OutputStream_label;
	public static String Tracking_OutputStream_TruncateLines_label;
	public static String Tracking_OutputStream_TruncateLines_error_Invalid_message;
	public static String Tracking_Sources_label;
	public static String Tracking_File_label;
	public static String Tracking_File_Append_label;
	public static String Tracking_File_Overwrite_label;
	public static String Tracking_Actions_label;
	public static String Tracking_Actions_PrependTimestamp_label;
	
	public static String Login_error_UnsupportedOperation_message;
	public static String Login_Dialog_title;
	public static String Login_Dialog_message;
	public static String Login_Dialog_Name_label;
	public static String Login_Dialog_Password_label;
	public static String Login_Dialog_Save_label;
	public static String Login_Safe_error_Saving_message;
	public static String Login_Safe_error_Loading_message;
	
	public static String ExecuteHandler_error_message;
	public static String Util_ChooseFile_Dialog_title;
	public static String Util_ChooseFile_File_label;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
