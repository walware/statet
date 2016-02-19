/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.console.ui.snippets;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.variables.IStringVariable;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.IValueVariableListener;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

import de.walware.statet.r.internal.console.ui.RConsoleUIPlugin;


public class RSnippetTemplatesContextType extends TemplateContextType
		implements IValueVariableListener {
	
	
	private static class VariableResolver extends TemplateVariableResolver {
		
		public VariableResolver(final IStringVariable variable) {
			super(variable.getName(), variable.getDescription());
		}
		
	}
	
	
	public static final String TYPE_ID = "de.walware.statet.r.templates.RConsoleSnippetContextType"; //$NON-NLS-1$
	
	public static final String TEMPLATES_KEY = "de.walware.statet.r.templates.rsnippets"; //$NON-NLS-1$
	
	
	private boolean fUpdate;
	
	
	public RSnippetTemplatesContextType() {
		super(TYPE_ID);
		
		fUpdate = true;
		VariablesPlugin.getDefault().getStringVariableManager().addValueVariableListener(this);
	}
	
	
	@Override
	public void variablesAdded(final IValueVariable[] variables) {
		fUpdate = true;
	}
	
	@Override
	public void variablesChanged(final IValueVariable[] variables) {
	}
	
	@Override
	public void variablesRemoved(final IValueVariable[] variables) {
		fUpdate = true;
	}
	
	@Override
	public Iterator resolvers() {
		if (fUpdate) {
			updateResolvers();
		}
		return super.resolvers();
	}
	
	
	private void updateResolvers() {
		fUpdate = false;
		
		removeAllResolvers();
		
		final RSnippets snippets = RConsoleUIPlugin.getDefault().getRSnippets();
		final List<IStringVariable> variables = snippets.getVariables();
		for (final IStringVariable variable : variables) {
			addResolver(new VariableResolver(variable));
		}
	}
	
	@Override
	public void validate(final String pattern) throws TemplateException {
	}
	
}
