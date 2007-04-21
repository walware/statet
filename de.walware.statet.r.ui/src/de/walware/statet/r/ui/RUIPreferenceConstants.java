/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui;

import static de.walware.statet.base.ui.IStatetUIPreferenceConstants.TS_BOLD_SUFFIX;
import static de.walware.statet.base.ui.IStatetUIPreferenceConstants.TS_COLOR_SUFFIX;
import static de.walware.statet.base.ui.IStatetUIPreferenceConstants.TS_ITALIC_SUFFIX;
import static de.walware.statet.base.ui.IStatetUIPreferenceConstants.TS_STRIKETHROUGH_SUFFIX;
import static de.walware.statet.base.ui.IStatetUIPreferenceConstants.TS_UNDERLINE_SUFFIX;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

import de.walware.statet.r.ui.text.r.IRTextTokens;
import de.walware.statet.r.ui.text.rd.IRdTextTokens;

/**
 * Preference constants used in the StatET-R preference store. Clients should only read the
 * RDT-UI preference store using these values. Clients are not allowed to modify the 
 * preference store programmatically.
 * 
 * @see org.eclipse.jface.resource.StringConverter
 * @see org.eclipse.jface.preference.PreferenceConverter
 *
 */
public class RUIPreferenceConstants {

	private RUIPreferenceConstants() {
	}

	public interface R {

		public final static String TS_ROOT = IRTextTokens.ROOT;
		
		public final static String TS_DEFAULT_ROOT = IRTextTokens.DEFAULT;
		public final static String TS_DEFAULT_COLOR = TS_DEFAULT_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_DEFAULT_BOLD = TS_DEFAULT_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_DEFAULT_ITALIC = TS_DEFAULT_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_DEFAULT_UNDERLINE = TS_DEFAULT_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_DEFAULT_STRIKETHROUGH = TS_DEFAULT_ROOT + TS_STRIKETHROUGH_SUFFIX;

		public final static String TS_UNDEFINED_ROOT = IRTextTokens.UNDEFINED;
		public final static String TS_UNDEFINED_COLOR = TS_UNDEFINED_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_UNDEFINED_BOLD = TS_UNDEFINED_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_UNDEFINED_ITALIC = TS_UNDEFINED_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_UNDEFINED_UNDERLINE = TS_UNDEFINED_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_UNDEFINED_STRIKETHROUGH = TS_UNDEFINED_ROOT + TS_STRIKETHROUGH_SUFFIX;

		public final static String TS_COMMENT_ROOT = IRTextTokens.COMMENT;
		public final static String TS_COMMENT_COLOR = TS_COMMENT_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_COMMENT_BOLD = TS_COMMENT_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_COMMENT_ITALIC = TS_COMMENT_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_COMMENT_UNDERLINE = TS_COMMENT_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_COMMENT_STRIKETHROUGH = TS_COMMENT_ROOT + TS_STRIKETHROUGH_SUFFIX;

		public final static String TS_TASK_TAG_ROOT = IRTextTokens.TASK_TAG;
		public final static String TS_TASK_TAG_COLOR = TS_TASK_TAG_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_TASK_TAG_BOLD = TS_TASK_TAG_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_TASK_TAG_ITALIC = TS_TASK_TAG_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_TASK_TAG_UNDERLINE = TS_TASK_TAG_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_TASK_TAG_STRIKETHROUGH = TS_TASK_TAG_ROOT + TS_STRIKETHROUGH_SUFFIX;

		public final static String TS_STRING_ROOT = IRTextTokens.STRING;
		public final static String TS_STRING_COLOR = TS_STRING_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_STRING_BOLD = TS_STRING_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_STRING_ITALIC = TS_STRING_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_STRING_UNDERLINE = TS_STRING_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_STRING_STRIKETHROUGH = TS_STRING_ROOT + TS_STRIKETHROUGH_SUFFIX;

