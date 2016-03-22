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
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.statet.r.debug.core.IRVariable;


@NonNullByDefault
public abstract class RVariable extends RDebugElement implements IRVariable {
	
	
	private @Nullable IRVariable parent;
	
	
	public RVariable(final RDebugTarget target, final @Nullable IRVariable parent) {
		super(target);
		
		this.parent= parent;
	}
	
	
	@Override
	public final @Nullable IRVariable getParent() {
		return this.parent;
	}
	
	void setParent(final IRVariable parent) {
		assert (this.parent == null);
		this.parent= parent;
	}
	
	
	@Override
	public boolean verifyValue(final IValue value) throws DebugException {
		throw newNotSupported();
	}
	
	@Override
	public void setValue(final String expression) throws DebugException {
		throw newNotSupported();
	}
	
	@Override
	public void setValue(final IValue value) throws DebugException {
		throw newNotSupported();
	}
	
}
