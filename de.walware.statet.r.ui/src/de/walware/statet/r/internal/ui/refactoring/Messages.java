/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.refactoring;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String RenameInWorkspace_Wizard_title;
	public static String RenameInWorkspace_Wizard_header;
	public static String RenameInWorkspace_Wizard_VariableName_label;
	public static String RenameInWorkspace_error_message;
	
	public static String RenameInRegion_Wizard_title;
	public static String RenameInRegion_Wizard_header;
	public static String RenameInRegion_error_message;
	
	public static String InlineTemp_Wizard_title;
	public static String InlineTemp_Wizard_header;
	public static String InlineTemp_error_message;
	
	public static String ExtractTemp_Wizard_title;
	public static String ExtractTemp_Wizard_header;
	public static String ExtractTemp_Wizard_VariableName_label;
	public static String ExtractTemp_Wizard_ReplaceAll_label;
	public static String ExtractTemp_error_message;
	
	public static String ExtractFunction_Wizard_title;
	public static String ExtractFunction_Wizard_header;
	public static String ExtractFunction_Wizard_VariableName_label;
	public static String ExtractFunction_error_message;
	
	public static String FunctionToS4Method_Wizard_title;
	public static String FunctionToS4Method_Wizard_header;
	public static String FunctionToS4Method_Wizard_VariableName_label;
	public static String FunctionToS4Method_Wizard_GenerateGeneric_label;
	public static String FunctionToS4Method_error_message;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
