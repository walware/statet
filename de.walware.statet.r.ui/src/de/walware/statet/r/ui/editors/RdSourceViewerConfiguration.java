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

import static de.walware.statet.r.ui.IRDocumentPartitions.RDOC_COMMENT;
import static de.walware.statet.r.ui.IRDocumentPartitions.RDOC_DEFAULT;
import static de.walware.statet.r.ui.IRDocumentPartitions.RDOC_DOCUMENT_PARTITIONING;
import static de.walware.statet.r.ui.IRDocumentPartitions.RDOC_PARTITIONS;
import static de.walware.statet.r.ui.IRDocumentPartitions.RDOC_PLATFORM_SPECIF;
import static de.walware.statet.r.ui.text.rd.IRdTextTokens.COMMENT;
import static de.walware.statet.r.ui.text.rd.IRdTextTokens.PLATFORM_SPECIF;
import static de.walware.statet.r.ui.text.rd.IRdTextTokens.TASK_TAG;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;

import de.walware.eclipsecommon.ui.util.ColorManager;
import de.walware.statet.base.StatetPreferenceConstants;
import de.walware.statet.ext.ui.editors.StatextSourceViewerConfiguration;
import de.walware.statet.ext.ui.text.CommentScanner;
import de.walware.statet.ext.ui.text.SingleTokenScanner;
import de.walware.statet.ext.ui.text.StatextTextScanner;
import de.walware.statet.r.ui.RUiPreferenceConstants;
import de.walware.statet.r.ui.text.rd.RdCodeScanner;
import de.walware.statet.r.ui.text.rd.RdDoubleClickStrategy;


/**
 * Default Configuration for SourceViewer of R code.
 * 
 * @author Stephan Wahlbrink
 */
public class RdSourceViewerConfiguration extends StatextSourceViewerConfiguration {


	private RdCodeScanner fDocScanner;
	private CommentScanner fCommentScanner;
	private SingleTokenScanner fPlatformSpecifScanner;
	
	private RdDoubleClickStrategy fDoubleClickStrategy;

	
	public RdSourceViewerConfiguration(ColorManager colorManager, IPreferenceStore preferenceStore) {

		super(colorManager, preferenceStore);
	}
	
	/**
	 * Initializes the scanners.
	 */
	protected StatextTextScanner[] initializeScanners() {

		fDocScanner = new RdCodeScanner(fColorManager, fPreferenceStore);
		fCommentScanner = new CommentScanner(fColorManager, fPreferenceStore, COMMENT, TASK_TAG);
		fPlatformSpecifScanner = new SingleTokenScanner(fColorManager, fPreferenceStore, PLATFORM_SPECIF);
		
		fDoubleClickStrategy = new RdDoubleClickStrategy();
		
		return new StatextTextScanner[] { fDocScanner, fCommentScanner, fPlatformSpecifScanner };
	}

	
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {

		return RDOC_PARTITIONS;
	}

	/* 
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredDocumentPartitioning(org.eclipse.jface.text.source.ISourceViewer) 
	 */
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {

		return RDOC_DOCUMENT_PARTITIONING;
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

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(fDocScanner);
		reconciler.setDamager(dr, RDOC_DEFAULT);
		reconciler.setRepairer(dr, RDOC_DEFAULT);

		dr = new DefaultDamagerRepairer(fCommentScanner);
		reconciler.setDamager(dr, RDOC_COMMENT);
		reconciler.setRepairer(dr, RDOC_COMMENT);

		dr = new DefaultDamagerRepairer(fPlatformSpecifScanner);
		reconciler.setDamager(dr, RDOC_PLATFORM_SPECIF);
		reconciler.setRepairer(dr, RDOC_PLATFORM_SPECIF);

		return reconciler;
	}

	/* @see SourceViewerConfiguration#getDefaultPrefixes(ISourceViewer, String)	*/
	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[] { "%", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public boolean affectsTextPresentation(PropertyChangeEvent event) {
		
		String property = event.getProperty();
		return property.startsWith(RUiPreferenceConstants.Rd.TS_ROOT)
				|| property.equals(StatetPreferenceConstants.TASK_TAGS);
	}
	
	@Override
	public void handlePropertyChangeEvent(PropertyChangeEvent event) {
		
		super.handlePropertyChangeEvent(event);
		
		if (event.getProperty().equals(StatetPreferenceConstants.TASK_TAGS))
			fCommentScanner.loadTaskTags();
	}
	
}