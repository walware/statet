/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.pkgmanager;

import de.walware.ecommons.preferences.Preference;

import de.walware.statet.r.core.pkgmanager.RRepo;


public class RRepoPref extends Preference<RRepo> {
	
	
	public RRepoPref(final String qualifier, final String key) {
		super(qualifier, key, Type.STRING);
	}
	
	
	@Override
	public Class<RRepo> getUsageType() {
		return RRepo.class;
	}
	
	@Override
	public RRepo store2Usage(final Object obj) {
		final String s = (String) obj;
		if (s != null && !s.isEmpty()) {
			final String[] parts = IS1_SEPARATOR_PATTERN.split(s); 
			if (parts.length >= 3) {
				return RVarRepo.create(parts[0].intern(), parts[1], parts[2],
						(parts.length >= 4) ? Util.getPkgType(parts[3]) : null );
			}
		}
		return null;
	}
	
	@Override
	public Object usage2Store(final RRepo repo) {
		if (repo == null) {
			return null;
		}
		
		final StringBuilder sb = new StringBuilder(32);
		sb.append(repo.getId());
		sb.append(IS1_SEPARATOR_CHAR);
		sb.append(repo.getName());
		sb.append(IS1_SEPARATOR_CHAR);
		sb.append((repo instanceof RVarRepo) ? ((RVarRepo) repo).getRawURL() : repo.getURL());
		sb.append(IS1_SEPARATOR_CHAR);
		if (repo.getPkgType() != null) {
			sb.append(repo.getPkgType());
		}
		return sb.toString();
	}
	
}
