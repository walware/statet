/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IVariable;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolService;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.AbstractRToolRunnable;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.server.dbg.CallStack;
import de.walware.rj.server.dbg.Frame;
import de.walware.rj.server.dbg.FrameContext;

import de.walware.statet.r.console.core.LoadReferenceRunnable;
import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.console.core.RWorkspace.ICombinedREnvironment;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.debug.core.IRDebugTarget;
import de.walware.statet.r.debug.core.IRStackFrame;
import de.walware.statet.r.debug.core.IRThread;
import de.walware.statet.r.debug.core.breakpoints.IRBreakpointStatus;
import de.walware.statet.r.nico.AbstractRDbgController;


public class RStackFrame extends RDebugElement implements IRStackFrame {
	
	
	public static interface PositionResolver {
		
		int getLineNumber();
		
		int getCharStart();
		
		int getCharEnd();
		
	}
	
	
	private class LoadContextRunnable implements ISystemRunnable {
		
		private boolean fCancel;
		
		@Override
		public String getTypeId() {
			return "r/dbg/stackframe";
		}
		
		@Override
		public String getLabel() {
			return "Update Debug Context (Stack Frame)";
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return (tool == fThread.getDebugTarget().getProcess());
		}
		
		@Override
		public boolean changed(final int event, final ITool process) {
			switch (event) {
			case REMOVING_FROM:
				return fCancel;
			case MOVING_FROM:
				return false;
			case BEING_ABANDONED:
//			case FINISHING_: // handled in #loadContext
				fLock.writeLock().lock();
				try {
					fContextCondition.signalAll();
				}
				finally {
					fLock.writeLock().unlock();
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
	
	abstract class LoadDataRunnable<V extends RObject> extends AbstractRToolRunnable implements ISystemRunnable {
		
		
		private boolean fCancel;
		
		private V fData;
		
		
		public LoadDataRunnable() {
			super("r/dbg/stackframe/loadData", "Update Debug Context (Variables)");
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return (tool == fThread.getDebugTarget().getProcess());
		}
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			switch (event) {
			case REMOVING_FROM:
				return fCancel;
			case MOVING_FROM:
				return false;
			case BEING_ABANDONED:
			case FINISHING_OK:
			case FINISHING_ERROR:
			case FINISHING_CANCEL:
				synchronized (LoadDataRunnable.this) {
					LoadDataRunnable.this.notifyAll();
				}
				break;
			default:
				break;
			}
			return true;
		}
		
		@Override
		public void run(final IRToolService r,
				final IProgressMonitor monitor) throws CoreException {
			final AbstractRDbgController controller = (AbstractRDbgController) r;
			if (fStamp != controller.getCounter()) {
				return;
			}
			try {
				fData = doLoad(controller, monitor);
			}
			catch (final UnexpectedRDataException e) {
				throw new CoreException(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID,
						"Unexpected state", e ));
			}
		}
		
		protected abstract V doLoad(IRToolService r,
				IProgressMonitor monitor) throws CoreException, UnexpectedRDataException;
		
	}
	
	
	private final ReentrantReadWriteLock fLock = new ReentrantReadWriteLock();
	
	private final IRThread fThread;
	
	private int fStamp;
	
	private Frame fDbgFrame;
	private FrameContext fDbgFrameContext;
	
	private final String fCall;
	private final String fFileName;
	
	private boolean fDetailLoaded;
	private LoadContextRunnable fContextRunnable;
	private final Condition fContextCondition = fLock.writeLock().newCondition();
	private final Condition fContextWaitCondition = fLock.writeLock().newCondition();
	
	private PositionResolver fPositionResolver;
	
	private RElementVariable fFrameVariable;
	private RListValue fVariables;
	
	private Map<Long, ICombinedRElement> fReferences;
	
	private IRBreakpointStatus fBreakpointStatus;
	
	
	public RStackFrame(final IRDebugTarget target, final IRThread thread, final int stamp,
			final Frame dbgFrame, final String call, final String fileName,
			final IRBreakpointStatus breakpointStatus) {
		super(target);
		fThread = thread;
		
		fStamp = stamp;
		fDbgFrame = dbgFrame;
		
		fCall = call;
		fFileName = fileName;
		
		fBreakpointStatus = breakpointStatus;
	}
	
	
	public synchronized RStackFrame update(final int stamp,
			final Frame dbgFrame, final String call, final String fileName,
			final IRBreakpointStatus breakpointStatus) {
		if (dbgFrame.getHandle() == fDbgFrame.getHandle()
				&& dbgFrame.getPosition() == fDbgFrame.getPosition()
				&& call.equals(fCall)
				&& ((dbgFrame.getFileName() != null) ?
						dbgFrame.getFileName().equals(fDbgFrame.getFileName()) :
						null == fDbgFrame.getFileName() )
				&& (dbgFrame.getFileTimestamp() == fDbgFrame.getFileTimestamp()
						|| dbgFrame.getFileTimestamp() == 0) ) {
			fLock.writeLock().lock();
			try {
				if (fStamp != stamp) {
					fStamp = stamp;
					if (((dbgFrame.getExprSrcref() != null) ?
								!dbgFrame.getExprSrcref().equals(fDbgFrame.getExprSrcref()) :
								null != fDbgFrame.getExprSrcref())
							|| dbgFrame.getFileTimestamp() == 0
							|| breakpointStatus != null ) {
						// need new detail
						fDbgFrame = dbgFrame;
						fDetailLoaded = false;
						fBreakpointStatus = breakpointStatus;
						
						if (fContextRunnable != null) {
							fContextRunnable.fCancel = true;
							fContextCondition.signalAll();
						}
					}
					fFrameVariable = null;
					fVariables = null;
				}
				return this;
			}
			finally {
				fLock.writeLock().unlock();
			}
		}
		return new RStackFrame(getDebugTarget(), getThread(), stamp,
				dbgFrame, call, fileName, breakpointStatus);
	}
	
	@Override
	public IRThread getThread() {
		return fThread;
	}
	
	@Override
	public String getName() throws DebugException {
		return fCall;
	}
	
	
	String getCall() {
		return fCall;
	}
	
	@Override
	public String getInfoFileName() {
		return fFileName;
	}
	
	@Override
	public int getInfoLineNumber() {
		final int[] exprSrcref = fDbgFrame.getExprSrcref();
		return (exprSrcref != null) ? exprSrcref[0] : -1;
	}
	
	@Override
	public int getPosition() {
		return fDbgFrame.getPosition();
	}
	
	
	@Override
	public boolean isTerminated() {
		return fThread.isTerminated();
	}
	
	@Override
	public boolean canTerminate() {
		return fThread.canTerminate();
	}
	
	@Override
	public void terminate() throws DebugException {
		fThread.terminate();
	}
	
	
	@Override
	public boolean isSuspended() {
		return fThread.isSuspended();
	}
	
	@Override
	public boolean canSuspend() {
		return fThread.canSuspend();
	}
	
	@Override
	public boolean canResume() {
		return fThread.canResume();
	}
	
	@Override
	public void suspend() throws DebugException {
		fThread.suspend();
	}
	
	@Override
	public void resume() throws DebugException {
		fThread.resume();
	}
	
	
	@Override
	public boolean isStepping() {
		return fThread.isStepping();
	}
	
	@Override
	public boolean canStepInto() {
		return (isSuspended() && fDbgFrame.isTopFrame());
	}
	
	@Override
	public boolean canStepOver() {
		return (isSuspended() && (fDbgFrame.getFlags() & CallStack.FLAG_NOSTEPPING) == 0);
	}
	
	@Override
	public boolean canStepReturn() {
		return (isSuspended() && fDbgFrame.getPosition() > 0 && !fDbgFrame.isTopLevelCommand());
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
			((RMainThread) getThread()).stepToFrame(this, 0);
		}
	}
	
