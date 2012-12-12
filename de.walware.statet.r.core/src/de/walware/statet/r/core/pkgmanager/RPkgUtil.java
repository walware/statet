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

import java.util.Collection;

import com.ibm.icu.text.Collator;

import de.walware.rj.services.RPlatform;

import de.walware.statet.r.core.RSymbolComparator;
import de.walware.statet.r.core.pkgmanager.IRLibPaths.Entry;
import de.walware.statet.r.core.renv.IRLibraryGroup;
import de.walware.statet.r.core.renv.IRLibraryLocation;
import de.walware.statet.r.internal.core.pkgmanager.RVarRepo;


public class RPkgUtil {
	
	
	public static Collator COLLATOR = RSymbolComparator.R_NAMES_COLLATOR;
	
	
	public static RRepo getRepoById(final Collection<? extends RRepo> list, final String id) {
		for (final RRepo repo : list) {
			if (id.equals(repo.getId())) {
				return repo;
			}
		}
		return null;
	}
	
	public static RRepo getRepoByName(final Collection<? extends RRepo> list, final String name) {
		for (final RRepo repo : list) {
			if (name.equals(repo.getName())) {
				return repo;
			}
		}
		return null;
	}
	
	public static RRepo getRepoByURL(final Collection<? extends RRepo> list, final RRepo repo) {
		return (repo instanceof RVarRepo) ?
				getRepoByRawURL(list, ((RVarRepo) repo).getRawURL()) :
				getRepoByURL(list, repo.getURL());
	}
	
	public static RRepo getRepoByURL(final Collection<? extends RRepo> list, String url) {
		url = RRepo.checkRepoURL(url);
		for (final RRepo repo : list) {
			if (url.equals(repo.getURL())) {
				return repo;
			}
		}
		return null;
	}
	
	private static RVarRepo getRepoByRawURL(final Collection<? extends RRepo> list, String url) {
		url = RRepo.checkRepoURL(url);
		for (final RRepo repo : list) {
			if (repo instanceof RVarRepo && url.equals(((RVarRepo) repo).getRawURL())) {
				return (RVarRepo) repo;
			}
		}
		return null;
	}
	
	public static RRepo findRepo(final Collection<? extends RRepo> list, final RRepo repo) {
		RRepo found = RPkgUtil.getRepoById(list, repo.getId());
		if (found == null && !repo.getURL().isEmpty()) {
			found = getRepoByURL(list, repo);
		}
		if (found == null && !repo.getName().isEmpty()) {
			found = getRepoByName(list, repo.getName());
		}
		return found;
	}
	
	
	private static boolean isWin(final RPlatform rPlatform) {
		return rPlatform.getOsType().equals(RPlatform.OS_WINDOWS);
	}
	
	private static boolean isMac(final RPlatform rPlatform) {
		return rPlatform.getOSName().regionMatches(true, 0, "Mac OS", 0, 6); //$NON-NLS-1$
	}
	
	public static RPkgType getPkgType(final RPlatform rPlatform, final String fileName) {
		if (fileName.endsWith(".tar.gz")) { //$NON-NLS-1$
			return RPkgType.SOURCE;
		}
		if (isWin(rPlatform)) {
			if (fileName.toLowerCase().endsWith(".zip")) { //$NON-NLS-1$
				return RPkgType.BINARY;
			}
		}
		else if (isMac(rPlatform)) {
			if (fileName.endsWith(".tgz")) { //$NON-NLS-1$
				return RPkgType.BINARY;
			}
		}
		return null;
	}
	
	public static String getPkgTypeInstallKey(final RPlatform rPlatform, final RPkgType type) {
		if (type == RPkgType.SOURCE) {
			return "source"; //$NON-NLS-1$
		}
		if (type == RPkgType.BINARY) {
			if (isWin(rPlatform)) {
				return "win.binary"; //$NON-NLS-1$
			}
			else if (isMac(rPlatform)) {
				return "mac.binary.leopard"; //$NON-NLS-1$
			}
		}
		return null;
	}
	
	
	private static final String[] DEFAULT_INSTALL_ORDER = new String[] {
		IRLibraryGroup.R_USER, IRLibraryGroup.R_OTHER, IRLibraryGroup.R_SITE, IRLibraryGroup.R_DEFAULT,
	};
	
	public static IRLibraryLocation getDefaultInstallLocation(final IRLibPaths rLibPaths) {
		for (final String groupId : DEFAULT_INSTALL_ORDER) {
			final IRLibraryGroup group = rLibPaths.getRLibraryGroup(groupId);
			for (final IRLibraryLocation location : group.getLibraries()) {
				final Entry entry = rLibPaths.getEntryByLocation(location);
				if (entry != null && (entry.getAccess() & IRLibPaths.WRITABLE) == IRLibPaths.WRITABLE) {
					return location;
				}
			}
		}
		return null;
	}
	
}
