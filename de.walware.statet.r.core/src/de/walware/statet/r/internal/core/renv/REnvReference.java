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


public class REnvReference implements IREnv {
	
	
	public static final String ID_DEFAULT_PREFIX = "default-"; //$NON-NLS-1$
	
	public static String updateId(final String id) {
		if (id == null) {
			return null;
		}
		if (id.startsWith(IREnv.USER_ENV_ID_PREFIX) || id.startsWith(ID_DEFAULT_PREFIX)) { 
			return id;
		}
		if (id.equals("default/workbench")) { //$NON-NLS-1$
			return IREnv.DEFAULT_WORKBENCH_ENV_ID;
		}
		int num = -1;
		for (int i = id.length()-1; i >= 0; i--) {
			final char c = id.charAt(i);
			if (c < '0' || c > '9') {
				num = i+1;
				break;
			}
		}
		if (num < 0) {
			return IREnv.USER_LOCAL_ENV_ID_PREFIX + id;
		}
		final long time = (num == id.length()) ? 0L : Long.parseLong(id.substring(num));
		return IREnv.USER_LOCAL_ENV_ID_PREFIX + Long.toString(
				((long) id.substring(0, num).hashCode() << 32) | time,
				36);
	}
	
	
	private final String fId;
	
	
	String fName;
	IREnvConfiguration fConfig;
	
	
	public REnvReference(final String id) {
		fId = id.intern();
	}
	
	
	@Override
	public String getId() {
		return fId;
	}
	
	@Override
	public String getName() {
		return fName;
	}
	
	@Override
	public IREnvConfiguration getConfig() {
		return fConfig;
	}
	
	@Override
	public IREnv resolve() {
		return this;
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
