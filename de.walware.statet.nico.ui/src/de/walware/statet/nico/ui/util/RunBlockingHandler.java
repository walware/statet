/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.util;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.runtime.IToolEventHandler;
import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.internal.ui.Messages;


/**
 * @see IToolEventHandler#RUN_BLOCKING_EVENT_ID
 */
public class RunBlockingHandler implements IToolEventHandler {
	
	
	public int handle(final IToolRunnableControllerAdapter tools, final Object contextData) {
		final IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		final IToolRunnable toolRunnable = (IToolRunnable) contextData;
		try {
			progressService.busyCursorWhile(new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						toolRunnable.run(tools, monitor);
					} catch (final CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
			return OK;
		} catch (final InvocationTargetException e) {
			final Throwable targetException = e.getTargetException();
//			if (targetException instanceof CoreException) {
//				return handleError(((CoreException) targetException).getStatus());
//			}
			return handleError(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID,
					NLS.bind(Messages.ExecuteHandler_error_message, toolRunnable.getLabel()), targetException));
		} catch (final InterruptedException e) {
			Thread.interrupted();
			return CANCEL;
		}
	}
	
	protected int handleError(final IStatus status) {
		StatusManager.getManager().handle(status, StatusManager.LOG | StatusManager.SHOW);
		return ERROR;
	}
	
}
