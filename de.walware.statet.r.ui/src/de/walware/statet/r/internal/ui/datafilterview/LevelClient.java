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

package de.walware.statet.r.internal.ui.datafilterview;

import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.databinding.UpdateSetStrategy;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.ui.actions.ControlServicesUtil;
import de.walware.ecommons.ui.actions.HandlerCollection;
import de.walware.ecommons.ui.actions.HandlerContributionItem;
import de.walware.ecommons.ui.util.AutoCheckController;

import de.walware.rj.data.RStore;

import de.walware.statet.r.internal.ui.datafilter.LevelVariableFilter;


public class LevelClient extends FilterClient {
	
	
	private CheckboxTableViewer fValueListViewer;
	
	private final WritableSet fSelectedValues;
	
	private RStore fAvailableValues;
	
	private final HandlerCollection fValueListHandlers = new HandlerCollection();
	private MenuManager fValueListMenuManager;
	
	private final LevelVariableFilter fFilter;
	
	
	public LevelClient(final VariableComposite parent, final LevelVariableFilter filter) {
		super(parent);
		
		fFilter = filter;
		filter.setListener(this);
		
		fAvailableValues = filter.getAvailableValues();
		fSelectedValues = filter.getSelectedValues();
		init(1);
	}
	
	
	@Override
	public LevelVariableFilter getFilter() {
		return fFilter;
	}
	
	@Override
	protected void addWidgets() {
		fValueListViewer = CheckboxTableViewer.newCheckList(this, SWT.MULTI | SWT.FLAT | SWT.FULL_SELECTION);
		fValueListViewer.setContentProvider(new RStoreContentProvider());
		fValueListViewer.setLabelProvider(new ColumnLabelProvider(fFilter.getColumn()));
	}
	
	@Override
	protected void initActions(final IServiceLocator serviceLocator) {
		final ControlServicesUtil servicesUtil = new ControlServicesUtil(serviceLocator,
				getClass().getName()+"/ValueList#"+hashCode(), this ); //$NON-NLS-1$
		servicesUtil.addControl(fValueListViewer.getTable());
		
		final AutoCheckController autoCheckController = new AutoCheckController(fValueListViewer, fFilter.getSelectedValues());
		{	final IHandler2 handler = autoCheckController.createSelectAllHandler();
			fValueListHandlers.add(SELECT_ALL_COMMAND_ID, handler);
			servicesUtil.activateHandler(SELECT_ALL_COMMAND_ID, handler);
		}
		
		fValueListMenuManager = new MenuManager();
		fValueListMenuManager.add(new HandlerContributionItem(new CommandContributionItemParameter(serviceLocator,
						null, SELECT_ALL_COMMAND_ID, HandlerContributionItem.STYLE_PUSH),
				fValueListHandlers.get(SELECT_ALL_COMMAND_ID) ));
		fValueListViewer.getTable().setMenu(fValueListMenuManager.createContextMenu(fValueListViewer.getControl()));
		
//		fValueListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
//			@Override
//			public void selectionChanged(final SelectionChangedEvent event) {
//				updateActions();
//			}
//		});
//		updateActions();
	}
	
	@Override
	protected void addBindings(final DataBindingSupport db) {
		db.getContext().bindSet(
				ViewersObservables.observeCheckedElements(fValueListViewer, Object.class),
				fSelectedValues,
				new UpdateSetStrategy().setConverter(UI2RStoreConverter.INSTANCE),
				new UpdateSetStrategy().setConverter(RStore2UIConverter.INSTANCE) );
	}
	
	@Override
	protected void updateInput() {
		fAvailableValues = fFilter.getAvailableValues();
		fValueListViewer.setInput(fAvailableValues);
		
		if (updateLayout()) {
			getParent().layout(new Control[] { this });
		}
	}
	
	@Override
	protected boolean updateLayout() {
		return updateLayout(fValueListViewer, fAvailableValues.getLength());
	}
	
	
	@Override
	protected void onDispose() {
		if (fValueListMenuManager != null) {
			fValueListMenuManager.dispose();
			fValueListMenuManager = null;
		}
		super.onDispose();
	}
	
}
