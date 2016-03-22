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

package de.walware.statet.r.internal.debug.core.eval;

import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.ecommons.debug.core.eval.EvaluationWatchExpressionResult;
import de.walware.ecommons.debug.core.eval.IEvaluationListener;
import de.walware.ecommons.debug.core.eval.IEvaluationResult;

import de.walware.statet.r.debug.core.IRStackFrame;
import de.walware.statet.r.debug.core.IRThread;
import de.walware.statet.r.internal.debug.core.model.RStackFrame;


@NonNullByDefault
public class RWatchExpressionDelegate implements IWatchExpressionDelegate, IEvaluationListener {
	
	
	private IWatchExpressionListener listener;
	
	
	public RWatchExpressionDelegate() {
	}
	
	
	@Override
	public void evaluateExpression(final String expression, final IDebugElement context,
			final IWatchExpressionListener listener) {
		boolean force= false;
		final StackTraceElement[] stackTrace= Thread.currentThread().getStackTrace();
		final int end= Math.min(stackTrace.length, 8);
		for (int i= 2; i < end; i++) {
			final String className= stackTrace[i].getClassName();
			if (className.equals("org.eclipse.debug.internal.ui.viewers.update.DefaultWatchExpressionModelProxy")) { //$NON-NLS-1$
				break;
			}
			else if (className.equals("org.eclipse.debug.internal.ui.actions.expressions.ReevaluateWatchExpressionAction")) { //$NON-NLS-1$
				force= true;
			}
		}
		
		final RStackFrame frame= checkContext(context);
		if (frame == null) {
			listener.watchEvaluationFinished(null);
			return;
		}
		
		this.listener= listener;
		frame.getThread().evaluate(expression, frame, force, this);
	}
	
	
	private @Nullable RStackFrame checkContext(final IDebugElement context) {
		IRStackFrame frame= null;
		if (context instanceof IRStackFrame) {
			frame= (IRStackFrame) context;
		}
		else if (context instanceof IRThread) {
			frame= ((IRThread) context).getTopStackFrame();
		}
		if (frame == null) {
			return null;
		}
		return (RStackFrame) frame;
	}
	
	
	@Override
	public void evaluationFinished(final IEvaluationResult result) {
		if ((result.getStatus() < IEvaluationResult.SKIPPED)) {
			this.listener.watchEvaluationFinished(new EvaluationWatchExpressionResult(result));
		}
		else {
			result.free();
		}
	}
	
}
