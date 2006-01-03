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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;

import de.walware.statet.base.StatetPlugin;


/**
 * Provides <code>IProcess</code> for a <code>ToolController</code>.
 */
public class ToolProcess extends PlatformObject implements IProcess {

	
	private ILaunch fLaunch;
	private ToolController fController;
	
	private Map<String, String> fAttributes;
	private boolean fCaptureOutput;
	
	protected int fExitValue = 0;
	
	
	public ToolProcess(ILaunch launch, ToolController controller) {
		
		fLaunch = launch;
		fController = controller;
		fAttributes = new HashMap<String, String>();
		
		String captureOutput = launch.getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT);
		fCaptureOutput = !("false".equals(captureOutput)); //$NON-NLS-1$
		
		launch.addProcess(this);
	}
	
	public String getLabel() {
		
		return fController.getName();
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
		
		fController.terminate();
	}

	public boolean isTerminated() {
		
		return fController.isTerminated();
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

}
