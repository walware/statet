/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.core;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

import de.walware.rj.data.RObject;


public class RValue extends RDebugElement implements IValue {
	
	
	protected final static RVariable[] NO_VARIABLES = new RVariable[0];
	
	protected static String subVector(final int offset, final int length) {
		final StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(offset+1);
		sb.append("L:"); //$NON-NLS-1$
		sb.append(offset+length);
		sb.append("L]"); //$NON-NLS-1$
		return sb.toString();
	}
	
	protected static String subList(final int offset) {
		final StringBuilder sb = new StringBuilder();
		sb.append("[["); //$NON-NLS-1$
		sb.append(offset+1);
		sb.append("L]]"); //$NON-NLS-1$
		return sb.toString();
	}
	
	
	protected final RElementVariable fVariable;
	
	
	public RValue(final RElementVariable variable) {
		super(variable.getDebugTarget());
		fVariable = variable;
	}
	
	
	@Override
	public String getValueString() throws DebugException {
		switch (fVariable.fElement.getElementType()) {
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
		return fVariable.fElement.getRClassName();
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
	public IVariable[] getVariables() throws DebugException {
		return NO_VARIABLES;
	}
	
}
