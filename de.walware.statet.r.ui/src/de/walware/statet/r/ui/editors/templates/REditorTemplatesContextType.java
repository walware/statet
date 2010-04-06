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

package de.walware.statet.r.ui.editors.templates;

import org.eclipse.jface.text.templates.ContextTypeRegistry;

import de.walware.ecommons.ltk.ui.templates.SourceUnitVariableResolver;

import de.walware.statet.ext.templates.StatextCodeTemplatesContextType;

import de.walware.statet.r.internal.ui.RUIMessages;


/**
 * Definition of context types in R code editors. The "editor contexts"
 * are usually used in the template content assistant
 * (in contrast to {@link de.walware.statet.r.codegeneration.RCodeTemplatesContextType}).
 */
public class REditorTemplatesContextType extends StatextCodeTemplatesContextType {
	
	
//	/**
//	 * Resolver for Array-Variables.
//	 */
//	protected static class VectorVar extends TemplateVariableResolver {
//
//		public VectorVar() {
//			super("vector", Messages.TemplateVariable_Vector_description);  //$NON-NLS-1$
//		}
//		
//		protected String resolve(TemplateContext context) {
//			return "vector";
//		}
//	}
//
//	/**
//	 * Resolver for a Variables.
//	 */
//	protected static class XVar extends TemplateVariableResolver {
//
//		public XVar() {
//			super("variable", Messages.TemplateVariable_Variable_description);  
//		}
//		
//		protected String resolve(TemplateContext context) {
//			return "x";
//		}
//	}
//
//	/**
//	 * Resolver for Idx-Variables.
//	 */
//	protected static class IndexVar extends TemplateVariableResolver {
//
//		public IndexVar() {
//			super("index", Messages.TemplateVariable_Index_description);  
//		}
//		
//		protected String resolve(TemplateContext context) {
//			return "i";
//		}
//	}
//
//	/**
//	 * Resolver for function parameter.
//	 */
//	protected static class Parameter extends TemplateVariableResolver {
//
//		public Parameter() {
//			super("parameter", Messages.TemplateVariable_Parameter_description);  
//		}
//		
//		protected String resolve(TemplateContext context) {
//			return "par";
//		}
//	}
	
	
/* context types **************************************************************/
	
	/**
	 * Common context for R source code.
	 */
	public static final String RCODE_CONTEXTTYPE = "r-code"; //$NON-NLS-1$
	
	/**
	 * Context for Roxygen documentation comments.
	 */
	public static final String ROXYGEN_CONTEXTTYPE = "roxygen"; //$NON-NLS-1$
	
	
	public static void registerContextTypes(final ContextTypeRegistry registry) {
		registry.addContextType(new REditorTemplatesContextType(RCODE_CONTEXTTYPE, RUIMessages.EditorTemplates_RCodeContext_label));
		registry.addContextType(new REditorTemplatesContextType(ROXYGEN_CONTEXTTYPE, RUIMessages.EditorTemplates_RoxygenContext_label));
	}
	
	
	REditorTemplatesContextType(final String id, final String name) {
		super(id, name);
		
		addCommonVariables();
		addEditorVariables();
		
		addResolver(new SourceUnitVariableResolver.FileName());
		
//		if (RCODE_CONTEXTTYPE.equals(id)) {
//			addResolver(new VectorVar());
//			addResolver(new XVar());
//			addResolver(new IndexVar());
//			addResolver(new Parameter());
//		}
	}
	
}
