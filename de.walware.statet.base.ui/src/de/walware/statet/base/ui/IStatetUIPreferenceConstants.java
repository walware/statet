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

package de.walware.statet.base.ui;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * Common Preference constants used in Statet UI preference store. 
 */
public interface IStatetUIPreferenceConstants {

	
	/**
	 * Preference key suffix for enablement of optional text styles.
	 * Value: @value
	 */
	public static final String TS_ENABLE_SUFFIX = ".enable";

	/**
	 * Preference key suffix for color text style preference keys.
	 * Value: @value
	 */
	public static final String TS_COLOR_SUFFIX = ".color"; //$NON-NLS-1$

	/**
	 * Preference key suffix for bold text style preference keys.
	 * Value: @value
	 */
	public static final String TS_BOLD_SUFFIX = ".bold"; //$NON-NLS-1$

	/**
	 * Preference key suffix for bold text style preference keys.
	 * Value: @value
	 */
	public static final String TS_ITALIC_SUFFIX = ".italic"; //$NON-NLS-1$	
	
	/**
	 * Preference key suffix for strikethrough text style preference keys.
	 * Value: @value
	 */
	public static final String TS_STRIKETHROUGH_SUFFIX = ".strikethrough"; //$NON-NLS-1$
	
	/**
	 * Preference key suffix for underline text style preference keys.
	 * Value: @value
	 */
	public static final String TS_UNDERLINE_SUFFIX = ".underline"; //$NON-NLS-1$

	
	
	public static final String ROOT = StatetUIPlugin.PLUGIN_ID + ".ui";
	
	
	/**
	 * A named preference that controls whether bracket matching highlighting is turned on or off.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 */
	public final static String EDITOR_MATCHING_BRACKETS = ROOT+".editors.MatchingBrackets"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to highlight matching brackets.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string 
	 * using class <code>PreferenceConverter</code>
	 */
	public final static String EDITOR_MATCHING_BRACKETS_COLOR = ROOT+".editors.MatchingBrackets.color"; //$NON-NLS-1$

	
	public final static String CODEASSIST_ROOT = ROOT + ".contentassist";
	
	/**
	 * A named preference that controls if the Java code assist gets auto activated.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String CODEASSIST_AUTOACTIVATION = CODEASSIST_ROOT + ".AutoActivation"; //$NON-NLS-1$

	/**
	 * A name preference that holds the auto activation delay time in milli seconds.
	 * <p>
	 * Value is of type <code>Integer</code>.
	 * </p>
	 */
	public final static String CODEASSIST_AUTOACTIVATION_DELAY = CODEASSIST_ROOT + ".AutoActivation.delay"; //$NON-NLS-1$

	/**
	 * A named preference that controls if the code assist inserts a
	 * proposal automatically if only one proposal is available.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String CODEASSIST_AUTOINSERT = CODEASSIST_ROOT +".AutoInsert"; //$NON-NLS-1$

	/**
	 * A named preference that holds the background color used in the code assist selection dialog.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 */
	public final static String CODEASSIST_PROPOSALS_BACKGROUND = CODEASSIST_ROOT + ".Proposals.background"; //$NON-NLS-1$

	/**
	 * A named preference that holds the foreground color used in the code assist selection dialog.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 */
	public final static String CODEASSIST_PROPOSALS_FOREGROUND = CODEASSIST_ROOT + ".Proposals.foreground"; //$NON-NLS-1$

	/**
	 * A named preference that holds the background color used for parameter hints.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 */
	public final static String CODEASSIST_PARAMETERS_BACKGROUND = CODEASSIST_ROOT + ".Parameters.background"; //$NON-NLS-1$

	/**
	 * A named preference that holds the foreground color used in the code assist selection dialog.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 */
	public final static String CODEASSIST_PARAMETERS_FOREGROUND = CODEASSIST_ROOT + "Parameters.foreground"; //$NON-NLS-1$

	/**
	 * A named preference that holds the background color used in the code
	 * assist selection dialog to mark replaced code.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 */
	public final static String CODEASSIST_REPLACEMENT_BACKGROUND = CODEASSIST_ROOT + "CompletionReplacement_background"; //$NON-NLS-1$

	/**
	 * A named preference that holds the foreground color used in the code
	 * assist selection dialog to mark replaced code.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 */
	public final static String CODEASSIST_REPLACEMENT_FOREGROUND = CODEASSIST_ROOT + "CompletionReplacement_foreground"; //$NON-NLS-1$

	
//	/**
//	 * A named preference that controls whether the ouline page should sort its elements.
//	 * <p>
//	 * Value is of type <code>Boolean</code>.
//	 */
//	public static final String EDITOROUTLINE_SORT = ID+"editor_outline.sort";
//	/**
//	 * A named preference that controls whether the ouline page links its selection to the active editor.
//	 * <p>
//	 * Value is of type <code>Boolean</code>.
//	 */
//	public static final String EDITOROUTLINE_LINKWITHEDITOR = ID+"editor_outline.link_with_editor";
//	
//	

}

