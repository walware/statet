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

import java.util.Arrays;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.rj.data.RDataUtil;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.debug.core.IRIndexedVariableItem;
import de.walware.statet.r.debug.core.IRValue;
import de.walware.statet.r.debug.core.IRVariable;


@NonNullByDefault
public class RArrayIndexVariable extends RVariable implements IRIndexedVariableItem, IRValue {
	
	
	private final RArrayValue mainValue;
	
	private final int[] dimIndex;
	
	
	public RArrayIndexVariable(final RArrayValue value, final int[] index,
			final IRVariable parent) {
		super(value.getDebugTarget(), parent);
		this.mainValue= value;
		this.dimIndex= index;
	}
	
	
	@Override
	public long[] getIndex() {
		final long[] index= new long[this.dimIndex.length];
		for (int i= 0; i < index.length; i++) {
			index[i]= this.dimIndex[i];
		}
		return index;
	}
	
	@Override
	public String getName() {
		final StringBuilder sb= new StringBuilder();
		{	sb.append('[');
			for (int i= 0; i < this.mainValue.dimCount - 1; i++) {
				sb.append(this.dimIndex[i] + 1);
				sb.append(", "); //$NON-NLS-1$
			}
			sb.append(this.dimIndex[this.mainValue.dimCount - 1] + 1);
			sb.append(']');
		}
		{	final String name= this.mainValue.getDimItemName(0, this.dimIndex[0]);
			if (name != null) {
				sb.append(' ');
				sb.append(name);
			}
		}
		return sb.toString();
	}
	
	
	@Override
	public boolean hasValueChanged() throws DebugException {
		return this.mainValue.hasValueChanged(this.dimIndex);
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
		final String data= this.mainValue.getData(this.dimIndex);
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
		this.mainValue.setDataValue(this.dimIndex, expression);
	}
	
	@Override
	public boolean hasVariables() throws DebugException {
		return false;
	}
	
	@Override
	public @NonNull IRVariable[] getVariables() throws DebugException {
		return RElementVariableValue.NO_VARIABLES;
	}
	
	
	@Override
	public int hashCode() {
		return this.mainValue.hashCode() + Arrays.hashCode(this.dimIndex);
	}
	
	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		obj= RVariableProxy.unproxy(obj);
		if (obj instanceof RArrayIndexVariable) {
			final RArrayIndexVariable other= (RArrayIndexVariable) obj;
			return (this.mainValue.equals(other.mainValue)
					&& Arrays.equals(this.dimIndex, other.dimIndex) );
		}
		return false;
	}
	
}
