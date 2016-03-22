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
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.statet.r.debug.core.IRDebugTarget;
import de.walware.statet.r.debug.core.IRValue;
import de.walware.statet.r.debug.core.IRVariable;


@NonNullByDefault
public class RValueProxy implements IRValue {
	
	
	public static RValueProxy create(IRValue value, final IRVariable variable) {
		if (value instanceof RValueProxy) {
			value= ((RValueProxy) value).value;
		}
		if (value instanceof IRIndexValueInternal) {
			return new RIndexedValueProxy((IRIndexValueInternal) value, variable);
		}
		return new RValueProxy(value, variable);
	}
	
	
	protected final IRValue value;
	
	protected final IRVariable variable;
	
	
	public RValueProxy(final IRValue value, final IRVariable variable) {
		this.value= value;
		this.variable= variable;
	}
	
	
	@Override
	public IRVariable getAssignedVariable() {
		return this.variable;
	}
	
	@Override
	public final String getModelIdentifier() {
		return this.value.getModelIdentifier();
	}
	
	@Override
	public final IRDebugTarget getDebugTarget() {
		return this.value.getDebugTarget();
	}
	
	@Override
	public final ILaunch getLaunch() {
		return this.value.getLaunch();
	}
	
	
	@Override
	public final String getReferenceTypeName() throws DebugException {
		return this.value.getReferenceTypeName();
	}
	
	@Override
	public final String getValueString() throws DebugException {
		if (this.value instanceof REnvValue) {
			return ((REnvValue) this.value).getValueString(this.variable);
		}
		return this.value.getValueString();
	}
	
	@Override
	public final boolean isAllocated() throws DebugException {
		return this.value.isAllocated();
	}
	
	@Override
	public final boolean hasVariables() throws DebugException {
		return this.value.hasVariables();
	}
	
	@Override
	public @NonNull IVariable[] getVariables() throws DebugException {
		final IVariable[] orgVariables= this.value.getVariables();
		if (orgVariables.length == 0) {
			return RElementVariableValue.NO_VARIABLES;
		}
		final @NonNull IVariable[] proxyVariables= new IVariable[orgVariables.length];
		for (int i= 0; i < orgVariables.length; i++) {
			proxyVariables[i]= RVariableProxy.create((IRVariable) orgVariables[i], this.variable);
		}
		return proxyVariables;
//		return orgVariables;
	}
	
	
	@Override
	public <T> @Nullable T getAdapter(final Class<T> type) {
		return this.value.getAdapter(type);
	}
	
	
	@Override
	public final int hashCode() {
		return this.value.hashCode();
	}
	
//	@Override
//	public final boolean equals(final Object obj) {
//		if (this == obj) {
//			return true;
//		}
//		return this.value.equals(obj);
//	}
	
	@Override
	public final String toString() {
		return this.value.toString();
	}
	
}
