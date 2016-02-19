/*=============================================================================#
 # Copyright (c) 2005-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.ext.ui.wizards;

import org.eclipse.osgi.util.NLS;


public class StatetWizardsMessages extends NLS {
	
	
	public static String NewProjectReferencePage_title;
	public static String NewProjectReferencePage_description;
	
	public static String ResourceGroup_NewFile_label;
	public static String ResourceGroup_error_EmptyName;
	public static String ResourceGroup_error_InvalidFilename;
	public static String ResourceGroup_error_ResourceExists;
	
	public static String NewElement_CreateFile_task;
	public static String NewElement_CreateProject_task;
	public static String NewElement_AddProjectToWorkingSet_task;
	public static String NewElement_error_DuringOperation_message;
	
	
	static {
		NLS.initializeMessages(StatetWizardsMessages.class.getName(), StatetWizardsMessages.class);
	}
	private StatetWizardsMessages() {}
	
}
