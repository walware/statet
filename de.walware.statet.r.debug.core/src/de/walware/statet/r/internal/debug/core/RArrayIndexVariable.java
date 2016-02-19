/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.core;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

import de.walware.ecommons.debug.core.model.IIndexedVariableItem;

import de.walware.rj.data.RDataUtil;


public class RArrayIndexVariable extends RVariable implements IIndexedVariableItem, IValue {
	
	
	private final RArrayValue fMainValue;
	
	private final int[] fDimIndex;
	
	
	public RArrayIndexVariable(final RArrayValue value, final int[] index) {
		super(value.getDebugTarget());
		fMainValue = value;
		fDimIndex = index;
	}
	
	
	@Override
	public String getName() throws DebugException {
		final StringBuilder sb = new StringBuilder();
		{	final String name = fMainValue.getDimItemName(0, fDimIndex[0]);
			if (name != null) {
				sb.append(name);
				sb.append(' ');
			}
		}
		{	sb.append('[');
			for (int i = 0; i < fMainValue.fDimCount - 1; i++) {
				sb.append(fDimIndex[i] + 1);
				sb.append(", "); //$NON-NLS-1$
			}
			sb.append(fDimIndex[fMainValue.fDimCount - 1] + 1);
			sb.append(']');
		}
		return sb.toString();
	}

	@Override
	public IValue getValue() throws DebugException {
		return this;
	}
	
	@Override
	public boolean isAllocated() throws DebugException {
		return true;
	}
	
	@Override
	public String getReferenceTypeName() throws DebugException {
		return RDataUtil.getStoreAbbr(fMainValue.fVariable.fElement.getData());
	}
	
	@Override
	public String getValueString() throws DebugException {
		final long dataIdx = RDataUtil.getDataIdx(fMainValue.fDim, fDimIndex);
		final String data = fMainValue.getData(dataIdx);
		if (data == null) {
			throw newRequestLoadDataFailed();
		}
		return data;
	}
	
	@Override
	public boolean hasVariables() throws DebugException {
		return false;
	}
	
	@Override
	public IVariable[] getVariables() throws DebugException {
		throw newNotSupported();
	}
	
}
