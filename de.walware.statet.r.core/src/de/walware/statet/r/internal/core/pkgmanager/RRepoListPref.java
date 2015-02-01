/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.pkgmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.walware.ecommons.preferences.Preference;

import de.walware.statet.r.core.pkgmanager.RRepo;


public class RRepoListPref extends Preference<List<RRepo>> {
	
	
	public RRepoListPref(final String qualifier, final String key) {
		super(qualifier, key, Type.STRING);
	}
	
	
	@Override
	public Class<List<RRepo>> getUsageType() {
		return (Class) List.class;
	}
	
	@Override
	public List<RRepo> store2Usage(final Object obj) {
		final String s = (String) obj;
		if (s == null || s.isEmpty()) {
			return Collections.emptyList();
		}
		final String[] repos = IS2_SEPARATOR_PATTERN.split(s);
		final List<RRepo> list = new ArrayList<RRepo>(repos.length);
		for (int i = 0; i < repos.length; i++) {
			final String[] parts = IS1_SEPARATOR_PATTERN.split(repos[i]);
			if (parts.length >= 3) {
				list.add(RVarRepo.create(parts[0].intern(), parts[1], parts[2],
						(parts.length >= 4) ? Util.getPkgType(parts[3]) : null ));
			}
		}
		return list;
	}
	
	@Override
	public Object usage2Store(final List<RRepo> obj) {
		if (obj == null) {
			return null;
		}
		if (obj.isEmpty()) {
			return ""; //$NON-NLS-1$
		}
		
		final StringBuilder sb = new StringBuilder(32 * obj.size());
		
		for (int i = 0; i < obj.size(); i++) {
			final RRepo repo = obj.get(i);
			sb.append(repo.getId());
			sb.append(IS1_SEPARATOR_CHAR);
			sb.append(repo.getName());
			sb.append(IS1_SEPARATOR_CHAR);
			sb.append((repo instanceof RVarRepo) ? ((RVarRepo) repo).getRawURL() : repo.getURL());
			sb.append(IS1_SEPARATOR_CHAR);
			if (repo.getPkgType() != null) {
				sb.append(repo.getPkgType());
			}
			sb.append(IS2_SEPARATOR_CHAR);
		}
		return sb.substring(0, sb.length()-1);
	}
	
}
