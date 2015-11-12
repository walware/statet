/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.model;

import de.walware.ecommons.ltk.core.model.ISourceUnit;

import de.walware.statet.r.core.IRCoreAccess;


/**
 * An R source (script) file
 */
public interface IRSourceUnit extends ISourceUnit {
	
	
	/**
	 * Model type id for R source files in workspace
	 */
	int R_WORKSPACE_SU = C2_SOURCE_FILE | 1;
	
	/**
	 * Model type id for R source files in libraries
	 */
	int R_LIBRARY_SU = C2_SOURCE_FILE | 2;
	
	/**
	 * Model type id for other R source files, e.g. external files
	 */
	int R_OTHER_SU = C2_SOURCE_FILE | 3;
	
	
	/**
	 * Returns the R core access provider for the source unit
	 * @return access of scope of the source unit
	 */
	IRCoreAccess getRCoreAccess();
	
}
