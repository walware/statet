/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.ui.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;
import de.walware.ecommons.ts.util.ToolCommandHandlerUtil;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.runtime.IToolEventHandler;
import de.walware.statet.nico.internal.ui.Messages;


/**
 * @see IToolEventHandler#RUN_BLOCKING_EVENT_ID
 */
public class RunBlockingHandler implements IToolEventHandler {
	
	
	@Override
	public IStatus execute(final String id, final IToolService service, final Map<String, Object> data, final IProgressMonitor monitor) {
		final IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		final IToolRunnable toolRunnable = ToolCommandHandlerUtil.getCheckedData(data, RUN_RUNNABLE_DATA_KEY, IToolRunnable.class, true); 
		try {
			progressService.busyCursorWhile(new IRunnableWithProgress() {
				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						toolRunnable.run(service, monitor);
					}
					catch (final CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
			return Status.OK_STATUS;
		}
		catch (final InvocationTargetException e) {
			final Throwable targetException = e.getCause();
			if (targetException instanceof CoreException && 
					((CoreException) targetException).getStatus().getSeverity() == IStatus.CANCEL) {
				return Status.CANCEL_STATUS;
			}
			return handleError(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID,
					NLS.bind(Messages.ExecuteHandler_error_message, toolRunnable.getLabel()), targetException));
		}
		catch (final InterruptedException e) {
			return Status.CANCEL_STATUS;
		}
	}
	
	protected IStatus handleError(final IStatus status) {
		StatusManager.getManager().handle(status, StatusManager.LOG | StatusManager.SHOW);
		return status;
	}
	
}
