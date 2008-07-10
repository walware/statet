/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.eclipsecommons.templates.TemplateVariableProcessor;

import de.walware.statet.base.ui.sourceeditors.SourceViewerConfigurator;
import de.walware.statet.base.ui.sourceeditors.SourceViewerUpdater;
import de.walware.statet.base.ui.util.SettingsUpdater;
import de.walware.statet.ext.ui.dialogs.ViewerEditorAdapter;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.RTemplateSourceViewerConfigurator;


/**
 * @see org.eclipse.jface.preference.PreferencePage
 */
public class REditorTemplatePreferencePage extends TemplatePreferencePage {
	
//	private FormattingPreferences fFormattingPreferences= new FormattingPreferences();
	SourceViewerConfigurator fViewerConfigurator;
	TemplateVariableProcessor fTemplateProcessor;
	SourceViewerConfigurator fDialogViewerConfigurator; // Dialog kann im anderen Context sein
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
		
		final ViewerEditorAdapter adapter = new ViewerEditorAdapter(viewer, null);
		fViewerConfigurator.setTarget(adapter, true);
		// updater
		new SettingsUpdater(fViewerConfigurator, viewer.getControl());
		new SourceViewerUpdater(viewer, 
				fViewerConfigurator.getSourceViewerConfiguration(), 
				fViewerConfigurator.getPreferenceStore());
		
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
//			fPatternViewer.getDocument().set(template.getPattern());
			final TemplateContextType type = getContextTypeRegistry().getContextType(template.getContextTypeId());
			fTemplateProcessor.setContextType(type);
		} else {
//			fPatternViewer.getDocument().set(""); //$NON-NLS-1$
		}
	}
	
	@Override
	protected Template editTemplate(final Template template, final boolean edit, final boolean isNameModifiable) {
		final de.walware.statet.ext.ui.preferences.EditTemplateDialog dialog = new de.walware.statet.ext.ui.preferences.EditTemplateDialog(
				getShell(), template, edit, isNameModifiable, 
				fDialogViewerConfigurator, fDialogTemplateProcessor, getContextTypeRegistry());
		if (dialog.open() == Dialog.OK) {
			return dialog.getTemplate();
		}
		return null;
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
