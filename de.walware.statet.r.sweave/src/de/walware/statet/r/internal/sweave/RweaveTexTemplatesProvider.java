/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.templates.TemplateVariableProcessor;
import de.walware.ecommons.ui.SharedUIResources;

import de.walware.statet.ext.templates.ICodeGenerationTemplatesCategory;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.internal.sweave.editors.RweaveTexDocumentSetupParticipant;
import de.walware.statet.r.internal.sweave.editors.RweaveTexViewerConfigurator;


/**
 * Integrates the R templates into the common StatET template
 * preference page.
 */
public class RweaveTexTemplatesProvider implements ICodeGenerationTemplatesCategory {
	
	
	public static class RweaveTexTemplateConfigurator extends RweaveTexViewerConfigurator {
		
		public RweaveTexTemplateConfigurator(
				final IRCoreAccess rCoreAccess,
				final TemplateVariableProcessor processor) {
			super(rCoreAccess);
			setConfiguration(new RweaveTexTemplatesSourceViewerConfiguration(
					processor,
					this,
					SweavePlugin.getDefault().getEditorRTexPreferenceStore(),
					SharedUIResources.getColors() ));
		}
		
		@Override
		public IDocumentSetupParticipant getDocumentSetupParticipant() {
			return new RweaveTexDocumentSetupParticipant(true);
		}
	}
	
	
	public RweaveTexTemplatesProvider() {
	}
	
	public String getProjectNatureId() {
		return RProject.NATURE_ID;
	}
	
	public TemplateStore getTemplateStore() {
		return SweavePlugin.getDefault().getRweaveTexGenerationTemplateStore();
	}
	
	public ContextTypeRegistry getContextTypeRegistry() {
		return SweavePlugin.getDefault().getRweaveTexGenerationTemplateContextRegistry();
	}
	
	public SourceEditorViewerConfigurator getEditTemplateDialogConfiguator(final TemplateVariableProcessor processor, final IProject project) {
		return new RweaveTexTemplateConfigurator(RProject.getRProject(project), processor);
	}
	
}
