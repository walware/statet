/*=============================================================================#
 # Copyright (c) 2009-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.renv;

import org.eclipse.core.filesystem.IFileStore;

import de.walware.statet.r.core.renv.IRLibraryLocation;


public class RLibraryLocation implements IRLibraryLocation {
	
	
	public static class Editable extends RLibraryLocation implements WorkingCopy {
		
		public Editable(final String path) {
			super(USER, path, null);
		}
		
		public Editable(final RLibraryLocation template) {
			super(template);
		}
		
	}
	
	
	private final String fSource;
	
	private final String fLabel;
	
	protected String fPath;
	protected IFileStore fStore;
	
	
	public RLibraryLocation(final IRLibraryLocation template) {
		fSource = template.getSource();
		fPath = template.getDirectoryPath();
		fLabel = template.getLabel();
	}
	
	public RLibraryLocation(final String source, final String path, final String label) {
		if (source == null) {
			throw new NullPointerException("source");
		}
		if (path == null) {
			throw new NullPointerException("path");
		}
		fSource = source;
		fPath = path;
		fLabel = label;
	}
	
	public Editable createWorkingCopy() {
		return new Editable(this);
	}
	
	
	@Override
	public String getSource() {
		return fSource;
	}
	
	@Override
	public String getLabel() {
		return fLabel;
	}
	
	
	@Override
	public String getDirectoryPath() {
		return fPath;
	}
	
	public void setDirectoryPath(final String path) {
		fPath = path;
	}
	
	@Override
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
	
	
	@Override
	public String toString() {
		return fPath;
	}
	
}
