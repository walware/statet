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

package de.walware.statet.ext.ui.editors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import de.walware.eclipsecommons.ui.util.ColorManager;

import de.walware.statet.base.internal.ui.StatetUIPlugin;
import de.walware.statet.base.ui.IStatetUIPreferenceConstants;


public class ContentAssistPreference {

	/** Preference key for content assist auto activation */
	private final static String AUTOACTIVATION = IStatetUIPreferenceConstants.CODEASSIST_AUTOACTIVATION;
	/** Preference key for content assist auto activation delay */
	private final static String AUTOACTIVATION_DELAY = IStatetUIPreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY;
	/** Preference key for content assist proposal color */
	private final static String PROPOSALS_FOREGROUND = IStatetUIPreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND;
	/** Preference key for content assist proposal color */
	private final static String PROPOSALS_BACKGROUND = IStatetUIPreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND;
	/** Preference key for content assist parameters color */
	private final static String PARAMETERS_FOREGROUND = IStatetUIPreferenceConstants.CODEASSIST_PARAMETERS_FOREGROUND;
	/** Preference key for content assist parameters color */
	private final static String PARAMETERS_BACKGROUND = IStatetUIPreferenceConstants.CODEASSIST_PARAMETERS_BACKGROUND;
	/** Preference key for content assist auto insert */
	private final static String AUTOINSERT= IStatetUIPreferenceConstants.CODEASSIST_AUTOINSERT;
	

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

	
	private static Color getColor(IPreferenceStore store, String key) {
		
		ColorManager manager = StatetUIPlugin.getDefault().getColorManager();
		RGB rgb = PreferenceConverter.getColor(store, PROPOSALS_FOREGROUND);
		
		return manager.getColor(rgb);
	}
	
	
	public static void adaptToPreferenceChange(ContentAssistant assistant, PropertyChangeEvent event) {

		String p = event.getProperty();
		if (!p.startsWith(IStatetUIPreferenceConstants.CODEASSIST_ROOT))
			return;
		
		IPreferenceStore store = StatetUIPlugin.getDefault().getPreferenceStore();
		
		if (AUTOACTIVATION.equals(p)) {
			assistant.enableAutoActivation(store.getBoolean(AUTOACTIVATION));
		} 
		else if (AUTOACTIVATION_DELAY.equals(p)) {
			assistant.setAutoActivationDelay(store.getInt(AUTOACTIVATION_DELAY));
		}
		else if (PROPOSALS_FOREGROUND.equals(p)) {
			assistant.setProposalSelectorForeground(getColor(store, PROPOSALS_FOREGROUND));
		} 
		else if (PROPOSALS_BACKGROUND.equals(p)) {
			assistant.setProposalSelectorBackground(getColor(store, PROPOSALS_BACKGROUND));
		} 
		else if (PARAMETERS_FOREGROUND.equals(p)) {
			Color c = getColor(store, PARAMETERS_FOREGROUND);
			assistant.setContextInformationPopupForeground(c);
			assistant.setContextSelectorForeground(c);
		} 
		else if (PARAMETERS_BACKGROUND.equals(p)) {
			Color c = getColor(store, PARAMETERS_BACKGROUND);
			assistant.setContextInformationPopupBackground(c);
			assistant.setContextSelectorBackground(c);
		} 
		else if (AUTOINSERT.equals(p)) {
			boolean enabled= store.getBoolean(AUTOINSERT);
			assistant.enableAutoInsert(enabled);
		} 
//			else if (PREFIX_COMPLETION.equals(p)) {
//			boolean enabled= store.getBoolean(PREFIX_COMPLETION);
//			assistant.enablePrefixCompletion(enabled);
//		}
		
//		changeJavaProcessor(assistant, store, p);
//		changeJavaDocProcessor(assistant, store, p);
		
	}
	
//	private static Color getColor(IPreferenceStore store, String key, ColorManager manager) {
//		
//		Color color = manager.getColor(key);
//		RGB rgb= PreferenceConverter.getColor(store, key);
//		return manager.getColor(rgb);
//	}
//
//	private static Color getColor(IPreferenceStore store, String key) {
//		JavaTextTools textTools= JavaPlugin.getDefault().getJavaTextTools();
//		return getColor(store, key, textTools.getColorManager());
//	}
//
//	private static JavaCompletionProcessor getJavaProcessor(ContentAssistant assistant) {
//		IContentAssistProcessor p= assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
//		if (p instanceof JavaCompletionProcessor)
//			return  (JavaCompletionProcessor) p;
//		return null;
//	}
//
//	private static JavaDocCompletionProcessor getJavaDocProcessor(ContentAssistant assistant) {
//		IContentAssistProcessor p= assistant.getContentAssistProcessor(IJavaPartitions.JAVA_DOC);
//		if (p instanceof JavaDocCompletionProcessor)
//			return (JavaDocCompletionProcessor) p;
//		return null;
//	}
//
//	private static void configureJavaProcessor(ContentAssistant assistant, IPreferenceStore store) {
//		JavaCompletionProcessor jcp= getJavaProcessor(assistant);
//		if (jcp == null)
//			return;
//
//		String triggers= store.getString(AUTOACTIVATION_TRIGGERS_JAVA);
//		if (triggers != null)
//			jcp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
//
//		boolean enabled= store.getBoolean(SHOW_VISIBLE_PROPOSALS);
//		jcp.restrictProposalsToVisibility(enabled);
//
//		enabled= store.getBoolean(CASE_SENSITIVITY);
//		jcp.restrictProposalsToMatchingCases(enabled);
//
//		enabled= store.getBoolean(ORDER_PROPOSALS);
//		jcp.orderProposalsAlphabetically(enabled);
//	}
//
//	private static void configureJavaDocProcessor(ContentAssistant assistant, IPreferenceStore store) {
//		JavaDocCompletionProcessor jdcp= getJavaDocProcessor(assistant);
//		if (jdcp == null)
//			return;
//
//		String triggers= store.getString(AUTOACTIVATION_TRIGGERS_JAVADOC);
//		if (triggers != null)
//			jdcp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
//
//		boolean enabled= store.getBoolean(CASE_SENSITIVITY);
//		jdcp.restrictProposalsToMatchingCases(enabled);
//
//		enabled= store.getBoolean(ORDER_PROPOSALS);
//		jdcp.orderProposalsAlphabetically(enabled);
//	}

