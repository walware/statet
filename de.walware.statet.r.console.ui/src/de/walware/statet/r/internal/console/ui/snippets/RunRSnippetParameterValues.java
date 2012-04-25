/*******************************************************************************
 * Copyright (c) 2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.console.ui.snippets;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;
import org.eclipse.jface.text.templates.Template;

import de.walware.statet.r.internal.console.ui.RConsoleUIPlugin;


public class RunRSnippetParameterValues implements IParameterValues {
	
	
	private final RSnippets fSnippets;
	
	
	public RunRSnippetParameterValues() {
		fSnippets = RConsoleUIPlugin.getDefault().getRSnippets();
	}
	
	
	@Override
	public Map getParameterValues() {
		final Template[] templates = fSnippets.getTemplateStore().getTemplates();
		
		final Map<String, String> parameters = new HashMap<String, String>();
		for (final Template template : templates) {
			parameters.put(template.getDescription(), template.getName());
		}
		return parameters;
	}
	
}
