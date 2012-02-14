/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;

import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolService;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.runtime.ToolController.IToolStatusListener;
import de.walware.statet.nico.core.runtime.ToolStatus;

import de.walware.rj.server.dbg.CallStack;

import de.walware.statet.r.debug.core.IRDebugTarget;
import de.walware.statet.r.debug.core.IRStackFrame;
import de.walware.statet.r.debug.core.IRThread;
import de.walware.statet.r.debug.core.breakpoints.IRBreakpointStatus;
import de.walware.statet.r.nico.AbstractRDbgController;


public class RMainThread extends RDebugElement implements IRThread,
		IToolStatusListener {
	
	
	private static final RStackFrame[] NO_FRAMES = new RStackFrame[0];
	
	
	private final AbstractRDbgController fController;
	
	private final String fName;
	
	private final Object fFramesLock = new Object();
	private int fFramesStamp;
	private RStackFrame[] fFrames;
	
	private volatile boolean fIsSuspended;
	private volatile boolean fIsStepping;
	private boolean fIsTerminated;
	
	
	public RMainThread(final IRDebugTarget target, final AbstractRDbgController controller,
			final String name) {
		super(target);
		fController = controller;
		fName = name;
		
		init();
	}
	
	
	protected void init() {
		synchronized (fFramesLock) {
			fFrames = NO_FRAMES;
		}
		
		fController.addSuspendUpdateRunnable(new ISystemRunnable() {
			@Override
			public String getTypeId() {
				return "r/dbg/thread"; //$NON-NLS-1$
			}
			@Override
			public String getLabel() {
				return "Main Thread"; //$NON-NLS-1$
			}
			@Override
			public boolean isRunnableIn(final ITool tool) {
				return (tool == fController.getTool());
			}
			@Override
			public boolean changed(final int event, final ITool process) {
				return true;
			}
			@Override
			public void run(final IToolService service,
					final IProgressMonitor monitor) throws CoreException {
				aboutToSuspend(fController.getSuspendEnterDetail(),
						fController.getSuspendEnterData(), monitor );
			}
		});
		fController.addToolStatusListener(this);
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
		switch (newStatus) {
		case STARTED_SUSPENDED:
			fIsSuspended = true;
			fIsStepping = false;
			eventCollection.add(new DebugEvent(this, DebugEvent.SUSPEND,
					fController.getSuspendEnterDetail()));
			eventCollection.add(new DebugEvent(this, DebugEvent.CHANGE, DebugEvent.CONTENT));
			break;
		case TERMINATED:
			break;
		case STARTED_PAUSED:
			if (fController.isSuspended()) {
				break; // also pause debugging
			}
			//$FALL-THROUGH$
		case STARTED_IDLING:
			if (fIsSuspended) {
				fIsSuspended = false;
				final int detail = (fController.getSuspendExitDetail() & (DebugEvent.UNSPECIFIED | DebugEvent.CLIENT_REQUEST));
				fIsStepping = false;
				eventCollection.add(new DebugEvent(this, DebugEvent.RESUME, detail));
			}
			else if (fIsStepping) {
				final int detail = DebugEvent.UNSPECIFIED;
				fIsStepping = false;
				eventCollection.add(new DebugEvent(this, DebugEvent.RESUME, detail));
			}
			break;
		default:
			if (fIsSuspended) {
				fIsSuspended = false;
				final int detail = fController.getSuspendExitDetail();
				fIsStepping = ((detail & (DebugEvent.STEP_INTO | DebugEvent.STEP_OVER | DebugEvent.STEP_RETURN)) != 0);
				eventCollection.add(new DebugEvent(this, DebugEvent.RESUME, detail));
			}
			else if (fIsStepping) {
				final int detail = fController.getSuspendExitDetail();
				fIsStepping = ((detail & (DebugEvent.STEP_INTO | DebugEvent.STEP_OVER | DebugEvent.STEP_RETURN)) != 0);
				if (!fIsStepping) {
					eventCollection.add(new DebugEvent(this, DebugEvent.RESUME, detail));
				}
			}
			break;
		}
	}
	
	protected void aboutToSuspend(final int suspendDetail, final Object suspendData,
			final IProgressMonitor monitor) throws CoreException {
		final int stamp = fController.getCounter();
		RStackFrame[] prevFrameStack;
		RStackFrame[] newFrameStack = NO_FRAMES;
		synchronized (fFramesLock) {
			prevFrameStack = fFrames;
		}
		try {
			final CallStack stack = fController.getCallStack(monitor);
			if (stack != null) {
				final int l = stack.getFrames().size();
				if (l > 0) {
					final boolean special = DebugPlugin.isUseStepFilters();
					int m = prevFrameStack.length - 1;
					final List<RStackFrame> frameStack = new ArrayList<RStackFrame>(l);
					for (int i = 0; i < l; i++) {
						CallStack.Frame dbgFrame = stack.getFrames().get(i);
						String call;
						if (i == 0) {
							call = "[Console]";
						}
						else if (dbgFrame.getCall() != null) {
							call = dbgFrame.getCall();
							if (special && i+2 < l) {
								switch (dbgFrame.getFlags() & 0xff) {
								case CallStack.FLAG_SOURCE:
									i = i+2;
									dbgFrame = stack.getFrames().get(i); 
									call = "[Sourcing Script]";
//									dbgFrame.addFlags(CallStack.FLAG_NOSTEPPING);
									break;
								case CallStack.FLAG_COMMAND:
									i = i+2;
									dbgFrame = stack.getFrames().get(i);
									call = "[Running Command]";
									if (frameStack.size() == 1) {
										frameStack.remove(0);
									}
									break;
								}
							}
						}
						else {
							call = "[Unkown]";
						}
						
						String fileName = dbgFrame.getFileName();
						if (fileName != null) {
							int idx = fileName.lastIndexOf('/');
							{	final int idx2 = fileName.lastIndexOf('\\');
								if (idx2 > idx) {
									idx = idx2;
								}
							}
							if (idx >= 0) {
								fileName = fileName.substring(idx+1);
							}
						}
						final IRBreakpointStatus breakpoint = (dbgFrame.isTopFrame()
								&& suspendData instanceof IRBreakpointStatus) ?
										(IRBreakpointStatus) suspendData : null;
						
						if (frameStack.size() <= m) {
							final RStackFrame prevFrame = prevFrameStack[m-frameStack.size()];
							final RStackFrame eFrame = prevFrame.update(stamp, dbgFrame, call,
									fileName, breakpoint );
							frameStack.add(eFrame);
							if (eFrame != prevFrame) {
								m = -1;
							}
						}
						else {
							final RStackFrame frame = new RStackFrame(getDebugTarget(), this, stamp,
									dbgFrame, call, fileName, breakpoint );
							frameStack.add(frame);
						}
					}
					frameStack.get(frameStack.size()-1).loadContext(fController, monitor);
					newFrameStack = new RStackFrame[frameStack.size()];
					for (int i = 0; i < newFrameStack.length; i++) {
						newFrameStack[i] = frameStack.get(newFrameStack.length - 1 - i);
					}
					frameStack.toArray();
				}
			}
		}
		finally {
			synchronized (fFramesLock) {
				fFramesStamp = stamp;
				fFrames = newFrameStack;
			}
		}
	}
	
	
	protected AbstractRDbgController getController() {
		return fController;
	}
	
	@Override
	public String getName() throws DebugException {
		return fName;
	}
	
	@Override
	public int getPriority() throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID,
				DebugException.NOT_SUPPORTED, "Not supported.", null));
	}
	
	
	@Override
	public boolean isTerminated() {
		return fIsTerminated;
	}
	
	@Override
	public boolean canTerminate() {
		return false;
	}
	
	@Override
	public void terminate() throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID,
				DebugException.NOT_SUPPORTED, "Not supported.", null));
	}
	
	protected void setTerminated() {
		fIsTerminated = true;
		synchronized (fFramesLock) {
			fFrames = new RStackFrame[0]; // use other instance
		}
	}
	
	
	@Override
	public boolean isSuspended() {
		return fIsSuspended;
	}
	
	@Override
	public boolean canSuspend() {
		return !(isSuspended() || fIsTerminated);
	}
	
	@Override
	public boolean canResume() {
		return isSuspended();
	}
	
	@Override
	public void suspend() throws DebugException {
		if (canSuspend()) {
			fController.debugSuspend();
		}
	}
	
	@Override
	public void resume() throws DebugException {
		if (canResume()) {
			fController.debugResume();
		}
	}
	
	
	@Override
	public boolean isStepping() {
		return fIsStepping;
	}
	
	@Override
	public boolean canStepInto() {
		final IStackFrame topFrame = getTopStackFrame();
		return (topFrame != null && topFrame.canStepInto());
	}
	
	@Override
	public boolean canStepOver() {
		final IStackFrame topFrame = getTopStackFrame();
		return (topFrame != null && topFrame.canStepOver());
	}
	
	@Override
	public boolean canStepReturn() {
		final IStackFrame topFrame = getTopStackFrame();
		return (topFrame != null && topFrame.canStepReturn());
	}
	
	@Override
	public void stepInto() throws DebugException {
	}
	
	@Override
	public void stepOver() throws DebugException {
		if (!canStepOver()) {
			return;
		}
		fController.debugStepOver();
		return;
	}
	
	@Override
	public void stepReturn() throws DebugException {
		if (!canStepReturn()) {
			return;
		}
		stepToFrame(null, 1);
		return;
	}
	
	/**
	 * 
	 * @param refFrame reference frame, if <code>null</code> current top frame is used
	 * @param relIdx steps to move (number of index of stack frames, not positions)
	 */
	public void stepToFrame(final RStackFrame refFrame, final int relIdx) {
		List<RStackFrame> frames;
		synchronized (fFramesLock) {
			frames = new ConstList<RStackFrame>(fFrames);
		}
		if (frames.isEmpty()) {
			return;
		}
		final int refIdx = (refFrame != null) ? frames.indexOf(refFrame) : 0;
		final int targetIdx = refIdx + relIdx;
		if (refIdx < 0 || targetIdx < 0 || targetIdx >= frames.size()) {
			return;
		}
		if (refIdx == 0 && relIdx == 0) {
			if (!canStepOver()) {
				return;
			}
			fController.debugStepOver();
			return;
		}
		if (!canStepReturn()) {
			return;
		}
		fController.debugStepToFrame(frames.get(targetIdx).getPosition());
		return;
	}
	
	
	@Override
	public boolean hasStackFrames() throws DebugException {
		return isSuspended();
	}
	
	@Override
	public IStackFrame[] getStackFrames() {
		if (!isSuspended()) {
			return NO_FRAMES;
		}
		synchronized (fFramesLock) {
			return fFrames;
		}
	}
	
	@Override
	public IStackFrame getTopStackFrame() {
		if (!isSuspended()) {
			return null;
		}
		synchronized (fFramesLock) {
			return (fFrames.length > 0) ? fFrames[0] : null;
		}
	}
	
	@Override
	public IBreakpoint[] getBreakpoints() {
		final IBreakpoint breakpoint;
		synchronized (fFramesLock) {
			breakpoint = (fFrames.length > 0) ? (IBreakpoint) fFrames[0].getAdapter(IBreakpoint.class) : null;
		}
		return (breakpoint != null) ? new IBreakpoint[] { breakpoint } : null;
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (IRThread.class.equals(required)) {
			return this;
		}
		if (IRStackFrame.class.equals(required)) {
			return getTopStackFrame();
		}
		return super.getAdapter(required);
	}
	
}
