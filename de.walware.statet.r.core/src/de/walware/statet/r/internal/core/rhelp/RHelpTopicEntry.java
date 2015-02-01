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

package de.walware.statet.r.internal.core.rhelp;

import de.walware.statet.r.core.RSymbolComparator;
import de.walware.statet.r.core.rhelp.IRHelpPage;


public class RHelpTopicEntry implements Comparable<RHelpTopicEntry> {
	
	
	private final String fAlias;
	
	private final IRHelpPage fPage;
	
	
	public RHelpTopicEntry(final String alias, final IRHelpPage page) {
		fAlias = alias;
		fPage = page;
	}
	
	
	public String getAlias() {
		return fAlias;
	}
	
	public IRHelpPage getPage() {
		return fPage;
	}
	
	
	@Override
	public int hashCode() {
		return fAlias.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RHelpTopicEntry)) {
			return false;
		}
		final RHelpTopicEntry other = (RHelpTopicEntry) obj;
		return fAlias.equals(other.fAlias) && fPage.equals(other.fPage);
	}
	
	@Override
	public int compareTo(final RHelpTopicEntry o) {
		final int diff = RSymbolComparator.R_NAMES_COLLATOR.compare(fAlias, o.fAlias);
		if (diff != 0) {
			return diff;
		}
		return fPage.compareTo(o.fPage);
	}
	
}
