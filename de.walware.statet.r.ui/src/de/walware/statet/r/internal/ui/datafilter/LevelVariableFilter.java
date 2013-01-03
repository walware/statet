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

import java.util.Collection;

import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RStore;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.services.FunctionCall;

import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class LevelVariableFilter extends VariableFilter {
	
	
	private RStore fAvailableValues;
	
	private final WritableSet fSelectedValues;
	
	
	public LevelVariableFilter(final FilterSet set, final RDataTableColumn column) {
		super(set, column);
		
		fAvailableValues = NO_VALUES;
		fSelectedValues = new WritableSet(set.getRealm());
		registerObservable(fSelectedValues);
	}
	
	
	@Override
	public FilterType getType() {
		return FilterType.LEVEL;
	}
	
	@Override
	public void load(final VariableFilter filter) {
		if (filter.getType() == FilterType.LEVEL) {
			final LevelVariableFilter levelFilter = (LevelVariableFilter) filter;
			runInRealm(new Runnable() {
				@Override
				public void run() {
					fSelectedValues.addAll(levelFilter.getSelectedValues());
				}
			});
		}
		else if (filter.getType() == FilterType.TEXT) {
			final TextVariableFilter textFilter = (TextVariableFilter) filter;
			runInRealm(new Runnable() {
				@Override
				public void run() {
					fSelectedValues.addAll(textFilter.getSelectedValues());
				}
			});
		}
	}
	
	@Override
	public void reset() {
		runInRealm(new Runnable() {
			@Override
			public void run() {
				fSelectedValues.clear();
			}
		});
	}
	
	@Override
	protected void update(final IRToolService r,
			final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final RDataTableColumn column = getColumn();
		{	final FunctionCall fcall = r.createFunctionCall("rj:::.getDataLevelValues"); //$NON-NLS-1$
			fcall.add(column.getRExpression());
			fcall.addInt("max", 1000); //$NON-NLS-1$
			
			final RObject data = fcall.evalData(monitor);
			if (data.getRObjectType() == RObject.TYPE_NULL) {
				setError(Messages.LevelFilter_TooMuch_message);
				return;
			}
			RDataUtil.checkRVector(data);
			if (column.getDataStore().getStoreType() == RStore.FACTOR) {
				setValues(RDataUtil.checkData(data.getData(), RStore.CHARACTER));
			}
			else {
				setValues(RDataUtil.checkData(data.getData(), column.getDataStore().getStoreType()));
			}
			return;
		}
	}
	
	@Override
	protected void setError(final String message) {
		runInRealm(new Runnable() {
			@Override
			public void run() {
				fAvailableValues = NO_VALUES;
				LevelVariableFilter.super.setError(message);
				notifyListeners();
			}
		});
	}
	
	protected void setValues(final RStore values) {
		runInRealm(new Runnable() {
			@Override
			public void run() {
				if (!fAvailableValues.equals(values) || getError() != null) {
					fAvailableValues = values;
					LevelVariableFilter.super.setError(null);
					notifyListeners();
				}
			}
		});
	}
	
	
	@Override
	protected String createFilter(final String varExpression) {
		return createLevelFilter(fAvailableValues, fSelectedValues, varExpression);
	}
	
	static String createLevelFilter(final RStore availableValues, final Collection<?> selectedValues,
			final String varExpression) {
		if (availableValues == null) {
			return null;
		}
		final StringBuilder sb = new StringBuilder();
		sb.append('(');
		int num = 0;
		int na = 0;
		for (int i = 0; i < availableValues.getLength(); i++) {
			final Object element = availableValues.get(i);
			if (element == null) {
				na = (selectedValues.contains(element)) ? 1 : -1;
				continue;
			}
			if (selectedValues.contains(element)) {
				if (num > 0) {
					sb.append(" | "); //$NON-NLS-1$
				}
				sb.append(varExpression);
				sb.append(" == "); //$NON-NLS-1$
				if (element instanceof String) {
					sb.append('"').append(RUtil.escapeCompletely((String) element)).append('"');
				}
				else {
					sb.append(element);
				}
				num++;
			}
		}
		if (num > 0 || na == 1) {
			if (na >= 0) {
				if (num > 0) {
					sb.append(" | "); //$NON-NLS-1$
				}
				sb.append("is.na(").append(varExpression).append(')'); //$NON-NLS-1$
			}
			else {
				if (num > 0) {
					sb.insert(0, '(');
					sb.append(')');
					sb.append(" & "); //$NON-NLS-1$
				}
				sb.append("!is.na(").append(varExpression).append(')'); //$NON-NLS-1$
			}
			if (na == 1) {
				num++;
			}
		}
		sb.append(')');
		return (sb.length() <= 2 || num == availableValues.getLength()) ? "" : sb.toString(); //$NON-NLS-1$
	}
	
	
	public RStore getAvailableValues() {
		return fAvailableValues;
	}
	
	public WritableSet getSelectedValues() {
		return fSelectedValues;
	}
	
}
