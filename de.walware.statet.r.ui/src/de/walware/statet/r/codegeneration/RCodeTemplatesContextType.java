/*******************************************************************************
 * Copyright (c) 2005-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

import de.walware.statet.ext.templates.StatextCodeTemplatesContextType;

import de.walware.statet.r.internal.ui.RUIMessages;


public class RCodeTemplatesContextType extends StatextCodeTemplatesContextType {
	
	
/* context types **************************************************************/
	public static final String NEW_RSCRIPTFILE_CONTEXTTYPE = "r_NewRScriptFile_context"; //$NON-NLS-1$
	
	public static final String ROXYGEN_COMMONFUNCTION_CONTEXTTYPE = "roxygen_CommonFunctionDef_context"; //$NON-NLS-1$
	public static final String ROXYGEN_CLASS_CONTEXTTYPE = "roxygen_ClassDef_context"; //$NON-NLS-1$
	public static final String ROXYGEN_METHOD_CONTEXTTYPE = "roxygen_MethodDef_context"; //$NON-NLS-1$
	
/* templates ******************************************************************/
	public static final String NEW_RSCRIPTFILE = "r_NewRScriptFile"; //$NON-NLS-1$
	
	public static final String ROXYGEN_COMMONFUNCTION_TEMPLATE = "roxygen_CommonFunctionDef"; //$NON-NLS-1$
	public static final String ROXYGEN_S4CLASS_TEMPLATE = "roxygen_S4ClassDef"; //$NON-NLS-1$
	public static final String ROXYGEN_S4METHOD_TEMPLATE = "roxygen_S4MethodDef"; //$NON-NLS-1$
	
/* variables ******************************************************************/
	public static final String ELEMENT_NAME_VARIABLE = "element_name"; //$NON-NLS-1$;
	
	public static final String ROXYGEN_PARAM_TAGS_VARIABLE = "param_tags"; //$NON-NLS-1$
	public static final String ROXYGEN_SLOT_TAGS_VARIABLE = "slot_tags"; //$NON-NLS-1$
	
	
	public static void registerContextTypes(final ContextTypeRegistry registry) {
		registry.addContextType(new RCodeTemplatesContextType(NEW_RSCRIPTFILE_CONTEXTTYPE));
		
		registry.addContextType(new RCodeTemplatesContextType(ROXYGEN_COMMONFUNCTION_CONTEXTTYPE));
		registry.addContextType(new RCodeTemplatesContextType(ROXYGEN_CLASS_CONTEXTTYPE));
		registry.addContextType(new RCodeTemplatesContextType(ROXYGEN_METHOD_CONTEXTTYPE));
	}
	
	
	private static class RoxygenParamTagsVariableResolver extends TemplateVariableResolver {
		
		public RoxygenParamTagsVariableResolver() {
			super(ROXYGEN_PARAM_TAGS_VARIABLE, RUIMessages.Templates_Variable_RoxygenParamTags_description); 
		}
		
		@Override
		protected String resolve(final TemplateContext context) {
			return "@param \u2026"; //$NON-NLS-1$
		}
	}
	
	private static class RoxygenSlotTagsVariableResolver extends TemplateVariableResolver {
		
		public RoxygenSlotTagsVariableResolver() {
			super(ROXYGEN_SLOT_TAGS_VARIABLE, RUIMessages.Templates_Variable_RoxygenSlotTags_description); 
		}
		
		@Override
		protected String resolve(final TemplateContext context) {
			return "@slot \u2026"; //$NON-NLS-1$
		}
	}
	
	private static class RElementNameVariableResolver extends TemplateVariableResolver {
		
		protected RElementNameVariableResolver() {
			super(ELEMENT_NAME_VARIABLE, RUIMessages.Templates_Variable_ElementName_description); 
		}
		
	}
	
	
	RCodeTemplatesContextType(final String contextName) {
		super(contextName);
		
		addCommonVariables();
		if (NEW_RSCRIPTFILE_CONTEXTTYPE.equals(contextName)) {
			addSourceUnitGenerationVariables();
		}
		else if (ROXYGEN_CLASS_CONTEXTTYPE.equals(contextName)) {
			addSourceUnitGenerationVariables();
			addResolver(new RElementNameVariableResolver());
			addResolver(new RoxygenSlotTagsVariableResolver()); 
		}
		else if (ROXYGEN_COMMONFUNCTION_CONTEXTTYPE.equals(contextName)) {
			addSourceUnitGenerationVariables();
			addResolver(new RElementNameVariableResolver());
			addResolver(new RoxygenParamTagsVariableResolver()); 
		}
		else if (ROXYGEN_METHOD_CONTEXTTYPE.equals(contextName)) {
			addSourceUnitGenerationVariables();
			addResolver(new RElementNameVariableResolver());
			addResolver(new RoxygenParamTagsVariableResolver()); 
		}
	}
	
}
