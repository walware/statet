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

import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import de.walware.eclipsecommons.templates.TemplateVariableProcessor;

import de.walware.statet.base.core.StatetProject;
import de.walware.statet.base.ui.StatetUIServices;
import de.walware.statet.ext.ui.editors.StatextSourceViewerConfiguration;
import de.walware.statet.ext.ui.preferences.ICodeGenerationTemplatesCategory;
import de.walware.statet.ext.ui.preferences.TemplateViewerConfigurationProvider;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.RSourceViewerConfiguration;
import de.walware.statet.r.ui.editors.RdDocumentSetupParticipant;
import de.walware.statet.r.ui.editors.RdSourceViewerConfiguration;


/**
 * Integrates the R templates into the common StatET template
 * preference page. 
 */
public class RdCodeTemplatesProvider implements ICodeGenerationTemplatesCategory {

	
	public RdCodeTemplatesProvider() {
	}
	
	public TemplateStore getTemplateStore() {
		return RUIPlugin.getDefault().getRdCodeGenerationTemplateStore();
	}

	public ContextTypeRegistry getContextTypeRegistry() {
		return RUIPlugin.getDefault().getRdCodeGenerationTemplateContextRegistry();
	}

	public TemplateViewerConfigurationProvider getEditTemplateDialogConfiguation(final TemplateVariableProcessor processor, StatetProject project) {

		StatextSourceViewerConfiguration configuration = new RdSourceViewerConfiguration(
				StatetUIServices.getSharedColorManager(), 
				RSourceViewerConfiguration.createCombinedPreferenceStore(
						RUIPlugin.getDefault().getPreferenceStore(), project)
				) {
			
			@Override
			public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
				
				return getTemplateVariableContentAssistant(sourceViewer, processor);
			}	
	
			@Override
			public int[] getConfiguredTextHoverStateMasks(ISourceViewer sourceViewer, String contentType) {
				
				return new int[] { ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK };
			}

			@Override
			public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
				
				return new TemplateVariableTextHover(processor);
			}
		
		};
		
		return new TemplateViewerConfigurationProvider(
				configuration,
				new RdDocumentSetupParticipant(),
				RUIPlugin.getDefault().getPreferenceStore() );
	}

}
