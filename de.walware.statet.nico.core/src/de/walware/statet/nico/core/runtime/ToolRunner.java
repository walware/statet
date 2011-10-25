/*******************************************************************************
 * Copyright (c) 2005-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ts.ITool;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.internal.core.Messages;
import de.walware.statet.nico.internal.core.NicoPlugin;


/**
 * Allows to run a ToolProcess in Eclipse.
 */
public class ToolRunner {
	
	
	public static boolean captureLogOnly(final ILaunchConfiguration configuration) {
		try {
			return (!configuration.getAttribute("org.eclipse.debug.ui.ATTR_CONSOLE_OUTPUT_ON", true) //$NON-NLS-1$
					&& configuration.getAttribute("org.eclipse.debug.ui.ATTR_CAPTURE_IN_FILE", (String) null) == null ); //$NON-NLS-1$
		}
		catch (final CoreException e) {
			return true;
		}
	}
	
	public static IStatus createOutputLogStatus(final ILogOutput log) {
		if (log != null) {
			final String s = log.getOutput();
			if (s.length() > 0) {
				return  new Status(IStatus.INFO, NicoCore.PLUGIN_ID, "Process Error Log:",
						new Exception(s) );
			}
		}
		return null;
	}
	
	private static class MultiErrorStatus extends Status {
		
		private final IStatus[] fChildren;
		
		public MultiErrorStatus(final String pluginId, final int code,
				final IStatus[] children, final String message, final Throwable exception) {
			super(IStatus.ERROR, pluginId, code, message, exception);
			fChildren = children;
		}
		
		
		@Override
		public IStatus[] getChildren() {
			return fChildren;
		}
		
	}
	
	
	public ToolRunner() {
	}
	
	
	private void run(final ToolProcess process) throws CoreException {
		final ToolController controller = process.getController();
		controller.run();
	}
	
	public <WorkspaceType extends ToolWorkspace> void runInBackgroundThread(
			final ToolProcess process, final IStatusHandler handler) {
		if (process == null || handler == null) {
			throw new NullPointerException();
		}
		final Thread background = new Thread() {
			@Override
			public void run() {
				try {
					ToolRunner.this.run(process);
				}
				catch (final CoreException e) {
					if (e.getStatus() != null && e.getStatus().getSeverity() == IStatus.CANCEL) {
					}
					else {
						process.fExitValue = NicoCore.EXITVALUE_CORE_EXCEPTION;
						final IStatus status = createStatus(process,
								NLS.bind(Messages.Runtime_error_UnexpectedTermination_message,
										new Object[] { process.getLabel(ITool.DEFAULT_LABEL), process.getLabel() }),
								e );
						try {
							handler.handleStatus(status, null);
						}
						catch (final CoreException e1) {
							NicoPlugin.log(status);
							NicoPlugin.logError(NicoPlugin.EXTERNAL_ERROR, Messages.ErrorHandling_error_message, e1);
						}
					}
				}
				catch (final Throwable e) {
					// We had some problems with Thread#setUncaughtExceptionHandler, so we catch simply all Throwables
					process.fExitValue = NicoCore.EXITVALUE_RUNTIME_EXCEPTION;
					final IStatus status = createStatus(process,
							NLS.bind(Messages.Runtime_error_CriticalError_message, getName()), e );
					try {
						handler.handleStatus(status, null);
					}
					catch (final CoreException e1) {
						NicoPlugin.log(status);
						NicoPlugin.logError(NicoPlugin.EXTERNAL_ERROR, Messages.ErrorHandling_error_message, e1);
					}
					final IStatus logStatus = createOutputLogStatus((ILogOutput) process.getAdapter(ILogOutput.class));
					if (logStatus != null) {
						NicoPlugin.log(status);
					}
				}
				
				try {
					final ILaunch launch = process.getLaunch();
					if (launch != null && !launch.isTerminated()) {
						launch.isTerminated();
					}
				}
				catch (final Throwable e) {}
			}
		};
		background.setDaemon(true);
		background.setName(process.getMainType() + " Engine '"+process.getLabel()+"'"); //$NON-NLS-1$ //$NON-NLS-2$
		
		background.start();
	}
	
	private IStatus createStatus(final ToolProcess process, final String message, final Throwable e) {
		final List<IStatus> list = new ArrayList<IStatus>();
		final IProcess[] processes = process.getLaunch().getProcesses();
		for (int i = 0; i < processes.length; i++) {
			final IStatus logStatus = createOutputLogStatus((ILogOutput) processes[i].getAdapter(ILogOutput.class));
			if (logStatus != null) {
				list.add(logStatus);
			}
		}
		if (list.isEmpty()) {
			return new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, 0, message, e);
		}
		return new MultiErrorStatus(NicoCore.PLUGIN_ID, NicoCore.STATUSCODE_RUNTIME_ERROR,
				list.toArray(new IStatus[list.size()]), message, e );
	}
	
}
