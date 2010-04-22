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

package de.walware.statet.r.core.renv;

import org.eclipse.core.filesystem.IFileStore;


/**
 * A single R library.
 * 
 * It usually points to a directory in a file system with
 * subdirectories containing the R packages.
 */
public interface IRLibraryLocation {
	
	
	public interface WorkingCopy extends IRLibraryLocation {
		
		public void setDirectoryPath(String path);
		
	}
	
	
	public String getDirectoryPath();
	
	public IFileStore getDirectoryStore();
	
}
