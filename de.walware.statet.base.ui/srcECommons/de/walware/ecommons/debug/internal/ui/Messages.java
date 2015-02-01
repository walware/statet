/*=============================================================================#
 # Copyright (c) 2005-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.debug.internal.ui;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String LaunchDelegate_LaunchingTask_label;
	public static String LaunchDelegate_Init_subtask;
	public static String BackgroundResourceRefresher_Job_name;
	
	public static String UnterminatedLaunchAlerter_WorkbenchClosing_title;
	public static String UnterminatedLaunchAlerter_WorkbenchClosing_message;
	public static String UnterminatedLaunchAlerter_WorkbenchClosing_button_Continue;
	public static String UnterminatedLaunchAlerter_WorkbenchClosing_button_Cancel;
	
	public static String HelpRequestor_Close_name;
	public static String HelpRequestor_Close_tooltip;
	public static String HelpRequestor_Task_name;
	public static String HelpRequestor_error_WhenRunProcess_message;
	public static String HelpRequestor_error_WhenReadOutput_message;
	
	public static String InsertVariable_label;
	public static String InputArguments_label;
	public static String InputArguments_note;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
