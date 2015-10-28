/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.rhelp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.actions.SimpleContributionItem;
import de.walware.ecommons.ui.mpbv.BrowserSession;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.workbench.search.ui.ExtTextSearchResultPage;
import de.walware.ecommons.workbench.search.ui.TextSearchLabelUtil;
import de.walware.ecommons.workbench.search.ui.TextSearchResultContentProvider;
import de.walware.ecommons.workbench.search.ui.TextSearchResultMatchTableContentProvider;
import de.walware.ecommons.workbench.search.ui.TextSearchResultTreeContentProvider;
import de.walware.ecommons.workbench.ui.DecoratingStyledLabelProvider;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rhelp.IRHelpSearchMatch;
import de.walware.statet.r.core.rhelp.IRHelpSearchMatch.MatchFragment;
import de.walware.statet.r.core.rhelp.IRPkgHelp;
import de.walware.statet.r.core.rhelp.RHelpSearchQuery;
import de.walware.statet.r.ui.RUI;


public class RHelpSearchResultPage extends ExtTextSearchResultPage<IRPkgHelp, RHelpSearchMatch> {
	
	
	private static final ViewerComparator ALPHA_SORTER= new ViewerComparator() {
		@Override
		public int compare(final Viewer viewer, final Object e1, final Object e2) {
			return ((RHelpSearchMatch) e1).compareTo((RHelpSearchMatch) e2);
	}
	};
	
	private static final ViewerComparator SCORE_SORTER= new ViewerComparator() {
		@Override
		public int compare(final Viewer viewer, final Object e1, final Object e2) {
			final IRHelpSearchMatch match1= ((RHelpSearchMatch) e1).getRHelpMatch();
			final IRHelpSearchMatch match2= ((RHelpSearchMatch) e2).getRHelpMatch();
			return (int) ((match2.getScore() - match1.getScore()) * 1e8);
		}
	};
	
	private static class MatchLabelProvider extends StyledCellLabelProvider {
		
		
		public MatchLabelProvider() {
		}
		
		
		@Override
		public void update(final ViewerCell cell) {
			final Object element= cell.getElement();
			final StyledString text= new StyledString();
			
			if (element instanceof RHelpSearchMatch) {
				final IRHelpSearchMatch match= ((RHelpSearchMatch) element).getRHelpMatch();
				final MatchFragment[] fragments= match.getBestFragments();
				if (fragments != null && fragments.length > 0) {
					{	final String fieldLabel= fragments[0].getFieldLabel();
						if (fieldLabel != null) {
							text.append(fieldLabel, StyledString.QUALIFIER_STYLER);
							text.append(": ", StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
						}
					}
					if (fragments[0].getField() == RHelpSearchQuery.TOPICS_FIELD) {
						RHelpLabelProvider.append(text, fragments[0]);
						for (int i= 1; i < fragments.length; i++) {
							if (fragments[i].getField() == RHelpSearchQuery.TOPICS_FIELD) {
								text.append(", "); //$NON-NLS-1$
								RHelpLabelProvider.append(text, fragments[i]);
							}
						}
					}
					else {
						RHelpLabelProvider.append(text, fragments[0]);
					}
				}
			}
			cell.setText(text.getString());
			cell.setStyleRanges(text.getStyleRanges());
			
			super.update(cell);
		}
		
		@Override
		protected StyleRange prepareStyleRange(StyleRange styleRange, final boolean applyColors) {
			if (!applyColors && styleRange.background != null) {
				styleRange= super.prepareStyleRange(styleRange, applyColors);
				styleRange.borderStyle= SWT.BORDER_DOT;
				return styleRange;
			}
			return super.prepareStyleRange(styleRange, applyColors);
		}
		
	}
	
