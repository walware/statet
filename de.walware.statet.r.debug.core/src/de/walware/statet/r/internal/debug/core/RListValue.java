/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
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
import org.eclipse.debug.core.model.IVariable;

import de.walware.ecommons.debug.core.model.IIndexedValue;
import de.walware.ecommons.debug.core.model.VariablePartitionFactory;

import de.walware.statet.r.console.core.RWorkspace.ICombinedRList;
import de.walware.statet.r.core.data.ICombinedRElement;


public class RListValue extends RValue implements IIndexedValue {
	
	
	private static class RListPartition extends RVariablePartition<RListValue> {
		
		
		public RListPartition(final RListValue value, final VariablePartitionFactory<RListValue>.PartitionHandle partition) {
			super(value, partition);
		}
		
		
		@Override
		protected String getNameIndexLabel(final long idx) {
			return value.fElement.getName(idx);
		}
		
	}
	
	private static final VariablePartitionFactory<RListValue> LIST_PARTITION_FACTORY = new VariablePartitionFactory<RListValue>() {
		
		@Override
		protected IVariable createPartition(final RListValue value, final PartitionHandle partition) {
			return new RListPartition(value, partition);
		}
		
	};
	
	
	private final ICombinedRList fElement; 
	
	
	public RListValue(final RElementVariable variable, final ICombinedRList element) {
		super(variable);
		fElement = element;
	}
	
	
	@Override
	public String getValueString() throws DebugException {
		final StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(fElement.getLength());
		sb.append(']');
		return sb.toString();
	}
	
	@Override
	public boolean hasVariables() throws DebugException {
		return fElement.hasModelChildren(null);
	}
	
	@Override
	public IVariable[] getVariables() throws DebugException {
		return LIST_PARTITION_FACTORY.getVariables(this);
	}
	
	
	@Override
	public long getSize() throws DebugException {
		return fElement.getLength();
	}
	
	@Override
	public IVariable[] getVariables(final long offset, final int length) {
		if (fVariable.fValue != this) {
			return NO_VARIABLES;
		}
		final RVariable[] variables = new RVariable[length];
		for (int i = 0; i < length; i++) {
			final ICombinedRElement child = fElement.get(offset + i);
			variables[i] = new RElementVariable(child, fVariable.fFrame, fVariable.fStamp);
		}
		return variables;
	}
	
}
