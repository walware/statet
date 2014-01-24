/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.console.ui.snippets;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.templates.Template;

import de.walware.statet.r.internal.console.ui.RConsoleUIPlugin;


public class SubmitRSnippetHandler extends AbstractHandler {
	
	
	private final RSnippets fSnippets;
	
	
	public SubmitRSnippetHandler() {
		fSnippets = RConsoleUIPlugin.getDefault().getRSnippets();
	}
	
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final String name = event.getParameter(RSnippets.SNIPPET_PAR);
		if (name == null) {
			return null;
		}
		final Template template = fSnippets.getTemplateStore().findTemplate(name);
		if (template != null) {
			fSnippets.run(template, event);
		}
		
		return null;
	}
	
	
}
