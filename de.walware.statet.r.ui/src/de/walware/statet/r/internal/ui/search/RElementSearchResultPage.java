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

package de.walware.statet.r.internal.ui.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;

import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.workbench.search.ui.ExtTextSearchResultPage;
import de.walware.ecommons.workbench.search.ui.LineElement;
import de.walware.ecommons.workbench.search.ui.TextSearchResultContentProvider;
import de.walware.ecommons.workbench.ui.DecoratingStyledLabelProvider;

import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.ui.RLabelProvider;


public class RElementSearchResultPage extends ExtTextSearchResultPage<IRSourceUnit, RElementMatch> {
	
	
	private static class LTKDecoratingLabelProvider extends DecoratingStyledLabelProvider {
		
		public LTKDecoratingLabelProvider(final IStyledLabelProvider provider) {
			super(provider);
		}
		
		
		@Override
		protected Object getElementToDecorate(final Object element) {
			if (element instanceof ISourceUnit) {
				return ((ISourceUnit) element).getResource();
			}
			return element;
		}
		
	}
	
	
	public RElementSearchResultPage() {
		super(RElementSearchResult.COMPARATOR);
	}
	
	
	@Override
	protected TextSearchResultContentProvider<IRSourceUnit, RElementMatch, TreeViewer> createTreeContentProvider(final TreeViewer viewer) {
		return new RElementSearchResultTreeContentProvider(this, viewer);
	}
	
	@Override
	protected void configureTableViewer(final TableViewer viewer) {
		super.configureTableViewer(viewer);
		
		viewer.setLabelProvider(new LTKDecoratingLabelProvider(
				new RElementSearchLabelProvider(this, RLabelProvider.RESOURCE_PATH) ));
	}
	
	@Override
	protected void configureTreeViewer(final TreeViewer viewer) {
		super.configureTreeViewer(viewer);
		
		viewer.setLabelProvider(new LTKDecoratingLabelProvider(
				new RElementSearchLabelProvider(this, RLabelProvider.RESOURCE_PATH) ));
	}
	
	
	@Override
	public RElementMatch[] getDisplayedMatches(final Object element) {
		if (element instanceof LineElement<?>) {
			final List<RElementMatch> matches= getDisplayedMatches((LineElement<?>) element);
			return matches.toArray(new RElementMatch[matches.size()]);
		}
		return super.getDisplayedMatches(element);
	}
	
	public List<RElementMatch> getDisplayedMatches(final LineElement<?> group) {
		final RElementMatch[] allMatches= getInput().getPickedMatches(group.getElement());
		final List<RElementMatch> groupMatches= new ArrayList<>();
		for (final RElementMatch match : allMatches) {
			if (match.getMatchGroup() == group) {
				groupMatches.add(match);
			}
		}
		return groupMatches;
	}
	
	@Override
	public int getDisplayedMatchCount(final Object element) {
		if (element instanceof LineElement<?>) {
			return getDisplayedMatchCount((LineElement<?>) element);
		}
		return super.getDisplayedMatchCount(element);
	}
	
	public int getDisplayedMatchCount(final LineElement<?> group) {
		final RElementMatch[] allMatches= getInput().getPickedMatches(group.getElement());
		int count= 0;
		for (final RElementMatch match : allMatches) {
			if (match.getMatchGroup() == group) {
				count++;
			}
		}
		return count++;
	}
	
}
