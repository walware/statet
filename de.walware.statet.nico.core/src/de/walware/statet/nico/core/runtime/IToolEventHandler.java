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

package de.walware.statet.nico.core.runtime;

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
	 * 
	 * contextData:  LoginEventData
	 * return:       OK = try login, CANCEL = cancel
	 */
	public static final String LOGIN_EVENT_ID = "common/login"; //$NON-NLS-1$
	
	public static final class LoginEventData {
		public String name;
		public String password;
	};
	
	/**
	 * 
	 * contextData:  IToolRunnable<IToolRunnableControllerAdapter>[*] = existing scheduled runnables
	 * return:       OK = schedule, CANCEL = do nothing
	 */
	public static final String SCHEDULE_QUIT_EVENT_ID = "common/scheduleQuit"; //$NON-NLS-1$
	
	/**
	 * Should try to block other actions (e.g. modal dialog)
	 * 
	 * contextData:  IRunnableWithProgress = existing scheduled runnables
	 * return:       OK, ERROR, CANCEL
	 */
	public static final String RUN_BLOCKING_EVENT_ID = "common/runBlocking"; //$NON-NLS-1$
	
	/**
	 * 
	 * return:       OK = schedule, CANCEL = do nothing
	 */
	public static final String SELECTFILE_EVENT_ID = "common/selectFile"; //$NON-NLS-1$
	
	public static final class SelectFileEventData {
		public boolean newFile;
		public String message;
		public String filename;
	};
	
	
	public int handle(IToolRunnableControllerAdapter tools, Object contextData);
	
}
