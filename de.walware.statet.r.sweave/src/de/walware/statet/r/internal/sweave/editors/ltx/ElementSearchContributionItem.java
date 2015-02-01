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

package de.walware.statet.r.internal.sweave.editors.ltx;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.internal.sweave.editors.LtxRweaveEditor;
import de.walware.statet.r.sweave.text.LtxRweaveSwitch;
import de.walware.statet.r.ui.editors.RElementSearchContributionItem;


public class ElementSearchContributionItem extends CompoundContributionItem
		implements IWorkbenchContribution, IExecutableExtension {
	
	
	private String commandId;
	
	private IServiceLocator serviceLocator;
	
	private RElementSearchContributionItem rItem;
	
	
	public ElementSearchContributionItem() {
		super(null);
	}
	
	
	@Override
	public void setInitializationData(final IConfigurationElement config,
			final String propertyName, final Object data) throws CoreException {
		if (this.commandId != null) {
			return;
		}
		final String s= config.getAttribute("id"); //$NON-NLS-1$
		if (s != null) {
			this.commandId= s.intern();
		}
	}
	
	@Override
	public void initialize(final IServiceLocator serviceLocator) {
		this.serviceLocator= serviceLocator;
		
		if (this.rItem != null) {
			this.rItem.initialize(serviceLocator);
		}
	}
	
	private RElementSearchContributionItem getRItem() {
		if (this.rItem == null) {
			this.rItem= new RElementSearchContributionItem(this.commandId);
			this.rItem.initialize(this.serviceLocator);
		}
		return this.rItem;
	}
	
	@Override
	protected IContributionItem[] getContributionItems() {
		final List<IContributionItem> items= new ArrayList<>();
		
		final IWorkbenchPart part= UIAccess.getActiveWorkbenchPart(true);
		if (part instanceof LtxRweaveEditor) {
			final ISourceEditor editor= (LtxRweaveEditor) part;
			final SourceViewer viewer= editor.getViewer();
			switch(LtxRweaveSwitch.get(viewer.getDocument(), viewer.getSelectedRange().x)) {
			case R:
				getRItem().createContributionItems(items);
				break;
			default:
				break;
			}
		}
		
		return items.toArray(new IContributionItem[items.size()]);
	}
	
}
