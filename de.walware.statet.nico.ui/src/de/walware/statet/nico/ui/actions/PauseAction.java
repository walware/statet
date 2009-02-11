/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.actions;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jface.action.Action;

import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.StatetImages;

import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.internal.ui.Messages;


/**
 * 
 */
public class PauseAction extends Action implements IDebugEventSetListener {
	
	
	private ToolProcess fProcess;
	
	
	public PauseAction() {
		setText(Messages.PauseAction_name);
		setToolTipText(Messages.PauseAction_tooltip);
		setImageDescriptor(StatetImages.getDescriptor(StatetImages.LOCTOOL_PAUSE));
		setDisabledImageDescriptor(StatetImages.getDescriptor(StatetImages.LOCTOOLD_PAUSE));
		setChecked(false);
		setEnabled(false);
	}
	
	
	@Override
	public synchronized void run() {
		ToolController controller;
		if (fProcess == null 
				|| (controller = fProcess.getController()) == null) {
			return;
		}
		final boolean checked = isChecked();
		final boolean paused = controller.pause(checked);
		if (checked != paused) {
			setChecked(paused);
		}
	}
	
	/**
	 * 
	 * @param process must not be <code>null</code>.
	 */
	public synchronized void setTool(final ToolProcess process) {
		ToolController controller;
		if (process != null && !process.isTerminated() && (controller = process.getController()) != null) {
			fProcess = process;
			DebugPlugin.getDefault().addDebugEventListener(this);
			setEnabled(true);
			setChecked(controller.isPaused());
		}
		else {
			disconnect();
			setEnabled(false);
			setChecked(false);
		}
	}
	
	private void disconnect() {
		fProcess = null;
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}
	
	public synchronized void handleDebugEvents(final DebugEvent[] events) {
		for (final DebugEvent event : events) {
			if (event.getSource() == fProcess) {
				switch (event.getKind()) {
				case DebugEvent.MODEL_SPECIFIC:
					Boolean checked = null;
					final int detail = event.getDetail();
					switch (detail) {
					case ToolProcess.REQUEST_PAUSE:
					case ToolProcess.STATUS_PAUSE:
						checked = Boolean.TRUE;
						break;
					case ToolProcess.REQUEST_PAUSE_CANCELED:
						checked = Boolean.FALSE;
						break;
					default:
						if ((detail & ToolProcess.STATUS) == ToolProcess.STATUS) { // status other than QUEUE_PAUSE
							checked = Boolean.FALSE;
						}
						break;
					}
					if (checked != null) {
						final boolean finalChecked = checked.booleanValue(); 
						UIAccess.getDisplay().syncExec(new Runnable() {
							public void run() {
								setChecked(finalChecked);
							}
						});
					}
					break;
				case DebugEvent.TERMINATE:
					UIAccess.getDisplay().syncExec(new Runnable() {
						public void run() {
							setChecked(false);
							setEnabled(false);
						}
					});
					disconnect();
					break;
				}
			}
		}
	}
	
	public void dispose() {
		disconnect();
	}
	
}
