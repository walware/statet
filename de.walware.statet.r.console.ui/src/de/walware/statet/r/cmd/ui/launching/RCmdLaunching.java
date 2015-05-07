/*=============================================================================#
 # Copyright (c) 2011-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.cmd.ui.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;

import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.internal.cmd.ui.launching.RCmdMainTab;
import de.walware.statet.r.launching.core.RLaunching;


public class RCmdLaunching {
	
	
	public static final String R_CMD_CONFIGURATION_TYPE_ID = "de.walware.statet.r.launchConfigurationTypes.RCmdTool"; //$NON-NLS-1$
	
	public static final String R_CMD_PROCESS_TYPE = "R.cmd"; //$NON-NLS-1$
	
	public static final String R_CMD_COMMAND_ATTR_NAME = RCmdMainTab.NS + "arguments.cmd"; //$NON-NLS-1$
	public static final String R_CMD_OPTIONS_ATTR_NAME= RCmdMainTab.NS + "arguments.options"; //$NON-NLS-1$
	public static final String R_CMD_RESOURCE_ATTR_NAME= RCmdMainTab.NS + "arguments.resource"; //$NON-NLS-1$
	
	public static final String WORKING_DIRECTORY_ATTR_NAME= RLaunching.ATTR_WORKING_DIRECTORY;
	
	public static final String RENV_CODE_ATTR_NAME= RLaunching.ATTR_RENV_CODE;
	
	
	public static ILaunchConfigurationWorkingCopy createNewRCmdConfig(final String name, final String cmd) throws CoreException {
		final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		final ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(R_CMD_CONFIGURATION_TYPE_ID);
		final ILaunchConfigurationWorkingCopy config = type.newInstance(null, name);
		
		initializeRCmdConfig(config, cmd);
		
		return config;
	}
	
	public static void initializeRCmdConfig(final ILaunchConfigurationWorkingCopy config,
			final String cmd) {
		new EnvironmentTab().setDefaults(config);
		new CommonTab().setDefaults(config);
		config.setAttribute(R_CMD_COMMAND_ATTR_NAME, cmd);
		config.setAttribute(R_CMD_OPTIONS_ATTR_NAME, ""); //$NON-NLS-1$
		config.setAttribute(R_CMD_RESOURCE_ATTR_NAME, "${resource_loc}"); //$NON-NLS-1$
		config.setAttribute(WORKING_DIRECTORY_ATTR_NAME, "${container_loc}"); //$NON-NLS-1$
		config.setAttribute(RENV_CODE_ATTR_NAME, IREnv.DEFAULT_WORKBENCH_ENV_ID);
	}
	
	
	private RCmdLaunching() {
	}
	
}
