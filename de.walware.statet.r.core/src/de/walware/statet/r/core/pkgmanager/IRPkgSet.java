/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.pkgmanager;

import java.util.List;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.statet.r.internal.core.pkgmanager.FullRPkgSet;


public interface IRPkgSet {
	
	
	interface Ext extends IRPkgSet {
		
		ImList<String> DEFAULT_PRIORITIES= ImCollections.newList(
				"base", "recommended", "other"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		
		List<String> getNames();
		
		@Override
		IRPkgCollection<? extends IRPkgInfoAndData> getInstalled();
		
		List<String> getPriorities();
		
		IRPkgCollection<? extends IRPkgData> getAvailable();
		
		IRPkgData getReverse(String name);
		
	}
	
	IRPkgSet.Ext DUMMY= FullRPkgSet.DUMMY;
	
	
	IRPkgCollection<? extends IRPkgInfo> getInstalled();
	
}
