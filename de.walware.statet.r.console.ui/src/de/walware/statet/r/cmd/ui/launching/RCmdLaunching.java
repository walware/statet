/*******************************************************************************
 * Copyright (c) 2011-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

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
	
	public static final String ATTR_R_CMD_COMMAND = RCmdMainTab.NS+"arguments.cmd"; //$NON-NLS-1$
	public static final String ATTR_R_CMD_OPTIONS = RCmdMainTab.NS+"arguments.options"; //$NON-NLS-1$
	public static final String ATTR_R_CMD_RESOURCE = RCmdMainTab.NS+"arguments.resource"; //$NON-NLS-1$
	
	
	public static ILaunchConfigurationWorkingCopy createNewRCmdConfig(final String name, final String cmd) throws CoreException {
		final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		final ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(R_CMD_CONFIGURATION_TYPE_ID);
		final ILaunchConfigurationWorkingCopy config = type.newInstance(null, name);
		
		new EnvironmentTab().setDefaults(config);
		new CommonTab().setDefaults(config);
		config.setAttribute(ATTR_R_CMD_COMMAND, cmd);
		config.setAttribute(ATTR_R_CMD_OPTIONS, ""); //$NON-NLS-1$
		config.setAttribute(ATTR_R_CMD_RESOURCE, "${resource_loc}"); //$NON-NLS-1$
		config.setAttribute(RLaunching.ATTR_WORKING_DIRECTORY, "${container_loc}"); //$NON-NLS-1$
		config.setAttribute(RLaunching.ATTR_RENV_CODE, IREnv.DEFAULT_WORKBENCH_ENV_ID);
		return config;
	}
	
	
	private RCmdLaunching() {
	}
	
}
