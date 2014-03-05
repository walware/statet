/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.rhelp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.walware.ecommons.collections.ConstArrayList;

import de.walware.rj.renv.IRPackageDescription;

import de.walware.statet.r.core.RSymbolComparator;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRPackageHelp;
import de.walware.statet.r.internal.core.RCorePlugin;


public class RPackageHelp implements IRPackageHelp {
	
	
	private final String name;
	private final String title;
	private final String version;
	
	private final IREnv rEnv;
	
	private List<IRHelpPage> helpPages;
	
	
	public RPackageHelp(final String name, final String title, final String version, final IREnv rEnv) {
		this.rEnv= rEnv;
		this.name= name;
		this.title= title;
		this.version= version;
		this.helpPages= new ArrayList<IRHelpPage>();
	}
	
	public RPackageHelp(final String name, final String title, final String version, final IREnv rEnv,
			final ConstArrayList<IRHelpPage> pages) {
		this.rEnv= rEnv;
		this.name= name;
		this.title= title;
		this.version= version;
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
	public IRPackageDescription getPackageDescription() {
		final REnvHelp help= RCorePlugin.getDefault().getRHelpManager().getHelp(this.rEnv);
		if (help != null) {
			try {
				return help.getPackageDescription(this);
			}
			finally {
				help.unlock();
			}
		}
		return null;
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
	
	public void freeze() {
		final IRHelpPage[] array= this.helpPages.toArray(new IRHelpPage[this.helpPages.size()]);
		Arrays.sort(array);
		this.helpPages= new ConstArrayList<IRHelpPage>(array);
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
		if (!(obj instanceof IRPackageHelp)) {
			return false;
		}
		final IRPackageHelp other= (IRPackageHelp) obj;
		return (this.name.equals(other.getName()) 
				&& this.rEnv.equals(other.getREnv()));
	}
	
	@Override
	public int compareTo(final IRPackageHelp o) {
		return RSymbolComparator.R_NAMES_COLLATOR.compare(this.name, o.getName());
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
}
