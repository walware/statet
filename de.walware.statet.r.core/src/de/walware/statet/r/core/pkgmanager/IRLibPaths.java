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

import de.walware.statet.r.core.renv.IRLibraryGroup;
import de.walware.statet.r.core.renv.IRLibraryLocation;


public interface IRLibPaths {
	
	
	int WRITABLE =                                         0x1;
	int EXISTS =                                           0x2;
	
	
	interface Entry {
		
		IRLibraryLocation getLocation();
		
		String getRPath();
		
		int getAccess();
		
		double getStamp();
		
	}
	
	
	List<? extends IRLibraryGroup> getRLibraryGroups();
	IRLibraryGroup getRLibraryGroup(String id);
	
	List<? extends Entry> getEntries();
	
	Entry getEntryByRPath(String rPath);
	Entry getEntryByLocation(IRLibraryLocation location);
	
}