		public final static String TS_NUMBERS_ROOT = IRTextTokens.NUMBERS;
		public final static String TS_NUMBERS_COLOR = TS_NUMBERS_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_NUMBERS_BOLD = TS_NUMBERS_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_NUMBERS_ITALIC = TS_NUMBERS_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_NUMBERS_UNDERLINE = TS_NUMBERS_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_NUMBERS_STRIKETHROUGH = TS_NUMBERS_ROOT + TS_STRIKETHROUGH_SUFFIX;

		public final static String TS_SPECIAL_CONSTANTS_ROOT = IRTextTokens.SPECIAL_CONSTANTS;
		public final static String TS_SPECIAL_CONSTANTS_COLOR = TS_SPECIAL_CONSTANTS_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_SPECIAL_CONSTANTS_BOLD = TS_SPECIAL_CONSTANTS_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_SPECIAL_CONSTANTS_ITALIC = TS_SPECIAL_CONSTANTS_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_SPECIAL_CONSTANTS_UNDERLINE = TS_SPECIAL_CONSTANTS_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_SPECIAL_CONSTANTS_STRIKETHROUGH = TS_SPECIAL_CONSTANTS_ROOT + TS_STRIKETHROUGH_SUFFIX;

		public final static String TS_LOGICAL_CONSTANTS_ROOT = IRTextTokens.LOGICAL_CONSTANTS;
		public final static String TS_LOGICAL_CONSTANTS_COLOR = TS_LOGICAL_CONSTANTS_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_LOGICAL_CONSTANTS_BOLD = TS_LOGICAL_CONSTANTS_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_LOGICAL_CONSTANTS_ITALIC = TS_LOGICAL_CONSTANTS_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_LOGICAL_CONSTANTS_UNDERLINE = TS_LOGICAL_CONSTANTS_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_LOGICAL_CONSTANTS_STRIKETHROUGH = TS_LOGICAL_CONSTANTS_ROOT + TS_STRIKETHROUGH_SUFFIX;

		public final static String TS_FLOWCONTROL_ROOT = IRTextTokens.FLOWCONTROL;
		public final static String TS_FLOWCONTROL_COLOR = TS_FLOWCONTROL_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_FLOWCONTROL_BOLD = TS_FLOWCONTROL_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_FLOWCONTROL_ITALIC = TS_FLOWCONTROL_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_FLOWCONTROL_UNDERLINE = TS_FLOWCONTROL_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_FLOWCONTROL_STRIKETHROUGH = TS_FLOWCONTROL_ROOT + TS_STRIKETHROUGH_SUFFIX;

		public final static String TS_SEPARATORS_ROOT = IRTextTokens.SEPARATORS;
		public final static String TS_SEPARATORS_COLOR = TS_SEPARATORS_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_SEPARATORS_BOLD = TS_SEPARATORS_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_SEPARATORS_ITALIC = TS_SEPARATORS_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_SEPARATORS_UNDERLINE = TS_SEPARATORS_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_SEPARATORS_STRIKETHROUGH = TS_SEPARATORS_ROOT + TS_STRIKETHROUGH_SUFFIX;

		public final static String TS_ASSIGNMENT_ROOT = IRTextTokens.ASSIGNMENT;
		public final static String TS_ASSIGNMENT_COLOR = TS_ASSIGNMENT_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_ASSIGNMENT_BOLD = TS_ASSIGNMENT_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_ASSIGNMENT_ITALIC = TS_ASSIGNMENT_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_ASSIGNMENT_UNDERLINE = TS_ASSIGNMENT_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_ASSIGNMENT_STRIKETHROUGH = TS_ASSIGNMENT_ROOT + TS_STRIKETHROUGH_SUFFIX;

		public final static String TS_OTHER_OPERATORS_ROOT = IRTextTokens.OTHER_OPERATORS;
		public final static String TS_OTHER_OPERATORS_COLOR = TS_OTHER_OPERATORS_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_OTHER_OPERATORS_BOLD = TS_OTHER_OPERATORS_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_OTHER_OPERATORS_ITALIC = TS_OTHER_OPERATORS_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_OTHER_OPERATORS_UNDERLINE = TS_OTHER_OPERATORS_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_OTHER_OPERATORS_STRIKETHROUGH = TS_OTHER_OPERATORS_ROOT + TS_STRIKETHROUGH_SUFFIX;

