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

import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolService;

import de.walware.rj.data.RObject;
import de.walware.rj.server.dbg.CallStack;
import de.walware.rj.server.dbg.Frame;
import de.walware.rj.server.dbg.FrameContext;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.debug.core.IRStackFrame;
import de.walware.statet.r.debug.core.breakpoints.IRBreakpointStatus;
import de.walware.statet.r.internal.debug.core.Messages;
import de.walware.statet.r.internal.debug.core.RDebugCorePlugin;
import de.walware.statet.r.nico.AbstractRDbgController;


public class RStackFrame extends RDebugElement implements IRStackFrame {
	
	
	public static interface PositionResolver {
		
		int getLineNumber();
		
		int getCharStart();
		
		int getCharEnd();
		
	}
	
	
	private class LoadContextRunnable implements ISystemRunnable {
		
		
		private boolean cancel;
		
		
		public LoadContextRunnable() {
		}
		
		
		@Override
		public String getTypeId() {
			return "r/dbg/stackframe"; //$NON-NLS-1$
		}
		
		@Override
		public String getLabel() {
			return Messages.DebugContext_UpdateStackFrame_task;
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return (tool == RStackFrame.this.thread.getTool());
		}
		
		@Override
		public boolean changed(final int event, final ITool process) {
			switch (event) {
			case REMOVING_FROM:
				return this.cancel;
			case MOVING_FROM:
				return false;
			case BEING_ABANDONED:
//			case FINISHING_: // handled in #loadContext
				RStackFrame.this.lock.writeLock().lock();
				try {
					RStackFrame.this.contextCondition.signalAll();
				}
				finally {
					RStackFrame.this.lock.writeLock().unlock();
				}
				break;
			default:
				break;
			}
			return true;
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			loadContext((AbstractRDbgController) service, monitor);
		}
		
	}
	
	
	private final ReentrantReadWriteLock lock= new ReentrantReadWriteLock();
	
	private final RMainThread thread;
	
	private int stamp;
	
	private Frame dbgFrame;
	private FrameContext dbgFrameContext;
	
	private final RElementName elementName;
	
	private final String call;
	private final String fileName;
	
	private Long handle;
	
	private boolean detailLoaded;
	private LoadContextRunnable contextRunnable;
	private final Condition contextCondition= this.lock.writeLock().newCondition();
	private final Condition contextWaitCondition= this.lock.writeLock().newCondition();
	
	private PositionResolver positionResolver;
	
	private RElementVariable frameVariable;
	private IValue variables;
	
	private IRBreakpointStatus breakpointStatus;
	
	
	public RStackFrame(final RDebugTarget target, final RMainThread thread, final int stamp,
			final Frame dbgFrame, final Long handle, final String call, final String fileName,
			final IRBreakpointStatus breakpointStatus) {
		super(target);
		this.thread= thread;
		
		this.stamp= stamp;
		this.dbgFrame= dbgFrame;
		
		if (dbgFrame.getPosition() > 0) {
			this.elementName= RElementName.create(RElementName.SCOPE_SYSFRAME,
					Integer.toString(dbgFrame.getPosition()) );
		}
		else if (dbgFrame.getPosition() == 0) {
			this.elementName= RModel.GLOBAL_ENV_NAME;
		}
		else {
			this.elementName= null;
		}
		
		this.handle= handle;
		this.call= call;
		this.fileName= fileName;
		
		this.breakpointStatus= breakpointStatus;
	}
	
	
	public boolean update(final int stamp,
			final Frame dbgFrame, final Long handle, final String call, final String fileName,
			final IRBreakpointStatus breakpointStatus) {
		if (call.equals(this.call)
				&& Objects.equals(dbgFrame.getFileName(), this.dbgFrame.getFileName())
				&& dbgFrame.getFileTimestamp() == this.dbgFrame.getFileTimestamp() ) {
			this.lock.writeLock().lock();
			try {
				if (this.stamp != stamp) {
					this.stamp= stamp;
					if (((dbgFrame.getExprSrcref() != null) ?
								!dbgFrame.getExprSrcref().equals(this.dbgFrame.getExprSrcref()) :
								null != this.dbgFrame.getExprSrcref())
							|| dbgFrame.getFileTimestamp() == 0
							|| breakpointStatus != null ) {
						// need new detail
						this.dbgFrame= dbgFrame;
						this.detailLoaded= false;
						this.breakpointStatus= breakpointStatus;
						
						if (this.contextRunnable != null) {
							this.contextRunnable.cancel= true;
							this.contextCondition.signalAll();
						}
					}
					this.handle= handle;
					this.frameVariable= null;
					this.variables= null;
				}
				return true;
			}
			finally {
				this.lock.writeLock().unlock();
			}
		}
		return false;
	}
	
