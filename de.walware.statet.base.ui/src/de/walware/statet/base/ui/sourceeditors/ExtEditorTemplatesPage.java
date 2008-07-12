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

import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.templates.AbstractTemplatesPage;
import org.eclipse.ui.texteditor.templates.ITemplatesPage;

import de.walware.eclipsecommons.templates.TemplateVariableProcessor;
import de.walware.eclipsecommons.ui.text.sourceediting.TemplateProposal;
import de.walware.eclipsecommons.ui.util.ISettingsChangedHandler;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.util.SettingsUpdater;
import de.walware.statet.ext.ui.dialogs.ViewerEditorAdapter;


/**
 * Abstract {@link ITemplatesPage} for StatextEditor1/SourceViewerConfigurator
 */
public abstract class ExtEditorTemplatesPage extends AbstractTemplatesPage {
	
	
	private final StatextEditor1 fEditor;
	
	private IEditorAdapter fPreviewEditor;
	private final TemplateVariableProcessor fPreviewTemplateProcessor;
	private final TemplateVariableProcessor fEditTemplateProcessor;
	
	private SourceViewerConfigurator fCurrentPreviewConfigurator;
	private SourceViewerUpdater fCurrentPreviewUpdater;
	
	
	protected ExtEditorTemplatesPage(final StatextEditor1 editor, final ISourceViewer viewer) {
		super(editor, viewer);
		
		fEditor = editor;
		
		fPreviewTemplateProcessor = new TemplateVariableProcessor();
		fEditTemplateProcessor = new TemplateVariableProcessor();
	}
	
	
	protected StatextEditor1 getEditor() {
		return fEditor;
	}
	
	
	@Override
	protected boolean isValidTemplate(final IDocument document, final Template template,
			final int offset, final int length) {
		final String[] contextIds = getContextTypeIds(document, offset);
		for (int i= 0; i < contextIds.length; i++) {
			if (contextIds[i].equals(template.getContextTypeId())) {
				final DocumentTemplateContext context = createContext(document, template, offset, length);
				return context.canEvaluate(template);
			}
		}
		return false;
	}
	
	@Override
	protected void insertTemplate(final Template template, final IDocument document) {
		final IEditorAdapter adapter = (IEditorAdapter) fEditor.getAdapter(IEditorAdapter.class);
		if (!adapter.isEditable(true)) {
			return;
		}
		final SourceViewer sourceViewer = adapter.getSourceViewer();
		final Point selectedRange = sourceViewer.getSelectedRange();
		final DocumentTemplateContext context = createContext(document, template, selectedRange.x, selectedRange.y);
		if (context == null) {
			return;
		}
		final IRegion region = new Region(selectedRange.x, selectedRange.y);
		final TemplateProposal proposal = new TemplateProposal(template, context, region, null, 0);
		fEditor.getSite().getPage().activate(fEditor);
		proposal.apply(sourceViewer, (char) 0, 0, region.getOffset());
	}
	
	@Override
	protected SourceViewer createPatternViewer(final Composite parent) {
		final SourceViewer viewer = new SourceViewer(parent, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.setEditable(false);
		viewer.getTextWidget().setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
		
		final IDocument document = new Document();
		viewer.setDocument(document);
		
		fPreviewEditor = new ViewerEditorAdapter(viewer, null);
		new SettingsUpdater(new ISettingsChangedHandler() {
			public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
				if (fCurrentPreviewConfigurator != null) {
					fCurrentPreviewConfigurator.handleSettingsChanged(groupIds, options);
				}
			}
		}, viewer.getControl());
		
		return viewer;
	}
	
	@Override
	protected void updatePatternViewer(final Template template) {
		final SourceViewer patternViewer = getPatternViewer();
		if (patternViewer == null || !(UIAccess.isOkToUse(patternViewer.getControl())) )
			return;
		
		if (template != null) {
			final SourceViewerConfigurator configurator = getTemplatePreviewConfig(template, fPreviewTemplateProcessor);
			final TemplateContextType type = getContextTypeRegistry().getContextType(template.getContextTypeId());
			fPreviewTemplateProcessor.setContextType(type);
			
			if (configurator != fCurrentPreviewConfigurator) {
				if (fCurrentPreviewUpdater != null) {
					fCurrentPreviewUpdater.unregister();
					fCurrentPreviewUpdater = null;
				}
				if (fCurrentPreviewConfigurator != null) {
					fCurrentPreviewConfigurator.unconfigureTarget();
				}
				
				fCurrentPreviewConfigurator = configurator;
				fCurrentPreviewConfigurator.setTarget(fPreviewEditor, true);
				fCurrentPreviewUpdater = new SourceViewerUpdater(patternViewer, 
						fCurrentPreviewConfigurator.getSourceViewerConfiguration(), 
						fCurrentPreviewConfigurator.getPreferenceStore());
				
				final IDocument document = new Document(template.getPattern());
				fCurrentPreviewConfigurator.getDocumentSetupParticipant().setup(document);
				patternViewer.setDocument(document);
				
			}
			else {
				patternViewer.getDocument().set(template.getPattern());
			}
			
		} else {
			patternViewer.getDocument().set(""); //$NON-NLS-1$
		}
	}
	
	@Override
	protected Template editTemplate(final Template template, final boolean edit, final boolean isNameModifiable) {
		final SourceViewerConfigurator configurator = getTemplateEditConfig(template, fEditTemplateProcessor);
		final de.walware.statet.ext.ui.preferences.EditTemplateDialog dialog = new de.walware.statet.ext.ui.preferences.EditTemplateDialog(
				getSite().getShell(), template, edit, isNameModifiable, 
				configurator, fEditTemplateProcessor, getContextTypeRegistry());
		if (dialog.open() == Dialog.OK) {
			return dialog.getTemplate();
		}
		return null;
	}
	
	
	protected abstract DocumentTemplateContext createContext(final IDocument document, final Template template, final int offset, final int length);
	
	protected abstract SourceViewerConfigurator getTemplatePreviewConfig(Template template, final TemplateVariableProcessor templateProcessor);
	
	protected abstract SourceViewerConfigurator getTemplateEditConfig(Template template, final TemplateVariableProcessor templateProcessor);
	
}
