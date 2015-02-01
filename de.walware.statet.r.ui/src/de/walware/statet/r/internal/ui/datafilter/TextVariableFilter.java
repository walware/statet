/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.datafilter;

import java.util.Collection;

import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RObject;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.data.defaultImpl.RCharacterDataImpl;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.services.FunctionCall;

import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class TextVariableFilter extends VariableFilter {
	
	
	private TextSearchType fSearchType;
	private String fSearchText;
	
	private RCharacterDataImpl fAvailableValues;
	
	private final WritableSet fSelectedValues;
	
	
	public TextVariableFilter(final FilterSet set, final RDataTableColumn column) {
		super(set, column);
		
		fAvailableValues = NO_VALUES;
		fSelectedValues = new WritableSet(set.getRealm());
		registerObservable(fSelectedValues);
	}
	
	
	@Override
	public FilterType getType() {
		return FilterType.TEXT;
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
					if (fAvailableValues.getLength() == 0) {
						fAvailableValues = textFilter.fAvailableValues;
					}
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
	protected void update(final IRToolService r, final IProgressMonitor monitor)
			throws CoreException, UnexpectedRDataException {
		final RDataTableColumn column = getColumn();
		TextSearchType searchType;
		String searchText;
		synchronized (this) {
			searchText = fSearchText;
			searchType = fSearchType;
			fSearchText = null;
			fSearchType = null;
		}
		if (searchType != null && searchText != null) {
			final FunctionCall fcall = r.createFunctionCall("rj:::.searchDataTextValues"); //$NON-NLS-1$
			fcall.add(column.getRExpression());
			fcall.addInt("type", searchType.getId()); //$NON-NLS-1$
			fcall.addChar("pattern", searchText); //$NON-NLS-1$
			fcall.addInt("max", 100); //$NON-NLS-1$
			
			final RObject data = fcall.evalData(monitor);
			if (data.getRObjectType() == RObject.TYPE_NULL) {
				setError(Messages.TextFilter_TooMuch_message);
				return;
			}
			addValues(RDataUtil.checkRCharVector(data).getData());
		}
	}
	
	private static RCharacterDataImpl combine(final RCharacterDataImpl old, final RCharacterStore add) {
		if (add.getLength() == 0) {
			return old;
		}
		if (old.getLength() == 0 && add instanceof RCharacterDataImpl) {
			return (RCharacterDataImpl) add;
		}
		final String[] values = new String[(int) Math.max(old.getLength() + add.getLength(), 10000)];
		int i = 0;
		for (; i < add.getLength(); i++) {
			values[i] = add.get(i);
		}
		for (int j = 0; j < add.getLength() && i < values.length; j++) {
			final String s = add.get(i);
			if (!add.contains(s)) {
				values[i++] = s;
			}
		}
		return new RCharacterDataImpl(values, i);
	}
	
	@Override
	protected void setError(final String message) {
		runInRealm(new Runnable() {
			@Override
			public void run() {
				TextVariableFilter.super.setError(message);
				notifyListeners();
			}
		});
	}
	
	protected void addValues(final RCharacterStore add) {
		runInRealm(new Runnable() {
			@Override
			public void run() {
				fAvailableValues = combine(fAvailableValues, add);
				TextVariableFilter.super.setError(null);
				notifyListeners();
			}
		});
	}
	
	@Override
	protected String createFilter(final String varExpression) {
		return LevelVariableFilter.createLevelFilter(fAvailableValues, fSelectedValues, varExpression);
	}
	
	
	public void search(final TextSearchType type, final String text) {
		synchronized (this) {
			fSearchType = type;
			fSearchText = text;
		}
		scheduleUpdate();
	}
	
	public RCharacterStore getAvailableValues() {
		return fAvailableValues;
	}
	
	public WritableSet getSelectedValues() {
		return fSelectedValues;
	}
	
	public void removeAllValues() {
		fAvailableValues = NO_VALUES;
		fSelectedValues.clear();
	}
	
	public void removeValues(final Collection<String> values) {
		if (values.isEmpty()) {
			return;
		}
		for (final String value : values) {
			final int idx = (int) fAvailableValues.indexOf(value);
			if (idx >= 0) {
				fAvailableValues.remove(idx);
			}
		}
		fSelectedValues.removeAll(values);
	}
	
}
