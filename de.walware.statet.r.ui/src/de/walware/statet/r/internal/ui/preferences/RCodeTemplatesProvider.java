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
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.templates.TemplateVariableProcessor;

import de.walware.statet.ext.templates.ICodeGenerationTemplatesCategory;

import de.walware.statet.r.core.RProjects;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.sourceediting.RTemplateSourceViewerConfigurator;


/**
 * Integrates the R templates into the common StatET template
 * preference page. 
 */
public class RCodeTemplatesProvider implements ICodeGenerationTemplatesCategory {
	
	
	public RCodeTemplatesProvider() {
	}
	
	@Override
	public String getProjectNatureId() {
		return RProjects.R_NATURE_ID;
	}
	
	@Override
	public TemplateStore getTemplateStore() {
		return RUIPlugin.getDefault().getRCodeGenerationTemplateStore();
	}
	
	@Override
	public ContextTypeRegistry getContextTypeRegistry() {
		return RUIPlugin.getDefault().getRCodeGenerationTemplateContextRegistry();
	}
	
	@Override
	public SourceEditorViewerConfigurator getEditTemplateDialogConfiguator(final TemplateVariableProcessor processor, final IProject project) {
		return new RTemplateSourceViewerConfigurator(RProjects.getRProject(project), processor);
	}
	
}
