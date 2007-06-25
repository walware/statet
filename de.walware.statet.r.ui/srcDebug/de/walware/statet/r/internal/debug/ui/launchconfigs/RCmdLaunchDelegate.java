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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
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

import de.walware.eclipsecommons.FileUtil;

import de.walware.statet.base.IStatetStatusConstants;
import de.walware.statet.base.ui.debug.LaunchConfigUtil;
import de.walware.statet.base.ui.debug.UnterminatedLaunchAlerter;
import de.walware.statet.r.core.renv.REnvConfiguration;
import de.walware.statet.r.debug.ui.launchconfigs.IRLaunchConfigurationConstants;
import de.walware.statet.r.debug.ui.launchconfigs.REnvTab;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.ui.RUI;


/**
 *
 */
public class RCmdLaunchDelegate extends LaunchConfigurationDelegate {
	
	public void launch(ILaunchConfiguration configuration, String mode,	ILaunch launch, 
			IProgressMonitor monitor) throws CoreException {
		
		try {
			monitor.beginTask(RLaunchingMessages.LaunchDelegate_Running, 25);
			if (monitor.isCanceled()) {
				return;
			}
			
			List<String> cmdLine = new ArrayList<String>();
	
			// r env
			REnvConfiguration renv = REnvTab.getREnv(configuration);
//			renv.validate();
			IPath r = FileUtil.expandToLocalPath(renv.getRHome(), "bin/R"); //$NON-NLS-1$
			cmdLine.add(r.toOSString());
			
			monitor.worked(1);
			if (monitor.isCanceled()) {
				return;
			}
			
			// working directory
			IFileStore workingDirectory = REnvTab.getWorkingDirectory(configuration);
			
			monitor.worked(1);
			if (monitor.isCanceled()) {
				return;
			}
			
			monitor.worked(1);
			if (monitor.isCanceled()) {
				return;
			}
	
			// arguments
			String cmd = configuration.getAttribute(RCmdMainTab.ATTR_CMD, (String) "").trim(); //$NON-NLS-1$
			if (cmd.length() != 0) {
				cmdLine.addAll(Arrays.asList(cmd.split(" "))); //$NON-NLS-1$
			}
			cmdLine.addAll(Arrays.asList(
					LaunchConfigUtil.getProcessArguments(configuration, RCmdMainTab.ATTR_OPTIONS) ));
	
			String resource = configuration.getAttribute(RCmdMainTab.ATTR_RESOURCE, ""); //$NON-NLS-1$
			IPath resourcePath = null;
			if (resource.length() > 0) {
				resourcePath = FileUtil.expandToLocalPath(resource, null);
				cmdLine.add(resourcePath.toOSString());
			}
	
			monitor.worked(1);
			if (monitor.isCanceled()) {
				return;
			}
	
			ProcessBuilder builder = new ProcessBuilder(cmdLine);
			if (workingDirectory != null) {
				builder.directory(workingDirectory.toLocalFile(EFS.NONE, null));
			}
			
			// environment
			Map<String, String> envp = builder.environment();
			LaunchConfigUtil.configureEnvironment(configuration, envp);
			envp.putAll(renv.getEnvironmentsVariables());

			// exec process
			UnterminatedLaunchAlerter.registerLaunchType(IRLaunchConfigurationConstants.ID_R_CMD_CONFIGURATION_TYPE);
			Process p;
			try {
				p = builder.start();
			} catch (IOException e) {
				throw new CoreException(new Status(Status.ERROR, RUI.PLUGIN_ID, IStatetStatusConstants.LAUNCHING_ERROR, 
						RLaunchingMessages.LaunchDelegate_error_StartingExec, e));
			}			
			monitor.worked(10);
			
			// register process
			Map<String, String> processAttributes = new HashMap<String, String>();
			processAttributes.put(IProcess.ATTR_PROCESS_TYPE, IRLaunchConfigurationConstants.ID_R_CMD_PROCESS_TYPE);
			String name = "R ('"+renv.getName()+"') "+cmd; //$NON-NLS-1$ //$NON-NLS-2$
			if (resourcePath != null) {
				name += " " + resourcePath.lastSegment(); //$NON-NLS-1$
			}
			IProcess process = DebugPlugin.newProcess(launch, p, name, processAttributes);
			if (process == null) {
				p.destroy();
				throw new CoreException(new Status(Status.ERROR, RUI.PLUGIN_ID, IStatetStatusConstants.LAUNCHING_ERROR,
						RLaunchingMessages.LaunchDelegate_error_ProcessHandle, null));
			}
			process.setAttribute(IProcess.ATTR_CMDLINE, LaunchConfigUtil.generateCommandLine(cmdLine));
			
			monitor.worked(5);
			
			LaunchConfigUtil.launchResourceRefresh(configuration, process, new SubProgressMonitor(monitor, 5));
		}
		finally {
			monitor.done();
		}
	}
	
}
