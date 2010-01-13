/*******************************************************************************
 * Copyright (c) 2005-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui.sourceeditors;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.swt.graphics.Color;

import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.Preference.BooleanPref;
import de.walware.ecommons.preferences.Preference.IntPref;
import de.walware.ecommons.ui.preferences.RGBPref;
import de.walware.ecommons.ui.util.ColorManager;

import de.walware.statet.base.internal.ui.StatetUIPlugin;
import de.walware.statet.base.ui.IStatetUIPreferenceConstants;


public class ContentAssistPreference {
	
	
	public static final String GROUP_ID = "statet.contentassist"; //$NON-NLS-1$
	
	
	/**
	 * Preference for content assist auto activation
	 */
	public final static BooleanPref AUTOACTIVATION = new BooleanPref(
			IStatetUIPreferenceConstants.CAT_CODEASSIST_QUALIFIER, "AutoActivation.enable"); //$NON-NLS-1$
	
	/**
	 * Preference for content assist auto activation delay
	 */
	public final static IntPref AUTOACTIVATION_DELAY = new IntPref(
			IStatetUIPreferenceConstants.CAT_CODEASSIST_QUALIFIER, "AutoActivation.delay"); //$NON-NLS-1$
	
	/**
	 * Preference for content assist auto insert
	 */
	public final static BooleanPref AUTOINSERT_SINGLE = new BooleanPref(
			IStatetUIPreferenceConstants.CAT_CODEASSIST_QUALIFIER, "AutoInsert.Single.enable"); //$NON-NLS-1$
	
	/**
	 * Preference for content assist auto insert
	 */
	public final static BooleanPref AUTOINSERT_COMMON = new BooleanPref(
			IStatetUIPreferenceConstants.CAT_CODEASSIST_QUALIFIER, "AutoInsert.Common.enable"); //$NON-NLS-1$
	
	/**
	 * Preference for content assist proposal color
	 */
	public final static RGBPref PROPOSALS_FOREGROUND = new RGBPref(
			IStatetUIPreferenceConstants.CAT_CODEASSIST_QUALIFIER, "Proposals.foreground"); //$NON-NLS-1$
	
	/**
	 * Preference for content assist proposal color
	 */
	public final static RGBPref PROPOSALS_BACKGROUND = new RGBPref(
			IStatetUIPreferenceConstants.CAT_CODEASSIST_QUALIFIER, "Proposals.background"); //$NON-NLS-1$
	
	/**
	 * Preference for content assist parameters color
	 */
	public final static RGBPref PARAMETERS_FOREGROUND = new RGBPref(
			IStatetUIPreferenceConstants.CAT_CODEASSIST_QUALIFIER, "Parameters.foreground"); //$NON-NLS-1$
	
	/**
	 * Preference key for content assist parameters color.
	 */
	public final static RGBPref PARAMETERS_BACKGROUND = new RGBPref(
			IStatetUIPreferenceConstants.CAT_CODEASSIST_QUALIFIER, "Parameters.background"); //$NON-NLS-1$
	
	/**
	 * A named preference that holds the foreground color used in the code
	 * assist selection dialog to mark replaced code.
	 */
	public final static RGBPref REPLACEMENT_FOREGROUND = new RGBPref(
			IStatetUIPreferenceConstants.CAT_CODEASSIST_QUALIFIER, "CompletionReplacement.foreground"); //$NON-NLS-1$
	
	/**
	 * A named preference that holds the background color used in the code
	 * assist selection dialog to mark replaced code.
	 */
	public final static RGBPref REPLACEMENT_BACKGROUND = new RGBPref(
			IStatetUIPreferenceConstants.CAT_CODEASSIST_QUALIFIER, "CompletionReplacement.background"); //$NON-NLS-1$
	
	
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
	
	
	/**
	 * Configure the given content assistant according common StatET settings.
	 */
	public static void configure(final ContentAssistant assistant) {
		final ColorManager manager = StatetUIPlugin.getDefault().getColorManager();
		final IPreferenceAccess statet = PreferencesUtil.getInstancePrefs();
		
		assistant.enableAutoActivation(statet.getPreferenceValue(AUTOACTIVATION));
		assistant.setAutoActivationDelay(statet.getPreferenceValue(AUTOACTIVATION_DELAY));
		assistant.enableAutoInsert(statet.getPreferenceValue(AUTOINSERT_SINGLE));
		assistant.enablePrefixCompletion(statet.getPreferenceValue(AUTOINSERT_COMMON));
		assistant.setProposalSelectorForeground(manager.getColor(statet.getPreferenceValue(PROPOSALS_FOREGROUND)));
		assistant.setProposalSelectorBackground(manager.getColor(statet.getPreferenceValue(PROPOSALS_BACKGROUND)));
		{	final Color c = manager.getColor(statet.getPreferenceValue(PARAMETERS_FOREGROUND));
			assistant.setContextInformationPopupForeground(c);
			assistant.setContextSelectorForeground(c);
		}
		{	final Color c = manager.getColor(statet.getPreferenceValue(PARAMETERS_BACKGROUND));
			assistant.setContextInformationPopupBackground(c);
			assistant.setContextSelectorBackground(c);
		}
		
//		assistant.enableColoredLabels(true);
	}
	
//	public static void configureInformationProposalMode(final ContentAssist assistant, final boolean enable) {
//		final ColorManager manager = StatetUIPlugin.getDefault().getColorManager();
//		final IPreferenceAccess statet = PreferencesUtil.getInstancePrefs();
//		
//		assistant.setProposalSelectorForeground(manager.getColor(statet.getPreferenceValue(enable ?
//				PARAMETERS_FOREGROUND : PROPOSALS_FOREGROUND)));
//		assistant.setProposalSelectorBackground(manager.getColor(statet.getPreferenceValue(enable ?
//				PARAMETERS_BACKGROUND : PROPOSALS_BACKGROUND)));
//	}
	
	/**
	 * Configure the given quick assistant according common StatET settings.
	 */
	public static void configure(final IQuickAssistAssistant assistant) {
		final ColorManager manager = StatetUIPlugin.getDefault().getColorManager();
		final IPreferenceAccess statet = PreferencesUtil.getInstancePrefs();
		
		assistant.setProposalSelectorForeground(manager.getColor(statet.getPreferenceValue(PROPOSALS_FOREGROUND)));
		assistant.setProposalSelectorBackground(manager.getColor(statet.getPreferenceValue(PROPOSALS_BACKGROUND)));
	}
	
}
