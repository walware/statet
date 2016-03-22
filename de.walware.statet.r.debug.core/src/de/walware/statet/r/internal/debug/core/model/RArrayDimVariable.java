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
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.ecommons.debug.core.model.IVariableDim;
import de.walware.ecommons.debug.core.model.VariablePartitionFactory;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.debug.core.IRValue;
import de.walware.statet.r.debug.core.IRVariable;


@NonNullByDefault
public class RArrayDimVariable extends RVariable implements IVariableDim, IRIndexValueInternal {
	
	
	private final RArrayValue mainValue;
	
	private final int[] dimIndex;
	
	
	public RArrayDimVariable(final RArrayValue value, final int[] selected,
			final IRVariable parent) {
		super(value.getDebugTarget(), parent);
		this.mainValue= value;
		this.dimIndex= selected;
	}
	
	
	@Override
	public ICombinedRElement getElement() {
		return this.mainValue.element;
	}
	
	@Override
	public String getName() {
		final StringBuilder sb= new StringBuilder();
		final int m= this.mainValue.dimCount - this.dimIndex.length;
		{	sb.append("[ "); //$NON-NLS-1$
			for (int i= 0; i < m; i++) {
				sb.append(", "); //$NON-NLS-1$
			}
			for (int i= m; i < this.mainValue.dimCount - 1; i++) {
				sb.append(this.dimIndex[i - m] + 1);
				sb.append(", "); //$NON-NLS-1$
			}
			sb.append(this.dimIndex[this.dimIndex.length - 1] + 1);
			sb.append(']');
		}
		{	final String name= this.mainValue.getDimItemName(m, this.dimIndex[0]);
			if (name != null) {
				sb.append(' ');
				sb.append(name);
			}
		}
		return sb.toString();
	}
	
	@Override
	public boolean hasValueChanged() throws DebugException {
		return false;
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
		return ""; //$NON-NLS-1$
	}
	
	@Override
	public String getValueString() throws DebugException {
		final StringBuilder sb= new StringBuilder();
		final int m= this.mainValue.dimCount - this.dimIndex.length;
		sb.append('[');
		sb.append(this.mainValue.dim.getInt(0));
		for (int i= 1; i < m; i++) {
			sb.append('×');
			sb.append(this.mainValue.dim.getInt(i));
		}
		sb.append(']');
		
		{	final String dimName= this.mainValue.getDimName(m - 1);
			if (dimName != null) {
				sb.append(" / "); //$NON-NLS-1$
				sb.append(dimName);
			}
		}
		
		return sb.toString();
	}
	
	@Override
	public boolean supportsValueModification() {
		return false;
	}
	
	@Override
	public boolean verifyValue(final String expression) throws DebugException {
		throw newNotSupported();
	}
	
	@Override
	public boolean hasVariables() throws DebugException {
		return (this.mainValue.dim.getInt(this.mainValue.dimCount - this.dimIndex.length) > 0);
	}
	
	@Override
	public IVariable[] getVariables() throws DebugException {
		return getPartitionFactory().getVariables(this);
	}
	
	
	@Override
	public final VariablePartitionFactory<IRIndexElementValue> getPartitionFactory() {
		return RElementValue.PARTITION_FACTORY;
	}
	
	@Override
	public long getSize() throws DebugException {
		return this.mainValue.dim.getInt(this.mainValue.dimCount - this.dimIndex.length - 1);
	}
	
	@Override
	public @NonNull IRVariable[] getVariables(final long offset, final int length) {
		return getVariables(offset, length, this);
	}
	
	@Override
	public @NonNull IRVariable[] getVariables(final long offset, final int length, final IRVariable parent) {
		{	final int n= this.mainValue.dim.getInt(this.mainValue.dimCount - this.dimIndex.length - 1);
			if (n <= 0) {
				throw new UnsupportedOperationException();
			}
			if (offset < 0 || length < 0 || offset > n - length) {
				throw new IllegalArgumentException();
			}
		}
		final int o= (int) offset;
		final @NonNull IRVariable[] variables= new @NonNull IRVariable[length];
		if (this.dimIndex.length == this.mainValue.dimCount - 1) {
			for (int i= 0; i < length; i++) {
				final int[] d= new int[this.dimIndex.length + 1];
				System.arraycopy(this.dimIndex, 0, d, 1, this.dimIndex.length);
				d[0]= o + i;
				variables[i]= new RArrayIndexVariable(this.mainValue, d, parent);
			}
		}
		else {
			for (int i= 0; i < length; i++) {
				final int[] d= new int[this.dimIndex.length + 1];
				System.arraycopy(this.dimIndex, 0, d, 1, this.dimIndex.length);
				d[0]= o + i;
				variables[i]= new RArrayDimVariable(this.mainValue, d, parent);
			}
		}
		return variables;
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
		if (obj instanceof RArrayDimVariable) {
			final RArrayDimVariable other= (RArrayDimVariable) obj;
			return (this.mainValue.equals(other.mainValue)
					&& Arrays.equals(this.dimIndex, other.dimIndex) );
		}
		return false;
	}
	
}
