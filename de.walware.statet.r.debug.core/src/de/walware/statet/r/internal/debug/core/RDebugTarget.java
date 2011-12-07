/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.core;

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

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.runtime.IRemoteEngineController;
import de.walware.statet.nico.core.runtime.ToolController.IToolStatusListener;
import de.walware.statet.nico.core.runtime.ToolStatus;

import de.walware.rj.server.dbg.DbgFilterState;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.debug.core.IRDebugTarget;
import de.walware.statet.r.debug.core.breakpoints.IRBreakpoint;
import de.walware.statet.r.internal.debug.core.breakpoints.RControllerBreakpointAdapter;
import de.walware.statet.r.nico.AbstractRDbgController;


public class RDebugTarget extends RDebugElement implements IRDebugTarget, IStepFilters,
		IToolStatusListener {
	
	
	protected final RProcess fProcess;
	private final AbstractRDbgController fController;
	
	private final RControllerBreakpointAdapter fBreakpointAdapter;
	
	protected final List<IThread> fThreads = new ArrayList<IThread>(1);
	private RMainThread fMainThread;
	
	protected boolean fStepFiltersEnabled;
	
	
	public RDebugTarget(final AbstractRDbgController controller) {
		super(null);
		fController = controller;
		fProcess = controller.getTool();
		
		fBreakpointAdapter = new RControllerBreakpointAdapter(this, fController);
		init();
		
		fController.initDebug(fBreakpointAdapter);
	}
	
	
	@Override
	public final RDebugTarget getDebugTarget() {
		return this;
	}
	
	@Override
	public ILaunch getLaunch() {
		return fProcess.getLaunch();
	}
	
	@Override
	public RProcess getProcess() {
		return fProcess;
	}
	
	@Override
	public String getName() throws DebugException {
		return "R Engine"; //$NON-NLS-1$
	}
	
	
	protected void init() {
		getLaunch().addDebugTarget(this);
		fireCreationEvent();
		fController.addToolStatusListener(this);
		
		fBreakpointAdapter.init();
		initState();
	}
	
	@Override
	public void controllerStatusRequested(final ToolStatus currentStatus,
			final ToolStatus requestedStatus, final List<DebugEvent> eventCollection) {
	}
	
	@Override
	public void controllerStatusRequestCanceled(final ToolStatus currentStatus,
			final ToolStatus requestedStatus, final List<DebugEvent> eventCollection) {
	}
	
	@Override
	public void controllerStatusChanged(final ToolStatus oldStatus,
			final ToolStatus newStatus, final List<DebugEvent> eventCollection) {
		if (newStatus == ToolStatus.TERMINATED) {
			fBreakpointAdapter.dispose();
			
			eventCollection.add(new DebugEvent(this, DebugEvent.TERMINATE));
			synchronized (fThreads) {
				if (fMainThread != null) {
					fMainThread.setTerminated();
					fMainThread = null;
				}
				fThreads.clear();
			}
		}
	}
	
	private void initState() {
		synchronized (fThreads) {
			final RMainThread thread = fMainThread = new RMainThread(this, fController, "R Main Thread");
			fThreads.add(thread);
			thread.fireCreationEvent();
		}
	}
	
	@Override
	public boolean hasThreads() throws DebugException {
		return !fThreads.isEmpty();
	}
	
	@Override
	public IThread[] getThreads() throws DebugException {
		synchronized (fThreads) {
			return fThreads.toArray(new IThread[fThreads.size()]);
		}
	}
	
	
	@Override
	public boolean isTerminated() {
		return fProcess.isTerminated() && !isDisconnected();
	}
	
	@Override
	public boolean canTerminate() {
		return fProcess.canTerminate();
	}
	
	@Override
	public void terminate() throws DebugException {
		fProcess.terminate();
	}
	
	@Override
	public boolean isDisconnected() {
		return (fProcess.isProvidingFeatureSet(IRemoteEngineController.FEATURE_SET_ID)
				&& ((IRemoteEngineController) fController).isDisconnected());
	}
	
	@Override
	public boolean canDisconnect() {
		return (fProcess.isProvidingFeatureSet(IRemoteEngineController.FEATURE_SET_ID)
				&& !fProcess.isTerminated() );
	}
	
	@Override
	public void disconnect() throws DebugException {
		if (canDisconnect()) {
			try {
				((IRemoteEngineController) fController).disconnect(new NullProgressMonitor());
			}
			catch (final CoreException e) {
				throw new DebugException(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID,
						DebugException.TARGET_REQUEST_FAILED, e.getMessage(), e));
			}
		}
	}
	
	
	@Override
	public boolean isSuspended() {
		final RMainThread mainThread = fMainThread;
		return (mainThread != null && fMainThread.isSuspended());
	}
	
	@Override
	public boolean canSuspend() {
		final RMainThread mainThread = fMainThread;
		return (mainThread != null && mainThread.canSuspend());
	}
	
	@Override
	public void suspend() throws DebugException {
		final RMainThread mainThread = fMainThread;
		if (mainThread != null) {
			mainThread.suspend();
		}
	}
	
	@Override
	public boolean canResume() {
		if (isSuspended()) {
			return true;
		}
		synchronized (fThreads) {
			for (int i = 0; i < fThreads.size(); i++) {
				if (fThreads.get(i).canResume()) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void resume() throws DebugException {
		if (canResume()) {
			fController.debugResume();
		}
	}
	
	
	@Override
	public boolean supportsBreakpoint(final IBreakpoint breakpoint) {
		if (breakpoint instanceof IRBreakpoint) {
			return fBreakpointAdapter.supportsBreakpoint((IRBreakpoint) breakpoint);
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
		return fStepFiltersEnabled;
	}
	
	@Override
	public boolean supportsStepFilters() {
		return true;
	}
	
	@Override
	public void setStepFiltersEnabled(final boolean enabled) {
		if (fProcess.isTerminated()) {
			return;
		}
		fStepFiltersEnabled = enabled;
		try {
			fController.exec(new DbgFilterState(enabled));
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
	
}
