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
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.debug.ui.LaunchConfigUtil;
import de.walware.ecommons.debug.ui.UnterminatedLaunchAlerter;
import de.walware.ecommons.net.RMIAddress;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolRunner;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.nico.ui.console.NIConsoleColorAdapter;
import de.walware.statet.nico.ui.util.WorkbenchStatusHandler;

import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.core.renv.REnvConfiguration;
import de.walware.statet.r.debug.ui.launchconfigs.REnvTab;
import de.walware.statet.r.debug.ui.launchconfigs.RLaunchConfigurations;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.RConsoleLaunching;
import de.walware.statet.r.nico.RWorkspace;
import de.walware.statet.r.nico.impl.RjsController;
import de.walware.statet.r.nico.ui.RConsole;
import de.walware.statet.r.ui.RUI;


/**
 * Launch delegate for RJ based R console using embedded RJ server
 */
public class RConsoleRJLaunchDelegate extends LaunchConfigurationDelegate {
	
	
	public void launch(final ILaunchConfiguration configuration, final String mode, 
			final ILaunch launch, final IProgressMonitor monitor) throws CoreException {
		final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(false);
		final SubMonitor progress = LaunchConfigUtil.initProgressMonitor(configuration, monitor, 25);
		
		progress.worked(1);
		if (progress.isCanceled()) {
			return;
		}
		
		// r env
		final REnvConfiguration renv = REnvTab.getREnv(configuration);
		
		final Integer port = PreferencesUtil.getInstancePrefs().getPreferenceValue(RMIUtil.PREF_LOCAL_REGISTRY_PORT);
		final String s = "//:"+port+"/rjs-local-"+System.currentTimeMillis(); //$NON-NLS-1$ //$NON-NLS-2$
		final RMIAddress rmiAddress;
		try {
			rmiAddress = new RMIAddress(s);  
		}
		catch (final UnknownHostException e) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
					RLaunchingMessages.RJLaunchDelegate_error_InvalidAddress_message, e));
		}
		catch (final MalformedURLException e) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
					RLaunchingMessages.RJLaunchDelegate_error_InvalidAddress_message, e));
		}
		final RJEngineLaunchDelegate engineLaunchDelegate = new RJEngineLaunchDelegate(rmiAddress.getAddress(), renv);
		
		progress.worked(1);
		if (progress.isCanceled()) {
			return;
		}
		
		// start server
		progress.subTask(RLaunchingMessages.RJLaunchDelegate_StartR_subtask);
		RMIUtil.startRegistry(port);
		engineLaunchDelegate.launch(configuration, mode, launch, progress.newChild(10));
		final IProcess[] processes = launch.getProcesses();
		if (processes.length == 0) {
			return;
		}
		
		progress.worked(1);
		if (progress.isCanceled()) {
			return;
		}
		
		// arguments
		final String[] rArgs = LaunchConfigUtil.getProcessArguments(configuration, RConsoleLaunching.ATTR_OPTIONS);
		
		progress.worked(1);
		if (progress.isCanceled()) {
			return;
		}
		
		// create process
		UnterminatedLaunchAlerter.registerLaunchType(RLaunchConfigurations.ID_R_CONSOLE_CONFIGURATION_TYPE);
		
		String name = rmiAddress.toString();
		name += ' ' + LaunchConfigUtil.createProcessTimestamp();
		final ToolProcess<RWorkspace> process = new ToolProcess<RWorkspace>(launch, "R", //$NON-NLS-1$
				LaunchConfigUtil.createLaunchPrefix(configuration), renv.getName() + " : R Console/RJ ~ " + name); //$NON-NLS-1$
		process.setAttribute(IProcess.ATTR_CMDLINE, name + " " + Arrays.toString(rArgs)); //$NON-NLS-1$
		
		// Wait until the engine is started or died
		progress.subTask(RLaunchingMessages.RJLaunchDelegate_WaitForR_subtask);
		WAIT: for (int i = 0; i < 50; i++) {
			if (processes[0].isTerminated()) {
				final boolean silent = configuration.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true);
				StatusManager.getManager().handle(new Status(silent ? IStatus.INFO : IStatus.ERROR, RUI.PLUGIN_ID,
						"Launching the R Console was cancelled, because it seems starting the Java process/R engine failed. \n"+
						"Please make sure that R package 'rJava' with JRI is installed and look into the Troubleshooting section on the homepage."),
						silent ? (StatusManager.LOG) :(StatusManager.LOG | StatusManager.SHOW));
				return;
			}
			if (progress.isCanceled()) {
				processes[0].terminate();
				throw new CoreException(Status.CANCEL_STATUS);
			}
			try {
				final String[] list = Naming.list(rmiAddress.getRegistryAddress());
				for (final String entry : list) {
					try {
						if (new RMIAddress(entry).equals(rmiAddress)) {
							break WAIT;
						}
					}
					catch (final UnknownHostException e) {}
				}
			}
			catch (final RemoteException e) {
				if (i > 25) {
					break WAIT;
				}
			}
			catch (final MalformedURLException e) {
			}
			try {
				Thread.sleep(500);
			}
			catch (final InterruptedException e) {
				Thread.interrupted();
			}
		}
		progress.worked(5);
		
		final RjsController controller = new RjsController(process, rmiAddress, null,
				true, true, rArgs,
				REnvTab.getWorkingDirectoryValidator(configuration, false).getFileStore());
		process.init(controller);
		RConsoleLaunching.registerDefaultHandlerTo(controller);
		
		progress.worked(5);
		
		final String startupSnippet = configuration.getAttribute(RConsoleLaunching.ATTR_INIT_SCRIPT_SNIPPET, (String) null);
		if (startupSnippet != null && startupSnippet.length() > 0) {
			controller.submit(RUtil.LINE_SEPARATOR_PATTERN.split(startupSnippet), SubmitType.OTHER);
		}
		
		final NIConsole console = new RConsole(process, new NIConsoleColorAdapter());
		NicoUITools.startConsoleLazy(console, page, 
				configuration.getAttribute(RConsoleLaunching.ATTR_PIN_CONSOLE, false));
		
		new ToolRunner().runInBackgroundThread(process, new WorkbenchStatusHandler());
		
		if (monitor != null) {
			monitor.done();
		}
	}
	
}
