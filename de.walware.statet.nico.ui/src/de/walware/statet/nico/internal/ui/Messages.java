/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

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
	
	public static String Console_SubmitButton_label;
	
	public static String Console_error_UnexpectedException_message;

	public static String PauseAction_name;
	public static String PauseAction_tooltip;
	public static String CancelAction_name;
	public static String CancelAction_tooltip;
	public static String ShowToolDescription_name;
	public static String ShowToolDescription_tooltip;
	public static String ShowProgress_name;
	public static String ShowProgress_tooltip;
	
	public static String TerminatingMonitor_title;
	public static String TerminatingMonitor_message;
	public static String TerminatingMonitor_CancelButton_label;
	public static String TerminatingMonitor_ForceButton_label;
	public static String TerminatingMonitor_WaitButton_label;


	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
