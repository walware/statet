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

import de.walware.statet.r.core.RSymbolComparator;
import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRPackageHelp;


public class RHelpPage implements IRHelpPage {
	
	
	private final IRPackageHelp fPackage;
	
	private final String fName;
	
	private final String fTitle;
	
	
	public RHelpPage(final IRPackageHelp packageHelp, final String name, final String title) {
		fPackage = packageHelp;
		fName = name;
		fTitle = title;
	}
	
	
	@Override
	public IRPackageHelp getPackage() {
		return fPackage;
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
	public int hashCode() {
		return fPackage.hashCode() + fName.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof IRHelpPage)) {
			return false;
		}
		final IRHelpPage other = (IRHelpPage) obj;
		return (this == other || (fName.equals(other.getName())
				&& fPackage.equals(other.getPackage()) ));
	}
	
	@Override
	public int compareTo(final IRHelpPage o) {
		return RSymbolComparator.R_NAMES_COLLATOR.compare(fName, o.getName());
	}
	
	@Override
	public String toString() {
		return fPackage.getName() + "::" +  fName;
	}
	
}
