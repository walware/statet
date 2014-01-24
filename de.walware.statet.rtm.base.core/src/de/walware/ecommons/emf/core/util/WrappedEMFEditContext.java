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

package de.walware.ecommons.emf.core.util;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.edit.domain.EditingDomain;


public class WrappedEMFEditContext implements IEMFEditContext {
	
	
	private IEMFEditContext fParent;
	
	
	public WrappedEMFEditContext(final IEMFEditContext parent) {
		setParent(parent);
	}
	
	
	protected void setParent(final IEMFEditContext parent) {
		if (parent == null) {
			throw new NullPointerException("parent");
		}
		fParent = parent;
	}
	
	@Override
	public DataBindingContext getDataBindingContext() {
		return fParent.getDataBindingContext();
	}
	
	@Override
	public Realm getRealm() {
		return fParent.getRealm();
	}
	
	@Override
	public IObservableValue getBaseObservable() {
		return fParent.getBaseObservable();
	}
	
	@Override
	public EditingDomain getEditingDomain() {
		return fParent.getEditingDomain();
	}
	
	@Override
	public Object getAdapter(final Class required) {
		if (required.isInstance(this)) {
			return this;
		}
		return fParent.getAdapter(required);
	}
	
}
