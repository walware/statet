/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.runtime;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.nico.runtime.ToolController.ToolStatus;


/**
 * Provides <code>IProcess</code> for a <code>ToolController</code>.
 */
public class ToolProcess extends PlatformObject implements IProcess {

	
	/** 
	 * Constant for detail of a DebugEvent, signalising that
	 * the process/controller started to work/calculate.
	 * 
	 * Applicable for DebugEvents of kind <code>MODEL_SPECIFIC</code>.
	 * The status can be ended by another status event or by a
	 * DebugEvent of kind <code>TERMINATE</code>.
	 */
	public static final int STATUS_CALCULATE = 1;

	/** 
	 * Constant for detail of a DebugEvent, signalising that
	 * the process/controller switched into idle mode.
	 * 
	 * Applicable for DebugEvents of kind <code>MODEL_SPECIFIC</code>.
	 * The status can be ended by another status event or by a
	 * DebugEvent of kind <code>TERMINATE</code>.
	 */
	public static final int STATUS_IDLE = 2;

	/** 
	 * Constant for detail of a DebugEvent, signalising that
	 * the process/controller was paused.
	 * 
	 * Applicable for DebugEvents of kind <code>MODEL_SPECIFIC</code>.
	 * The status can be ended by another status event or by a
	 * DebugEvent of kind <code>TERMINATE</code>.
	 */
	public static final int STATUS_QUEUE_PAUSE = 4;
	
	
	private ILaunch fLaunch;
	private String fName;
	private ToolController fController;
	
	private Map<String, String> fAttributes;
	private boolean fCaptureOutput;
	
	private volatile boolean fIsTerminated = false;
	protected int fExitValue = 0;
	
	
	public ToolProcess(ILaunch launch, String name) {
		
		fLaunch = launch;
		fName = name;
		fAttributes = new HashMap<String, String>();
		
		String captureOutput = launch.getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT);
		fCaptureOutput = !("false".equals(captureOutput)); //$NON-NLS-1$
		
		launch.addProcess(this);
		fireEvent(new DebugEvent(this, DebugEvent.CREATE));
	}
	
	public void setController(ToolController controller) {
		
		fController = controller;

	}
	
	public String getLabel() {
		
		return fName;
	}

	public ILaunch getLaunch() {
		
		return fLaunch;
	}
	
	public ToolController getController() {
		
		return fController;
	}

	public IStreamsProxy getStreamsProxy() {
		
		return (fCaptureOutput) ? fController.getStreams() : null;
	}

	
	public void setAttribute(String key, String value) {
		
		fAttributes.put(key, value);
	}

	public String getAttribute(String key) {
		
		return fAttributes.get(key);
	}


//	public Object getAdapter(Class adapter) {
//
//		return super.getAdapter(adapter);
//	}


	public boolean canTerminate() {

		return (!isTerminated());
	}

	public void terminate() throws DebugException {
		
		if (fController != null) {
			fController.terminate();
		}
	}

	public boolean isTerminated() {
		
		return fIsTerminated;
	}
	
	public int getExitValue() throws DebugException {
		
		if (!isTerminated()) {
			throw new DebugException(new Status(
					IStatus.ERROR, StatetPlugin.ID, 0,
					"Exit value is not available until process terminates.",
					null));
		}
		return fExitValue;
	}

	
	void controllerStatusChanged(ToolStatus oldStatus, ToolStatus newStatus) {

		switch(newStatus) {
		
		case STARTED_CALCULATING:
			fireEvent(new DebugEvent(ToolProcess.this, DebugEvent.MODEL_SPECIFIC, STATUS_CALCULATE));
			break;
		case STARTED_IDLE:
			fireEvent(new DebugEvent(ToolProcess.this, DebugEvent.MODEL_SPECIFIC, STATUS_IDLE));
			break;
		case STARTED_PAUSED:
			fireEvent(new DebugEvent(ToolProcess.this, DebugEvent.MODEL_SPECIFIC, STATUS_QUEUE_PAUSE));
			break;
			
		case TERMINATED:
			fIsTerminated = true;
			fireEvent(new DebugEvent(ToolProcess.this, DebugEvent.TERMINATE));
			fController = null;
			break;
		}
	}
	
	/**
	 * Fires the given debug event.
	 * 
	 * @param event debug event to fire
	 */
	protected void fireEvent(DebugEvent event) {
		DebugPlugin manager= DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(new DebugEvent[]{event});
		}
	}

}
