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

import java.util.ArrayList;
import java.util.List;

import de.walware.ecommons.ConstList;


/**
 * A group of {@link RLibraryLocation R library locations}.
 */
public class RLibraryGroup {
	
	/** Id of R default library group */
	public static final String R_DEFAULT = "r.default"; //$NON-NLS-1$
	/** Id of R site libraries group */
	public static final String R_SITE = "r.site"; //$NON-NLS-1$
	/** Id of R other libraries group */
	public static final String R_OTHER = "r.common"; //$NON-NLS-1$
	/** Id of R user libraries group */
	public static final String R_USER = "r.user"; //$NON-NLS-1$
	
	public static final String DEFAULTLOCATION_R_DEFAULT = "${env_var:R_HOME}/library"; //$NON-NLS-1$
	public static final String DEFAULTLOCATION_R_SITE = "${env_var:R_HOME}/site-library"; //$NON-NLS-1$
	
	
	private final String fId;
	private final String fLabel;
	private final List<RLibraryLocation> fLibraries;
	
	
	RLibraryGroup(final String id, final String label, final List<RLibraryLocation> libraries) {
		fId = id;
		fLabel = label;
		fLibraries = libraries;
	}
	
	RLibraryGroup(final RLibraryGroup template, final boolean editable) {
		fId = template.getId();
		fLabel = template.getLabel();
		final List<RLibraryLocation> libs = template.getLibraries();
		fLibraries = (editable) ? new ArrayList<RLibraryLocation>(libs) :
			new ConstList<RLibraryLocation>(libs);
	}
	
	
	public String getId() {
		return fId;
	}
	
	public String getLabel() {
		return fLabel;
	}
	
	public List<RLibraryLocation> getLibraries() {
		return fLibraries;
	}
	
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof RLibraryGroup)) {
			return false;
		}
		final RLibraryGroup other = (RLibraryGroup) obj;
		return ( fLabel.equals(other.fLabel)
				&& fLibraries.equals(other.fLibraries) );
	}
	
}
