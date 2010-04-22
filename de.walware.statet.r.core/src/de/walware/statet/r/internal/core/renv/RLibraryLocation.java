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

package de.walware.statet.r.internal.core.renv;

import org.eclipse.core.filesystem.IFileStore;

import de.walware.statet.r.core.renv.IRLibraryLocation;


public class RLibraryLocation implements IRLibraryLocation {
	
	
	public static class Editable extends RLibraryLocation implements WorkingCopy {
		
		public Editable(final String path) {
			super(path);
		}
		
		public Editable(final RLibraryLocation template) {
			super(template);
		}
		
	}
	
	
	protected String fPath;
	protected IFileStore fStore;
	
	
	public RLibraryLocation(final RLibraryLocation template) {
		fPath = template.fPath;
	}
	
	public RLibraryLocation(final String path) {
		fPath = path;
	}
	
	public Editable createWorkingCopy() {
		return new Editable(this);
	}
	
	
	public String getDirectoryPath() {
		return fPath;
	}
	
	public void setDirectoryPath(final String path) {
		fPath = path;
	}
	
	public IFileStore getDirectoryStore() {
		return fStore;
	}
	
	
	@Override
	public int hashCode() {
		return fPath.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RLibraryLocation)) {
			return false;
		}
		final RLibraryLocation other = (RLibraryLocation) obj;
		return fPath.equals(other.getDirectoryPath());
	}
	
}
