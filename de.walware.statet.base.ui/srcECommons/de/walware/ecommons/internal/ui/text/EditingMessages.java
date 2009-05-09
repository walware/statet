/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.internal.ui.text;

import org.eclipse.osgi.util.NLS;


public class EditingMessages extends NLS {
	
	
	public static String ContentAssistProcessor_DefaultProposalCategory;
	public static String ContentAssistProcessor_ContextSelection_label;
	public static String ContentAssistProcessor_Empty_message;
	public static String ContentAssistProcessor_ToggleAffordance_message;
	public static String ContentAssistProcessor_ToggleAffordance_PressGesture_message;
	public static String ContentAssistProcessor_ToggleAffordance_ClickGesture_message;
	
	public static String ContentAssistProcessor_ComputingProposals_task;
	public static String ContentAssistProcessor_ComputingProposals_Sorting_task;
	public static String ContentAssistProcessor_ComputingProposals_Collecting_task;
	public static String ContentAssistProcessor_ComputingContexts_task;
	public static String ContentAssistProcessor_ComputingContexts_Sorting_task;
	public static String ContentAssistProcessor_ComputingContexts_Collecting_task;
	
	public static String GotoMatchingBracketAction_error_BracketOutsideSelectedElement;
	public static String GotoMatchingBracketAction_error_InvalidSelection;
	public static String GotoMatchingBracketAction_error_NoMatchingBracket;
	
	public static String ContentAssistAdvancedConfig_description;
	public static String ContentAssistAdvancedConfig_Default_label;
	public static String ContentAssistAdvancedConfig_DefaultTable_label;
	public static String ContentAssistAdvancedConfig_ProposalKinds_label;
	public static String ContentAssistAdvancedConfig_KeyBinding_label;
	public static String ContentAssistAdvancedConfig_message_DefaultKeyBinding;
	public static String ContentAssistAdvancedConfig_message_NoDefaultKeyBinding;
	public static String ContentAssistAdvancedConfig_message_KeyBindingHint;
	public static String ContentAssistAdvancedConfig_Cicling_label;
	public static String ContentAssistAdvancedConfig_CiclingTable_label;
	
	
	static {
		NLS.initializeMessages(EditingMessages.class.getName(), EditingMessages.class);
	}
	private EditingMessages() {}
	
}
