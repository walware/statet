/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

import de.walware.eclipsecommons.ui.text.presentation.ITextPresentationConstants;

import de.walware.statet.r.ui.text.r.IRTextTokens;
import de.walware.statet.r.ui.text.rd.IRdTextTokens;

/**
 * Preference constants used in the StatET-R preference store. Clients should only read the
 * RDT-UI preference store using these values. Clients are not allowed to modify the
 * preference store programmatically.
 * 
 * @see org.eclipse.jface.resource.StringConverter
 * @see org.eclipse.jface.preference.PreferenceConverter
 */
public class RUIPreferenceConstants {
	
	public interface R {
		public final static String TS_GROUP_ID = "r.editor/textstyles"; //$NON-NLS-1$
		public final static String TS_ROOT = IRTextTokens.ROOT;
		public final static String TS_ITEMS_SUFFIX = ".items"; //$NON-NLS-1$
		
		public final static String TS_DEFAULT_ROOT = IRTextTokens.SYMBOL_KEY;
		public final static String TS_DEFAULT_COLOR = TS_DEFAULT_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_DEFAULT_BOLD = TS_DEFAULT_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_DEFAULT_ITALIC = TS_DEFAULT_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_DEFAULT_UNDERLINE = TS_DEFAULT_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_DEFAULT_STRIKETHROUGH = TS_DEFAULT_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_IDENTIFIER_SUB_ASSIGNMENT_ROOT = IRTextTokens.SYMBOL_SUB_ASSIGN_KEY;
		public final static String TS_IDENTIFIER_SUB_ASSIGNMENT_USE = TS_IDENTIFIER_SUB_ASSIGNMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_USE_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_ASSIGNMENT_ITEMS = TS_IDENTIFIER_SUB_ASSIGNMENT_ROOT + TS_ITEMS_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_ASSIGNMENT_COLOR = TS_IDENTIFIER_SUB_ASSIGNMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_ASSIGNMENT_BOLD = TS_IDENTIFIER_SUB_ASSIGNMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_ASSIGNMENT_ITALIC = TS_IDENTIFIER_SUB_ASSIGNMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_ASSIGNMENT_UNDERLINE = TS_IDENTIFIER_SUB_ASSIGNMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_ASSIGNMENT_STRIKETHROUGH = TS_IDENTIFIER_SUB_ASSIGNMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_IDENTIFIER_SUB_LOGICAL_ROOT = IRTextTokens.SYMBOL_SUB_LOGICAL_KEY;
		public final static String TS_IDENTIFIER_SUB_LOGICAL_USE = TS_IDENTIFIER_SUB_LOGICAL_ROOT + ITextPresentationConstants.TEXTSTYLE_USE_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_LOGICAL_ITEMS = TS_IDENTIFIER_SUB_LOGICAL_ROOT + TS_ITEMS_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_LOGICAL_COLOR = TS_IDENTIFIER_SUB_LOGICAL_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_LOGICAL_BOLD = TS_IDENTIFIER_SUB_LOGICAL_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_LOGICAL_ITALIC = TS_IDENTIFIER_SUB_LOGICAL_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_LOGICAL_UNDERLINE = TS_IDENTIFIER_SUB_LOGICAL_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_LOGICAL_STRIKETHROUGH = TS_IDENTIFIER_SUB_LOGICAL_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_IDENTIFIER_SUB_FLOWCONTROL_ROOT = IRTextTokens.SYMBOL_SUB_FLOWCONTROL_KEY;
		public final static String TS_IDENTIFIER_SUB_FLOWCONTROL_USE = TS_IDENTIFIER_SUB_FLOWCONTROL_ROOT + ITextPresentationConstants.TEXTSTYLE_USE_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_FLOWCONTROL_ITEMS = TS_IDENTIFIER_SUB_FLOWCONTROL_ROOT + TS_ITEMS_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_FLOWCONTROL_COLOR = TS_IDENTIFIER_SUB_FLOWCONTROL_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_FLOWCONTROL_BOLD = TS_IDENTIFIER_SUB_FLOWCONTROL_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_FLOWCONTROL_ITALIC = TS_IDENTIFIER_SUB_FLOWCONTROL_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_FLOWCONTROL_UNDERLINE = TS_IDENTIFIER_SUB_FLOWCONTROL_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_FLOWCONTROL_STRIKETHROUGH = TS_IDENTIFIER_SUB_FLOWCONTROL_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_IDENTIFIER_SUB_CUSTOM1_ROOT = IRTextTokens.SYMBOL_SUB_CUSTOM1_KEY;
		public final static String TS_IDENTIFIER_SUB_CUSTOM1_USE = TS_IDENTIFIER_SUB_CUSTOM1_ROOT + ITextPresentationConstants.TEXTSTYLE_USE_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_CUSTOM1_ITEMS = TS_IDENTIFIER_SUB_CUSTOM1_ROOT + TS_ITEMS_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_CUSTOM1_COLOR = TS_IDENTIFIER_SUB_CUSTOM1_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_CUSTOM1_BOLD = TS_IDENTIFIER_SUB_CUSTOM1_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_CUSTOM1_ITALIC = TS_IDENTIFIER_SUB_CUSTOM1_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_CUSTOM1_UNDERLINE = TS_IDENTIFIER_SUB_CUSTOM1_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_CUSTOM1_STRIKETHROUGH = TS_IDENTIFIER_SUB_CUSTOM1_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_IDENTIFIER_SUB_CUSTOM2_ROOT = IRTextTokens.SYMBOL_SUB_CUSTOM2_KEY;
		public final static String TS_IDENTIFIER_SUB_CUSTOM2_USE = TS_IDENTIFIER_SUB_CUSTOM2_ROOT + ITextPresentationConstants.TEXTSTYLE_USE_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_CUSTOM2_ITEMS = TS_IDENTIFIER_SUB_CUSTOM2_ROOT + TS_ITEMS_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_CUSTOM2_COLOR = TS_IDENTIFIER_SUB_CUSTOM2_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_CUSTOM2_BOLD = TS_IDENTIFIER_SUB_CUSTOM2_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_CUSTOM2_ITALIC = TS_IDENTIFIER_SUB_CUSTOM2_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_CUSTOM2_UNDERLINE = TS_IDENTIFIER_SUB_CUSTOM2_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_IDENTIFIER_SUB_CUSTOM2_STRIKETHROUGH = TS_IDENTIFIER_SUB_CUSTOM2_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_UNDEFINED_ROOT = IRTextTokens.UNDEFINED_KEY;
		public final static String TS_UNDEFINED_COLOR = TS_UNDEFINED_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_UNDEFINED_BOLD = TS_UNDEFINED_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_UNDEFINED_ITALIC = TS_UNDEFINED_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_UNDEFINED_UNDERLINE = TS_UNDEFINED_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_UNDEFINED_STRIKETHROUGH = TS_UNDEFINED_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_COMMENT_ROOT = IRTextTokens.COMMENT_KEY;
		public final static String TS_COMMENT_COLOR = TS_COMMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_COMMENT_BOLD = TS_COMMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_COMMENT_ITALIC = TS_COMMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_COMMENT_UNDERLINE = TS_COMMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_COMMENT_STRIKETHROUGH = TS_COMMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_TASK_TAG_ROOT = IRTextTokens.TASK_TAG_KEY;
		public final static String TS_TASK_TAG_COLOR = TS_TASK_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_TASK_TAG_BOLD = TS_TASK_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_TASK_TAG_ITALIC = TS_TASK_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_TASK_TAG_UNDERLINE = TS_TASK_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_TASK_TAG_STRIKETHROUGH = TS_TASK_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_STRING_ROOT = IRTextTokens.STRING_KEY;
		public final static String TS_STRING_COLOR = TS_STRING_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_STRING_BOLD = TS_STRING_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_STRING_ITALIC = TS_STRING_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_STRING_UNDERLINE = TS_STRING_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_STRING_STRIKETHROUGH = TS_STRING_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_NUMBERS_ROOT = IRTextTokens.NUM_KEY;
		public final static String TS_NUMBERS_COLOR = TS_NUMBERS_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_NUMBERS_BOLD = TS_NUMBERS_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_NUMBERS_ITALIC = TS_NUMBERS_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_NUMBERS_UNDERLINE = TS_NUMBERS_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_NUMBERS_STRIKETHROUGH = TS_NUMBERS_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_NUMBERS_SUB_INT_ROOT = IRTextTokens.NUM_SUB_INT_KEY;
		public final static String TS_NUMBERS_SUB_INT_USE = TS_NUMBERS_SUB_INT_ROOT + ITextPresentationConstants.TEXTSTYLE_USE_SUFFIX;
		public final static String TS_NUMBERS_SUB_INT_COLOR = TS_NUMBERS_SUB_INT_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_NUMBERS_SUB_INT_BOLD = TS_NUMBERS_SUB_INT_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_NUMBERS_SUB_INT_ITALIC = TS_NUMBERS_SUB_INT_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_NUMBERS_SUB_INT_UNDERLINE = TS_NUMBERS_SUB_INT_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_NUMBERS_SUB_INT_STRIKETHROUGH = TS_NUMBERS_SUB_INT_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_NUMBERS_SUB_CPLX_ROOT = IRTextTokens.NUM_SUB_CPLX_KEY;
		public final static String TS_NUMBERS_SUB_CPLX_USE = TS_NUMBERS_SUB_CPLX_ROOT + ITextPresentationConstants.TEXTSTYLE_USE_SUFFIX;
		public final static String TS_NUMBERS_SUB_CPLX_COLOR = TS_NUMBERS_SUB_CPLX_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_NUMBERS_SUB_CPLX_BOLD = TS_NUMBERS_SUB_CPLX_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_NUMBERS_SUB_CPLX_ITALIC = TS_NUMBERS_SUB_CPLX_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_NUMBERS_SUB_CPLX_UNDERLINE = TS_NUMBERS_SUB_CPLX_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_NUMBERS_SUB_CPLX_STRIKETHROUGH = TS_NUMBERS_SUB_CPLX_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_SPECIAL_CONSTANTS_ROOT = IRTextTokens.SPECIALCONST_KEY;
		public final static String TS_SPECIAL_CONSTANTS_COLOR = TS_SPECIAL_CONSTANTS_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_SPECIAL_CONSTANTS_BOLD = TS_SPECIAL_CONSTANTS_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_SPECIAL_CONSTANTS_ITALIC = TS_SPECIAL_CONSTANTS_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_SPECIAL_CONSTANTS_UNDERLINE = TS_SPECIAL_CONSTANTS_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_SPECIAL_CONSTANTS_STRIKETHROUGH = TS_SPECIAL_CONSTANTS_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_LOGICAL_CONSTANTS_ROOT = IRTextTokens.LOGICALCONST_KEY;
		public final static String TS_LOGICAL_CONSTANTS_COLOR = TS_LOGICAL_CONSTANTS_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_LOGICAL_CONSTANTS_BOLD = TS_LOGICAL_CONSTANTS_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_LOGICAL_CONSTANTS_ITALIC = TS_LOGICAL_CONSTANTS_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_LOGICAL_CONSTANTS_UNDERLINE = TS_LOGICAL_CONSTANTS_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_LOGICAL_CONSTANTS_STRIKETHROUGH = TS_LOGICAL_CONSTANTS_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_FLOWCONTROL_ROOT = IRTextTokens.FLOWCONTROL_KEY;
		public final static String TS_FLOWCONTROL_COLOR = TS_FLOWCONTROL_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_FLOWCONTROL_BOLD = TS_FLOWCONTROL_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_FLOWCONTROL_ITALIC = TS_FLOWCONTROL_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_FLOWCONTROL_UNDERLINE = TS_FLOWCONTROL_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_FLOWCONTROL_STRIKETHROUGH = TS_FLOWCONTROL_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_SEPARATORS_ROOT = IRTextTokens.SEPARATOR_KEY;
		public final static String TS_SEPARATORS_COLOR = TS_SEPARATORS_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_SEPARATORS_BOLD = TS_SEPARATORS_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_SEPARATORS_ITALIC = TS_SEPARATORS_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_SEPARATORS_UNDERLINE = TS_SEPARATORS_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_SEPARATORS_STRIKETHROUGH = TS_SEPARATORS_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_ASSIGNMENT_ROOT = IRTextTokens.ASSIGN_KEY;
		public final static String TS_ASSIGNMENT_COLOR = TS_ASSIGNMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_ASSIGNMENT_BOLD = TS_ASSIGNMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_ASSIGNMENT_ITALIC = TS_ASSIGNMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_ASSIGNMENT_UNDERLINE = TS_ASSIGNMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_ASSIGNMENT_STRIKETHROUGH = TS_ASSIGNMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_ASSIGNMENT_SUB_EQUALSIGN_ROOT = IRTextTokens.ASSIGN_SUB_EQUAL_KEY;
		public final static String TS_ASSIGNMENT_SUB_EQUALSIGN_USE = TS_ASSIGNMENT_SUB_EQUALSIGN_ROOT + ITextPresentationConstants.TEXTSTYLE_USE_SUFFIX;
		public final static String TS_ASSIGNMENT_SUB_EQUALSIGN_COLOR = TS_ASSIGNMENT_SUB_EQUALSIGN_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_ASSIGNMENT_SUB_EQUALSIGN_BOLD = TS_ASSIGNMENT_SUB_EQUALSIGN_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_ASSIGNMENT_SUB_EQUALSIGN_ITALIC = TS_ASSIGNMENT_SUB_EQUALSIGN_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_ASSIGNMENT_SUB_EQUALSIGN_UNDERLINE = TS_ASSIGNMENT_SUB_EQUALSIGN_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_ASSIGNMENT_SUB_EQUALSIGN_STRIKETHROUGH = TS_ASSIGNMENT_SUB_EQUALSIGN_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_OTHER_OPERATORS_ROOT = IRTextTokens.OP_KEY;
		public final static String TS_OTHER_OPERATORS_COLOR = TS_OTHER_OPERATORS_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_OTHER_OPERATORS_BOLD = TS_OTHER_OPERATORS_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_OTHER_OPERATORS_ITALIC = TS_OTHER_OPERATORS_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_OTHER_OPERATORS_UNDERLINE = TS_OTHER_OPERATORS_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_OTHER_OPERATORS_STRIKETHROUGH = TS_OTHER_OPERATORS_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_OPERATORS_SUB_LOGICAL_ROOT = IRTextTokens.OP_SUB_LOGICAL_KEY;
		public final static String TS_OPERATORS_SUB_LOGICAL_USE = TS_OPERATORS_SUB_LOGICAL_ROOT + ITextPresentationConstants.TEXTSTYLE_USE_SUFFIX;
		public final static String TS_OPERATORS_SUB_LOGICAL_COLOR = TS_OPERATORS_SUB_LOGICAL_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_OPERATORS_SUB_LOGICAL_BOLD = TS_OPERATORS_SUB_LOGICAL_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_OPERATORS_SUB_LOGICAL_ITALIC = TS_OPERATORS_SUB_LOGICAL_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_OPERATORS_SUB_LOGICAL_UNDERLINE = TS_OPERATORS_SUB_LOGICAL_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_OPERATORS_SUB_LOGICAL_STRIKETHROUGH = TS_OPERATORS_SUB_LOGICAL_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_OPERATORS_SUB_RELATIONAL_ROOT = IRTextTokens.OP_SUB_RELATIONAL_KEY;
		public final static String TS_OPERATORS_SUB_RELATIONAL_USE = TS_OPERATORS_SUB_RELATIONAL_ROOT + ITextPresentationConstants.TEXTSTYLE_USE_SUFFIX;
		public final static String TS_OPERATORS_SUB_RELATIONAL_COLOR = TS_OPERATORS_SUB_RELATIONAL_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_OPERATORS_SUB_RELATIONAL_BOLD = TS_OPERATORS_SUB_RELATIONAL_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_OPERATORS_SUB_RELATIONAL_ITALIC = TS_OPERATORS_SUB_RELATIONAL_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_OPERATORS_SUB_RELATIONAL_UNDERLINE = TS_OPERATORS_SUB_RELATIONAL_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_OPERATORS_SUB_RELATIONAL_STRIKETHROUGH = TS_OPERATORS_SUB_RELATIONAL_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_OPERATORS_SUB_USERDEFINED_ROOT = IRTextTokens.OP_SUB_USERDEFINED_KEY;
		public final static String TS_OPERATORS_SUB_USERDEFINED_USE = TS_OPERATORS_SUB_USERDEFINED_ROOT + ITextPresentationConstants.TEXTSTYLE_USE_SUFFIX;
		public final static String TS_OPERATORS_SUB_USERDEFINED_COLOR = TS_OPERATORS_SUB_USERDEFINED_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_OPERATORS_SUB_USERDEFINED_BOLD = TS_OPERATORS_SUB_USERDEFINED_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_OPERATORS_SUB_USERDEFINED_ITALIC = TS_OPERATORS_SUB_USERDEFINED_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_OPERATORS_SUB_USERDEFINED_UNDERLINE = TS_OPERATORS_SUB_USERDEFINED_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_OPERATORS_SUB_USERDEFINED_STRIKETHROUGH = TS_OPERATORS_SUB_USERDEFINED_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_GROUPING_ROOT = IRTextTokens.GROUPING_KEY;
		public final static String TS_GROUPING_COLOR = TS_GROUPING_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_GROUPING_BOLD = TS_GROUPING_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_GROUPING_ITALIC = TS_GROUPING_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_GROUPING_UNDERLINE = TS_GROUPING_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_GROUPING_STRIKETHROUGH = TS_GROUPING_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_INDEXING_ROOT = IRTextTokens.SUBACCESS_KEY;
		public final static String TS_INDEXING_COLOR = TS_INDEXING_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_INDEXING_BOLD = TS_INDEXING_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_INDEXING_ITALIC = TS_INDEXING_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_INDEXING_UNDERLINE = TS_INDEXING_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_INDEXING_STRIKETHROUGH = TS_INDEXING_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
	
	}
	
