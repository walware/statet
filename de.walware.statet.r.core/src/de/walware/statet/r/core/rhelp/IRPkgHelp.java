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

package de.walware.statet.r.core.rhelp;

import java.util.List;

import de.walware.statet.r.core.renv.IREnv;


public interface IRPkgHelp extends Comparable<IRPkgHelp> {
	
	
	String getName();
	String getTitle();
	String getVersion();
	
	IREnv getREnv();
	String getBuilt();
	
	List<IRHelpPage> getHelpPages();
	
	IRHelpPage getHelpPage(String name);
	
}
