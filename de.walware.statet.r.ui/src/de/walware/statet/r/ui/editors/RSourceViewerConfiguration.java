/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.texteditor.ITextEditor;

import de.walware.eclipsecommon.ui.util.ColorManager;
import de.walware.statet.base.StatetPreferenceConstants;
import de.walware.statet.ext.ui.editors.ContentAssistPreference;
import de.walware.statet.ext.ui.editors.StatextSourceViewerConfiguration;
import de.walware.statet.ext.ui.text.CommentScanner;
import de.walware.statet.ext.ui.text.SingleTokenScanner;
import de.walware.statet.ext.ui.text.StatextTextScanner;
import de.walware.statet.r.ui.IRDocumentPartitions;
import de.walware.statet.r.ui.RUiPreferenceConstants;
import de.walware.statet.r.ui.editors.templates.REditorTemplatesCompletionProcessor;
import de.walware.statet.r.ui.text.r.IRTextTokens;
import de.walware.statet.r.ui.text.r.RCodeScanner;
import de.walware.statet.r.ui.text.r.RDoubleClickStrategy;


/**
 * Default Configuration for SourceViewer of R code.
 * 
 * @author Stephan Wahlbrink
 */
public class RSourceViewerConfiguration extends StatextSourceViewerConfiguration {

	private RCodeScanner fCodeScanner;
	private CommentScanner fCommentScanner;
	private SingleTokenScanner fStringScanner;
	
	private RDoubleClickStrategy fDoubleClickStrategy;
	
	private ITextEditor fEditor;

	
	public RSourceViewerConfiguration(ITextEditor editor, 
			ColorManager colorManager, IPreferenceStore preferenceStore) {

		super(colorManager, preferenceStore);
		fEditor = editor;
	}
	
	/**
	 * Initializes the scanners.
	 */
	protected StatextTextScanner[] initializeScanners() {

		fCodeScanner = new RCodeScanner(fColorManager, fPreferenceStore);
		fCommentScanner = new CommentScanner(fColorManager, fPreferenceStore, IRTextTokens.COMMENT, IRTextTokens.TASK_TAG);
		fStringScanner = new SingleTokenScanner(fColorManager, fPreferenceStore, IRTextTokens.STRING);
		
		fDoubleClickStrategy = new RDoubleClickStrategy();
		
		return new StatextTextScanner[] { fCodeScanner, fCommentScanner, fStringScanner };
	}

	
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {

		return IRDocumentPartitions.R_PARTITIONS;
	}

	/* 
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredDocumentPartitioning(org.eclipse.jface.text.source.ISourceViewer) 
	 */
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {

		return IRDocumentPartitions.R_DOCUMENT_PARTITIONING;
	}
	
	/* 
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getDoubleClickStrategy(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
	 */
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {

		return fDoubleClickStrategy;
	}

	/* 
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer) 
	 */
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {

		PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(fCodeScanner);
		reconciler.setDamager(dr, IRDocumentPartitions.R_DEFAULT);
		reconciler.setRepairer(dr, IRDocumentPartitions.R_DEFAULT);

		dr = new DefaultDamagerRepairer(fStringScanner);
		reconciler.setDamager(dr, IRDocumentPartitions.R_STRING);
		reconciler.setRepairer(dr, IRDocumentPartitions.R_STRING);

		dr = new DefaultDamagerRepairer(fCommentScanner);
		reconciler.setDamager(dr, IRDocumentPartitions.R_COMMENT);
		reconciler.setRepairer(dr, IRDocumentPartitions.R_COMMENT);

		return reconciler;
	}

	/* @see SourceViewerConfiguration#getDefaultPrefixes(ISourceViewer, String)	*/
	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
		
		return new String[] { "#", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public boolean affectsTextPresentation(PropertyChangeEvent event) {
		
		String property = event.getProperty();
		return property.startsWith(RUiPreferenceConstants.R.TS_ROOT) 
				|| property.equals(StatetPreferenceConstants.TASK_TAGS);
	}
	
	@Override
	public void handlePropertyChangeEvent(PropertyChangeEvent event) {
		
		super.handlePropertyChangeEvent(event);
		
		if (event.getProperty().equals(StatetPreferenceConstants.TASK_TAGS))
			fCommentScanner.loadTaskTags();
	}
	
	
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

		if (fEditor != null) {
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
			return fContentAssistant;
		}
		return null;
	}
}