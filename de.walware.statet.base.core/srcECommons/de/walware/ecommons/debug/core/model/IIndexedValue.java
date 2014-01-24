/*=============================================================================#
 # Copyright (c) 2013-2014 Stephan Wahlbrink (WalWare.de) and others.
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
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;


/**
 * A value containing an indexed collection of variables.
 * 
 * Like {@link org.eclipse.debug.core.model.IIndexedValue}, but supporting collections of long size.
 */
public interface IIndexedValue extends IValue {
	
	
	long getSize() throws DebugException;
	
	IVariable[] getVariables(long offset, int length);
	
}