		public final static String TS_GROUPING_ROOT = IRTextTokens.GROUPING;
		public final static String TS_GROUPING_COLOR = TS_GROUPING_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_GROUPING_BOLD = TS_GROUPING_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_GROUPING_ITALIC = TS_GROUPING_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_GROUPING_UNDERLINE = TS_GROUPING_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_GROUPING_STRIKETHROUGH = TS_GROUPING_ROOT + TS_STRIKETHROUGH_SUFFIX;

		public final static String TS_INDEXING_ROOT = IRTextTokens.INDEXING;
		public final static String TS_INDEXING_COLOR = TS_INDEXING_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_INDEXING_BOLD = TS_INDEXING_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_INDEXING_ITALIC = TS_INDEXING_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_INDEXING_UNDERLINE = TS_INDEXING_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_INDEXING_STRIKETHROUGH = TS_INDEXING_ROOT + TS_STRIKETHROUGH_SUFFIX;
	
	}
	
	public interface Rd {

		public final static String TS_ROOT = IRdTextTokens.ROOT;
		
		public final static String TS_BRACKETS_ROOT = IRdTextTokens.BRACKETS;
		public final static String TS_BRACKETS_COLOR = TS_BRACKETS_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_BRACKETS_BOLD = TS_BRACKETS_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_BRACKETS_ITALIC = TS_BRACKETS_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_BRACKETS_UNDERLINE = TS_BRACKETS_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_BRACKETS_STRIKETHROUGH = TS_BRACKETS_ROOT + TS_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_COMMENT_ROOT = IRdTextTokens.COMMENT;
		public final static String TS_COMMENT_COLOR = TS_COMMENT_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_COMMENT_BOLD = TS_COMMENT_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_COMMENT_ITALIC = TS_COMMENT_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_COMMENT_UNDERLINE = TS_COMMENT_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_COMMENT_STRIKETHROUGH = TS_COMMENT_ROOT + TS_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_DEFAULT_ROOT = IRdTextTokens.DEFAULT;
		public final static String TS_DEFAULT_COLOR = TS_DEFAULT_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_DEFAULT_BOLD = TS_DEFAULT_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_DEFAULT_ITALIC = TS_DEFAULT_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_DEFAULT_UNDERLINE = TS_DEFAULT_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_DEFAULT_STRIKETHROUGH = TS_DEFAULT_ROOT + TS_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_OTHER_TAG_ROOT = IRdTextTokens.OTHER_TAG;
		public final static String TS_OTHER_TAG_COLOR = TS_OTHER_TAG_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_OTHER_TAG_BOLD = TS_OTHER_TAG_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_OTHER_TAG_ITALIC = TS_OTHER_TAG_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_OTHER_TAG_UNDERLINE = TS_OTHER_TAG_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_OTHER_TAG_STRIKETHROUGH = TS_OTHER_TAG_ROOT + TS_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_PLATFORM_SPECIF_ROOT = IRdTextTokens.PLATFORM_SPECIF;
		public final static String TS_PLATFORM_SPECIF_COLOR = TS_PLATFORM_SPECIF_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_PLATFORM_SPECIF_BOLD = TS_PLATFORM_SPECIF_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_PLATFORM_SPECIF_ITALIC = TS_PLATFORM_SPECIF_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_PLATFORM_SPECIF_UNDERLINE = TS_PLATFORM_SPECIF_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_PLATFORM_SPECIF_STRIKETHROUGH = TS_PLATFORM_SPECIF_ROOT + TS_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_SECTION_TAG_ROOT = IRdTextTokens.SECTION_TAG;
		public final static String TS_SECTION_TAG_COLOR = TS_SECTION_TAG_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_SECTION_TAG_BOLD = TS_SECTION_TAG_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_SECTION_TAG_ITALIC = TS_SECTION_TAG_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_SECTION_TAG_UNDERLINE = TS_SECTION_TAG_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_SECTION_TAG_STRIKETHROUGH = TS_SECTION_TAG_ROOT + TS_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_SUBSECTION_TAG_ROOT = IRdTextTokens.SUBSECTION_TAG;
		public final static String TS_SUBSECTION_TAG_COLOR = TS_SUBSECTION_TAG_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_SUBSECTION_TAG_BOLD = TS_SUBSECTION_TAG_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_SUBSECTION_TAG_ITALIC = TS_SUBSECTION_TAG_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_SUBSECTION_TAG_UNDERLINE = TS_SUBSECTION_TAG_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_SUBSECTION_TAG_STRIKETHROUGH = TS_SUBSECTION_TAG_ROOT + TS_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_TASK_TAG_ROOT = IRdTextTokens.TASK_TAG;
		public final static String TS_TASK_TAG_COLOR = TS_TASK_TAG_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_TASK_TAG_BOLD = TS_TASK_TAG_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_TASK_TAG_ITALIC = TS_TASK_TAG_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_TASK_TAG_UNDERLINE = TS_TASK_TAG_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_TASK_TAG_STRIKETHROUGH = TS_TASK_TAG_ROOT + TS_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_UNLISTED_TAG_ROOT = IRdTextTokens.UNLISTED_TAG;
		public final static String TS_UNLISTED_TAG_COLOR = TS_UNLISTED_TAG_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_UNLISTED_TAG_BOLD = TS_UNLISTED_TAG_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_UNLISTED_TAG_ITALIC = TS_UNLISTED_TAG_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_UNLISTED_TAG_UNDERLINE = TS_UNLISTED_TAG_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_UNLISTED_TAG_STRIKETHROUGH = TS_UNLISTED_TAG_ROOT + TS_STRIKETHROUGH_SUFFIX;
		
