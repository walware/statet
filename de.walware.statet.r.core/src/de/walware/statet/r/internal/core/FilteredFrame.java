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

package de.walware.statet.r.internal.core;

import java.util.List;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ISourceElement;
import de.walware.ecommons.ltk.ISourceUnit;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;


public class FilteredFrame implements IRFrame, IModelElement.Filter {
	
	
	private final IRFrame fFrame;
	private final ISourceUnit fExclude;
	
	
	public FilteredFrame(final IRFrame frame, final ISourceUnit exclude) {
		fFrame = frame;
		fExclude = exclude;
	}
	
	
	@Override
	public String getFrameId() {
		return fFrame.getFrameId();
	}
	
	@Override
	public int getFrameType() {
		return fFrame.getFrameType();
	}
	
	@Override
	public RElementName getElementName() {
		return null;
	}
	
	@Override
	public boolean hasModelChildren(final IModelElement.Filter filter) {
		return fFrame.hasModelChildren((fExclude != null) ? this : null);
	}
	
	@Override
	public List<? extends IRLangElement> getModelChildren(final IModelElement.Filter filter) {
		return fFrame.getModelChildren((fExclude != null) ? this : null);
	}
	
	@Override
	public List<? extends IRElement> getModelElements() {
		return fFrame.getModelElements();
	}
	
	@Override
	public List<? extends IRFrame> getPotentialParents() {
		return fFrame.getPotentialParents();
	}
	
	
	@Override
	public boolean include(final IModelElement element) {
		final ISourceUnit su = (element instanceof ISourceElement) ?
				((ISourceElement) element).getSourceUnit() : null;
		return (su == null || !fExclude.getId().equals(su.getId()) );
	}
	
}
