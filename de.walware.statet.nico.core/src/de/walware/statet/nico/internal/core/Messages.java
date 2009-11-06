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

package de.walware.statet.nico.internal.core;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String SubmitType_Console_label;
	public static String SubmitType_Editor_label;
	public static String SubmitType_Tools_label;
	public static String SubmitType_Other_label;
	
	public static String LoadHistory_AllocatingTask_label;
	public static String LoadHistory_ok_message;
	public static String LoadHistory_error_message;
	public static String SaveHistory_ok_message;
	public static String SaveHistory_error_message;
	
	public static String ErrorHandling_error_message;
	public static String ToolRunnable_error_RuntimeError_message;
	public static String Runtime_error_CriticalError_message;
	public static String Runtime_error_UnexpectedTermination_message;
	
	public static String ToolController_CommonStartTask_label;
	public static String ToolController_FileOperation_error_CannotResolve_message;
	public static String ToolController_SubmitCancelled_message;
	public static String ToolController_ToolTerminated_message;
	
	public static String Progress_Starting_label;
	public static String Progress_Terminating_label;
	
	public static String Progress_Canceled_label;
	public static String Progress_Blocked_label;
	
	public static String SessionVariables_StartupDate_description;
	public static String SessionVariables_StartupTime_description;
	public static String SessionVariables_ConnectionDate_description;
	public static String SessionVariables_ConnectionTime_description;
	public static String SessionVariables_StartupWD_description;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
