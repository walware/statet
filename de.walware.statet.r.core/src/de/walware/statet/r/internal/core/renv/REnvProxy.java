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

package de.walware.statet.r.internal.core.renv;

import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;


public class REnvProxy implements IREnv {
	
	
	private final String fId;
	
	
	String fName;
	IREnv fLink;
	
	
	public REnvProxy(final String id) {
		fId = id;
	}
	
	
	@Override
	public String getId() {
		return fId;
	}
	
	@Override
	public String getName() {
		final IREnv rEnv = resolve();
		return (rEnv != null) ? rEnv.getName() : "";
	}
	
	@Override
	public IREnvConfiguration getConfig() {
		final IREnv rEnv = resolve();
		return (rEnv != null) ? rEnv.getConfig() : null;
	}
	
	@Override
	public IREnv resolve() {
		return fLink;
	}
	
	
	@Override
	public int hashCode() {
		return fId.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		return (obj instanceof IREnv && fId.equals(((IREnv) obj).getId()));
	}
	
}
