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

import de.walware.ecommons.collections.CategoryElementList;
import de.walware.ecommons.collections.SortedListSet;


public class TextSearchResultMatchTableContentProvider<E, M extends Match>
		extends TextSearchResultContentProvider<E, M, TableViewer> {
	
	
	protected final ElementMatchComparator<E, M> comparator;
	
	private final CategoryElementList<E, M> currentMatches;
	
	
	public TextSearchResultMatchTableContentProvider(final ExtTextSearchResultPage<E, M> page,
			final TableViewer viewer) {
		super(page, viewer);
		
		this.comparator= page.comparator;
		this.currentMatches= new CategoryElementList<E, M>(this.comparator.getMatch0(), this.comparator);
	}
	
	
	@Override
	protected void reset() {
		super.reset();
		this.currentMatches.clear();
	}
	
	@Override
	public Object[] getElements(final Object inputElement) {
		if (!this.active) {
			final ExtTextSearchResult<E, M> result= getInput();
			assert (result == inputElement);
			if (result == null) {
				return NO_ELEMENTS;
			}
			assert (this.currentMatches.isEmpty());
			
			int limit= getElementLimit();
			
			final E[] elements= result.getElements();
			for (int i= 0; i < elements.length && limit > 0; i++) {
				final E element= elements[i];
				final M[] matches= result.getPickedMatches(element);
				this.currentMatches.addAllE(this.currentMatches.size(), matches, 0,
						Math.min(matches.length, limit) );
				limit-= matches.length;
			}
			this.active= true;
		}
		return this.currentMatches.toArray();
	}
	
	@Override
	public void elementsChanged(final Object[] elements) {
		if (!this.active) {
			return;
		}
		final ExtTextSearchResult<E, M> result= getInput();
		final int limit= getElementLimit();
		
		final TableViewer viewer= getViewer();
		
		final List<M> toAdd= new ArrayList<M>();
		final List<M> toUpdate= new ArrayList<M>();
		final List<M> toRemove= new ArrayList<M>();
		for (int i= 0; i < elements.length; i++) {
			final E element= (E) elements[i];
			final M[] matches= result.getPickedMatches(element);
			final SortedListSet<M> currentElementMatches= this.currentMatches.subList(element);
			for (int j= 0; j < matches.length; j++) {
				final M match= matches[j];
				int k;
				if ((this.currentMatches.size() < limit)) {
					k= currentElementMatches.addE(match);
					if (k >= 0) {
						toAdd.add(match);
					}
					else {
						toUpdate.add(match);
						k= -(k + 1);
					}
				}
				else {
					k= currentElementMatches.indexOf(match);
					if (k >= 0) {
						toUpdate.add(match);
					}
					else {
						k= -(k + 1);
					}
				}
				while (k > j) {
					toRemove.add(currentElementMatches.remove(j));
					k--;
				}
			}
			while (currentElementMatches.size() > matches.length) {
				currentElementMatches.remove(matches.length);
			}
		}
		
		viewer.getTable().setRedraw(false);
		try {
			viewer.remove(toRemove.toArray());
			viewer.refresh(toUpdate.toArray(), true);
			viewer.add(toAdd.toArray());
		}
		finally {
			viewer.getTable().setRedraw(true);
		}
	}
	
}