	@Override
	public RMainThread getThread() {
		return this.thread;
	}
	
	@Override
	public String getName() throws DebugException {
		return this.call;
	}
	
	@Override
	public RElementName getElementName() {
		final RElementVariable variable= this.frameVariable;
		if (variable != null) {
			final ICombinedRElement element= variable.getElement();
			return element.getElementName();
		}
		return this.elementName;
	}
	
	public @NonNull Long getHandle() {
		return this.handle;
	}
	
	public Frame getDbgFrame() {
		return this.dbgFrame;
	}
	
	@Override
	public String getInfoFileName() {
		return this.fileName;
	}
	
	@Override
	public int getInfoLineNumber() {
		final int[] exprSrcref= this.dbgFrame.getExprSrcref();
		return (exprSrcref != null) ? exprSrcref[0] : -1;
	}
	
	@Override
	public int getPosition() {
		return this.dbgFrame.getPosition();
	}
	
	
	@Override
	public boolean isTerminated() {
		return this.thread.isTerminated();
	}
	
	@Override
	public boolean canTerminate() {
		return this.thread.canTerminate();
	}
	
	@Override
	public void terminate() throws DebugException {
		this.thread.terminate();
	}
	
	
	@Override
	public boolean isSuspended() {
		return this.thread.isSuspended();
	}
	
	@Override
	public boolean canSuspend() {
		return this.thread.canSuspend();
	}
	
	@Override
	public boolean canResume() {
		return this.thread.canResume();
	}
	
	@Override
	public void suspend() throws DebugException {
		this.thread.suspend();
	}
	
	@Override
	public void resume() throws DebugException {
		this.thread.resume();
	}
	
	
	@Override
	public boolean isStepping() {
		return this.thread.isStepping();
	}
	
	@Override
	public boolean canStepInto() {
		return (isSuspended() && this.dbgFrame.isTopFrame());
	}
	
	@Override
	public boolean canStepOver() {
		return (isSuspended() && (this.dbgFrame.getFlags() & CallStack.FLAG_NOSTEPPING) == 0);
	}
	
	@Override
	public boolean canStepReturn() {
		return (isSuspended() && this.dbgFrame.getPosition() > 0 && !this.dbgFrame.isTopLevelCommand());
	}
	
	@Override
	public void stepInto() throws DebugException {
		if (canStepInto()) {
			getThread().stepInto();
		}
	}
	
	@Override
	public void stepOver() throws DebugException {
		if (canStepOver()) {
			getThread().stepToFrame(this, 0);
		}
	}
	
	@Override
	public void stepReturn() throws DebugException {
		if (canStepReturn()) {
			getThread().stepToFrame(this, 1);
		}
	}
	
	
	@Override
	public boolean hasVariables() throws DebugException {
		return this.dbgFrame.getPosition() > 0;
	}
	
	@Override
	public IVariable[] getVariables() throws DebugException {
		this.lock.readLock().lock();
		try {
			if (ensureContext() != null && this.variables != null) {
				return this.variables.getVariables();
			}
			return new IVariable[0];
		}
		finally {
			this.lock.readLock().unlock();
		}
	}
	
	@Override
	public boolean hasRegisterGroups() throws DebugException {
		return false;
	}
	
