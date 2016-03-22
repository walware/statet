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

package de.walware.statet.r.debug.core;

import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.ecommons.debug.core.eval.IEvaluationListener;


/**
 * Represents an R thread in the Eclipse debug model for R.
 */
public interface IRThread extends IThread {
	
	
	@Override
	@NonNull IRDebugTarget getDebugTarget();
	
	@Override
	@Nullable IRStackFrame getTopStackFrame();
	
	void evaluate(@NonNull String expressionText, @NonNull IRStackFrame stackFrame,
			boolean forceReevaluate, @NonNull IEvaluationListener listener);
	
	
	@Override
	<T> @Nullable T getAdapter(final Class<T> type);
	
}
