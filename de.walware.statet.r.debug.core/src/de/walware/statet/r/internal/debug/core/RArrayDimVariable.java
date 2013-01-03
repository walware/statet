/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.debug.core.model.IIndexedValue;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

import de.walware.rj.data.RStore;

import de.walware.statet.r.debug.core.IRDimVariable;


public class RArrayDimVariable extends RVariable implements IRDimVariable, IIndexedValue {
	
	
	private final RArrayValue fMainValue;
	
	private final int[] fSeletectedDim;
	
	
	public RArrayDimVariable(final RArrayValue value, final int[] selected) {
		super(value.getDebugTarget());
		fMainValue = value;
		fSeletectedDim = selected;
	}
	
	
	@Override
	public String getName() throws DebugException {
		final StringBuilder sb = new StringBuilder();
		{	final int dim = fMainValue.fDim.getLength()-fSeletectedDim.length;
			final RStore names = fMainValue.getDimNames(dim, fSeletectedDim[0] / RArrayValue.LOAD_SIZE);
			if (names != null) {
				final int index = fSeletectedDim[0] % RArrayValue.LOAD_SIZE;
				sb.append(names.isNA(index) ? "<NA>" : names.getChar(index));
				sb.append(' ');
			}
		}
		{	final int n = fMainValue.fDim.getLength();
			final int m = n - fSeletectedDim.length;
			sb.append("[ ");
			for (int i = 0; i < m; i++) {
				sb.append(", ");
			}
			for (int i = m; i < n-1; i++) {
				sb.append(fSeletectedDim[i-m]+1);
				sb.append(", ");
			}
			sb.append(fSeletectedDim[fSeletectedDim.length-1]+1);
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
		return "";
	}
	
	@Override
	public String getValueString() throws DebugException {
		final StringBuilder sb = new StringBuilder();
		final int m = fMainValue.fDim.getLength() - fSeletectedDim.length;
		sb.append('[');
		sb.append(fMainValue.fDim.getInt(0));
		for (int i = 1; i < m; i++) {
			sb.append('×');
			sb.append(fMainValue.fDim.getInt(i));
		}
		sb.append(']');
		
		final RStore dimNames = fMainValue.getDimNames();
		if (dimNames != null) {
			sb.append(" / ");
			sb.append(dimNames.get(m-1));
		}
		
		return sb.toString();
	}
	
	@Override
	public boolean hasVariables() throws DebugException {
		return (fMainValue.fDim.getInt(fMainValue.fDim.getLength()-fSeletectedDim.length) > 0);
	}
	
	@Override
	public IVariable[] getVariables() throws DebugException {
		return getVariables(1, getSize());
	}
	
	@Override
	public int getInitialOffset() {
		return 1;
	}
	
	@Override
	public int getSize() throws DebugException {
		return fMainValue.fDim.getInt(fMainValue.fDim.getLength()-fSeletectedDim.length-1);
	}
	
	@Override
	public IVariable getVariable(final int offset) throws DebugException {
		{	final int n = fMainValue.fDim.getInt(fMainValue.fDim.getLength()-fSeletectedDim.length-1);
			if (n <= 0) {
				throw newNotSupported();
			}
			if (offset < 1 || offset > n) {
				throw newRequestIllegalIndexFailed();
			}
		}
		if (fSeletectedDim.length == fMainValue.fDim.getLength() - 1) {
			final int[] d = new int[fSeletectedDim.length+1];
			System.arraycopy(fSeletectedDim, 0, d, 1, fSeletectedDim.length);
			d[0] = offset-1;
			return new RArrayIndexVariable(fMainValue, d);
		}
		else {
			final int[] d = new int[fSeletectedDim.length+1];
			System.arraycopy(fSeletectedDim, 0, d, 1, fSeletectedDim.length);
			d[0] = offset-1;
			return new RArrayDimVariable(fMainValue, d);
		}
	}
	
	@Override
	public IVariable[] getVariables(final int offset, final int length) throws DebugException {
		{	final int n = fMainValue.fDim.getInt(fMainValue.fDim.getLength()-fSeletectedDim.length-1);
			if (n <= 0) {
				throw newNotSupported();
			}
			if (offset < 1 || length < 0 || offset+length-1 > n) {
				throw newRequestIllegalIndexFailed();
			}
		}
		final RVariable[] variables = new RVariable[length];
		if (fSeletectedDim.length == fMainValue.fDim.getLength() - 1) {
			for (int i = 0; i < length; i++) {
				final int[] d = new int[fSeletectedDim.length+1];
				System.arraycopy(fSeletectedDim, 0, d, 1, fSeletectedDim.length);
				d[0] = offset+i-1;
				variables[i] = new RArrayIndexVariable(fMainValue, d);
			}
		}
		else {
			for (int i = 0; i < length; i++) {
				final int[] d = new int[fSeletectedDim.length+1];
				System.arraycopy(fSeletectedDim, 0, d, 1, fSeletectedDim.length);
				d[0] = offset+i-1;
				variables[i] = new RArrayDimVariable(fMainValue, d);
			}
		}
		return variables;
	}
	
}
