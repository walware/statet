/*******************************************************************************
 * Copyright (c) 2007-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.templates.TemplateVariableProcessor;

import de.walware.docmlet.tex.core.TexCore;

import de.walware.statet.ext.templates.ICodeGenerationTemplatesCategory;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.sweave.TexRweaveCoreAccess;


/**
 * Integrates the R templates into the common StatET template
 * preference page.
 */
public class LtxRweaveTemplatesProvider implements ICodeGenerationTemplatesCategory {
	
	
	public LtxRweaveTemplatesProvider() {
	}
	
	
	@Override
	public String getProjectNatureId() {
		return RProject.NATURE_ID;
	}
	
	@Override
	public TemplateStore getTemplateStore() {
		return SweavePlugin.getDefault().getRweaveTexGenerationTemplateStore();
	}
	
	@Override
	public ContextTypeRegistry getContextTypeRegistry() {
		return SweavePlugin.getDefault().getRweaveTexGenerationTemplateContextRegistry();
	}
	
	@Override
	public SourceEditorViewerConfigurator getEditTemplateDialogConfiguator(final TemplateVariableProcessor processor, final IProject project) {
		final RProject rProject = RProject.getRProject(project);
		return new LtxRweaveTemplateConfigurator(new TexRweaveCoreAccess(
						TexCore.getWorkbenchAccess(),
						(rProject != null) ? rProject : RCore.getWorkbenchAccess() ),
				processor);
	}
	
}
