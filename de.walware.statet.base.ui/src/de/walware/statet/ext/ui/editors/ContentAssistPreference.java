/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.editors;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import de.walware.eclipsecommons.ui.util.ColorManager;

import de.walware.statet.base.internal.ui.StatetUIPlugin;
import de.walware.statet.base.ui.IStatetUIPreferenceConstants;


public class ContentAssistPreference {

	/**
	 * Preference key for content assist auto activation
	 */
	public final static String AUTOACTIVATION = "AutoActivation.enable"; //$NON-NLS-1$
	
	/**
	 * Preference key for content assist auto activation delay 
	 */
	public final static String AUTOACTIVATION_DELAY = "AutoActivation.delay"; //$NON-NLS-1$
	
	/**
	 * Preference key for content assist auto insert 
	 */
	public final static String AUTOINSERT = "AutoInsert.enable"; //$NON-NLS-1$

	/**
	 * Preference key for content assist proposal color
	 * <p>
	 * Value is of type <code>String</code>/<code>RGB</code>.
	 */
	public final static String PROPOSALS_FOREGROUND = "Proposals.foreground"; //$NON-NLS-1$
	
	/**
	 * Preference key for content assist proposal color 
	 * <p>
	 * Value is of type <code>String</code>/<code>RGB</code>.
	 */
	public final static String PROPOSALS_BACKGROUND = "Proposals.background"; //$NON-NLS-1$
	
	/**
	 * Preference key for content assist parameters color 
	 * <p>
	 * Value is of type <code>String</code>/<code>RGB</code>.
	 */
	public final static String PARAMETERS_FOREGROUND = "Parameters.foreground"; //$NON-NLS-1$
	
	/** 
	 * Preference key for content assist parameters color.
	 * <p>
	 * Value is of type <code>String</code>/<code>RGB</code>.
	 */
	public final static String PARAMETERS_BACKGROUND = "Parameters.background"; //$NON-NLS-1$
	
	/**
	 * A named preference that holds the foreground color used in the code
	 * assist selection dialog to mark replaced code.
	 * <p>
	 * Value is of type <code>String</code>/<code>RGB</code>.
	 */
	public final static String REPLACEMENT_FOREGROUND = "CompletionReplacement.foreground"; //$NON-NLS-1$

	/**
	 * A named preference that holds the background color used in the code
	 * assist selection dialog to mark replaced code.
	 * <p>
	 * Value is of type <code>String</code>/<code>RGB</code>.
	 */
	public final static String REPLACEMENT_BACKGROUND = "CompletionReplacement.background"; //$NON-NLS-1$

	public static IEclipsePreferences getCommonPreferencesNode() {
		return new InstanceScope().getNode(IStatetUIPreferenceConstants.CAT_CODEASSIST_QUALIFIER);
	}

//	/** Preference key for java content assist auto activation triggers */
//	private final static String AUTOACTIVATION_TRIGGERS_JAVA= PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA;
//	/** Preference key for javadoc content assist auto activation triggers */
//	private final static String AUTOACTIVATION_TRIGGERS_JAVADOC= PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVADOC;

//	/** Preference key for visibility of proposals */
//	private final static String SHOW_VISIBLE_PROPOSALS= PreferenceConstants.CODEASSIST_SHOW_VISIBLE_PROPOSALS;
//	/** Preference key for alphabetic ordering of proposals */
//	private final static String ORDER_PROPOSALS= PreferenceConstants.CODEASSIST_ORDER_PROPOSALS;
//	/** Preference key for case sensitivity of proposals */
//	private final static String CASE_SENSITIVITY= PreferenceConstants.CODEASSIST_CASE_SENSITIVITY;
//	/** Preference key for adding imports on code assist */
//	/** Preference key for filling argument names on method completion */
//	private static final String FILL_METHOD_ARGUMENTS= PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES;
//	/** Preference key for prefix completion. */
//	private static final String PREFIX_COMPLETION= PreferenceConstants.CODEASSIST_PREFIX_COMPLETION;

	
	private static Color getColor(IEclipsePreferences node, String key) {
		
		ColorManager manager = StatetUIPlugin.getDefault().getColorManager();
		RGB rgb = StringConverter.asRGB(node.get(key, null));
		return manager.getColor(rgb);
	}
	
	
	public static void adaptToPreferenceChange(ContentAssistant assistant, PreferenceChangeEvent event) {
		String p = (event != null) ? event.getKey() : null;
		
		IEclipsePreferences node = getCommonPreferencesNode();
		if (p == null || AUTOACTIVATION.equals(p)) {
			assistant.enableAutoActivation(node.getBoolean(AUTOACTIVATION, false));
		} 
		if (p == null || AUTOACTIVATION_DELAY.equals(p)) {
			assistant.setAutoActivationDelay(node.getInt(AUTOACTIVATION_DELAY, 200));
		}
		if (p == null || AUTOINSERT.equals(p)) {
			assistant.enableAutoInsert(node.getBoolean(AUTOINSERT, false));
		} 
		if (p == null || PROPOSALS_FOREGROUND.equals(p)) {
			assistant.setProposalSelectorForeground(getColor(node, PROPOSALS_FOREGROUND));
		} 
		if (p == null || PROPOSALS_BACKGROUND.equals(p)) {
			assistant.setProposalSelectorBackground(getColor(node, PROPOSALS_BACKGROUND));
		}
		if (p == null || PARAMETERS_FOREGROUND.equals(p)) {
			Color c = getColor(node, PARAMETERS_FOREGROUND);
			assistant.setContextInformationPopupForeground(c);
			assistant.setContextSelectorForeground(c);
		} 
		if (p == null || PARAMETERS_BACKGROUND.equals(p)) {
			Color c = getColor(node, PARAMETERS_BACKGROUND);
			assistant.setContextInformationPopupBackground(c);
			assistant.setContextSelectorBackground(c);
		}
	}
	
	/**
	 * Configure the given content assistant from the given store.
	 */
	public static void configure(ContentAssistant assistant) {

		adaptToPreferenceChange(assistant, null);
	}

}
