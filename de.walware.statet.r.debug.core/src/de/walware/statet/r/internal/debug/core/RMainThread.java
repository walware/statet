/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
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

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolService;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.runtime.ToolController.IToolStatusListener;
import de.walware.statet.nico.core.runtime.ToolStatus;

import de.walware.rj.server.dbg.CallStack;
import de.walware.rj.server.dbg.DbgRequest;
import de.walware.rj.server.dbg.Frame;
import de.walware.rj.server.dbg.FrameRef;

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
						Frame dbgFrame = stack.getFrames().get(i);
						String call;
						if (i == 0) {
							call = "[Console]";
						}
						else if (dbgFrame.getCall() != null) {
							call = dbgFrame.getCall();
							if (special && i+2 < l) {
								int flag = (dbgFrame.getFlags() & 0xff);
								switch (flag) {
								case CallStack.FLAG_SOURCE:
									call = "[Sourcing Script]";
//									dbgFrame.addFlags(CallStack.FLAG_NOSTEPPING);
									break;
								case CallStack.FLAG_COMMAND:
									call = "[Running Command]";
									break;
								default:
									flag = 0;
								}
								if (flag != 0) {
									while (i + 1 < l) {
										final Frame nextFrame = stack.getFrames().get(i + 1);
										if ((nextFrame.getFlags() & 0xff) != ++flag) {
											break;
										}
										dbgFrame = nextFrame;
										i++;
									}
									if ((flag & 0xf0) == CallStack.FLAG_COMMAND
											&& frameStack.size() == 1) {
										frameStack.remove(0);
									}
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
	
	
	protected void exec(final DbgRequest request) throws DebugException {
		try {
			fController.exec(request);
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
			exec(new DbgRequest.Resume());
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
		if (canStepInto()) {
			exec(new DbgRequest.StepInto());
		}
	}
	
	@Override
	public void stepOver() throws DebugException {
		if (canStepOver()) {
			exec(new DbgRequest.StepOver());
		}
	}
	
	@Override
	public void stepReturn() throws DebugException {
		if (canStepReturn()) {
			stepToFrame(null, 1);
		}
	}
	
	/**
	 * 
	 * @param refFrame reference frame, if <code>null</code> current top frame is used
	 * @param relIdx steps to move (number of index of stack frames, not positions)
	 * @throws DebugException 
	 */
	public void stepToFrame(final RStackFrame refFrame, final int relIdx) throws DebugException {
		ImList<RStackFrame> frames;
		synchronized (fFramesLock) {
			frames= ImCollections.newList(fFrames);
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
			if (canStepOver()) {
				exec(new DbgRequest.StepOver());
			}
			return;
		}
		if (canStepReturn()) {
			exec(new DbgRequest.StepReturn(
					new FrameRef.ByPosition(frames.get(targetIdx).getPosition()) ));
		}
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
