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

import org.eclipse.jface.text.templates.ContextTypeRegistry;

import de.walware.statet.ext.templates.StatextCodeTemplatesContextType;
import de.walware.statet.ext.templates.TemplatesMessages;


public class RdCodeTemplatesContextType extends StatextCodeTemplatesContextType {
	
	
/* context types **************************************************************/
	public static final String NEW_RDOCFILE_CONTEXTTYPE = "rd_NewRDocFile_context"; //$NON-NLS-1$
	
/* templates ******************************************************************/
	public static final String NEW_RDOCFILE = "rd_NewRDocFile";	 //$NON-NLS-1$
	
	
	public static void registerContextTypes(final ContextTypeRegistry registry) {
		registry.addContextType(new RdCodeTemplatesContextType(NEW_RDOCFILE_CONTEXTTYPE));
	}
	
	
	RdCodeTemplatesContextType(final String contextName) {
		super(contextName);
		
		addCommonVariables();
		if (NEW_RDOCFILE_CONTEXTTYPE.equals(contextName)) {
			addRUnitVariables();
			addInitialSelectionResolver();
		}
		
	}
	
	private void addRUnitVariables() {
		addResolver(new CodeTemplatesVariableResolver(FILENAME_VARIABLE, TemplatesMessages.Templates_Variable_File_description));
	}
	
}
