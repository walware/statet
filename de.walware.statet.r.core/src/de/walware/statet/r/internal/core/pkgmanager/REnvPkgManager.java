/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.pkgmanager;

import java.util.HashMap;
import java.util.Map;

import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.internal.core.renv.REnvManager;


public class REnvPkgManager {
	
	
	private final REnvManager fREnvManager;
	
	private final Map<IREnv, RPkgManager> fRPkgManagers = new HashMap<IREnv, RPkgManager>();
	
	
	public REnvPkgManager(final REnvManager rEnvManager) {
		fREnvManager = rEnvManager;
	}
	
	
	public synchronized RPkgManager getManager(IREnv env) {
		env = env.resolve();
		RPkgManager mgr = fRPkgManagers.get(env);
		if (mgr == null) {
			final IREnvConfiguration config = env.getConfig();
			if (config == null || config.isDeleted()) {
				return null;
			}
			mgr = new RPkgManager(config);
			fRPkgManagers.put(env, mgr);
		}
		return mgr;
	}
	
}
