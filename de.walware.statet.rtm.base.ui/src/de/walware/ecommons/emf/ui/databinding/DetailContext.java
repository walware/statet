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

package de.walware.ecommons.emf.ui.databinding;

import org.eclipse.core.databinding.observable.value.IObservableValue;

import de.walware.ecommons.emf.core.util.IEMFEditContext;
import de.walware.ecommons.emf.core.util.WrappedEMFEditContext;


public class DetailContext extends WrappedEMFEditContext {
	
	
	private final IObservableValue fBaseValue;
	
	
	public DetailContext(final IEMFEditContext parent, final IObservableValue detailValue) {
		super(parent);
		
		fBaseValue = detailValue;
	}
	
	
	@Override
	public IObservableValue getBaseObservable() {
		return fBaseValue;
	}
	
}