	public interface Rd {
		public final static String TS_GROUP_ID = "rd.editor/textstyles"; //$NON-NLS-1$
		public final static String TS_ROOT = IRdTextTokens.ROOT;
		
		public final static String TS_BRACKETS_ROOT = IRdTextTokens.BRACKETS;
		public final static String TS_BRACKETS_COLOR = TS_BRACKETS_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_BRACKETS_BOLD = TS_BRACKETS_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_BRACKETS_ITALIC = TS_BRACKETS_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_BRACKETS_UNDERLINE = TS_BRACKETS_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_BRACKETS_STRIKETHROUGH = TS_BRACKETS_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_COMMENT_ROOT = IRdTextTokens.COMMENT;
		public final static String TS_COMMENT_COLOR = TS_COMMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_COMMENT_BOLD = TS_COMMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_COMMENT_ITALIC = TS_COMMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_COMMENT_UNDERLINE = TS_COMMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_COMMENT_STRIKETHROUGH = TS_COMMENT_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_DEFAULT_ROOT = IRdTextTokens.DEFAULT;
		public final static String TS_DEFAULT_COLOR = TS_DEFAULT_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_DEFAULT_BOLD = TS_DEFAULT_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_DEFAULT_ITALIC = TS_DEFAULT_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_DEFAULT_UNDERLINE = TS_DEFAULT_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_DEFAULT_STRIKETHROUGH = TS_DEFAULT_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_OTHER_TAG_ROOT = IRdTextTokens.OTHER_TAG;
		public final static String TS_OTHER_TAG_COLOR = TS_OTHER_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_OTHER_TAG_BOLD = TS_OTHER_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_OTHER_TAG_ITALIC = TS_OTHER_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_OTHER_TAG_UNDERLINE = TS_OTHER_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_OTHER_TAG_STRIKETHROUGH = TS_OTHER_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_PLATFORM_SPECIF_ROOT = IRdTextTokens.PLATFORM_SPECIF;
		public final static String TS_PLATFORM_SPECIF_COLOR = TS_PLATFORM_SPECIF_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_PLATFORM_SPECIF_BOLD = TS_PLATFORM_SPECIF_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_PLATFORM_SPECIF_ITALIC = TS_PLATFORM_SPECIF_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_PLATFORM_SPECIF_UNDERLINE = TS_PLATFORM_SPECIF_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_PLATFORM_SPECIF_STRIKETHROUGH = TS_PLATFORM_SPECIF_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_SECTION_TAG_ROOT = IRdTextTokens.SECTION_TAG;
		public final static String TS_SECTION_TAG_COLOR = TS_SECTION_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_SECTION_TAG_BOLD = TS_SECTION_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_SECTION_TAG_ITALIC = TS_SECTION_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_SECTION_TAG_UNDERLINE = TS_SECTION_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_SECTION_TAG_STRIKETHROUGH = TS_SECTION_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_SUBSECTION_TAG_ROOT = IRdTextTokens.SUBSECTION_TAG;
		public final static String TS_SUBSECTION_TAG_COLOR = TS_SUBSECTION_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_SUBSECTION_TAG_BOLD = TS_SUBSECTION_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_SUBSECTION_TAG_ITALIC = TS_SUBSECTION_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_SUBSECTION_TAG_UNDERLINE = TS_SUBSECTION_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_SUBSECTION_TAG_STRIKETHROUGH = TS_SUBSECTION_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_TASK_TAG_ROOT = IRdTextTokens.TASK_TAG;
		public final static String TS_TASK_TAG_COLOR = TS_TASK_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_TASK_TAG_BOLD = TS_TASK_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_TASK_TAG_ITALIC = TS_TASK_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_TASK_TAG_UNDERLINE = TS_TASK_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_TASK_TAG_STRIKETHROUGH = TS_TASK_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_UNLISTED_TAG_ROOT = IRdTextTokens.UNLISTED_TAG;
		public final static String TS_UNLISTED_TAG_COLOR = TS_UNLISTED_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_UNLISTED_TAG_BOLD = TS_UNLISTED_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_UNLISTED_TAG_ITALIC = TS_UNLISTED_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_UNLISTED_TAG_UNDERLINE = TS_UNLISTED_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_UNLISTED_TAG_STRIKETHROUGH = TS_UNLISTED_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_VERBATIM_ROOT = IRdTextTokens.VERBATIM;
		public final static String TS_VERBATIM_COLOR = TS_VERBATIM_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_VERBATIM_BOLD = TS_VERBATIM_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_VERBATIM_ITALIC = TS_VERBATIM_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_VERBATIM_UNDERLINE = TS_VERBATIM_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_VERBATIM_STRIKETHROUGH = TS_VERBATIM_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
	}
	
	
	/**
	 * Initializes the given preference store with the default values.
	 * 
	 * @param store the preference store to be initialized
	 */
	public static void initializeDefaultValues(final IPreferenceStore store) {
		
		PreferenceConverter.setDefault(store, R.TS_DEFAULT_COLOR, new RGB(0, 0, 0));
		store.setDefault(R.TS_DEFAULT_BOLD, false);
		store.setDefault(R.TS_DEFAULT_ITALIC, false);
		store.setDefault(R.TS_DEFAULT_UNDERLINE, false);
		store.setDefault(R.TS_DEFAULT_STRIKETHROUGH, false);
		
		final String[] identifierSubs = new String[] {
				R.TS_IDENTIFIER_SUB_ASSIGNMENT_ROOT,
				R.TS_IDENTIFIER_SUB_LOGICAL_ROOT,
				R.TS_IDENTIFIER_SUB_FLOWCONTROL_ROOT,
				R.TS_IDENTIFIER_SUB_CUSTOM1_ROOT,
				R.TS_IDENTIFIER_SUB_CUSTOM2_ROOT,
		};
		for (final String root : identifierSubs) {
			store.setDefault(root + ITextPresentationConstants.TEXTSTYLE_USE_SUFFIX, R.TS_DEFAULT_ROOT);
			PreferenceConverter.setDefault(store, root + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX, new RGB(0, 0, 0));
			store.setDefault(root + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX, false);
			store.setDefault(root + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX, false);
			store.setDefault(root + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX, false);
			store.setDefault(root + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		}
		store.setDefault(R.TS_IDENTIFIER_SUB_ASSIGNMENT_ITEMS, "assign,rm,remove,setMethod,setGeneric,setGroupGeneric,removeClass,removeGeneric,removeMethod,removeMethods,attach,detach,source"); //$NON-NLS-1$
		store.setDefault(R.TS_IDENTIFIER_SUB_LOGICAL_ITEMS, "xor,any,all"); //$NON-NLS-1$
		store.setDefault(R.TS_IDENTIFIER_SUB_FLOWCONTROL_ITEMS, "return,switch,ifelse,stop,warning,try,tryCatch"); //$NON-NLS-1$
		store.setDefault(R.TS_IDENTIFIER_SUB_CUSTOM1_ITEMS, ""); //$NON-NLS-1$
		store.setDefault(R.TS_IDENTIFIER_SUB_CUSTOM2_ITEMS, ""); //$NON-NLS-1$
		
		PreferenceConverter.setDefault(store, R.TS_UNDEFINED_COLOR, new RGB(223, 63, 127));
		store.setDefault(R.TS_UNDEFINED_BOLD, true);
		store.setDefault(R.TS_UNDEFINED_ITALIC, false);
		store.setDefault(R.TS_UNDEFINED_UNDERLINE, false);
		store.setDefault(R.TS_UNDEFINED_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, R.TS_COMMENT_COLOR, new RGB(63, 127, 95));
		store.setDefault(R.TS_COMMENT_BOLD, false);
		store.setDefault(R.TS_COMMENT_ITALIC, false);
		store.setDefault(R.TS_COMMENT_UNDERLINE, false);
		store.setDefault(R.TS_COMMENT_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, R.TS_TASK_TAG_COLOR, new RGB(63, 127, 95));
		store.setDefault(R.TS_TASK_TAG_BOLD, true);
		store.setDefault(R.TS_TASK_TAG_ITALIC, false);
		store.setDefault(R.TS_TASK_TAG_UNDERLINE, false);
		store.setDefault(R.TS_TASK_TAG_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, R.TS_STRING_COLOR, new RGB(79, 111, 167));
		store.setDefault(R.TS_STRING_BOLD, false);
		store.setDefault(R.TS_STRING_ITALIC, false);
		store.setDefault(R.TS_STRING_UNDERLINE, false);
		store.setDefault(R.TS_STRING_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, R.TS_NUMBERS_COLOR, new RGB(0, 0, 127));
		store.setDefault(R.TS_NUMBERS_BOLD, false);
		store.setDefault(R.TS_NUMBERS_ITALIC, false);
		store.setDefault(R.TS_NUMBERS_UNDERLINE, false);
		store.setDefault(R.TS_NUMBERS_STRIKETHROUGH, false);
		
		final String[] numberSubs = new String[] {
				R.TS_NUMBERS_SUB_INT_ROOT,
				R.TS_NUMBERS_SUB_CPLX_ROOT,
		};
		for (final String root : numberSubs) {
			store.setDefault(root + ITextPresentationConstants.TEXTSTYLE_USE_SUFFIX, R.TS_NUMBERS_ROOT);
			PreferenceConverter.setDefault(store, root + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX, new RGB(0, 0, 127));
			store.setDefault(root + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX, false);
			store.setDefault(root + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX, false);
			store.setDefault(root + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX, false);
			store.setDefault(root + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		}
		
		PreferenceConverter.setDefault(store, R.TS_SPECIAL_CONSTANTS_COLOR, new RGB(127, 0, 127));
		store.setDefault(R.TS_SPECIAL_CONSTANTS_BOLD, false);
		store.setDefault(R.TS_SPECIAL_CONSTANTS_ITALIC, false);
		store.setDefault(R.TS_SPECIAL_CONSTANTS_UNDERLINE, false);
		store.setDefault(R.TS_SPECIAL_CONSTANTS_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, R.TS_LOGICAL_CONSTANTS_COLOR, new RGB(127, 0, 127));
		store.setDefault(R.TS_LOGICAL_CONSTANTS_BOLD, false);
		store.setDefault(R.TS_LOGICAL_CONSTANTS_ITALIC, false);
		store.setDefault(R.TS_LOGICAL_CONSTANTS_UNDERLINE, false);
		store.setDefault(R.TS_LOGICAL_CONSTANTS_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, R.TS_FLOWCONTROL_COLOR, new RGB(127, 0, 95));
		store.setDefault(R.TS_FLOWCONTROL_BOLD, true);
		store.setDefault(R.TS_FLOWCONTROL_ITALIC, false);
		store.setDefault(R.TS_FLOWCONTROL_UNDERLINE, false);
		store.setDefault(R.TS_FLOWCONTROL_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, R.TS_SEPARATORS_COLOR, new RGB(0, 0, 0));
		store.setDefault(R.TS_SEPARATORS_BOLD, false);
		store.setDefault(R.TS_SEPARATORS_ITALIC, false);
		store.setDefault(R.TS_SEPARATORS_UNDERLINE, false);
		store.setDefault(R.TS_SEPARATORS_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, R.TS_ASSIGNMENT_COLOR, new RGB(0, 0, 0));
		store.setDefault(R.TS_ASSIGNMENT_BOLD, true);
		store.setDefault(R.TS_ASSIGNMENT_ITALIC, false);
		store.setDefault(R.TS_ASSIGNMENT_UNDERLINE, false);
		store.setDefault(R.TS_ASSIGNMENT_STRIKETHROUGH, false);
		
		store.setDefault(R.TS_ASSIGNMENT_SUB_EQUALSIGN_USE, ""); //$NON-NLS-1$
		PreferenceConverter.setDefault(store, R.TS_ASSIGNMENT_SUB_EQUALSIGN_COLOR, new RGB(0, 0, 0));
		store.setDefault(R.TS_ASSIGNMENT_SUB_EQUALSIGN_BOLD, false);
		store.setDefault(R.TS_ASSIGNMENT_SUB_EQUALSIGN_ITALIC, false);
		store.setDefault(R.TS_ASSIGNMENT_SUB_EQUALSIGN_UNDERLINE, false);
		store.setDefault(R.TS_ASSIGNMENT_SUB_EQUALSIGN_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, R.TS_OTHER_OPERATORS_COLOR, new RGB(159, 63, 127));
		store.setDefault(R.TS_OTHER_OPERATORS_BOLD, false);
		store.setDefault(R.TS_OTHER_OPERATORS_ITALIC, false);
		store.setDefault(R.TS_OTHER_OPERATORS_UNDERLINE, false);
		store.setDefault(R.TS_OTHER_OPERATORS_STRIKETHROUGH, false);
		
		store.setDefault(R.TS_OPERATORS_SUB_LOGICAL_USE, R.TS_OTHER_OPERATORS_ROOT);
		PreferenceConverter.setDefault(store, R.TS_OPERATORS_SUB_LOGICAL_COLOR, new RGB(159, 63, 127));
		store.setDefault(R.TS_OPERATORS_SUB_LOGICAL_BOLD, false);
		store.setDefault(R.TS_OPERATORS_SUB_LOGICAL_ITALIC, false);
		store.setDefault(R.TS_OPERATORS_SUB_LOGICAL_UNDERLINE, false);
		store.setDefault(R.TS_OPERATORS_SUB_LOGICAL_STRIKETHROUGH, false);
		
		store.setDefault(R.TS_OPERATORS_SUB_RELATIONAL_USE, R.TS_OTHER_OPERATORS_ROOT);
		PreferenceConverter.setDefault(store, R.TS_OPERATORS_SUB_RELATIONAL_COLOR, new RGB(159, 63, 127));
		store.setDefault(R.TS_OPERATORS_SUB_RELATIONAL_BOLD, false);
		store.setDefault(R.TS_OPERATORS_SUB_RELATIONAL_ITALIC, false);
		store.setDefault(R.TS_OPERATORS_SUB_RELATIONAL_UNDERLINE, false);
		store.setDefault(R.TS_OPERATORS_SUB_RELATIONAL_STRIKETHROUGH, false);
		
		store.setDefault(R.TS_OPERATORS_SUB_USERDEFINED_USE, R.TS_OTHER_OPERATORS_ROOT);
		PreferenceConverter.setDefault(store, R.TS_OPERATORS_SUB_USERDEFINED_COLOR, new RGB(159, 63, 127));
		store.setDefault(R.TS_OPERATORS_SUB_USERDEFINED_BOLD, false);
		store.setDefault(R.TS_OPERATORS_SUB_USERDEFINED_ITALIC, false);
		store.setDefault(R.TS_OPERATORS_SUB_USERDEFINED_UNDERLINE, false);
		store.setDefault(R.TS_OPERATORS_SUB_USERDEFINED_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, R.TS_GROUPING_COLOR, new RGB(0, 0, 0));
		store.setDefault(R.TS_GROUPING_BOLD, false);
		store.setDefault(R.TS_GROUPING_ITALIC, false);
		store.setDefault(R.TS_GROUPING_UNDERLINE, false);
		store.setDefault(R.TS_GROUPING_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, R.TS_INDEXING_COLOR, new RGB(63, 95, 95));
		store.setDefault(R.TS_INDEXING_BOLD, false);
		store.setDefault(R.TS_INDEXING_ITALIC, false);
		store.setDefault(R.TS_INDEXING_UNDERLINE, false);
		store.setDefault(R.TS_INDEXING_STRIKETHROUGH, false);
		
		
		// RdEditorPreferences
		PreferenceConverter.setDefault(store, Rd.TS_DEFAULT_COLOR, new RGB(0, 0, 0));
		store.setDefault(Rd.TS_DEFAULT_BOLD, false);
		store.setDefault(Rd.TS_DEFAULT_ITALIC, true);
		store.setDefault(Rd.TS_DEFAULT_UNDERLINE, false);
		store.setDefault(Rd.TS_DEFAULT_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, Rd.TS_VERBATIM_COLOR, new RGB(31, 31, 31));
		store.setDefault(Rd.TS_VERBATIM_BOLD, false);
		store.setDefault(Rd.TS_VERBATIM_ITALIC, false);
		store.setDefault(Rd.TS_VERBATIM_UNDERLINE, false);
		store.setDefault(Rd.TS_VERBATIM_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, Rd.TS_COMMENT_COLOR, new RGB(63, 127, 95));
		store.setDefault(Rd.TS_COMMENT_BOLD, false);
		store.setDefault(Rd.TS_COMMENT_ITALIC, false);
		store.setDefault(Rd.TS_COMMENT_UNDERLINE, false);
		store.setDefault(Rd.TS_COMMENT_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, Rd.TS_TASK_TAG_COLOR, new RGB(63, 127, 95));
		store.setDefault(Rd.TS_TASK_TAG_BOLD, true);
		store.setDefault(Rd.TS_TASK_TAG_ITALIC, false);
		store.setDefault(Rd.TS_TASK_TAG_UNDERLINE, false);
		store.setDefault(Rd.TS_TASK_TAG_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, Rd.TS_PLATFORM_SPECIF_COLOR, new RGB(95, 95, 255));
		store.setDefault(Rd.TS_PLATFORM_SPECIF_BOLD, false);
		store.setDefault(Rd.TS_PLATFORM_SPECIF_ITALIC, false);
		store.setDefault(Rd.TS_PLATFORM_SPECIF_UNDERLINE, false);
		store.setDefault(Rd.TS_PLATFORM_SPECIF_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, Rd.TS_SECTION_TAG_COLOR, new RGB(127, 0, 95));
		store.setDefault(Rd.TS_SECTION_TAG_BOLD, true);
		store.setDefault(Rd.TS_SECTION_TAG_ITALIC, false);
		store.setDefault(Rd.TS_SECTION_TAG_UNDERLINE, false);
		store.setDefault(Rd.TS_SECTION_TAG_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, Rd.TS_SUBSECTION_TAG_COLOR, new RGB(127, 0, 95));
		store.setDefault(Rd.TS_SUBSECTION_TAG_BOLD, false);
		store.setDefault(Rd.TS_SUBSECTION_TAG_ITALIC, false);
		store.setDefault(Rd.TS_SUBSECTION_TAG_UNDERLINE, false);
		store.setDefault(Rd.TS_SUBSECTION_TAG_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, Rd.TS_OTHER_TAG_COLOR, new RGB(63, 0, 127));
		store.setDefault(Rd.TS_OTHER_TAG_BOLD, false);
		store.setDefault(Rd.TS_OTHER_TAG_ITALIC, true);
		store.setDefault(Rd.TS_OTHER_TAG_UNDERLINE, false);
		store.setDefault(Rd.TS_OTHER_TAG_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, Rd.TS_UNLISTED_TAG_COLOR, new RGB(95, 95, 127));
		store.setDefault(Rd.TS_UNLISTED_TAG_BOLD, false);
		store.setDefault(Rd.TS_UNLISTED_TAG_ITALIC, true);
		store.setDefault(Rd.TS_UNLISTED_TAG_UNDERLINE, false);
		store.setDefault(Rd.TS_UNLISTED_TAG_STRIKETHROUGH, false);
		
		PreferenceConverter.setDefault(store, Rd.TS_BRACKETS_COLOR, new RGB(0, 0, 0));
		store.setDefault(Rd.TS_BRACKETS_BOLD, false);
		store.setDefault(Rd.TS_BRACKETS_ITALIC, false);
		store.setDefault(Rd.TS_BRACKETS_UNDERLINE, false);
		store.setDefault(Rd.TS_BRACKETS_STRIKETHROUGH, false);
		
	}
	
	private RUIPreferenceConstants() {
	}
	
}
