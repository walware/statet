/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.RAstNode;


final class RSourceUnitElement implements IBuildSourceFrameElement {
	
	
	private ISourceUnit fSourceUnit;
	
	private final RAstNode fSourceNode;
	private List<? extends IRLangSourceElement> fSourceChildrenProtected = NO_R_SOURCE_CHILDREN;
	private BuildSourceFrame fEnvir;
	
	
	public RSourceUnitElement(final ISourceUnit unit, final BuildSourceFrame envir, final RAstNode node) {
		fSourceNode = node;
		fSourceUnit = unit;
	}
	
	
	public void setSourceChildren(final List<? extends IRLangSourceElement> children) {
		fSourceChildrenProtected = children;
	}
	
	public BuildSourceFrame getBuildFrame() {
		return fEnvir;
	}
	
	
	public String getModelTypeId() {
		return RModel.TYPE_ID;
	}
	
	public int getElementType() {
		return IModelElement.C2_SOURCE_FILE;
	}
	
	public IElementName getElementName() {
		return fSourceUnit.getElementName();
	}
	
	public String getId() {
		return fSourceUnit.getId();
	}
	
	public boolean exists() {
		final ISourceUnitModelInfo modelInfo = getSourceUnit().getModelInfo(RModel.TYPE_ID, 0, null);
		return (modelInfo != null && modelInfo.getSourceElement() == this);
	}
	
	public boolean isReadOnly() {
		return fSourceUnit.isReadOnly();
	}
	
	
	public IRLangElement getModelParent() {
		return null;
	}
	
	public boolean hasModelChildren(final Filter filter) {
		return false;
	}
	
	public List<? extends IRLangSourceElement> getModelChildren(final Filter filter) {
		return null;
	}
	
	public ISourceStructElement getSourceParent() {
		return null;
	}
	
	public boolean hasSourceChildren(final Filter filter) {
		return RSourceElements.hasChildren(fSourceChildrenProtected, filter);
	}
	
	public List<? extends IRLangSourceElement> getSourceChildren(final Filter filter) {
		return RSourceElements.getChildren(fSourceChildrenProtected, filter);
	}
	
	public ISourceUnit getSourceUnit() {
		return fSourceUnit;
	}
	
	
	public IRegion getSourceRange() {
		return fSourceNode;
	}
	
	public IRegion getNameSourceRange() {
		return null;
	}
	
	public IRegion getDocumentationRange() {
		return null;
	}
	
	
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
