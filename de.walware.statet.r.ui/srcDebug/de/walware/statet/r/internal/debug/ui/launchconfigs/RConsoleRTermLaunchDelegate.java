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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
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

import de.walware.statet.r.core.renv.REnvConfiguration;
import de.walware.statet.r.core.renv.REnvConfiguration.Exec;
import de.walware.statet.r.debug.ui.launchconfigs.REnvTab;
import de.walware.statet.r.debug.ui.launchconfigs.RLaunchConfigurations;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.RConsoleLaunching;
import de.walware.statet.r.nico.RTool;
import de.walware.statet.r.nico.RWorkspace;
import de.walware.statet.r.nico.impl.RTermController;
import de.walware.statet.r.nico.ui.RConsole;
import de.walware.statet.r.ui.RUI;


public class RConsoleRTermLaunchDelegate implements ILaunchConfigurationDelegate {
	
	
	public void launch(final ILaunchConfiguration configuration, final String mode,
			final ILaunch launch, final IProgressMonitor monitor) throws CoreException {
		final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(false);
		final SubMonitor progress = SubMonitor.convert(monitor, 15);
		
		progress.worked(1);
		if (progress.isCanceled()) {
			return;
		}
		
		// r env
		final REnvConfiguration renv = REnvTab.getREnv(configuration);
//		renv.validate();
		
		// working directory
		final IFileStore workingDirectory = REnvTab.getWorkingDirectory(configuration);
		
		progress.worked(1);
		if (progress.isCanceled()) {
			return;
		}
		
		final List<String> cmdLine = new ArrayList<String>();
		cmdLine.addAll(0, renv.getExecCommand(Exec.TERM));
		if (Platform.getOS().startsWith("win")) { //$NON-NLS-1$
			cmdLine.add("--ess"); //$NON-NLS-1$
		}
		
		// arguments
		cmdLine.addAll(Arrays.asList(
				LaunchConfigUtil.getProcessArguments(configuration, RConsoleLaunching.ATTR_OPTIONS) ));
		
		progress.worked(1);
		if (progress.isCanceled()) {
			return;
		}
		
		final ProcessBuilder builder = new ProcessBuilder(cmdLine);
		builder.directory(workingDirectory.toLocalFile(EFS.NONE, null));
		
		// environment
		final Map<String, String> envp = builder.environment();
		LaunchConfigUtil.configureEnvironment(envp, configuration, renv.getEnvironmentsVariables());
		
		final String encoding = configuration.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, ""); //$NON-NLS-1$
		Charset charset;
		try {
			if (encoding.length() > 0) {
				charset = Charset.forName(encoding);
			}
			else {
				charset = Charset.defaultCharset();
			}
		} catch (final Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
					NLS.bind(RLaunchingMessages.LaunchDelegate_error_InvalidUnsupportedConsoleEncoding_message, encoding), e));
		}
		
		progress.worked(1);
		if (progress.isCanceled()) {
			return;
		}
		
		// create process
		UnterminatedLaunchAlerter.registerLaunchType(RLaunchConfigurations.ID_R_CONSOLE_CONFIGURATION_TYPE);
		
		String name = cmdLine.get(0);
		name += ' ' + LaunchConfigUtil.createProcessTimestamp();
		final ToolProcess<RWorkspace> process = new ToolProcess<RWorkspace>(launch, RTool.TYPE,
				LaunchConfigUtil.createLaunchPrefix(configuration), renv.getName() + " : R Console/Rterm ~ " + name, //$NON-NLS-1$
				null);
		process.setAttribute(IProcess.ATTR_CMDLINE, LaunchConfigUtil.generateCommandLine(cmdLine));
		
		final RTermController controller = new RTermController(process, builder, charset);
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
