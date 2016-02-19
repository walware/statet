/*=============================================================================#
 # Copyright (c) 2013-2016 Stephan Wahlbrink (WalWare.de) and others.
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchEvent;
import org.eclipse.search.ui.text.RemoveAllEvent;

import de.walware.jcommons.collections.SortedArraySet;
import de.walware.jcommons.collections.SortedListSet;

import de.walware.ecommons.FastList;


/**
 * 
 * 
 * @param <E> element type
 * @param <M> match type
 */
public abstract class ExtTextSearchResult<E, M extends Match> extends AbstractTextSearchResult {
	
	
	private static class ChangeEvent extends MatchEvent {
		
		private static final long serialVersionUID= -5266244762347509979L;
		
		public ChangeEvent(final ISearchResult searchResult) {
			super(searchResult);
		}
		
		@Override
		protected void setKind(final int kind) {
			super.setKind(kind);
		}
		
		@Override
		protected void setMatch(final Match match) {
			super.setMatch(match);
		}
		
		@Override
		protected void setMatches(final Match[] matches) {
			super.setMatches(matches);
		}
		
	}
	
	protected static class DefaultMatchComparator<M extends Match> implements Comparator<M> {
		
		public DefaultMatchComparator() {
		}
		
		@Override
		public int compare(final M o1, final M o2) {
			final int d= o1.getOffset() - o2.getOffset();
			if (d != 0) {
				return d;
			}
			return o1.getLength() - o2.getLength();
		}
		
	}
	
	
	/* Locking:
	 *   - All write changes => events are thrown in correct order:
	 *         synchronized (this)
	 *   - All access to toplevel collections {@link #elementMatches} and {@link #elementList}:
	 *         synchronized (this.elementMatches)
	 *   - All access to match lists, values of {@link #elementMatches}:
	 *         synchronized (matches)
	 */
	
	private final ElementMatchComparator<E, M> comparator;
	
	private final SortedListSet<E> elementList;
	private final Map<E, SortedListSet<M>> elementMatches;
	
	private final FastList<ISearchResultListener> listeners= new FastList<>(ISearchResultListener.class, FastList.IDENTITY);
	private final ChangeEvent changeEvent= new ChangeEvent(this);
	
	
	public ExtTextSearchResult(final ElementMatchComparator<E, M> comparator) {
		this.comparator= comparator;
		
		this.elementList= new SortedArraySet<>(comparator.getElement0(), comparator.getElementComparator());
		this.elementMatches= new HashMap<>();
	}
	
	
	public ElementMatchComparator<E, M> getComparator() {
		return this.comparator;
	}
	
	@Override
	public String getTooltip() {
		return getLabel();
	}
	
	@Override
	public IEditorMatchAdapter getEditorMatchAdapter() {
		return null;
	}
	
	@Override
	public IFileMatchAdapter getFileMatchAdapter() {
		return null;
	}
	
	
	@Override
	public void addListener(final ISearchResultListener l) {
		this.listeners.add(l);
	}
	
	@Override
	public void removeListener(final ISearchResultListener l) {
		this.listeners.remove(l);
	}
	
	protected ChangeEvent getChangeEvent(final int eventKind, final Match match) {
		this.changeEvent.setKind(eventKind);
		this.changeEvent.setMatch(match);
		return this.changeEvent;
	}
	
	protected ChangeEvent getChangeEvent(final int eventKind, final List<Match> matches) {
		this.changeEvent.setKind(eventKind);
		this.changeEvent.setMatches(matches.toArray(new Match[matches.size()]));
		return this.changeEvent;
	}
	
	@Override
	protected void fireChange(final SearchResultEvent e) {
		final ISearchResultListener[] listeners= this.listeners.toArray();
		for (final ISearchResultListener listener : listeners) {
			listener.searchResultChanged(e);
		}
	}
	
	
	public int getElementCount() {
		synchronized (this.elementMatches) {
			return this.elementList.size();
		}
	}
	
	@Override
	public E[] getElements() {
		synchronized (this.elementMatches) {
			return this.elementList.toArray(this.comparator.getElement0());
		}
	}
	
	
	@Override
	public synchronized void addMatch(final Match match) {
		final boolean done;
		synchronized (this.elementMatches) {
			done= doAddMatch((M) match);
		}
		if (done) {
			fireChange(getChangeEvent(MatchEvent.ADDED, match));
		}
	}
	
	@Override
	public synchronized void addMatches(final Match[] matches) {
		final List<Match> added= new ArrayList<>(matches.length);
		synchronized (this.elementMatches) {
			for (int i= 0; i < matches.length; i++) {
				if (doAddMatch((M) matches[i])) {
					added.add(matches[i]);
				}
			}
		}
		if (!added.isEmpty()) {
			fireChange(getChangeEvent(MatchEvent.ADDED, added));
		}
	}
	
