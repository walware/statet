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

import java.util.Collection;
import java.util.List;

import com.ibm.icu.text.Collator;

import de.walware.statet.r.core.RSymbolComparator;
import de.walware.statet.r.core.pkgmanager.IRLibPaths.Entry;
import de.walware.statet.r.core.renv.IRLibraryGroup;
import de.walware.statet.r.core.renv.IRLibraryLocation;
import de.walware.statet.r.internal.core.pkgmanager.RVarRepo;


public class RPkgUtil extends de.walware.rj.renv.RPkgUtil {
	
	
	public static Collator COLLATOR = RSymbolComparator.R_NAMES_COLLATOR;
	
	public static boolean DEBUG = Boolean.parseBoolean(System.getProperty("de.walware.statet.r.core.pkgmanager.debug")); //$NON-NLS-1$
	
	
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
	
	public static boolean areInstalled(final IRPkgManager manager, final List<String> pkgNames) {
		final IRPkgCollection<? extends IRPkgInfo> installedPkgs = manager.getRPkgSet().getInstalled();
		for (final String pkgName : pkgNames) {
			if (!installedPkgs.containsByName(pkgName)) {
				return false;
			}
		}
		return true;
	}
	
}
