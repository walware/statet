/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk;

import org.eclipse.core.resources.IMarker;


/**
 * Problem
 */
public interface IProblem {
	
	
	/** 
	 * Error severity constant indicating an error.
	 * 
	 * {@link IMarker#SEVERITY_ERROR}
	 */
	public static final int SEVERITY_ERROR = 2;
	
	/** 
	 * Error severity constant indicating a warning.
	 * 
	 * {@link IMarker#SEVERITY_WARNING}
	 */
	public static final int SEVERITY_WARNING = 1;
	
	/** 
	 * Error severity constant indicating an information only.
	 * 
	 * {@link IMarker#SEVERITY_INFO}
	 */
	public static final int SEVERITY_INFO = 0;
	
	
	public ISourceUnit getSourceUnit();
	
	public int getSourceLine();
	public int getSourceStartOffset();
	public int getSourceStopOffset();
	
	public int getSeverity();
	public int getCode();
	public String getMessage();
	
}
