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

package de.walware.statet.r.internal.ui.pkgmanager;

import java.util.Collection;

import de.walware.statet.r.core.pkgmanager.IRPkgCollection;
import de.walware.statet.r.core.pkgmanager.IRPkgData;
import de.walware.statet.r.core.pkgmanager.IRPkgList;


class Util {
	
	
	public static IRPkgData getPkgByRepo(final IRPkgCollection<? extends IRPkgData> collection,
			final String name, final String repoId) {
		for (final IRPkgList<? extends IRPkgData> list : collection.getAll()) {
			final IRPkgData pkg = list.get(name);
			if (pkg != null && repoId.equals(pkg.getRepoId())) {
				return pkg;
			}
		}
		return null;
	}
	
	public static boolean hasPkgPriority(final IRPkgCollection<? extends IRPkgData> collection,
			final String name, final Collection<?> priorities) {
		for (final IRPkgList<? extends IRPkgData> list : collection.getAll()) {
			final IRPkgData pkg = list.get(name);
			if (pkg != null && priorities.contains(pkg.getPriority())) {
				return true;
			}
		}
		return false;
	}
	
}
