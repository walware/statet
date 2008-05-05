/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui.sourceeditors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.ContentMergeViewer;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.internal.MergeSourceViewer;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;

import de.walware.statet.base.ui.util.SettingsUpdater;


/**
 * {@link ContentMergeViewer} for source code using a {@link SourceViewerConfigurator}
 * to setup the text viewers.
 */
public abstract class CompareMergeTextViewer extends TextMergeViewer {
	
	
	private static final String SYMBOLIC_FONT_NAME = TextMergeViewer.class.getName();
	
	
	private static class ViewerEditorAdapter implements IEditorAdapter {
		
		
		private SourceViewer fEditorViewer;
		private SourceViewerConfigurator fConfigurator;
		
		
		public ViewerEditorAdapter(final SourceViewer viewer, final SourceViewerConfigurator configurator) {
			fEditorViewer = viewer;
			fConfigurator = configurator;
		}
		
		
		public SourceViewer getSourceViewer() {
			return fEditorViewer;
		}
		
		public IWorkbenchPart getWorkbenchPart() {
			return null;
		}
		
		public void install(final IEditorInstallable installable) {
			if (fConfigurator != null) {
				fConfigurator.installModul(installable);
			}
		}
		
		public boolean isEditable(final boolean validate) {
			return fEditorViewer.isEditable();
		}
		
		public void setStatusLineErrorMessage(final String message) {
		}
		
		public Object getAdapter(final Class adapter) {
			return null;
		}
		
	}
	
	private static void updateFont(final TextViewer viewer, final Font font) {
		if (viewer instanceof MergeSourceViewer) {
			((MergeSourceViewer) viewer).setFont(font);
		}
		else {
			viewer.getTextWidget().setFont(font);
		}
	}
	
	
	private List<TextViewer> fTextViewers;
	private IDocumentSetupParticipant fDocumentSetupParticipant;
	
	
	public CompareMergeTextViewer(final Composite parent, final CompareConfiguration configuration) {
		super(parent, SWT.LEFT_TO_RIGHT, configuration);
		
		// TOCHECK Eclipse bug #219757
		final Font font = JFaceResources.getFont(SYMBOLIC_FONT_NAME);
		if (fTextViewers != null) {
			for (final TextViewer viewer : fTextViewers) {
				updateFont(viewer, font);
			}
		}
	}
	
	
	@Override
	protected void setupDocument(final IDocument document) {
		if (fDocumentSetupParticipant == null) {
			fDocumentSetupParticipant = createDocumentSetupParticipant();
		}
		fDocumentSetupParticipant.setup(document);
	}
	
	protected abstract IDocumentSetupParticipant createDocumentSetupParticipant();
	protected abstract SourceViewerConfigurator createConfigurator(SourceViewer sourceViewer);
	
	@Override
	protected void configureTextViewer(final TextViewer textViewer) {
		final Font font = JFaceResources.getFont(SYMBOLIC_FONT_NAME);
		updateFont(textViewer, font);
		
		if (textViewer instanceof SourceViewer) {
			final SourceViewer sourceViewer = (SourceViewer) textViewer;
			
			final SourceViewerConfigurator configurator = createConfigurator(sourceViewer);
			configurator.setTarget(new ViewerEditorAdapter(sourceViewer, configurator), true);
			
			new SourceViewerUpdater(sourceViewer, configurator.getSourceViewerConfiguration(), configurator.getPreferenceStore(), SYMBOLIC_FONT_NAME) {
				@Override
				protected void updateFont(final SourceViewer viewer, final Font font) {
					CompareMergeTextViewer.updateFont(viewer, font);
				}
			};
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
