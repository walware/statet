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
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.statet.r.debug.core.IRDebugTarget;
import de.walware.statet.r.debug.core.IRElementVariable;
import de.walware.statet.r.debug.core.IRValue;
import de.walware.statet.r.debug.core.IRVariable;


@NonNullByDefault
public class RVariableProxy implements IRVariable {
	
	
	public static RVariableProxy create(final IRVariable variable, final IRVariable parent) {
		if (variable instanceof IRElementVariable) {
			return new RElementVariableProxy((IRElementVariable) variable, parent);
		}
		return new RVariableProxy(variable, parent);
	}
	
	public static @Nullable Object unproxy(@Nullable final Object obj) {
		if (obj instanceof RVariableProxy) {
			return ((RVariableProxy) obj).variable;
		}
		return obj;
	}
	
	
	protected final IRVariable variable;
	
	protected final IRVariable parent;
	
	
	public RVariableProxy(final IRVariable variable, final IRVariable parent) {
		if (variable instanceof RVariableProxy) {
			System.out.println("XXX Variable");
		}
		this.variable= variable;
		this.parent= parent;
	}
	
	
	@Override
	public final String getModelIdentifier() {
		return this.variable.getModelIdentifier();
	}
	
	@Override
	public final IRDebugTarget getDebugTarget() {
		return this.variable.getDebugTarget();
	}
	
	@Override
	public final ILaunch getLaunch() {
		return this.variable.getLaunch();
	}
	
	@Override
	public final @Nullable IRVariable getParent() {
		return this.parent;
	}
	
	
	@Override
	public final String getName() {
		return this.variable.getName();
	}
	
	@Override
	public final String getReferenceTypeName() throws DebugException {
		return this.variable.getReferenceTypeName();
	}
	
	@Override
	public final boolean hasValueChanged() throws DebugException {
		return this.variable.hasValueChanged();
	}
	
	@Override
	public final IRValue getValue() throws DebugException {
		return RValueProxy.create(this.variable.getValue(), this);
	}
	
	@Override
	public final boolean supportsValueModification() {
		return this.variable.supportsValueModification();
	}
	
	@Override
	public final boolean verifyValue(final String expression) throws DebugException {
		return this.variable.verifyValue(expression);
	}
	
	@Override
	public final boolean verifyValue(final IValue value) throws DebugException {
		return this.variable.verifyValue(value);
	}
	
	@Override
	public final void setValue(final String expression) throws DebugException {
		this.variable.setValue(expression);
	}
	
	@Override
	public final void setValue(final IValue value) throws DebugException {
		this.variable.setValue(value);
	}
	
	
	@Override
	public <T> @Nullable T getAdapter(final Class<T> type) {
		return this.variable.getAdapter(type);
	}
	
	
	@Override
	public final int hashCode() {
		return this.variable.hashCode();
	}
	
	@Override
	public final boolean equals(final @Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		return this.variable.equals(obj);
	}
	
	@Override
	public final String toString() {
		return this.variable.toString();
	}
	
}
