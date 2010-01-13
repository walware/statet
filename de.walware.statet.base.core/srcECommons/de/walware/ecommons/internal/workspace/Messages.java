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

package de.walware.ecommons.internal.workspace;

import org.eclipse.osgi.util.NLS;


public class Messages {
	
	
	public static String CoreUtility_Build_Job_title;
	public static String CoreUtility_Build_AllTask_name;
	public static String CoreUtility_Build_ProjectTask_name;
	
	public static String Resource_error_NoInput_message;
	public static String Resource_error_DoesNotExists_message;
	public static String Resource_error_AlreadyExists_message;
	public static String Resource_error_NoValidSpecification_message;
	public static String Resource_error_Other_message;
	public static String Resource_error_IsFile_message;
	public static String Resource_error_IsDirectory_message;
	public static String Resource_error_NotLocal_message;
	
	public static String FileType_Local_name;
	public static String FileType_Workspace_name;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
