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

package de.walware.statet.r.internal.ui.dataeditor;

import de.walware.rj.data.RStore;

import de.walware.statet.r.ui.dataeditor.IRDataTableVariable;


public class FTableVariable implements IRDataTableVariable {
	
	
	private final int fPresentation;
	
	private final String fName;
	
	private final RStore fDataStore;
	
	
	public FTableVariable(final int orientation, final String name, final RStore dataStore) {
		fPresentation = orientation;
		fName = name;
		fDataStore = dataStore;
	}
	
	
	@Override
	public int getVarPresentation() {
		return fPresentation;
	}
	
	@Override
	public String getName() {
		return fName;
	}
	
	@Override
	public int getVarType() {
		return FACTOR;
	}
	
	public RStore getLevelStore() {
		return fDataStore;
	}
	
	
	@Override
	public int hashCode() {
		return fName.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof FTableVariable)) {
			return false;
		}
		final FTableVariable other = (FTableVariable) obj;
		return fName.equals(other.fName);
	}
	
}
