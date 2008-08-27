/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
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


public class RCodeTemplatesContextType extends StatextCodeTemplatesContextType {
	
	
/* context types **************************************************************/
	public static final String NEW_RSCRIPTFILE_CONTEXTTYPE = "r_NewRScriptFile_context"; //$NON-NLS-1$
	
/* templates ******************************************************************/
	public static final String NEW_RSCRIPTFILE = "r_NewRScriptFile"; //$NON-NLS-1$
	
	
	public static void registerContextTypes(final ContextTypeRegistry registry) {
		registry.addContextType(new RCodeTemplatesContextType(NEW_RSCRIPTFILE_CONTEXTTYPE));
	}
	
	
	public RCodeTemplatesContextType(final String contextName) {
		super(contextName);
		
		addCommonVariables();
		if (NEW_RSCRIPTFILE_CONTEXTTYPE.equals(contextName)) {
			addRUnitVariables();
			addInitialSelectionResolver();
		}
	}
	
	private void addRUnitVariables() {
		addResolver(new CodeTemplatesVariableResolver(FILENAME_VARIABLE, TemplatesMessages.Templates_Variable_File_description));
	}
	
}
