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

import java.lang.Thread.UncaughtExceptionHandler;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import de.walware.statet.base.IStatetStatusConstants;
import de.walware.statet.base.StatetPlugin;
import de.walware.statet.nico.Messages;
import de.walware.statet.ui.util.ExceptionHandler;


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
	
	public void runInBackgroundThread(final ToolProcess process) {
		
		Runnable r = new Runnable() {
			public void run() {
				try {
					ToolRunner.this.run(process);
				} 
				catch (CoreException e) {
					ToolController controller = process.getController();
					ExceptionHandler.handle(e, NLS.bind(
							Messages.LaunchDelegate_error_UnexpectedTermination_message, 
							controller.getName()
					));
					controller.setStatus(ToolController.ToolStatus.TERMINATED);
					process.fExitValue = 1010;
				}
			}
		};
		
		Thread background = new Thread(r);
        background.setDaemon(true);
        background.setName("StatET Thread \""+process.getLabel()+"\"");
		background.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				ExceptionHandler.handle(new Status(
						IStatus.ERROR, 
						StatetPlugin.ID, 
						IStatetStatusConstants.RUNTIME_ERROR, 
						NLS.bind(Messages.Runtime_error_message, t.getName()), 
						e)
				);
				process.fExitValue = 1020;
			}
		});
		
		background.start();
	}
}
