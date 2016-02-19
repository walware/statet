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

import de.walware.rj.renv.IRPkg;

import de.walware.statet.r.core.renv.IRLibraryLocation;


public interface IRPkgInfo extends IRPkg {
	
	
	String getTitle();
	
	String getBuilt();
	
	IRLibraryLocation getLibraryLocation();
	
	int getFlags();
	long getInstallStamp();
	String getRepoId();
	
	
}
