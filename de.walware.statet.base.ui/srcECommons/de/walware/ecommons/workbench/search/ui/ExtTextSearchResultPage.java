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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


public abstract class ExtTextSearchResultPage<E, M extends Match> extends AbstractTextSearchViewPage {
	
	
	protected final ElementMatchComparator<E, M> comparator;
	
	private TextSearchResultContentProvider<E, M, ?> contentProvider;
	
	
	public ExtTextSearchResultPage(final ElementMatchComparator<E, M> comparator) {
		super();
		
		this.comparator= comparator;
	}
	
	
	@Override
	public ExtTextSearchResult<E, M> getInput() {
		return (ExtTextSearchResult<E, M>) super.getInput();
	}
	
	
	@Override
	protected void configureTableViewer(final TableViewer viewer) {
		viewer.setUseHashlookup(true);
		
		this.contentProvider= createTableContentProvider(viewer);
		viewer.setContentProvider(this.contentProvider);
	}
	
	@Override
	protected void configureTreeViewer(final TreeViewer viewer) {
		viewer.setUseHashlookup(true);
		
		this.contentProvider= createTreeContentProvider(viewer);
		viewer.setContentProvider(this.contentProvider);
	}
	
	@Override
	protected TableViewer createTableViewer(final Composite parent) {
		return new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
	}
	
	protected TextSearchResultContentProvider<E, M, TableViewer> createTableContentProvider(final TableViewer viewer) {
		return new TextSearchResultTableContentProvider<>(this, viewer);
	}
	
	protected abstract TextSearchResultContentProvider<E, M, TreeViewer> createTreeContentProvider(TreeViewer viewer);
	
	
	protected String getSearchLabel() {
		return getInput().getQuery().getLabel();
	}
	
	@Override
	public int getDisplayedMatchCount(final Object element) {
		final ExtTextSearchResult<E, M> result= getInput();
		if (result == null) {
			return 0;
		}
		if (element instanceof Match) { // only for R help?
			return 1;
		}
		return result.getPickedMatchCount(element);
	}
	
	@Override
	public M[] getDisplayedMatches(final Object element) {
		final ExtTextSearchResult<E, M> result= getInput();
		if (result == null) {
			return (M[]) new Match[0];
		}
		if (element instanceof Match) { // only for R help?
			return (M[]) new Match[] { (Match) element };
		}
		return result.getPickedMatches(element);
	}
	
	@Override
	protected void elementsChanged(final Object[] elements) {
		try {
			if (this.contentProvider != null) {
				this.contentProvider.elementsChanged(elements);
			}
		}
		catch (final Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, StatetUIPlugin.PLUGIN_ID, -1,
					NLS.bind("An error occurred when updating the result table of search: {0}.",
							getSearchLabel() ), e ), StatusManager.LOG | StatusManager.SHOW);
		}
	}
	
	@Override
	protected void clear() {
		try {
			if (this.contentProvider != null) {
				this.contentProvider.clear();
			}
		}
		catch (final Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, StatetUIPlugin.PLUGIN_ID, -1,
					NLS.bind("An error occurred when updating the result table of search: {0}.",
							getSearchLabel() ), e ), StatusManager.LOG | StatusManager.SHOW);
		}
	}
	
	
	@Override
	protected void showMatch(final Match match, final int currentOffset, final int currentLength,
			final boolean activate) throws PartInitException {
		final ExtTextSearchResult<E,M> input= getInput();
		if (input == null) {
			return;
		}
		final IFileMatchAdapter fileAdapter= input.getFileMatchAdapter();
		if (fileAdapter == null) {
			return;
		}
		final IFile file= fileAdapter.getFile(match.getElement());
		if (file == null) {
			return;
		}
		final IWorkbenchPage page= getSite().getPage();
		if (currentOffset >= 0 && currentLength != 0) {
			openAndSelect(page, file, currentOffset, currentLength, activate);
		}
		else {
			open(page, file, activate);
		}
	}
	
}
