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

package de.walware.statet.r.internal.core.pkgmanager;

import java.util.List;

import de.walware.statet.r.core.pkgmanager.RPkgUtil;
import de.walware.statet.r.core.pkgmanager.RRepo;
import de.walware.statet.r.core.renv.IRPkg;


public final class Util extends RPkgUtil {
	
	
	public static RRepo createRepoFromR(String id, final String name, String url) {
		if (id == null) {
			id = ""; //$NON-NLS-1$
		}
		if ("@CRAN@".equals(url)) { //$NON-NLS-1$
			url = "%cran"; //$NON-NLS-1$
			if (id.isEmpty()) {
				id = "CRAN"; //$NON-NLS-1$
			}
		}
		else {
			url = checkURL(url);
			if (url.isEmpty()) {
				return null;
			}
		}
		if (!id.isEmpty()
				&& !id.startsWith(RRepo.CUSTOM_PREFIX)
				&& !id.startsWith(RRepo.SPECIAL_PREFIX) ) {
			id = RRepo.R_PREFIX + id;
		}
		return RVarRepo.create(id, name, url, null);
	}
	
	public static String checkURL(final String url) {
		if (url == null || url.isEmpty() || url.equals("@CRAN@")) { //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}
		if (url.charAt(url.length()-1) == '/') {
			return url.substring(0, url.length()-1);
		}
		return url;
	}
	
	/** For unsorted lists */
	public static int findPkg(final List<? extends IRPkg> list, final String name) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getName().equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
}
