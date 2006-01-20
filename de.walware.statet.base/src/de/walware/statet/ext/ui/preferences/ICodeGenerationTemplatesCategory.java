/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.preferences;

import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import de.walware.eclipsecommon.templates.TemplateVariableProcessor;
import de.walware.statet.base.core.StatetProject;


/**
 * @author Stephan Wahlbrink
 */
public interface ICodeGenerationTemplatesCategory {

	public TemplateStore getTemplateStore();
	
	public ContextTypeRegistry getContextTypeRegistry();
	
	public TemplateViewerConfigurationProvider getEditTemplateDialogConfiguation(
			TemplateVariableProcessor processor, StatetProject project);
	
}
