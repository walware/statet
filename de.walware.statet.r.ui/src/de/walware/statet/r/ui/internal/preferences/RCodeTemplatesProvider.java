/*******************************************************************************
 * Copyright (c) 2005-2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.internal.preferences;

import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import de.walware.eclipsecommons.templates.TemplateVariableProcessor;

import de.walware.statet.base.core.StatetProject;
import de.walware.statet.ext.ui.editors.StatextSourceViewerConfiguration;
import de.walware.statet.ext.ui.preferences.ICodeGenerationTemplatesCategory;
import de.walware.statet.ext.ui.preferences.TemplateViewerConfigurationProvider;
import de.walware.statet.r.ui.internal.RUIPlugin;
import de.walware.statet.r.ui.editors.RDocumentSetupParticipant;


/**
 * Integrates the R templates into the common StatET template
 * preference page. 
 */
public class RCodeTemplatesProvider implements ICodeGenerationTemplatesCategory {

	
	public RCodeTemplatesProvider() {
	}
	
	public TemplateStore getTemplateStore() {
		return RUIPlugin.getDefault().getRCodeGenerationTemplateStore();
	}

	public ContextTypeRegistry getContextTypeRegistry() {
		return RUIPlugin.getDefault().getRCodeGenerationTemplateContextRegistry();
	}

	public TemplateViewerConfigurationProvider getEditTemplateDialogConfiguation(
			final TemplateVariableProcessor processor, StatetProject project) {

		StatextSourceViewerConfiguration configuration = 
			new RTemplateSourceViewerConfiguration(processor, project);
		
		return new TemplateViewerConfigurationProvider(
				configuration,
				new RDocumentSetupParticipant(),
				RUIPlugin.getDefault().getPreferenceStore() );
	}

}
