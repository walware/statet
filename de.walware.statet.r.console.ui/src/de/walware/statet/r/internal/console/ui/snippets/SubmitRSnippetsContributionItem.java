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

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.ui.util.MessageUtil;

import de.walware.statet.r.internal.console.ui.RConsoleUIPlugin;


public class SubmitRSnippetsContributionItem extends CompoundContributionItem
		implements IWorkbenchContribution {
	
	
	private IServiceLocator serviceLocator;
	
	private final RSnippets snippets;
	
	
	public SubmitRSnippetsContributionItem() {
		this.snippets = RConsoleUIPlugin.getDefault().getRSnippets();
	}
	
	
	@Override
	public void initialize(final IServiceLocator serviceLocator) {
		this.serviceLocator = serviceLocator;
	}
	
	@Override
	protected IContributionItem[] getContributionItems() {
		final List<Template> filtered = this.snippets.validate(
				this.snippets.getTemplateStore().getTemplates() );
		
		IServiceLocator serviceLocator = this.serviceLocator;
		if (serviceLocator == null) {
			serviceLocator = PlatformUI.getWorkbench();
		}
		final IContributionItem[] items = new IContributionItem[filtered.size()];
		for (int i = 0; i < items.length; i++) {
			final Template template = filtered.get(i);
			String label = MessageUtil.escapeForMenu(template.getDescription());
			String mnemonic = null;
			if (i < 9) {
				mnemonic = Integer.toString(i + 1);
				label = mnemonic + ' ' + label;
			}
			items[i] = new CommandContributionItem(new CommandContributionItemParameter(
					serviceLocator, null, RSnippets.SUBMIT_SNIPPET_COMMAND_ID,
					Collections.singletonMap(RSnippets.SNIPPET_PAR, template.getName()),
					null, null, null,
					label, mnemonic, null,
					CommandContributionItem.STYLE_PUSH, null, false ));
		}
		return items;
	}
	
}
