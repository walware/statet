/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.launching;

import de.walware.statet.nico.core.runtime.HistoryOperationsHandler;
import de.walware.statet.nico.core.runtime.IToolEventHandler;
import de.walware.statet.nico.ui.util.EclipseIDEOperationsHandler;
import de.walware.statet.nico.ui.util.QuitHandler;
import de.walware.statet.nico.ui.util.ReportStatusHandler;
import de.walware.statet.nico.ui.util.RunBlockingHandler;
import de.walware.statet.nico.ui.util.SelectFileHandler;

import de.walware.statet.r.nico.AbstractRController;


/**
 * 
 */
public class RConsoleLaunching {
	
	
	public static final String LOCAL_RTERM = "local.rterm"; //$NON-NLS-1$
	public static final String LOCAL_RJS = "local.rjs-rmi"; //$NON-NLS-1$
	public static final String REMOTE_RJS = "remote.rjs-rmi"; //$NON-NLS-1$
	public static final String REMOTE_RJS_RECONNECT = "remote.rjs-rmi-reconnect"; //$NON-NLS-1$
	public static final String REMOTE_RJS_SSH = "remote.rjs-rmi.ssh"; //$NON-NLS-1$
	
	
	static final String ATTR_ROOT = "de.walware.statet.r.debug/RConsole/"; //$NON-NLS-1$
	
	/**
	 * Attribute key for R console type
	 */
	public static final String ATTR_TYPE = ATTR_ROOT+"type"; //$NON-NLS-1$
	
	/**
	 * Attribute key for R startup arguments
	 */
	public static final String ATTR_OPTIONS = ATTR_ROOT+"arguments.options"; //$NON-NLS-1$
	
	/**
	 * Attribute key for address, if required by console type
	 */
	public static final String ATTR_ADDRESS = ATTR_ROOT+"address"; //$NON-NLS-1$
	
	/**
	 * Attribute key to pin console at startup
	 */
	public static final String ATTR_PIN_CONSOLE = ATTR_ROOT+"console.pin"; //$NON-NLS-1$
	
	/**
	 * Attribute key for port of SSH number
	 */
	public static final String ATTR_SSH_PORT = ATTR_ROOT+"ssh.port"; //$NON-NLS-1$
	
	/**
	 * Attribute key for command to startup R
	 */
	public static final String ATTR_COMMAND = ATTR_ROOT+"command"; //$NON-NLS-1$
	
	/**
	 * Attribute key for login (e.g. SSH)
	 */
	public static final String ATTR_LOGIN_NAME = ATTR_ROOT+"login.name";
	
	/**
	 * Attribute key for init snippet (submitted after startup)
	 */
	public static final String ATTR_INIT_SCRIPT_SNIPPET = ATTR_ROOT+"init.script.snippet"; //$NON-NLS-1$
	
	/**
	 * Attribute key for disable object db
	 */
	public static final String ATTR_DISABLE_OBJECTDB = ATTR_ROOT+"objectdb.disabled"; //$NON-NLS-1$
	
	
	public static void registerDefaultHandlerTo(final AbstractRController controller) {
		controller.addEventHandler(IToolEventHandler.SCHEDULE_QUIT_EVENT_ID, new QuitHandler());
		controller.addEventHandler(IToolEventHandler.RUN_BLOCKING_EVENT_ID, new RunBlockingHandler());
		controller.addEventHandler(IToolEventHandler.REPORT_STATUS_EVENT_ID, new ReportStatusHandler());
		final IToolEventHandler historyHandler = new HistoryOperationsHandler();
		controller.addEventHandler(HistoryOperationsHandler.LOAD_HISTORY_ID, historyHandler);
		controller.addEventHandler(HistoryOperationsHandler.SAVE_HISTORY_ID, historyHandler);
		controller.addEventHandler(HistoryOperationsHandler.ADDTO_HISTORY_ID, historyHandler);
		controller.addEventHandler(IToolEventHandler.SELECTFILE_EVENT_ID, new SelectFileHandler());
		final EclipseIDEOperationsHandler ideHandler = new EclipseIDEOperationsHandler();
		controller.addEventHandler(IToolEventHandler.SHOW_HISTORY_ID, ideHandler);
		controller.addEventHandler(IToolEventHandler.SHOW_FILE_ID, ideHandler);
	}
	
}
