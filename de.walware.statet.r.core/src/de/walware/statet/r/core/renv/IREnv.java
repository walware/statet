/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.renv;


/**
 * Reference to an R "runtime" environment (configuration).
 * Stays up-to-date even the configuration changed.
 */
public interface IREnv {
	
	/**
	 * The R environment id for the default configuration in the workbench
	 */
	String DEFAULT_WORKBENCH_ENV_ID = "default-workbench"; //$NON-NLS-1$
	
	/**
	 * The prefix of R environment ids for user defined configurations
	 */
	String USER_ENV_ID_PREFIX = "user-"; //$NON-NLS-1$
	
	/**
	 * The prefix of R environment ids for user defined configurations of local R installations
	 */
	String USER_LOCAL_ENV_ID_PREFIX = USER_ENV_ID_PREFIX + "local-"; //$NON-NLS-1$
	
	/**
	 * The prefix of R environment ids for user defined configurations of remote R installations
	 */
	String USER_REMOTE_ENV_ID_PREFIX = USER_ENV_ID_PREFIX + "remote-"; //$NON-NLS-1$
	
	
	/**
	 * The id of the R environment.
	 * 
	 * The id of an instance of {@link IREnv} and {@link IREnvConfiguration} never changes.
	 * 
	 * @return the id
	 */
	String getId();
	
	/**
	 * The current name of the R environment.
	 * 
	 * @return the name
	 */
	String getName();
	
	/**
	 * Resolves finally the reference and returns the configuration, if available.
	 * 
	 * @return the configuration if available, otherwise <code>null</code>.
	 */
	IREnvConfiguration getConfig();
	
	/**
	 * Resolves virtual references (like workbench default) to references of a real configuration.
	 * 
	 * A virtual reference can return <code>null</code>, if it doesn't currently point to a valid 
	 * configuration. A reference of a real configuration always return itself.
	 * 
	 * @return the final reference, if available, otherwise <code>null</code>.
	 */
	IREnv resolve();
	
}
