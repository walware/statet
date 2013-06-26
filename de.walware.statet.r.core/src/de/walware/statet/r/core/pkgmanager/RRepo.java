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

package de.walware.statet.r.core.pkgmanager;

import java.net.URL;

import de.walware.rj.renv.RPkgType;


public class RRepo {
	
	
	public static final String CUSTOM_PREFIX = "custom-"; //$NON-NLS-1$
	public static final String SPECIAL_PREFIX = "special-"; //$NON-NLS-1$
	public static final String R_PREFIX = "r-"; //$NON-NLS-1$
	
	public static final String CRAN_ID = R_PREFIX + "cran"; //$NON-NLS-1$
	public static final String BIOC_ID_PREFIX = R_PREFIX + "bioc"; //$NON-NLS-1$
	public static final String WS_CACHE_PREFIX = SPECIAL_PREFIX + "ws-cache-"; //$NON-NLS-1$
	public static final String WS_CACHE_SOURCE_ID = WS_CACHE_PREFIX + "source"; // RPkgType.SOURCE.name().toLowerCase() 
	public static final String WS_CACHE_BINARY_ID = WS_CACHE_PREFIX + "binary"; // RPkgType.BINARY.name().toLowerCase() 
	
	public static final RRepo WS_CACHE_SOURCE_REPO = new RRepo(WS_CACHE_SOURCE_ID,
			"Local Packages", null, RPkgType.SOURCE );
	public static final RRepo WS_CACHE_BINARY_REPO = new RRepo(WS_CACHE_BINARY_ID,
			"Local Packages", null, RPkgType.BINARY );
	
	
	public static String checkRepoURL(final String url) {
		if (url.length() > 0 && url.charAt(url.length() - 1) != '/') {
			return url + '/';
		}
		return url;
	}
	
	public static String hintName(final RRepo repo) {
		try {
			final URL url = new URL(repo.getURL());
			if (url.getHost() != null) {
				return url.getHost();
			}
			else if (url.getPath() != null){
				return url.getPath();
			}
		}
		catch (final Exception e) {}
		return null;
	}
	
	
	private final String fId;
	
	private String fName;
	
	private String fURL;
	
	private RPkgType fPkgType;
	
	
	public RRepo(final String id, final String name, final String url,
			final RPkgType pkgType) {
		if (id == null) {
			throw new NullPointerException("id"); //$NON-NLS-1$
		}
		fId = id.intern();
		setName(name);
		setURL(url);
		setPkgType(pkgType);
	}
	
	public RRepo(final String id) {
		this(id, null, null, null);
	}
	
	
	public String getId() {
		return fId;
	}
	
	public void set(final RRepo template) {
		fName = template.fName;
		fURL = template.fURL;
		fPkgType = template.fPkgType;
	}
	
	public void setName(final String name) {
		fName = (name != null) ? name : ""; //$NON-NLS-1$
	}
	
	public String getName() {
		return fName;
	}
	
	public void setURL(final String url) {
		fURL = (url != null) ? checkRepoURL(url) : ""; //$NON-NLS-1$
	}
	
	public String getURL() {
		return fURL;
	}
	
	public void setPkgType(final RPkgType type) {
		fPkgType = type;
	}
	
	public RPkgType getPkgType() {
		return fPkgType;
	}
	
	
	@Override
	public int hashCode() {
		return fId.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		return ((obj instanceof RRepo) && fId.equals(((RRepo) obj).fId));
	}
	
	@Override
	public String toString() {
		return getName() + " (" + getURL() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
}
