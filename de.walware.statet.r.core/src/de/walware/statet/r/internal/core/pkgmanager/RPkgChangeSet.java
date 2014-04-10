/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.pkgmanager;

import java.util.List;

import de.walware.ecommons.collections.SortedArraySet;

import de.walware.statet.r.core.pkgmanager.IRPkgChangeSet;
import de.walware.statet.r.core.pkgmanager.IRPkgInfo;
import de.walware.statet.r.core.pkgmanager.RPkgUtil;


public class RPkgChangeSet implements IRPkgChangeSet {
	
	
	final SortedArraySet<String> names= new SortedArraySet<>(new String[8], 0, RPkgUtil.COLLATOR);
	
	final RPkgList<IRPkgInfo> added= new RPkgList<>(8);
	final RPkgList<IRPkgInfo> changed= new RPkgList<>(8);
	final RPkgList<IRPkgInfo> deleted= new RPkgList<>(8);
	
	
	@Override
	public List<String> getNames() {
		return this.names;
	}
	
	@Override
	public RPkgList<IRPkgInfo> getAdded() {
		return this.added;
	}
	
	@Override
	public RPkgList<IRPkgInfo> getChanged() {
		return this.changed;
	}
	
	@Override
	public RPkgList<IRPkgInfo> getDeleted() {
		return this.deleted;
	}
	
}
