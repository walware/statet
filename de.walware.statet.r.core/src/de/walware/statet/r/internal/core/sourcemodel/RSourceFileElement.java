/*=============================================================================#
 # Copyright (c) 2008-2015 Stephan Wahlbrink (WalWare.de) and others.
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

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ltk.core.model.ISourceStructElement;
import de.walware.ecommons.ltk.core.model.ISourceUnitModelInfo;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;


abstract class RSourceFileElement implements IRLangSourceElement {
	
	
	private final IRSourceUnit fSourceUnit;
	
	protected final BuildSourceFrame fEnvir;
	
	
	public RSourceFileElement(final IRSourceUnit su, final BuildSourceFrame envir) {
		fSourceUnit = su;
		fEnvir = envir;
	}
	
	
	@Override
	public final String getModelTypeId() {
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
	public IRSourceUnit getSourceUnit() {
		return fSourceUnit;
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
