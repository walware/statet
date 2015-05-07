/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.cmd.ui.launching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.TextConsole;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.debug.core.util.LaunchUtils;
import de.walware.ecommons.debug.ui.UnterminatedLaunchAlerter;
import de.walware.ecommons.debug.ui.config.LaunchConfigUtil;
import de.walware.ecommons.io.FileUtil;

import de.walware.statet.r.cmd.ui.launching.RCmdLaunching;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.renv.IREnvConfiguration.Exec;
import de.walware.statet.r.internal.console.ui.RConsoleUIPlugin;
import de.walware.statet.r.launching.core.RLaunching;
import de.walware.statet.r.launching.ui.REnvTab;
import de.walware.statet.r.launching.ui.RErrorLineTracker;


public class RCmdLaunchDelegate extends LaunchConfigurationDelegate {
	
	
	public RCmdLaunchDelegate() {
	}
	
	
	@Override
	public void launch(final ILaunchConfiguration configuration, final String mode,	final ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		final SubMonitor m= LaunchUtils.initProgressMonitor(configuration, monitor, 25);
		final long timestamp= System.currentTimeMillis();
		try {
			if (m.isCanceled()) {
				return;
			}
			
			final List<String> cmdLine = new ArrayList<>();
			
			// r env
			final IREnvConfiguration renv = RLaunching.getREnvConfig(configuration, true);
//			renv.validate();
			
			final String cmd = configuration.getAttribute(RCmdLaunching.R_CMD_COMMAND_ATTR_NAME, "").trim(); //$NON-NLS-1$
			if (cmd.length() != 0) {
				cmdLine.addAll(Arrays.asList(cmd.split(" "))); //$NON-NLS-1$
			}
			String arg1 = null;
			if (cmdLine.size() > 0) {
				arg1 = cmdLine.remove(0);
			}
			cmdLine.addAll(0, renv.getExecCommand(arg1, EnumSet.of(Exec.CMD, Exec.TERM)));
			
			m.worked(1);
			if (m.isCanceled()) {
				return;
			}
			
			// working directory
			final IFileStore workingDirectory = REnvTab.getWorkingDirectory(configuration);
			
			m.worked(1);
			if (m.isCanceled()) {
				return;
			}
			
			// arguments
			cmdLine.addAll(Arrays.asList(
					LaunchUtils.getProcessArguments(configuration, RCmdLaunching.R_CMD_OPTIONS_ATTR_NAME) ));
			
			final String resourceValue = configuration.getAttribute(RCmdLaunching.R_CMD_RESOURCE_ATTR_NAME, ""); //$NON-NLS-1$
			IFileStore resource = null;
			IPath resourcePathAbsolute = null;
			IPath resourcePathAuto = null;
			if (resourceValue.length() > 0) {
				resource = FileUtil.expandToLocalFileStore(resourceValue, workingDirectory, null);
				final IPath workingDirectoryPath = URIUtil.toPath(workingDirectory.toURI());
				resourcePathAuto = resourcePathAbsolute = URIUtil.toPath(resource.toURI());
				if (workingDirectoryPath.isPrefixOf(resourcePathAuto)) {
					resourcePathAuto = resourcePathAuto.setDevice(null);
					resourcePathAuto = resourcePathAuto.removeFirstSegments(workingDirectoryPath.segmentCount());
				}
				cmdLine.add(resourcePathAuto.toString());
			}
			
			m.worked(1);
			if (m.isCanceled()) {
				return;
			}
			
			final ProcessBuilder builder = new ProcessBuilder(cmdLine);
			builder.directory(workingDirectory.toLocalFile(EFS.NONE, null));
			
			// environment
			final Map<String, String> envp = builder.environment();
			LaunchUtils.configureEnvironment(envp, configuration, renv.getEnvironmentsVariables());
			
			// exec process
			UnterminatedLaunchAlerter.registerLaunchType(RCmdLaunching.R_CMD_CONFIGURATION_TYPE_ID);
			Process p;
			try {
				p = builder.start();
			} catch (final IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, RConsoleUIPlugin.PLUGIN_ID,
						ICommonStatusConstants.LAUNCHING,
						RCmdMessages.RCmd_LaunchDelegate_error_StartingExec, e ));
			}
			m.worked(10);
			
			// register process
			final Map<String, String> processAttributes = new HashMap<>();
			processAttributes.put(IProcess.ATTR_PROCESS_TYPE, RCmdLaunching.R_CMD_PROCESS_TYPE);
			final String processName = cmdLine.get(0) + ' ' + LaunchUtils.createProcessTimestamp(timestamp);
			final String label;
			{
				final StringBuilder sb = new StringBuilder(200);
				sb.append(LaunchUtils.createLaunchPrefix(configuration));
				sb.append(' ').append(renv.getName());
				sb.append(" : R ").append(cmd); //$NON-NLS-1$
				if (resourcePathAbsolute != null) {
					sb.append(' ').append(resourcePathAbsolute.toOSString());
				}
				sb.append(" ~ ").append(processName); //$NON-NLS-1$
				label = sb.toString();
			}
			
			final IProcess process = DebugPlugin.newProcess(launch, p, processName, processAttributes);
			if (process == null) {
				p.destroy();
				throw new CoreException(new Status(IStatus.ERROR, RConsoleUIPlugin.PLUGIN_ID,
						ICommonStatusConstants.LAUNCHING,
						RCmdMessages.RCmd_LaunchDelegate_error_ProcessHandle, null ));
			}
			process.setAttribute(IProcess.ATTR_CMDLINE, LaunchUtils.generateCommandLine(cmdLine));
			process.setAttribute(IProcess.ATTR_PROCESS_LABEL, label);
			
			m.worked(5);
			if (!process.isTerminated() && !CommonTab.isLaunchInBackground(configuration)) {
				m.subTask(RCmdMessages.RCmd_LaunchDelegate_Running_label);
			}
			
			final IConsole console = DebugUITools.getConsole(process);
			if (console instanceof TextConsole) {
				final RErrorLineTracker lineMatcher = new RErrorLineTracker(workingDirectory);
				((TextConsole) console).addPatternMatchListener(lineMatcher);
			}
			
			LaunchConfigUtil.launchResourceRefresh(configuration, process, new SubProgressMonitor(m, 5));
		}
		finally {
			m.done();
		}
	}
	
}
