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

package de.walware.statet.nico.ui.util;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.progress.WorkbenchJob;

import de.walware.eclipsecommons.preferences.PreferencesUtil;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.NicoPreferenceNodes;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.internal.Messages;
import de.walware.statet.ui.util.ExceptionHandler;


/**
 *
 */
public class TerminatingMonitor implements IDebugEventSetListener {

	
	private class CheckJob extends WorkbenchJob {
		
		CheckJob(String title) {
			super(title);
			setSystem(true);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			
			final ToolProcess process = fProcess;
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			if (process == null || process.isTerminated()) {
				return Status.OK_STATUS;
			}

			IWorkbenchWindow window = UIAccess.getActiveWorkbenchWindow(true);
			String title = NLS.bind(Messages.TerminatingMonitor_title, process.getToolLabel(false));
			String message = NLS.bind(Messages.TerminatingMonitor_message, process.getToolLabel(true));
			String[] buttons = { Messages.TerminatingMonitor_WaitButton_label, Messages.TerminatingMonitor_ForceButton_label, Messages.TerminatingMonitor_CancelButton_label };
			
			MessageDialog dialog = new MessageDialog(window.getShell(), title, null, message, MessageDialog.QUESTION, buttons, 0);
			switch (dialog.open()) {
			case 0:
				rescheduleCheck();
				break;
			case 1: {
					try {
						window.run(true, true, new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								try {
									ToolController controller = process.getController();
									if (controller != null) {
										controller.terminateForced(monitor);
									}
									else {
										cancelCheck();
									}
								} catch (CoreException e) {
									throw new InvocationTargetException(e);
								}
							}
						});
					} 
					catch (InterruptedException e) {
						break;
					}
					catch (InvocationTargetException e) {
						CoreException ce = (CoreException) e.getTargetException();
						ExceptionHandler.handle(ce.getStatus());
						break;
					}
					break;
				}
			case 2:
				ToolController controller = process.getController();
				if (controller != null) {
					controller.cancelTermination();
				}
				cancelCheck();
				break;
			}
			return Status.OK_STATUS;
		}

	}

	
	private ToolProcess fProcess;
	private Job fCheckJob;
	private int fWaitIntervall;
	
	
	public TerminatingMonitor(ToolProcess process) {
		
		fProcess = process;
		fWaitIntervall = PreferencesUtil.getInstancePrefs().getPreferenceValue(NicoPreferenceNodes.KEY_DEFAULT_TIMEOUT);
		DebugPlugin.getDefault().addDebugEventListener(this);
		if (fProcess.isTerminated()) {
			dispose();
		}
	}
	
	public void handleDebugEvents(DebugEvent[] events) {
		
		for (DebugEvent event : events) {
			if (event.getSource() == fProcess) {
				switch (event.getKind()) {
				case DebugEvent.MODEL_SPECIFIC:
					switch (event.getDetail()) {
					case ToolProcess.REQUEST_TERMINATE:
						scheduleCheck();
						break;
					case ToolProcess.REQUEST_TERMINATE_CANCELED:
						cancelCheck();
						break;
					}
					break;
				case DebugEvent.TERMINATE:
					DebugPlugin.getDefault().removeDebugEventListener(this);
					dispose();
					return;
				}
			}
		}
	}

	private synchronized void scheduleCheck() {

		ToolProcess process = fProcess;
		if (process != null) {
			if (fCheckJob == null){
				fCheckJob = new CheckJob(NLS.bind("Monitor Termination {0}", process.getToolLabel(false))); //$NON-NLS-1$
				fCheckJob.schedule(fWaitIntervall);
			}
			else {
				if (fCheckJob.cancel()) {
					fCheckJob.schedule();
				}
			}
		}
	}
	
	private synchronized void rescheduleCheck() {
		
		if (fCheckJob != null) {
			fCheckJob.schedule(fWaitIntervall);
		}
	}
	
	private synchronized void cancelCheck() {
		
		if (fCheckJob != null) {
			fCheckJob.cancel();
			fCheckJob = null;
		}
	}
	
	private void dispose() {
		
		cancelCheck();
		fProcess = null;
	}
}
