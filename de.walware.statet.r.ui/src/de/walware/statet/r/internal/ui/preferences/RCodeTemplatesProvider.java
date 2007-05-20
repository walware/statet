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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import de.walware.eclipsecommons.templates.TemplateVariableProcessor;

import de.walware.statet.base.ui.StatetUIServices;
import de.walware.statet.ext.ui.editors.SourceViewerConfigurator;
import de.walware.statet.ext.ui.preferences.ICodeGenerationTemplatesCategory;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.RSourceViewerConfigurator;


/**
 * Integrates the R templates into the common StatET template
 * preference page. 
 */
public class RCodeTemplatesProvider implements ICodeGenerationTemplatesCategory {

	
	public static class RTemplateConfigurator extends RSourceViewerConfigurator {

		public RTemplateConfigurator(
				IRCoreAccess rCoreAccess,
				TemplateVariableProcessor processor) {
			super(rCoreAccess, RUIPlugin.getDefault().getEditorPreferenceStore());
			setConfiguration(new RTemplateSourceViewerConfiguration(
					processor, 
					this, 
					getPreferenceStore(),
					StatetUIServices.getSharedColorManager()));
		}
	}
	
	
	public RCodeTemplatesProvider() {
	}
	
	public String getProjectNatureId() {
		return RProject.NATURE_ID;
	}
	
	public TemplateStore getTemplateStore() {
		return RUIPlugin.getDefault().getRCodeGenerationTemplateStore();
	}

	public ContextTypeRegistry getContextTypeRegistry() {
		return RUIPlugin.getDefault().getRCodeGenerationTemplateContextRegistry();
	}

	public SourceViewerConfigurator getEditTemplateDialogConfiguator(TemplateVariableProcessor processor, IProject project) {
		return new RTemplateConfigurator(RProject.getRProject(project), processor);
	}

}
