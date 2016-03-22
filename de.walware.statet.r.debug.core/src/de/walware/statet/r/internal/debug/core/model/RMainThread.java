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

import static de.walware.statet.r.console.core.RWorkspace.RESOLVE_UPTODATE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.debug.core.eval.IEvaluationListener;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolService;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.runtime.ToolController.IToolStatusListener;
import de.walware.statet.nico.core.runtime.ToolStatus;

import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RLanguage;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.data.defaultImpl.RLanguageImpl;
import de.walware.rj.data.defaultImpl.RReferenceImpl;
import de.walware.rj.eclient.FQRObjectRef;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.server.dbg.CallStack;
import de.walware.rj.server.dbg.DbgRequest;
import de.walware.rj.server.dbg.Frame;
import de.walware.rj.server.dbg.FrameRef;
import de.walware.rj.services.IFQRObjectRef;
import de.walware.rj.services.RService;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.console.core.RWorkspace.ICombinedREnvironment;
import de.walware.statet.r.console.core.util.LoadReferenceRunnable;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.debug.core.IRStackFrame;
import de.walware.statet.r.debug.core.IRThread;
import de.walware.statet.r.debug.core.breakpoints.IRBreakpoint;
import de.walware.statet.r.debug.core.breakpoints.IRBreakpointStatus;
import de.walware.statet.r.internal.debug.core.Messages;
import de.walware.statet.r.internal.debug.core.RDebugCorePlugin;
import de.walware.statet.r.internal.debug.core.RJTmp;
import de.walware.statet.r.internal.debug.core.eval.ExpressionManager;
import de.walware.statet.r.nico.AbstractRDbgController;


