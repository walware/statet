/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave;

import org.eclipse.jface.text.templates.ContextTypeRegistry;

import de.walware.statet.ext.templates.StatextCodeTemplatesContextType;


public class LtxRweaveTemplatesContextType extends StatextCodeTemplatesContextType {
	
	
	public static final String SWEAVEDOC_TEMPLATES_KEY = "de.walware.statet.r.sweave.templates.sweavedoc"; //$NON-NLS-1$
	public static final String LTXRWEAVE_TEMPLATES_KEY = "de.walware.statet.r.sweave.rweave_tex_templates"; //$NON-NLS-1$
	
/* context types **************************************************************/
	public static final String NEW_RWEAVETEX_CONTEXTTYPE = "ltx-rweave_NewSweaveDoc"; //$NON-NLS-1$
	public static final String RWEAVETEX_DEFAULT_CONTEXTTYPE = "ltx-rweave_DocDefault"; //$NON-NLS-1$
	
/* templates ******************************************************************/
	public static final String NEW_RCHUNK_ID = LTXRWEAVE_TEMPLATES_KEY+".NewRChunk"; //$NON-NLS-1$
		
	public static void registerContextTypes(final ContextTypeRegistry registry) {
		registry.addContextType(new LtxRweaveTemplatesContextType(RWEAVETEX_DEFAULT_CONTEXTTYPE));
	}
	
	
	public LtxRweaveTemplatesContextType(final String contextName) {
		super(contextName);
		
		addCommonVariables();
		if (NEW_RWEAVETEX_CONTEXTTYPE.equals(contextName)) {
			addSourceUnitGenerationVariables();
		}
		else if (RWEAVETEX_DEFAULT_CONTEXTTYPE.equals(contextName)) {
			addEditorVariables();
		}
	}
	
}
