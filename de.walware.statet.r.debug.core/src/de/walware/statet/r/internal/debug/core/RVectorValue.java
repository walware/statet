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
import org.eclipse.debug.core.model.IIndexedValue;
import org.eclipse.debug.core.model.IVariable;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RStore;
import de.walware.rj.data.RVector;
import de.walware.statet.r.core.data.ICombinedRElement;


public class RVectorValue extends RValue implements IIndexedValue {
	
	
	public static final int LOAD_SIZE = 1000;
	
	
	protected final int fLength;
	
	protected RVector<?>[] fData;
	
	
	public RVectorValue(final RElementVariable variable) {
		super(variable);
		fLength = fVariable.fElement.getLength();
	}
	
	
	@Override
	public String getValueString() throws DebugException {
		switch (fLength) {
		case 0:
			return "";
		case 1:
			final RStore data = getData(0);
			if (data == null) {
				throw newRequestLoadDataFailed();
			}
			return data.isNA(0) ? "NA" : data.getChar(0);
		default:
			return "[" + fLength + "]";
		}
	}
	
	@Override
	public boolean hasVariables() throws DebugException {
		return (fLength > 1);
	}
	
	@Override
	public IVariable[] getVariables() throws DebugException {
		return getVariables(1, getSize());
	}
	
	public int getInitialOffset() {
		return 1;
	}
	
	public int getSize() throws DebugException {
		if (fLength <= 1) {
			return 0;
		}
		return fLength;
	}
	
	public IVariable getVariable(final int offset) throws DebugException {
		final ICombinedRElement element = fVariable.fElement;
		if (element.getLength() <= 1) {
			throw newNotSupported();
		}
		if (offset < 1 || offset > element.getLength()) {
			throw newRequestIllegalIndexFailed();
		}
		return new RVectorIndexVariable(this, offset-1);
	}
	
	public IVariable[] getVariables(final int offset, final int length) throws DebugException {
		final ICombinedRElement element = fVariable.fElement;
		if (element.getLength() <= 1) {
			throw newNotSupported();
		}
		if (offset < 1 || length < 0 || offset+length-1 > element.getLength()) {
			throw newRequestIllegalIndexFailed();
		}
		final RVariable[] variables = new RVariable[length];
		for (int i = 0; i < length; i++) {
			variables[i] = new RVectorIndexVariable(this, offset+i-1);
		}
		return variables;
	}
	
	
	protected RStore getData(final int loadIdx) {
		final RVector<?> data = ensureData(loadIdx);
		return (data != null) ? data.getData() : null;
	}
	
	protected RStore getNames(final int loadIdx) {
		final RVector<?> data = ensureData(loadIdx);
		return (data != null) ? data.getNames() : null;
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
				final String[] command = new String[] { null, subVector((loadIdx) * LOAD_SIZE, length) };
				final RObject data = fVariable.fFrame.loadData(fVariable.fElement, command, fVariable.fStamp);
				if (data instanceof RVector) {
					fData[loadIdx] = (RVector<?>) data;
				}
			}
			return fData[loadIdx];
		}
	}
	
}
