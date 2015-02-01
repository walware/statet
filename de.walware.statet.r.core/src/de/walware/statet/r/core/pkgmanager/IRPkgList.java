/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
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

import de.walware.rj.renv.IRPkg;


public interface IRPkgList<T extends IRPkg> extends List<T> {
	
	
	boolean contains(String name);
	
	int indexOf(String name);
	
	T get(String name);
	
}