	/**
	 * Configure the given content assistant from the given store.
	 */
	public static void configure(ContentAssistant assistant) {

		IPreferenceStore store = StatetUIPlugin.getDefault().getPreferenceStore();
		ColorManager manager = StatetUIPlugin.getDefault().getColorManager();
		
		assistant.enableAutoActivation(store.getBoolean(AUTOACTIVATION));
		assistant.setAutoActivationDelay(store.getInt(AUTOACTIVATION_DELAY));

		assistant.enableAutoInsert(store.getBoolean(AUTOINSERT));

		Color c = manager.getColor(PreferenceConverter.getColor(store, PROPOSALS_FOREGROUND));
		assistant.setProposalSelectorForeground(c);
		c = manager.getColor(PreferenceConverter.getColor(store, PROPOSALS_BACKGROUND));
		assistant.setProposalSelectorBackground(c);

		c = manager.getColor(PreferenceConverter.getColor(store, PARAMETERS_FOREGROUND));
		assistant.setContextInformationPopupForeground(c);
		assistant.setContextSelectorForeground(c);

		c = manager.getColor(PreferenceConverter.getColor(store, PARAMETERS_BACKGROUND));
		assistant.setContextInformationPopupBackground(c);
		assistant.setContextSelectorBackground(c);

//		enabled= store.getBoolean(PREFIX_COMPLETION);
//		assistant.enablePrefixCompletion(enabled);

//		configureJavaProcessor(assistant, store);
//		configureJavaDocProcessor(assistant, store);
	}


//	private static void changeJavaProcessor(ContentAssistant assistant, IPreferenceStore store, String key) {
//		JavaCompletionProcessor jcp= getJavaProcessor(assistant);
//		if (jcp == null)
//			return;
//
//		if (AUTOACTIVATION_TRIGGERS_JAVA.equals(key)) {
//			String triggers= store.getString(AUTOACTIVATION_TRIGGERS_JAVA);
//			if (triggers != null)
//				jcp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
//		} else if (SHOW_VISIBLE_PROPOSALS.equals(key)) {
//			boolean enabled= store.getBoolean(SHOW_VISIBLE_PROPOSALS);
//			jcp.restrictProposalsToVisibility(enabled);
//		} else if (CASE_SENSITIVITY.equals(key)) {
//			boolean enabled= store.getBoolean(CASE_SENSITIVITY);
//			jcp.restrictProposalsToMatchingCases(enabled);
//		} else if (ORDER_PROPOSALS.equals(key)) {
//			boolean enable= store.getBoolean(ORDER_PROPOSALS);
//			jcp.orderProposalsAlphabetically(enable);
//		}
//	}
//
//	private static void changeJavaDocProcessor(ContentAssistant assistant, IPreferenceStore store, String key) {
//		JavaDocCompletionProcessor jdcp= getJavaDocProcessor(assistant);
//		if (jdcp == null)
//			return;
//
//		if (AUTOACTIVATION_TRIGGERS_JAVADOC.equals(key)) {
//			String triggers= store.getString(AUTOACTIVATION_TRIGGERS_JAVADOC);
//			if (triggers != null)
//				jdcp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
//		} else if (CASE_SENSITIVITY.equals(key)) {
//			boolean enabled= store.getBoolean(CASE_SENSITIVITY);
//			jdcp.restrictProposalsToMatchingCases(enabled);
//		} else if (ORDER_PROPOSALS.equals(key)) {
//			boolean enable= store.getBoolean(ORDER_PROPOSALS);
//			jdcp.orderProposalsAlphabetically(enable);
//		}
//	}
//
//	public static boolean fillArgumentsOnMethodCompletion(IPreferenceStore store) {
//		return store.getBoolean(FILL_METHOD_ARGUMENTS);
//	}
}
