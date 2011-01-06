/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.renv;

import java.util.concurrent.locks.Lock;

import org.eclipse.core.runtime.CoreException;


/**
 * Manages R environment configurations.
 */
public interface IREnvManager {
	
	
	String SETTINGS_GROUP_ID = "r.envs"; //$NON-NLS-1$
	
	
	String[] set(IREnvConfiguration[] configs, String defaultConfigName) throws CoreException;
	
	Lock getReadLock();
	IREnvConfiguration[] getConfigurations();
	
	IREnv get(String id, String name);
	IREnv getDefault();
	IREnvConfiguration.WorkingCopy newConfiguration(String type);
	
}
