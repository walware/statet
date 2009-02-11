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

import de.walware.ecommons.debug.ui.LaunchConfigUtil;
import de.walware.ecommons.debug.ui.UnterminatedLaunchAlerter;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolRunner;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.nico.ui.console.NIConsoleColorAdapter;
import de.walware.statet.nico.ui.util.WorkbenchStatusHandler;

import de.walware.statet.r.core.renv.REnvConfiguration;
import de.walware.statet.r.debug.ui.launchconfigs.REnvTab;
import de.walware.statet.r.debug.ui.launchconfigs.RLaunchConfigurations;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.RConsoleLaunching;
import de.walware.statet.r.nico.RWorkspace;
import de.walware.statet.r.nico.impl.RjsController;
import de.walware.statet.r.nico.ui.RConsole;
import de.walware.statet.r.ui.RUI;


public class RConsoleRJLaunchDelegate extends LaunchConfigurationDelegate {
	
	
	public void launch(final ILaunchConfiguration configuration, final String mode, 
			final ILaunch launch, final IProgressMonitor monitor) throws CoreException {
		final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(false);
		final SubMonitor progress = SubMonitor.convert(monitor, RLaunchingMessages.LaunchDelegate_task, 25);
		progress.subTask(RLaunchingMessages.LaunchDelegate_Init_subtask);
		
		progress.worked(1);
		if (progress.isCanceled()) {
			return;
		}
		
		// r env
		final REnvConfiguration renv = REnvTab.getREnv(configuration);
		
		final Integer port = PreferencesUtil.getInstancePrefs().getPreferenceValue(RMIUtil.PREF_LOCAL_REGISTRY_PORT);
		final String address = "//:"+port+"/rjs-local-"+System.currentTimeMillis(); //$NON-NLS-1$ //$NON-NLS-2$
		final RJEngineLaunchDelegate engineLaunchDelegate = new RJEngineLaunchDelegate(address, renv);
		
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
		
		String name = "rmi:"+address; //$NON-NLS-1$
		name += ' ' + LaunchConfigUtil.createProcessTimestamp();
		final ToolProcess<RWorkspace> process = new ToolProcess<RWorkspace>(launch,
				RLaunchConfigurations.ID_R_CONSOLE_PROCESS_TYPE,
				LaunchConfigUtil.createLaunchPrefix(configuration), renv.getName() + " : R Console/RJ ~ " + name); //$NON-NLS-1$
		process.setAttribute(IProcess.ATTR_CMDLINE, name + " " + Arrays.toString(rArgs)); //$NON-NLS-1$
		
		progress.subTask(RLaunchingMessages.RJLaunchDelegate_WaitForR_subtask);
		WAIT: for (int i = 0; i < 50; i++) {
			if (processes[0].isTerminated()) {
				final boolean silent = configuration.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true);
				StatusManager.getManager().handle(new Status(silent ? IStatus.INFO : IStatus.ERROR, RUI.PLUGIN_ID,
						"Launching the R Console was cancelled, because it seems starting the Java process/R engine failed. \n"+
						"Please make sure that R package 'rJava' with JRI is installed."),
						silent ? (StatusManager.LOG) :(StatusManager.LOG | StatusManager.SHOW));
				return;
			}
			if (progress.isCanceled()) {
				processes[0].terminate();
				return;
			}
			try {
				final String[] list = Naming.list(address);
				for (final String entry : list) {
					if (entry.equals(address)) {
						break WAIT;
					}
				}
			}
			catch (final RemoteException e) {
			}
			catch (final MalformedURLException e) {
			}
			try {
				Thread.sleep(500);
			} catch (final InterruptedException e) {
				Thread.interrupted();
			}
		}
		progress.worked(5);
		
		final RjsController controller = new RjsController(process, address, rArgs,
				REnvTab.getWorkingDirectoryValidator(configuration, false).getFileStore());
		process.init(controller);
		RConsoleLaunching.registerDefaultHandlerTo(controller);
		
		progress.worked(5);
		
		final NIConsole console = new RConsole(process, new NIConsoleColorAdapter());
		NicoUITools.startConsoleLazy(console, page, 
				configuration.getAttribute(RConsoleLaunching.ATTR_PIN_CONSOLE, false));
		
		new ToolRunner().runInBackgroundThread(process, new WorkbenchStatusHandler());
		
		if (monitor != null) {
			monitor.done();
		}
	}
	
}
