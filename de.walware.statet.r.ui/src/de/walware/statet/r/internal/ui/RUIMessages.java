/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui;

import org.eclipse.osgi.util.NLS;


public class RUIMessages extends NLS {
	
	
	public static String ChooseREnv_None_label;
	public static String ChooseREnv_WorkbenchDefault_label;
	public static String ChooseREnv_Selected_label;
	public static String ChooseREnv_Configure_label;
	public static String ChooseREnv_error_InvalidPreferences_message;
	public static String ChooseREnv_error_IncompleteSelection_message;
	public static String ChooseREnv_error_InvalidSelection_message;
	
	public static String StripComments_task_label;
	public static String CorrectIndent_task_label;
	
	public static String Proposal_RenameInFile_label;
	public static String Proposal_RenameInFile_description;
	public static String Proposal_RenameInChunk_label;
	public static String Proposal_RenameInChunk_description;
	public static String Proposal_RenameInFilePrecending_label;
	public static String Proposal_RenameInFilePrecending_description;
	public static String Proposal_RenameInFileFollowing_label;
	public static String Proposal_RenameInFileFollowing_description;
	
	public static String Proposal_RenameInRegion_label;
	public static String Proposal_RenameInRegion_description;
	public static String Proposal_RenameInWorkspace_label;
	public static String Proposal_RenameInWorkspace_description;
	
	public static String Outline_HideGeneralVariables_name;
	public static String Outline_HideLocalElements_name;
	
	public static String EditorTemplates_RCodeContext_label;
	public static String EditorTemplates_RoxygenContext_label;
	
	public static String GenerateRoxygenElementComment_label;
	public static String GenerateRoxygenElementComment_error_message;
	
	public static String Templates_Variable_ElementName_description;
	public static String Templates_Variable_RoxygenParamTags_description;
	public static String Templates_Variable_RoxygenSlotTags_description;
	public static String Templates_Variable_RoxygenSigList_description;
	
	
	static {
		NLS.initializeMessages(RUIMessages.class.getName(), RUIMessages.class);
	}
	private RUIMessages() {}
	
}
