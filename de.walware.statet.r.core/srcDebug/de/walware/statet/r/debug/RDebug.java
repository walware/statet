/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.debug;

import org.eclipse.core.variables.IStringVariable;

import de.walware.ecommons.variables.core.StringVariable;


public class RDebug {
	
	/**
	 * {@link IStringVariable String variable} name for the R working directory.
	 */
	public static final String WORKING_DIRECTORY_VARNAME = "r_wd"; //$NON-NLS-1$
	
	/**
	 * String variable for the R working directory.
	 * 
	 * Note: Listing and functionality must be explicitly implemented.
	 */
	public static final IStringVariable WORKING_DIRECTORY_VARIABLE = new StringVariable(RDebug.WORKING_DIRECTORY_VARNAME, "The configured R working directory");
	
	
}
