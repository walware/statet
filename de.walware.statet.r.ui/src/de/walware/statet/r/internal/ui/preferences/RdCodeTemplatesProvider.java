/*=============================================================================#
 # Copyright (c) 2005-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.templates.TemplateVariableProcessor;

import de.walware.statet.ext.templates.ICodeGenerationTemplatesCategory;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RProjects;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.RdSourceViewerConfiguration;
import de.walware.statet.r.ui.editors.RdSourceViewerConfigurator;


/**
 * Integrates the R templates into the common StatET template
 * preference page.
 */
public class RdCodeTemplatesProvider implements ICodeGenerationTemplatesCategory {
	
	
	public static class RdTemplateConfigurator extends RdSourceViewerConfigurator {
		
		public RdTemplateConfigurator(
				final IRCoreAccess rCoreAccess,
				final TemplateVariableProcessor processor) {
			super(rCoreAccess, new RdSourceViewerConfiguration() {
				
				@Override
				protected ContentAssistant createContentAssistant(final ISourceViewer sourceViewer) {
					return createTemplateVariableContentAssistant(sourceViewer, processor);
				}
				
				@Override
				public int[] getConfiguredTextHoverStateMasks(final ISourceViewer sourceViewer, final String contentType) {
					return new int[] { ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK };
				}
				
				@Override
				public ITextHover getTextHover(final ISourceViewer sourceViewer, final String contentType, final int stateMask) {
					return new TemplateVariableTextHover(processor);
				}
			});
		}
	}
	
	
	public RdCodeTemplatesProvider() {
	}
	
	@Override
	public String getProjectNatureId() {
		return RProjects.R_NATURE_ID;
	}
	
	@Override
	public TemplateStore getTemplateStore() {
		return RUIPlugin.getDefault().getRdCodeGenerationTemplateStore();
	}
	
	@Override
	public ContextTypeRegistry getContextTypeRegistry() {
		return RUIPlugin.getDefault().getRdCodeGenerationTemplateContextRegistry();
	}
	
	@Override
	public SourceEditorViewerConfigurator getEditTemplateDialogConfiguator(final TemplateVariableProcessor processor, final IProject project) {
		return new RdTemplateConfigurator(RProjects.getRProject(project), processor);
	}
	
}
