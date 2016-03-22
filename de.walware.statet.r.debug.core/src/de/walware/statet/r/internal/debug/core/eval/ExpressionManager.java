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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.jcommons.collections.CopyOnWriteList;
import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.debug.core.eval.IEvaluationListener;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolService;

import de.walware.rj.services.FunctionCall;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.debug.core.IRStackFrame;
import de.walware.statet.r.internal.debug.core.Messages;
import de.walware.statet.r.internal.debug.core.RDebugCorePlugin;
import de.walware.statet.r.internal.debug.core.RJTmp;
import de.walware.statet.r.internal.debug.core.eval.REvalExpressionTask.Key;
import de.walware.statet.r.internal.debug.core.model.RDebugTarget;
import de.walware.statet.r.internal.debug.core.model.RMainThread;
import de.walware.statet.r.internal.debug.core.model.RStackFrame;
import de.walware.statet.r.nico.AbstractRDbgController;


@NonNullByDefault
public class ExpressionManager {
	
	
	private class CleanRunnable implements ISystemRunnable {
		
		
		private boolean scheduled;
		
		
		public CleanRunnable() {
		}
		
		
		@Override
		public String getTypeId() {
			return "r/dbg/exprs/clean"; //$NON-NLS-1$
		}
		
		@Override
		public String getLabel() {
			return Messages.Expression_Clean_task;
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return (tool == ExpressionManager.this.thread.getTool());
		}
		
		@Override
		public boolean changed(final int event, final ITool process) {
			switch (event) {
			case REMOVING_FROM:
			case MOVING_FROM:
				return false;
			case BEING_ABANDONED:
//			case FINISHING_: // handled in #loadContext
				break;
			default:
				break;
			}
			return true;
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			synchronized (this) {
				this.scheduled= false;
			}
			cleanEvalResults((AbstractRDbgController) service, monitor);
		}
		
	}
	
	
	private final Map<REvalExpressionTask.Key, @Nullable REvaluationResult> evalResults= new HashMap<>();
	private final List<@NonNull REvaluationResult> oldEvalResults= new ArrayList<>();
	
	private final CopyOnWriteList<@NonNull REvaluationExpression> expressions= new CopyOnWriteList<>();
	
	private final RMainThread thread;
	
	private final CleanRunnable cleanRunnable= new CleanRunnable();
	
	
	public ExpressionManager(final RMainThread thread) {
		this.thread= thread;
	}
	
	
	public void clearCache(final int stamp, final @Nullable IProgressMonitor monitor) {
		synchronized (this) {
			if (!this.evalResults.isEmpty()) {
				for (final Iterator<Entry<Key, @Nullable REvaluationResult>> iter= evalResults.entrySet().iterator(); iter.hasNext();) {
					final Entry<Key, @Nullable REvaluationResult> entry= iter.next();
					final REvaluationResult result= entry.getValue();
					if (result != null) {
						this.oldEvalResults.add(result);
						entry.setValue(null);
					}
					else {
						iter.remove();
					}
				}
			}
		}
		
		int cleanCounter= 0;
		for (final REvaluationResult result : this.oldEvalResults) {
			if (result.isLocked()) {
				if (stamp != 0) {
					result.reset(stamp, monitor);
				}
			}
			else {
				cleanCounter++;
			}
		}
		if (cleanCounter > 0) {
			scheduleClean();
		}
	}
	
