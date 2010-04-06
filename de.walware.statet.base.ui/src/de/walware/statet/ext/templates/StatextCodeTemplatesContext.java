/*******************************************************************************
 * Copyright (c) 2005-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.templates;

import org.eclipse.jface.text.templates.TemplateContextType;

import de.walware.ecommons.ltk.ui.templates.CodeGenerationTemplateContext;

import de.walware.statet.base.core.StatetProject;


public class StatextCodeTemplatesContext extends CodeGenerationTemplateContext {
	
	
	private StatetProject fProject;
	
	
	public StatextCodeTemplatesContext(
			final TemplateContextType contextType,
			final StatetProject project,
			final String lineDelim) {
		super(contextType, lineDelim);
		fProject = project;
	}
	
	
	public StatetProject getStatetProject() {
		return fProject;
	}
	
}
