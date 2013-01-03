/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.pkgmanager;


public class RPkgType {
	
	
	public static final String SOURCE_ID = "source"; //$NON-NLS-1$
	public static final String BINARY_ID = "binary"; //$NON-NLS-1$
	
	public static final RPkgType SOURCE = new RPkgType(SOURCE_ID, "Source");
	public static final RPkgType BINARY = new RPkgType(BINARY_ID, "Binary");
	
	public static RPkgType getPkgType(final String id) {
		if (id != null) {
			if (id.equals(SOURCE_ID)) {
				return SOURCE;
			}
			if (id.equals(BINARY_ID)) {
				return BINARY;
			}
		}
		return null;
	}
	
	
	private final String fId;
	private final String fLabel;
	
	
	public RPkgType(final String id, final String label) {
		if (id == null) {
			throw new NullPointerException("id"); //$NON-NLS-1$
		}
		if (label == null) {
			throw new NullPointerException("label"); //$NON-NLS-1$
		}
		fId = id;
		fLabel = label;
	}
	
	
	public String getId() {
		return fId;
	}
	
	public String getLabel() {
		return fLabel;
	}
	
	
	@Override
	public int hashCode() {
		return fId.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		return (obj == this || (obj instanceof RPkgType
				&& (fId.equals(((RPkgType) obj).fId)) ));
	}
	
	@Override
	public String toString() {
		return fLabel;
	}
	
}
