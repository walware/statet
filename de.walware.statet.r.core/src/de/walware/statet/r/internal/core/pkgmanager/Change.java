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

import de.walware.statet.r.core.pkgmanager.IRPkgChangeSet;
import de.walware.statet.r.core.pkgmanager.IRPkgManager.Event;
import de.walware.statet.r.core.pkgmanager.IRPkgSet;
import de.walware.statet.r.core.renv.IREnv;


final class Change implements Event {
	
	
	final IREnv fREnv;
	final long fStamp;
	
	int fRepos;
	
	int fPkgs;
	IRPkgSet fOldPkgs;
	IRPkgSet fNewPkgs;
	RPkgChangeSet fInstalledPkgs;
	
	int fViews;
	
	
	public Change(final IREnv rEnv) {
		fREnv = rEnv;
		fStamp = System.currentTimeMillis();
	}
	
	
	@Override
	public IREnv getREnv() {
		return fREnv;
	}
	
	@Override
	public int reposChanged() {
		return fRepos;
	}
	
	@Override
	public int pkgsChanged() {
		return fPkgs;
	}
	
	@Override
	public IRPkgSet getOldPkgSet() {
		return fOldPkgs;
	}
	
	@Override
	public IRPkgSet getNewPkgSet() {
		return fNewPkgs;
	}
	
	@Override
	public IRPkgChangeSet getInstalledPkgChangeSet() {
		return fInstalledPkgs;
	}
	
	@Override
	public int viewsChanged() {
		return fViews;
	}
	
}
