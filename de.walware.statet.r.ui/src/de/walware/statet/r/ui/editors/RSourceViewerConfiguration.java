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

package de.walware.statet.r.ui.editors;

import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;

import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.ui.preferences.ICombinedPreferenceStore;
import de.walware.eclipsecommons.ui.util.ColorManager;

import de.walware.statet.base.core.preferences.StatetCorePreferenceNodes;
import de.walware.statet.base.core.preferences.TaskTagsPreferences;
import de.walware.statet.ext.ui.editors.ContentAssistPreference;
import de.walware.statet.ext.ui.editors.StatextSourceViewerConfiguration;
import de.walware.statet.ext.ui.text.CommentScanner;
import de.walware.statet.ext.ui.text.SingleTokenScanner;
import de.walware.statet.ext.ui.text.StatextTextScanner;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCodeStyleSettings.IndentationType;
import de.walware.statet.r.ui.IRDocumentPartitions;
import de.walware.statet.r.ui.RUIPreferenceConstants;
import de.walware.statet.r.ui.editors.templates.REditorTemplatesCompletionProcessor;
import de.walware.statet.r.ui.text.r.IRTextTokens;
import de.walware.statet.r.ui.text.r.RCodeScanner;
import de.walware.statet.r.ui.text.r.RDoubleClickStrategy;
import de.walware.statet.r.ui.text.r.RIndentation;
import de.walware.statet.r.ui.text.r.RInfixOperatorScanner;


/**
 * Default Configuration for SourceViewer of R code.
 * 
 * @author Stephan Wahlbrink
 */
public class RSourceViewerConfiguration extends StatextSourceViewerConfiguration {


	public static ICombinedPreferenceStore createCombinedPreferenceStore(
			IPreferenceStore store, IPreferenceAccess corePrefs) {
		return StatextSourceViewerConfiguration.createCombinedPreferenceStore(
				store, corePrefs, new String[] { 
					StatetCorePreferenceNodes.CAT_MANAGMENT_QUALIFIER });
	}

	public static void fetchSettingsContexts(Set<String> list) {
		list.add(ContentAssistPreference.CONTEXT_ID);
	}
	
	
	private RCodeScanner fCodeScanner;
	private RInfixOperatorScanner fInfixScanner;
	private CommentScanner fCommentScanner;
	private SingleTokenScanner fStringScanner;
	
	private RDoubleClickStrategy fDoubleClickStrategy;
	
	private REditor fEditor;
	private IRCoreAccess fRCoreAccess;

	
	public RSourceViewerConfiguration(REditor editor, 
			ColorManager colorManager, ICombinedPreferenceStore preferenceStore) {

		super(colorManager, preferenceStore);
		fEditor = editor;
		fRCoreAccess = editor;
	}

	public RSourceViewerConfiguration(IRCoreAccess rCoreAccess, 
			ColorManager colorManager, ICombinedPreferenceStore preferenceStore) {

		super(colorManager, preferenceStore);
		fRCoreAccess = rCoreAccess;
	}
	
	
	/**
	 * Initializes the scanners.
	 */
	protected StatextTextScanner[] initializeScanners() {

		ICombinedPreferenceStore store = getPreferences();
		fCodeScanner = new RCodeScanner(fColorManager, store);
		fInfixScanner = new RInfixOperatorScanner(fColorManager, store);
		fCommentScanner = new CommentScanner(fColorManager, store, IRTextTokens.COMMENT, IRTextTokens.TASK_TAG);
		fStringScanner = new SingleTokenScanner(fColorManager, store, IRTextTokens.STRING);
		
		fDoubleClickStrategy = new RDoubleClickStrategy();
		
		return new StatextTextScanner[] { fCodeScanner, fInfixScanner, fCommentScanner, fStringScanner };
	}

	
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {

		return IRDocumentPartitions.R_PARTITIONS;
	}

	@Override
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {

		return IRDocumentPartitions.R_DOCUMENT_PARTITIONING;
	}
	
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {

		return fDoubleClickStrategy;
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {

		PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(fCodeScanner);
		reconciler.setDamager(dr, IRDocumentPartitions.R_DEFAULT);
		reconciler.setRepairer(dr, IRDocumentPartitions.R_DEFAULT);

		dr = new DefaultDamagerRepairer(fInfixScanner);
		reconciler.setDamager(dr, IRDocumentPartitions.R_INFIX_OPERATOR);
		reconciler.setRepairer(dr, IRDocumentPartitions.R_INFIX_OPERATOR);
		
		dr = new DefaultDamagerRepairer(fStringScanner);
		reconciler.setDamager(dr, IRDocumentPartitions.R_STRING);
		reconciler.setRepairer(dr, IRDocumentPartitions.R_STRING);

		dr = new DefaultDamagerRepairer(fCommentScanner);
		reconciler.setDamager(dr, IRDocumentPartitions.R_COMMENT);
		reconciler.setRepairer(dr, IRDocumentPartitions.R_COMMENT);

		return reconciler;
	}

	@Override
	public int getTabWidth(ISourceViewer sourceViewer) {
		
		return fRCoreAccess.getRCodeStyle().getTabSize();
	}

	@Override
	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
		
		return new String[] { "#", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public String[] getIndentPrefixes(ISourceViewer sourceViewer,
			String contentType) {
		
		String[] prefixes = getIndentPrefixesForTab(getTabWidth(sourceViewer));
		RCodeStyleSettings codeStyle = fRCoreAccess.getRCodeStyle();
		if (codeStyle.getIndentDefaultType() == IndentationType.SPACES) {
			String[] enhPrefixes = new String[prefixes.length+1];
			enhPrefixes[0] = new String(RIndentation.repeat(' ', codeStyle.getIndentSpacesCount()));
			System.arraycopy(prefixes, 0, enhPrefixes, 1, prefixes.length);
			return enhPrefixes;
		}
		return prefixes;
	}
	
	@Override
	public boolean affectsTextPresentation(PropertyChangeEvent event) {
		
		String property = event.getProperty();
		return (property.startsWith(RUIPreferenceConstants.R.TS_ROOT) 
				|| property.equals(TaskTagsPreferences.PREF_TAGS.getKey()) );
	}
	
	public void handleSettingsChange(Set<String> contexts) {
		if (contexts.contains(ContentAssistPreference.CONTEXT_ID)) {
			ContentAssistPreference.configure(fContentAssistant);
		}
	}
	
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		if (fEditor != null && fContentAssistant == null) {
			ContentAssistant assist = new ContentAssistant();
			REditorTemplatesCompletionProcessor processor = new REditorTemplatesCompletionProcessor(fEditor);
			
			assist.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
			for (String contentType : getConfiguredContentTypes(sourceViewer)) {
				assist.setContentAssistProcessor(processor, contentType);
			}
	
			ContentAssistPreference.configure(assist);
			
			assist.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
			assist.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
	
			assist.setInformationControlCreator(getInformationControlCreator(sourceViewer));
	
			fContentAssistant = assist;
		}
		return fContentAssistant;
	}
	
}