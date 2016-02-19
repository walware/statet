/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core;

import java.util.List;

import de.walware.jcommons.lang.ObjectUtils;

import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ltk.core.model.ISourceElement;
import de.walware.ecommons.ltk.core.model.ISourceUnit;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;


public class FilteredFrame implements IRFrame, IModelElement.Filter {
	
	
	private final IRFrame frame;
	private final ISourceUnit exclude;
	
	
	public FilteredFrame(final IRFrame frame, final ISourceUnit exclude) {
		this.frame= frame;
		this.exclude= exclude;
	}
	
	
	@Override
	public String getFrameId() {
		return this.frame.getFrameId();
	}
	
	@Override
	public int getFrameType() {
		return this.frame.getFrameType();
	}
	
	@Override
	public RElementName getElementName() {
		return this.frame.getElementName();
	}
	
	@Override
	public boolean hasModelChildren(final IModelElement.Filter filter) {
		return this.frame.hasModelChildren((this.exclude != null) ? this : null);
	}
	
	@Override
	public List<? extends IRLangElement> getModelChildren(final IModelElement.Filter filter) {
		return this.frame.getModelChildren((this.exclude != null) ? this : null);
	}
	
	@Override
	public List<? extends IRElement> getModelElements() {
		return this.frame.getModelElements();
	}
	
	@Override
	public List<? extends IRFrame> getPotentialParents() {
		return this.frame.getPotentialParents();
	}
	
	
	@Override
	public boolean include(final IModelElement element) {
		final ISourceUnit su= (element instanceof ISourceElement) ?
				((ISourceElement) element).getSourceUnit() : null;
		return (su == null || !this.exclude.getId().equals(su.getId()) );
	}
	
	
	@Override
	public String toString() {
		final ObjectUtils.ToStringBuilder builder= new ObjectUtils.ToStringBuilder(
				"FilteredFrame", getClass() ); //$NON-NLS-1$
		builder.addProp("frame", this.frame); //$NON-NLS-1$
		builder.addProp("exclude", this.exclude); //$NON-NLS-1$
		return builder.build();
	}
	
}
