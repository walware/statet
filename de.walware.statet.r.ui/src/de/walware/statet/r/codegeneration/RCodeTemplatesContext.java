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

package de.walware.statet.r.codegeneration;

import de.walware.statet.base.StatetProject;
import de.walware.statet.ext.templates.StatextCodeTemplatesContext;
import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.ui.RUiPlugin;


public class RCodeTemplatesContext extends StatextCodeTemplatesContext {
	
	
	public RCodeTemplatesContext(String contextTypeName, StatetProject project, String lineDelim) {
		
		super(
				RUiPlugin.getDefault().getRCodeGenerationTemplateContextRegistry().getContextType(contextTypeName), 
				project, 
				lineDelim);
	}
	

	public void setCodeUnitVariables(RResourceUnit cu) {
		setVariable(RCodeTemplatesContextType.FILENAME, cu.getElementName());
//		setVariable(RCodeTemplatesContextType.PACKAGENAME, cu.getParent().getElementName());
//		setVariable(RCodeTemplatesContextType.PROJECTNAME, cu.getRProject().getElementName());
	}

}
