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

import java.util.Comparator;
import java.util.List;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.statet.r.core.RSymbolComparator;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRPkgHelp;


public class RPkgHelp implements IRPkgHelp {
	
	
	private final String name;
	private final String title;
	private final String version;
	
	private final IREnv rEnv;
	private final String built;
	
	private List<IRHelpPage> helpPages;
	
	
	public RPkgHelp(final String name, final String title, final String version,
			final IREnv rEnv, final String built, final List<IRHelpPage> pages) {
		this.name= name;
		this.title= title;
		this.version= version;
		this.rEnv= rEnv;
		this.built= built;
		this.helpPages= pages;
	}
	
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public String getTitle() {
		return this.title;
	}
	
	@Override
	public String getVersion() {
		return this.version;
	}
	
	@Override
	public IREnv getREnv() {
		return this.rEnv;
	}
	
	@Override
	public String getBuilt() {
		return this.built;
	}
	
	@Override
	public List<IRHelpPage> getHelpPages() {
		return this.helpPages;
	}
	
	@Override
	public IRHelpPage getHelpPage(final String name) {
		if (name != null) {
			for (final IRHelpPage page : this.helpPages) {
				if (page.getName().equals(name)) {
					return page;
				}
			}
		}
		return null;
	}
	
	public void addPage(final RHelpPage page) {
		this.helpPages.add(page);
	}
	
	public void setPages(final ImList<IRHelpPage> pages) {
		this.helpPages= pages;
	}
	
	public void freeze() {
		this.helpPages= ImCollections.toList(this.helpPages, (Comparator<IRHelpPage>) null);
	}
	
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof IRPkgHelp)) {
			return false;
		}
		final IRPkgHelp other= (IRPkgHelp) obj;
		return (this.name.equals(other.getName()) 
				&& this.rEnv.equals(other.getREnv()));
	}
	
	@Override
	public int compareTo(final IRPkgHelp o) {
		return RSymbolComparator.R_NAMES_COLLATOR.compare(this.name, o.getName());
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
}
