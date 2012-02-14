/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.rhelp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchEvent;
import org.eclipse.search.ui.text.RemoveAllEvent;

import de.walware.ecommons.FastList;

import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRPackageHelp;
import de.walware.statet.r.ui.RUI;


public class RHelpSearchUIResult extends AbstractTextSearchResult {
	
	
	private static final RHelpSearchUIMatch[] NO_MATCHES = new RHelpSearchUIMatch[0];
	
	
	private static class ChangeEvent extends MatchEvent {
		
		private static final long serialVersionUID = -5266244762347509979L;
		
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
	
	
	private final RHelpSearchUIQuery fQuery;
	
	private IREnv fREnv;
	private final List<IRPackageHelp> fPkgs = new ArrayList<IRPackageHelp>();
	private final Map<IRPackageHelp, List<RHelpSearchUIMatch>> fPkgToMatches = new HashMap<IRPackageHelp, List<RHelpSearchUIMatch>>();
	
	private final FastList<ISearchResultListener> fListeners = new FastList<ISearchResultListener>(ISearchResultListener.class, FastList.IDENTITY);
	private final ChangeEvent fChangeEvent = new ChangeEvent(this);
	
	
	public RHelpSearchUIResult(final RHelpSearchUIQuery query) {
		fQuery = query;
	}
	
	
	@Override
	public ImageDescriptor getImageDescriptor() {
		return RUI.getImageDescriptor(RUI.IMG_OBJ_R_HELP_SEARCH);
	}
	
	@Override
	public String getLabel() {
		final String queryLabel = fQuery.getLongLabel();
		final String matchLabel;
		final Object[] data = new Object[3];
		if ((data[2] = fREnv.getName()) == null) {
			data[2] = "-"; //$NON-NLS-1$
		}
		int count = 0;
		synchronized (fPkgToMatches) {
			data[1] = fPkgToMatches.size();
			for (final List<RHelpSearchUIMatch> matches : fPkgToMatches.values()) {
				count += matches.size();
			};
			data[0] = count;
		}
		if (count == 1) {
			matchLabel = NLS.bind(Messages.Search_SingleMatch_label, data[2]);
		}
		else {
			matchLabel = NLS.bind(Messages.Search_MultipleMatches_label, data);
		}
		return queryLabel + " – " + matchLabel; //$NON-NLS-1$
	}
	
	@Override
	public String getTooltip() {
		return getLabel();
	}
	
	@Override
	public RHelpSearchUIQuery getQuery() {
		return fQuery;
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
		fListeners.add(l);
	}
	
	@Override
	public void removeListener(final ISearchResultListener l) {
		fListeners.remove(l);
	}
	
	@Override
	protected void fireChange(final SearchResultEvent e) {
		final ISearchResultListener[] listeners = fListeners.toArray();
		for (final ISearchResultListener listener : listeners) {
			listener.searchResultChanged(e);
		}
	}
	
	
	public void init(final IREnv renv) {
		fREnv = renv;
		removeAll();
	}
	
	@Override
	public void removeAll() {
		synchronized (fPkgToMatches) {
			fPkgs.clear();
			fPkgToMatches.clear();
		}
		fireChange(new RemoveAllEvent(this));
	}
	
	@Override
	public void addMatch(final Match match) {
		addMatch((RHelpSearchUIMatch) match);
	}
	
	public void addMatch(final RHelpSearchUIMatch match) {
		final IRPackageHelp pkg = match.getRHelpMatch().getPage().getPackage();
		synchronized (fPkgToMatches) {
			List<RHelpSearchUIMatch> matches = fPkgToMatches.get(pkg);
			if (matches == null) {
				matches = new ArrayList<RHelpSearchUIMatch>();
				fPkgs.add(-Collections.binarySearch(fPkgs, pkg)-1, pkg);
				fPkgToMatches.put(pkg, matches);
			}
			final int idx = Collections.binarySearch(matches, match);
			if (idx < 0) {
				matches.add(-idx-1, match);
			}
		}
		fireChange(getChangeEvent(MatchEvent.ADDED, match));
	}
	
	@Override
	public void removeMatch(final Match match) {
		removeMatch((RHelpSearchUIMatch) match);
	}
	
	public void removeMatch(final RHelpSearchUIMatch match) {
		final IRHelpPage page = match.getRHelpMatch().getPage();
		final IRPackageHelp pkg = page.getPackage();
		
		Match removed = null;
		synchronized (fPkgToMatches) {
			final List<RHelpSearchUIMatch> matches = fPkgToMatches.get(pkg);
			if (matches != null && matches.remove(page)) {
				removed = match;
				if (matches.isEmpty()) {
					fPkgs.remove(pkg);
					fPkgToMatches.remove(pkg);
				}
			}
		}
		if (removed != null) {
			fireChange(getChangeEvent(MatchEvent.REMOVED, removed));
		}
	}
	
