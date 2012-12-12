/*******************************************************************************
 * Copyright (c) 2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.pkgmanager;

import java.util.List;

import de.walware.ecommons.collections.ConstList;

import de.walware.statet.r.internal.core.pkgmanager.FullRPkgSet;


public interface IRPkgSet {
	
	
	interface Ext extends IRPkgSet {
		
		List<String> DEFAULT_PRIORITIES = new ConstList<String>(
				"base", "recommended", "other"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		
		@Override
		IRPkgCollection<? extends IRPkgDescription> getInstalled();
		
		List<String> getPriorities();
		
		IRPkgCollection<? extends IRPkgData> getAvailable();
		
		IRPkgData getReverse(String name);
		
	}
	
	IRPkgSet.Ext DUMMY = FullRPkgSet.DUMMY;
	
	
	List<String> getNames();
	
	IRPkgCollection<? extends IRPkgInfo> getInstalled();
	
}
