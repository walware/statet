/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.pkgmanager;

import java.util.Collections;
import java.util.Set;

import de.walware.statet.r.core.pkgmanager.IRLibPaths;
import de.walware.statet.r.core.pkgmanager.IRLibPaths.Entry;
import de.walware.statet.r.core.pkgmanager.IRPkgData;
import de.walware.statet.r.core.pkgmanager.IRPkgInfoAndData;
import de.walware.statet.r.core.renv.IRLibraryLocation;


interface IGetPkgFilter {
	
	
	boolean exclude(IRPkgInfoAndData inst, IRPkgData avail);
	
}


class RequireInstFilter implements IGetPkgFilter {
	
	
	public RequireInstFilter() {
	}
	
	
	@Override
	public boolean exclude(final IRPkgInfoAndData inst, final IRPkgData avail) {
		return (inst == null);
	}
	
}


class LibSourceFilter implements IGetPkgFilter {
	
	static final Set<String> EXCLUDE_EPLUGIN = Collections.singleton(IRLibraryLocation.EPLUGIN);
	
	
	private final Set<String> fSources;
	
	public LibSourceFilter() {
		this(EXCLUDE_EPLUGIN);
	}
	
	public LibSourceFilter(final Set<String> sources) {
		fSources = sources;
	}
	
	
	@Override
	public boolean exclude(final IRPkgInfoAndData inst, final IRPkgData avail) {
		return fSources.contains(inst.getLibraryLocation().getSource());
	}
	
}


class ReadOnlyFilter implements IGetPkgFilter {
	
	
	private final IRLibPaths fRLibPaths;
	
	
	public ReadOnlyFilter(final IRLibPaths rLibPaths) {
		fRLibPaths = rLibPaths;
	}
	
	
	@Override
	public boolean exclude(final IRPkgInfoAndData inst, final IRPkgData avail) {
		final Entry entry = fRLibPaths.getEntryByLocation(inst.getLibraryLocation());
		return (entry == null || (entry.getAccess() & IRLibPaths.WRITABLE) != IRLibPaths.WRITABLE);
	}
	
}


class LaterVersionFilter implements IGetPkgFilter {
	
	
	public LaterVersionFilter() {
	}
	
	
	@Override
	public boolean exclude(final IRPkgInfoAndData inst, final IRPkgData avail) {
		return inst.getVersion().isGreaterEqualThan(avail.getVersion());
	}
	
}

class NotOlderVersionFilter implements IGetPkgFilter {
	
	
	public NotOlderVersionFilter() {
	}
	
	
	@Override
	public boolean exclude(final IRPkgInfoAndData inst, final IRPkgData avail) {
		return inst.getVersion().isSmallerThan(avail.getVersion());
	}
	
}