	private static Object getRelevantElement(final TreePath treePath) {
		if (treePath.getSegmentCount() > 2 
				&& treePath.getLastSegment() instanceof IRHelpSearchMatch.MatchFragment) {
			return treePath.getSegment(treePath.getSegmentCount()-2);
		}
		return treePath.getLastSegment();
	}
	
	
	private static class TreeContentProvider extends TextSearchResultTreeContentProvider<IRPkgHelp, RHelpSearchMatch> {
		
		
		public TreeContentProvider(final RHelpSearchResultPage page, final TreeViewer viewer) {
			super(page, viewer);
		}
		
		
		@Override
		public boolean hasChildren(final Object element) {
			if (element instanceof IRPkgHelp) {
				return true;
			}
			if (element instanceof RHelpSearchMatch) {
				final MatchFragment[] fragments= ((RHelpSearchMatch) element).getRHelpMatch().getBestFragments();
				return (fragments != null && fragments.length > 0);
			}
			return false;
		}
		
		@Override
		public Object[] getChildren(final Object parentElement) {
			if (parentElement instanceof IRPkgHelp) {
				return super.getChildren(parentElement);
			}
			if (parentElement instanceof RHelpSearchMatch) {
				return ((RHelpSearchMatch) parentElement).getRHelpMatch().getBestFragments();
			}
			return NO_ELEMENTS;
		}
		
	}
	
	private class OpenHandler extends SimpleContributionItem {
		
		public OpenHandler() {
			super("Open (New Page)", "O");
		}
		
		@Override
		protected void execute() throws ExecutionException {
			final StructuredViewer viewer= getViewer();
			if (UIAccess.isOkToUse(viewer)) {
				final IStructuredSelection selection= (IStructuredSelection) viewer.getSelection();
				if (selection instanceof ITreeSelection) {
					final TreePath[] paths= ((ITreeSelection) selection).getPaths();
					for (int i= 0; i < paths.length; i++) {
						open(getRelevantElement(paths[i]), true, true);
					}
				}
				else {
					for (final Iterator<?> iter= selection.iterator(); iter.hasNext(); ) {
						open(iter.next(), true, true);
					}
				}
			}
		}
		
	}
	
	
	private BrowserSession reusedSession;
	private String reusedSessionUrl;
	private IWorkbenchPartReference reusedSessionView;
	
	private ViewerComparator currentSorter= ALPHA_SORTER;
	
	private SimpleContributionItem sortByName;
	private SimpleContributionItem sortByScore;
	
	
	public RHelpSearchResultPage() {
		super(RHelpSearchResult.COMPARATOR);
	}
	
	
	@Override
	public void init(final IPageSite pageSite) {
		super.init(pageSite);
		
		initActions();
	}
	
	private void initActions() {
		this.sortByName= new SimpleContributionItem(
				SharedUIResources.getImages().getDescriptor(SharedUIResources.LOCTOOL_SORT_ALPHA_IMAGE_ID), null,
				"Sort by Name", null, SimpleContributionItem.STYLE_CHECK) {
			@Override
			protected void execute() throws ExecutionException {
				RHelpSearchResultPage.this.currentSorter= ALPHA_SORTER;
				updateSorter();
			}
		};
		this.sortByScore= new SimpleContributionItem(
				SharedUIResources.getImages().getDescriptor(SharedUIResources.LOCTOOL_SORT_SCORE_IMAGE_ID), null,
				"Sort by Relevance", null, SimpleContributionItem.STYLE_CHECK) {
			@Override
			protected void execute() throws ExecutionException {
				RHelpSearchResultPage.this.currentSorter= SCORE_SORTER;
				updateSorter();
			}
		};
	}
	
	@Override
	public void restoreState(final IMemento memento) {
		super.restoreState(memento);
		
		if (memento != null) {
			final String sortValue= memento.getString("sort.by"); //$NON-NLS-1$
			if (sortValue != null && sortValue.equals("score")) { //$NON-NLS-1$
				this.currentSorter= SCORE_SORTER;
			}
			else {
				this.currentSorter= ALPHA_SORTER;
			}
			updateSorter();
		}
	}
	
	@Override
	public void saveState(final IMemento memento) {
		super.saveState(memento);
		
		final String sortValue= (this.currentSorter == SCORE_SORTER) ? "score" : ""; //$NON-NLS-1$ //$NON-NLS-2$
		memento.putString("sort.by", sortValue); //$NON-NLS-1$
	}
	
