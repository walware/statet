/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
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

import de.walware.ecommons.debug.core.model.VariablePartition;
import de.walware.ecommons.debug.core.model.VariablePartitionFactory;

import de.walware.rj.data.RObject;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.debug.core.IRDebugTarget;
import de.walware.statet.r.debug.core.IRValue;
import de.walware.statet.r.debug.core.IRVariable;


@NonNullByDefault
public abstract class RElementValue<TRElement extends ICombinedRElement> extends RDebugElement
		implements IRValue {
	
	
	protected static final @NonNull IRVariable[] NO_VARIABLES= new @NonNull IRVariable[0];
	
	
	protected static class RVariablePartition extends VariablePartition<IRIndexElementValue>
			implements IRVariable, IRValue {
		
		
		public RVariablePartition(final IRIndexElementValue value,
				final VariablePartitionFactory<IRIndexElementValue>.PartitionHandle partition) {
			super(value, partition);
		}
		
		
		@Override
		public IRDebugTarget getDebugTarget() {
			return (IRDebugTarget) super.getDebugTarget();
		}
		
		@Override
		public @Nullable IRVariable getParent() {
			return null;
		}
		
		@Override
		protected int getNameIndexBase() {
			return 1;
		}
		
		@Override
		public IRValue getValue() {
			return this;
		}
		
		@Override
		public IRVariable getAssignedVariable() {
			return this;
		}
		
	}
	
	protected final static VariablePartitionFactory<IRIndexElementValue> PARTITION_FACTORY= new VariablePartitionFactory<IRIndexElementValue>() {
		
		@Override
		protected IRVariable createPartition(final IRIndexElementValue value,
				final VariablePartitionFactory<IRIndexElementValue>.PartitionHandle partition) {
			return new RVariablePartition(value, partition);
		}
		
	};
	
	
	protected final TRElement element;
	
	protected final int stamp;
	
	
	public RElementValue(final RDebugTarget debugTarget,
			final TRElement element, final int stamp) {
		super(debugTarget);
		this.element=  element;
		this.stamp= stamp;
	}
	
	
	public final TRElement getElement() {
		return this.element;
	}
	
	
	@Override
	public String getValueString() throws DebugException {
		switch (this.element.getElementType()) {
		case RObject.TYPE_NULL:
			return "NULL"; //$NON-NLS-1$
//		case RObject.TYPE_MISSING:
//			return "<missing>";
//		case RObject.TYPE_PROMISE:
//			return "<not yet evaluated>";
		}
		return ""; //$NON-NLS-1$
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
	public boolean hasVariables() throws DebugException {
		return false;
	}
	
	@Override
	public @NonNull IVariable[] getVariables() throws DebugException {
		return NO_VARIABLES;
	}
	
}
