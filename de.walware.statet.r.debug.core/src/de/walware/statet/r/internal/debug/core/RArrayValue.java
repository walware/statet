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
import org.eclipse.debug.core.model.IVariable;

import de.walware.rj.data.RArray;
import de.walware.rj.data.RIntegerStore;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RStore;
import de.walware.rj.data.RVector;


public class RArrayValue extends RValue implements IIndexedValue {
	
	
	public static final int LOAD_SIZE = 500;
	
	
	protected RVector<?>[] fData;
	protected RVector<?> fDimNames;
	protected RVector<?>[][] fDimEntryNames;
	
	protected final int fLength;
	protected final RIntegerStore fDim;
	
	
	public RArrayValue(final RElementVariable variable) {
		super(variable);
		
		fLength = fVariable.fElement.getLength();
		fDim = ((RArray<?>) fVariable.fElement).getDim();
	}
	
	
	@Override
	public String getValueString() throws DebugException {
		final StringBuilder sb = new StringBuilder();
		sb.append('['); 
		if (fDim.getLength() > 0) {
			sb.append(fDim.getInt(0));
			for (int i = 1; i < fDim.getLength(); i++) {
				sb.append('×');
				sb.append(fDim.getInt(i));
			}
		}
		sb.append(']');
		
		final RStore dimNames = getDimNames();
		if (dimNames != null && 0 < dimNames.getLength()) {
			sb.append(" / ");
			sb.append(dimNames.get(fDim.getLength()-1));
		}
		
		return sb.toString();
	}
	
	@Override
	public boolean hasVariables() throws DebugException {
		return (fLength > 0 && fDim.getInt(fDim.getLength()-1) > 0);
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
		return (fLength > 0) ? fDim.getInt(fDim.getLength()-1) : 0;
	}
	
	@Override
	public IVariable getVariable(final int offset) throws DebugException {
		{	final int n = fDim.getInt(fDim.getLength()-1);
			if (n <= 0) {
				throw newNotSupported();
			}
			if (offset < 1 || offset > n) {
				throw newRequestIllegalIndexFailed();
			}
		}
		if (fDim.getLength() == 1) {
			final int[] d = new int[] { offset-1 };
			return new RArrayIndexVariable(this, d);
		}
		else {
			final int[] d = new int[] { offset-1 };
			return new RArrayDimVariable(this, d);
		}
	}
	
	@Override
	public IVariable[] getVariables(final int offset, final int length) throws DebugException {
		{	final int n = fDim.getInt(fDim.getLength()-1);
			if (n <= 0) {
				throw newNotSupported();
			}
			if (offset < 1 || length < 0 || offset+length-1 > n) {
				throw newRequestIllegalIndexFailed();
			}
		}
		final RVariable[] variables = new RVariable[length];
		if (fDim.getLength() == 1) {
			for (int i = 0; i < length; i++) {
				final int[] d = new int[] { offset+i-1 };
				variables[i] = new RArrayIndexVariable(this, d);
			}
		}
		else {
			for (int i = 0; i < length; i++) {
				final int[] d = new int[] { offset+i-1 };
				variables[i] = new RArrayDimVariable(this, d);
			}
		}
		return variables;
	}
	
	
	protected RStore getData(final int loadIdx) {
		final RVector<?> data = ensureData(loadIdx);
		return (data != null) ? data.getData() : null;
	}
	
	protected RStore getDimNames() {
		synchronized (fVariable) {
			if (fVariable.fValue != this) {
				return null;
			}
			if (fDimNames == null) {
				final String[] command = new String[] { "names(dimnames(", null, "))" };
				final RObject data = fVariable.fFrame.loadData(fVariable.fElement, command, fVariable.fStamp);
				if (data instanceof RVector) {
					fDimNames = (RVector<?>) data;
				}
			}
			return (fDimNames != null) ? fDimNames.getData() : null;
		}
	}
	
	protected RStore getDimNames(final int dim, final int loadIdx) {
		synchronized (fVariable) {
			if (fVariable.fValue != this) {
				return null;
			}
			if (fDimEntryNames == null) {
				fDimEntryNames = new RVector[fDim.getLength()][];
			}
			if (fDimEntryNames[dim] == null) {
				fDimEntryNames[dim] = new RVector[1 + fDim.get(dim) / LOAD_SIZE];
			}
			if (fDimEntryNames[dim][loadIdx] == null) {
				int length;
				if (loadIdx == fDim.get(dim) / LOAD_SIZE) { // last
					length = fDim.get(dim) % LOAD_SIZE;
					if (length == 0) {
						length = LOAD_SIZE;
					}
				}
				else {
					length = LOAD_SIZE;
				}
				final String[] command = new String[] { "dimnames(", null, ")", subList(dim),
						subVector((loadIdx) * LOAD_SIZE, length) };
				final RObject data = fVariable.fFrame.loadData(fVariable.fElement, command, fVariable.fStamp);
				if (data instanceof RVector) {
					fDimEntryNames[dim][loadIdx] = (RVector<?>) data;
				}
			}
			return (fDimEntryNames[dim][loadIdx] != null) ? fDimEntryNames[dim][loadIdx].getData() : null;
		}
	}
	
	private RVector<?> ensureData(final int loadIdx) {
		synchronized (fVariable) {
			if (fVariable.fValue != this) {
				return null;
			}
			if (fData == null) {
				fData = new RVector[1 + fLength / LOAD_SIZE];
			}
			if (fData[loadIdx] == null) {
				int length;
				if (loadIdx == fLength / LOAD_SIZE) { // last
					length = fLength % LOAD_SIZE;
					if (length == 0) {
						length = LOAD_SIZE;
					}
				}
				else {
					length = LOAD_SIZE;
				}
				final String[] command = new String[] { "as.vector(", null, subVector((loadIdx) * LOAD_SIZE, length), ")" };
				final RObject data = fVariable.fFrame.loadData(fVariable.fElement, command, fVariable.fStamp);
				if (data instanceof RVector) {
					fData[loadIdx] = (RVector<?>) data;
				}
			}
			return fData[loadIdx];
		}
	}
	
}
