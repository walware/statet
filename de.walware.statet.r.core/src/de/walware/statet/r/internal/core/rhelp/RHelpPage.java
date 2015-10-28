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
import de.walware.statet.r.core.rhelp.IRPkgHelp;


public final class RHelpPage implements IRHelpPage {
	
	
	private final IRPkgHelp pkg;
	
	private final String name;
	
	private final String title;
	
	
	public RHelpPage(final IRPkgHelp pkgHelp, final String name, final String title) {
		this.pkg= pkgHelp;
		this.name= name;
		this.title= title;
	}
	
	
	@Override
	public IRPkgHelp getPackage() {
		return this.pkg;
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
	public int hashCode() {
		return this.pkg.hashCode() + this.name.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof IRHelpPage) {
			final IRHelpPage other= (IRHelpPage) obj;
			return (this.name.equals(other.getName())
					&& this.pkg.equals(other.getPackage()) );
		}
		return false;
	}
	
	@Override
	public int compareTo(final IRHelpPage o) {
		return RSymbolComparator.R_NAMES_COLLATOR.compare(this.name, o.getName());
	}
	
	@Override
	public String toString() {
		return this.pkg.getName() + "::" +  this.name;
	}
	
}
