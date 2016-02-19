/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.renv;

import java.util.List;
import java.util.concurrent.locks.Lock;

import org.eclipse.core.runtime.CoreException;

import de.walware.jcommons.collections.ImList;

import de.walware.statet.r.core.RCore;


/**
 * Manages R environment configurations.
 */
public interface IREnvManager {
	
	
	String PREF_QUALIFIER= RCore.PLUGIN_ID + "/r.environments"; //$NON-NLS-1$ //$NON-NLS-1$
	
	
	void set(ImList<IREnvConfiguration> configs, String defaultConfigName) throws CoreException;
	
	Lock getReadLock();
	List<IREnvConfiguration> getConfigurations();
	
	IREnv get(String id, String name);
	IREnv getDefault();
	IREnvConfiguration.WorkingCopy newConfiguration(String type);
	
}