		public final static String TS_VERBATIM_ROOT = IRdTextTokens.VERBATIM;
		public final static String TS_VERBATIM_COLOR = TS_VERBATIM_ROOT + TS_COLOR_SUFFIX;
		public final static String TS_VERBATIM_BOLD = TS_VERBATIM_ROOT + TS_BOLD_SUFFIX;
		public final static String TS_VERBATIM_ITALIC = TS_VERBATIM_ROOT + TS_ITALIC_SUFFIX;
		public final static String TS_VERBATIM_UNDERLINE = TS_VERBATIM_ROOT + TS_UNDERLINE_SUFFIX;
		public final static String TS_VERBATIM_STRIKETHROUGH = TS_VERBATIM_ROOT + TS_STRIKETHROUGH_SUFFIX;
		
	}
	
	
	
	/**
	 * Initializes the given preference store with the default values.
	 * 
	 * @param store the preference store to be initialized
	 */
	public static void initializeDefaultValues(IPreferenceStore store) {

		PreferenceConverter.setDefault(store, R.TS_DEFAULT_COLOR, new RGB(0, 0, 0));
		store.setDefault(R.TS_DEFAULT_BOLD, false);
		store.setDefault(R.TS_DEFAULT_ITALIC, false);
		store.setDefault(R.TS_DEFAULT_UNDERLINE, false);
		store.setDefault(R.TS_DEFAULT_STRIKETHROUGH, false);

		PreferenceConverter.setDefault(store, R.TS_UNDEFINED_COLOR, new RGB(127, 0, 0));
		store.setDefault(R.TS_UNDEFINED_BOLD, false);
		store.setDefault(R.TS_UNDEFINED_ITALIC, true);
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
		store.setDefault(R.TS_ASSIGNMENT_BOLD, false);
		store.setDefault(R.TS_ASSIGNMENT_ITALIC, false);
		store.setDefault(R.TS_ASSIGNMENT_UNDERLINE, false);
		store.setDefault(R.TS_ASSIGNMENT_STRIKETHROUGH, false);

		PreferenceConverter.setDefault(store, R.TS_OTHER_OPERATORS_COLOR, new RGB(159, 63, 127));
		store.setDefault(R.TS_OTHER_OPERATORS_BOLD, false);
		store.setDefault(R.TS_OTHER_OPERATORS_ITALIC, false);
		store.setDefault(R.TS_OTHER_OPERATORS_UNDERLINE, false);
		store.setDefault(R.TS_OTHER_OPERATORS_STRIKETHROUGH, false);

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

}

