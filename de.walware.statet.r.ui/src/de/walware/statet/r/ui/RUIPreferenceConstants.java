/*******************************************************************************
 * Copyright (c) 2005-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

import de.walware.ecommons.text.ui.presentation.ITextPresentationConstants;

import de.walware.workbench.ui.IWaThemeConstants;
import de.walware.workbench.ui.util.ThemeUtil;

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
		
		public final static String TS_ROXYGEN_ROOT = IRTextTokens.ROXYGEN_KEY;
		public final static String TS_ROXYGEN_COLOR = TS_ROXYGEN_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_ROXYGEN_BOLD = TS_ROXYGEN_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_ROXYGEN_ITALIC = TS_ROXYGEN_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_ROXYGEN_UNDERLINE = TS_ROXYGEN_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_ROXYGEN_STRIKETHROUGH = TS_ROXYGEN_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_ROXYGEN_TAG_ROOT = IRTextTokens.ROXYGEN_TAG_KEY;
		public final static String TS_ROXYGEN_TAG_COLOR = TS_ROXYGEN_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
		public final static String TS_ROXYGEN_TAG_BOLD = TS_ROXYGEN_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
		public final static String TS_ROXYGEN_TAG_ITALIC = TS_ROXYGEN_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
		public final static String TS_ROXYGEN_TAG_UNDERLINE = TS_ROXYGEN_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
		public final static String TS_ROXYGEN_TAG_STRIKETHROUGH = TS_ROXYGEN_TAG_ROOT + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
		
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
	 * @param scope the preference scope to be initialized
	 */
	public static void initializeDefaultValues(final IScopeContext scope) {
		final IEclipsePreferences pref = scope.getNode(RUI.PLUGIN_ID);
		final ThemeUtil theme = new ThemeUtil();
		
		final String defaultColor = theme.getColorPrefValue(IWaThemeConstants.CODE_DEFAULT_COLOR);
		final String commentColor = theme.getColorPrefValue(IWaThemeConstants.CODE_COMMENT_COLOR);
		final String commentTasktagColor = theme.getColorPrefValue(IWaThemeConstants.CODE_COMMENT_TASKTAG_COLOR);
		final String keywordColor = theme.getColorPrefValue(IWaThemeConstants.CODE_KEYWORD_COLOR);
		String color;
		
		pref.put(R.TS_DEFAULT_COLOR, defaultColor);
		pref.putBoolean(R.TS_DEFAULT_BOLD, false);
		pref.putBoolean(R.TS_DEFAULT_ITALIC, false);
		pref.putBoolean(R.TS_DEFAULT_UNDERLINE, false);
		pref.putBoolean(R.TS_DEFAULT_STRIKETHROUGH, false);
		
		final String[] identifierSubs = new String[] {
				R.TS_IDENTIFIER_SUB_ASSIGNMENT_ROOT,
				R.TS_IDENTIFIER_SUB_LOGICAL_ROOT,
				R.TS_IDENTIFIER_SUB_FLOWCONTROL_ROOT,
				R.TS_IDENTIFIER_SUB_CUSTOM1_ROOT,
				R.TS_IDENTIFIER_SUB_CUSTOM2_ROOT,
		};
		for (final String root : identifierSubs) {
			pref.put(root + ITextPresentationConstants.TEXTSTYLE_USE_SUFFIX, R.TS_DEFAULT_ROOT);
			pref.put(root + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX, defaultColor);
			pref.putBoolean(root + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX, false);
			pref.putBoolean(root + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX, false);
			pref.putBoolean(root + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX, false);
			pref.putBoolean(root + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		}
		pref.put(R.TS_IDENTIFIER_SUB_ASSIGNMENT_ITEMS, "assign,rm,remove,setMethod,setGeneric,setGroupGeneric,setClass,setClassUnion,setIs,setAs,setValidity,removeClass,removeGeneric,removeMethod,removeMethods,attach,detach,source"); //$NON-NLS-1$
		pref.put(R.TS_IDENTIFIER_SUB_LOGICAL_ITEMS, "xor,any,all"); //$NON-NLS-1$
		pref.put(R.TS_IDENTIFIER_SUB_FLOWCONTROL_ITEMS, "return,switch,ifelse,stop,warning,try,tryCatch"); //$NON-NLS-1$
		pref.put(R.TS_IDENTIFIER_SUB_CUSTOM1_ITEMS, ""); //$NON-NLS-1$
		pref.put(R.TS_IDENTIFIER_SUB_CUSTOM2_ITEMS, ""); //$NON-NLS-1$
		
		pref.put(R.TS_UNDEFINED_COLOR, theme.getColorPrefValue(IWaThemeConstants.CODE_UNDEFINED_COLOR));
		pref.putBoolean(R.TS_UNDEFINED_BOLD, true);
		pref.putBoolean(R.TS_UNDEFINED_ITALIC, false);
		pref.putBoolean(R.TS_UNDEFINED_UNDERLINE, false);
		pref.putBoolean(R.TS_UNDEFINED_STRIKETHROUGH, false);
		
		pref.put(R.TS_COMMENT_COLOR, commentColor);
		pref.putBoolean(R.TS_COMMENT_BOLD, false);
		pref.putBoolean(R.TS_COMMENT_ITALIC, false);
		pref.putBoolean(R.TS_COMMENT_UNDERLINE, false);
		pref.putBoolean(R.TS_COMMENT_STRIKETHROUGH, false);
		
		pref.put(R.TS_TASK_TAG_COLOR, commentTasktagColor);
		pref.putBoolean(R.TS_TASK_TAG_BOLD, true);
		pref.putBoolean(R.TS_TASK_TAG_ITALIC, false);
		pref.putBoolean(R.TS_TASK_TAG_UNDERLINE, false);
		pref.putBoolean(R.TS_TASK_TAG_STRIKETHROUGH, false);
		
		pref.put(R.TS_ROXYGEN_COLOR, theme.getColorPrefValue(IWaThemeConstants.CODE_DOCU_COLOR));
		pref.putBoolean(R.TS_ROXYGEN_BOLD, false);
		pref.putBoolean(R.TS_ROXYGEN_ITALIC, false);
		pref.putBoolean(R.TS_ROXYGEN_UNDERLINE, false);
		pref.putBoolean(R.TS_ROXYGEN_STRIKETHROUGH, false);
		
		pref.put(R.TS_ROXYGEN_TAG_COLOR, theme.getColorPrefValue(IWaThemeConstants.CODE_DOCU_TAG_COLOR));
		pref.putBoolean(R.TS_ROXYGEN_TAG_BOLD, true);
		pref.putBoolean(R.TS_ROXYGEN_TAG_ITALIC, false);
		pref.putBoolean(R.TS_ROXYGEN_TAG_UNDERLINE, false);
		pref.putBoolean(R.TS_ROXYGEN_TAG_STRIKETHROUGH, false);
		
		pref.put(R.TS_STRING_COLOR, theme.getColorPrefValue(IWaThemeConstants.CODE_STRING_COLOR));
		pref.putBoolean(R.TS_STRING_BOLD, false);
		pref.putBoolean(R.TS_STRING_ITALIC, false);
		pref.putBoolean(R.TS_STRING_UNDERLINE, false);
		pref.putBoolean(R.TS_STRING_STRIKETHROUGH, false);
		
		color = theme.getColorPrefValue(IWaThemeConstants.CODE_NUMBER_COLOR);
		pref.put(R.TS_NUMBERS_COLOR, color);
		pref.putBoolean(R.TS_NUMBERS_BOLD, false);
		pref.putBoolean(R.TS_NUMBERS_ITALIC, false);
		pref.putBoolean(R.TS_NUMBERS_UNDERLINE, false);
		pref.putBoolean(R.TS_NUMBERS_STRIKETHROUGH, false);
		
		final String[] numberSubs = new String[] {
				R.TS_NUMBERS_SUB_INT_ROOT,
				R.TS_NUMBERS_SUB_CPLX_ROOT,
		};
		for (final String root : numberSubs) {
			pref.put(root + ITextPresentationConstants.TEXTSTYLE_USE_SUFFIX, R.TS_NUMBERS_ROOT);
			pref.put(root + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX, color);
			pref.putBoolean(root + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX, false);
			pref.putBoolean(root + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX, false);
			pref.putBoolean(root + ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX, false);
			pref.putBoolean(root + ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		}
		
		color = theme.getColorPrefValue(IWaThemeConstants.CODE_CONST_COLOR);
		pref.put(R.TS_SPECIAL_CONSTANTS_COLOR, color);
		pref.putBoolean(R.TS_SPECIAL_CONSTANTS_BOLD, false);
		pref.putBoolean(R.TS_SPECIAL_CONSTANTS_ITALIC, false);
		pref.putBoolean(R.TS_SPECIAL_CONSTANTS_UNDERLINE, false);
		pref.putBoolean(R.TS_SPECIAL_CONSTANTS_STRIKETHROUGH, false);
		
		pref.put(R.TS_LOGICAL_CONSTANTS_COLOR, color);
		pref.putBoolean(R.TS_LOGICAL_CONSTANTS_BOLD, false);
		pref.putBoolean(R.TS_LOGICAL_CONSTANTS_ITALIC, false);
		pref.putBoolean(R.TS_LOGICAL_CONSTANTS_UNDERLINE, false);
		pref.putBoolean(R.TS_LOGICAL_CONSTANTS_STRIKETHROUGH, false);
		
		pref.put(R.TS_FLOWCONTROL_COLOR, keywordColor);
		pref.putBoolean(R.TS_FLOWCONTROL_BOLD, true);
		pref.putBoolean(R.TS_FLOWCONTROL_ITALIC, false);
		pref.putBoolean(R.TS_FLOWCONTROL_UNDERLINE, false);
		pref.putBoolean(R.TS_FLOWCONTROL_STRIKETHROUGH, false);
		
		pref.put(R.TS_SEPARATORS_COLOR, defaultColor);
		pref.putBoolean(R.TS_SEPARATORS_BOLD, false);
		pref.putBoolean(R.TS_SEPARATORS_ITALIC, false);
		pref.putBoolean(R.TS_SEPARATORS_UNDERLINE, false);
		pref.putBoolean(R.TS_SEPARATORS_STRIKETHROUGH, false);
		
		pref.put(R.TS_ASSIGNMENT_COLOR, defaultColor);
		pref.putBoolean(R.TS_ASSIGNMENT_BOLD, true);
		pref.putBoolean(R.TS_ASSIGNMENT_ITALIC, false);
		pref.putBoolean(R.TS_ASSIGNMENT_UNDERLINE, false);
		pref.putBoolean(R.TS_ASSIGNMENT_STRIKETHROUGH, false);
		
		pref.put(R.TS_ASSIGNMENT_SUB_EQUALSIGN_USE, ""); //$NON-NLS-1$
		pref.put(R.TS_ASSIGNMENT_SUB_EQUALSIGN_COLOR, defaultColor);
		pref.putBoolean(R.TS_ASSIGNMENT_SUB_EQUALSIGN_BOLD, false);
		pref.putBoolean(R.TS_ASSIGNMENT_SUB_EQUALSIGN_ITALIC, false);
		pref.putBoolean(R.TS_ASSIGNMENT_SUB_EQUALSIGN_UNDERLINE, false);
		pref.putBoolean(R.TS_ASSIGNMENT_SUB_EQUALSIGN_STRIKETHROUGH, false);
		
		color = theme.getColorPrefValue(IWaThemeConstants.CODE_OPERATOR_COLOR);
		pref.put(R.TS_OTHER_OPERATORS_COLOR, color);
		pref.putBoolean(R.TS_OTHER_OPERATORS_BOLD, false);
		pref.putBoolean(R.TS_OTHER_OPERATORS_ITALIC, false);
		pref.putBoolean(R.TS_OTHER_OPERATORS_UNDERLINE, false);
		pref.putBoolean(R.TS_OTHER_OPERATORS_STRIKETHROUGH, false);
		
		pref.put(R.TS_OPERATORS_SUB_LOGICAL_USE, R.TS_OTHER_OPERATORS_ROOT);
		pref.put(R.TS_OPERATORS_SUB_LOGICAL_COLOR, color);
		pref.putBoolean(R.TS_OPERATORS_SUB_LOGICAL_BOLD, false);
		pref.putBoolean(R.TS_OPERATORS_SUB_LOGICAL_ITALIC, false);
		pref.putBoolean(R.TS_OPERATORS_SUB_LOGICAL_UNDERLINE, false);
		pref.putBoolean(R.TS_OPERATORS_SUB_LOGICAL_STRIKETHROUGH, false);
		
		pref.put(R.TS_OPERATORS_SUB_RELATIONAL_USE, R.TS_OTHER_OPERATORS_ROOT);
		pref.put(R.TS_OPERATORS_SUB_RELATIONAL_COLOR, color);
		pref.putBoolean(R.TS_OPERATORS_SUB_RELATIONAL_BOLD, false);
		pref.putBoolean(R.TS_OPERATORS_SUB_RELATIONAL_ITALIC, false);
		pref.putBoolean(R.TS_OPERATORS_SUB_RELATIONAL_UNDERLINE, false);
		pref.putBoolean(R.TS_OPERATORS_SUB_RELATIONAL_STRIKETHROUGH, false);
		
		pref.put(R.TS_OPERATORS_SUB_USERDEFINED_USE, R.TS_OTHER_OPERATORS_ROOT);
		pref.put(R.TS_OPERATORS_SUB_USERDEFINED_COLOR, color);
		pref.putBoolean(R.TS_OPERATORS_SUB_USERDEFINED_BOLD, false);
		pref.putBoolean(R.TS_OPERATORS_SUB_USERDEFINED_ITALIC, false);
		pref.putBoolean(R.TS_OPERATORS_SUB_USERDEFINED_UNDERLINE, false);
		pref.putBoolean(R.TS_OPERATORS_SUB_USERDEFINED_STRIKETHROUGH, false);
		
		pref.put(R.TS_GROUPING_COLOR, defaultColor);
		pref.putBoolean(R.TS_GROUPING_BOLD, false);
		pref.putBoolean(R.TS_GROUPING_ITALIC, false);
		pref.putBoolean(R.TS_GROUPING_UNDERLINE, false);
		pref.putBoolean(R.TS_GROUPING_STRIKETHROUGH, false);
		
		pref.put(R.TS_INDEXING_COLOR, theme.getColorPrefValue(IWaThemeConstants.CODE_SUB_COLOR));
		pref.putBoolean(R.TS_INDEXING_BOLD, false);
		pref.putBoolean(R.TS_INDEXING_ITALIC, false);
		pref.putBoolean(R.TS_INDEXING_UNDERLINE, false);
		pref.putBoolean(R.TS_INDEXING_STRIKETHROUGH, false);
		
		
		// RdEditorPreferences
		pref.put(Rd.TS_DEFAULT_COLOR, defaultColor);
		pref.putBoolean(Rd.TS_DEFAULT_BOLD, false);
		pref.putBoolean(Rd.TS_DEFAULT_ITALIC, true);
		pref.putBoolean(Rd.TS_DEFAULT_UNDERLINE, false);
		pref.putBoolean(Rd.TS_DEFAULT_STRIKETHROUGH, false);
		
		pref.put(Rd.TS_VERBATIM_COLOR, theme.getColorPrefValue(IWaThemeConstants.CODE_VERBATIM_COLOR));
		pref.putBoolean(Rd.TS_VERBATIM_BOLD, false);
		pref.putBoolean(Rd.TS_VERBATIM_ITALIC, false);
		pref.putBoolean(Rd.TS_VERBATIM_UNDERLINE, false);
		pref.putBoolean(Rd.TS_VERBATIM_STRIKETHROUGH, false);
		
		pref.put(Rd.TS_COMMENT_COLOR, commentColor);
		pref.putBoolean(Rd.TS_COMMENT_BOLD, false);
		pref.putBoolean(Rd.TS_COMMENT_ITALIC, false);
		pref.putBoolean(Rd.TS_COMMENT_UNDERLINE, false);
		pref.putBoolean(Rd.TS_COMMENT_STRIKETHROUGH, false);
		
		pref.put(Rd.TS_TASK_TAG_COLOR, commentTasktagColor);
		pref.putBoolean(Rd.TS_TASK_TAG_BOLD, true);
		pref.putBoolean(Rd.TS_TASK_TAG_ITALIC, false);
		pref.putBoolean(Rd.TS_TASK_TAG_UNDERLINE, false);
		pref.putBoolean(Rd.TS_TASK_TAG_STRIKETHROUGH, false);
		
		pref.put(Rd.TS_PLATFORM_SPECIF_COLOR, theme.getColorPrefValue(IWaThemeConstants.CODE_PREPROCESSOR_COLOR));
		pref.putBoolean(Rd.TS_PLATFORM_SPECIF_BOLD, false);
		pref.putBoolean(Rd.TS_PLATFORM_SPECIF_ITALIC, false);
		pref.putBoolean(Rd.TS_PLATFORM_SPECIF_UNDERLINE, false);
		pref.putBoolean(Rd.TS_PLATFORM_SPECIF_STRIKETHROUGH, false);
		
		color = theme.getColorPrefValue(IWaThemeConstants.CODE_DOC_COMMAND_COLOR);
		pref.put(Rd.TS_SECTION_TAG_COLOR, color);
		pref.putBoolean(Rd.TS_SECTION_TAG_BOLD, true);
		pref.putBoolean(Rd.TS_SECTION_TAG_ITALIC, false);
		pref.putBoolean(Rd.TS_SECTION_TAG_UNDERLINE, false);
		pref.putBoolean(Rd.TS_SECTION_TAG_STRIKETHROUGH, false);
		
		pref.put(Rd.TS_SUBSECTION_TAG_COLOR, color);
		pref.putBoolean(Rd.TS_SUBSECTION_TAG_BOLD, false);
		pref.putBoolean(Rd.TS_SUBSECTION_TAG_ITALIC, false);
		pref.putBoolean(Rd.TS_SUBSECTION_TAG_UNDERLINE, false);
		pref.putBoolean(Rd.TS_SUBSECTION_TAG_STRIKETHROUGH, false);
		
		pref.put(Rd.TS_OTHER_TAG_COLOR, theme.getColorPrefValue(IWaThemeConstants.CODE_DOC_COMMAND_SPECIAL_COLOR));
		pref.putBoolean(Rd.TS_OTHER_TAG_BOLD, false);
		pref.putBoolean(Rd.TS_OTHER_TAG_ITALIC, true);
		pref.putBoolean(Rd.TS_OTHER_TAG_UNDERLINE, false);
		pref.putBoolean(Rd.TS_OTHER_TAG_STRIKETHROUGH, false);
		
		pref.put(Rd.TS_UNLISTED_TAG_COLOR, theme.getColorPrefValue(IWaThemeConstants.CODE_DOC_2ND_COMMAND_COLOR));
		pref.putBoolean(Rd.TS_UNLISTED_TAG_BOLD, false);
		pref.putBoolean(Rd.TS_UNLISTED_TAG_ITALIC, true);
		pref.putBoolean(Rd.TS_UNLISTED_TAG_UNDERLINE, false);
		pref.putBoolean(Rd.TS_UNLISTED_TAG_STRIKETHROUGH, false);
		
		pref.put(Rd.TS_BRACKETS_COLOR, defaultColor);
		pref.putBoolean(Rd.TS_BRACKETS_BOLD, false);
		pref.putBoolean(Rd.TS_BRACKETS_ITALIC, false);
		pref.putBoolean(Rd.TS_BRACKETS_UNDERLINE, false);
		pref.putBoolean(Rd.TS_BRACKETS_STRIKETHROUGH, false);
		
	}
	
}
