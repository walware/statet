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

package de.walware.ecommons.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;


/**
 * Supports long collections and avoids widows.
 */
public class VariablePartition<T extends IIndexedValue> extends DebugElement implements IIndexedVariablePartition, IValue {
	
	
	protected final T value;
	
	private final VariablePartitionFactory<T>.PartitionHandle partition;
	
	
	public VariablePartition(final T value, final VariablePartitionFactory<T>.PartitionHandle partition) {
		super(value.getDebugTarget());
		this.value = value;
		this.partition = partition;
	}
	
	
	@Override
	public String getModelIdentifier() {
		return this.value.getModelIdentifier();
	}
	
	
	@Override
	public final long getPartitionStart() {
		return this.partition.getStart();
	}
	
	@Override
	public final long getPartitionLength() {
		return this.partition.getLength();
	}
	
	protected int getNameIndexBase() {
		return 0;
	}
	
	protected String getNameIndexLabel(final long idx) {
		return null;
	}
	
	
	@Override
	public String getName() throws DebugException {
		final StringBuilder sb = new StringBuilder();
		{	final long startIndex = getPartitionStart() + getNameIndexBase();
			sb.append('[');
			sb.append(startIndex);
			sb.append(" ... "); //$NON-NLS-1$
			sb.append(startIndex + (getPartitionLength() - 1));
			sb.append(']');
		}
		{	String label = getNameIndexLabel(getPartitionStart());
			if (label != null) {
				sb.append("  "); //$NON-NLS-1$
				sb.append(label);
				sb.append(" ... "); //$NON-NLS-1$
				label = getNameIndexLabel(getPartitionStart() + (getPartitionLength() - 1));
				if (label != null) {
					sb.append(label);
				}
			}
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
	public boolean hasValueChanged() throws DebugException {
		return false;
	}
	
	@Override
	public String getValueString() throws DebugException {
		return ""; //$NON-NLS-1$
	}
	
	@Override
	public boolean hasVariables() throws DebugException {
		return true;
	}
	
	@Override
	public IVariable[] getVariables() throws DebugException {
		try {
			return this.partition.getElements(this.value);
		}
		catch (final UnsupportedOperationException e) {
			throw VariablePartitionFactory.newNotSupported(e);
		}
		catch (final IllegalArgumentException e) {
			throw VariablePartitionFactory.newRequestInvalidFailed(e);
		}
	}
	
	
	@Override
	public boolean supportsValueModification() {
		return false;
	}
	
	@Override
	public boolean verifyValue(final String expression) throws DebugException {
		throw VariablePartitionFactory.newNotSupported();
	}
	
	@Override
	public void setValue(final String expression) throws DebugException {
		throw VariablePartitionFactory.newNotSupported();
	}
	
	@Override
	public boolean verifyValue(final IValue value) throws DebugException {
		throw VariablePartitionFactory.newNotSupported();
	}
	
	@Override
	public void setValue(final IValue value) throws DebugException {
		throw VariablePartitionFactory.newNotSupported();
	}
	
	
	@Override
	public int hashCode() {
		return this.value.hashCode() + this.partition.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof VariablePartition)) {
			return false;
		}
		final VariablePartition<?> other = (VariablePartition<?>) obj;
		return (this.value.equals(other.value) && this.partition.equals(other.partition));
	}
	
	@Override
	public Object getAdapter(final Class required) {
//		final Object adapter = fValue.getAdapter(required);
//		if (adapter != null) {
//			return adapter;
//		}
		return super.getAdapter(required);
	}
	
}
