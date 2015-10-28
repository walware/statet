/*=============================================================================#
 # Copyright (c) 2011-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.sourcemodel;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IRegion;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.statet.r.core.model.IRCompositeSourceElement;
import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.core.model.IRSourceUnit;


public class CompositeSourceElement extends RSourceFileElement
		implements IRCompositeSourceElement {
	
	
	private final ImList<? extends RChunkBuildElement> compositeElements;
	
	private final IRegion sourceRange;
	
	private volatile List<IRLangSourceElement> allSourceChildren;
	
	
	public CompositeSourceElement(final IRSourceUnit su, final BuildSourceFrame envir,
			final List<? extends RChunkBuildElement> elements, final IRegion sourceRange) {
		super(su, envir);
		
		this.compositeElements= ImCollections.toList(elements);
		this.sourceRange= sourceRange;
	}
	
	
//	@Override
//	public int getElementType() {
//		return IRElement.C2_SOURCE_FILE | 0x1;
//	}
	
	@Override
	public ImList<? extends IRLangSourceElement> getCompositeElements() {
		return this.compositeElements;
	}
	
	
	@Override
	public IRegion getSourceRange() {
		return this.sourceRange;
	}
	
	@Override
	public boolean hasSourceChildren(final Filter filter) {
		for (final RChunkBuildElement element : this.compositeElements) {
			if (element.hasSourceChildren(filter)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public List<IRLangSourceElement> getSourceChildren(final Filter filter) {
		if (filter == null) {
			List<IRLangSourceElement> children= this.allSourceChildren;
			if (children == null) {
				final List<? extends IRLangSourceElement>[] compositeLists= new List[this.compositeElements.size()];
				for (int i= 0; i < compositeLists.length; i++) {
					compositeLists[i]= this.compositeElements.get(i).getSourceChildren(null);
				}
				children= this.allSourceChildren= ImCollections.concatList(compositeLists);
			}
			return children;
		}
		else {
			final List<IRLangSourceElement> children= new ArrayList<>();
			for (final RChunkBuildElement element : this.compositeElements) {
				final List<? extends IRLangSourceElement> list= element.getSourceChildren(null);
				for (final IRLangSourceElement child : list) {
					if (filter.include(child)) {
						children.add(child);
					}
				}
			}
			return children;
		}
	}
	
}
