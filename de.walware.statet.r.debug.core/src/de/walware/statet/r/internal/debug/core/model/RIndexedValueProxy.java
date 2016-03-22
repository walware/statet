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

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.debug.core.IRVariable;


@NonNullByDefault
public class RIndexedValueProxy extends RValueProxy implements IRIndexElementValue {
	
	
	public RIndexedValueProxy(final IRIndexValueInternal value, final IRVariable variable) {
		super(value, variable);
	}
	
	
	@Override
	public ICombinedRElement getElement() {
		final IRIndexValueInternal indexed= (IRIndexValueInternal) this.value;
		return indexed.getElement();
	}
	
	@Override
	public final @NonNull IVariable[] getVariables() throws DebugException {
		final IRIndexValueInternal indexed= (IRIndexValueInternal) this.value;
		return indexed.getPartitionFactory().getVariables(this);
	}
	
	
	@Override
	public final long getSize() throws DebugException {
		final IRIndexValueInternal indexed= (IRIndexValueInternal) this.value;
		return indexed.getSize();
	}
	
	@Override
	public final @NonNull IRVariable[] getVariables(final long offset, final int length) {
		final IRIndexValueInternal indexed= (IRIndexValueInternal) this.value;
		return indexed.getVariables(offset, length, this.variable);
	}
	
}
