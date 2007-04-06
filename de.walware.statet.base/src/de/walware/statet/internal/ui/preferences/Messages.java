/*******************************************************************************
 * Copyright (c) 2005-2007 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.internal.ui.preferences;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String StatetBase_description;

	public static String PropertyAndPreference_UseProjectSettings_label;
	public static String PropertyAndPreference_ShowProjectSpecificSettings_label;
	public static String PropertyAndPreference_ShowWorkspaceSettings_label;

	public static String Editors_description;
	public static String Editors_link;
	public static String Editors_Appearance;
	public static String Editors_HighlightMatchingBrackets;
	public static String Editors_AppearanceColors;
	public static String Editors_Color;
	public static String Editors_MatchingBracketsHighlightColor;
	public static String Editors_CodeAssistProposalsForegroundColor;
	public static String Editors_CodeAssistProposalsBackgroundColor;
	public static String Editors_CodeAssistParametersForegrondColor;
	public static String Editors_CodeAssistParametersBackgroundColor;
	public static String Editors_CodeAssistReplacementForegroundColor;
	public static String Editors_CodeAssistReplacementBackgroundColor;
	public static String Editors_CodeAssist;
	public static String Editors_CodeAssist_AutoInsert;

	public static String SyntaxColoring_link;
	public static String SyntaxColoring_List_label;
	public static String SyntaxColoring_Category_tooltip;
	public static String SyntaxColoring_Enable;
	public static String SyntaxColoring_Color;
	public static String SyntaxColoring_Bold;
	public static String SyntaxColoring_Italic;
	public static String SyntaxColoring_Underline;
	public static String SyntaxColoring_Strikethrough;
	public static String SyntaxColoring_Preview;

	public static String CodeTemplates_title;
	public static String CodeTemplates_label;
	public static String CodeTemplates_EditButton_label;
	public static String CodeTemplates_ImportButton_label;
	public static String CodeTemplates_ExportButton_label;
	public static String CodeTemplates_ExportAllButton_label;
	public static String CodeTemplates_Preview_label;
	
	public static String CodeTemplates_error_title;
	public static String CodeTemplates_error_Parse_message;
	public static String CodeTemplates_error_Read_message;
	public static String CodeTemplates_error_Write_message;

	public static String CodeTemplates_Import_title;
	public static String CodeTemplates_Import_extension;
	public static String CodeTemplates_Export_title;
	public static String CodeTemplates_Export_extension;
	public static String CodeTemplates_Export_filename;
	public static String CodeTemplates_Export_Error_title;
	public static String CodeTemplates_Export_Error_Hidden_message;
	public static String CodeTemplates_Export_Error_CanNotWrite_message;
	public static String CodeTemplates_Export_Exists_title;
	public static String CodeTemplates_Export_Exists_message;


	public static String TaskTags_title;
	public static String TaskTags_description;
	public static String TaskTags_TaskColumn_name;
	public static String TaskTags_PriorityColumn_name;
	public static String TaskTags_DefaultTask;
	public static String TaskTags_AddButton_label;
	public static String TaskTags_EditButton_label;
	public static String TaskTags_RemoveButton_label;
	public static String TaskTags_DefaultButton_label;
	public static String TaskTags_CaseSensitive_label;
	public static String TaskTags_warning_NoTag_message;
	public static String TaskTags_error_DefaultTast_message;

	public static String TaskTags_NeedsBuild_title;
	public static String TaskTags_NeedsFullBuild_message;
	public static String TaskTags_NeedsProjectBuild_message;

	public static String TaskTags_InputDialog_NewTag_title;
	public static String TaskTags_InputDialog_EditTag_title;
	public static String TaskTags_InputDialog_Name_label;
	public static String TaskTags_InputDialog_Priority_label;
	public static String TaskTags_InputDialog_error_EnterName_message;
	public static String TaskTags_InputDialog_error_Comma_message;
	public static String TaskTags_InputDialog_error_EntryExists_message;
	public static String TaskTags_InputDialog_error_ShouldStartWithLetterOrDigit_message;


	private static final String BUNDLE_NAME = Messages.class.getName();
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	private Messages() {}
	
}
