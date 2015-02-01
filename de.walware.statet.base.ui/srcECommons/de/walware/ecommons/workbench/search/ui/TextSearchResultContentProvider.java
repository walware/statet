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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.ui.text.Match;


public abstract class TextSearchResultContentProvider<E, M extends Match, V extends StructuredViewer>
		implements IStructuredContentProvider {
	
	
	protected static final Object[] NO_ELEMENTS= new Object[0];
	
	
	private final ExtTextSearchResultPage page;
	
	private ExtTextSearchResult<E, M> input;
	private final V viewer;
	
	protected boolean active;
	
	
	public TextSearchResultContentProvider(final ExtTextSearchResultPage page, final V viewer) {
		this.page= page;
		this.viewer= viewer;
	}
	
	
	@Override
	public void dispose() {
	}
	
	
	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		assert (this.viewer == viewer);
		this.input= (ExtTextSearchResult<E, M>) newInput;
		reset();
	}
	
	protected void reset() {
		this.active= false;
	}
	
	
	protected ExtTextSearchResultPage getPage() {
		return this.page;
	}
	
	protected V getViewer() {
		return this.viewer;
	}
	
	protected ExtTextSearchResult<E, M> getInput() {
		return this.input;
	}
	
	protected int getElementLimit() {
		final Integer limit= getPage().getElementLimit();
		if (limit == null || limit.intValue() < 0) {
			return Integer.MAX_VALUE;
		}
		return limit;
	}
	
	
	public abstract void elementsChanged(final Object[] elements);
	
	public void clear() {
		this.viewer.refresh();
	}
	
}
