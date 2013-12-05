/*******************************************************************************
 * Copyright (c) 2008-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.ecommons.ltk.ast.IAstNode;

import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.rsource.ast.RAstNode;


final class RSourceUnitElement extends RSourceFileElement implements IBuildSourceFrameElement {
	
	
	private final RAstNode fSourceNode;
	private List<? extends IRLangSourceElement> fSourceChildrenProtected = RSourceElements.NO_R_SOURCE_CHILDREN;
	
	
	public RSourceUnitElement(final IRSourceUnit su, final BuildSourceFrame envir, final RAstNode node) {
		super(su, envir);
		fSourceNode = node;
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
	public boolean hasSourceChildren(final Filter filter) {
		return RSourceElements.hasChildren(fSourceChildrenProtected, filter);
	}
	
	@Override
	public List<? extends IRLangSourceElement> getSourceChildren(final Filter filter) {
		return RSourceElements.getChildren(fSourceChildrenProtected, filter);
	}
	
	
	@Override
	public IRegion getSourceRange() {
		return fSourceNode;
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (IAstNode.class.equals(required)) {
			return fSourceNode;
		}
		return null;
	}
	
	
	@Override
	public String toString() {
		final StringBuilder sb= new StringBuilder("RSourceUnitElement"); //$NON-NLS-1$
		final RElementName elementName= getElementName();
		if (elementName != null) {
			sb.append(' ').append(elementName);
		}
		else {
			sb.append(" <unnamed>"); //$NON-NLS-1$
		}
		
		return sb.toString();
	}
	
}
