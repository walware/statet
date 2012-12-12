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

import de.walware.statet.r.core.pkgmanager.IRPkgChangeSet;
import de.walware.statet.r.core.pkgmanager.IRPkgInfo;


public class RPkgChangeSet implements IRPkgChangeSet {
	
	
	final RPkgNameList fNames = new RPkgNameList(8);
	
	final RPkgList<IRPkgInfo> fAdded = new RPkgList<IRPkgInfo>(8);
	final RPkgList<IRPkgInfo> fChanged = new RPkgList<IRPkgInfo>(8);
	final RPkgList<IRPkgInfo> fDeleted = new RPkgList<IRPkgInfo>(8);
	
	
	@Override
	public List<String> getNames() {
		return fNames;
	}
	
	@Override
	public RPkgList<IRPkgInfo> getAdded() {
		return fAdded;
	}
	
	@Override
	public RPkgList<IRPkgInfo> getChanged() {
		return fChanged;
	}
	
	@Override
	public RPkgList<IRPkgInfo> getDeleted() {
		return fDeleted;
	}
	
}