	@Override
	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		return null;
	}
	
	
	public void setPositionResolver(final FrameContext context, final PositionResolver resolver) {
		this.lock.writeLock().lock();
		if ((this.dbgFrameContext != null) ? this.dbgFrameContext == context : null == context) {
			this.positionResolver= resolver;
		}
		this.lock.writeLock().unlock();
	}
	
	@Override
	public int getLineNumber() throws DebugException {
		PositionResolver resolver;
		this.lock.readLock().lock();
		try {
			resolver= this.positionResolver;
		}
		finally {
			this.lock.readLock().unlock();
		}
		if (resolver != null) {
			return resolver.getLineNumber() + 1;
		}
		return getInfoLineNumber();
	}
	
	@Override
	public int getCharStart() throws DebugException {
		final PositionResolver resolver;
		this.lock.readLock().lock();
		try {
			resolver= this.positionResolver;
		}
		finally {
			this.lock.readLock().unlock();
		}
		if (resolver != null) {
			return resolver.getCharStart();
		}
		return -1;
	}
	
	@Override
	public int getCharEnd() throws DebugException {
		final PositionResolver resolver;
		this.lock.readLock().lock();
		try {
			resolver= this.positionResolver;
		}
		finally {
			this.lock.readLock().unlock();
		}
		if (resolver != null) {
			return resolver.getCharEnd();
		}
		return -1;
	}
	
	
	private FrameContext ensureContext() {
		if (!this.detailLoaded) {
			this.lock.readLock().unlock();
			this.lock.writeLock().lock();
			try {
				final Frame frame= this.dbgFrame;
				while (this.contextRunnable != null && this.dbgFrame == frame) {
					try {
						this.contextWaitCondition.await();
					}
					catch (final InterruptedException e) {}
				}
				
				if (!this.detailLoaded && this.dbgFrame == frame) {
					if (this.dbgFrame.getPosition() < 0) {
						this.detailLoaded= true;
					}
					else {
						this.contextRunnable= new LoadContextRunnable();
						try {
							if (getDebugTarget().getProcess().getQueue().addHot(this.contextRunnable).isOK()) {
								try {
									this.contextCondition.await();
								}
								catch (final InterruptedException e) {
									this.contextRunnable.cancel= true;
								}
								if (this.contextRunnable.cancel) {
									getDebugTarget().getProcess().getQueue().removeHot(this.contextRunnable);
								}
							}
						}
						finally {
							this.contextRunnable= null;
							this.contextWaitCondition.signalAll();
						}
					}
				}
				
				if (this.dbgFrame != frame) {
					return null;
				}
			}
			finally {
				this.lock.readLock().lock();
				this.lock.writeLock().unlock();
			}
		}
		return this.dbgFrameContext;
	}
	
	public FrameContext getContext() {
		this.lock.readLock().lock();
		try {
			return ensureContext();
		}
		finally {
			this.lock.readLock().unlock();
		}
	}
	
	protected void loadContext(final AbstractRDbgController r, final IProgressMonitor monitor) throws CoreException {
		this.lock.writeLock().lock();
		try {
			if (!r.isSuspended() || r.getHotTasksState() > 1) {
				return;
			}
			this.detailLoaded= true;
			if (this.stamp == r.getChangeStamp()) {
				this.dbgFrameContext= r.evalFrameContext(this.dbgFrame.getPosition(), monitor);
				ICombinedRElement element= null;
				if (this.dbgFrame.getPosition() >= 0) {
					final ICombinedRElement ref= (ICombinedRElement) r.getWorkspaceData()
							.createReference(getHandle(), this.elementName,
									RObject.TYPE_ENV, RObject.CLASSNAME_ENV);
					element= this.thread.resolveReference(ref, this.stamp, monitor);
				}
				if (element != null) {
					this.frameVariable= new RElementVariable(element, this.thread, this.stamp, null);
					this.variables= this.frameVariable.getValue();
				}
			}
		}
		catch (final CoreException e) {
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
					"An error occured when updating the debug context (position "+this.dbgFrame.getPosition()+").", e));
		}
		finally {
			try {
				this.contextCondition.signalAll();
			}
			finally {
				this.lock.writeLock().unlock();
			}
		}
	}
	
	
	@Override
	public <T> @Nullable T getAdapter(final Class<T> type) {
		if (type == IRStackFrame.class) {
			return (T) this;
		}
		if (type == IRBreakpointStatus.class) {
			return (T) this.breakpointStatus;
		}
		if (type == IBreakpoint.class) {
			final IRBreakpointStatus breakpointStatus= this.breakpointStatus;
			return (breakpointStatus != null) ? (T) breakpointStatus.getBreakpoint() : null;
		}
		if (type == IModelElement.class) {
			final RElementVariable variable= this.frameVariable;
			return (variable != null) ? (T) this.frameVariable.getElement() : null;
		}
		return super.getAdapter(type);
	}
	
	
	@Override
	public String toString() {
		final StringBuilder sb= new StringBuilder(getClass().getName());
		sb.append("\n\t"); //$NON-NLS-1$
		sb.append("position= ").append(this.dbgFrame.getPosition()); //$NON-NLS-1$
		sb.append("\n\t"); //$NON-NLS-1$
		sb.append("fileName= ").append(this.dbgFrame.getFileName()); //$NON-NLS-1$
		sb.append("\n\t"); //$NON-NLS-1$
		sb.append("exprSrcref= ").append(getInfoLineNumber()); //$NON-NLS-1$
		return sb.toString();
	}
	
}
