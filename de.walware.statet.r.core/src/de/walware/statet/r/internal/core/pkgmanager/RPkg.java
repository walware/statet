/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.pkgmanager;

import de.walware.statet.r.core.renv.IRPkg;
import de.walware.statet.r.core.renv.RNumVersion;


public class RPkg implements IRPkg {
	
	
	private final String fName;
	
	private RNumVersion fVersion;
	
	
	public RPkg(final String name, final RNumVersion version) {
		if (name == null) {
			throw new NullPointerException();
		}
		fName = name;
		setVersion(version);
	}
	
	
	@Override
	public String getName() {
		return fName;
	}
	
	@Override
	public RNumVersion getVersion() {
		return fVersion;
	}
	
	public void setVersion(final RNumVersion version) {
		fVersion = (version != null) ? version : RNumVersion.NONE;
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
		if (!(obj instanceof RPkg)) {
			return false;
		}
		final RPkg other = (RPkg) obj;
		return (fName.equals(other.fName) && fVersion.equals(other.fVersion));
	}
	
	
	@Override
	public String toString() {
		return fName;
	}
	
}
