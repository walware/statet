/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.rhelp;

import org.eclipse.search.ui.text.Match;

import de.walware.statet.r.core.rhelp.IRHelpSearchMatch;


public class RHelpSearchUIMatch extends Match implements Comparable<RHelpSearchUIMatch> {
	
	
	private final IRHelpSearchMatch fRMatch;
	
	
	public RHelpSearchUIMatch(final IRHelpSearchMatch rMatch) {
		super(rMatch.getPage(), Match.UNIT_CHARACTER, 0, 0);
		fRMatch = rMatch;
	}
	
	
	@Override
	public RHelpSearchUIMatch getElement() {
		return this;
	}
	
	public IRHelpSearchMatch getRHelpMatch() {
		return fRMatch;
	}
	
	
	@Override
	public int hashCode() {
		return fRMatch.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		return ((obj instanceof RHelpSearchUIMatch)
				&& fRMatch.equals(((RHelpSearchUIMatch) obj).getRHelpMatch()) );
	}
	
	@Override
	public int compareTo(final RHelpSearchUIMatch o) {
		return fRMatch.getPage().compareTo(o.getRHelpMatch().getPage());
	}
	
}