	protected boolean doAddMatch(final M match) {
		final E element= (E) match.getElement();
		if (element == null) {
			return false;
		}
		SortedListSet<M> matches= this.elementMatches.get(element);
		if (matches == null) {
			if (this.elementList.addE(element) < 0) {
				return false;
			}
			matches= new SortedArraySet<>(this.comparator.getMatch0(), this.comparator.getMatchComparator());
			this.elementMatches.put(element, matches);
		}
		synchronized (matches) {
			if (matches.addE(match) >= 0) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public synchronized void removeMatch(final Match match) {
		final boolean done;
		synchronized (this.elementMatches) {
			done= doRemoveMatch((M) match);
		}
		if (done) {
			fireChange(getChangeEvent(MatchEvent.REMOVED, match));
		}
	}
	
	@Override
	public synchronized void removeMatches(final Match[] matches) {
		final List<Match> removed= new ArrayList<>(matches.length);
		synchronized (this.elementMatches) {
			for (int i= 0; i < matches.length; i++) {
				if (doRemoveMatch((M) matches[i])) {
					removed.add(matches[i]);
				}
			}
		}
		if (!removed.isEmpty()) {
			fireChange(getChangeEvent(MatchEvent.REMOVED, removed));
		}
	}
	
	protected boolean doRemoveMatch(final M match) {
		final E element= (E) match.getElement();
		if (element == null) {
			return false;
		}
		final SortedListSet<M> matches= this.elementMatches.get(element);
		if (matches == null) {
			return false;
		}
		synchronized (matches) {
			if (matches.removeE(match) < 0) {
				return false;
			}
		}
		if (matches.isEmpty()) {
			this.elementList.remove(element);
			this.elementMatches.remove(element);
		}
		return true;
	}
	
	@Override
	public synchronized void removeAll() {
		synchronized (this.elementMatches) {
			doRemoveAll();
		}
		fireChange(new RemoveAllEvent(this));
	}
	
	protected void doRemoveAll() {
		this.elementList.clear();
		this.elementMatches.clear();
	}
	
	@Override
	public int getMatchCount() {
		synchronized (this.elementMatches) {
			int count= 0;
			for (final List<M> matches : this.elementMatches.values()) {
				count+= matches.size();
			}
			return count;
		}
	}
	
	public boolean hasMatches(final Object element) {
		synchronized (this.elementMatches) {
			return this.elementMatches.containsKey(element);
		}
	}
	
	public boolean hasPickedMatches(final Object element) {
		final SortedListSet<M> matches;
		synchronized (this.elementMatches) {
			matches= this.elementMatches.get(element);
		}
		if (matches != null) {
			if (getActiveMatchFilters() == null) {
				return true;
			}
			else {
				synchronized (matches) {
					for (final M match : matches) {
						if (!match.isFiltered()) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public int getMatchCount(final Object element) {
		synchronized (this.elementMatches) {
			final SortedListSet<M> matches= this.elementMatches.get(element);
			if (matches != null) {
				return matches.size();
			}
			return 0;
		}
	}
	
	public int getPickedMatchCount(final Object element) {
		final SortedListSet<M> matches;
		synchronized (this.elementMatches) {
			matches= this.elementMatches.get(element);
		}
		if (matches != null) {
			if (getActiveMatchFilters() == null) {
				synchronized (matches) {
					return matches.size();
				}
			}
			else {
				synchronized (matches) {
					int count= 0;
					for (final M match : matches) {
						if (!match.isFiltered()) {
							count++;
						}
					}
					return count;
				}
			}
		}
		return 0;
	}
	
	@Override
	public M[] getMatches(final Object element) {
		synchronized (this.elementMatches) {
			final SortedListSet<M> matches= this.elementMatches.get(element);
			if (matches != null) {
				return matches.toArray(this.comparator.getMatch0());
			}
			return this.comparator.getMatch0();
		}
	}
	
	public M[] getPickedMatches(final Object element) {
		final SortedListSet<M> matches;
		synchronized (this.elementMatches) {
			matches= this.elementMatches.get(element);
		}
		if (matches != null) {
			if (getActiveMatchFilters() == null) {
				synchronized (matches) {
					return matches.toArray(this.comparator.getMatch0());
				}
			}
			else {
				synchronized (matches) {
					final List<M> filtered= new ArrayList<>(matches.size());
					for (final M match : matches) {
						if (!match.isFiltered()) {
							filtered.add(match);
						}
					}
					return filtered.toArray(this.comparator.getMatch0());
				}
			}
		}
		return this.comparator.getMatch0();
	}
	
}
