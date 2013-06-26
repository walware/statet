/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.rj.renv.IRPkg;


public interface IRPkgData extends IRPkg {
	
	
	String getLicense();
	
	List<? extends IRPkg> getDepends();
	List<? extends IRPkg> getImports();
	List<? extends IRPkg> getLinkingTo();
	List<? extends IRPkg> getSuggests();
	List<? extends IRPkg> getEnhances();
	
	String getPriority();
	String getRepoId();
	
}
