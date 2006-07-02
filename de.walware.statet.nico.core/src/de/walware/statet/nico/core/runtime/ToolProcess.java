/*******************************************************************************
 * Copyright (c) 2005-2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import static de.walware.statet.nico.core.runtime.ToolController.ToolStatus.STARTED_CALCULATING;
import static de.walware.statet.nico.core.runtime.ToolController.ToolStatus.STARTED_IDLE;
import static de.walware.statet.nico.core.runtime.ToolController.ToolStatus.STARTED_PAUSED;
import static de.walware.statet.nico.core.runtime.ToolController.ToolStatus.STARTING;
import static de.walware.statet.nico.core.runtime.ToolController.ToolStatus.TERMINATED;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;

import de.walware.statet.nico.core.NicoCoreMessages;
import de.walware.statet.nico.core.internal.NicoPlugin;
import de.walware.statet.nico.core.runtime.ToolController.IToolStatusListener;
import de.walware.statet.nico.core.runtime.ToolController.ToolStatus;


/**
 * Provides <code>IProcess</code> for a <code>ToolController</code>.
 */
public class ToolProcess<WorkspaceType extends ToolWorkspace> 
		extends PlatformObject implements IProcess, IToolStatusListener {

	
	public static final int MASK_STATUS = (1 << 6);
	public static final int MASK_QUEUE_ENTRY = (2 << 6);
	
	
	/** 
	 * Constant for detail of a DebugEvent, signalising that
	 * the process/controller started to work/calculate.
	 * 
	 * Applicable for DebugEvents of kind <code>MODEL_SPECIFIC</code>.
	 * The status can be ended by another status event or by a
	 * DebugEvent of kind <code>TERMINATE</code>.
	 */
	public static final int STATUS_CALCULATE = MASK_STATUS | 1;

	/** 
	 * Constant for detail of a DebugEvent, signalising that
	 * the process/controller switched into idle mode.
	 * 
	 * Applicable for DebugEvents of kind <code>MODEL_SPECIFIC</code>.
	 * The status can be ended by another status event or by a
	 * DebugEvent of kind <code>TERMINATE</code>.
	 */
	public static final int STATUS_IDLE = MASK_STATUS | 2;

	/** 
	 * Constant for detail of a DebugEvent, signalising that
	 * the process/controller was paused.
	 * 
	 * Applicable for DebugEvents of kind <code>MODEL_SPECIFIC</code>.
	 * The status can be ended by another status event or by a
	 * DebugEvent of kind <code>TERMINATE</code>.
	 */
	public static final int STATUS_QUEUE_PAUSE = MASK_STATUS | 3;
	
	
	/**
	 * Constant for detail of a DebugEvent, signalising that
	 * a entry (IToolRunnable) was added to the queue.
	 * <p>
	 * The queue entry (<code>IToolRunnable</code>) is attached as
	 * data to this event. The source of the event is the ToolProcess.
	 * <p>
	 * Usage: Events of this type are sended by the ToolProcess/its queue.
	 * The constant is applicable for DebugEvents of kind 
	 * <code>MODEL_SPECIFIC</code>.</p>
	 */
	public static final int QUEUE_ENTRIES_ADDED = MASK_QUEUE_ENTRY | 1;
	
	/**
	 * Constant for detail of a DebugEvent, signalising that
	 * queue has changed e.g. reordered, cleared,... .
	 * <p>
	 * The queue entries (<code>IToolRunnable[]</code>) are attached as
	 * data to this event. The source of the event is the ToolProcess.
	 * <p>
	 * Usage: Events of this type are sended by the ToolProcess/its queue.
	 * The constant is applicable for DebugEvents of kind 
	 * <code>MODEL_SPECIFIC</code>.</p>
	 */
	public static final int QUEUE_COMPLETE_CHANGE = MASK_QUEUE_ENTRY | 2;

	/**
	 * Constant for detail of a DebugEvent, sending the complete queue.
	 * This does not signalising, that the queue has changed.
	 * <p>
	 * The queue entries (<code>IToolRunnable[]</code>) are attached as
	 * data to this event. The source of the event is the ToolProcess.
	 * <p>
	 * Usage: Events of this type are sended by the ToolProcess/its queue.
	 * The constant is applicable for DebugEvents of kind 
	 * <code>MODEL_SPECIFIC</code>.</p>
	 */
	public static final int QUEUE_COMPLETE_INFO = MASK_QUEUE_ENTRY | 3;
	
	/**
	 * Constant for detail of a DebugEvent, signalising that
	 * a entry (IToolRunnable) was removed from the queue and that 
	 * the process/controller started processing the entry.
	 * <p>
	 * The queue entry (<code>IToolRunnable</code>) is attached as
	 * data to this event. The source of the event is the ToolProcess.
	 * <p>
	 * Usage: Events of this type are sended by the ToolProcess/its queue.
	 * The constant is applicable for DebugEvents of kind 
	 * <code>MODEL_SPECIFIC</code>.</p>
	 */
	public static final int QUEUE_ENTRY_STARTED_PROCESSING = MASK_QUEUE_ENTRY | 4;
	
	
	private final ILaunch fLaunch;
	private final String fName;
	private String fToolLabelShort;
	
	private ToolController<?, WorkspaceType> fController;
	private Queue fQueue;
	private History fHistory;
	private WorkspaceType fWorkspaceData;
	
	private final Map<String, String> fAttributes;
	private final boolean fCaptureOutput;
	
	private volatile boolean fIsTerminated = false;
	protected volatile int fExitValue = 0;
	
	
	public ToolProcess(ILaunch launch, String name) {
		
		fLaunch = launch;
		fName = name;
		fAttributes = new HashMap<String, String>(5);
		computeToolLabel();
		doSetAttribute(IProcess.ATTR_PROCESS_LABEL, computeDynamicLabel(ToolStatus.STARTING));
		
		String captureOutput = launch.getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT);
		fCaptureOutput = !("false".equals(captureOutput)); //$NON-NLS-1$

	}
	
	public void init(ToolController<?, WorkspaceType> controller) {
		
		fController = controller;
		fHistory = new History(this);
		fQueue = fController.getQueue();
		fWorkspaceData = fController.fWorkspaceData;

		fLaunch.addProcess(this);
		fireEvent(new DebugEvent(ToolProcess.this, DebugEvent.CREATE));
	}
	
	public String getLabel() {
		
		return fName;
	}
	
	public String getToolLabel(boolean longLabel) {
		
		if (longLabel) {
			return fToolLabelShort + ' ' + getLabel();
		}
		return fToolLabelShort;
	}
	
	private void computeToolLabel() {
		
		StringBuilder s = new StringBuilder();
        ILaunchConfiguration config = getLaunch().getLaunchConfiguration();
        if (config != null) {
        	String type = null;
            try {
                type = config.getType().getName();
            } catch (CoreException e) {
            }
            s.append(config.getName());
            if (type != null) {
                s.append(" ["); //$NON-NLS-1$
                s.append(type);
                s.append("]"); //$NON-NLS-1$
            }
            
            fToolLabelShort = s.toString();
        }
        else {
        	fToolLabelShort = "[-]"; //$NON-NLS-1$
        }
	}
	
	private String computeDynamicLabel(ToolStatus status) {
		
		StringBuilder s = new StringBuilder(fToolLabelShort);
		s.append(' ');
		s.append(getLabel());
		
        s.append(" <"); //$NON-NLS-1$
        switch(status) {
        case STARTING:
        	s.append(NicoCoreMessages.Status_Starting_label);
        	break;
        case STARTED_IDLE:
        	s.append(NicoCoreMessages.Status_StartedIdle_label);
        	break;
        case STARTED_PAUSED:
        	s.append(NicoCoreMessages.Status_StartedPaused_label);
        	break;
        case STARTED_CALCULATING:
        	s.append(NicoCoreMessages.Status_StartedCalculating_label);
        	break;
        case TERMINATED:
        	s.append(NicoCoreMessages.Status_Terminated_label);
        	break;
        }
        s.append('>');
        
		return s.toString();
	}

	public ILaunch getLaunch() {
		
		return fLaunch;
	}
	
	public ToolController getController() {
		
		return fController;
	}
	
	public History getHistory() {
		
		return fHistory;
	}
	
	public Queue getQueue() {
		
		return fQueue;
	}
	
	public IStreamsProxy getStreamsProxy() {
		
		return (fCaptureOutput && fController != null) ? fController.getStreams() : null;
	}

	
	public WorkspaceType getWorkspaceData() {

		return fWorkspaceData;
	}

	public void setAttribute(String key, String value) {
		
		DebugEvent event = doSetAttribute(key, value);
		if (event != null) {
			fireEvent(event);
		}
	}
	
	private DebugEvent doSetAttribute(String key, String value) {
		
		synchronized (fAttributes) {
			String oldValue = fAttributes.put(key, value);
			if (oldValue == value 
					|| (oldValue != null && oldValue.equals(value)) ) {
				return null;
			}
			DebugEvent event = new DebugEvent(ToolProcess.this, DebugEvent.CHANGE);
			event.setData(new String[] { key, oldValue, value });
			return event;
		}
	}

	public String getAttribute(String key) {
		
		synchronized (fAttributes) {
			return fAttributes.get(key);
		}
	}


	@Override
	public Object getAdapter(Class adapter) {

		if (adapter.equals(IProcess.class)) {
			return this;
		}
		if (adapter.equals(IDebugTarget.class)) {
			ILaunch launch = getLaunch();
			IDebugTarget[] targets = launch.getDebugTargets();
			for (int i = 0; i < targets.length; i++) {
				if (this.equals(targets[i].getProcess())) {
					return targets[i];
				}
			}
			return null;
		}
		if (adapter.equals(ILaunch.class)) {
			return getLaunch();
		}
		return super.getAdapter(adapter);
	}


	public boolean canTerminate() {

		return (!isTerminated());
	}

	public void terminate() throws DebugException {
		
		if (fController != null) {
			fController.asyncTerminate();
			// TODO: add monitor / force terminate
		}
	}

	public boolean isTerminated() {
		
		return fIsTerminated;
	}
	
	public int getExitValue() throws DebugException {
		
		if (!isTerminated()) {
			throw new DebugException(new Status(
					IStatus.ERROR, NicoPlugin.PLUGIN_ID, 0,
					"Exit value is not available until process terminates.", //$NON-NLS-1$
					null));
		}
		return fExitValue;
	}

	/** Called by Controller */
	public void controllerStatusChanged(ToolStatus oldStatus, ToolStatus newStatus, List<DebugEvent> eventCollection) {

		switch(newStatus) {
		
		case STARTED_CALCULATING:
			eventCollection.add(new DebugEvent(ToolProcess.this, 
					DebugEvent.MODEL_SPECIFIC, STATUS_CALCULATE) );
			break;
		case STARTED_IDLE:
			eventCollection.add(new DebugEvent(ToolProcess.this, 
					DebugEvent.MODEL_SPECIFIC, STATUS_IDLE) );
			break;
		case STARTED_PAUSED:
			eventCollection.add(new DebugEvent(ToolProcess.this, 
					DebugEvent.MODEL_SPECIFIC, STATUS_QUEUE_PAUSE) );
			break;
			
		case TERMINATED:
			fIsTerminated = true;
			fController = null;
			eventCollection.add(new DebugEvent(ToolProcess.this, DebugEvent.TERMINATE) );
			break;
		}
		
		synchronized (fAttributes) {
			DebugEvent nameEvent = doSetAttribute(IProcess.ATTR_PROCESS_LABEL, computeDynamicLabel(newStatus));
			if (nameEvent != null) {
				eventCollection.add(nameEvent);
			}
		}
	}

	
	/**
	 * Fires the given debug events.
	 * 
	 * @param event array with debug events to fire
	 */
	protected void fireEvent(DebugEvent event) {
				
		DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(new DebugEvent[] { event });
		}
	}

}
