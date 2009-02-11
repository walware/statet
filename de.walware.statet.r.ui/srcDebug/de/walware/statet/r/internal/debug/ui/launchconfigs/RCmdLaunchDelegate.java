/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launchconfigs;

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
import org.eclipse.core.runtime.Status;
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

import de.walware.ecommons.FileUtil;
import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.debug.ui.LaunchConfigUtil;
import de.walware.ecommons.debug.ui.UnterminatedLaunchAlerter;

import de.walware.statet.r.core.renv.REnvConfiguration;
import de.walware.statet.r.core.renv.REnvConfiguration.Exec;
import de.walware.statet.r.debug.ui.launchconfigs.REnvTab;
import de.walware.statet.r.debug.ui.launchconfigs.RErrorLineTracker;
import de.walware.statet.r.debug.ui.launchconfigs.RLaunchConfigurations;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.ui.RUI;


/**
 * 
 */
public class RCmdLaunchDelegate extends LaunchConfigurationDelegate {
	
	
	public void launch(final ILaunchConfiguration configuration, final String mode,	final ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		
		try {
			monitor = LaunchConfigUtil.initProgressMonitor(configuration, monitor, 25);
			if (monitor.isCanceled()) {
				return;
			}
			
			final List<String> cmdLine = new ArrayList<String>();
			
			// r env
			final REnvConfiguration renv = REnvTab.getREnv(configuration);
//			renv.validate();
			
			final String cmd = configuration.getAttribute(RLaunchConfigurations.ATTR_R_CMD_COMMAND, "").trim(); //$NON-NLS-1$
			if (cmd.length() != 0) {
				cmdLine.addAll(Arrays.asList(cmd.split(" "))); //$NON-NLS-1$
			}
			String arg1 = null;
			if (cmdLine.size() > 0) {
				arg1 = cmdLine.remove(0);
			}
			cmdLine.addAll(0, renv.getExecCommand(arg1, EnumSet.of(Exec.CMD, Exec.TERM)));
			
			monitor.worked(1);
			if (monitor.isCanceled()) {
				return;
			}
			
			// working directory
			final IFileStore workingDirectory = REnvTab.getWorkingDirectory(configuration);
			
			monitor.worked(1);
			if (monitor.isCanceled()) {
				return;
			}
			
			// arguments
			cmdLine.addAll(Arrays.asList(
					LaunchConfigUtil.getProcessArguments(configuration, RLaunchConfigurations.ATTR_R_CMD_OPTIONS) ));
			
			final String resourceValue = configuration.getAttribute(RLaunchConfigurations.ATTR_R_CMD_RESOURCE, ""); //$NON-NLS-1$
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
			
			monitor.worked(1);
			if (monitor.isCanceled()) {
				return;
			}
			
			final ProcessBuilder builder = new ProcessBuilder(cmdLine);
			builder.directory(workingDirectory.toLocalFile(EFS.NONE, null));
			
			// environment
			final Map<String, String> envp = builder.environment();
			LaunchConfigUtil.configureEnvironment(envp, configuration, renv.getEnvironmentsVariables());
			
			// exec process
			UnterminatedLaunchAlerter.registerLaunchType(RLaunchConfigurations.ID_R_CMD_CONFIGURATION_TYPE);
			Process p;
			try {
				p = builder.start();
			} catch (final IOException e) {
				throw new CoreException(new Status(Status.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHING,
						RLaunchingMessages.LaunchDelegate_error_StartingExec, e));
			}
			monitor.worked(10);
			
			// register process
			final Map<String, String> processAttributes = new HashMap<String, String>();
			processAttributes.put(IProcess.ATTR_PROCESS_TYPE, RLaunchConfigurations.ID_R_CMD_PROCESS_TYPE);
			final String processName = cmdLine.get(0) + ' ' + LaunchConfigUtil.createProcessTimestamp();
			final String label;
			{
				final StringBuilder sb = new StringBuilder(200);
				sb.append(LaunchConfigUtil.createLaunchPrefix(configuration));
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
				throw new CoreException(new Status(Status.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHING,
						RLaunchingMessages.LaunchDelegate_error_ProcessHandle, null));
			}
			process.setAttribute(IProcess.ATTR_CMDLINE, LaunchConfigUtil.generateCommandLine(cmdLine));
			process.setAttribute(IProcess.ATTR_PROCESS_LABEL, label);
			
			monitor.worked(5);
			if (!process.isTerminated() && !CommonTab.isLaunchInBackground(configuration)) {
				monitor.subTask(RLaunchingMessages.RCmd_LaunchDelegate_Running_label);
			}
			
			final IConsole console = DebugUITools.getConsole(process);
			if (console instanceof TextConsole) {
				final RErrorLineTracker lineMatcher = new RErrorLineTracker(workingDirectory);
				((TextConsole) console).addPatternMatchListener(lineMatcher);
			}
			
			LaunchConfigUtil.launchResourceRefresh(configuration, process, new SubProgressMonitor(monitor, 5));
		}
		finally {
			monitor.done();
		}
	}
	
}
