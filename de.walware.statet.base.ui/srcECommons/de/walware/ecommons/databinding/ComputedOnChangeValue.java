/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.databinding;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;


/**
 * 
 */
public abstract class ComputedOnChangeValue extends AbstractObservableValue {
	
	
	private Object fValueType;
	
	private IObservable[] fDependencies;
	private IChangeListener fListener;
	
	private boolean fSetting;
	
	private Object fValue;
	
	
	public ComputedOnChangeValue(final Object valueType, final IObservable... dependencies) {
		super(dependencies[0].getRealm());
		fValueType = valueType;
		fDependencies = dependencies;
		fListener = new IChangeListener() {
			public void handleChange(final ChangeEvent event) {
				if (!fSetting) {
					final Object newValue = calculate();
					final Object oldValue = fValue;
					if ((oldValue != null) ? !oldValue.equals(newValue) : null != newValue) {
						fireValueChange(Diffs.createValueDiff(oldValue, newValue));
					}
				}
			}
		};
		for (final IObservable obs : dependencies) {
			obs.addChangeListener(fListener);
		}
	}
	
	@Override
	public synchronized void dispose() {
		for (final IObservable obs : fDependencies) {
			obs.removeChangeListener(fListener);
		}
		super.dispose();
	}
	
	
	public Object getValueType() {
		return fValueType;
	}
	
	@Override
	protected final Object doGetValue() {
		return calculate();
	}
	
	@Override
	protected final void doSetValue(final Object value) {
		fSetting = true;
		try {
			extractAndSet(value);
		}
		finally {
			fSetting = false;
		}
	}
	
	protected abstract Object calculate();
	
	protected void extractAndSet(final Object value) {
		throw new UnsupportedOperationException();
	}
	
}
