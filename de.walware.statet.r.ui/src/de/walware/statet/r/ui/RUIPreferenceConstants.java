/*******************************************************************************
 * Copyright (c) 2005-2013 Stephan Wahlbrink (www.walware.de/goto/opensource)
 * and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui;

import static de.walware.ecommons.text.ui.presentation.ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX;
import static de.walware.ecommons.text.ui.presentation.ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX;
import static de.walware.ecommons.text.ui.presentation.ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX;
import static de.walware.ecommons.text.ui.presentation.ITextPresentationConstants.TEXTSTYLE_STRIKETHROUGH_SUFFIX;
import static de.walware.ecommons.text.ui.presentation.ITextPresentationConstants.TEXTSTYLE_UNDERLINE_SUFFIX;
import static de.walware.ecommons.text.ui.presentation.ITextPresentationConstants.TEXTSTYLE_USE_SUFFIX;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

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
		
		String TS_GROUP_ID = "r.editor/textstyles"; //$NON-NLS-1$
		String TS_ROOT = IRTextTokens.ROOT;
		String TS_ITEMS_SUFFIX = ".items"; //$NON-NLS-1$
		
		String TS_DEFAULT_ROOT = IRTextTokens.SYMBOL_KEY;
		
		String TS_IDENTIFIER_SUB_ASSIGNMENT_ROOT = IRTextTokens.SYMBOL_SUB_ASSIGN_KEY;
		String TS_IDENTIFIER_SUB_ASSIGNMENT_ITEMS = TS_IDENTIFIER_SUB_ASSIGNMENT_ROOT + TS_ITEMS_SUFFIX;
		String TS_IDENTIFIER_SUB_LOGICAL_ROOT = IRTextTokens.SYMBOL_SUB_LOGICAL_KEY;
		String TS_IDENTIFIER_SUB_LOGICAL_ITEMS = TS_IDENTIFIER_SUB_LOGICAL_ROOT + TS_ITEMS_SUFFIX;
		String TS_IDENTIFIER_SUB_FLOWCONTROL_ROOT = IRTextTokens.SYMBOL_SUB_FLOWCONTROL_KEY;
		String TS_IDENTIFIER_SUB_FLOWCONTROL_ITEMS = TS_IDENTIFIER_SUB_FLOWCONTROL_ROOT + TS_ITEMS_SUFFIX;
		String TS_IDENTIFIER_SUB_CUSTOM1_ROOT = IRTextTokens.SYMBOL_SUB_CUSTOM1_KEY;
		String TS_IDENTIFIER_SUB_CUSTOM1_ITEMS = TS_IDENTIFIER_SUB_CUSTOM1_ROOT + TS_ITEMS_SUFFIX;
		String TS_IDENTIFIER_SUB_CUSTOM2_ROOT = IRTextTokens.SYMBOL_SUB_CUSTOM2_KEY;
		String TS_IDENTIFIER_SUB_CUSTOM2_ITEMS = TS_IDENTIFIER_SUB_CUSTOM2_ROOT + TS_ITEMS_SUFFIX;
		
		String TS_UNDEFINED_ROOT = IRTextTokens.UNDEFINED_KEY;
		
		String TS_COMMENT_ROOT = IRTextTokens.COMMENT_KEY;
		String TS_TASK_TAG_ROOT = IRTextTokens.TASK_TAG_KEY;
		
		String TS_ROXYGEN_ROOT = IRTextTokens.ROXYGEN_KEY;
		String TS_ROXYGEN_TAG_ROOT = IRTextTokens.ROXYGEN_TAG_KEY;
		
		String TS_STRING_ROOT = IRTextTokens.STRING_KEY;
		
		String TS_NUMBERS_ROOT = IRTextTokens.NUM_KEY;
		String TS_NUMBERS_SUB_INT_ROOT = IRTextTokens.NUM_SUB_INT_KEY;
		String TS_NUMBERS_SUB_CPLX_ROOT = IRTextTokens.NUM_SUB_CPLX_KEY;
		
		String TS_SPECIAL_CONSTANTS_ROOT = IRTextTokens.SPECIALCONST_KEY;
		String TS_LOGICAL_CONSTANTS_ROOT = IRTextTokens.LOGICALCONST_KEY;
		String TS_FLOWCONTROL_ROOT = IRTextTokens.FLOWCONTROL_KEY;
		
		String TS_SEPARATORS_ROOT = IRTextTokens.SEPARATOR_KEY;
		String TS_ASSIGNMENT_ROOT = IRTextTokens.ASSIGN_KEY;
		String TS_ASSIGNMENT_SUB_EQUALSIGN_ROOT = IRTextTokens.ASSIGN_SUB_EQUAL_KEY;
		String TS_OTHER_OPERATORS_ROOT = IRTextTokens.OP_KEY;
		String TS_OPERATORS_SUB_LOGICAL_ROOT = IRTextTokens.OP_SUB_LOGICAL_KEY;
		String TS_OPERATORS_SUB_RELATIONAL_ROOT = IRTextTokens.OP_SUB_RELATIONAL_KEY;
		String TS_OPERATORS_SUB_USERDEFINED_ROOT = IRTextTokens.OP_SUB_USERDEFINED_KEY;
		String TS_GROUPING_ROOT = IRTextTokens.GROUPING_KEY;
		String TS_INDEXING_ROOT = IRTextTokens.SUBACCESS_KEY;
		
	}
	
	public interface Rd {
		
		String TS_GROUP_ID = "rd.editor/textstyles"; //$NON-NLS-1$
		String TS_ROOT = IRdTextTokens.ROOT;
		
		String TS_BRACKETS_ROOT = IRdTextTokens.BRACKETS;
		String TS_COMMENT_ROOT = IRdTextTokens.COMMENT;
		String TS_DEFAULT_ROOT = IRdTextTokens.DEFAULT;
		String TS_OTHER_TAG_ROOT = IRdTextTokens.OTHER_TAG;
		String TS_PLATFORM_SPECIF_ROOT = IRdTextTokens.PLATFORM_SPECIF;
		String TS_SECTION_TAG_ROOT = IRdTextTokens.SECTION_TAG;
		String TS_SUBSECTION_TAG_ROOT = IRdTextTokens.SUBSECTION_TAG;
		String TS_TASK_TAG_ROOT = IRdTextTokens.TASK_TAG;
		String TS_UNLISTED_TAG_ROOT = IRdTextTokens.UNLISTED_TAG;
		String TS_VERBATIM_ROOT = IRdTextTokens.VERBATIM;
		
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
		
		pref.put(R.TS_DEFAULT_ROOT + TEXTSTYLE_COLOR_SUFFIX, defaultColor);
		pref.putBoolean(R.TS_DEFAULT_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(R.TS_DEFAULT_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_DEFAULT_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_DEFAULT_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		final String[] identifierSubs = new String[] {
				R.TS_IDENTIFIER_SUB_ASSIGNMENT_ROOT,
				R.TS_IDENTIFIER_SUB_LOGICAL_ROOT,
				R.TS_IDENTIFIER_SUB_FLOWCONTROL_ROOT,
				R.TS_IDENTIFIER_SUB_CUSTOM1_ROOT,
				R.TS_IDENTIFIER_SUB_CUSTOM2_ROOT,
		};
		for (final String root : identifierSubs) {
			pref.put(root + TEXTSTYLE_USE_SUFFIX, R.TS_DEFAULT_ROOT);
			pref.put(root + TEXTSTYLE_COLOR_SUFFIX, defaultColor);
			pref.putBoolean(root + TEXTSTYLE_BOLD_SUFFIX, false);
			pref.putBoolean(root + TEXTSTYLE_ITALIC_SUFFIX, false);
			pref.putBoolean(root + TEXTSTYLE_UNDERLINE_SUFFIX, false);
			pref.putBoolean(root + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		}
		pref.put(R.TS_IDENTIFIER_SUB_ASSIGNMENT_ITEMS, "assign,rm,remove,setMethod,setGeneric,setGroupGeneric,setClass,setClassUnion,setIs,setAs,setValidity,removeClass,removeGeneric,removeMethod,removeMethods,attach,detach,source"); //$NON-NLS-1$
		pref.put(R.TS_IDENTIFIER_SUB_LOGICAL_ITEMS, "xor,any,all"); //$NON-NLS-1$
		pref.put(R.TS_IDENTIFIER_SUB_FLOWCONTROL_ITEMS, "return,switch,ifelse,stop,warning,try,tryCatch"); //$NON-NLS-1$
		pref.put(R.TS_IDENTIFIER_SUB_CUSTOM1_ITEMS, ""); //$NON-NLS-1$
		pref.put(R.TS_IDENTIFIER_SUB_CUSTOM2_ITEMS, ""); //$NON-NLS-1$
		
		pref.put(R.TS_UNDEFINED_ROOT + TEXTSTYLE_COLOR_SUFFIX,
				theme.getColorPrefValue(IWaThemeConstants.CODE_UNDEFINED_COLOR) );
		pref.putBoolean(R.TS_UNDEFINED_ROOT + TEXTSTYLE_BOLD_SUFFIX, true);
		pref.putBoolean(R.TS_UNDEFINED_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_UNDEFINED_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_UNDEFINED_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(R.TS_COMMENT_ROOT + TEXTSTYLE_COLOR_SUFFIX, commentColor);
		pref.putBoolean(R.TS_COMMENT_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(R.TS_COMMENT_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_COMMENT_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_COMMENT_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(R.TS_TASK_TAG_ROOT + TEXTSTYLE_COLOR_SUFFIX, commentTasktagColor);
		pref.putBoolean(R.TS_TASK_TAG_ROOT + TEXTSTYLE_BOLD_SUFFIX, true);
		pref.putBoolean(R.TS_TASK_TAG_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_TASK_TAG_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_TASK_TAG_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(R.TS_ROXYGEN_ROOT + TEXTSTYLE_COLOR_SUFFIX,
				theme.getColorPrefValue(IWaThemeConstants.CODE_DOCU_COLOR) );
		pref.putBoolean(R.TS_ROXYGEN_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(R.TS_ROXYGEN_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_ROXYGEN_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_ROXYGEN_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(R.TS_ROXYGEN_TAG_ROOT + TEXTSTYLE_COLOR_SUFFIX,
				theme.getColorPrefValue(IWaThemeConstants.CODE_DOCU_TAG_COLOR) );
		pref.putBoolean(R.TS_ROXYGEN_TAG_ROOT + TEXTSTYLE_BOLD_SUFFIX, true);
		pref.putBoolean(R.TS_ROXYGEN_TAG_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_ROXYGEN_TAG_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_ROXYGEN_TAG_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(R.TS_STRING_ROOT + TEXTSTYLE_COLOR_SUFFIX,
				theme.getColorPrefValue(IWaThemeConstants.CODE_STRING_COLOR) );
		pref.putBoolean(R.TS_STRING_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(R.TS_STRING_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_STRING_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_STRING_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		color = theme.getColorPrefValue(IWaThemeConstants.CODE_NUMBER_COLOR);
		pref.put(R.TS_NUMBERS_ROOT + TEXTSTYLE_COLOR_SUFFIX, color);
		pref.putBoolean(R.TS_NUMBERS_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(R.TS_NUMBERS_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_NUMBERS_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_NUMBERS_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		final String[] numberSubs = new String[] {
				R.TS_NUMBERS_SUB_INT_ROOT,
				R.TS_NUMBERS_SUB_CPLX_ROOT,
		};
		for (final String root : numberSubs) {
			pref.put(root + TEXTSTYLE_USE_SUFFIX, R.TS_NUMBERS_ROOT);
			pref.put(root + TEXTSTYLE_COLOR_SUFFIX, color);
			pref.putBoolean(root + TEXTSTYLE_BOLD_SUFFIX, false);
			pref.putBoolean(root + TEXTSTYLE_ITALIC_SUFFIX, false);
			pref.putBoolean(root + TEXTSTYLE_UNDERLINE_SUFFIX, false);
			pref.putBoolean(root + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		}
		
		color = theme.getColorPrefValue(IWaThemeConstants.CODE_CONST_COLOR);
		pref.put(R.TS_SPECIAL_CONSTANTS_ROOT + TEXTSTYLE_COLOR_SUFFIX, color);
		pref.putBoolean(R.TS_SPECIAL_CONSTANTS_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(R.TS_SPECIAL_CONSTANTS_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_SPECIAL_CONSTANTS_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_SPECIAL_CONSTANTS_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(R.TS_LOGICAL_CONSTANTS_ROOT + TEXTSTYLE_COLOR_SUFFIX, color);
		pref.putBoolean(R.TS_LOGICAL_CONSTANTS_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(R.TS_LOGICAL_CONSTANTS_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_LOGICAL_CONSTANTS_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_LOGICAL_CONSTANTS_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(R.TS_FLOWCONTROL_ROOT + TEXTSTYLE_COLOR_SUFFIX, keywordColor);
		pref.putBoolean(R.TS_FLOWCONTROL_ROOT + TEXTSTYLE_BOLD_SUFFIX, true);
		pref.putBoolean(R.TS_FLOWCONTROL_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_FLOWCONTROL_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_FLOWCONTROL_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(R.TS_SEPARATORS_ROOT + TEXTSTYLE_COLOR_SUFFIX, defaultColor);
		pref.putBoolean(R.TS_SEPARATORS_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(R.TS_SEPARATORS_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_SEPARATORS_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_SEPARATORS_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(R.TS_ASSIGNMENT_ROOT + TEXTSTYLE_COLOR_SUFFIX, defaultColor);
		pref.putBoolean(R.TS_ASSIGNMENT_ROOT + TEXTSTYLE_BOLD_SUFFIX, true);
		pref.putBoolean(R.TS_ASSIGNMENT_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_ASSIGNMENT_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_ASSIGNMENT_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(R.TS_ASSIGNMENT_SUB_EQUALSIGN_ROOT + TEXTSTYLE_USE_SUFFIX, ""); //$NON-NLS-1$
		pref.put(R.TS_ASSIGNMENT_SUB_EQUALSIGN_ROOT + TEXTSTYLE_COLOR_SUFFIX, defaultColor);
		pref.putBoolean(R.TS_ASSIGNMENT_SUB_EQUALSIGN_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(R.TS_ASSIGNMENT_SUB_EQUALSIGN_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_ASSIGNMENT_SUB_EQUALSIGN_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_ASSIGNMENT_SUB_EQUALSIGN_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		color = theme.getColorPrefValue(IWaThemeConstants.CODE_OPERATOR_COLOR);
		pref.put(R.TS_OTHER_OPERATORS_ROOT + TEXTSTYLE_COLOR_SUFFIX, color);
		pref.putBoolean(R.TS_OTHER_OPERATORS_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(R.TS_OTHER_OPERATORS_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_OTHER_OPERATORS_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_OTHER_OPERATORS_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(R.TS_OPERATORS_SUB_LOGICAL_ROOT + TEXTSTYLE_USE_SUFFIX,
				R.TS_OTHER_OPERATORS_ROOT );
		pref.put(R.TS_OPERATORS_SUB_LOGICAL_ROOT + TEXTSTYLE_COLOR_SUFFIX, color);
		pref.putBoolean(R.TS_OPERATORS_SUB_LOGICAL_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(R.TS_OPERATORS_SUB_LOGICAL_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_OPERATORS_SUB_LOGICAL_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_OPERATORS_SUB_LOGICAL_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(R.TS_OPERATORS_SUB_RELATIONAL_ROOT + TEXTSTYLE_USE_SUFFIX,
				R.TS_OTHER_OPERATORS_ROOT );
		pref.put(R.TS_OPERATORS_SUB_RELATIONAL_ROOT + TEXTSTYLE_COLOR_SUFFIX, color);
		pref.putBoolean(R.TS_OPERATORS_SUB_RELATIONAL_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(R.TS_OPERATORS_SUB_RELATIONAL_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_OPERATORS_SUB_RELATIONAL_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_OPERATORS_SUB_RELATIONAL_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(R.TS_OPERATORS_SUB_USERDEFINED_ROOT + TEXTSTYLE_USE_SUFFIX,
				R.TS_OTHER_OPERATORS_ROOT );
		pref.put(R.TS_OPERATORS_SUB_USERDEFINED_ROOT + TEXTSTYLE_COLOR_SUFFIX, color);
		pref.putBoolean(R.TS_OPERATORS_SUB_USERDEFINED_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(R.TS_OPERATORS_SUB_USERDEFINED_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_OPERATORS_SUB_USERDEFINED_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_OPERATORS_SUB_USERDEFINED_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(R.TS_GROUPING_ROOT + TEXTSTYLE_COLOR_SUFFIX, defaultColor);
		pref.putBoolean(R.TS_GROUPING_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(R.TS_GROUPING_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_GROUPING_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_GROUPING_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(R.TS_INDEXING_ROOT + TEXTSTYLE_COLOR_SUFFIX,
				theme.getColorPrefValue(IWaThemeConstants.CODE_SUB_COLOR) );
		pref.putBoolean(R.TS_INDEXING_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(R.TS_INDEXING_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(R.TS_INDEXING_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(R.TS_INDEXING_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		
		// RdEditorPreferences
		pref.put(Rd.TS_DEFAULT_ROOT + TEXTSTYLE_COLOR_SUFFIX, defaultColor);
		pref.putBoolean(Rd.TS_DEFAULT_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(Rd.TS_DEFAULT_ROOT + TEXTSTYLE_ITALIC_SUFFIX, true);
		pref.putBoolean(Rd.TS_DEFAULT_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(Rd.TS_DEFAULT_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(Rd.TS_VERBATIM_ROOT + TEXTSTYLE_COLOR_SUFFIX,
				theme.getColorPrefValue(IWaThemeConstants.CODE_VERBATIM_COLOR) );
		pref.putBoolean(Rd.TS_VERBATIM_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(Rd.TS_VERBATIM_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(Rd.TS_VERBATIM_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(Rd.TS_VERBATIM_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(Rd.TS_COMMENT_ROOT + TEXTSTYLE_COLOR_SUFFIX, commentColor);
		pref.putBoolean(Rd.TS_COMMENT_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(Rd.TS_COMMENT_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(Rd.TS_COMMENT_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(Rd.TS_COMMENT_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(Rd.TS_TASK_TAG_ROOT + TEXTSTYLE_COLOR_SUFFIX, commentTasktagColor);
		pref.putBoolean(Rd.TS_TASK_TAG_ROOT + TEXTSTYLE_BOLD_SUFFIX, true);
		pref.putBoolean(Rd.TS_TASK_TAG_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(Rd.TS_TASK_TAG_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(Rd.TS_TASK_TAG_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(Rd.TS_PLATFORM_SPECIF_ROOT + TEXTSTYLE_COLOR_SUFFIX,
				theme.getColorPrefValue(IWaThemeConstants.CODE_PREPROCESSOR_COLOR) );
		pref.putBoolean(Rd.TS_PLATFORM_SPECIF_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(Rd.TS_PLATFORM_SPECIF_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(Rd.TS_PLATFORM_SPECIF_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(Rd.TS_PLATFORM_SPECIF_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		color = theme.getColorPrefValue(IWaThemeConstants.CODE_DOC_COMMAND_COLOR);
		pref.put(Rd.TS_SECTION_TAG_ROOT + TEXTSTYLE_COLOR_SUFFIX, color);
		pref.putBoolean(Rd.TS_SECTION_TAG_ROOT + TEXTSTYLE_BOLD_SUFFIX, true);
		pref.putBoolean(Rd.TS_SECTION_TAG_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(Rd.TS_SECTION_TAG_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(Rd.TS_SECTION_TAG_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(Rd.TS_SUBSECTION_TAG_ROOT + TEXTSTYLE_COLOR_SUFFIX, color);
		pref.putBoolean(Rd.TS_SUBSECTION_TAG_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(Rd.TS_SUBSECTION_TAG_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(Rd.TS_SUBSECTION_TAG_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(Rd.TS_SUBSECTION_TAG_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(Rd.TS_OTHER_TAG_ROOT + TEXTSTYLE_COLOR_SUFFIX,
				theme.getColorPrefValue(IWaThemeConstants.CODE_DOC_COMMAND_SPECIAL_COLOR) );
		pref.putBoolean(Rd.TS_OTHER_TAG_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(Rd.TS_OTHER_TAG_ROOT + TEXTSTYLE_ITALIC_SUFFIX, true);
		pref.putBoolean(Rd.TS_OTHER_TAG_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(Rd.TS_OTHER_TAG_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(Rd.TS_UNLISTED_TAG_ROOT + TEXTSTYLE_COLOR_SUFFIX,
				theme.getColorPrefValue(IWaThemeConstants.CODE_DOC_2ND_COMMAND_COLOR) );
		pref.putBoolean(Rd.TS_UNLISTED_TAG_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(Rd.TS_UNLISTED_TAG_ROOT + TEXTSTYLE_ITALIC_SUFFIX, true);
		pref.putBoolean(Rd.TS_UNLISTED_TAG_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(Rd.TS_UNLISTED_TAG_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
		
		pref.put(Rd.TS_BRACKETS_ROOT + TEXTSTYLE_COLOR_SUFFIX, defaultColor);
		pref.putBoolean(Rd.TS_BRACKETS_ROOT + TEXTSTYLE_BOLD_SUFFIX, false);
		pref.putBoolean(Rd.TS_BRACKETS_ROOT + TEXTSTYLE_ITALIC_SUFFIX, false);
		pref.putBoolean(Rd.TS_BRACKETS_ROOT + TEXTSTYLE_UNDERLINE_SUFFIX, false);
		pref.putBoolean(Rd.TS_BRACKETS_ROOT + TEXTSTYLE_STRIKETHROUGH_SUFFIX, false);
	}
	
}
