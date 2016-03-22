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

package de.walware.statet.r.debug.core;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.annotation.NonNull;


public interface IRValue extends IValue {
	
	
	@Override
	@NonNull IRDebugTarget getDebugTarget();
	
	@NonNull IRVariable getAssignedVariable();
	
	@Override
	@NonNull String getValueString() throws DebugException;
	
}
