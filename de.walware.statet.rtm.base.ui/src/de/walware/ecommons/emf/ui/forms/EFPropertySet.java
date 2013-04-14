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

package de.walware.ecommons.emf.ui.forms;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.emf.core.util.IEMFEditContext;


public class EFPropertySet {
	
	
	private final List<EFProperty> fList = new ArrayList<EFProperty>(8);
	
	
	public EFPropertySet() {
	}
	
	
	public void add(final EFProperty property) {
		if (property != null) {
			fList.add(property);
		}
	}
	
	public void createControls(final Composite parent, final IEFFormPage toolkit) {
		for (final EFProperty property : fList) {
			property.create(parent, toolkit);
		}
	}
	
	public void bind(final IEMFEditContext context) {
		for (final EFProperty property : fList) {
			property.bind(context);
		}
	}
	
	public EFProperty get(final EStructuralFeature eFeature) {
		for (final EFProperty property : fList) {
			if (property.getEFeature() == eFeature) {
				return property;
			}
		}
		return null;
	}
	
	public List<EFProperty> getAll() {
		return fList;
	}
	
	public List<IObservable> getModelObservables() {
		final List<IObservable> observables = new ArrayList<IObservable>();
		for (final EFProperty property : fList) {
			final IObservable observable = property.getPropertyObservable();
			if (observable != null) {
				observables.add(observable);
			}
		}
		return observables;
	}
	
}
