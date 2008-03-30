/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.launching;

import de.walware.statet.nico.core.runtime.IToolEventHandler;
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
	
	
	static final String ATTR_ROOT = "de.walware.statet.r.debug/RConsole/"; //$NON-NLS-1$
	
	/**
	 * R console type
	 */
	public static final String ATTR_TYPE = ATTR_ROOT+"type"; //$NON-NLS-1$
	
	/**
	 * R startup arguments
	 */
	public static final String ATTR_OPTIONS = ATTR_ROOT+"arguments.options"; //$NON-NLS-1$
	
	/**
	 * Address, if required by console type
	 */
	public static final String ATTR_ADDRESS = ATTR_ROOT+"address"; //$NON-NLS-1$
	
	
	public static final String ATTR_PIN_CONSOLE = "console.pin"; //$NON-NLS-1$
	
	
	public static void registerDefaultHandlerTo(final AbstractRController controller) {
		controller.addEventHandler(IToolEventHandler.SCHEDULE_QUIT_EVENT_ID, new QuitHandler());
		controller.addEventHandler(IToolEventHandler.RUN_BLOCKING_EVENT_ID, new RunBlockingHandler());
		controller.addEventHandler(IToolEventHandler.REPORT_STATUS_EVENT_ID, new ReportStatusHandler());
		controller.addEventHandler(IToolEventHandler.SELECTFILE_EVENT_ID, new SelectFileHandler());
	}
	
}
