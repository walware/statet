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
		this.fPresentation = orientation;
		this.fName = name;
		this.fDataStore = dataStore;
	}
	
	
	@Override
	public int getVarPresentation() {
		return this.fPresentation;
	}
	
	@Override
	public String getName() {
		return this.fName;
	}
	
	@Override
	public int getVarType() {
		return FACTOR;
	}
	
	public RStore getLevelStore() {
		return this.fDataStore;
	}
	
	
	@Override
	public int hashCode() {
		return this.fName.hashCode();
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
		return this.fName.equals(other.fName);
	}
	
}
