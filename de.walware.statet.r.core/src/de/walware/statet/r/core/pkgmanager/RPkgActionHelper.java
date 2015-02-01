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

package de.walware.statet.r.core.pkgmanager;

import java.util.List;

import de.walware.statet.r.core.pkgmanager.IRLibPaths.Entry;
import de.walware.statet.r.core.renv.IRLibraryLocation;


public class RPkgActionHelper {
	
	
	private final boolean fSameLocation;
	private final IRLibraryLocation fDefaultLocation;
	
	private final IRLibPaths fRLibPaths;
	
	
	public RPkgActionHelper(final boolean sameLocation, final IRLibraryLocation defaultLocation,
			final IRLibPaths rLibPaths) {
		fSameLocation = sameLocation;
		fDefaultLocation = defaultLocation;
		fRLibPaths = rLibPaths;
	}
	
	
	public void updateLocation(final RPkgAction.Install action) {
		if (fSameLocation) {
			final IRPkgInfoAndData referencePkg = action.getReferencePkg();
			if (referencePkg != null) {
				final IRLibraryLocation location = referencePkg.getLibraryLocation();
				final Entry entry = fRLibPaths.getEntryByLocation(location);
				if (entry != null && (entry.getAccess() & IRLibPaths.WRITABLE) == IRLibPaths.WRITABLE) {
					action.setLibraryLocation(location);
				}
				return;
			}
		}
		if (fDefaultLocation == null) {
			throw new UnsupportedOperationException("missing default location");
		}
		action.setLibraryLocation(fDefaultLocation);
	}
	
	
	public void update(final List<? extends RPkgAction.Install> actions) {
		for (final RPkgAction.Install action : actions) {
			updateLocation(action);
		}
	}
	
	
	@Override
	public int hashCode() {
		int h = (fDefaultLocation != null) ? fDefaultLocation.hashCode() : 0;
		if (fSameLocation) {
			h++;
		}
		return h;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RPkgActionHelper)) {
			return false;
		}
		final RPkgActionHelper other = (RPkgActionHelper) obj;
		return (((fDefaultLocation != null) ? fDefaultLocation.equals(other.fDefaultLocation) : null == other.fDefaultLocation)
				&& (fSameLocation == other.fSameLocation) );
	}
	
}
