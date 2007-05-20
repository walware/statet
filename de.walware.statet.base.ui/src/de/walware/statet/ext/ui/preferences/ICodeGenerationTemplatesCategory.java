/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import de.walware.eclipsecommons.templates.TemplateVariableProcessor;

import de.walware.statet.ext.ui.editors.SourceViewerConfigurator;


/**
 * @author Stephan Wahlbrink
 */
public interface ICodeGenerationTemplatesCategory {

	public String getProjectNatureId();
	
	public TemplateStore getTemplateStore();
	
	public ContextTypeRegistry getContextTypeRegistry();
	
	public SourceViewerConfigurator getEditTemplateDialogConfiguator(
			TemplateVariableProcessor processor, IProject project);
	
}
