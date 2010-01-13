/*******************************************************************************
 * Copyright (c) 2008-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ISourceStructElement;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ast.IAstNode;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.RAstNode;


final class RoxygenRCodeElement implements IBuildSourceFrameElement {
	
	
	private final ISourceStructElement fParent;
	private final int fNumber;
	
	private final RAstNode fSourceNode;
	private List<? extends IRLangSourceElement> fSourceChildrenProtected = NO_R_SOURCE_CHILDREN;
	private BuildSourceFrame fEnvir;
	
	
	public RoxygenRCodeElement(final ISourceStructElement parent, final int number, final BuildSourceFrame envir, final RAstNode node) {
		fParent = parent;
		fNumber = number;
		fSourceNode = node;
	}
	
	
	public void setSourceChildren(final List<? extends IRLangSourceElement> children) {
//		fSourceChildrenProtected = children;
	}
	
	public BuildSourceFrame getBuildFrame() {
		return fEnvir;
	}
	
	
	public String getModelTypeId() {
		return RModel.TYPE_ID;
	}
	
	public int getElementType() {
		return IRElement.R_DOC_EXAMPLE_CHUNK;
	}
	
	public RElementName getElementName() {
		return null;
	}
	
	public String getId() {
		return Integer.toHexString(IRElement.R_DOC_EXAMPLE_CHUNK) + ":#" + fNumber;
	}
	
	public boolean exists() {
		return getSourceUnit().exists();
	}
	
	public boolean isReadOnly() {
		return getSourceUnit().isReadOnly();
	}
	
	
	public IRElement getModelParent() {
		return null;
	}
	
	public boolean hasModelChildren(final Filter filter) {
		return false;
	}
	
	public List<? extends IRLangSourceElement> getModelChildren(final Filter filter) {
		return NO_R_SOURCE_CHILDREN;
	}
	
	public ISourceStructElement getSourceParent() {
		return fParent;
	}
	
	public boolean hasSourceChildren(final Filter filter) {
		return RSourceElements.hasChildren(fSourceChildrenProtected, filter);
	}
	
	public List<? extends IRLangSourceElement> getSourceChildren(final Filter filter) {
		return RSourceElements.getChildren(fSourceChildrenProtected, filter);
	}
	
	public ISourceUnit getSourceUnit() {
		return fParent.getSourceUnit();
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
