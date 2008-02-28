/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
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
 * 
 */
public interface IREnvManager {
	
	
	public static final String SETTINGS_GROUP_ID = "r.envs"; //$NON-NLS-1$
	
	
	public String[] set(REnvConfiguration[] configs, String defaultConfigName) throws CoreException;
	
	public Lock getReadLock();
	public String[] getNames();
	public REnvConfiguration get(String id, String name);
	public REnvConfiguration getDefault();
	
}
