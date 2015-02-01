/*=============================================================================#
 # Copyright (c) 2014-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.ui.tex.sourceediting;

import de.walware.ecommons.ltk.ISourceStructElement;
import de.walware.ecommons.ltk.ui.sourceediting.OutlineContentProvider;

import de.walware.docmlet.tex.core.model.ILtxSourceElement;
import de.walware.docmlet.tex.core.model.TexModel;

import de.walware.statet.r.internal.sweave.model.TexRChunkElement;


public class LtxROutlineContentProvider extends OutlineContentProvider {
	
	
	public LtxROutlineContentProvider(final IOutlineContent content) {
		super(content);
	}
	
	
	@Override
	public Object getParent(final Object element) {
		final Object parent= super.getParent(element);
		if (parent instanceof TexRChunkElement) {
			return ((ISourceStructElement) element).getSourceParent();
		}
		return parent;
	}
	
	@Override
	public boolean hasChildren(final Object element) {
		final ISourceStructElement e= (ISourceStructElement) element;
		if (e.getModelTypeId() == TexModel.LTX_TYPE_ID
				&& e.getElementType() == ILtxSourceElement.C1_EMBEDDED) {
			return e.hasSourceChildren(null)
					&& e.getSourceChildren(null).get(0).hasSourceChildren(getContent().getContentFilter());
		}
		return super.hasChildren(element);
	}
	
	@Override
	public Object[] getChildren(final Object parentElement) {
		final ISourceStructElement e= (ISourceStructElement) parentElement;
		if (e.getModelTypeId() == TexModel.LTX_TYPE_ID
				&& e.getElementType() == ILtxSourceElement.C1_EMBEDDED) {
			return e.getSourceChildren(null).get(0).getSourceChildren(getContent().getContentFilter()).toArray();
		}
		return super.getChildren(parentElement);
	}
	
}
