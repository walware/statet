/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.util.ExceptionHandler;
import de.walware.statet.nico.core.runtime.IToolEventHandler;
import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.internal.ui.Messages;


/**
 * Default quit handler
 */
public class QuitHandler implements IToolEventHandler {
	
	private class UIRunnable implements Runnable {
		
		private ToolController fController;
		private String fDialogTitle;
		private String fDialogMessage;
		private String[] fDialogOptions;
		private volatile int fResult;
		
		public void run() {
			IWorkbenchWindow window = UIAccess.getActiveWorkbenchWindow(true);
			MessageDialog dialog = new MessageDialog(window.getShell(), fDialogTitle, null, fDialogMessage, MessageDialog.QUESTION, fDialogOptions, 0);
			fResult = dialog.open();
			
			if (fResult == 1) {
				try {
					window.run(true, true, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							try {
								fController.kill(monitor);
							} catch (CoreException e) {
								throw new InvocationTargetException(e);
							}
						}
					});
				}
				catch (InterruptedException e) {
					Thread.interrupted();
				}
				catch (InvocationTargetException e) {
					CoreException ce = (CoreException) e.getTargetException();
					ExceptionHandler.handle(ce.getStatus());
				}
			}
		}
	}
	
	public int handle(IToolRunnableControllerAdapter tools, Object contextData) {
		IToolRunnable<IToolRunnableControllerAdapter>[] quitRunnables = (IToolRunnable<IToolRunnableControllerAdapter>[]) contextData;
		if (quitRunnables.length == 0) {
			return OK; // run default = schedule quit
		}
		
		UIRunnable runner = new UIRunnable();
		runner.fController = tools.getController();
		ToolProcess process = runner.fController.getProcess();
		runner.fDialogTitle = NLS.bind(Messages.TerminatingMonitor_title, process.getToolLabel(false));
		runner.fDialogMessage = NLS.bind(Messages.TerminatingMonitor_message, process.getToolLabel(true));
		runner.fDialogOptions = new String[] { Messages.TerminatingMonitor_WaitButton_label, Messages.TerminatingMonitor_ForceButton_label, Messages.TerminatingMonitor_CancelButton_label };

		UIAccess.getDisplay().syncExec(runner);
		if (runner.fResult == 2) {
			runner.fController.cancelQuit();
		}
		return CANCEL; // do nothing
	}
}
