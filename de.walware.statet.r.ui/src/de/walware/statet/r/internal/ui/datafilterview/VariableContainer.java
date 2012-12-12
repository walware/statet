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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.internal.ui.dataeditor.RDataLabelProvider;
import de.walware.statet.r.internal.ui.dataeditor.RDataTableContentDescription;
import de.walware.statet.r.internal.ui.datafilter.FilterSet;
import de.walware.statet.r.internal.ui.datafilter.IFilterListener;
import de.walware.statet.r.internal.ui.datafilter.IntervalVariableFilter;
import de.walware.statet.r.internal.ui.datafilter.LevelVariableFilter;
import de.walware.statet.r.internal.ui.datafilter.TextVariableFilter;
import de.walware.statet.r.internal.ui.datafilter.VariableFilter;
import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class VariableContainer {
	
	
	private final IServiceLocator fServiceLocator;
	
	private final ScrolledPageComposite fVariableComposite;
	
	private final List<VariableComposite> fVariables;
	
	private final RDataLabelProvider fLabelProvider;
	
	private final FilterSet fFilterSet;
	
	private RDataTableContentDescription fDescription;
	
	
	public VariableContainer(final IServiceLocator serviceLocator, final ScrolledPageComposite variableComposite) {
		fServiceLocator = serviceLocator;
		fVariableComposite = variableComposite;
		fVariables = new ArrayList<VariableComposite>();
		
		fVariableComposite.getContent().setLayout(LayoutUtil.createContentGrid(1));
		
		fLabelProvider = new RDataLabelProvider();
		
		fFilterSet = new FilterSet(Realm.getDefault()) {
			@Override
			protected void filterRemoved(final VariableFilter oldFilter) {
				final int vIdx = getVariable(oldFilter);
				if (vIdx >= 0) {
					final VariableComposite composite = fVariables.remove(vIdx);
					composite.dispose();
				}
			}
			@Override
			protected void filterReplaced(final int idx, final VariableFilter oldFilter, final VariableFilter newFilter) {
				final int vIdx = getVariable(oldFilter);
				if (vIdx >= 0) {
					final VariableComposite composite = fVariables.get(vIdx);
					composite.setColumn(newFilter.getColumn());
					final FilterClient oldClient = composite.getClient();
					createFilterClient(composite, newFilter);
					oldClient.dispose();
					if (vIdx != idx) {
						fVariables.remove(vIdx);
						fVariables.add(idx, composite);
					}
					composite.layout(new Control[] { composite.getClient() });
				}
				else {
					filterAdded(idx, newFilter);
				}
			}
			@Override
			protected void filterAdded(final int idx, final VariableFilter newFilter) {
				final VariableComposite composite = createVariable(newFilter.getColumn());
				createFilterClient(composite, newFilter);
				fVariables.add(idx, composite);
			}
		};
		
		fFilterSet.addPostListener(new IFilterListener() {
			@Override
			public void filterChanged() {
				for (final VariableComposite variable : fVariables) {
					variable.updateImage(false);
				}
			}
		});
	}
	
	
	public void updateInput(final RDataTableContentDescription description) {
		fDescription = description;
		
		fVariableComposite.setRedraw(false);
		fVariableComposite.setDelayedReflow(true);
		try {
			fFilterSet.updateInput(description);
			
			if (fVariables.size() == 1) {
				fVariables.get(0).setExpanded(true);
			}
		}
		finally {
			fVariableComposite.setDelayedReflow(false);
			fVariableComposite.setRedraw(true);
			fVariableComposite.reflow(true);
		}
	}
	
	public RDataTableContentDescription getDescription() {
		return fDescription;
	}
	
	protected FilterClient createFilterClient(final VariableComposite composite, final VariableFilter filter) {
		if (filter == null) {
			return null;
		}
		FilterClient client;
		switch (filter.getType().getId()) {
		case 0:
			client = new LevelClient(composite, (LevelVariableFilter) filter);
			break;
		case 1:
			client = new IntervalClient(composite, (IntervalVariableFilter) filter);
			break;
		case 2:
			client = new TextClient(composite, (TextVariableFilter) filter);
			break;
		default:
			throw new IllegalStateException(filter.toString());
		}
		fVariableComposite.adaptChild(client);
		
		return client;
	}
	
	protected VariableComposite createVariable(final RDataTableColumn column) {
		final VariableComposite expandable = new VariableComposite(
				fVariableComposite.getContent(), this, column);
		expandable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//		expandable.setExpanded(true);
		fVariableComposite.adaptChild(expandable);
		
		return expandable;
	}
	
	
	public FilterSet getFilterSet() {
		return fFilterSet;
	}
	
	public IServiceLocator getServiceLocator() {
		return fServiceLocator;
	}
	
	public ScrolledPageComposite getVariableComposite() {
		return fVariableComposite;
	}
	
	public List<VariableComposite> getVariables() {
		return fVariables;
	}
	
	protected int getVariable(final VariableFilter filter) {
		for (int i = 0; i < fVariables.size(); i++) {
			final FilterClient client = fVariables.get(i).getClient();
			if (client != null && client.getFilter() == filter) {
				return i;
			}
		}
		return -1;
	}
	
	public RDataLabelProvider getLabelProvider() {
		return fLabelProvider;
	}
	
	public void dispose() {
		fLabelProvider.dispose();
	}
	
}
