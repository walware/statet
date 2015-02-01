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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.text.Match;

import de.walware.ecommons.collections.SortedArraySet;


public abstract class TextSearchResultTreeContentProvider<E, M extends Match>
		extends TextSearchResultContentProvider<E, M, TreeViewer>
		implements ITreeContentProvider {
	
	
	protected final ElementMatchComparator<E, M> comparator;
	
	/** current, filtered elements */
	private final SortedArraySet<E> currentElements;
	
	
	public TextSearchResultTreeContentProvider(final ExtTextSearchResultPage<E, M> page,
			final TreeViewer viewer) {
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
				this.currentElements.addAllE(0, elements, 0, elements.length);
				
				this.active= true;
				return elements;
			}
			
			for (int i= 0; i < elements.length && this.currentElements.size() < limit; i++) {
				final E element= elements[i];
				if (result.hasPickedMatches(element)) {
					this.currentElements.addE(this.currentElements.size(), element);
				}
			}
			this.active= true;
		}
		return this.currentElements.toArray();
	}
	
	@Override
	public Object getParent(final Object element) {
		if (element instanceof Match) {
			return ((Match) element).getElement();
		}
		if (element instanceof IMatchGroup<?>) {
			return ((IMatchGroup<?>) element).getElement();
		}
		return null;
	}
	
	@Override
	public boolean hasChildren(final Object element) {
		return (!(element instanceof Match || element instanceof LineElement<?>));
	}
	
	@Override
	public Object[] getChildren(final Object parentElement) {
		final ExtTextSearchResult<E, M> result= getInput();
		
		final M[] matches= result.getPickedMatches(parentElement);
		return getShownMatches(matches);
	}
	
	public Object[] getShownMatches(final M[] matches) {
		return matches;
	}
	
	
	@Override
	public void elementsChanged(final Object[] elements) {
		if (!this.active) {
			return;
		}
		final ExtTextSearchResult<E, M> result= getInput();
		final int limit= getElementLimit();
		
		final TreeViewer viewer= getViewer();
		viewer.getTree().setRedraw(false);
		try {
			for (int i= 0; i < elements.length; i++) {
				final E element= (E) elements[i];
				if (result.hasPickedMatches(element)) {
					if (this.currentElements.size() < limit) {
						final int currentIdx= this.currentElements.addE(element);
						if (currentIdx >= 0) {
							addElement(viewer, element, currentIdx);
						}
						else {
							viewer.refresh(element, true);
						}
					}
					else {
						final int currentIdx= this.currentElements.indexOfE(element);
						if (currentIdx >= 0) {
							viewer.refresh(element, true);
						}
					}
				}
				else {
					final int currentIdx= this.currentElements.removeE(element);
					if (currentIdx >= 0) {
						removeElement(viewer, element, currentIdx);
					}
				}
			}
		}
		finally {
			viewer.getTree().setRedraw(true);
		}
	}
	
	protected void addElement(final TreeViewer viewer, final E element, final int idx) {
		viewer.insert(TreePath.EMPTY, element, idx);
	}
	
	protected void removeElement(final TreeViewer viewer, final E element, final int idx) {
		viewer.remove(TreePath.EMPTY, idx);
	}
	
	@Override
	public void clear() {
		reset();
		super.clear();
	}
	
}
