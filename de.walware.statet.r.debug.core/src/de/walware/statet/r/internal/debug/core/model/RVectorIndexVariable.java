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

package de.walware.statet.r.internal.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.rj.data.RDataUtil;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.debug.core.IRIndexedVariableItem;
import de.walware.statet.r.debug.core.IRValue;
import de.walware.statet.r.debug.core.IRVariable;


@NonNullByDefault
public class RVectorIndexVariable extends RVariable implements IRIndexedVariableItem, IRValue {
	
	
	private final RVectorValue mainValue;
	
	private final long index;
	
	
	public RVectorIndexVariable(final RVectorValue value, final long index,
			final IRVariable parent) {
		super(value.getDebugTarget(), parent);
		this.mainValue= value;
		this.index= index;
	}
	
	
	@Override
	public long[] getIndex() {
		return new long[] { this.index };
	}
	
	@Override
	public String getName() {
		final StringBuilder sb= new StringBuilder();
		sb.append('[');
		sb.append(this.index + 1);
		sb.append(']');
		final String name= this.mainValue.getName(this.index);
		if (name != null) {
			sb.append(' ');
			sb.append(' ');
			sb.append(name);
		}
		return sb.toString();
	}
	
	@Override
	public boolean hasValueChanged() throws DebugException {
		return this.mainValue.hasValueChanged(this.index);
	}
	
	@Override
	public IRValue getValue() throws DebugException {
		return this;
	}
	
	@Override
	public IRVariable getAssignedVariable() {
		return this;
	}
	
	@Override
	public boolean isAllocated() throws DebugException {
		return true;
	}
	
	@Override
	public String getReferenceTypeName() throws DebugException {
		final ICombinedRElement element= this.mainValue.element;
		return RDataUtil.getStoreAbbr(element.getData());
	}
	
	@Override
	public String getValueString() throws DebugException {
		final String data= this.mainValue.getData(this.index);
		if (data == null) {
			throw newRequestLoadDataFailed();
		}
		return data;
	}
	
	@Override
	public boolean supportsValueModification() {
		return true;
	}
	
	@Override
	public boolean verifyValue(final String expression) throws DebugException {
		return this.mainValue.validateDataValue(expression);
	}
	
	@Override
	public void setValue(final String expression) throws DebugException {
		this.mainValue.setDataValue(this.index, expression);
	}
	
	@Override
	public boolean hasVariables() throws DebugException {
		return false;
	}
	
	@Override
	public IVariable[] getVariables() throws DebugException {
		return RElementVariableValue.NO_VARIABLES;
	}
	
	
	@Override
	public int hashCode() {
		return this.mainValue.hashCode() + (int) (this.index ^ (this.index >>> 32));
	}
	
	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		obj= RVariableProxy.unproxy(obj);
		if (obj instanceof RVectorIndexVariable) {
			final RVectorIndexVariable other= (RVectorIndexVariable) obj;
			return (this.mainValue.equals(other.mainValue)
					&& this.index == other.index );
		}
		return false;
	}
	
}
