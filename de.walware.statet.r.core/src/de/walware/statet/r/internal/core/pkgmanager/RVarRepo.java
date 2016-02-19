/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.pkgmanager;

import java.util.Collection;
import java.util.regex.Pattern;

import de.walware.rj.renv.RPkgType;

import de.walware.statet.r.core.pkgmanager.ISelectedRepos;
import de.walware.statet.r.core.pkgmanager.RRepo;


public class RVarRepo extends RRepo {
	
	
	static final String CRAN_MIRROR_VAR = "%cran"; //$NON-NLS-1$
	static final String BIOC_MIRROR_VAR = "%bm"; //$NON-NLS-1$
	static final String BIOC_VERSION_VAR = "%v"; //$NON-NLS-1$
	
	private static final Pattern CRAN_MIRROR_VAR_PATTERN = Pattern.compile(Pattern.quote(CRAN_MIRROR_VAR)+"/?");
	private static final Pattern BIOC_MIRROR_VAR_PATTERN = Pattern.compile(Pattern.quote(BIOC_MIRROR_VAR)+"/?");
	private static final Pattern BIOC_VERSION_VAR_PATTERN = Pattern.compile(Pattern.quote(BIOC_VERSION_VAR));
	
	
	static boolean hasVars(final String url) {
		return (url.indexOf(BIOC_MIRROR_VAR) >= 0
				|| url.indexOf(BIOC_VERSION_VAR) >= 0
				|| url.indexOf(CRAN_MIRROR_VAR) >= 0);
	}
	
	static boolean requireCRANMirror(final Collection<RRepo> repos) {
		for (final RRepo repo : repos) {
			if (repo instanceof RVarRepo) {
				if (((RVarRepo) repo).getRawURL().indexOf(RVarRepo.CRAN_MIRROR_VAR) >= 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	static boolean requireBioCMirror(final Collection<RRepo> repos) {
		for (final RRepo repo : repos) {
			if (repo instanceof RVarRepo) {
				if (((RVarRepo) repo).getRawURL().indexOf(RVarRepo.BIOC_MIRROR_VAR) >= 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	static RRepo create(final String id, final String name, final String url, final RPkgType pkgType) {
		return (hasVars(url)) ? new RVarRepo(id, name, url, pkgType) : new RRepo(id, name, url, pkgType);
	}
	
	
	private String fRawURL;
	
	
	public RVarRepo(final String id, final String name, final String url, final RPkgType pkgType) {
		super(id, name, url, pkgType);
		fRawURL = getURL();
	}
	
	
	public void updateURL(final ISelectedRepos settings) {
		String url = fRawURL;
		if (settings.getCRANMirror() != null) {
			url = CRAN_MIRROR_VAR_PATTERN.matcher(url).replaceAll(settings.getCRANMirror().getURL());
		}
		if (settings.getBioCMirror() != null) {
			url = BIOC_MIRROR_VAR_PATTERN.matcher(url).replaceAll(settings.getBioCMirror().getURL());
		}
		if (settings.getBioCVersion() != null) {
			url = BIOC_VERSION_VAR_PATTERN.matcher(url).replaceAll(settings.getBioCVersion());
		}
		if (!url.equals(getURL())) {
			super.setURL(url);
		}
	}
	
	@Override
	public void setURL(final String url) {
		super.setURL(url);
		fRawURL = url;
	}
	
	public String getRawURL() {
		return fRawURL;
	}
	
}
