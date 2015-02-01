/*=============================================================================#
 # Copyright (c) 2013-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.search;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	public static String Search_Query_label;
	public static String Search_error_RunFailed_message;
	public static String Search_Match_sing_label;
	public static String Search_Match_plural_label;
	public static String Search_Occurrence_sing_label;
	public static String Search_Occurrence_plural_label;
	public static String Search_WriteOccurrence_sing_label;
	public static String Search_WriteOccurrence_plural_label;
	
	public static String menus_Scope_Workspace_name;
	public static String menus_Scope_Workspace_mnemonic;
	public static String menus_Scope_Project_name;
	public static String menus_Scope_Project_mnemonic;
	public static String menus_Scope_File_name;
	public static String menus_Scope_File_mnemonic;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
