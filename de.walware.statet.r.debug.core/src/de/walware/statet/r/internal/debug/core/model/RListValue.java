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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.ecommons.debug.core.model.VariablePartitionFactory;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RList;

import de.walware.statet.r.console.core.RWorkspace.ICombinedRList;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.debug.core.IRVariable;


@NonNullByDefault
public class RListValue extends RElementVariableValue<ICombinedRList> implements IRIndexValueInternal {
	
	
	protected static class RListPartition extends RVariablePartition {
		
		
		public RListPartition(final IRIndexElementValue value,
				final VariablePartitionFactory<IRIndexElementValue>.PartitionHandle partition) {
			super(value, partition);
		}
		
		
		@Override
		protected @Nullable String getNameIndexLabel(final long idx) {
			return ((RList) this.value.getElement()).getName(idx);
		}
		
	}
	
	protected static final VariablePartitionFactory<IRIndexElementValue> LIST_PARTITION_FACTORY= 
			new VariablePartitionFactory<IRIndexElementValue>() {
		
		@Override
		protected IRVariable createPartition(final IRIndexElementValue value, final PartitionHandle partition) {
			return new RListPartition(value, partition);
		}
		
	};
	
	protected static class ByName extends RListValue {
		
		public ByName(final RElementVariable variable) {
			super(variable);
		}
		
		
		@Override
		protected @Nullable RElementVariable checkPreviousVariable(final RListValue previousValue,
				final long idx, final ICombinedRElement element) {
			final RCharacterStore names= previousValue.element.getNames();
			if (names != null) {
				return super.checkPreviousVariable(previousValue,
						names.indexOf(this.element.getName(idx)), element);
			}
			return null;
		}
		
	}
	
	
	private final RElementVariableStore childVariables;
	
	
	public RListValue(final RElementVariable variable) {
		super(variable);
		
		this.childVariables= new RElementVariableStore(this.element.getLength());
	}
	
	
	public final @Nullable RListValue getVariablePreviousValue() {
		return (RListValue) this.variable.getPreviousValue();
	}
	
	
	@Override
	public String getReferenceTypeName() throws DebugException {
		return this.element.getRClassName();
	}
	
	@Override
	public boolean isAllocated() throws DebugException {
		return true;
	}
	
	@Override
	public String getValueString() throws DebugException {
		final StringBuilder sb= new StringBuilder();
		sb.append('[');
		sb.append(this.element.getLength());
		sb.append(']');
		return sb.toString();
	}
	
	@Override
	public boolean hasVariables() throws DebugException {
		return this.element.hasModelChildren(null);
	}
	
	@Override
	public @NonNull IVariable[] getVariables() throws DebugException {
		return getPartitionFactory().getVariables(this);
	}
	
	
	@Override
	public final VariablePartitionFactory<IRIndexElementValue> getPartitionFactory() {
		return LIST_PARTITION_FACTORY;
	}
	
	@Override
	public long getSize() throws DebugException {
		return this.element.getLength();
	}
	
	@Override
	public @NonNull IRVariable[] getVariables(final long offset, final int length) {
		return getVariables(offset, length, this.variable);
	}
	
	@Override
	public @NonNull IRVariable[] getVariables(final long offset, final int length, final IRVariable parent) {
		final RListValue previousValue;
		synchronized (this.variable) {
			if (this != this.variable.getCurrentValue()) {
				return NO_VARIABLES;
			}
			previousValue= getVariablePreviousValue();
		}
		synchronized (this.childVariables) {
			final @NonNull IRVariable[] variables= new @NonNull IRVariable[length];
			final boolean direct= (parent == this.variable);
			for (int i= 0; i < length; i++) {
				final long idx= offset + i;
				RElementVariable childVariable= this.childVariables.get(idx);
				if (childVariable == null) {
					final ICombinedRElement childElement= this.element.get(idx);
					if (previousValue != null) {
						childVariable= checkPreviousVariable(previousValue, idx, childElement);
					}
					if (childVariable == null) {
						childVariable= new RElementVariable(childElement,
								this.variable.getThread(), this.stamp, this.variable );
					}
					this.childVariables.set(idx, childVariable);
				}
				variables[i]= (direct) ? childVariable : RVariableProxy.create(childVariable, parent);
			}
			return variables;
		}
	}
	
	protected @Nullable RElementVariable checkPreviousVariable(final RListValue previousValue,
			final long idx, final ICombinedRElement element) {
		if (idx >= 0 && idx < previousValue.element.getLength()) {
			final RElementVariable previousVariable;
			synchronized (previousValue) {
				previousVariable= previousValue.childVariables.clear(idx);
			}
			if (previousVariable != null
					&& previousVariable.update(element, this.stamp) ) {
				return previousVariable;
			}
		}
		return null;
	}
	
}
