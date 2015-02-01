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

import de.walware.statet.r.core.pkgmanager.IRPkgSet;
import de.walware.statet.r.core.pkgmanager.RPkgInfo;


class RPkgSet implements IRPkgSet {
	
	
	private final RPkgCollection<RPkgInfo> installed;
	
	
	public RPkgSet(final int installed) {
		this.installed= new RPkgCollection<>(installed);
	}
	
	
	@Override
	public RPkgCollection<RPkgInfo> getInstalled() {
		return this.installed;
	}
	
}
