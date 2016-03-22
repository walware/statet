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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.lang.ObjectUtils;
import de.walware.jcommons.lang.ObjectUtils.ToStringBuilder;

import de.walware.ecommons.debug.core.eval.IEvaluationListener;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;

import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.data.defaultImpl.RReferenceImpl;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.services.FunctionCall;
import de.walware.rj.services.RService;

import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.tool.AbstractStatetRRunnable;
import de.walware.statet.r.internal.debug.core.Messages;
import de.walware.statet.r.internal.debug.core.RDebugCorePlugin;
import de.walware.statet.r.internal.debug.core.RJTmp;
import de.walware.statet.r.internal.debug.core.model.RElementVariable;
import de.walware.statet.r.internal.debug.core.model.RMainThread;
import de.walware.statet.r.nico.AbstractRDbgController;


@NonNullByDefault
public class EvalExpressionRunnable extends AbstractStatetRRunnable implements ISystemRunnable {
	
	
	private final REvalExpressionTask task;
	
	private final int stamp;
	
	private final IEvaluationListener listener;
	
	private int state;
	
	
	public EvalExpressionRunnable(final REvalExpressionTask task, final int stamp,
			final IEvaluationListener listener) {
		super("r/dbg/watch", Messages.Expression_Evaluate_task);
		
		this.task= task;
		this.stamp= stamp;
		this.listener= listener;
	}
	
	
	@Override
	public boolean isRunnableIn(final ITool tool) {
		return (tool == this.task.getThread().getTool());
	}
	
	@Override
	public boolean changed(final int event, final ITool tool) {
		switch (event) {
		case REMOVING_FROM:
		case MOVING_FROM:
			return false;
		case BEING_ABANDONED:
		case FINISHING_OK:
		case FINISHING_ERROR:
		case FINISHING_CANCEL:
			synchronized (this) {
				if (this.state == 0) {
					this.state= event;
					this.listener.evaluationFinished(new REvaluationResult(
							this.task.getRExpression(), this.task.getThread()) );
				}
			}
			break;
		default:
			break;
		}
		
		return true;
	}
	
	@Override
	protected void run(final IRToolService service,
			final IProgressMonitor monitor) throws CoreException {
		try {
			final AbstractRDbgController r= (AbstractRDbgController) service;
			
			if (this.stamp != r.getChangeStamp()) {
				return;
			}
			
			final RMainThread thread= this.task.getThread();
			final @Nullable REvaluationResult result= thread.getExpressionManager()
					.getEvalResult(this.task.getKey());
			if (result != null) {
				this.state= FINISHING_OK;
				this.listener.evaluationFinished(result);
				return;
			}
			
			final @NonNull String id;
			{	final FunctionCall call= r.createFunctionCall(RJTmp.CREATE_ID);
				call.addChar(RJTmp.PREFIX_PAR, "dbg_watch"); //$NON-NLS-1$
				id= RDataUtil.checkSingleCharValue(call.evalData(monitor));
			}
			try {
				evalExpression(id, r, monitor);
			}
			finally {
				if (this.state != FINISHING_OK) {
					thread.getExpressionManager().cleanEvalResult(id, r, monitor);
				}
			}
		}
		catch (final CoreException | UnexpectedRDataException e) {
			this.state= FINISHING_ERROR;
			final ToStringBuilder sb= new ObjectUtils.ToStringBuilder(
					"An error occurred when evaluating watch expression." );
			sb.addProp("expression", this.task.getRExpression()); //$NON-NLS-1$
			sb.addProp("frame", this.task.getStackFrame()); //$NON-NLS-1$
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
					sb.toString(), e ));
		}
	}
	
	private void evalExpression(final String id, final AbstractRDbgController r,
			final IProgressMonitor monitor) throws UnexpectedRDataException, CoreException, DebugException {
		final RMainThread thread= this.task.getThread();
		
		final String valueName= id + ".value"; //$NON-NLS-1$
		final RElementName valueElementName= RElementName.create(ImCollections.newList(
				RJTmp.PKG_NAME, RJTmp.ENV_NAME,
				RElementName.create(RElementName.MAIN_DEFAULT, valueName) ));
		
//		System.out.println(this.task.getRExpression() + " in " + this.task.getStackFrame());
		
		ICombinedRElement element;
		final RReference ref;
		try {
			element= r.evalCombinedStruct(
					this.task.getRExpression(),
					new RReferenceImpl(this.task.getStackFrame().getHandle(), RObject.TYPE_ENV, null),
					0, RService.DEPTH_REFERENCE, valueElementName, monitor );
			ref= RDataUtil.checkRReference(element);
		}
		catch (final CoreException e) {
			synchronized (this) {
				this.state= FINISHING_ERROR;
				final IStatus status= e.getStatus();
				if (status.getSeverity() == IStatus.CANCEL) {
					evalCompleted(new REvaluationResult(this.task.getRExpression(), thread,
							IStatus.CANCEL, Messages.Expression_Evaluate_Cancelled_message ));
				}
				else {
					evalCompleted(new REvaluationResult(this.task.getRExpression(), thread,
							IStatus.ERROR, status.getMessage() ));
				}
				return;
			}
		}
		
		{	final FunctionCall call= r.createFunctionCall(RJTmp.SET);
			call.addChar(RJTmp.NAME_PAR, valueName);
			call.add(RJTmp.VALUE_PAR, ref);
			call.evalVoid(monitor);
		}
		
		if (ref.getReferencedRObjectType() == RObject.TYPE_ENV) {
			thread.resolveReference((ICombinedRElement) ref, this.stamp, monitor);
		}
		else {
			element= r.getWorkspaceData().resolve(ref, RWorkspace.RESOLVE_UPTODATE, 0, monitor);
			if (element == null) {
				element= r.evalCombinedStruct(valueElementName, 0, RService.DEPTH_INFINITE,
						monitor );
				if (element == null) {
					throw new UnexpectedRDataException("null");
				}
			}
		}
		
		final RElementVariable variable= new RElementVariable(element, thread, this.stamp, null);
		variable.getValue(monitor);
		this.state= FINISHING_OK;
		evalCompleted(new REvaluationResult(this.task.getRExpression(), thread,
				variable, id ));
	}
	
	private void evalCompleted(final REvaluationResult evalResult) {
		this.task.getThread().getExpressionManager().setEvalResult(this.task.getKey(), evalResult);
		this.listener.evaluationFinished(evalResult);
	}
	
}