	public void evaluate(final String expressionText, final IRStackFrame stackFrame,
			final boolean forceReevaluate, final IEvaluationListener listener) {
		if (!checkExpression(expressionText, listener)) {
			return;
		}
		
		final REvalExpressionTask task= new REvalExpressionTask(expressionText,
				(@NonNull RStackFrame) stackFrame );
		REvaluationResult result;
		final REvaluationResult prev;
		synchronized (this) {
			if (forceReevaluate) {
				result= null;
				this.evalResults.put(task.getKey(), null);
			}
			else {
				result= this.evalResults.get(task.getKey());
				if (result != null) {
					result.lock();
				}
			}
		}
		
		if (result == null) {
			final int stamp= this.thread.checkStackFrame(stackFrame);
			if (stamp != 0) {
				final RProcess tool= this.thread.getTool();
				final EvalExpressionRunnable runnable= new EvalExpressionRunnable(task, stamp,
						listener );
				synchronized (runnable) {
					if (tool.getQueue().add(runnable).isOK()) {
						// async
						return;
					}
				}
			}
			result= new REvaluationResult(expressionText, this.thread);
		}
		
		listener.evaluationFinished(result);
	}
	
	private boolean checkExpression(final String rExpression, final IEvaluationListener listener) {
		final RDebugTarget debugTarget= this.thread.getDebugTarget();
		final ExpressionValidator expressionValidator= debugTarget.getExpressionValidator();
		final String errorMessage;
		synchronized (expressionValidator) {
			errorMessage= expressionValidator.checkExpression(rExpression);
		}
		if (errorMessage != null) {
			listener.evaluationFinished(new REvaluationResult(rExpression, this.thread,
					IStatus.ERROR, errorMessage ));
			return false;
		}
		return true;
	}
	
	
	public void setEvalResult(final REvalExpressionTask.Key evalKey,
			final REvaluationResult result) {
		final REvaluationResult prev;
		synchronized (this) {
			prev= this.evalResults.put(evalKey, result);
		}
		if (prev != null) {
			prev.free();
		}
	}
	
	public @Nullable REvaluationResult getEvalResult(final REvalExpressionTask.Key evalKey) {
		synchronized (this) {
			final REvaluationResult result= this.evalResults.get(evalKey);
			if (result != null) {
				result.lock();
			}
			return result;
		}
	}
	
	
	public void scheduleClean() {
		synchronized (this.cleanRunnable) {
			if (this.cleanRunnable.scheduled) {
				return;
			}
			this.thread.getTool().getQueue().addHot(this.cleanRunnable);
			this.cleanRunnable.scheduled= true;
		}
	}
	
	private void cleanEvalResults(
			final AbstractRDbgController r, final IProgressMonitor monitor) {
		for (final Iterator<REvaluationResult> iter= this.oldEvalResults.iterator(); iter.hasNext(); ) {
			final REvaluationResult result= iter.next();
			if (result.isLocked()) {
				continue;
			}
			iter.remove();
			final @Nullable String tmpId= result.getTmpId();
			if (tmpId != null) {
				cleanEvalResult(tmpId, r, monitor);
			}
		}
	}
	
	public void cleanEvalResult(final String id,
			final AbstractRDbgController r, final IProgressMonitor monitor) {
		try {
			final FunctionCall call= r.createFunctionCall(RJTmp.REMOVE_ALL);
			call.addChar(RJTmp.ID_PAR, id);
			call.evalVoid(monitor);
		}
		catch (final CoreException e) {
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID,
					0, "An error occurred when cleaning up temporary objects.", e ));
		}
	}
	
	
	public void register(final REvaluationExpression expression) {
		this.expressions.add(expression);
	}
	
	public void unregister(final REvaluationExpression expression) {
		this.expressions.remove(expression);
	}
	
	public void updateExpressions(final List<@NonNull DebugEvent> eventCollection) {
		final ImList<@NonNull REvaluationExpression> list= this.expressions.toList();
		
		for (final REvaluationExpression expression : list) {
			eventCollection.add(new DebugEvent(expression, DebugEvent.CHANGE, DebugEvent.CONTENT));
		}
	}
	
	public void cleanExpressions(final List<@NonNull DebugEvent> eventCollection) {
		final ImList<@NonNull REvaluationExpression> list= this.expressions.clearToList();
		final IExpressionManager manager= DebugPlugin.getDefault().getExpressionManager();
		
		for (final REvaluationExpression expression : list) {
			manager.removeExpression(expression);
		}
	}
	
}
