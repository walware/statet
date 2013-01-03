/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.datafilter;

import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RStore;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.services.FunctionCall;

import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class IntervalVariableFilter extends VariableFilter {
	
	
	public static final int MIN_IDX = 0;
	public static final int MAX_IDX = 1;
	public static final int NA_IDX = 2;
	
	
	private RStore fMinMaxData;
	
	private final WritableValue fSelectedLowerValue;
	private final WritableValue fSelectedUpperValue;
	private final WritableValue fSelectedNA;
	
	
	protected IntervalVariableFilter(final FilterSet set, final RDataTableColumn column) {
		super(set, column);
		
		fSelectedLowerValue = new WritableValue(set.getRealm());
		fSelectedUpperValue = new WritableValue(set.getRealm());
		fSelectedNA = new WritableValue(set.getRealm(), true, Boolean.TYPE);
		registerObservable(fSelectedLowerValue);
		registerObservable(fSelectedUpperValue);
		registerObservable(fSelectedNA);
	}
	
	
	@Override
	public FilterType getType() {
		return FilterType.INTERVAL;
	}
	
	@Override
	public void load(final VariableFilter filter) {
		if (filter.getType() == FilterType.INTERVAL
				&& filter.getColumn().getDataStore().getStoreType() == getColumn().getDataStore().getStoreType()) {
			final IntervalVariableFilter intervalFilter = (IntervalVariableFilter) filter;
			runInRealm(new Runnable() {
				@Override
				public void run() {
					if (fMinMaxData == null) {
						fSelectedLowerValue.setValue(intervalFilter.getSelectedLowerValue().getValue());
						fSelectedUpperValue.setValue(intervalFilter.getSelectedUpperValue().getValue());
						fSelectedNA.setValue(intervalFilter.getSelectedNA().getValue());
					}
				}
			});
		}
	}
	
	@Override
	public void reset() {
		runInRealm(new Runnable() {
			@Override
			public void run() {
				if (fMinMaxData == null) {
					return;
				}
				fSelectedLowerValue.setValue(fMinMaxData.get(MIN_IDX));
				fSelectedUpperValue.setValue(fMinMaxData.get(MAX_IDX));
				fSelectedNA.setValue(Boolean.TRUE);
			}
		});
	}
	
	@Override
	protected void update(final IRToolService r,
			final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final RDataTableColumn column = getColumn();
		{	final FunctionCall fcall = r.createFunctionCall("rj:::.getDataIntervalValues"); //$NON-NLS-1$
			fcall.add(column.getRExpression());
			
			final RObject data = fcall.evalData(monitor);
			RDataUtil.checkRVector(data);
			setValues(RDataUtil.checkData(data.getData(), column.getDataStore().getStoreType()));
			return;
		}
	}
	
	@Override
	protected void setError(final String message) {
		runInRealm(new Runnable() {
			@Override
			public void run() {
				fMinMaxData = null;
				IntervalVariableFilter.super.setError(message);
				notifyListeners();
			}
		});
	}
	
	protected void setValues(final RStore minMaxData) {
		runInRealm(new Runnable() {
			@Override
			public void run() {
				boolean wasMin;
				{	final Object value = fSelectedLowerValue.getValue();
					wasMin = (value == null || value.equals(minMaxData.get(MIN_IDX)));
				}
				boolean wasMax;
				{	final Object value = fSelectedLowerValue.getValue();
					wasMax = (value == null || value.equals(minMaxData.get(MAX_IDX)));
				}
				fMinMaxData = minMaxData;
				if (wasMin) {
					fSelectedLowerValue.setValue(fMinMaxData.get(MIN_IDX));
				}
				if (wasMax) {
					fSelectedUpperValue.setValue(fMinMaxData.get(MAX_IDX));
				}
				IntervalVariableFilter.super.setError(null);
				notifyListeners();
			}
		});
	}
	
	private static boolean isSmaller(final Object e1, final Object e2) {
		if (e1 instanceof Integer) {
			return (((Integer) e1).doubleValue() < ((Integer) e2).doubleValue());
		}
		return (((Number) e1).doubleValue() < ((Number) e2).doubleValue());
	}
	
	private static boolean isGreater(final Object e1, final Object e2) {
		if (e1 instanceof Integer) {
			return (((Integer) e1).doubleValue() > ((Integer) e2).doubleValue());
		}
		return (((Number) e1).doubleValue() > ((Number) e2).doubleValue());
	}
	
	@Override
	protected String createFilter(final String varExpression) {
		if (fMinMaxData == null
				|| fSelectedLowerValue.getValue() == null
				|| fSelectedUpperValue.getValue() == null) {
			return null;
		}
		final StringBuilder sb = new StringBuilder();
		sb.append('(');
		{
			final Object lower = fSelectedLowerValue.getValue();
			if (isGreater(lower, fMinMaxData.get(MIN_IDX))) {
				sb.append(varExpression);
				sb.append(" >= "); //$NON-NLS-1$
				sb.append(lower);
			}
		}
		{	final Object upper = fSelectedUpperValue.getValue();
			if (isSmaller(upper, fMinMaxData.get(MAX_IDX))) {
				if (sb.length() > 1) {
					sb.append(" & "); //$NON-NLS-1$
				}
				sb.append(varExpression);
				sb.append(" <= "); //$NON-NLS-1$
				sb.append(upper);
			}
		}
//		if (fMinMaxData.getLogi(NA_IDX)) {
			final Boolean na = (!fMinMaxData.getLogi(NA_IDX))
					|| (Boolean) fSelectedNA.getValue();
			if (na) {
				if (sb.length() > 1) {
					sb.insert(0, '(');
					sb.append(") | "); //$NON-NLS-1$
					sb.append("is.na(").append(varExpression).append(')'); //$NON-NLS-1$
				}
			}
			else { // !na
				if (sb.length() > 1) {
					sb.append(" & "); //$NON-NLS-1$
				}
				sb.append("!is.na(").append(varExpression).append(')'); //$NON-NLS-1$
			}
//		}
		sb.append(')');
		return (sb.length() <= 2) ? "" : sb.toString(); //$NON-NLS-1$
	}
	
	
	public RStore getMinMaxData() {
		return fMinMaxData;
	}
	
	
	public WritableValue getSelectedLowerValue() {
		return fSelectedLowerValue;
	}
	
	public WritableValue getSelectedUpperValue() {
		return fSelectedUpperValue;
	}
	
	public WritableValue getSelectedNA() {
		return fSelectedNA;
	}
	
}
