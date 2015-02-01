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

import de.walware.rj.renv.IRPkg;
import de.walware.rj.renv.RNumVersion;


public class VersionListRPkg implements IRPkg {
	
	
	private final String fName;
	
	private RNumVersion fVersion;
	
	
	public VersionListRPkg(final String name, final RNumVersion version) {
		if (name == null) {
			throw new NullPointerException();
		}
		fName = name;
		fVersion = (version != null) ? version : RNumVersion.NONE;
	}
	
	
	@Override
	public String getName() {
		return fName;
	}
	
	@Override
	public RNumVersion getVersion() {
		return fVersion;
	}
	
	public void addVersion(final RNumVersion version) {
		if (fVersion.equals(version) || version == RNumVersion.NONE) {
			return;
		}
		if (fVersion == RNumVersion.NONE) {
			fVersion = version;
			return;
		}
		final String listString = fVersion.toString();
		final String addString = version.toString();
		for (int i = 0; i < listString.length(); i++) {
			final int idx = listString.indexOf(addString, i);
			if (idx >= 0) {
				if ((idx == 0 || listString.regionMatches(idx - 2, ", ", 0, 2)) //$NON-NLS-1$
						&& (idx + addString.length() == listString.length()
								|| listString.regionMatches(idx + addString.length(), ", ", 0, 2) )) { //$NON-NLS-1$
					return;
				}
				continue;
			}
			break;
		}
		fVersion = RNumVersion.create(listString + ", " + addString); //$NON-NLS-1$
	}
	
	
	@Override
	public int hashCode() {
		return fName.hashCode() + fVersion.hashCode() * 7;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof IRPkg)) {
			return false;
		}
		final IRPkg other = (IRPkg) obj;
		return (fName.equals(other.getName()) && fVersion.equals(other.getVersion()));
	}
	
	
	@Override
	public String toString() {
		return fName;
	}
	
}
