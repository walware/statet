/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.nico.ui;

import org.eclipse.osgi.util.NLS;


public class RNicoMessages extends NLS {
	
	
	public static String AdjustWidth_label;
	public static String AdjustWidth_description;
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
		NLS.initializeMessages(RNicoMessages.class.getName(), RNicoMessages.class);
	}
	private RNicoMessages() {}
	
}
