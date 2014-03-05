/*=============================================================================#
 # Copyright (c) 2009-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.refactoring;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String SearchScope_Workspace_label;
	public static String SearchScope_CurrentAndReferencingProjects_label;
	public static String SearchScope_CurrentProject_label;
	public static String SearchScope_CurrentFile_label;
	public static String SearchScope_LocalFrame_label;
	
	public static String SearchProcessor_label;
	
	public static String RenameInWorkspace_label;
	public static String RenameInWorkspace_Descriptor_description;
	public static String RenameInWorkspace_error_InvalidSelection_message;
	public static String RenameInWorkspace_warning_NoDefinition_message;
	public static String RenameInWorkspace_warning_MultipleDefinitions_message;
	public static String RenameInWorkspace_Changes_ReplaceOccurrence_name;
	
	public static String RenameInRegion_label;
	public static String RenameInRegion_Descriptor_description;
	public static String RenameInRegion_Changes_VariableGroup_name;
	public static String RenameInRegion_Changes_ReplaceOccurrence_name;
	public static String RenameInRegion_Changes_ReplaceOccurrenceOf_name;
	
	public static String InlineTemp_label;
	public static String InlineTemp_Descriptor_description;
	public static String InlineTemp_error_InvalidSelection_message;
	public static String InlineTemp_error_InvalidSelectionNotLocal_message;
	public static String InlineTemp_error_InvalidSelectionParameter_message;
	public static String InlineTemp_error_InvalidSelectionNoArrow_message;
	public static String InlineTemp_error_MissingDefinition_message;
	public static String InlineTemp_warning_ValueSyntaxError_message;
	public static String InlineTemp_Changes_DeleteAssignment_name;
	public static String InlineTemp_Changes_ReplaceAssignment_name;
	public static String InlineTemp_Changes_ReplaceReference_name;
	
	public static String ExtractTemp_label;
	public static String ExtractTemp_Descriptor_description;
	public static String ExtractTemp_error_InvalidSelection_message;
	public static String ExtractTemp_error_InvalidSelectionType_message;
	public static String ExtractTemp_error_InvalidSelectionFHeader_message;
	public static String ExtractTemp_warning_OccurrencesSyntaxError_message;
	public static String ExtractTemp_warning_ChangedRange_message;
	public static String ExtractTemp_Changes_AddVariable_name;
	public static String ExtractTemp_Changes_ReplaceOccurrence_name;
	
	public static String ExtractFunction_label;
	public static String ExtractFunction_Descriptor_description;
	public static String ExtractFunction_error_InvalidSelection_message;
	public static String ExtractFunction_warning_SelectionSyntaxError_message;
	public static String ExtractFunction_warning_ChangedRange_message;
	public static String ExtractFunction_Changes_AddFunctionDef_name;
	public static String ExtractFunction_Changes_DeleteOld_name;
	public static String ExtractFunction_Changes_ReplaceOldWithFunctionDef_name;
	public static String ExtractFunction_Changes_AddFunctionCall_name;
	
	public static String FunctionToS4Method_label;
	public static String FunctionToS4Method_Descriptor_description;
	public static String FunctionToS4Method_error_InvalidSelection_message;
	public static String FunctionToS4Method_error_SelectionAlreadyS4_message;
	public static String FunctionToS4Method_warning_SelectionSyntaxError_message;
	public static String FunctionToS4Method_Changes_DeleteOld_name;
	public static String FunctionToS4Method_Changes_AddGenericDef_name;
	public static String FunctionToS4Method_Changes_AddMethodDef_name;
	
	public static String RIdentifiers_error_Empty_message;
	public static String RIdentifiers_error_EmptyFor_message;
	public static String RIdentifiers_error_Invalid_message;
	public static String RIdentifiers_error_InvalidFor_message;
	
	public static String RModel_DeleteParticipant_name;
	public static String RModel_DeleteProject_name;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