	@Override
	protected void configureTableViewer(final TableViewer viewer) {
		super.configureTableViewer(viewer);
		
		final TableColumnLayout layout= new TableColumnLayout();
		viewer.getControl().getParent().setLayout(layout);
		viewer.getTable().setHeaderVisible(true);
		
		{	final TableViewerColumn column= new TableViewerColumn(viewer, SWT.LEFT);
			column.getColumn().setText("Page");
			layout.setColumnData(column.getColumn(), new ColumnWeightData(1));
			column.setLabelProvider(new DecoratingStyledLabelProvider(new RHelpLabelProvider(),
					TextSearchLabelUtil.DEFAULT_SEARCH_LABEL_PROPERTIES));
		}
		{	final TableViewerColumn column= new TableViewerColumn(viewer, SWT.LEFT);
			column.getColumn().setText("Package");
			layout.setColumnData(column.getColumn(), new ColumnPixelData(
					new PixelConverter(JFaceResources.getDialogFont()).convertWidthInCharsToPixels(10),
					true, true ));
			column.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(ViewerCell cell) {
					final Object element= cell.getElement();
					String text= ""; //$NON-NLS-1$
					
					if (element instanceof RHelpSearchMatch) {
						final IRHelpSearchMatch match= ((RHelpSearchMatch) element).getRHelpMatch();
						text= match.getPage().getPackage().getName();
					}
					
					cell.setText(text);
				}
			});
		}
		{	final TableViewerColumn column= new TableViewerColumn(viewer, SWT.LEFT);
			column.getColumn().setText("Best Match");
			layout.setColumnData(column.getColumn(), new ColumnWeightData(1));
			column.setLabelProvider(new MatchLabelProvider());
		}
		ColumnViewerToolTipSupport.enableFor(viewer);
		updateSorter();
	}
	
	@Override
	protected void configureTreeViewer(final TreeViewer viewer) {
		super.configureTreeViewer(viewer);
		
		final TreeColumnLayout layout= new TreeColumnLayout();
		viewer.getControl().getParent().setLayout(layout);
		
		final TreeViewerColumn column1= new TreeViewerColumn(viewer, SWT.LEFT);
		column1.getColumn().setText("Package / Page / Match");
		layout.setColumnData(column1.getColumn(), new ColumnWeightData(1));
		column1.setLabelProvider(new RHelpLabelProvider());
		
		ColumnViewerToolTipSupport.enableFor(viewer);
	}
	
	@Override
	protected TextSearchResultTreeContentProvider<IRPkgHelp, RHelpSearchMatch> createTreeContentProvider(
			final TreeViewer viewer) {
		return new TreeContentProvider(this, viewer);
	}
	
	@Override
	protected TextSearchResultContentProvider<IRPkgHelp, RHelpSearchMatch, TableViewer> createTableContentProvider(
			final TableViewer viewer) {
		return new TextSearchResultMatchTableContentProvider<>(this, viewer);
	}
	
	@Override
	protected void fillToolbar(final IToolBarManager tbm) {
		super.fillToolbar(tbm);
		
		if (getLayout() == FLAG_LAYOUT_FLAT) {
			updateSorter();
			tbm.appendToGroup(IContextMenuConstants.GROUP_VIEWER_SETUP, new Separator(".sorting")); //$NON-NLS-1$
			tbm.appendToGroup(".sorting", this.sortByName); //$NON-NLS-1$
			tbm.appendToGroup(".sorting", this.sortByScore); //$NON-NLS-1$
		}
	}
	
	@Override
	protected void fillContextMenu(final IMenuManager mgr) {
		mgr.appendToGroup(IContextMenuConstants.GROUP_OPEN, new OpenHandler());
		super.fillContextMenu(mgr);
	}
	
	private void updateSorter() {
		final ViewerComparator sorter= this.currentSorter;
		this.sortByName.setChecked(sorter == ALPHA_SORTER);
		this.sortByScore.setChecked(sorter == SCORE_SORTER);
		
		if (getLayout() == FLAG_LAYOUT_FLAT) {
			final StructuredViewer viewer= getViewer();
			if (UIAccess.isOkToUse(viewer)) {
				viewer.setComparator(sorter);
			}
		}
	}
	
	@Override
	public RHelpSearchResult getInput() {
		return (RHelpSearchResult) super.getInput();
	}
	
	
	@Override
	protected void handleOpen(final OpenEvent event) {
		final IStructuredSelection selection= (IStructuredSelection) event.getSelection();
		if (selection.size() == 1) {
			Object element= null;
			if (selection instanceof ITreeSelection) {
				element= getRelevantElement(((ITreeSelection) selection).getPaths()[0]);
			}
			if (element == null) {
				element= selection.getFirstElement();
			}
			open(element, true, false);
		}
	}
	
	@Override
	protected void showMatch(final Match match, final int currentOffset, final int currentLength,
			final boolean activate)
			throws PartInitException {
		openPage((RHelpSearchMatch) match, activate, false);
	}
	
	
	protected void open(final Object element, final boolean activate, final boolean newPage) {
		try {
			if (element instanceof RHelpSearchMatch) {
				openPage((RHelpSearchMatch) element, activate, newPage);
			}
			if (element instanceof IRPkgHelp) {
				openPackage((IRPkgHelp) element, activate, newPage);
			}
		}
		catch (final PartInitException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					"An error occurred when trying to open the R help page (element= " + element + ").", e));
		}
	}
	
	protected void openPage(final RHelpSearchMatch match, final boolean activate, final boolean newPage)
			throws PartInitException {
		final IRHelpSearchMatch rMatch= match.getRHelpMatch();
		String url= RCore.getRHelpManager().getPageHttpUrl(rMatch.getPage(),
				RHelpUIServlet.BROWSE_TARGET);
		final RHelpSearchQuery rHelpQuery= getInput().getQuery().getRHelpQuery();
		if (rHelpQuery.getSearchType() == RHelpSearchQuery.DOC_SEARCH
				&& rHelpQuery.getSearchString().length() > 0) {
			try {
				final String value= URLEncoder.encode(rHelpQuery.getSearchString(), "UTF-8"); //$NON-NLS-1$
				url+= "?qs=" + value; //$NON-NLS-1$
			} catch (final UnsupportedEncodingException ignore) {}
		}
		doOpen(url, activate, newPage);
	}
	
	protected void openPackage(final IRPkgHelp packageHelp, final boolean activate, final boolean newPage)
			throws PartInitException {
		final String url= RCore.getRHelpManager().getPackageHttpUrl(packageHelp,
				RHelpUIServlet.BROWSE_TARGET);
		doOpen(url, activate, newPage);
	}
	
	private void doOpen(final String url, final boolean activate, final boolean newPage)
			throws PartInitException {
		RHelpView view= null;
		final IWorkbenchPartReference reference= getSite().getPage().getReference(view);
		if ( !newPage && this.reusedSession != null
				&& !this.reusedSession.getUrl().equals(this.reusedSessionUrl) ) {
			this.reusedSession= null;
		}
		else if ( this.reusedSession != null
				&& this.reusedSessionView != null) {
			view= (RHelpView) this.reusedSessionView.getPart(false);
		}
		if (view != null) {
			if (activate) {
				view.getSite().getPage().activate(view);
			}
			else {
				view.getSite().getPage().bringToTop(view);
			}
		}
		else {
			view= (RHelpView) getSite().getPage().showView(RUI.R_HELP_VIEW_ID, null,
					activate ? IWorkbenchPage.VIEW_ACTIVATE : IWorkbenchPage.VIEW_VISIBLE);
		}
		
		if (showOpenPage(view, url)) {
			return;
		}
		
		if ( !newPage && PreferencesUtil.getInstancePrefs().getPreferenceValue(
				RHelpPreferences.SEARCH_REUSE_PAGE_ENABLED_PREF )) {
			if (this.reusedSession != null && !view.canOpen(this.reusedSession)) {
				this.reusedSession= null;
			}
			this.reusedSession= view.openUrl(url, this.reusedSession);
		}
		else {
			this.reusedSession= view.openUrl(url, null);
		}
		this.reusedSessionUrl= url;
		this.reusedSessionView= reference;
	}
	
	private boolean showOpenPage(final RHelpView view, final String url) {
		final BrowserSession session= view.findBrowserSession(url);
		if (session != null) {
			view.showPage(session);
			return true;
		}
		return false;
	}
	
}
