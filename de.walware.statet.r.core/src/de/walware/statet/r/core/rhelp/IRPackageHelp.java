/*******************************************************************************
 * Copyright (c) 2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rhelp;

import java.util.List;

import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IRPackageDescription;


public interface IRPackageHelp extends Comparable<IRPackageHelp> {
	
	
	String getName();
	String getTitle();
	String getVersion();
	
	IREnv getREnv();
	
	IRPackageDescription getPackageDescription();
	
	List<IRHelpPage> getHelpPages();
	
	IRHelpPage getHelpPage(String name);
	
}
