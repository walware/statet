/*******************************************************************************
 * Copyright (c) 2005-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import de.walware.ecommons.ltk.ui.templates.CodeGenerationTemplateContext;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.internal.ui.RUIPlugin;


public class RCodeTemplatesContext extends CodeGenerationTemplateContext {
	
	
	RCodeTemplatesContext(final String contextTypeName, final ISourceUnit su, final String lineDelim) {
		super(RUIPlugin.getDefault().getRCodeGenerationTemplateContextRegistry().getContextType(contextTypeName),
				su, lineDelim);
	}
	
	
	public void setRElement(final IRElement element) {
		setVariable(RCodeTemplatesContextType.ELEMENT_NAME_VARIABLE, element.getElementName().getDisplayName());
	}
	
}
