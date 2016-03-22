/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.core.eval;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.statet.r.debug.core.IRValue;
import de.walware.statet.r.debug.core.IRVariable;


public class ExpressionAdapterFactory implements IAdapterFactory {
	
	
	private static final Class<?>[] ADAPTERS = new Class<?>[] {
		IRVariable.class,
	};
	
	
	public ExpressionAdapterFactory() {
	}
	
	
	@Override
	public Class<?>[] getAdapterList() {
		return ADAPTERS;
	}
	
	@Override
	public <T> @Nullable T getAdapter(final Object adaptableObject, final Class<T> adapterType) {
		if (adapterType == IRVariable.class) {
			final IValue value= ((IExpression) adaptableObject).getValue();
			if (value instanceof IRValue) {
				return (T) ((IRValue) value).getAssignedVariable();
			}
			return null;
		}
		return null;
	}
	
}
