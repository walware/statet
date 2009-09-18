/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
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
public class RLibraryLocation {
	
	
	public static class WorkingCopy extends RLibraryLocation {
		
		public WorkingCopy() {
			super(""); //$NON-NLS-1$
		}
		
		private WorkingCopy(final RLibraryLocation template) {
			super(template);
		}
		
		
		public void setDirectoryPath(final String path) {
			fPath = path;
		}
		
	}
	
	
	protected String fPath;
	protected IFileStore fStore;
	
	
	RLibraryLocation(final RLibraryLocation template) {
		fPath = template.fPath;
	}
	
	public RLibraryLocation(final String path) {
		fPath = path;
	}
	
	public WorkingCopy createWorkingCopy() {
		return new WorkingCopy(this);
	}
	
	
	public String getDirectoryPath() {
		return fPath;
	}
	
	public IFileStore getDirectoryStore() {
		return fStore;
	}
	
}
