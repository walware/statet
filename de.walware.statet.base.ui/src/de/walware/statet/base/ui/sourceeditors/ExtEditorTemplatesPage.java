/*******************************************************************************
 * Copyright (c) 2008-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.jface.text.AbstractDocument;
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
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.templates.AbstractTemplatesPage;
import org.eclipse.ui.texteditor.templates.ITemplatesPage;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ltk.ui.sourceediting.ViewerSourceEditorAdapter;
import de.walware.ecommons.ltk.ui.sourceediting.assist.TemplateProposal;
import de.walware.ecommons.preferences.ui.SettingsUpdater;
import de.walware.ecommons.templates.TemplateVariableProcessor;
import de.walware.ecommons.text.ui.TextViewerEditorColorUpdater;
import de.walware.ecommons.text.ui.TextViewerJFaceUpdater;
import de.walware.ecommons.ui.ISettingsChangedHandler;
import de.walware.ecommons.ui.util.UIAccess;


/**
 * Abstract {@link ITemplatesPage} for StatextEditor1/SourceViewerConfigurator
 */
public abstract class ExtEditorTemplatesPage extends AbstractTemplatesPage {
	
	
	private final SourceEditor1 fEditor;
	
	private ISourceEditor fPreviewEditor;
	private final TemplateVariableProcessor fPreviewTemplateProcessor;
	private final TemplateVariableProcessor fEditTemplateProcessor;
	
	private SourceEditorViewerConfigurator fCurrentPreviewConfigurator;
	private TextViewerJFaceUpdater fCurrentPreviewUpdater;
	
	
	protected ExtEditorTemplatesPage(final SourceEditor1 editor, final ISourceViewer viewer) {
		super(editor, viewer);
		
		fEditor = editor;
		
		fPreviewTemplateProcessor = new TemplateVariableProcessor();
		fEditTemplateProcessor = new TemplateVariableProcessor();
	}
	
	
	protected SourceEditor1 getEditor() {
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
		final ISourceEditor sourceEditor = (ISourceEditor) fEditor.getAdapter(ISourceEditor.class);
		if (!sourceEditor.isEditable(true)) {
			return;
		}
		final SourceViewer sourceViewer = sourceEditor.getViewer();
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
		new TextViewerEditorColorUpdater(viewer, EditorsUI.getPreferenceStore());
		
		final IDocument document = new Document();
		viewer.setDocument(document);
		
		fPreviewEditor = new ViewerSourceEditorAdapter(viewer, null);
		new SettingsUpdater(new ISettingsChangedHandler() {
			@Override
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
		if (patternViewer == null || !(UIAccess.isOkToUse(patternViewer.getControl())) ) {
			return;
		}
		
		if (template != null) {
			final SourceEditorViewerConfigurator configurator = getTemplatePreviewConfig(template, fPreviewTemplateProcessor);
			final TemplateContextType type = getContextTypeRegistry().getContextType(template.getContextTypeId());
			fPreviewTemplateProcessor.setContextType(type);
			
			if (configurator != fCurrentPreviewConfigurator) {
				if (fCurrentPreviewUpdater != null) {
					fCurrentPreviewUpdater.dispose();
					fCurrentPreviewUpdater = null;
				}
				if (fCurrentPreviewConfigurator != null) {
					fCurrentPreviewConfigurator.unconfigureTarget();
				}
				
				fCurrentPreviewConfigurator = configurator;
				fCurrentPreviewConfigurator.setTarget(fPreviewEditor);
				fCurrentPreviewUpdater = new TextViewerJFaceUpdater(patternViewer,
						fCurrentPreviewConfigurator.getSourceViewerConfiguration().getPreferences() );
				
				final AbstractDocument document = new Document();
				fCurrentPreviewConfigurator.getDocumentSetupParticipant().setup(document);
				configureDocument(document, type, configurator);
				document.set(template.getPattern());
				patternViewer.setDocument(document);
			}
			else {
				final AbstractDocument document = (AbstractDocument) patternViewer.getDocument();
				document.set(""); //$NON-NLS-1$
				configureDocument(document, type, configurator);
				document.set(template.getPattern());
			}
			
		}
		else {
			patternViewer.getDocument().set(""); //$NON-NLS-1$
		}
		patternViewer.setSelectedRange(0, 0);
	}
	
	@Override
	protected Template editTemplate(final Template template, final boolean edit, final boolean isNameModifiable) {
		final SourceEditorViewerConfigurator configurator = getTemplateEditConfig(template, fEditTemplateProcessor);
		final de.walware.ecommons.ltk.ui.templates.EditTemplateDialog dialog = new de.walware.ecommons.ltk.ui.templates.EditTemplateDialog(
				getSite().getShell(), template, edit,
				de.walware.ecommons.ltk.ui.templates.EditTemplateDialog.EDITOR_TEMPLATE,
				configurator, fEditTemplateProcessor, getContextTypeRegistry()) {
			
			@Override
			protected void configureForContext(final TemplateContextType contextType) {
				super.configureForContext(contextType);
				final SourceViewer sourceViewer = getSourceViewer();
				final AbstractDocument document = (AbstractDocument) sourceViewer.getDocument();
				ExtEditorTemplatesPage.this.configureDocument(document, contextType, getSourceViewerConfigurator());
			}
		};
		if (dialog.open() == Dialog.OK) {
			return dialog.getTemplate();
		}
		return null;
	}
	
	
	protected abstract DocumentTemplateContext createContext(final IDocument document, final Template template, final int offset, final int length);
	
	protected abstract SourceEditorViewerConfigurator getTemplatePreviewConfig(final Template template, final TemplateVariableProcessor templateProcessor);
	
	protected abstract SourceEditorViewerConfigurator getTemplateEditConfig(final Template template, final TemplateVariableProcessor templateProcessor);
	
	/**
	 * Can be implemented to configure the document when the context is changed
	 * 
	 * @param document the document to adapt
	 * @param contextType the new context
	 * @param configurator the configurator of the viewer/document (preview or edit)
	 */
	protected void configureDocument(final AbstractDocument document, final TemplateContextType contextType, final SourceEditorViewerConfigurator configurator) {
	}
	
}
