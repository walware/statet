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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.FastList;
import de.walware.ecommons.collections.ConstArrayList;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;

import de.walware.rj.data.RFactorStore;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.IRToolService;

import de.walware.statet.r.internal.ui.dataeditor.RDataTableContentDescription;
import de.walware.statet.r.ui.dataeditor.IRDataTableVariable;
import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class FilterSet {
	
	
	private final static int POST_DELAY = 400;
	
	private final static int STD_DELAY = 1;
	private final static int NO_DELAY = 2;
	
	private RDataTableContentDescription fInput;
	
	private boolean fInputUpdate;
	
	private final Object fUpdateLock = new Object();
	private boolean fUpdateScheduled;
	private final IToolRunnable fUpdateRunnable = new ISystemRunnable() {
		
		@Override
		public String getTypeId() {
			return "r/datafilter/load"; //$NON-NLS-1$
		}
		
		@Override
		public String getLabel() {
			return NLS.bind(Messages.UpdateJob_label, fInput.getLabel());
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return true; // TODO
		}
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			switch (event) {
			case MOVING_FROM:
				return false;
			case REMOVING_FROM:
			case BEING_ABANDONED:
				synchronized (fUpdateLock) {
					fUpdateScheduled = false;
					fUpdateLock.notifyAll();
				}
				break;
			default:
				break;
			}
			return true;
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			runUpdate((IRToolService) service, monitor);
		}
		
	};
	private boolean fUpdateAll;
	
	private final List<VariableFilter> fFilters = new ArrayList<VariableFilter>();
	private final List<String> fFilterNames = new ArrayList<String>();
	
	private final FastList<IFilterListener> fListeners = new FastList<IFilterListener>(IFilterListener.class);
	private final FastList<IFilterListener> fPostListeners = new FastList<IFilterListener>(IFilterListener.class);
	private volatile int fListenerScheduled;
	private final Runnable fListenerRunnable = new Runnable() {
		@Override
		public void run() {
			final int schedule = fListenerScheduled;
			fListenerScheduled = 0;
			
			final IFilterListener[] listeners = fListeners.toArray();
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].filterChanged();
			}
			
			if (schedule != NO_DELAY) {
				fPostListenerTime = System.nanoTime() + POST_DELAY;
				if (fPostListenerScheduled) {
					return;
				}
				fPostListenerScheduled = true;
				fRealm.timerExec(POST_DELAY, fPostListenerRunnable);
			}
			else {
				fPostListenerTime = System.nanoTime();
				fPostListenerScheduled = true;
				fPostListenerRunnable.run();
			}
		}
	};
	private boolean fPostListenerScheduled;
	private long fPostListenerTime;
	private final Runnable fPostListenerRunnable = new Runnable() {
		@Override
		public void run() {
			if (fListenerScheduled > 0) {
				fPostListenerScheduled = false;
				return;
			}
			final long time = fPostListenerTime - System.nanoTime();
			if (time > 20) {
				fRealm.timerExec((int) time, this);
				return;
			}
			
			fPostListenerScheduled = false;
			final IFilterListener[] listeners = fPostListeners.toArray();
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].filterChanged();
			}
		}
	};
	
	private final Realm fRealm;
	
	private boolean fEnabled;
	
	private String fFilterRExpression;
	
	
	public FilterSet(final Realm realm) {
		fRealm = realm;
		fEnabled = true;
	}
	
	
	public Realm getRealm() {
		return fRealm;
	}
	
	protected void runInRealm(final Runnable runnable) {
		if (fRealm.isCurrent()) {
			runnable.run();
		}
		else {
			fRealm.asyncExec(runnable);
		}
	}	
	
	public void updateInput(final RDataTableContentDescription input) {
		synchronized (fUpdateLock) {
			fInput = input;
			fInputUpdate = true;
		}
		synchronized (this) {
			int idx = 0;
			if (input != null) {
				final List<RDataTableColumn> columns = input.getDataColumns();
				for (; idx < columns.size(); idx++) {
					final RDataTableColumn column = columns.get(idx);
					if (column.getRExpression() == null || column.getName() == null) {
						continue;
					}
					final VariableFilter filter = createFilter(getDefaultFilter(column), column);
					if (filter == null) {
						continue;
					}
					final int oldIdx = fFilterNames.indexOf(column.getName());
					if (oldIdx >= 0) {
						final VariableFilter oldFilter = fFilters.get(oldIdx);
						filter.load(oldFilter);
						if (idx != oldIdx) {
							fFilterNames.remove(oldIdx);
							fFilters.remove(oldIdx);
							fFilterNames.add(idx, column.getName());
							fFilters.add(idx, filter);
						}
						else {
							fFilters.set(idx, filter);
						}
						filterReplaced(idx, oldFilter, filter);
						continue;
					}
					fFilterNames.add(idx, column.getName());
					fFilters.add(idx, filter);
					filterAdded(idx, filter);
				}
			}
			while (fFilters.size() > idx) {
				fFilterNames.remove(idx);
				final VariableFilter oldFilter = fFilters.remove(idx);
				filterRemoved(oldFilter);
			}
		}
		synchronized (fUpdateLock) {
			fInputUpdate = false;
			scheduleUpdate(true);
		}
	}
	
	protected void filterRemoved(final VariableFilter oldFilter) {
	}
	
	protected void filterReplaced(final int idx, final VariableFilter oldFilter, final VariableFilter newFilter) {
	}
	
	protected void filterAdded(final int idx, final VariableFilter newFilter) {
	}
	
	public FilterType getDefaultFilter(final RDataTableColumn column) {
		switch (column.getVarType()) {
		case IRDataTableVariable.LOGI:
		case IRDataTableVariable.FACTOR:
		case IRDataTableVariable.RAW:
			return FilterType.LEVEL;
		case IRDataTableVariable.INT:
		case IRDataTableVariable.NUM:
		case IRDataTableVariable.DATE:
		case IRDataTableVariable.DATETIME:
			return FilterType.INTERVAL;
		case IRDataTableVariable.CHAR:
			return FilterType.TEXT;
		default:
			return null;
		}
	}
	
	public List<FilterType> getAvailableFilters(final RDataTableColumn column) {
		switch (column.getVarType()) {
		case IRDataTableVariable.LOGI:
		case IRDataTableVariable.RAW:
			return new ConstArrayList<FilterType>(FilterType.LEVEL);
		case IRDataTableVariable.FACTOR:
			if (((RFactorStore) column.getDataStore()).isOrdered()) {
				return new ConstArrayList<FilterType>(FilterType.LEVEL, FilterType.INTERVAL);
			}
			return new ConstArrayList<FilterType>(FilterType.LEVEL);
		case IRDataTableVariable.INT:
		case IRDataTableVariable.NUM:
		case IRDataTableVariable.DATE:
		case IRDataTableVariable.DATETIME:
			return new ConstArrayList<FilterType>(FilterType.INTERVAL, FilterType.LEVEL);
		case IRDataTableVariable.CHAR:
			return new ConstArrayList<FilterType>(FilterType.TEXT, FilterType.LEVEL);
		default:
			return null;
		}
	}
	
	public VariableFilter replace(final VariableFilter currentFilter, final FilterType filterType) {
		synchronized (fUpdateLock) {
			fInputUpdate = true;
		}
		final VariableFilter filter;
		synchronized (this) {
			final int idx = fFilters.indexOf(currentFilter);
			if (idx < 0) {
				return currentFilter;
			}
			filter = createFilter(filterType, currentFilter.getColumn());
			if (filter == null) {
				return currentFilter;
			}
			filter.load(currentFilter);
			fFilters.set(idx, filter);
			filterReplaced(idx, currentFilter, filter);
		}
		synchronized (fUpdateLock) {
			fInputUpdate = false;
			filter.scheduleUpdate();
		}
		return filter;
	}
	
	protected VariableFilter createFilter(final FilterType filterType, final RDataTableColumn column) {
		if (filterType == null) {
			return null;
		}
		switch (filterType.getId()) {
		case 0:
			return new LevelVariableFilter(this, column);
		case 1:
			return new IntervalVariableFilter(this, column);
		case 2:
			return new TextVariableFilter(this, column);
		default:
			throw new UnsupportedOperationException(filterType.toString());
		}
	}
	
	public synchronized List<VariableFilter> getFilters() {
		return new ConstArrayList<VariableFilter>(fFilters);
	}
	
	
	protected void scheduleUpdate(final boolean all) {
		synchronized (fUpdateLock) {
			if (all) {
				fUpdateAll = true;
			}
			if (fUpdateScheduled || fInputUpdate) {
				return;
			}
			if (fInput != null) {
				fInput.getRHandle().getQueue().add(fUpdateRunnable);
				fUpdateScheduled = true;
			}
		}
	}
	
	protected ITool getTool() {
		final RDataTableContentDescription input = fInput;
		return (input != null) ? input.getRHandle() : null;
	}
	
	private void runUpdate(final IRToolService r, final IProgressMonitor monitor) {
		final boolean all;
		synchronized (fUpdateLock) {
			fUpdateScheduled = false;
			if (fInputUpdate || fInput == null || fInput.getRHandle() != r.getTool()) {
				return;
			}
			all = fUpdateAll;
			fUpdateAll = false;
		}
		final VariableFilter[] filters;
		synchronized (this) {
			filters = fFilters.toArray(new VariableFilter[fFilters.size()]);
		}
		for (int i = 0; i < filters.length; i++) {
			final VariableFilter filter = filters[i];
			if (all || filter.fUpdateScheduled) {
				filter.fUpdateScheduled = false;
				Exception error = null;
				try {
					filter.update(r, monitor);
				}
				catch (final CoreException e) {
					error = e;
				}
				catch (final UnexpectedRDataException e) {
					error = e;
				}
				if (error != null) {
					error.printStackTrace();
					filters[i].setError(error.getMessage());
				}
			}
		}
		updateFilter(true);
	}
	
	
	public void addListener(final IFilterListener listener) {
		fListeners.add(listener);
	}
	
	public void removeListener(final IFilterListener listener) {
		fListeners.remove(listener);
	}
	
	public void addPostListener(final IFilterListener listener) {
		fPostListeners.add(listener);
	}
	
	public void removePostListener(final IFilterListener listener) {
		fPostListeners.remove(listener);
	}
	
	public String getFilterRExpression() {
		return fFilterRExpression;
	}
	
	public String getFilterRExpression(String varExpression, final int nameFlags) {
		final VariableFilter[] filters;
		synchronized (this) {
			if (fInputUpdate || fInput == null ) {
				return null;
			}
			filters = fFilters.toArray(new VariableFilter[fFilters.size()]);
		}
		if (varExpression == null) {
			varExpression = fInput.getElementName().getDisplayName(nameFlags);
		}
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < filters.length; i++) {
			final VariableFilter filter = filters[i];
			final String rExpression = filter.getFilterRExpression(varExpression, nameFlags);
			if (rExpression != null && !rExpression.isEmpty()) {
				if (sb.length() > 0) {
					sb.append(" & "); //$NON-NLS-1$
				}
				sb.append(rExpression);
			}
		}
		return sb.toString();
	}
	
	public void setEnabled(final boolean enabled) {
		if (fEnabled == enabled) {
			return;
		}
		fEnabled = enabled;
		notifyListeners(NO_DELAY);
	}
	
	public boolean getEnabled() {
		return fEnabled;
	}
	
	void updateFilter(final boolean delay) {
		final VariableFilter[] filters;
		synchronized (this) {
			if (fInputUpdate || fInput == null ) {
				return;
			}
			filters = fFilters.toArray(new VariableFilter[fFilters.size()]);
		}
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < filters.length; i++) {
			final VariableFilter filter = filters[i];
			final String rExpression = filter.getFilterRExpression();
			if (rExpression != null && !rExpression.isEmpty()) {
				if (sb.length() > 0) {
					sb.append(" & "); //$NON-NLS-1$
				}
				sb.append(rExpression);
			}
		}
		String filterRExpression;
		if (sb.length() == 0) {
			filterRExpression = null; 
			if (fFilterRExpression == null) {
				return;
			}
		}
		else {
			filterRExpression = sb.toString();
			if (filterRExpression.equals(fFilterRExpression)) {
				return;
			}
		}
		fFilterRExpression = filterRExpression;
		notifyListeners((delay) ? STD_DELAY : NO_DELAY);
	}
	
	private void notifyListeners(final int mode) {
		final int schedule = fListenerScheduled;
		if (schedule >= mode) {
			return;
		}
		fListenerScheduled = mode;
		runInRealm(fListenerRunnable);
	}
	
}
