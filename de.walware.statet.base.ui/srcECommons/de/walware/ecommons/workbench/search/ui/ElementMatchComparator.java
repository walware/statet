/*=============================================================================#
 # Copyright (c) 2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.workbench.search.ui;

import java.util.Comparator;

import org.eclipse.search.ui.text.Match;

import de.walware.ecommons.collections.CategoryElementComparator;


public final class ElementMatchComparator<E, M extends Match> extends CategoryElementComparator<E, M> {
	
	
	private final E[] elements0;
	private final Comparator<? super E> elementComparator;
	private final M[] matches0;
	private final Comparator<? super M> matchComparator;
	
	
	public ElementMatchComparator(
			final E[] elements0, final Comparator<? super E> elementComparator,
			final M[] matches0, final Comparator<? super M> matchComparator) {
		if (elements0 == null) {
			throw new NullPointerException("elements0"); //$NON-NLS-1$
		}
		if (elementComparator == null && !Comparable.class.isAssignableFrom(elements0.getClass().getComponentType())) {
			throw new NullPointerException("elementComparator"); //$NON-NLS-1$
		}
		if (matches0 == null) {
			throw new NullPointerException("matches0"); //$NON-NLS-1$
		}
		if (matchComparator == null && !Comparable.class.isAssignableFrom(matches0.getClass().getComponentType())) {
			throw new NullPointerException("matchComparator"); //$NON-NLS-1$
		}
		
		this.elements0= elements0;
		this.elementComparator= elementComparator;
		this.matches0= matches0;
		this.matchComparator= matchComparator;
	}
	
	
	public E[] getElement0() {
		return this.elements0;
	}
	
	public Comparator<? super E> getElementComparator() {
		return this.elementComparator;
	}
	
	public M[] getMatch0() {
		return this.matches0;
	}
	
	public Comparator<? super M> getMatchComparator() {
		return this.matchComparator;
	}
	
	
	@Override
	public E getCategory(final M element) {
		return (E) element.getElement();
	}
	
	@Override
	public int compareElement(final M element1, final M element2) {
		return (this.matchComparator != null) ?
				this.matchComparator.compare(element1, element2) :
				((Comparable<? super M>) element1).compareTo(element2);
	}
	
	@Override
	public int compareCategory(final E category1, final E category2) {
		return (this.elementComparator != null) ?
				this.elementComparator.compare(category1, category2) :
				((Comparable<? super E>) category1).compareTo(category2);
	}
	
}
