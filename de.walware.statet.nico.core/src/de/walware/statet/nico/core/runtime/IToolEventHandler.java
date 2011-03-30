/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import java.util.Map;

import javax.security.auth.callback.Callback;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


/**
 * Can react on tool events.
 * 
 * Tool events allows automation and the separation of UI.
 */
public interface IToolEventHandler {
	
	public static final int OK = Status.OK;
	public static final int ERROR = Status.ERROR;
	public static final int CANCEL = Status.CANCEL;
	
	public static final int YES = 0;
	public static final int NO = -1;
	
	
	/**
	 * Called when a login is required
	 * 
	 * return:  OK = try login, CANCEL = cancel
	 */
	public static final String LOGIN_REQUEST_EVENT_ID = "common/login.request"; //$NON-NLS-1$
	
	/**
	 * Called when a login was successful
	 * 
	 * return:  OK = try login, CANCEL = cancel
	 */
	public static final String LOGIN_OK_EVENT_ID = "common/login.ok"; //$NON-NLS-1$
	
	/** {@link String} = message to show (e.g. previous login error) (optional) */
	public static final String LOGIN_MESSAGE_DATA_KEY = "message"; //$NON-NLS-1$
	/** {@link String} = address to identify login (optional) */
	public static final String LOGIN_ADDRESS_DATA_KEY = "address"; //$NON-NLS-1$
	/** {@link Callback}[] = callbacks to answer (required) */
	public static final String LOGIN_CALLBACKS_DATA_KEY = "callbacks"; //$NON-NLS-1$
	/** {@link String} = username proposal (optional) */
	public static final String LOGIN_USERNAME_DATA_KEY = "username"; //$NON-NLS-1$
	/** {@link String} = flag to force usage of username (optional) */
	public static final String LOGIN_USERNAME_FORCE_DATA_KEY = "username.force"; //$NON-NLS-1$
	/** {@link String} = SSH host (when using SSH) */
	public static final String LOGIN_SSH_HOST_DATA_KEY = "ssh.host"; //$NON-NLS-1$
	/** {@link Integer} = SSH port (when using SSH) */
	public static final String LOGIN_SSH_PORT_DATA_KEY = "ssh.port"; //$NON-NLS-1$
	
	
	/**
	 * 
	 * data:    IStatus status
	 */
	public static final String REPORT_STATUS_EVENT_ID = "common/reportStatus"; //$NON-NLS-1$
	
	/** {@link IStatus} = status to report (required) */
	public static final String REPORT_STATUS_DATA_KEY = "status"; //$NON-NLS-1$
	
	/**
	 * 
	 * data:    IToolRunnable<IToolRunnableControllerAdapter>[*] schedulesQuitTasks = existing scheduled runnables
	 * return:  OK = schedule, CANCEL = do nothing
	 */
	public static final String SCHEDULE_QUIT_EVENT_ID = "common/scheduleQuit"; //$NON-NLS-1$
	
	/**
	 * Should try to block other actions (e.g. modal dialog)
	 * 
	 * return:  OK, ERROR, CANCEL
	 */
	public static final String RUN_BLOCKING_EVENT_ID = "common/runBlocking"; //$NON-NLS-1$
	
	/** {@link IToolRunnable} = runnable to run */
	public static final String RUN_RUNNABLE_DATA_KEY = "runnable"; //$NON-NLS-1$
	
	
	public IStatus handle(String id, IToolRunnableControllerAdapter tools, Map<String, Object> data, IProgressMonitor monitor);
	
}
