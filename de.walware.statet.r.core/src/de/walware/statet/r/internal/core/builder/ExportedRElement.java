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

package de.walware.statet.r.internal.core.builder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ltk.core.model.ISourceUnit;

import de.walware.statet.r.core.model.IRElement;
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
			for (final IRElement child : children) {
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
	
	private int fSourceOffset;
	private int fSourceLength;
	private int fNameOffset;
	private int fNameLength;
	
	
	public ExportedRElement(final IRLangElement parent, final IRLangElement sourceElement) {
		fParent = parent;
		fElementType = sourceElement.getElementType();
		fElementName = RElementName.cloneName(sourceElement.getElementName(), false);
		fElementId = sourceElement.getId();
		
		{	final IRegion sourceRange = sourceElement.getSourceRange();
			if (sourceRange != null) {
				fSourceOffset = sourceRange.getOffset();
				fSourceLength = sourceRange.getLength();
			}
			else {
				fSourceOffset = -1;
			}
		}
		{
			final IRegion sourceRange = sourceElement.getNameSourceRange();
			if (sourceRange != null) {
				fNameOffset = sourceRange.getOffset();
				fNameLength = sourceRange.getLength();
			}
			else {
				fNameOffset = -1;
			}
		}
	}
	
	public ExportedRElement() {
	}
	
	
	@Override
	public String getModelTypeId() {
		return RModel.TYPE_ID;
	}
	
	@Override
	public String getId() {
		return fElementId;
	}
	
	@Override
	public int getElementType() {
		return fElementType;
	}
	
	@Override
	public RElementName getElementName() {
		return fElementName;
	}
	
	@Override
	public boolean exists() {
		return true;
	}
	
	@Override
	public boolean isReadOnly() {
		return false;
	}
	
	
	@Override
	public IRElement getModelParent() {
		return fParent;
	}
	
	@Override
	public boolean hasModelChildren(final Filter filter) {
		return false;
	}
	
	@Override
	public List<? extends IRLangElement> getModelChildren(final Filter filter) {
		return Collections.EMPTY_LIST;
	}
	
	
	@Override
	public ISourceUnit getSourceUnit() {
		return fParent.getSourceUnit();
	}
	
	@Override
	public IRegion getSourceRange() {
		if (fSourceOffset >= 0) {
			return new Region(fSourceOffset, fSourceLength);
		}
		return null;
	}
	
	@Override
	public IRegion getNameSourceRange() {
		if (fNameOffset >= 0) {
			return new Region(fNameOffset, fNameLength);
		}
		return null;
	}
	
	@Override
	public IRegion getDocumentationRange() {
		return null;
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		return null;
	}
	
}
