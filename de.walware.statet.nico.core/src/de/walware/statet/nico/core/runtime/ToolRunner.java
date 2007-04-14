/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import java.lang.Thread.UncaughtExceptionHandler;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.osgi.util.NLS;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.internal.Messages;
import de.walware.statet.nico.core.internal.NicoPlugin;


/**
 * Allows to run a ToolProcess in Eclipse.
 */
public class ToolRunner {

	
	private Object run(ToolProcess process) throws CoreException {
		
		ToolController controller = process.getController();
		controller.run();
		
		return null;
	}
	
	public <WorkspaceType extends ToolWorkspace> void runInBackgroundThread(final ToolProcess<WorkspaceType> process, final IStatusHandler handler) {
		
		Runnable r = new Runnable() {
			public void run() {
				try {
					ToolRunner.this.run(process);
				} 
				catch (CoreException e) {
					process.fExitValue = NicoCore.EXITVALUE_CORE_EXCEPTION;
					IStatus status = new Status(
							IStatus.ERROR,
							NicoCore.PLUGIN_ID,
							NicoCore.STATUSCODE_RUNTIME_ERROR,
							NLS.bind(Messages.Runtime_error_UnexpectedTermination_message, 
									new Object[] { process.getToolLabel(false), process.getLabel() }),
							e);
					try {
						handler.handleStatus(status, null);
					} catch (CoreException e1) {
						NicoPlugin.log(status);
						NicoPlugin.logError(NicoPlugin.EXTERNAL_ERROR, Messages.ErrorHandling_error_message, e1);
					}
				}
			}
		};
		
		Thread background = new Thread(r);
		background.setDaemon(true);
		background.setName("StatET Thread '"+process.getLabel()+"'"); //$NON-NLS-1$ //$NON-NLS-2$
		background.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				process.fExitValue = NicoCore.EXITVALUE_RUNTIME_EXCEPTION;
				IStatus status = new Status(
						IStatus.ERROR, 
						NicoCore.PLUGIN_ID, 
						NicoCore.STATUSCODE_RUNTIME_ERROR,
						NLS.bind(Messages.Runtime_error_CriticalError_message, t.getName()), 
						e);
				try {
					handler.handleStatus(status, null);
				} catch (CoreException e1) {
					NicoPlugin.log(status);
					NicoPlugin.logError(NicoPlugin.EXTERNAL_ERROR, Messages.ErrorHandling_error_message, e1);
				}
			}
		});
		
		background.start();
	}
}
