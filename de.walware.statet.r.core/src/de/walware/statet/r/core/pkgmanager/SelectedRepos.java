/*******************************************************************************
 * Copyright (c) 2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.pkgmanager;

import java.util.List;


public class SelectedRepos implements ISelectedRepos {
	
	private final List<RRepo> fRepos;
	
	private final RRepo fCRANMirror;
	
	private final String fBioCVersion;
	private final RRepo fBioCMirror;
	
	
	public SelectedRepos(final List<RRepo> repos, final RRepo cranMirror,
			final String bioCVersion, final RRepo bioCMirror) {
		fRepos = repos;
		fCRANMirror = cranMirror;
		fBioCVersion = bioCVersion;
		fBioCMirror = bioCMirror;
	}
	
	
	@Override
	public List<RRepo> getRepos() {
		return fRepos;
	}
	
	@Override
	public RRepo getRepo(final String repoId) {
		if (repoId == RRepo.WS_CACHE_SOURCE_ID) {
			return RRepo.WS_CACHE_SOURCE_REPO;
		}
		if (repoId == RRepo.WS_CACHE_BINARY_ID) {
			return RRepo.WS_CACHE_BINARY_REPO;
		}
		return RPkgUtil.getRepoById(fRepos, repoId);
	}
	
	@Override
	public RRepo getCRANMirror() {
		return fCRANMirror;
	}
	
	@Override
	public RRepo getBioCMirror() {
		return fBioCMirror;
	}
	
	@Override
	public String getBioCVersion() {
		return fBioCVersion;
	}
	
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ISelectedRepos)) {
			return false;
		}
		final ISelectedRepos other = (ISelectedRepos) obj;
		return (fRepos.equals(other.getRepos())
				&& ((fCRANMirror != null) ? fCRANMirror.equals(other.getCRANMirror()) : null == other.getCRANMirror())
				&& ((fBioCMirror != null) ? fBioCMirror.equals(other.getBioCMirror()) : null == other.getBioCMirror()) );
	}
	
}
