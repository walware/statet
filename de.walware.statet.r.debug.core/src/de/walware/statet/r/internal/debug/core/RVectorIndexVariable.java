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
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

import de.walware.ecommons.debug.core.model.IIndexedVariableItem;

import de.walware.rj.data.RDataUtil;



public class RVectorIndexVariable extends RVariable implements IIndexedVariableItem, IValue {
	
	
	private final RVectorValue fMainValue;
	
	private final long fIndex;
	
	
	public RVectorIndexVariable(final RVectorValue value, final long index) {
		super(value.getDebugTarget());
		fMainValue = value;
		fIndex = index;
	}
	
	
	@Override
	public String getName() throws DebugException {
		final StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(fIndex + 1);
		sb.append(']');
		final String name = fMainValue.getName(fIndex);
		if (name != null) {
			sb.append(' ');
			sb.append(' ');
			sb.append(name);
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
		final String data = fMainValue.getData(fIndex);
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
