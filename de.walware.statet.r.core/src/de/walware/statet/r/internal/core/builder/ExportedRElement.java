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

package de.walware.statet.r.internal.core.builder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ISourceUnit;

import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;


public class ExportedRElement implements IRLangElement, Serializable {
	
	
	private static final long serialVersionUID = -493469386405499748L;
	
	
	static final List<? extends IRLangElement> getChildren(final List<? extends IRLangElement> children, final IModelElement.Filter filter) {
		if (filter == null) {
			return children;
		}
		else {
			final ArrayList<IRLangElement> filtered = new ArrayList<IRLangElement>(children.size());
			for (final IRLangElement child : children) {
				if (filter.include(child)) {
					filtered.add(child);
				}
			}
			return filtered;
		}
	}
	
	static final boolean hasChildren(final List<? extends IRLangElement> children, final IModelElement.Filter filter) {
		if (filter == null) {
			return (!children.isEmpty());
		}
		else {
			for (final IRLangElement child : children) {
				if (filter.include(child)) {
					return true;
				}
			}
			return false;
		}
	}
	
	
	private IRLangElement fParent;
	private int fElementType;
	private RElementName fElementName;
	private String fElementId;
	
	
	public ExportedRElement(final IRLangElement parent, final IRLangElement sourceElement) {
		fParent = parent;
		fElementType = sourceElement.getElementType();
		fElementName = RElementName.cloneName(sourceElement.getElementName(), false);
		fElementId = sourceElement.getId();
	}
	
	public ExportedRElement() {
	}
	
	
	public String getModelTypeId() {
		return RModel.TYPE_ID;
	}
	
	public String getId() {
		return fElementId;
	}
	
	public int getElementType() {
		return fElementType;
	}
	
	public RElementName getElementName() {
		return fElementName;
	}
	
	public boolean exists() {
		return true;
	}
	
	public boolean isReadOnly() {
		return false;
	}
	
	
	public ISourceUnit getSourceUnit() {
		return fParent.getSourceUnit();
	}
	
	public IRLangElement getModelParent() {
		return fParent;
	}
	
	public boolean hasModelChildren(final Filter filter) {
		return false;
	}
	
	public List<? extends IRLangElement> getModelChildren(final Filter filter) {
		return Collections.EMPTY_LIST;
	}
	
	public Object getAdapter(final Class required) {
		return null;
	}
	
}
