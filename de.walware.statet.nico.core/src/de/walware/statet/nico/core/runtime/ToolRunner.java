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

package de.walware.statet.nico.core.runtime;

import java.lang.Thread.UncaughtExceptionHandler;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.osgi.util.NLS;

import de.walware.statet.base.IStatetStatusConstants;
import de.walware.statet.nico.core.internal.NicoMessages;
import de.walware.statet.nico.core.internal.NicoPlugin;


/**
 * Allows to run a ToolProcess in Eclipse.
 */
public class ToolRunner implements IPlatformRunnable {

	
	public Object run(Object args) throws CoreException {

		if (args instanceof ToolProcess) {
			
			return doRun((ToolProcess) args);
		}
		return null;
	}

	
	private Object doRun(ToolProcess process) throws CoreException {
		
		ToolController controller = process.getController();
		controller.run();
		
		return null;
	}
	
	public void runInBackgroundThread(final ToolProcess process, final IStatusHandler handler) {
		
		Runnable r = new Runnable() {
			public void run() {
				try {
					ToolRunner.this.run(process);
				} 
				catch (CoreException e) {
					process.fExitValue = 1010;
					try {
						handler.handleStatus(new Status(
								IStatus.ERROR,
								NicoPlugin.ID,
								IStatetStatusConstants.RUNTIME_ERROR,
								NLS.bind(NicoMessages.Runtime_error_UnexpectedTermination_message, process.getLabel()),
								e), 
								null);
					} catch (CoreException e1) {
						NicoPlugin.logUnexpectedError(e);
					}
				}
			}
		};
		
		Thread background = new Thread(r);
		background.setDaemon(true);
		background.setName("StatET Thread \""+process.getLabel()+"\"");
		background.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				process.fExitValue = 1020;
				try {
					handler.handleStatus(new Status(
							IStatus.ERROR, 
							NicoPlugin.ID, 
							IStatetStatusConstants.RUNTIME_ERROR, 
							NLS.bind(NicoMessages.Runtime_error_CriticalError_message, t.getName()), 
							e), 
							null);
				} catch (CoreException e1) {
					NicoPlugin.logUnexpectedError(e);
				}
			}
		});
		
		background.start();
	}
}
