/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.core;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RStore;

import de.walware.statet.r.debug.core.IRIndexVariable;


public class RArrayIndexVariable extends RVariable implements IRIndexVariable, IValue {
	
	
	private final RArrayValue fMainValue;
	
	private final int[] fIndex;
	
	
	public RArrayIndexVariable(final RArrayValue value, final int[] index) {
		super(value.getDebugTarget());
		fMainValue = value;
		fIndex = index;
	}
	
	
	@Override
	public String getName() throws DebugException {
		final StringBuilder sb = new StringBuilder();
		{	final RStore names = fMainValue.getDimNames(0, fIndex[0] / RArrayValue.LOAD_SIZE);
			if (names != null) {
				final int index = fIndex[0] % RArrayValue.LOAD_SIZE;
				sb.append(names.isNA(index) ? "<NA>" : names.getChar(index));
				sb.append(' ');
			}
		}
		{	final int n = fMainValue.fDim.getLength();
			sb.append('[');
			for (int i = 0; i < n-1; i++) {
				sb.append(fIndex[i]+1);
				sb.append(", ");
			}
			sb.append(fIndex[n-1]+1);
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
		final int dataIdx = RDataUtil.getDataIdx(fMainValue.fDim, fIndex);
		final RStore data = fMainValue.getData(dataIdx / RArrayValue.LOAD_SIZE);
		if (data == null) {
			throw newRequestLoadDataFailed();
		}
		final int index = dataIdx % RArrayValue.LOAD_SIZE;
		return data.isNA(index) ? "NA" : data.getChar(index);
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
