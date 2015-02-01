/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.rhelp;

import org.eclipse.search.ui.text.Match;

import de.walware.statet.r.core.rhelp.IRHelpSearchMatch;


public class RHelpSearchMatch extends Match implements Comparable<RHelpSearchMatch> {
	
	
	private final IRHelpSearchMatch coreMatch;
	
	
	public RHelpSearchMatch(final IRHelpSearchMatch rMatch) {
		super(rMatch.getPage().getPackage(), Match.UNIT_CHARACTER, 0, 0);
		this.coreMatch= rMatch;
	}
	
	
	public IRHelpSearchMatch getRHelpMatch() {
		return this.coreMatch;
	}
	
	
	@Override
	public int hashCode() {
		return this.coreMatch.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		return ((obj instanceof RHelpSearchMatch)
				&& this.coreMatch.equals(((RHelpSearchMatch) obj).getRHelpMatch()) );
	}
	
	@Override
	public int compareTo(final RHelpSearchMatch o) {
		return this.coreMatch.getPage().compareTo(o.getRHelpMatch().getPage());
	}
	
	
	@Override
	public String toString() {
		return this.coreMatch.toString();
	}
	
}
