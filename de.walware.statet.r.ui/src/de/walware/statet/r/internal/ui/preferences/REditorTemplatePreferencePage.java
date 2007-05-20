/*******************************************************************************
 * Copyright (c) 2005-2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
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

import de.walware.statet.base.ui.util.SettingsUpdater;
import de.walware.statet.ext.ui.editors.SourceViewerConfigurator;
import de.walware.statet.ext.ui.editors.SourceViewerUpdater;
import de.walware.statet.ext.ui.editors.StatextSourceViewerConfiguration;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.ui.RUIPlugin;


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
        fViewerConfigurator = new RCodeTemplatesProvider.RTemplateConfigurator(
        		RCore.getWorkbenchAccess(),
        		fTemplateProcessor);

        fDialogTemplateProcessor = new TemplateVariableProcessor();
        fDialogViewerConfigurator = new RCodeTemplatesProvider.RTemplateConfigurator(
        		RCore.getWorkbenchAccess(),
        		fDialogTemplateProcessor);
    }
    
    @Override
    public void setVisible(boolean visible) {
    	super.setVisible(visible);
    	setTitle(Messages.REditorTemplates_title);
    }

    @Override
	protected SourceViewer createViewer(Composite parent) {
    	SourceViewer viewer = new SourceViewer(parent, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.setEditable(false);	
		viewer.getTextWidget().setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
		
		StatextSourceViewerConfiguration configuration = fViewerConfigurator.getSourceViewerConfiguration();
		viewer.configure(configuration);
		// updater
		new SettingsUpdater(fViewerConfigurator, viewer.getControl());
		new SourceViewerUpdater(viewer, configuration, fViewerConfigurator.getPreferenceStore());

		IDocument document = new Document();
		fViewerConfigurator.getDocumentSetupParticipant().setup(document);
		viewer.setDocument(document);
		
		return viewer;
    }
    
    @Override
    protected void updateViewerInput() {
    	super.updateViewerInput();
    	
		IStructuredSelection selection = (IStructuredSelection) getTableViewer().getSelection();

		if (selection.size() == 1) {
			TemplatePersistenceData data = (TemplatePersistenceData) selection.getFirstElement();
			Template template = data.getTemplate();
//			fPatternViewer.getDocument().set(template.getPattern());
			TemplateContextType type = getContextTypeRegistry().getContextType(template.getContextTypeId());
			fTemplateProcessor.setContextType(type);
		} else {
//			fPatternViewer.getDocument().set(""); //$NON-NLS-1$
		}
    }
    
    @Override
    protected Template editTemplate(Template template, boolean edit, boolean isNameModifiable) {
    	de.walware.statet.ext.ui.preferences.EditTemplateDialog dialog = new de.walware.statet.ext.ui.preferences.EditTemplateDialog(
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