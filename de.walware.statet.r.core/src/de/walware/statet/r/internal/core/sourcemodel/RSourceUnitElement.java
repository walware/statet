/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import org.eclipse.jface.text.IRegion;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.text.ISourceStructElement;

import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.RAstNode;


class RSourceUnitElement extends AbstractRModelElement {
	
	
	private ISourceUnit fUnit;
	private final RAstNode fSourceNode;
	
	
	public RSourceUnitElement(final ISourceUnit unit, final RAstNode node) {
		fSourceNode = node;
		fUnit = unit;
	}
	
	
	public IModelElement getParent() {
		return null;
	}
	
	public ISourceUnit getSourceUnit() {
		return fUnit;
	}
	
	public int getElementType() {
		return IModelElement.C2_SOURCE_FILE;
	}
	
	public IElementName getElementName() {
		return fUnit.getElementName();
	}
	
	public String getId() {
		return fUnit.getId();
	}
	
	public boolean exists() {
		final ISourceUnitModelInfo modelInfo = getSourceUnit().getModelInfo(RModel.TYPE_ID, 0, null);
		return (modelInfo != null && modelInfo.getSourceElement() == this);
	}
	
	public boolean isReadOnly() {
		return fUnit.isReadOnly();
	}
	
	
	public IRegion getSourceRange() {
		return fSourceNode;
	}
	
	public IRegion getNameSourceRange() {
		return null;
	}
	
	
	public Object getAdapter(final Class adapter) {
		if (IAstNode.class.equals(adapter)) {
			return fSourceNode;
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		return fUnit.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof ISourceStructElement)) {
			return false;
		}
		final ISourceStructElement other = (ISourceStructElement) obj;
		return ((other.getElementType() & IModelElement.MASK_C2) == IModelElement.C2_SOURCE_FILE)
				&& fUnit.equals(other.getSourceUnit());
	}
	
}