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

package de.walware.statet.r.internal.debug.ui.launchconfigs;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchPage;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.debug.ui.LaunchConfigUtil;
import de.walware.ecommons.debug.ui.UnterminatedLaunchAlerter;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolRunner;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.nico.ui.console.NIConsoleColorAdapter;
import de.walware.statet.nico.ui.util.WorkbenchStatusHandler;

import de.walware.statet.r.debug.ui.launchconfigs.RLaunchConfigurations;
import de.walware.statet.r.launching.RConsoleLaunching;
import de.walware.statet.r.nico.RWorkspace;
import de.walware.statet.r.nico.impl.RjsController;
import de.walware.statet.r.nico.ui.RConsole;
import de.walware.statet.r.ui.RUI;


public class RRemoteConsoleLaunchDelegate extends LaunchConfigurationDelegate {
	
	
	public void launch(final ILaunchConfiguration configuration, final String mode, final ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		try {
			monitor = LaunchConfigUtil.initProgressMonitor(configuration, monitor, 100);
			if (monitor.isCanceled()) {
				return;
			}
			
			final String type = configuration.getAttribute(RConsoleLaunching.ATTR_TYPE, "").trim(); //$NON-NLS-1$
			if (type.equals(RConsoleLaunching.REMOTE_RJS)) { 
				launchRjsJriRemote(configuration, mode, launch, monitor);
				return;
			}
			throw new CoreException(new Status(Status.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
					NLS.bind("R Remote Console launch type ''{0}'' is not available.", type), null));
		}
		finally {
			monitor.done();
		}
	}
	
	private void launchRjsJriRemote(final ILaunchConfiguration configuration, final String mode, final ILaunch launch,
			final IProgressMonitor monitor) throws CoreException {
		final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(false);
		
		monitor.worked(1);
		if (monitor.isCanceled()) {
			return;
		}
		
		// r env
//		REnvConfiguration renv = REnvTab.getREnv(configuration);
//		renv.validate();
		
		monitor.worked(1);
		if (monitor.isCanceled()) {
			return;
		}
		
		// arguments
		String address = configuration.getAttribute(RConsoleLaunching.ATTR_ADDRESS, (String) null);
		final String[] args = LaunchConfigUtil.getProcessArguments(configuration, RConsoleLaunching.ATTR_OPTIONS);
		if (address == null || address.length() == 0) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
					"Invalid launch configuration: missing address", null));
		}
		if (!address.startsWith("//")) { //$NON-NLS-1$
			address = "//" + address; //$NON-NLS-1$
		}
		try {
			new URL("http:"+address); //$NON-NLS-1$
		}
		catch (final MalformedURLException e) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
					"Invalid launch configuration: invalid address", e));
		}
		
		monitor.worked(1);
		if (monitor.isCanceled()) {
			return;
		}
		
		// create process
		UnterminatedLaunchAlerter.registerLaunchType(RLaunchConfigurations.ID_R_REMOTE_CONSOLE_CONFIGURATION_TYPE);
		
		String name = "rmi:"+address; //$NON-NLS-1$
		name += ' ' + LaunchConfigUtil.createProcessTimestamp();
		final ToolProcess<RWorkspace> process = new ToolProcess<RWorkspace>(launch,
				RLaunchConfigurations.ID_R_CONSOLE_PROCESS_TYPE,
				LaunchConfigUtil.createLaunchPrefix(configuration), " (Remote) : R Console/RJ ~ " + name); //$NON-NLS-1$
		process.setAttribute(IProcess.ATTR_CMDLINE, name + " " + Arrays.toString(args)); //$NON-NLS-1$
		
		final RjsController controller = new RjsController(process, address, args, null);
		process.init(controller);
		RConsoleLaunching.registerDefaultHandlerTo(controller);
		
		monitor.worked(5);
		
		final NIConsole console = new RConsole(process, new NIConsoleColorAdapter());
		NicoUITools.startConsoleLazy(console, page,
				configuration.getAttribute(RConsoleLaunching.ATTR_PIN_CONSOLE, false));
		// start
		new ToolRunner().runInBackgroundThread(process, new WorkbenchStatusHandler());
	}
	
}
