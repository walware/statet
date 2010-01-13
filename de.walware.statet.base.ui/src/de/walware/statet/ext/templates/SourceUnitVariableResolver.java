/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.templates;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

import de.walware.ecommons.ltk.ISourceUnit;


public abstract class SourceUnitVariableResolver extends TemplateVariableResolver {
	
	
	public static final String FILENAME_TYPE = "file_name"; //$NON-NLS-1$
	
	
	public static class FileName extends SourceUnitVariableResolver {
		
		public FileName() {
			super(FILENAME_TYPE, TemplatesMessages.Templates_Variable_File_description);
		}
		
		@Override
		protected String resolve(final TemplateContext context) {
			final ISourceUnit su = ((IExtTemplateContext) context).getSourceUnit();
			if (su != null) {
				return su.getElementName().getDisplayName();
			}
			return null;
		}
		
	}
	
	
	protected SourceUnitVariableResolver(final String type, final String description) {
		super(type, description);
	}
	
	@Override
	protected boolean isUnambiguous(final TemplateContext context) {
		return (((IExtTemplateContext) context).getSourceUnit() != null);
	}
	
}
