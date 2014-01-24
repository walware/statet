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

package de.walware.statet.rtm.ggplot.internal.ui.editors;

import java.util.List;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import de.walware.ecommons.ui.util.UIAccess;


public class AutoExpandListener implements IValueChangeListener, DisposeListener {
	
	
	private final ExpandableComposite fComposite;
	
	private int fValues;
	
	private List<? extends IObservable> fObservables;
	
	
	public AutoExpandListener(final ExpandableComposite composite, final List<? extends IObservable> observables) {
		fComposite = composite;
		
		fObservables = observables;
		for (final IObservable observable : observables) {
			if (observable instanceof IObservableValue) {
				final IObservableValue observableValue = (IObservableValue) observable;
				if (observableValue.getValue() != null) {
					fValues++;
				}
				observableValue.addValueChangeListener(this);
			}
		}
		if (fValues > 0) {
			fComposite.setExpanded(true);
		}
		
		fComposite.addDisposeListener(this);
	}
	
	
	@Override
	public void handleValueChange(final ValueChangeEvent event) {
		if (fValues > 0 || fComposite.isExpanded() || fObservables == null) {
			return;
		}
		if (event.diff.getOldValue() != null) {
			if (event.diff.getNewValue() == null) {
				fValues--;
			}
		}
		else {
			if (event.diff.getNewValue() != null) {
				fValues++;
			}
		}
		if (fValues > 0 && UIAccess.isOkToUse(fComposite)) {
			fComposite.setExpanded(true);
		}
	}
	
	@Override
	public void widgetDisposed(final DisposeEvent e) {
		final List<? extends IObservable> observables = fObservables;
		fObservables = null;
		if (observables != null) {
			for (final IObservable observable : observables) {
				if (observable instanceof IObservableValue) {
					final IObservableValue observableValue = (IObservableValue) observable;
					if (!observableValue.isDisposed()) {
						observableValue.removeValueChangeListener(this);
					}
				}
			}
		}
	}
	
}
