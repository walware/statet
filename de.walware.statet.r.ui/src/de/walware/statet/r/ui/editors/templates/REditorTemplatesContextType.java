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

package de.walware.statet.r.ui.editors.templates;

import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;

import de.walware.statet.ext.templates.StatextCodeTemplatesContextType;


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
//			super("variable", Messages.TemplateVariable_Variable_description);  //$NON-NLS-1$
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
//			super("index", Messages.TemplateVariable_Index_description);  //$NON-NLS-1$
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
//			super("parameter", Messages.TemplateVariable_Parameter_description);  //$NON-NLS-1$
//		}
//		
//		protected String resolve(TemplateContext context) {
//			return "par";
//		}
//	}
	
	
/* context types **************************************************************/
	public static final String RSCIRPT_CONTEXTTYPE = "rscript"; //$NON-NLS-1$

	
	public REditorTemplatesContextType(String contextName) {
		
		super(contextName);

		addResolver(new GlobalTemplateVariables.Cursor());
		addResolver(new GlobalTemplateVariables.WordSelection());
		addResolver(new GlobalTemplateVariables.LineSelection());
		
		if (RSCIRPT_CONTEXTTYPE.equals(contextName)) {
//			addResolver(new VectorVar());
//			addResolver(new XVar());
//			addResolver(new IndexVar());
//			addResolver(new Parameter());
		}
		
	}
	
	public static void registerContextTypes(ContextTypeRegistry registry) {
		
		registry.addContextType(new REditorTemplatesContextType(RSCIRPT_CONTEXTTYPE));
	}

}
