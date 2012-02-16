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

package de.walware.statet.r.internal.core.sourcemodel;

import java.util.List;

import org.eclipse.jface.text.IRegion;

import de.walware.ecommons.ltk.ISourceStructElement;
import de.walware.ecommons.ltk.ast.IAstNode;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.core.model.RElementName;


public abstract class RChunkBuildElement implements IBuildSourceFrameElement {
	
	
	protected final ISourceStructElement fParent;
	protected final IAstNode fNode;
	
	protected final RElementName fName;
	protected final IRegion fNameRegion;
	int fOccurrenceCount;
	
	BuildSourceFrame fEnvir;
	List<? extends IRLangSourceElement> fSourceChildrenProtected = RSourceElements.NO_R_SOURCE_CHILDREN;
	
	
	public RChunkBuildElement(final ISourceStructElement parent, final IAstNode node,
			final RElementName name, final IRegion nameRegion) {
		fParent = parent;
		fNode = node;
		
		fName = name;
		fNameRegion = nameRegion;
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
	public Object getAdapter(final Class required) {
		if (IRFrame.class.equals(required)) {
			return fEnvir;
		}
		if (IAstNode.class.equals(required)) {
			return fNode;
		}
		return null;
	}
	
	
	@Override
	public int hashCode() {
		return IRElement.C2_SOURCE_CHUNK * fName.getSegmentName().hashCode() + fOccurrenceCount;
	}
	
	@Override
	public boolean equals(final Object obj) {
		final RChunkBuildElement other;
		return ( (obj instanceof RChunkBuildElement)
				&& fOccurrenceCount == (other = (RChunkBuildElement) obj).fOccurrenceCount
				&& fName.equals(other.fName) );
	}
	
}
