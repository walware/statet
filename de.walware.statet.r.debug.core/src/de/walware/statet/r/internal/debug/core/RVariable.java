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

package de.walware.statet.r.internal.debug.core;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

import de.walware.statet.r.debug.core.IRDebugTarget;


public abstract class RVariable extends RDebugElement implements IVariable {
	
	
	public RVariable(final IRDebugTarget target) {
		super(target);
	}
	
	
	@Override
	public boolean hasValueChanged() throws DebugException {
		return false;
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
