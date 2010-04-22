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

package de.walware.statet.r.internal.rdata;

import org.eclipse.jface.text.IRegion;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ISourceUnit;

import de.walware.rj.data.RList;
import de.walware.rj.data.RObject;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;


public abstract class CombinedElement implements ICombinedRElement {
	
	
	CombinedElement fParent;
	protected RElementName fElementName;
	
	
	public RList getAttributes() {
		return null;
	}
	
	
	public String getModelTypeId() {
		return null;
	}
	
	public String getId() {
		return null; // not yet implemented
	}
	
	public final RElementName getElementName() {
		return fElementName;
	}
	
	public boolean exists() {
		return true;
	}
	
	public boolean isReadOnly() {
		return false;
	}
	
	public final CombinedElement getModelParent() {
		return fParent;
	}
	
	public ISourceUnit getSourceUnit() {
		return null;
	}
	
	public IRegion getSourceRange() {
		return null;
	}
	
	public IRegion getNameSourceRange() {
		return null;
	}
	
	public IRegion getDocumentationRange() {
		return null;
	}
	
	
	public Object getAdapter(final Class required) {
		if (IModelElement.class.equals(required)) {
			return this;
		}
		if (RObject.class.equals(required)) {
			return this;
		}
		return null;
	}
	
	
	@Override
	public final int hashCode() {
		if (fParent != null) {
			return singleHash()-fParent.singleHash();
		}
		return singleHash();
	}
	
	protected int singleHash() {
		return getElementName().hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof CombinedElement)) {
			return false;
		}
		final ICombinedRElement other = (ICombinedRElement) obj;
		return (   getElementName().equals(other.getElementName())
				&& ((fParent != null) ? fParent.equals(other.getModelParent()) : (other.getModelParent() == null)));
	}
	
}
