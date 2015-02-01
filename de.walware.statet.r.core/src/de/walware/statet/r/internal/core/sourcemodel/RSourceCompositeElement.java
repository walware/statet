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

import de.walware.ecommons.collections.CollectionUtils;
import de.walware.ecommons.collections.ConstArrayList;

import de.walware.statet.r.core.model.IRCompositeSourceElement;
import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.core.model.IRSourceUnit;


public class RSourceCompositeElement extends RSourceFileElement
		implements IRCompositeSourceElement {
	
	
	private final List<? extends RChunkBuildElement> fElementsProtected;
	
	private final IRegion fSourceRange;
	
	private List<? extends IRLangSourceElement> fAllSourceChildrenProtected;
	
	
	public RSourceCompositeElement(final IRSourceUnit su, final BuildSourceFrame envir,
			final List<? extends RChunkBuildElement> elements, final IRegion sourceRange) {
		super(su, envir);
		
		fElementsProtected = CollectionUtils.asConstList(elements);
		fSourceRange = sourceRange;
	}
	
	
//	@Override
//	public int getElementType() {
//		return IRElement.C2_SOURCE_FILE | 0x1;
//	}
	
	@Override
	public List<? extends IRLangSourceElement> getCompositeElements() {
		return fElementsProtected;
	}
	
	
	@Override
	public boolean hasSourceChildren(final Filter filter) {
		for (final RChunkBuildElement element : fElementsProtected) {
			if (element.hasSourceChildren(filter)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public List<? extends IRLangSourceElement> getSourceChildren(final Filter filter) {
		if (filter == null) {
			if (fAllSourceChildrenProtected == null) {
				int i = 0;
				for (final RChunkBuildElement element : fElementsProtected) {
					i += element.fSourceChildrenProtected.size();
				}
				final IRLangSourceElement[] children = new IRLangSourceElement[i];
				i = 0;
				for (final RChunkBuildElement element : fElementsProtected) {
					final List<? extends IRLangSourceElement> list = element.fSourceChildrenProtected;
					for (final IRLangSourceElement child : list) {
						children[i++] = child;
					}
				}
				fAllSourceChildrenProtected = new ConstArrayList<IRLangSourceElement>(children);
			}
			return fAllSourceChildrenProtected;
		}
		else {
			final List<IRLangSourceElement> children = new ArrayList<IRLangSourceElement>();
			for (final RChunkBuildElement element : fElementsProtected) {
				final List<? extends IRLangSourceElement> list = element.fSourceChildrenProtected;
				for (final IRLangSourceElement child : list) {
					if (filter.include(child)) {
						children.add(child);
					}
				}
			}
			return children;
		}
	}
	
	
	@Override
	public IRegion getSourceRange() {
		return fSourceRange;
	}
	
	
	
}
