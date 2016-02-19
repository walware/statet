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

import de.walware.ecommons.debug.core.model.IIndexedValue;
import de.walware.ecommons.debug.core.model.IVariableDim;


public class RArrayDimVariable extends RVariable implements IVariableDim, IIndexedValue {
	
	
	private final RArrayValue fMainValue;
	
	private final int[] fDimIndex;
	
	
	public RArrayDimVariable(final RArrayValue value, final int[] selected) {
		super(value.getDebugTarget());
		fMainValue = value;
		fDimIndex = selected;
	}
	
	
	@Override
	public String getName() throws DebugException {
		final StringBuilder sb = new StringBuilder();
		final int m = fMainValue.fDimCount - fDimIndex.length;
		{	final String name = fMainValue.getDimItemName(m, fDimIndex[0]);
			if (name != null) {
				sb.append(name);
				sb.append(' ');
			}
		}
		{	sb.append("[ "); //$NON-NLS-1$
			for (int i = 0; i < m; i++) {
				sb.append(", "); //$NON-NLS-1$
			}
			for (int i = m; i < fMainValue.fDimCount - 1; i++) {
				sb.append(fDimIndex[i - m] + 1);
				sb.append(", "); //$NON-NLS-1$
			}
			sb.append(fDimIndex[fDimIndex.length - 1] + 1);
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
		return ""; //$NON-NLS-1$
	}
	
	@Override
	public String getValueString() throws DebugException {
		final StringBuilder sb = new StringBuilder();
		final int m = fMainValue.fDimCount - fDimIndex.length;
		sb.append('[');
		sb.append(fMainValue.fDim.getInt(0));
		for (int i = 1; i < m; i++) {
			sb.append('×');
			sb.append(fMainValue.fDim.getInt(i));
		}
		sb.append(']');
		
		{	final String dimName = fMainValue.getDimName(m - 1);
			if (dimName != null) {
				sb.append(" / "); //$NON-NLS-1$
				sb.append(dimName);
			}
		}
		
		return sb.toString();
	}
	
	@Override
	public boolean hasVariables() throws DebugException {
		return (fMainValue.fDim.getInt(fMainValue.fDimCount - fDimIndex.length) > 0);
	}
	
	@Override
	public IVariable[] getVariables() throws DebugException {
		return RValue.PARTITION_FACTORY.getVariables(this);
	}
	
	
	@Override
	public long getSize() throws DebugException {
		return fMainValue.fDim.getInt(fMainValue.fDimCount - fDimIndex.length - 1);
	}
	
	@Override
	public IVariable[] getVariables(final long offset, final int length) {
		{	final int n = fMainValue.fDim.getInt(fMainValue.fDimCount - fDimIndex.length - 1);
			if (n <= 0) {
				throw new UnsupportedOperationException();
			}
			if (offset < 0 || length < 0 || offset > n - length) {
				throw new IllegalArgumentException();
			}
		}
		final int o = (int) offset;
		final RVariable[] variables = new RVariable[length];
		if (fDimIndex.length == fMainValue.fDimCount - 1) {
			for (int i = 0; i < length; i++) {
				final int[] d = new int[fDimIndex.length + 1];
				System.arraycopy(fDimIndex, 0, d, 1, fDimIndex.length);
				d[0] = o + i;
				variables[i] = new RArrayIndexVariable(fMainValue, d);
			}
		}
		else {
			for (int i = 0; i < length; i++) {
				final int[] d = new int[fDimIndex.length + 1];
				System.arraycopy(fDimIndex, 0, d, 1, fDimIndex.length);
				d[0] = o + i;
				variables[i] = new RArrayDimVariable(fMainValue, d);
			}
		}
		return variables;
	}
	
}