@NonNullByDefault
public class RMainThread extends RDebugElement implements IRThread,
		IToolStatusListener {
	
	
	private static final byte SUSPENDED= 1;
	private static final byte RUN_EVALUATING_SYSTEM= 2;
	private static final byte RUN_EVALUATING_USER= 3;
	private static final byte RUN_STEPPING= 4;
	private static final byte RUN_OTHER= 5;
	private static final byte TERMINATED= 6;
	
	private static final byte getRunState(final int detail) {
		switch (detail) {
		case DebugEvent.STEP_INTO:
		case DebugEvent.STEP_OVER:
		case DebugEvent.STEP_RETURN:
			return RUN_STEPPING;
		case DebugEvent.EVALUATION:
			return RUN_EVALUATING_USER;
		case DebugEvent.EVALUATION_IMPLICIT:
			return RUN_EVALUATING_SYSTEM;
		default:
			return RUN_OTHER;
		}
	}
	
	
	private static class EnvItem {
		
		final Long handle;
		
		@Nullable Long prevHandle;
		
		@Nullable ICombinedREnvironment element;
		@Nullable REnvValue value;
		
		public EnvItem(final Long handle) {
			this.handle= handle;
			this.prevHandle= handle;
		}
		
	}
	
	
	abstract class AccessDataRunnable<V extends RObject> implements ISystemRunnable {
		
		
		private boolean cancel;
		
		private V data;
		
		
		public AccessDataRunnable() {
		}
		
		@Override
		public String getTypeId() {
			return "r/dbg/stackframe/loadData"; //$NON-NLS-1$
		}
		
		@Override
		public String getLabel() {
			return Messages.DebugContext_UpdateVariables_task;
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return (tool == getTool());
		}
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			switch (event) {
			case REMOVING_FROM:
				return this.cancel;
			case MOVING_FROM:
				return false;
			case BEING_ABANDONED:
			case FINISHING_OK:
			case FINISHING_ERROR:
			case FINISHING_CANCEL:
				synchronized (AccessDataRunnable.this) {
					AccessDataRunnable.this.notifyAll();
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
			final AbstractRDbgController controller= (AbstractRDbgController) service;
			if (!controller.isSuspended() || getRequiredStamp() != controller.getChangeStamp()) {
				return;
			}
			try {
				this.data= doRun(controller, monitor);
			}
			catch (final UnexpectedRDataException e) {
				throw new CoreException(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID,
						"Unexpected state", e ));
			}
		}
		
		protected final RMainThread getThread() {
			return RMainThread.this;
		}
		
		
		protected abstract int getRequiredStamp();
		
		protected abstract V doRun(IRToolService r,
				IProgressMonitor monitor) throws CoreException, UnexpectedRDataException;
		
	}
	
	
	private static final @NonNull RStackFrame[] NO_FRAMES= new RStackFrame[0];
	
	private static final @NonNull IRBreakpoint[] NO_BREAKPOINTS= new IRBreakpoint[0];
	
	
	private final AbstractRDbgController controller;
	
	private final String name;
	
	private final Object stateLock= new Object();
	private volatile byte state;
	
	private final Object suspendLock= new Object();
	private int stamp;
	private @Nullable List<RStackFrame> framesStack;
	private @NonNull RStackFrame[] frames;
	private boolean stampChanged;
	
	private Map<Long, @Nullable EnvItem> envItems;
	private @Nullable Map<Long, @Nullable EnvItem> envPrevItems;
	
	private @Nullable RReference rGlobelEnvRef;
	private @Nullable RReference rjTmpEnvRef;
	
	private final ExpressionManager expressionManager;
	
	
	public RMainThread(final RDebugTarget target, final AbstractRDbgController controller,
			final String name) {
		super(target);
		this.controller= controller;
		this.name= name;
		
		this.expressionManager= new ExpressionManager(this);
		
		init();
	}
	
	
	protected void init() {
		synchronized (this.suspendLock) {
			this.frames= NO_FRAMES;
			this.envItems= Collections.EMPTY_MAP;
		}
		
		this.controller.addSuspendUpdateRunnable(new ISystemRunnable() {
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
				return (tool == RMainThread.this.controller.getTool());
			}
			@Override
			public boolean changed(final int event, final ITool process) {
				return true;
			}
			@Override
			public void run(final IToolService service,
					final IProgressMonitor monitor) throws CoreException {
				checkInit(monitor);
				aboutToSuspend(RMainThread.this.controller.getSuspendEnterDetail(),
						RMainThread.this.controller.getSuspendEnterData(), monitor );
			}
		});
		this.controller.addToolStatusListener(this);
	}
	
	public ExpressionManager getExpressionManager() {
		return this.expressionManager;
	}
	
	
	@Override
	public void controllerStatusChanged(final ToolStatus oldStatus,
			final ToolStatus newStatus, final List<DebugEvent> eventCollection) {
		synchronized (this.stateLock) {
			if (this.state == TERMINATED) {
				return;
			}
			int detail= 0;
			switch (newStatus) {
			case STARTED_SUSPENDED:
				if (this.stampChanged) {
					detail= this.controller.getSuspendEnterDetail();
					if (detail == DebugEvent.UNSPECIFIED && this.state == RUN_STEPPING) {
						detail= DebugEvent.STEP_END;
					}
					this.expressionManager.updateExpressions(eventCollection);
				}
				else {
					detail= DebugEvent.EVALUATION_IMPLICIT;
				}
				this.state= SUSPENDED;
				eventCollection.add(new DebugEvent(this, DebugEvent.SUSPEND, detail));
				if (this.stampChanged) {
					eventCollection.add(new DebugEvent(this, DebugEvent.CHANGE, DebugEvent.CONTENT));
				}
				break;
			case TERMINATED:
				this.expressionManager.cleanExpressions(eventCollection);
				break;
			case STARTED_PAUSED:
				if (this.controller.isSuspended()) {
					break; // also pause debugging
				}
				//$FALL-THROUGH$
			case STARTED_IDLING:
				if (this.state != RUN_OTHER) {
					detail= (this.controller.getSuspendExitDetail() & (DebugEvent.UNSPECIFIED | DebugEvent.CLIENT_REQUEST));
					this.state= RUN_OTHER;
					eventCollection.add(new DebugEvent(this, DebugEvent.RESUME, detail));
					
					this.expressionManager.clearCache(0, null);
				}
				break;
			default: // PROCESSING
				detail= this.controller.getSuspendExitDetail();
				final byte newState= getRunState(detail);
				if (newState > this.state || (newState == this.state && newState == RUN_STEPPING)) {
					this.state= newState;
					eventCollection.add(new DebugEvent(this, DebugEvent.RESUME, detail));
					
					this.expressionManager.clearCache(0, null);
				}
				break;
			}
		}
	}
	
	
	protected void checkInit(final IProgressMonitor monitor) {
		try {
			if (this.rjTmpEnvRef == null) {
				{	final RObject rObject= this.controller.evalData(RJTmp.ENV,
							null, 0, RService.DEPTH_REFERENCE, monitor );
					this.rjTmpEnvRef= RDataUtil.checkRReference(rObject, RObject.TYPE_ENV);
				}
				{	final RObject rObject= this.controller.evalData(".GlobalEnv", //$NON-NLS-1$
							null, 0, RService.DEPTH_REFERENCE, monitor );
					this.rGlobelEnvRef= RDataUtil.checkRReference(rObject, RObject.TYPE_ENV);
				}
			}
		}
		catch (final CoreException | UnexpectedRDataException e) {
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
					"An error occurred when initializing debug target.", e));
		}
	}
	
	protected void aboutToSuspend(final int suspendDetail, final Object suspendData,
			final IProgressMonitor monitor) throws CoreException {
		final int prevStamp;
		final int stamp= this.controller.getChangeStamp();
		List<RStackFrame> prevFramesStack;
		List<RStackFrame> newFramesStack= null;
		RStackFrame[] newFrames= NO_FRAMES;
		final Map<Long, EnvItem> newEnvItems= new HashMap<>();
		synchronized (this.suspendLock) {
			prevStamp= this.stamp;
			prevFramesStack= this.framesStack;
		}
		try {
			final CallStack rStack= this.controller.getCallStack(monitor);
			if (rStack != null) {
				final int l= rStack.getFrames().size();
				if (l > 0) {
					final boolean special= DebugPlugin.isUseStepFilters();
					final List<RStackFrame> eStack= new ArrayList<>(l);
					int startIdx= 0;
					for (int i= 0; i < l; i++) {
						Frame dbgFrame= rStack.getFrames().get(i);
						String call;
						Long handle= Long.valueOf(dbgFrame.getHandle());
						if (i == 0) {
							call= "[Console]";
							if (handle == 0) {
								handle= Long.valueOf(this.rGlobelEnvRef.getHandle());
							}
						}
						else if (dbgFrame.getCall() != null) {
							call= dbgFrame.getCall();
							if (special && i + 2 < l) {
								int flag= (dbgFrame.getFlags() & 0xff);
								switch (flag) {
								case CallStack.FLAG_SOURCE:
									call= "[Sourcing Script]";
//									dbgFrame.addFlags(CallStack.FLAG_NOSTEPPING);
									break;
								case CallStack.FLAG_COMMAND:
									call= "[Running Command]";
									break;
								default:
									flag= 0;
								}
								if (flag != 0) {
									while (i + 1 < l) {
										final Frame nextFrame= rStack.getFrames().get(i + 1);
										if ((nextFrame.getFlags() & 0xff) != ++flag) {
											break;
										}
										dbgFrame= nextFrame;
										i++;
									}
									if ((flag & 0xf0) == CallStack.FLAG_COMMAND
											&& eStack.size() == 1) {
										startIdx= 1;
									}
								}
							}
						}
						else {
							call= "[Unkown]";
						}
						
						String fileName= dbgFrame.getFileName();
						if (fileName != null) {
							int idx= fileName.lastIndexOf('/');
							{	final int idx2= fileName.lastIndexOf('\\');
								if (idx2 > idx) {
									idx= idx2;
								}
							}
							if (idx >= 0) {
								fileName= fileName.substring(idx + 1);
							}
						}
						final IRBreakpointStatus breakpoint= (dbgFrame.isTopFrame()
								&& suspendData instanceof IRBreakpointStatus) ?
										(IRBreakpointStatus) suspendData : null;
						
						final EnvItem envItem= new EnvItem(handle);
						newEnvItems.put(envItem.handle, envItem);
						
						if (prevFramesStack != null) {
							if (eStack.size() < prevFramesStack.size()) {
								final RStackFrame prevFrame= prevFramesStack.get(eStack.size());
								final EnvItem prevItem= getEnvItem(handle,
										prevFrame.getHandle() );
								if ((prevItem == null || prevItem.handle.longValue() == prevFrame.getHandle().longValue())
										&& prevFrame.update(stamp, dbgFrame, handle, call, fileName, breakpoint)) {
									if (prevItem != null) {
										envItem.prevHandle= prevItem.handle;
									}
									eStack.add(prevFrame);
									continue;
								}
							}
							if (stamp == prevStamp) {
								RDebugCorePlugin.log(new Status(IStatus.WARNING, RDebugCorePlugin.PLUGIN_ID,
										"Frame stack changed, but controller.changeStamp is unchanged." ));
							}
							prevFramesStack= null;
						}
						{	final RStackFrame newFrame= new RStackFrame(getDebugTarget(), this,
									stamp, dbgFrame, handle, call, fileName, breakpoint );
							eStack.add(newFrame);
							continue;
						}
					}
					
					{	newFramesStack= eStack;
						newFrames= new RStackFrame[eStack.size() - startIdx];
						final int endIdx= eStack.size() - 1;
						for (int i= 0; i < newFrames.length; i++) {
							newFrames[i]= eStack.get(endIdx - i);
						}
					}
				}
			}
		}
		finally {
			synchronized (this.suspendLock) {
				this.stamp= stamp;
				this.framesStack= newFramesStack;
				this.frames= newFrames;
				
				this.envPrevItems= this.envItems;
				this.envItems= newEnvItems;
				
				this.stampChanged= (stamp != prevStamp);
			}
			
			if (this.frames.length > 0) {
				this.frames[0].loadContext(this.controller, monitor);
			}
			
			if (this.stampChanged) {
				this.expressionManager.clearCache(stamp, monitor);
			}
		}
	}
	
	private @Nullable EnvItem getEnvItem(final Long first, final Long second) {
		synchronized (this.suspendLock) {
			EnvItem envItem= this.envItems.get(first);
			if (envItem == null && second != first) {
				envItem= this.envItems.get(second);
			}
			return envItem;
		}
	}
	
	
	public final RProcess getTool() {
		return this.controller.getTool();
	}
	
	@Override
	public String getName() throws DebugException {
		return this.name;
	}
	
	@Override
	public int getPriority() throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID,
				DebugException.NOT_SUPPORTED, "Not supported.", null));
	}
	
	public int getCurrentStamp() {
		synchronized (this.suspendLock) {
			return this.stamp;
		}
	}
	
	
	@Override
	public boolean isTerminated() {
		return (this.state == TERMINATED);
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
		synchronized (this.stateLock) {
			this.state= TERMINATED;
		}
		synchronized (this.suspendLock) {
			this.frames= new RStackFrame[0]; // use other instance
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
		final byte state= this.state;
		return (state >= SUSPENDED && state <= RUN_EVALUATING_USER);
	}
	
	@Override
	public boolean canSuspend() {
		final byte state= this.state;
		return (state >= RUN_STEPPING && state <= RUN_OTHER);
	}
	
	@Override
	public boolean canResume() {
		final byte state= this.state;
		return (state >= SUSPENDED && state <= RUN_EVALUATING_USER);
	}
	
	@Override
	public void suspend() throws DebugException {
		if (canSuspend()) {
			this.controller.debugSuspend();
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
		return (this.state == RUN_STEPPING);
	}
	
	@Override
	public boolean canStepInto() {
		final IStackFrame topFrame= getTopStackFrame();
		return (topFrame != null && topFrame.canStepInto());
	}
	
	@Override
	public boolean canStepOver() {
		final IStackFrame topFrame= getTopStackFrame();
		return (topFrame != null && topFrame.canStepOver());
	}
	
	@Override
	public boolean canStepReturn() {
		final IStackFrame topFrame= getTopStackFrame();
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
	public void stepToFrame(final @Nullable RStackFrame refFrame, final int relIdx)
			throws DebugException {
		ImList<RStackFrame> frames;
		synchronized (this.suspendLock) {
			frames= ImCollections.newList(this.frames);
		}
		if (frames.isEmpty()) {
			return;
		}
		final int refIdx= (refFrame != null) ? frames.indexOf(refFrame) : 0;
		final int targetIdx= refIdx + relIdx;
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
	public @NonNull IRStackFrame[] getStackFrames() {
		if (!isSuspended()) {
			return NO_FRAMES;
		}
		synchronized (this.suspendLock) {
			return this.frames;
		}
	}
	
	public int checkStackFrame(final IRStackFrame frame) {
		synchronized (this.suspendLock) {
			for (int i= 0; i < this.frames.length; i++) {
				if (this.frames[i] == frame) {
					return this.stamp;
				}
			}
			return 0;
		}
	}
	
	@Override
	public @Nullable IRStackFrame getTopStackFrame() {
		if (!isSuspended()) {
			return null;
		}
		synchronized (this.suspendLock) {
			return (this.frames.length > 0) ? this.frames[0] : null;
		}
	}
	
	
	public @Nullable IFQRObjectRef createElementRef(@Nullable ICombinedRElement element, final int stamp,
			final IProgressMonitor monitor) {
		if (stamp != getCurrentStamp()) {
			return null;
		}
		try {
			final List<RElementName> segments= new ArrayList<>();
			while (element != null) {
				if (element.getRObjectType() == RObject.TYPE_ENV) {
					final RReference envRef= verifyEnv((ICombinedREnvironment) element, monitor);
					Collections.reverse(segments);
					final RElementName name= RElementName.create(segments);
					return new FQRObjectRef(this.controller.getTool(), envRef,
							new RLanguageImpl(RLanguage.CALL, name.getDisplayName(RElementName.DISPLAY_EXACT), null) );
				}
				else {
					final RElementName elementName= element.getElementName();
					if (elementName.getNextSegment() != null) {
						if (RJTmp.PKG_NAME.equals(elementName.getScope())
								&& RJTmp.ENV_NAME.getSegmentName().equals(elementName.getSegmentName()) ) {
							segments.add(elementName.getNextSegment());
							Collections.reverse(segments);
							final RElementName name= RElementName.create(segments);
							return new FQRObjectRef(this.controller.getTool(), this.rjTmpEnvRef,
									new RLanguageImpl(RLanguage.CALL, name.getDisplayName(RElementName.DISPLAY_EXACT), null) );
						}
						break;
					}
					segments.add(elementName);
					element= element.getModelParent();
				}
			}
			
			throw new IllegalStateException("Unable to create name.");
		}
		catch (final Exception e) {
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
					"An error occurred when creating R element name to load data.", e));
			return null;
		}
	}
	
	private RReference verifyEnv(final ICombinedREnvironment env, final IProgressMonitor monitor) {
		final RWorkspace workspace= this.controller.getWorkspaceData();
		if (workspace.isUptodate(env)) {
			return new RReferenceImpl(env.getHandle(), RObject.TYPE_ENV, RObject.CLASSNAME_ENV);
		}
		throw new IllegalStateException("Unable to create name.");
	}
	
	
	private EnvItem getEnvItem(final Long handle) {
		EnvItem item= this.envItems.get(handle);
		if (item == null) {
			item= new EnvItem(handle);
			this.envItems.put(handle, item);
		}
		return item;
	}
	
	public ICombinedRElement resolveReference(final ICombinedRElement element, final int stamp) {
		final RReference reference= (RReference) element;
		final EnvItem envItem;
		synchronized (this.suspendLock) {
			if (stamp != this.stamp) {
				return element;
			}
			envItem= getEnvItem(reference.getHandle());
		}
		synchronized (envItem) {
			if (envItem.element != null) {
				return envItem.element;
			}
			final RWorkspace workspace= getDebugTarget().getProcess().getWorkspaceData();
			if (workspace != null) {
				ICombinedRElement resolved;
				resolved= workspace.resolve(reference, 0);
				if (resolved instanceof ICombinedREnvironment
						&& ((ICombinedREnvironment) resolved).getStamp() == stamp) {
					envItem.element= (ICombinedREnvironment) resolved;
					return envItem.element;
				}
				if (resolved != null) {
					return resolved;
				}
				resolved= loadReference(reference, stamp);
				if (resolved instanceof ICombinedREnvironment) {
					envItem.element= (ICombinedREnvironment) resolved;
					return envItem.element;
				}
			}
			return element;
		}
	}
	
	public ICombinedRElement resolveReference(final ICombinedRElement element, final int stamp,
			final IProgressMonitor monitor) throws CoreException {
		final RReference reference= (RReference) element;
		final EnvItem envItem;
		synchronized (this.suspendLock) {
			if (stamp != this.stamp) {
				return element;
			}
			envItem= getEnvItem(reference.getHandle());
		}
		synchronized (envItem) {
			if (envItem.element != null) {
				return envItem.element;
			}
			final RWorkspace workspace= getDebugTarget().getProcess().getWorkspaceData();
			if (workspace != null) {
				ICombinedRElement resolved;
				resolved= workspace.resolve((RReference) element,
						RESOLVE_UPTODATE, 0, monitor );
				if (resolved instanceof ICombinedREnvironment) {
					envItem.element= (ICombinedREnvironment) resolved;
					return envItem.element;
				}
			}
			return element;
		}
	}
	
	public @Nullable REnvValue getEnvValue(final ICombinedREnvironment element, final int stamp) {
		final EnvItem envItem;
		final Map<Long, EnvItem> prevItems;
		synchronized (this.suspendLock) {
			if (stamp != this.stamp) {
				return null;
			}
			envItem= getEnvItem(element.getHandle());
			prevItems= this.envPrevItems;
		}
		synchronized (envItem) {
			return doGetEnvValue(element, stamp, envItem, prevItems);
		}
	}
	
	private REnvValue doGetEnvValue(final ICombinedREnvironment element, final int stamp,
			final EnvItem envItem, final Map<Long, @Nullable EnvItem> prevItems) {
		if (envItem.value != null) {
			return envItem.value;
		}
		
		REnvValue previousValue= null;
		if (prevItems != null) {
			final EnvItem prevItem= prevItems.get(envItem.prevHandle);
			if (prevItem != null) {
				previousValue= prevItem.value;
			}
		}
		envItem.value= new REnvValue(element, this, stamp, previousValue);
		return envItem.value;
	}
	
	
	public <V extends RObject> @Nullable V loadData(final AccessDataRunnable<V> runnable) {
		if (runnable.getRequiredStamp() != getCurrentStamp()) {
			return null;
		}
		final RProcess tool= this.controller.getTool();
		synchronized (runnable) {
			if (tool.getQueue().addHot(runnable).isOK()) {
				try {
					runnable.wait();
					return runnable.data;
				}
				catch (final InterruptedException e) {
					runnable.cancel= true;
					getDebugTarget().getProcess().getQueue().removeHot(runnable);
				}
			}
			return null;
		}
	}
	
	private @Nullable ICombinedRElement loadReference(final RReference reference, final int stamp) {
		final RProcess tool= this.controller.getTool();
		final LoadReferenceRunnable runnable= new LoadReferenceRunnable(reference, tool, stamp,
				Messages.DebugContext_label );
		synchronized (runnable) {
			if (tool.getQueue().addHot(runnable).isOK()) {
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
	
	
	@Override
	public @NonNull IBreakpoint [] getBreakpoints() {
		final IBreakpoint breakpoint;
		synchronized (this.suspendLock) {
			breakpoint= (this.frames.length > 0) ? this.frames[0].getAdapter(IBreakpoint.class) : null;
		}
		return (breakpoint != null) ?
				new @NonNull IBreakpoint[] { breakpoint } :
				NO_BREAKPOINTS;
	}
	
	@Override
	public void evaluate(final String expressionText, final IRStackFrame stackFrame,
			final boolean forceReevaluate, final IEvaluationListener listener) {
		this.expressionManager.evaluate(expressionText, stackFrame, forceReevaluate, listener);
	}
	
	
	@Override
	public <T> @Nullable T getAdapter(final Class<T> type) {
		if (type == IRThread.class) {
			return (T) this;
		}
		if (type == IRStackFrame.class) {
			return (T) getTopStackFrame();
		}
		return super.getAdapter(type);
	}
	
}
