/*******************************************************************************
 * Copyright (c) 2008-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import java.util.List;

import org.eclipse.jface.text.IRegion;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ISourceStructElement;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.ast.IAstNode;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.RAstNode;


final class RSourceUnitElement implements IBuildSourceFrameElement {
	
	
	private final ISourceUnit fSourceUnit;
	
	private final RAstNode fSourceNode;
	private List<? extends IRLangSourceElement> fSourceChildrenProtected = RSourceElements.NO_R_SOURCE_CHILDREN;
	private BuildSourceFrame fEnvir;
	
	
	public RSourceUnitElement(final ISourceUnit unit, final BuildSourceFrame envir, final RAstNode node) {
		fSourceNode = node;
		fSourceUnit = unit;
	}
	
	
	@Override
	public void setSourceChildren(final List<? extends IRLangSourceElement> children) {
		fSourceChildrenProtected = children;
	}
	
	@Override
	public BuildSourceFrame getBuildFrame() {
		return fEnvir;
	}
	
	
	@Override
	public String getModelTypeId() {
		return RModel.TYPE_ID;
	}
	
	@Override
	public int getElementType() {
		return IModelElement.C2_SOURCE_FILE;
	}
	
	@Override
	public RElementName getElementName() {
		final IElementName elementName = fSourceUnit.getElementName();
		if (elementName instanceof RElementName) {
			return (RElementName) elementName;
		}
		return RElementName.create(RElementName.RESOURCE, elementName.getSegmentName());
	}
	
	@Override
	public String getId() {
		return fSourceUnit.getId();
	}
	
	@Override
	public boolean exists() {
		final ISourceUnitModelInfo modelInfo = getSourceUnit().getModelInfo(RModel.TYPE_ID, 0, null);
		return (modelInfo != null && modelInfo.getSourceElement() == this);
	}
	
	@Override
	public boolean isReadOnly() {
		return fSourceUnit.isReadOnly();
	}
	
	
	@Override
	public IRElement getModelParent() {
		return null;
	}
	
	@Override
	public boolean hasModelChildren(final Filter filter) {
		return false;
	}
	
	@Override
	public List<? extends IRLangSourceElement> getModelChildren(final Filter filter) {
		return null;
	}
	
	@Override
	public ISourceStructElement getSourceParent() {
		return null;
	}
	
	@Override
	public boolean hasSourceChildren(final Filter filter) {
		return RSourceElements.hasChildren(fSourceChildrenProtected, filter);
	}
	
	@Override
	public List<? extends IRLangSourceElement> getSourceChildren(final Filter filter) {
		return RSourceElements.getChildren(fSourceChildrenProtected, filter);
	}
	
	@Override
	public ISourceUnit getSourceUnit() {
		return fSourceUnit;
	}
	
	
	@Override
	public IRegion getSourceRange() {
		return fSourceNode;
	}
	
	@Override
	public IRegion getNameSourceRange() {
		return null;
	}
	
	@Override
	public IRegion getDocumentationRange() {
		return null;
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (IAstNode.class.equals(required)) {
			return fSourceNode;
		}
		if (IRFrame.class.equals(required)) {
			return fEnvir;
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		return fSourceUnit.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof ISourceStructElement)) {
			return false;
		}
		final ISourceStructElement other = (ISourceStructElement) obj;
		return ((other.getElementType() & IModelElement.MASK_C2) == IModelElement.C2_SOURCE_FILE)
				&& fSourceUnit.equals(other.getSourceUnit());
	}
	
}
