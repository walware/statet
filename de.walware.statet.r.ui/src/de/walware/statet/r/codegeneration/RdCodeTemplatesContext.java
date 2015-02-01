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

package de.walware.statet.r.codegeneration;

import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.ui.templates.CodeGenerationTemplateContext;

import de.walware.statet.r.internal.ui.RUIPlugin;


public class RdCodeTemplatesContext extends CodeGenerationTemplateContext {
	
	
	RdCodeTemplatesContext(final String contextTypeName, final ISourceUnit su, final String lineDelim) {
		super(RUIPlugin.getDefault().getRdCodeGenerationTemplateContextRegistry().getContextType(contextTypeName),
				su, lineDelim);
	}
	
}
