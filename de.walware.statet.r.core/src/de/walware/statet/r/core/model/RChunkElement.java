/*=============================================================================#
 # Copyright (c) 2011-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.model;

import org.eclipse.jface.text.IRegion;

import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.core.model.ISourceStructElement;

import de.walware.statet.r.core.rsource.ast.SourceComponent;
import de.walware.statet.r.internal.core.sourcemodel.RChunkBuildElement;


public abstract class RChunkElement extends RChunkBuildElement implements IRLangSourceElement {
	
	
	public RChunkElement(final ISourceStructElement parent, final IAstNode node,
			final RElementName name, final IRegion nameRegion) {
		super(parent, node, name, nameRegion);
	}
	
	
	protected IAstNode getNode() {
		return fNode;
	}
	
	protected abstract ImList<SourceComponent> getSourceComponents();
	
	
	@Override
	public final String getModelTypeId() {
		return RModel.R_TYPE_ID;
	}
	
	@Override
	public final int getElementType() {
		return IRElement.C2_SOURCE_CHUNK;
	}
	
	@Override
	public RElementName getElementName() {
		return fName;
	}
	
	@Override
	public String getId() {
		return fName.getSegmentName();
	}
	
	@Override
	public IRegion getSourceRange() {
		return fNode;
	}
	
	@Override
	public IRegion getNameSourceRange() {
		return fNameRegion;
	}
	
	@Override
	public IRegion getDocumentationRange() {
		return null;
	}
	
	
	@Override
	public IRSourceUnit getSourceUnit() {
		return (IRSourceUnit) fParent.getSourceUnit();
	}
	
	@Override
	public boolean isReadOnly() {
		return fParent.isReadOnly();
	}
	
	@Override
	public boolean exists() {
		return fParent.exists();
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (SourceComponent.class.equals(required)) {
			return getSourceComponents();
		}
		return super.getAdapter(required);
	}
	
}