	@Override
	public void stepReturn() throws DebugException {
		if (canStepReturn()) {
			((RMainThread) getThread()).stepToFrame(this, 1);
		}
	}
	
	
	@Override
	public boolean hasVariables() throws DebugException {
		return fDbgFrame.getPosition() > 0;
	}
	
	@Override
	public IVariable[] getVariables() throws DebugException {
		fLock.readLock().lock();
		try {
			if (ensureContext() != null && fVariables != null ) {
				return fVariables.getVariables();
			}
			return new IVariable[0];
		}
		finally {
			fLock.readLock().unlock();
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
		fLock.writeLock().lock();
		if ((fDbgFrameContext != null) ? fDbgFrameContext == context : null == context) {
			fPositionResolver = resolver;
		}
		fLock.writeLock().unlock();
	}
	
	@Override
	public int getLineNumber() throws DebugException {
		PositionResolver resolver;
		fLock.readLock().lock();
		try {
			resolver = fPositionResolver;
		}
		finally {
			fLock.readLock().unlock();
		}
		if (resolver != null) {
			return resolver.getLineNumber() + 1;
		}
		return getInfoLineNumber();
	}
	
	@Override
	public int getCharStart() throws DebugException {
		final PositionResolver resolver;
		fLock.readLock().lock();
		try {
			resolver = fPositionResolver;
		}
		finally {
			fLock.readLock().unlock();
		}
		if (resolver != null) {
			return resolver.getCharStart();
		}
		return -1;
	}
	
	@Override
	public int getCharEnd() throws DebugException {
		final PositionResolver resolver;
		fLock.readLock().lock();
		try {
			resolver = fPositionResolver;
		}
		finally {
			fLock.readLock().unlock();
		}
		if (resolver != null) {
			return resolver.getCharEnd();
		}
		return -1;
	}
	
	
	private FrameContext ensureContext() {
		if (!fDetailLoaded) {
			fLock.readLock().unlock();
			fLock.writeLock().lock();
			try {
				final Frame frame = fDbgFrame;
				while (fContextRunnable != null && fDbgFrame == frame) {
					try {
						fContextWaitCondition.await();
					}
					catch (final InterruptedException e) {}
				}
				
				if (!fDetailLoaded && fDbgFrame == frame) {
					if (fDbgFrame.getPosition() < 0) {
						fDetailLoaded = true;
					}
					else {
						fContextRunnable = new LoadContextRunnable();
						try {
							if (getDebugTarget().getProcess().getQueue().addHot(fContextRunnable).isOK()) {
								try {
									fContextCondition.await();
								}
								catch (final InterruptedException e) {
									fContextRunnable.fCancel = true;
								}
								if (fContextRunnable.fCancel) {
									getDebugTarget().getProcess().getQueue().removeHot(fContextRunnable);
								}
							}
						}
						finally {
							fContextRunnable = null;
							fContextWaitCondition.signalAll();
						}
					}
				}
				
				if (fDbgFrame != frame) {
					return null;
				}
			}
			finally {
				fLock.readLock().lock();
				fLock.writeLock().unlock();
			}
		}
		return fDbgFrameContext;
	}
	
	public FrameContext getContext() {
		fLock.readLock().lock();
		try {
			return ensureContext();
		}
		finally {
			fLock.readLock().unlock();
		}
	}
	
	protected void loadContext(final AbstractRDbgController r, final IProgressMonitor monitor) throws CoreException {
		fLock.writeLock().lock();
		try {
			if (r.isSuspended() && r.getHotTasksState() <= 1) {
				fDetailLoaded = true;
				if (fStamp == r.getCounter()) {
					fDbgFrameContext = r.evalFrameContext(fDbgFrame.getPosition(), monitor);
					ICombinedRElement element = null;
					if (fDbgFrame.getPosition() > 0) {
						final RReference ref = r.getWorkspaceData().createReference(fDbgFrame.getHandle(),
								RElementName.create(RElementName.MAIN_SYSFRAME, Integer.toString(fDbgFrame.getPosition())),
								RObject.CLASSNAME_ENV);
						element = r.getWorkspaceData().resolve(ref, monitor);
					}
					else if (fDbgFrame.getPosition() == 0) {
						final List<? extends ICombinedREnvironment> environments = r.getWorkspaceData().getRSearchEnvironments();
						if (!environments.isEmpty()) {
							element = environments.get(0);
						}
					}
					if (element != null) {
						fFrameVariable = new RElementVariable(element, this, fStamp);
						fVariables = (RListValue) fFrameVariable.getValue();
					}
				}
			}
		}
		catch (final CoreException e) {
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
					"An error occured when updating the debug context (position "+fDbgFrame.getPosition()+").", e));
		}
		finally {
			try {
				fContextCondition.signalAll();
			}
			finally {
				fLock.writeLock().unlock();
			}
		}
	}
	
