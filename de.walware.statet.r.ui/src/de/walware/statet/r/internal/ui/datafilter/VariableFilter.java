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

package de.walware.statet.r.internal.ui.datafilter;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.data.defaultImpl.RCharacterDataImpl;
import de.walware.rj.eclient.IRToolService;

import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public abstract class VariableFilter {
	
	
	protected final static RCharacterDataImpl NO_VALUES = new RCharacterDataImpl();
	
	private class BindingListener implements IValueChangeListener, ISetChangeListener {
		
		@Override
		public void handleSetChange(final SetChangeEvent event) {
			updateFilter(1);
		}
		
		@Override
		public void handleValueChange(final ValueChangeEvent event) {
			updateFilter(1);
		}
		
	}
	
	
	private final FilterSet fFilterSet;
	
	private final RDataTableColumn fColumn;
	
	private IFilterListener fListener;
	
	private String fErrorMessage;
	
	volatile boolean fUpdateScheduled;
	
	private String fFilterRExpression;
	
	private final BindingListener fValueListener = new BindingListener();
	
	
	protected VariableFilter(final FilterSet set, final RDataTableColumn column) {
		fFilterSet = set;
		fColumn = column;
	}
	
	
	public abstract FilterType getType();
	
	public FilterSet getSet() {
		return fFilterSet;
	}
	
	public RDataTableColumn getColumn() {
		return fColumn;
	}
	
	public void load(final VariableFilter filter) {
	}
	
	public void reset() {
	}
	
	protected void scheduleUpdate() {
		fUpdateScheduled = true;
		fFilterSet.scheduleUpdate(false);
	}
	
	protected void registerObservable(final IObservable observable) {
		if (observable instanceof IObservableValue) {
			((IObservableValue) observable).addValueChangeListener(fValueListener);
		}
		if (observable instanceof IObservableSet) {
			((IObservableSet) observable).addSetChangeListener(fValueListener);
		}
	}
	
	protected void updateFilter(final int flag) {
		String rExpression = createFilter(getColumn().getRExpression());
		if (rExpression != null && rExpression.isEmpty()) {
			rExpression = null;
		}
		if ((fFilterRExpression != null) ? fFilterRExpression.equals(rExpression) : null == rExpression) {
			return;
		}
		fFilterRExpression = rExpression;
		fFilterSet.updateFilter((flag & 1) == 1);
	}
	
	protected abstract String createFilter(String varExpression);
	
	protected void runInRealm(final Runnable runnable) {
		fFilterSet.runInRealm(runnable);
	}
	
	
	public void setListener(final IFilterListener listener) {
		fListener = listener;
	}
	
	protected abstract void update(IRToolService r,
			IProgressMonitor monitor) throws CoreException, UnexpectedRDataException;
	
	protected void notifyListeners() {
		if (fListener != null) {
			fListener.filterChanged();
		}
	}
	
	protected void setError(final String message) {
		fErrorMessage = message;
	}
	
	public String getError() {
		return fErrorMessage;
	}
	
	public String getFilterRExpression() {
		return fFilterRExpression;
	}
	
	public String getFilterRExpression(final String mainExpression, final int nameFlags) {
		final String varExpression = mainExpression
				+ getColumn().getElementName().getDisplayName(nameFlags).substring(1);
		return createFilter(varExpression);
	}
	
	
//	@Override
//	public int hashCode() {
//		return fColumn.getName().hashCode();
//	}
//	
//	@Override
//	public boolean equals(Object obj) {
//		return (obj instanceof VariableFilter
//				&& fColumn.getName().equals(((VariableFilter) obj).fColumn.getName()) );
//	}
	
}
