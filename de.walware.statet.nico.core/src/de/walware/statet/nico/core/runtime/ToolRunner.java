/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.osgi.util.NLS;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.internal.core.Messages;
import de.walware.statet.nico.internal.core.NicoPlugin;


/**
 * Allows to run a ToolProcess in Eclipse.
 */
public class ToolRunner {
	
	
	public ToolRunner() {
	}
	
	
	private Object run(final ToolProcess process) throws CoreException {
		final ToolController controller = process.getController();
		controller.run();
		
		return null;
	}
	
	public <WorkspaceType extends ToolWorkspace> void runInBackgroundThread(final ToolProcess<WorkspaceType> process, final IStatusHandler handler) {
		final Thread background = new Thread() {
			@Override
			public void run() {
				try {
					ToolRunner.this.run(process);
				}
				catch (final CoreException e) {
					process.fExitValue = NicoCore.EXITVALUE_CORE_EXCEPTION;
					final IStatus status = new Status(
							IStatus.ERROR,
							NicoCore.PLUGIN_ID,
							NicoCore.STATUSCODE_RUNTIME_ERROR,
							NLS.bind(Messages.Runtime_error_UnexpectedTermination_message,
									new Object[] { process.getToolLabel(false), process.getLabel() }),
							e);
					try {
						handler.handleStatus(status, null);
					} catch (final CoreException e1) {
						NicoPlugin.log(status);
						NicoPlugin.logError(NicoPlugin.EXTERNAL_ERROR, Messages.ErrorHandling_error_message, e1);
					}
				}
				catch (final Throwable e) {
					// We had some problems with Thread#setUncaughtExceptionHandler, so we catch simply all Throwables
					process.fExitValue = NicoCore.EXITVALUE_RUNTIME_EXCEPTION;
					final IStatus status = new Status(
							IStatus.ERROR,
							NicoCore.PLUGIN_ID,
							NicoCore.STATUSCODE_RUNTIME_ERROR,
							NLS.bind(Messages.Runtime_error_CriticalError_message, getName()),
							e);
					try {
						handler.handleStatus(status, null);
					} catch (final CoreException e1) {
						NicoPlugin.log(status);
						NicoPlugin.logError(NicoPlugin.EXTERNAL_ERROR, Messages.ErrorHandling_error_message, e1);
					}
				}
			}
		};
		background.setDaemon(true);
		background.setName("StatET Thread '"+process.getLabel()+"'"); //$NON-NLS-1$ //$NON-NLS-2$
		
		background.start();
	}
	
}
