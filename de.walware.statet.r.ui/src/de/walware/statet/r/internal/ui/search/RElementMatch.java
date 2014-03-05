/*=============================================================================#
 # Copyright (c) 2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.search;

import org.eclipse.search.ui.text.Match;

import de.walware.ecommons.workbench.search.ui.LineElement;

import de.walware.statet.r.core.model.IRSourceUnit;


public class RElementMatch extends Match {
	
	
	private final LineElement<IRSourceUnit> group;
	
	
	public RElementMatch(final LineElement<IRSourceUnit> group,
			final int offset, final int length,
			final boolean isWriteAccess) {
		super(group.getElement(), UNIT_CHARACTER, offset, length);
		
		this.group= group;
	}
	
	
	public LineElement<IRSourceUnit> getMatchGroup() {
		return this.group;
	}
	
}
