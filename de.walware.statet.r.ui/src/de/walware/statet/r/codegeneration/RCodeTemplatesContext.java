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

package de.walware.statet.r.codegeneration;

import de.walware.ecommons.ltk.ISourceUnit;

import de.walware.statet.base.core.StatetProject;
import de.walware.statet.ext.templates.StatextCodeTemplatesContext;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.internal.ui.RUIPlugin;


public class RCodeTemplatesContext extends StatextCodeTemplatesContext {
	
	
	RCodeTemplatesContext(final String contextTypeName, final StatetProject project, final String lineDelim) {
		super(
				RUIPlugin.getDefault().getRCodeGenerationTemplateContextRegistry().getContextType(contextTypeName),
				project,
				lineDelim);
	}
	
	
	public void setSourceUnit(final ISourceUnit u) {
		setVariable(RCodeTemplatesContextType.FILENAME_VARIABLE, u.getElementName().getDisplayName());
	}
	
	public void setRElement(final IRElement element) {
		setVariable(RCodeTemplatesContextType.ELEMENT_NAME_VARIABLE, element.getElementName().getDisplayName());
	}
	
}
