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

import java.util.Collections;
import java.util.List;

import de.walware.statet.r.core.pkgmanager.IRPkgSet;
import de.walware.statet.r.core.pkgmanager.RPkgInfo;


class RPkgSet implements IRPkgSet {
	
	
	private final RPkgCollection<RPkgInfo> fInstalled;
	
	private List<String> fNames;
	
	
	public RPkgSet(final int installed) {
		fInstalled = new RPkgCollection<RPkgInfo>(installed);
	}
	
	
	@Override
	public synchronized List<String> getNames() {
		if (fNames == null) {
			final RPkgNameList names = new RPkgNameList(64);
			fInstalled.addNames(names);
			fNames = Collections.unmodifiableList(names);
		}
		return fNames;
	}
	
	@Override
	public RPkgCollection<RPkgInfo> getInstalled() {
		return fInstalled;
	}
	
}
