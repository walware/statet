/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License 
 * v2.1 or newer, which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.rserve.launchconfigs;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.ui.IWorkbenchPage;

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.debug.LaunchConfigUtil;
import de.walware.statet.base.ui.debug.UnterminatedLaunchAlerter;
import de.walware.statet.base.ui.util.ExceptionHandler;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolRunner;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.nico.ui.console.NIConsoleColorAdapter;
import de.walware.statet.nico.ui.util.TerminatingMonitor;
import de.walware.statet.r.nico.RWorkspace;
import de.walware.statet.r.nico.ui.RConsole;
import de.walware.statet.r.rserve.RServeClientController;


public class RServeClientLaunchConfigDelegate implements ILaunchConfigurationDelegate {

	
	public void launch(ILaunchConfiguration configuration, String mode, 
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		try {
			monitor = LaunchConfigUtil.initProgressMonitor(configuration, monitor, 4);
			if (monitor.isCanceled()) {
				return;
			}
			
			final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(false);
			final ConnectionConfig connectionConfig = new ConnectionConfig();
			connectionConfig.load(configuration);
			String name = "rserve://"+connectionConfig.getServerAddress()+":"+connectionConfig.getServerPort()
					+" ("+DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()))+")";
			
			monitor.worked(1);
			if (monitor.isCanceled()) {
				return;
			}
			
			UnterminatedLaunchAlerter.registerLaunchType(IRServeConstants.ID_RSERVE_LAUNCHCONFIG);
			final ToolProcess<RWorkspace> process = new ToolProcess<RWorkspace>(launch, name);
			process.init(new RServeClientController(process, connectionConfig));
			final NIConsole console = new RConsole(process, new NIConsoleColorAdapter());
			new TerminatingMonitor(process);
	    	NicoUITools.startConsoleLazy(console, page);
	    	// start
	    	new ToolRunner().runInBackgroundThread(process, new ExceptionHandler.StatusHandler());
		}
		finally {
			monitor.done();
		}
	}
	
}
