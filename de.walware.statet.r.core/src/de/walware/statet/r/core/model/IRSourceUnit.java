/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.model;

import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.ISourceUnit;

import de.walware.statet.r.core.IRCoreAccess;


/**
 * An R source (script) file
 */
public interface IRSourceUnit extends ISourceUnit {
	
	/**
	 * Content type id for R sources
	 */
	public static final String R_CONTENT = "de.walware.statet.r.contentTypes.R"; //$NON-NLS-1$
	
	/**
	 * Content type id for Rd sources
	 */
	public static final String RD_CONTENT = "de.walware.statet.r.contentTypes.Rd"; //$NON-NLS-1$
	
	
	/**
	 * Model type id for R source files in workspace
	 */
	public static final int R_WORKSPACE_SU = C2_SOURCE_FILE | 1;
	
	/**
	 * Model type id for R source files in libraries
	 */
	public static final int R_LIBRARY_SU = C2_SOURCE_FILE | 2;
	
	/**
	 * Model type id for other R source files, e.g. external files
	 */
	public static final int R_OTHER_SU = C2_SOURCE_FILE | 3;
	
	
	/**
	 * Returns the R core access provider for the source unit
	 * @return access of scope of the source unit
	 */
	public IRCoreAccess getRCoreAccess();
	
	/**
	 * Forces that the the R model of the source unit is up-to-date.
	 * 
	 * @param reconcileLevel the model level the model must have (min)
	 * @param monitor
	 */
	public void reconcileRModel(int reconcileLevel, IProgressMonitor monitor);
	
}
