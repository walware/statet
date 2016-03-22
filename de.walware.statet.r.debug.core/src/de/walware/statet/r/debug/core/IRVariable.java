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
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;


public interface IRVariable extends IVariable {
	
	
	@Override
	@NonNull IRDebugTarget getDebugTarget();
	
	@Nullable IRVariable getParent();
	
	@Override
	@NonNull String getName();
	
	@Override
	@NonNull String getReferenceTypeName() throws DebugException;
	
	
	@Override
	@NonNull IRValue getValue() throws DebugException;
	
	@Override
	boolean supportsValueModification();
	
	@Override
	boolean verifyValue(@NonNull String expression) throws DebugException;
	
	@Override
	void setValue(@NonNull String expression) throws DebugException;
	
	
	@Override
	<T> @Nullable T getAdapter(final Class<T> type);
	
}
