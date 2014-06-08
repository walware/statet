/*=============================================================================#
 # Copyright (c) 2008-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.sourcemodel;

import java.util.List;

import org.eclipse.jface.text.IRegion;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ltk.core.model.ISourceStructElement;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.RAstNode;


final class RoxygenRCodeElement implements IBuildSourceFrameElement {
	
	
	private final IRLangSourceElement fParent;
	private final int fNumber;
	
	private final RAstNode fSourceNode;
	private final List<? extends IRLangSourceElement> fSourceChildrenProtected = RSourceElements.NO_R_SOURCE_CHILDREN;
	private BuildSourceFrame fEnvir;
	
	
	public RoxygenRCodeElement(final IRLangSourceElement parent, final int number, final BuildSourceFrame envir, final RAstNode node) {
		fParent = parent;
		fNumber = number;
		fSourceNode = node;
	}
	
	
	@Override
	public void setSourceChildren(final List<? extends IRLangSourceElement> children) {
//		fSourceChildrenProtected = children;
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
		return IRElement.R_DOC_EXAMPLE_CHUNK;
	}
	
	@Override
	public RElementName getElementName() {
		return null;
	}
	
	@Override
	public String getId() {
		return Integer.toHexString(IRElement.R_DOC_EXAMPLE_CHUNK) + ":#" + fNumber;
	}
	
	@Override
	public boolean exists() {
		return getSourceUnit().exists();
	}
	
	@Override
	public boolean isReadOnly() {
		return getSourceUnit().isReadOnly();
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
		return RSourceElements.NO_R_SOURCE_CHILDREN;
	}
	
	@Override
	public ISourceStructElement getSourceParent() {
		return fParent;
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
	public IRSourceUnit getSourceUnit() {
		return fParent.getSourceUnit();
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
		return fParent.hashCode()+fNumber;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof RoxygenRCodeElement)) {
			return false;
		}
		final RoxygenRCodeElement other = (RoxygenRCodeElement) obj;
		return ( ((other.getElementType() & IModelElement.MASK_C3) == IRElement.R_DOC_EXAMPLE_CHUNK)
				&& fNumber == other.fNumber
				&& fParent.equals(other.fParent) );
	}
	
}
