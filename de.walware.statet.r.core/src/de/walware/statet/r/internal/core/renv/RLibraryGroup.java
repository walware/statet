/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.renv;

import java.util.ArrayList;
import java.util.List;

import de.walware.ecommons.collections.ConstList;

import de.walware.statet.r.core.renv.IRLibraryGroup;
import de.walware.statet.r.core.renv.IRLibraryLocation;


public abstract class RLibraryGroup implements IRLibraryGroup {
	
	
	private static List<IRLibraryLocation.WorkingCopy> toWorkingCopy(final List<? extends IRLibraryLocation> locations) {
		final List<IRLibraryLocation.WorkingCopy> copies = new ArrayList<IRLibraryLocation.WorkingCopy>(locations.size());
		for (final IRLibraryLocation location : locations) {
			copies.add(((RLibraryLocation) location).createWorkingCopy());
		}
		return copies;
	}
	
	public static class Final extends RLibraryGroup {
		
		
		public Final(final String id, final String label, final List<RLibraryLocation> libraries) {
			super(id, label, libraries);
		}
		
		public Final(final RLibraryGroup template) {
			super(template.getId(), template.getLabel(),
					new ConstList<IRLibraryLocation>(template.getLibraries() ));
		}
		
	}
	
	public static class Editable extends RLibraryGroup implements WorkingCopy {
		
		
		public Editable(final RLibraryGroup template) {
			super(template.getId(), template.getLabel(),
					toWorkingCopy(template.getLibraries()) );
		}
		
		public Editable(final String id, final String label) {
			super(id, label, new ArrayList<IRLibraryLocation.WorkingCopy>());
		}
		
		
		public RLibraryLocation.Editable newLibrary(final String path) {
			return new RLibraryLocation.Editable(path);
		}
		
		@Override
		public List<IRLibraryLocation.WorkingCopy> getLibraries() {
			return (List<IRLibraryLocation.WorkingCopy>) fLibraries;
		}
		
	}
	
	
	private final String fId;
	private String fLabel;
	protected final List<? extends IRLibraryLocation> fLibraries;
	
	
	private RLibraryGroup(final String id, final String label, final List<? extends IRLibraryLocation> libraries) {
		fId = id;
		fLabel = label;
		fLibraries = libraries;
	}
	
	
	public String getId() {
		return fId;
	}
	
	public String getLabel() {
		return fLabel;
	}
	
	public void setLabel(final String label) {
		fLabel = label;
	}
	
	public List<? extends IRLibraryLocation> getLibraries() {
		return fLibraries;
	}
	
	
	@Override
	public int hashCode() {
		return fId.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RLibraryGroup)) {
			return false;
		}
		final RLibraryGroup other = (RLibraryGroup) obj;
		return (fId.equals(other.fId)
				&& fLabel.equals(other.fLabel)
				&& fLibraries.equals(other.fLibraries) );
	}
	
}
