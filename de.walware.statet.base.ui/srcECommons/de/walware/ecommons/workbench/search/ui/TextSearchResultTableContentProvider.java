/*=============================================================================#
 # Copyright (c) 2013-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.workbench.search.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.search.ui.text.Match;

import de.walware.jcommons.collections.SortedArraySet;
import de.walware.jcommons.collections.SortedListSet;


public class TextSearchResultTableContentProvider<E, M extends Match>
		extends TextSearchResultContentProvider<E, M, TableViewer> {
	
	
	protected final ElementMatchComparator<E, M> comparator;
	
	private final SortedListSet<E> currentElements;
	
	
	public TextSearchResultTableContentProvider(final ExtTextSearchResultPage<E, M> page,
			final TableViewer viewer) {
		super(page, viewer);
		
		this.comparator= page.comparator;
		this.currentElements= new SortedArraySet<E>(this.comparator.getElement0(), this.comparator.getElementComparator());
	}
	
	
	@Override
	protected void reset() {
		super.reset();
		this.currentElements.clear();
	}
	
	@Override
	public Object[] getElements(final Object inputElement) {
		if (!this.active) {
			final ExtTextSearchResult<E, M> result= getInput();
			assert (result == inputElement);
			if (result == null) {
				return NO_ELEMENTS;
			}
			assert (this.currentElements.isEmpty());
			
			final E[] elements= result.getElements();
			final int limit= getElementLimit();
			
			if (elements.length <= limit && result.getActiveMatchFilters() == null) {
				this.currentElements.addAll(new SortedArraySet<E>(elements, result.getComparator().getElementComparator()));
				
				this.active= true;
				return elements;
			}
			
			for (int i= 0; i < elements.length && this.currentElements.size() < limit; i++) {
				if (result.hasPickedMatches(elements[i])) {
					this.currentElements.addE(this.currentElements.size(), elements[i]);
				}
			}
			this.active= true;
		}
		return this.currentElements.toArray();
	}
	
	@Override
	public void elementsChanged(final Object[] elements) {
		if (!this.active) {
			return;
		}
		
		final ExtTextSearchResult<E, M> result= getInput();
		final int limit= getElementLimit();
		final TableViewer viewer= getViewer();
		
		viewer.getTable().setRedraw(false);
		try {
//			final List<E> toAdd= new ArrayList<E>();
			final List<E> toUpdate= new ArrayList<E>();
//			final List<E> toRemove= new ArrayList<E>();
			for (int i= 0; i < elements.length; i++) {
				final E element= (E) elements[i];
				if (result.hasPickedMatches(element)) {
					if (this.currentElements.size() < limit) {
						final int idx= this.currentElements.addE(element);
						if (idx >= 0) {
							viewer.insert(element, idx);
//							toAdd.add(element);
						}
						else {
							toUpdate.add(element);
						}
					}
					else if (this.currentElements.contains(element)) {
						toUpdate.add(element);
					}
				}
				else {
					final int idx= this.currentElements.removeE(element);
					if (idx >= 0) {
						viewer.remove(element);
//						toRemove.add(element);
					}
				}
			}
			
//			viewer.remove(toRemove.toArray());
//			viewer.add(toAdd.toArray());
			viewer.refresh(toUpdate.toArray(), true);
		}
		finally {
			viewer.getTable().setRedraw(true);
		}
	}
	
}