	public String createRefExpression(final ICombinedRElement element, final int stamp) {
		fLock.readLock().lock();
		try {
			if (fStamp != stamp) {
				return null;
			}
			final List<RElementName> segments = new ArrayList<RElementName>();
			createName(element, segments);
			return RElementName.createDisplayName(RElementName.concat(segments),
						RElementName.DISPLAY_NS_PREFIX | RElementName.DISPLAY_EXACT);
		}
		catch (final Exception e) {
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
					"An error occurred when creating R element name to load data.", e));
			return null;
		}
		finally {
			fLock.readLock().unlock();
		}
	}
	
	public <V extends RObject> V loadData(final LoadDataRunnable<V> runnable, final int stamp) {
		synchronized (runnable) {
			if (getDebugTarget().getProcess().getQueue().addHot(runnable).isOK()) {
				try {
					runnable.wait();
					return runnable.fData;
				}
				catch (final InterruptedException e) {
					runnable.fCancel = true;
					getDebugTarget().getProcess().getQueue().removeHot(runnable);
				}
			}
			return null;
		}
	}
	
	public ICombinedRElement loadReference(final RReference reference, final int stamp) {
		if (fStamp != stamp) {
			return null;
		}
		final RProcess process = getDebugTarget().getProcess();
		final LoadReferenceRunnable runnable = new LoadReferenceRunnable(reference, process, stamp,
				"Debug Context" );
		synchronized (runnable) {
			if (process.getQueue().addHot(runnable).isOK()) {
				try {
					runnable.wait();
					return runnable.getResolvedElement();
				}
				catch (final InterruptedException e) {
					runnable.cancel();
					getDebugTarget().getProcess().getQueue().removeHot(runnable);
				}
			}
			return null;
		}
	}
	
	public void registerReference(final RReference reference, final int stamp) {
		fLock.readLock().lock();
		try {
			if (fStamp == stamp && fVariables != null) {
				synchronized (fVariables) {
					if (fReferences == null) {
						fReferences = new HashMap<Long, ICombinedRElement>();
					}
					final Long handle = Long.valueOf(reference.getHandle());
					if (!fReferences.containsKey(handle)) {
						fReferences.put(handle, (ICombinedRElement) reference);
					}
				}
			}
		}
		finally {
			fLock.readLock().unlock();
		}
	}
	
	private void createName(final ICombinedRElement element, final List<RElementName> segments) {
		if (element.getRObjectType() != RObject.TYPE_ENV) {
			final ICombinedRElement parent = element.getModelParent();
			if (parent != null) {
				createName(parent, segments);
			}
			segments.add(element.getElementName());
			return;
		}
		else {
			final RElementName name = element.getElementName();
			if (name != null) {
				switch (name.getType()) {
				case RElementName.MAIN_SEARCH_ENV:
				case RElementName.MAIN_PACKAGE:
				case RElementName.MAIN_SYSFRAME:
				case RElementName.MAIN_PROJECT:
					segments.add(element.getElementName());
					return;
				}
			}
			
			ICombinedRElement resolved = null;
			if (fVariables != null) {
				synchronized (fVariables) {
					if (fReferences != null) {
						resolved = fReferences.get(((ICombinedREnvironment) element).getHandle());
					}
				}
			}
			if (resolved != null) {
				createName(resolved, segments);
				return;
			}
			throw new IllegalStateException("Unable to create name.");
		}
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (IRStackFrame.class.equals(required)) {
			return this;
		}
		if (IRBreakpointStatus.class.equals(required)) {
			return fBreakpointStatus;
		}
		if (IBreakpoint.class.equals(required)) {
			final IRBreakpointStatus breakpointStatus = fBreakpointStatus;
			return (breakpointStatus != null) ? breakpointStatus.getBreakpoint() : null;
		}
		if (IModelElement.class.equals(required)) {
			final RElementVariable variable = fFrameVariable;
			return (variable != null) ? fFrameVariable.getElement() : null;
		}
		return super.getAdapter(required);
	}
	
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(getClass().getName());
		sb.append("\n\t"); //$NON-NLS-1$
		sb.append("position= ").append(fDbgFrame.getPosition()); //$NON-NLS-1$
		sb.append("\n\t"); //$NON-NLS-1$
		sb.append("fileName= ").append(fDbgFrame.getFileName()); //$NON-NLS-1$
		sb.append("\n\t"); //$NON-NLS-1$
		sb.append("exprSrcref= ").append(getInfoLineNumber()); //$NON-NLS-1$
		return sb.toString();
	}
	
}
