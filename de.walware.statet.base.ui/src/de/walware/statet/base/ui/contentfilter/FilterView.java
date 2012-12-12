/*******************************************************************************
 * Copyright (c) 2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui.contentfilter;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;


public class FilterView extends PageBookView {
	
	
	public static final String VIEW_ID = "de.walware.statet.base.views.ContentFilter"; //$NON-NLS-1$
	
	
	public FilterView() {
		super();
	}
	
	
	@Override
	protected IPage createDefaultPage(final PageBook book) {
		final MessagePage page = new MessagePage();
		initPage(page);
		page.createControl(book);
		page.setMessage("No data source available");
		
		initPage(page);
		page.createControl(book);
		
		return page;
	}
	
	@Override
	protected PageRec doCreatePage(final IWorkbenchPart part) {
		final IFilterPage page = (IFilterPage) part.getAdapter(IFilterPage.class);
		if (page != null) {
			initPage(page);
			page.createControl(getPageBook());
			return new PageRec(part, page);
		}
		return null;
	}
	
	@Override
	protected void doDestroyPage(final IWorkbenchPart part, final PageRec rec) {
		final IFilterPage page = (IFilterPage) rec.page;
		page.dispose();
		rec.dispose();
	}
	
	@Override
	public Object getAdapter(final Class required) {
		if (required == IContributedContentsView.class) {
			return new IContributedContentsView() {
				@Override
				public IWorkbenchPart getContributingPart() {
					return getContributingEditor();
				}
			};
		}
		return super.getAdapter(required);
	}
	
	@Override
	protected IWorkbenchPart getBootstrapPart() {
		final IWorkbenchPage page = getSite().getPage();
		if (page != null) {
			return page.getActiveEditor();
		}
		return null;
	}
	
	private IWorkbenchPart getContributingEditor() {
		return getCurrentContributingPart();
	}
	
	@Override
	protected boolean isImportant(final IWorkbenchPart part) {
		return (part instanceof IEditorPart);
	}
	
	@Override
	public void partBroughtToTop(final IWorkbenchPart part) {
		partActivated(part);
	}
	
}
