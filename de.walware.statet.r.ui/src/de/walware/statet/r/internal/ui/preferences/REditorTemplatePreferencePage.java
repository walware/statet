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

package de.walware.statet.r.internal.ui.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ltk.ui.sourceediting.SourceViewerJFaceUpdater;
import de.walware.ecommons.ltk.ui.sourceediting.ViewerSourceEditorAdapter;
import de.walware.ecommons.preferences.ui.SettingsUpdater;
import de.walware.ecommons.templates.TemplateVariableProcessor;
import de.walware.ecommons.text.Partitioner;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.RTemplateSourceViewerConfigurator;
import de.walware.statet.r.ui.editors.templates.REditorTemplatesContextType;


/**
 * @see org.eclipse.jface.preference.PreferencePage
 */
public class REditorTemplatePreferencePage extends TemplatePreferencePage {
	
//	private FormattingPreferences fFormattingPreferences= new FormattingPreferences();
	SourceEditorViewerConfigurator fViewerConfigurator;
	TemplateVariableProcessor fTemplateProcessor;
	SourceEditorViewerConfigurator fDialogViewerConfigurator; // Dialog kann im anderen Context sein
	TemplateVariableProcessor fDialogTemplateProcessor;
	
	
	public REditorTemplatePreferencePage() {
		setPreferenceStore(RUIPlugin.getDefault().getPreferenceStore());
		setTemplateStore(RUIPlugin.getDefault().getREditorTemplateStore());
		setContextTypeRegistry(RUIPlugin.getDefault().getREditorTemplateContextRegistry());
		
		fTemplateProcessor = new TemplateVariableProcessor();
		fViewerConfigurator = new RTemplateSourceViewerConfigurator(
				RCore.getWorkbenchAccess(),
				fTemplateProcessor);
		
		fDialogTemplateProcessor = new TemplateVariableProcessor();
		fDialogViewerConfigurator = new RTemplateSourceViewerConfigurator(
				RCore.getWorkbenchAccess(),
				fDialogTemplateProcessor);
	}
	
	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);
		setTitle(Messages.REditorTemplates_title);
	}
	
	@Override
	protected SourceViewer createViewer(final Composite parent) {
		final SourceViewer viewer = new SourceViewer(parent, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.setEditable(false);	
		viewer.getTextWidget().setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
		
		final ViewerSourceEditorAdapter adapter = new ViewerSourceEditorAdapter(viewer, null);
		fViewerConfigurator.setTarget(adapter);
		// updater
		new SettingsUpdater(fViewerConfigurator, viewer.getControl());
		new SourceViewerJFaceUpdater(viewer, 
				fViewerConfigurator.getSourceViewerConfiguration());
		
		final IDocument document = new Document();
		fViewerConfigurator.getDocumentSetupParticipant().setup(document);
		viewer.setDocument(document);
		
		return viewer;
	}
	
	@Override
	protected void updateViewerInput() {
		super.updateViewerInput();
		
		final IStructuredSelection selection = (IStructuredSelection) getTableViewer().getSelection();
		
		if (selection.size() == 1) {
			final TemplatePersistenceData data = (TemplatePersistenceData) selection.getFirstElement();
			final Template template = data.getTemplate();
			final TemplateContextType contextType = getContextTypeRegistry().getContextType(template.getContextTypeId());
			fTemplateProcessor.setContextType(contextType);
			final AbstractDocument document = (AbstractDocument) getViewer().getDocument();
			configureContext(document, contextType, fViewerConfigurator);
		}
	}
	
	@Override
	protected Template editTemplate(final Template template, final boolean edit, final boolean isNameModifiable) {
		final de.walware.ecommons.ltk.ui.templates.EditTemplateDialog dialog = new de.walware.ecommons.ltk.ui.templates.EditTemplateDialog(
				getShell(), template, edit, isNameModifiable, 
				fDialogViewerConfigurator, fDialogTemplateProcessor, getContextTypeRegistry()) {
			
			@Override
			protected void configureForContext(final TemplateContextType contextType) {
				super.configureForContext(contextType);
				final SourceViewer sourceViewer = getSourceViewer();
				final AbstractDocument document = (AbstractDocument) sourceViewer.getDocument();
				REditorTemplatePreferencePage.this.configureContext(document, contextType, getSourceViewerConfigurator());
			}
		};
		if (dialog.open() == Dialog.OK) {
			return dialog.getTemplate();
		}
		return null;
	}
	
	protected void configureContext(final AbstractDocument document, final TemplateContextType contextType, final SourceEditorViewerConfigurator configurator) {
		final Partitioner partitioner = (Partitioner) document.getDocumentPartitioner(configurator.getPartitioning().getPartitioning());
		if (contextType.getId().equals(REditorTemplatesContextType.ROXYGEN_CONTEXTTYPE)) {
			partitioner.setStartPartitionType(IRDocumentPartitions.R_ROXYGEN);
		}
		else {
			partitioner.setStartPartitionType(IRDocumentPartitions.R_DEFAULT_EXPL);
		}
		partitioner.disconnect();
		partitioner.connect(document);
		document.setDocumentPartitioner(configurator.getPartitioning().getPartitioning(), partitioner);
	}
	
	@Override
	protected boolean isShowFormatterSetting() {
		return false;
	}
	
	@Override
	protected String getFormatterPreferenceKey() {
		return null;
	}
	
}
