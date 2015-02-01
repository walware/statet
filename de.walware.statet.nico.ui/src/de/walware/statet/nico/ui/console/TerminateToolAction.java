/*=============================================================================#
 # Copyright (c) 2000-2015 IBM Corporation and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     IBM Corporation - initial API and implementation
 #     Stephan Wahlbrink - adaptation to StatET
 #=============================================================================*/

package de.walware.statet.nico.ui.console;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.views.console.ConsoleMessages;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.texteditor.IUpdate;

import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.internal.ui.Messages;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;


public class TerminateToolAction extends Action implements IUpdate {
	
	
	private IProcess fProcess;
	
	
	/**
	 * Creates a terminate action for the console
	 */
	public TerminateToolAction(final IProcess process) {
		super(ConsoleMessages.ConsoleTerminateAction_0);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.CONSOLE_TERMINATE_ACTION);
		fProcess = process;
		setToolTipText(ConsoleMessages.ConsoleTerminateAction_1);
		final ImageRegistry registry = NicoUIPlugin.getDefault().getImageRegistry();
		setImageDescriptor(registry.getDescriptor(NicoUI.LOCTOOL_TERMINATE_IMAGE_ID));
		setDisabledImageDescriptor(registry.getDescriptor(NicoUI.LOCTOOLD_TERMINATE_IMAGE_ID));
		update();
	}
	
	
	@Override
	public void update() {
		setEnabled(fProcess.canTerminate());
	}
	
	@Override
	public void run() {
		try {
//			killTargets(fProcess);
			fProcess.terminate();
			
			final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(false);
			final IConsole console = DebugUITools.getConsole(fProcess);
			if (console instanceof NIConsole) {
				NicoUITools.showConsole((NIConsole) console, page, true);
			}
		} catch (final DebugException e) {
			final int severity = e.getStatus().getSeverity();
			StatusManager.getManager().handle(new Status(severity, NicoUI.PLUGIN_ID, -1, Messages.TerminateToolAction_error_message, e),
					(severity >= IStatus.ERROR) ? StatusManager.LOG | StatusManager.SHOW : StatusManager.LOG);
		}
	}
	
	private void killTargets(final IProcess process) throws DebugException {
		final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		final ILaunch[] launches = launchManager.getLaunches();
		
		for (int i = 0; i < launches.length; i++) {
			final ILaunch launch = launches[i];
			final IProcess[] processes = launch.getProcesses();
			for (int j = 0; j < processes.length; j++) {
				final IProcess process2 = processes[j];
				if (process2.equals(process)) {
					final IDebugTarget[] debugTargets = launch.getDebugTargets();
					for (int k = 0; k < debugTargets.length; k++) {
						final IDebugTarget target = debugTargets[k];
						if (target.canTerminate()) {
							target.terminate();
						}
					}
					return; // all possible targets have been terminated for the launch.
				}
			}
		}
	}
	
	public void dispose() {
		fProcess = null;
	}
	
}
