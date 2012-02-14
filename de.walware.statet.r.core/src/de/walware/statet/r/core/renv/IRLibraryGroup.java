/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.renv;

import java.util.List;

import de.walware.statet.r.internal.core.renv.RLibraryLocation;


/**
 * A group of {@link RLibraryLocation R library locations}.
 */
public interface IRLibraryGroup {
	
	/** Id of R default library group */
	public static final String R_DEFAULT = "r.default"; //$NON-NLS-1$
	/** Id of R site libraries group */
	public static final String R_SITE = "r.site"; //$NON-NLS-1$
	/** Id of R user libraries group */
	public static final String R_USER = "r.user"; //$NON-NLS-1$
	/** Id of R other libraries group */
	public static final String R_OTHER = "r.common"; //$NON-NLS-1$
	
	public static final String DEFAULTLOCATION_R_DEFAULT = "${env_var:R_HOME}/library"; //$NON-NLS-1$
	
	public static final String DEFAULTLOCATION_R_SITE = "${env_var:R_HOME}/site-library"; //$NON-NLS-1$
	
	
	public interface WorkingCopy extends IRLibraryGroup {
		
		@Override
		public List<IRLibraryLocation.WorkingCopy> getLibraries();
		
		IRLibraryLocation.WorkingCopy newLibrary(String path);
		
	}
	
	
	public String getId();
	
	public String getLabel();
	
	public List<? extends IRLibraryLocation> getLibraries();
	
}
