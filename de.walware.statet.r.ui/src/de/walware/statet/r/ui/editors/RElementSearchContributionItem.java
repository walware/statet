/*=============================================================================#
 # Copyright (c) 2013-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.ltk.ui.LTKUI;

import de.walware.statet.r.internal.ui.search.Messages;


public class RElementSearchContributionItem extends CompoundContributionItem
		implements IWorkbenchContribution {
	
	
	public static class All extends RElementSearchContributionItem {
		
		public All() {
			super(LTKUI.SEARCH_ALL_ELEMENT_ACCESS_COMMAND_ID);
		}
		
		
	}
	
	public static class Write extends RElementSearchContributionItem {
		
		public Write() {
			super(LTKUI.SEARCH_WRITE_ELEMENT_ACCESS_COMMAND_ID);
		}
		
		
	}
	
	
	private final String commandId;
	
	private IServiceLocator serviceLocator;
	
	
	public RElementSearchContributionItem(final String commandId) {
		super(null);
		
		this.commandId= commandId;
	}
	
	
	@Override
	public void initialize(final IServiceLocator serviceLocator) {
		this.serviceLocator= serviceLocator;
	}
	
	@Override
	protected IContributionItem[] getContributionItems() {
		final List<IContributionItem> items= new ArrayList<>();
		
		createContributionItems(items);
		
		return items.toArray(new IContributionItem[items.size()]);
	}
	
	public void createContributionItems(final List<IContributionItem> items) {
		items.add(new CommandContributionItem(new CommandContributionItemParameter(
				this.serviceLocator, null, this.commandId, Collections.singletonMap(
						LTKUI.SEARCH_SCOPE_PARAMETER_NAME, LTKUI.SEARCH_SCOPE_WORKSPACE_PARAMETER_VALUE ), 
				null, null, null, 
				Messages.menus_Scope_Workspace_name, Messages.menus_Scope_Workspace_mnemonic, null,
				CommandContributionItem.STYLE_PUSH, null, false )));
		items.add(new CommandContributionItem(new CommandContributionItemParameter(
				this.serviceLocator, null, this.commandId, Collections.singletonMap(
						LTKUI.SEARCH_SCOPE_PARAMETER_NAME, LTKUI.SEARCH_SCOPE_PROJECT_PARAMETER_VALUE ), 
				null, null, null, 
				Messages.menus_Scope_Project_name, Messages.menus_Scope_Project_mnemonic, null,
				CommandContributionItem.STYLE_PUSH, null, false )));
		items.add(new CommandContributionItem(new CommandContributionItemParameter(
				this.serviceLocator, null, this.commandId, Collections.singletonMap(
						LTKUI.SEARCH_SCOPE_PARAMETER_NAME, LTKUI.SEARCH_SCOPE_FILE_PARAMETER_VALUE ), 
				null, null, null, 
				Messages.menus_Scope_File_name, Messages.menus_Scope_File_mnemonic, null,
				CommandContributionItem.STYLE_PUSH, null, false )));
	}
	
}
