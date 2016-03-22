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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IStepFilters;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.runtime.IRemoteEngineController;
import de.walware.statet.nico.core.runtime.ToolController.IToolStatusListener;
import de.walware.statet.nico.core.runtime.ToolStatus;

import de.walware.rj.server.dbg.DbgFilterState;
import de.walware.rj.server.dbg.DbgRequest;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.core.data.RValueFormatter;
import de.walware.statet.r.core.data.RValueValidator;
import de.walware.statet.r.debug.core.IRDebugTarget;
import de.walware.statet.r.debug.core.breakpoints.IRBreakpoint;
import de.walware.statet.r.internal.debug.core.RDebugCorePlugin;
import de.walware.statet.r.internal.debug.core.breakpoints.RControllerBreakpointAdapter;
import de.walware.statet.r.internal.debug.core.eval.ExpressionValidator;
import de.walware.statet.r.nico.AbstractRDbgController;


@NonNullByDefault
public class RDebugTarget extends RDebugElement implements IRDebugTarget, IStepFilters,
		IToolStatusListener {
	
	
	protected final RProcess process;
	private final AbstractRDbgController controller;
	
	private final RControllerBreakpointAdapter breakpointAdapter;
	
	protected final List<IThread> threads= new ArrayList<>(1);
	private @Nullable RMainThread mainThread;
	
	protected boolean stepFiltersEnabled;
	
	private @Nullable RValueValidator valueValidator;
	private @Nullable RValueFormatter valueFormatter;
	private @Nullable ExpressionValidator expressionValidator;
	
	
	public RDebugTarget(final AbstractRDbgController controller) {
		super(null);
		this.controller= controller;
		this.process= controller.getTool();
		
		this.breakpointAdapter= new RControllerBreakpointAdapter(this, this.controller);
		init();
		
		this.controller.initDebug(this.breakpointAdapter);
	}
	
	
	@Override
	public final RDebugTarget getDebugTarget() {
		return this;
	}
	
	@Override
	public ILaunch getLaunch() {
		return this.process.getLaunch();
	}
	
	@Override
	public RProcess getProcess() {
		return this.process;
	}
	
	@Override
	public String getName() throws DebugException {
		return "R Engine"; //$NON-NLS-1$
	}
	
	
	protected void init() {
		getLaunch().addDebugTarget(this);
		fireCreationEvent();
		this.controller.addToolStatusListener(this);
		
		this.breakpointAdapter.init();
		initState();
	}
	
	@Override
	public void controllerStatusChanged(final ToolStatus oldStatus,
			final ToolStatus newStatus, final List<DebugEvent> eventCollection) {
		if (newStatus == ToolStatus.TERMINATED) {
			this.breakpointAdapter.dispose();
			
			eventCollection.add(new DebugEvent(this, DebugEvent.TERMINATE));
			synchronized (this.threads) {
				if (this.mainThread != null) {
					this.mainThread.setTerminated();
					this.mainThread= null;
				}
				this.threads.clear();
			}
		}
	}
	
	private void initState() {
		synchronized (this.threads) {
			final RMainThread thread= this.mainThread= new RMainThread(this, this.controller, "R Main Thread");
			this.threads.add(thread);
			thread.fireCreationEvent();
		}
	}
	
	@Override
	public boolean hasThreads() throws DebugException {
		return !this.threads.isEmpty();
	}
	
	@Override
	public @NonNull IThread[] getThreads() throws DebugException {
		synchronized (this.threads) {
			return this.threads.toArray(new IThread[this.threads.size()]);
		}
	}
	
	
	@Override
	public boolean isTerminated() {
		return this.process.isTerminated() && !isDisconnected();
	}
	
	@Override
	public boolean canTerminate() {
		return this.process.canTerminate();
	}
	
	@Override
	public void terminate() throws DebugException {
		this.process.terminate();
	}
	
	@Override
	public boolean isDisconnected() {
		return (this.process.isProvidingFeatureSet(IRemoteEngineController.FEATURE_SET_ID)
				&& ((IRemoteEngineController) this.controller).isDisconnected());
	}
	
	@Override
	public boolean canDisconnect() {
		return (this.process.isProvidingFeatureSet(IRemoteEngineController.FEATURE_SET_ID)
				&& !this.process.isTerminated() );
	}
	
	@Override
	public void disconnect() throws DebugException {
		if (canDisconnect()) {
			try {
				((IRemoteEngineController) this.controller).disconnect(new NullProgressMonitor());
			}
			catch (final CoreException e) {
				throw new DebugException(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID,
						DebugException.TARGET_REQUEST_FAILED, e.getMessage(), e));
			}
		}
	}
	
	
	protected void exec(final DbgRequest request) throws DebugException {
		try {
			this.controller.exec(request);
		}
		catch (final CoreException e) {
			throw new DebugException(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID,
					DebugException.REQUEST_FAILED,
					"An error occurred when executing debug request in the R engine.",
					e ));
		}
	}
	
	@Override
	public boolean isSuspended() {
		final RMainThread mainThread= this.mainThread;
		return (mainThread != null && mainThread.isSuspended());
	}
	
	@Override
	public boolean canSuspend() {
		final RMainThread mainThread= this.mainThread;
		return (mainThread != null && mainThread.canSuspend());
	}
	
	@Override
	public void suspend() throws DebugException {
		final RMainThread mainThread= this.mainThread;
		if (mainThread != null) {
			mainThread.suspend();
		}
	}
	
	@Override
	public boolean canResume() {
		if (isSuspended()) {
			return true;
		}
		synchronized (this.threads) {
			for (int i= 0; i < this.threads.size(); i++) {
				if (this.threads.get(i).canResume()) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void resume() throws DebugException {
		if (canResume()) {
			exec(new DbgRequest.Resume());
		}
	}
	
	
	@Override
	public boolean supportsBreakpoint(final IBreakpoint breakpoint) {
		if (breakpoint instanceof IRBreakpoint) {
			return this.breakpointAdapter.supportsBreakpoint((IRBreakpoint) breakpoint);
		}
		return false;
	}
	
	@Override
	public void breakpointAdded(final IBreakpoint breakpoint) {
	}
	
	@Override
	public void breakpointRemoved(final IBreakpoint breakpoint, final IMarkerDelta delta) {
	}
	
	@Override
	public void breakpointChanged(final IBreakpoint breakpoint, final IMarkerDelta delta) {
	}
	
	@Override
	public boolean isStepFiltersEnabled() {
		return this.stepFiltersEnabled;
	}
	
	@Override
	public boolean supportsStepFilters() {
		return true;
	}
	
	@Override
	public void setStepFiltersEnabled(final boolean enabled) {
		if (this.process.isTerminated()) {
			return;
		}
		this.stepFiltersEnabled= enabled;
		try {
			this.controller.exec(new DbgFilterState(enabled));
		}
		catch (final CoreException e) {
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
					"An error occurred when updating step filters in the R engine.", e));
		}
	}
	
	
	@Override
	public boolean supportsStorageRetrieval() {
		return false;
	}
	
	@Override
	public IMemoryBlock getMemoryBlock(final long startAddress, final long length) throws DebugException {
		return null;
	}
	
	
	public synchronized RValueValidator getValueValidator() {
		if (this.valueValidator == null) {
			this.valueValidator= new RValueValidator();
		}
		return this.valueValidator;
	}
	
	public synchronized RValueFormatter getValueFormatter() {
		if (this.valueFormatter == null) {
			this.valueFormatter= new RValueFormatter();
		}
		return this.valueFormatter;
	}
	
	public synchronized ExpressionValidator getExpressionValidator() {
		if (this.expressionValidator == null) {
			this.expressionValidator= new ExpressionValidator();
		}
		return this.expressionValidator;
	}
	
}
