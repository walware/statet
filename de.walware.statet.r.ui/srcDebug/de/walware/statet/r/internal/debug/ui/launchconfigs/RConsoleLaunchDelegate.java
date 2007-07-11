/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launchconfigs;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchPage;

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.IStatetStatusConstants;
import de.walware.statet.base.ui.debug.LaunchConfigUtil;
import de.walware.statet.base.ui.debug.UnterminatedLaunchAlerter;
import de.walware.statet.base.ui.util.ExceptionHandler;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolRunner;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.nico.ui.console.NIConsoleColorAdapter;
import de.walware.statet.nico.ui.util.QuitHandler;
import de.walware.statet.r.core.renv.REnvConfiguration;
import de.walware.statet.r.core.renv.REnvConfiguration.Exec;
import de.walware.statet.r.debug.ui.launchconfigs.IRLaunchConfigurationConstants;
import de.walware.statet.r.debug.ui.launchconfigs.REnvTab;
import de.walware.statet.r.nico.RWorkspace;
import de.walware.statet.r.nico.ui.RConsole;
import de.walware.statet.r.ui.RUI;


/**
 *
 */
public class RConsoleLaunchDelegate extends LaunchConfigurationDelegate {
	
	
	public void launch(ILaunchConfiguration configuration, String mode,	ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		
		try {
			final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(false);
			monitor = LaunchConfigUtil.initProgressMonitor(configuration, monitor, 25);
			if (monitor.isCanceled()) {
				return;
			}
			
			// r env
			REnvConfiguration renv = REnvTab.getREnv(configuration);
//			renv.validate();
			
			// working directory
			IFileStore workingDirectory = REnvTab.getWorkingDirectory(configuration);

			String type = configuration.getAttribute(RConsoleMainTab.ATTR_TYPE, "").trim(); //$NON-NLS-1$
			if (!type.equals("rterm")) { //$NON-NLS-1$
				throw new CoreException(new Status(Status.ERROR, RUI.PLUGIN_ID, IStatetStatusConstants.LAUNCHCONFIG_ERROR,
						NLS.bind("R Console launch type ''{0}'' is not available.", type), null));
			}

			monitor.worked(1);
			if (monitor.isCanceled()) {
				return;
			}

// rterm --
			List<String> cmdLine = new ArrayList<String>();
			cmdLine.addAll(0, renv.getExecCommand(Exec.TERM));
			if (Platform.getOS().startsWith("win")) {
				cmdLine.add("--ess");
			}
			
			// arguments
			cmdLine.addAll(Arrays.asList(
					LaunchConfigUtil.getProcessArguments(configuration, RConsoleMainTab.ATTR_OPTIONS) ));

			monitor.worked(1);
			if (monitor.isCanceled()) {
				return;
			}
			
			ProcessBuilder builder = new ProcessBuilder(cmdLine);
			builder.directory(workingDirectory.toLocalFile(EFS.NONE, null));
			
			// environment
			Map<String, String> envp = builder.environment();
			envp.putAll(renv.getEnvironmentsVariables());
			LaunchConfigUtil.configureEnvironment(configuration, envp);
			
			String encoding = configuration.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, "");
			Charset charset;
			try {
				if (encoding.length() > 0) {
					charset = Charset.forName(encoding);
				}
				else {
					charset = Charset.defaultCharset();
				}
			} catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, IStatetStatusConstants.LAUNCHCONFIG_ERROR,
						NLS.bind("Invalid or unsupported console encoding ''{0}''.", encoding), e));
			}

			monitor.worked(1);
			if (monitor.isCanceled()) {
				return;
			}
			
			// create process
			UnterminatedLaunchAlerter.registerLaunchType(IRLaunchConfigurationConstants.ID_R_CONSOLE_CONFIGURATION_TYPE);
			
			String name = cmdLine.get(0);
			name += ' ' + LaunchConfigUtil.createProcessTimestamp();
			final ToolProcess<RWorkspace> process = new ToolProcess<RWorkspace>(launch,
					IRLaunchConfigurationConstants.ID_R_CONSOLE_PROCESS_TYPE,
					LaunchConfigUtil.createLaunchPrefix(configuration), renv.getName() + " : R Console/Rterm ~ " + name);
			process.setAttribute(IProcess.ATTR_CMDLINE, LaunchConfigUtil.generateCommandLine(cmdLine));
			
			RTermController controller = new RTermController(process, builder, charset);
			process.init(controller);
			controller.addEventHandler(ToolController.SCHEDULE_QUIT_EVENT_ID, new QuitHandler());
			
			monitor.worked(5);
			
			final NIConsole console = new RConsole(process, new NIConsoleColorAdapter());
	    	NicoUITools.startConsoleLazy(console, page);
	    	// start
	    	new ToolRunner().runInBackgroundThread(process, new ExceptionHandler.StatusHandler());
		}
		finally {
			monitor.done();
		}
	}
	
}
