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


public class RLibraryGroup implements IRLibraryGroup {
	
	
	private static List<RLibraryLocation.Editable> toWorkingCopy(final List<RLibraryLocation> locations) {
		final List<RLibraryLocation.Editable> copies = new ArrayList<RLibraryLocation.Editable>(locations.size());
		for (final RLibraryLocation location : locations) {
			copies.add(location.createWorkingCopy());
		}
		return copies;
	}
	
	public static class Editable extends RLibraryGroup implements WorkingCopy {
		
		
		public Editable(final RLibraryGroup template) {
			super(template.getId(), template.getLabel(),
					toWorkingCopy(template.getLibraries()) );
		}
		
		public Editable(final String id, final String label) {
			super(id, label, new ArrayList<IRLibraryLocation>());
		}
		
		
		public RLibraryLocation.Editable newLibrary(final String path) {
			return new RLibraryLocation.Editable(path);
		}
		
	}
	
	
	private final String fId;
	private String fLabel;
	protected final List fLibraries;
	
	
	public RLibraryGroup(final String id, final String label, final List<? extends IRLibraryLocation> libraries) {
		fId = id;
		fLabel = label;
		fLibraries = libraries;
	}
	
	public RLibraryGroup(final IRLibraryGroup template) {
		this(template.getId(), template.getLabel(),
				new ConstList<IRLibraryLocation>(template.getLibraries() ));
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
	
	public List getLibraries() {
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
