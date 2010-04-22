/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.debug.ui.launchconfigs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;

import de.walware.statet.nico.core.runtime.ToolProcess;

import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.internal.debug.ui.launchconfigs.RCmdMainTab;


/**
 * 
 */
public class RLaunchConfigurations {
	
	public static final String ID_R_CMD_CONFIGURATION_TYPE = "de.walware.statet.r.launchConfigurationTypes.RCmdTool"; //$NON-NLS-1$
	public static final String ID_R_CMD_PROCESS_TYPE = "R.cmd"; //$NON-NLS-1$
	
	public static final String ID_R_CONSOLE_CONFIGURATION_TYPE = "de.walware.statet.r.launchConfigurationTypes.RConsole"; //$NON-NLS-1$
	public static final String ID_R_REMOTE_CONSOLE_CONFIGURATION_TYPE = "de.walware.statet.r.launchConfigurationTypes.RRemoteConsole"; //$NON-NLS-1$
	public static final String ID_R_CONSOLE_PROCESS_TYPE = "R"+ToolProcess.PROCESS_TYPE_SUFFIX; //$NON-NLS-1$
	
	
	public static final String ATTR_R_CMD_COMMAND = RCmdMainTab.NS+"arguments.cmd"; //$NON-NLS-1$
	public static final String ATTR_R_CMD_OPTIONS = RCmdMainTab.NS+"arguments.options"; //$NON-NLS-1$
	public static final String ATTR_R_CMD_RESOURCE = RCmdMainTab.NS+"arguments.resource"; //$NON-NLS-1$
	public static final String ATTR_RENV_SETTING = REnvTab.NS+"REnvSetting"; //$NON-NLS-1$
	public static final String ATTR_WORKING_DIRECTORY = REnvTab.NS+"workingDirectory"; //$NON-NLS-1$
	
	
	public static ILaunchConfigurationWorkingCopy createNewRCmdConfig(final String name, final String cmd) throws CoreException {
		final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		final ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(RLaunchConfigurations.ID_R_CMD_CONFIGURATION_TYPE);
		final ILaunchConfigurationWorkingCopy config = type.newInstance(null, name);
		
		new EnvironmentTab().setDefaults(config);
		new CommonTab().setDefaults(config);
		config.setAttribute(RLaunchConfigurations.ATTR_R_CMD_COMMAND, cmd);
		config.setAttribute(RLaunchConfigurations.ATTR_R_CMD_OPTIONS, ""); //$NON-NLS-1$
		config.setAttribute(RLaunchConfigurations.ATTR_R_CMD_RESOURCE, "${resource_loc}"); //$NON-NLS-1$
		config.setAttribute(RLaunchConfigurations.ATTR_WORKING_DIRECTORY, "${container_loc}"); //$NON-NLS-1$
		config.setAttribute(RLaunchConfigurations.ATTR_RENV_SETTING, IREnv.DEFAULT_WORKBENCH_ENV_ID);
		return config;
	}
	
	
	private RLaunchConfigurations() {
	}
	
}
