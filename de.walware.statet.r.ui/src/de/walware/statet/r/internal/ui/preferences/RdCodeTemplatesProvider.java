/*******************************************************************************
 * Copyright (c) 2005-2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import de.walware.eclipsecommons.templates.TemplateVariableProcessor;

import de.walware.statet.base.ui.StatetUIServices;
import de.walware.statet.base.ui.sourceeditors.SourceViewerConfigurator;
import de.walware.statet.ext.ui.preferences.ICodeGenerationTemplatesCategory;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RProject;
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
			super(rCoreAccess, RUIPlugin.getDefault().getEditorPreferenceStore());
			setConfiguration(new RdSourceViewerConfiguration(
					this,
					getPreferenceStore(),
					StatetUIServices.getSharedColorManager()) {
				
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
	
	public String getProjectNatureId() {
		return RProject.NATURE_ID;
	}
	
	public TemplateStore getTemplateStore() {
		return RUIPlugin.getDefault().getRdCodeGenerationTemplateStore();
	}
	
	public ContextTypeRegistry getContextTypeRegistry() {
		return RUIPlugin.getDefault().getRdCodeGenerationTemplateContextRegistry();
	}
	
	public SourceViewerConfigurator getEditTemplateDialogConfiguator(final TemplateVariableProcessor processor, final IProject project) {
		return new RdTemplateConfigurator(RProject.getRProject(project), processor);
	}
	
}
