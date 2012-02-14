/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.launching.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariable;
import org.eclipse.debug.core.ILaunchConfiguration;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.variables.core.StringVariable;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.renv.REnvUtil;
import de.walware.statet.r.internal.launching.core.Messages;

public class RLaunching {
	
	
	public static final String CONFIG_RENV_NS = "de.walware.statet.r/renv"; //$NON-NLS-1$
	
	public static final String ATTR_RENV_CODE = CONFIG_RENV_NS + "/REnvCode"; //$NON-NLS-1$
	
	public static final String ATTR_WORKING_DIRECTORY = CONFIG_RENV_NS + "/WorkingDirectory"; //$NON-NLS-1$
	
	@Deprecated
	public static final String OLD_NS = "de.walware.statet.r.debug/REnv"; //$NON-NLS-1$
	@Deprecated
	public static final String OLD_ATTR_RENV_CODE = OLD_NS + "/REnvSetting"; //$NON-NLS-1$
	@Deprecated
	public static final String OLD_ATTR_WORKING_DIRECTORY = OLD_NS + "/workingDirectory"; //$NON-NLS-1$
	
	/**
	 * {@link IStringVariable String variable} name for the R working directory.
	 */
	public static final String WORKING_DIRECTORY_VARNAME = "r_wd"; //$NON-NLS-1$
	
	/**
	 * String variable for the R working directory.
	 * 
	 * Note: Listing and functionality must be explicitly implemented.
	 */
	public static final IStringVariable WORKING_DIRECTORY_VARIABLE = new StringVariable(WORKING_DIRECTORY_VARNAME, "The configured R working directory");
	
	
	public static IREnv readREnv(final ILaunchConfiguration configuration) throws CoreException {
		String code = configuration.getAttribute(OLD_ATTR_RENV_CODE, (String) null);
		if (code == null) {
			code = configuration.getAttribute(ATTR_RENV_CODE, (String) null);
		}
		return REnvUtil.decode(code, RCore.getREnvManager());
	}
	
	/**
	 * Reads the setting from the configuration, resolves the REnvironment and validates the configuration.
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public static IREnvConfiguration getREnvConfig(final ILaunchConfiguration configuration, final boolean local)
			throws CoreException {
		final IREnv rEnv = readREnv(configuration);
		final IREnvConfiguration config = (rEnv != null) ? rEnv.getConfig() : null;
		if (config == null) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
					Messages.REnv_Runtime_error_CouldNotFound_message, null));
		}
		
		final IStatus status = config.validate();
		if (status.getSeverity() == IStatus.ERROR) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
					Messages.REnv_Runtime_error_Invalid_message+' '+status.getMessage(), null));
		}
		if (local && !config.isLocal()) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, "The R environment configuration must specify a local R installation.", null));
		}
		return config;
	}
	
}