	@Override
	public void removeMatches(final Match[] matchesToRemove) {
		final List<Match> removed = new ArrayList<Match>(matchesToRemove.length);
		synchronized (fPkgToMatches) {
			for (int i = 0; i < matchesToRemove.length; i++) {
				final RHelpSearchUIMatch match = (RHelpSearchUIMatch) matchesToRemove[i];
				final IRHelpPage page = match.getRHelpMatch().getPage();
				final IRPackageHelp pkg = page.getPackage();
				final List<RHelpSearchUIMatch> matches = fPkgToMatches.get(pkg);
				if (matches != null && matches.remove(match)) {
					removed.add(match);
					if (matches.isEmpty()) {
						fPkgs.remove(pkg);
						fPkgToMatches.remove(pkg);
					}
				}
			}
		}
		if (!removed.isEmpty()) {
			fireChange(getChangeEvent(MatchEvent.REMOVED, removed));
		}
	}
	
	public IRPackageHelp[] getPackages() {
		synchronized (fPkgToMatches) {
			return fPkgs.toArray(new IRPackageHelp[fPkgs.size()]);
		}
	}
	
//	public IRHelpPage[] getPages() {
//		final List<IRHelpPage> allPages = new ArrayList<IRHelpPage>();
//		synchronized (fPkgToMatches) {
//			for (final List<RHelpSearchUIMatch> matches : fPkgToMatches.values()) {
//				allPages.addAll(matches);
//			}
//		}
//		final IRHelpPage[] array = allPages.toArray(new IRHelpPage[allPages.size()]);
//		Arrays.sort(array);
//		return array;
//	}
	
	public RHelpSearchUIMatch[] getMatches() {
		final ArrayList<RHelpSearchUIMatch> allMatches = new ArrayList<RHelpSearchUIMatch>();
		synchronized (fPkgToMatches) {
			for (final List<RHelpSearchUIMatch> matches : fPkgToMatches.values()) {
				allMatches.addAll(matches);
			}
		}
		final RHelpSearchUIMatch[] array = allMatches.toArray(new RHelpSearchUIMatch[allMatches.size()]);
		Arrays.sort(array);
		return array;
	}
	
	public boolean hasMatches(final IRPackageHelp pkg) {
		synchronized (fPkgToMatches) {
			return fPkgToMatches.containsKey(pkg);
		}
	}
	
	@Override
	public RHelpSearchUIMatch[] getMatches(final Object element) {
		if (element instanceof IRPackageHelp) {
			return getMatches((IRPackageHelp) element);
		}
		if (element instanceof RHelpSearchUIMatch[]) {
			return (RHelpSearchUIMatch[]) element;
		}
		if (element instanceof RHelpSearchUIMatch) {
			return new RHelpSearchUIMatch[] { (RHelpSearchUIMatch) element };
		}
		return NO_MATCHES;
	}
	
	public RHelpSearchUIMatch[] getMatches(final IRPackageHelp pkg) {
		synchronized (fPkgToMatches) {
			final List<RHelpSearchUIMatch> matches = fPkgToMatches.get(pkg);
			return (matches != null) ? matches.toArray(new RHelpSearchUIMatch[matches.size()]) : NO_MATCHES;
		}
	}
	
	@Override
	public int getMatchCount() {
		int count = 0;
		synchronized (fPkgToMatches) {
			for (final List<RHelpSearchUIMatch> matches : fPkgToMatches.values()) {
				count += matches.size();
			};
		}
		return count;
	}
	
	@Override
	public int getMatchCount(final Object element) {
		if (element instanceof IRPackageHelp) {
			synchronized (fPkgToMatches) {
				final List<RHelpSearchUIMatch> matches = fPkgToMatches.get(element);
				return (matches != null) ? matches.size() : 0;
			}
		}
		if (element instanceof RHelpSearchUIMatch[]) {
			return ((RHelpSearchUIMatch[]) element).length;
		}
		if (element instanceof RHelpSearchUIMatch) {
			return 1;
		}
		return 0;
	}
	
	protected ChangeEvent getChangeEvent(final int eventKind, final Match match) {
		fChangeEvent.setKind(eventKind);
		fChangeEvent.setMatch(match);
		return fChangeEvent;
	}
	
	protected ChangeEvent getChangeEvent(final int eventKind, final List<Match> matches) {
		fChangeEvent.setKind(eventKind);
		fChangeEvent.setMatches(matches.toArray(new Match[matches.size()]));
		return fChangeEvent;
	}
	
}
