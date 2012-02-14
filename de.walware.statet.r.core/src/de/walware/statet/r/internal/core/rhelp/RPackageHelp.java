/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.rhelp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.walware.ecommons.collections.ConstList;

import de.walware.statet.r.core.RSymbolComparator;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IRPackageDescription;
import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRPackageHelp;
import de.walware.statet.r.internal.core.RCorePlugin;


public class RPackageHelp implements IRPackageHelp {
	
	
	private final String fName;
	private final String fTitle;
	private final String fVersion;
	
	private final IREnv fREnv;
	
	private List<IRHelpPage> fHelpPages;
	
	
	public RPackageHelp(final String name, final String title, final String version, final IREnv rEnv) {
		fREnv = rEnv;
		fName = name;
		fTitle = title;
		fVersion = version;
		fHelpPages = new ArrayList<IRHelpPage>();
	}
	
	public RPackageHelp(final String name, final String title, final String version, final IREnv rEnv,
			final ConstList<IRHelpPage> pages) {
		fREnv = rEnv;
		fName = name;
		fTitle = title;
		fVersion = version;
		fHelpPages = pages;
	}
	
	
	@Override
	public String getName() {
		return fName;
	}
	
	@Override
	public String getTitle() {
		return fTitle;
	}
	
	@Override
	public String getVersion() {
		return fVersion;
	}
	
	@Override
	public IREnv getREnv() {
		return fREnv;
	}
	
	@Override
	public IRPackageDescription getPackageDescription() {
		final REnvHelp help = RCorePlugin.getDefault().getRHelpManager().getHelp(fREnv);
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
		return fHelpPages;
	}
	
	@Override
	public IRHelpPage getHelpPage(final String name) {
		if (name != null) {
			for (final IRHelpPage page : fHelpPages) {
				if (page.getName().equals(name)) {
					return page;
				}
			}
		}
		return null;
	}
	
	public void addPage(final RHelpPage page) {
		fHelpPages.add(page);
	}
	
	public void freeze() {
		final IRHelpPage[] array = fHelpPages.toArray(new IRHelpPage[fHelpPages.size()]);
		Arrays.sort(array);
		fHelpPages = new ConstList<IRHelpPage>(array);
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
		if (!(obj instanceof IRPackageHelp)) {
			return false;
		}
		final IRPackageHelp other = (IRPackageHelp) obj;
		return (fName.equals(other.getName()) 
				&& fREnv.equals(other.getREnv()));
	}
	
	@Override
	public int compareTo(final IRPackageHelp o) {
		return RSymbolComparator.R_NAMES_COLLATOR.compare(fName, o.getName());
	}
	
	@Override
	public String toString() {
		return fName;
	}
	
}
