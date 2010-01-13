/*******************************************************************************
 * Copyright (c) 2008-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.workbench;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.ContentMergeViewer;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.ui.preferences.SettingsUpdater;
import de.walware.ecommons.ui.text.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ui.text.sourceediting.ViewerSourceEditorAdapter;


/**
 * {@link ContentMergeViewer} for source code using a {@link SourceEditorViewerConfigurator}
 * to setup the text viewers.
 */
public abstract class CompareMergeTextViewer extends TextMergeViewer {
	
	
	private List<TextViewer> fTextViewers;
	private IDocumentSetupParticipant fDocumentSetupParticipant;
	
	
	public CompareMergeTextViewer(final Composite parent, final CompareConfiguration configuration) {
		super(parent, SWT.LEFT_TO_RIGHT, configuration);
	}
	
	
	@Override
	protected void setupDocument(final IDocument document) {
		if (fDocumentSetupParticipant == null) {
			fDocumentSetupParticipant = createDocumentSetupParticipant();
		}
		fDocumentSetupParticipant.setup(document);
	}
	
	protected abstract IDocumentSetupParticipant createDocumentSetupParticipant();
	protected abstract SourceEditorViewerConfigurator createConfigurator(SourceViewer sourceViewer);
	
	@Override
	protected void configureTextViewer(final TextViewer textViewer) {
		if (textViewer instanceof SourceViewer) {
			final SourceViewer sourceViewer = (SourceViewer) textViewer;
			
			final SourceEditorViewerConfigurator configurator = createConfigurator(sourceViewer);
			configurator.setTarget(new ViewerSourceEditorAdapter(sourceViewer, configurator));
			
			new SettingsUpdater(configurator, sourceViewer.getControl());
		}
		else {
			super.configureTextViewer(textViewer);
		}
		
		if (fTextViewers == null) {
			fTextViewers = new ArrayList<TextViewer>(3);
		}
		fTextViewers.add(textViewer);
	}
	
}
