/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.actions;

import org.eclipse.jface.action.Action;

import de.walware.statet.nico.runtime.ToolController;
import de.walware.statet.nico.runtime.ToolProcess;
import de.walware.statet.nico.runtime.ToolController.IPauseRequestListener;
import de.walware.statet.nico.ui.NicoMessages;
import de.walware.statet.ui.StatetImages;


/**
 * 
 */
public class PauseAction extends Action implements IPauseRequestListener {
	
	
	private ToolProcess fProcess;
	
	
	public PauseAction() {
		
		setText(NicoMessages.PauseAction_name);
		setToolTipText(NicoMessages.PasteSubmitAction_tooltip);
		setImageDescriptor(StatetImages.DESC_LOCTOOL_PAUSE);
		setDisabledImageDescriptor(StatetImages.DESC_LOCTOOLD_PAUSE);
		setChecked(false);
		setEnabled(false);
	}
	
	public void run() {
		
		ToolController controller = null;
		if (fProcess == null 
				|| (controller = fProcess.getController()) == null) {
			return;
		}
		controller.pause(isChecked());
	}

	/**
	 * 
	 * @param process must not be <code>null</code>.
	 */
	public void connect(ToolProcess process) {
		
		fProcess = process;
		ToolController controller;
		if (!fProcess.isTerminated() && (controller = fProcess.getController()) != null) {
			setEnabled(true);
			setChecked(controller.isPaused());
			controller.addPauseRequestListener(this);
		}
	}
	
	public void disconnect() {
		
		disconnectProcess();
		setEnabled(false);
		setChecked(false);
	}
	
	public void pauseRequested() {
		
		setChecked(true);
	}

	public void unpauseRequested() {
		
		setChecked(false);
	}
	
	private void disconnectProcess() {
		
		ToolController controller;
		if (fProcess != null && (controller = fProcess.getController()) != null) {
			controller.removePauseRequestListener(this);
		}
		fProcess = null;
	}
	
	public void dispose() {
		
		disconnectProcess();
	}
	
}
