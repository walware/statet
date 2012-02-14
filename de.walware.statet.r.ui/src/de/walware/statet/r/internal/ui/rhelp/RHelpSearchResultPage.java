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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
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
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.actions.SimpleContributionItem;
import de.walware.ecommons.ui.mpbv.BrowserSession;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRHelpSearchMatch;
import de.walware.statet.r.core.rhelp.IRHelpSearchMatch.MatchFragment;
import de.walware.statet.r.core.rhelp.IRPackageHelp;
import de.walware.statet.r.core.rhelp.RHelpSearchQuery;
import de.walware.statet.r.ui.RUI;


public class RHelpSearchResultPage extends AbstractTextSearchViewPage
		implements IPropertyChangeListener {
	
	
	private static final RHelpSearchUIMatch[] NO_MATCHES = new RHelpSearchUIMatch[0];
	
	private static interface IResultContentProvider extends IContentProvider {
		
		void elementsChanged(final Object[] elements);
		
	}
	
	private static final ViewerComparator SCORE_SORTER = new ViewerComparator() {
		@Override
		public int compare(final Viewer viewer, final Object e1, final Object e2) {
			final IRHelpSearchMatch match1 = ((RHelpSearchUIMatch) e1).getRHelpMatch();
			final IRHelpSearchMatch match2 = ((RHelpSearchUIMatch) e2).getRHelpMatch();
			return (int) ((match2.getScore() - match1.getScore()) * 1e8);
		}
	};
	
	private static class MatchLabelProvider extends StyledCellLabelProvider {
		
		
		public MatchLabelProvider() {
		}
		
		
		@Override
		public void update(final ViewerCell cell) {
			final Object element = cell.getElement();
			final StyledString text = new StyledString();
			
			if (element instanceof RHelpSearchUIMatch) {
				final IRHelpSearchMatch match = ((RHelpSearchUIMatch) element).getRHelpMatch();
				final MatchFragment[] fragments = match.getBestFragments();
				if (fragments != null && fragments.length > 0) {
					text.append(fragments[0].getFieldLabel(), StyledString.QUALIFIER_STYLER);
					text.append(": ", StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
					if (fragments[0].getField() == RHelpSearchQuery.TOPICS_FIELD) {
						RHelpLabelProvider.append(text, fragments[0]);
						for (int i = 1; i < fragments.length; i++) {
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
				styleRange = super.prepareStyleRange(styleRange, applyColors);
				styleRange.borderStyle = SWT.BORDER_DOT;
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
	
	
	private class TreeContentProvider implements ITreeContentProvider, IResultContentProvider {
		
		private TreeViewer fViewer;
		
		private final List<IRPackageHelp> fCurrentPackages = new ArrayList<IRPackageHelp>();
		
		public TreeContentProvider() {
		}
		
		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			fViewer = (TreeViewer) viewer;
		}
		
		@Override
		public void dispose() {
		}
		
		@Override
		public Object[] getElements(final Object inputElement) {
			final IRPackageHelp[] packages = getInput().getPackages();
			fCurrentPackages.clear();
			fCurrentPackages.addAll(Arrays.asList(packages));
			return packages;
		}
		
		@Override
		public Object getParent(final Object element) {
			if (element instanceof IRHelpPage) {
				return ((IRHelpPage) element).getPackage();
			}
			return null;
		}
		
		@Override
		public boolean hasChildren(final Object element) {
			if (element instanceof IRPackageHelp) {
				return true;
			}
			if (element instanceof RHelpSearchUIMatch) {
				final MatchFragment[] fragments = ((RHelpSearchUIMatch) element).getRHelpMatch().getBestFragments();
				return (fragments != null && fragments.length > 0);
			}
			return false;
		}
		
		@Override
		public Object[] getChildren(final Object parentElement) {
			if (parentElement instanceof IRPackageHelp) {
				return getInput().getMatches((IRPackageHelp) parentElement);
			}
			if (parentElement instanceof RHelpSearchUIMatch) {
				return ((RHelpSearchUIMatch) parentElement).getRHelpMatch().getBestFragments();
			}
			return new Object[0];
		}
		
		@Override
		public void elementsChanged(final Object[] elements) {
			final Set<IRPackageHelp> packages = new HashSet<IRPackageHelp>();
			for (int i = 0; i < elements.length; i++) {
				final RHelpSearchUIMatch match = (RHelpSearchUIMatch) elements[i];
				final IRPackageHelp pkg = match.getRHelpMatch().getPage().getPackage();
				if (packages.contains(pkg)) {
					continue;
				}
				packages.add(pkg);
			}
			
			if (packages.isEmpty()) {
				return;
			}
			fViewer.getTree().setRedraw(false);
			try {
				for (final IRPackageHelp pkg : packages) {
					if (getInput().hasMatches(pkg)) {
						int idx = Collections.binarySearch(fCurrentPackages, pkg);
						if (idx >= 0) {
							fViewer.refresh(pkg, true);
						}
						else {
							idx = -idx-1;
							fCurrentPackages.add(idx, pkg);
							fViewer.insert(TreePath.EMPTY, pkg, idx);
						}
					}
					else {
						final int idx = Collections.binarySearch(fCurrentPackages, pkg);
						if (idx >= 0) {
							fCurrentPackages.remove(idx);
							fViewer.remove(TreePath.EMPTY, idx);
						}
					}
				}
			}
			finally {
				fViewer.getTree().setRedraw(true);
			}
		}
		
	}
	
	private class TableContentProvider implements IStructuredContentProvider, IResultContentProvider {
		
		private TableViewer fViewer;
		
		private final List<RHelpSearchUIMatch> fCurrentMatches = new ArrayList<RHelpSearchUIMatch>();
		
		public TableContentProvider() {
		}
		
		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			fViewer = (TableViewer) viewer;
		}
		
		@Override
		public void dispose() {
		}
		
		@Override
		public Object[] getElements(final Object inputElement) {
			final RHelpSearchUIMatch[] matches = getInput().getMatches();
			fCurrentMatches.clear();
			fCurrentMatches.addAll(Arrays.asList(matches));
			return matches;
		}
		
		@Override
		public void elementsChanged(final Object[] elements) {
			final Map<IRPackageHelp, RHelpSearchUIMatch[]> packages = new HashMap<IRPackageHelp, RHelpSearchUIMatch[]>();
			fViewer.getTable().setRedraw(false);
			try {
				for (int i = 0; i < elements.length; i++) {
					final RHelpSearchUIMatch match = (RHelpSearchUIMatch) elements[i];
					final IRPackageHelp pkg = match.getRHelpMatch().getPage().getPackage();
					RHelpSearchUIMatch[] matches = packages.get(pkg);
					if (matches == null) {
						matches = getInput().getMatches(pkg);
						if (matches == null) {
							matches = NO_MATCHES;
						}
					}
					if (Arrays.binarySearch(matches, match) < 0) {
						final int idx = Collections.binarySearch(fCurrentMatches, match);
						if (idx >= 0) {
							fCurrentMatches.remove(idx);
							fViewer.remove(match);
						}
					}
					else {
						int idx = Collections.binarySearch(fCurrentMatches, match);
						if (idx >= 0) {
							fViewer.refresh(match, true);
						}
						else {
							idx = -idx-1;
							fCurrentMatches.add(idx, match);
							fViewer.insert(match, idx);
						}
					}
				}
			}
			finally {
				fViewer.getTable().setRedraw(true);
			}
		}
		
	}
	
	private class OpenHandler extends SimpleContributionItem {
		
		public OpenHandler() {
			super("Open (New Page)", "O");
		}
		
		@Override
		protected void execute() throws ExecutionException {
			final StructuredViewer viewer = getViewer();
			if (UIAccess.isOkToUse(viewer)) {
				final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection instanceof ITreeSelection) {
					final TreePath[] paths = ((ITreeSelection) selection).getPaths();
					for (int i = 0; i < paths.length; i++) {
						open(getRelevantElement(paths[i]), true, true);
					}
				}
				else {
					for (final Iterator<?> iter = selection.iterator(); iter.hasNext(); ) {
						open(iter.next(), true, true);
					}
				}
			}
		}
		
	}
	
	
	private IResultContentProvider fContentProvider;
	
	private BrowserSession fReusedSession;
	private String fReusedSessionUrl;
	private IWorkbenchPartReference fReusedSessionView;
	
	private ViewerComparator fSorter;
	
	private SimpleContributionItem fSortByPackage;
	private SimpleContributionItem fSortByScore;
	
	
	public RHelpSearchResultPage() {
	}
	
	
	@Override
	public void init(final IPageSite pageSite) {
		super.init(pageSite);
		
		initActions();
	}
	
	@Override
	public void createControl(final Composite parent) {
		super.createControl(parent);
		
		JFaceResources.getColorRegistry().addListener(this);
	}
	
	private void initActions() {
		fSortByPackage = new SimpleContributionItem(
				RUI.getImageDescriptor(RUI.IMG_LOCTOOL_SORT_PACKAGE), null,
				"Sort by Package", null, SimpleContributionItem.STYLE_CHECK) {
			@Override
			protected void execute() throws ExecutionException {
				fSorter = null;
				updateSorter();
			}
		};
		fSortByScore = new SimpleContributionItem(
				SharedUIResources.getImages().getDescriptor(SharedUIResources.LOCTOOL_SORT_SCORE_IMAGE_ID), null,
				"Sort by Relevance", null, SimpleContributionItem.STYLE_CHECK) {
			@Override
			protected void execute() throws ExecutionException {
				fSorter = SCORE_SORTER;
				updateSorter();
			}
		};
	}
	
	@Override
	public void restoreState(final IMemento memento) {
		super.restoreState(memento);
		
		if (memento != null) {
			final String sortValue = memento.getString("sort.by"); //$NON-NLS-1$
			if (sortValue != null && sortValue.equals("score")) { //$NON-NLS-1$
				fSorter = SCORE_SORTER;
			}
			else {
				fSorter = null;
			}
			updateSorter();
		}
	}
	
	@Override
	public void saveState(final IMemento memento) {
		super.saveState(memento);
		
		final String sortValue = (fSorter == SCORE_SORTER) ? "score" : ""; //$NON-NLS-1$ //$NON-NLS-2$
		memento.putString("sort.by", sortValue); //$NON-NLS-1$
	}
	
	@Override
	protected TableViewer createTableViewer(final Composite parent) {
		return new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
	}
	
	@Override
	protected void configureTableViewer(final TableViewer viewer) {
		final TableColumnLayout layout = new TableColumnLayout();
		viewer.getControl().getParent().setLayout(layout);
		viewer.getTable().setHeaderVisible(true);
		
		viewer.setUseHashlookup(true);
		fContentProvider = new TableContentProvider();
		viewer.setContentProvider(fContentProvider);
		
		final TableViewerColumn column1 = new TableViewerColumn(viewer, SWT.LEFT);
		column1.getColumn().setText("Package / Page");
		layout.setColumnData(column1.getColumn(), new ColumnWeightData(1));
		column1.setLabelProvider(new RHelpLabelProvider());
		
		final TableViewerColumn column2 = new TableViewerColumn(viewer, SWT.LEFT);
		column2.getColumn().setText("Best Match");
		layout.setColumnData(column2.getColumn(), new ColumnWeightData(1));
		column2.setLabelProvider(new MatchLabelProvider());
		
		ColumnViewerToolTipSupport.enableFor(viewer);
		updateSorter();
	}
	
	@Override
	protected void configureTreeViewer(final TreeViewer viewer) {
		final TreeColumnLayout layout = new TreeColumnLayout();
		viewer.getControl().getParent().setLayout(layout);
		
		viewer.setUseHashlookup(true);
		fContentProvider = new TreeContentProvider();
		viewer.setContentProvider(fContentProvider);
		
		final TreeViewerColumn column1 = new TreeViewerColumn(viewer, SWT.LEFT);
		column1.getColumn().setText("Package / Page / Match");
		layout.setColumnData(column1.getColumn(), new ColumnWeightData(1));
		column1.setLabelProvider(new RHelpLabelProvider());
		
		ColumnViewerToolTipSupport.enableFor(viewer);
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		final String property = event.getProperty();
		if (property.equals(JFacePreferences.QUALIFIER_COLOR)
				|| property.equals(JFacePreferences.COUNTER_COLOR)
				|| property.equals(JFacePreferences.DECORATIONS_COLOR)
				|| property.equals(RHelpLabelProvider.HIGHLIGHT_BG_COLOR_NAME)
				|| property.equals(IWorkbenchPreferenceConstants.USE_COLORED_LABELS)) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					final StructuredViewer viewer = getViewer();
					if (UIAccess.isOkToUse(viewer)) {
						viewer.refresh();
					}
				}
			});
		}
	}
	
	@Override
	protected void fillToolbar(final IToolBarManager tbm) {
		super.fillToolbar(tbm);
		
		if (getLayout() == FLAG_LAYOUT_FLAT) {
			updateSorter();
			tbm.appendToGroup(IContextMenuConstants.GROUP_VIEWER_SETUP, new Separator(".sorting")); //$NON-NLS-1$
			tbm.appendToGroup(".sorting", fSortByPackage); //$NON-NLS-1$
			tbm.appendToGroup(".sorting", fSortByScore); //$NON-NLS-1$
		}
	}
	
	@Override
	protected void fillContextMenu(final IMenuManager mgr) {
		mgr.appendToGroup(IContextMenuConstants.GROUP_OPEN, new OpenHandler());
		super.fillContextMenu(mgr);
	}
	
	private void updateSorter() {
		final ViewerComparator sorter = fSorter;
		fSortByPackage.setChecked(sorter == null);
		fSortByScore.setChecked(sorter == SCORE_SORTER);
		
		if (getLayout() == FLAG_LAYOUT_FLAT) {
			final StructuredViewer viewer = getViewer();
			if (UIAccess.isOkToUse(viewer)) {
				viewer.setComparator(sorter);
			}
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		JFaceResources.getColorRegistry().removeListener(this);
	}
	
	
	@Override
	protected void elementsChanged(final Object[] objects) {
		try {
			if (fContentProvider != null) {
				fContentProvider.elementsChanged(objects);
			}
		}
		catch (final Throwable e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					"An error occurred when updating the R help search result table.", e));
		}
	}
	
	@Override
	protected void clear() {
		getViewer().setInput(getInput());
	}
	
	@Override
	public RHelpSearchUIResult getInput() {
		return (RHelpSearchUIResult) super.getInput();
	}
	
	
	@Override
	protected void handleOpen(final OpenEvent event) {
		final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		if (selection.size() == 1) {
			Object element = null;
			if (selection instanceof ITreeSelection) {
				element = getRelevantElement(((ITreeSelection) selection).getPaths()[0]);
			}
			if (element == null) {
				element = selection.getFirstElement();
			}
			open(element, true, false);
		}
	}
	
	@Override
	public int getDisplayedMatchCount(final Object element) {
		if (!(element instanceof RHelpSearchUIMatch)) {
			return 0;
		}
		return super.getDisplayedMatchCount(element);
	}
	
	@Override
	protected void showMatch(final Match match, final int currentOffset, final int currentLength,
			final boolean activate)
			throws PartInitException {
		openPage((RHelpSearchUIMatch) match, activate, false);
	}
	
	
	protected void open(final Object element, final boolean activate, final boolean newPage) {
		try {
			if (element instanceof RHelpSearchUIMatch) {
				openPage((RHelpSearchUIMatch) element, activate, newPage);
			}
			if (element instanceof IRPackageHelp) {
				openPackage((IRPackageHelp) element, activate, newPage);
			}
		}
		catch (final PartInitException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					"An error occurred when trying to open the R help page (element = " + element + ".", e));
		}
	}
	
	protected void openPage(final RHelpSearchUIMatch match, final boolean activate, final boolean newPage)
			throws PartInitException {
		final IRHelpSearchMatch rMatch = match.getRHelpMatch();
		String url = RCore.getRHelpManager().getPageHttpUrl(rMatch.getPage(),
				RHelpUIServlet.BROWSE_TARGET);
		final RHelpSearchQuery rHelpQuery = getInput().getQuery().getRHelpQuery();
		if (rHelpQuery.getSearchType() == RHelpSearchQuery.DOC_SEARCH
				&& rHelpQuery.getSearchString().length() > 0) {
			try {
				final String value = URLEncoder.encode(rHelpQuery.getSearchString(), "UTF-8"); //$NON-NLS-1$
				url += "?qs=" + value; //$NON-NLS-1$
			} catch (final UnsupportedEncodingException ignore) {}
		}
		doOpen(url, activate, newPage);
	}
	
	protected void openPackage(final IRPackageHelp packageHelp, final boolean activate, final boolean newPage)
			throws PartInitException {
		final String url = RCore.getRHelpManager().getPackageHttpUrl(packageHelp,
				RHelpUIServlet.BROWSE_TARGET);
		doOpen(url, activate, newPage);
	}
	
	private void doOpen(final String url, final boolean activate, final boolean newPage)
			throws PartInitException {
		RHelpView view = null;
		final IWorkbenchPartReference reference = getSite().getPage().getReference(view);
		if ( !newPage && fReusedSession != null
				&& !fReusedSession.getUrl().equals(fReusedSessionUrl) ) {
			fReusedSession = null;
		}
		else if ( fReusedSession != null
				&& fReusedSessionView != null) {
			view = (RHelpView) fReusedSessionView.getPart(false);
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
			view = (RHelpView) getSite().getPage().showView(RUI.R_HELP_VIEW_ID, null,
					activate ? IWorkbenchPage.VIEW_ACTIVATE : IWorkbenchPage.VIEW_VISIBLE);
		}
		
		if (showOpenPage(view, url)) {
			return;
		}
		
		if ( !newPage && PreferencesUtil.getInstancePrefs().getPreferenceValue(
				RHelpPreferences.SEARCH_REUSE_PAGE_ENABLED_PREF )) {
			if (fReusedSession != null && !view.canOpen(fReusedSession)) {
				fReusedSession = null;
			}
			fReusedSession = view.openUrl(url, fReusedSession);
		}
		else {
			fReusedSession = view.openUrl(url, null);
		}
		fReusedSessionUrl = url;
		fReusedSessionView = reference;
	}
	
	private boolean showOpenPage(final RHelpView view, final String url) {
		final List<BrowserSession> sessions = view.getSessions();
		for (final BrowserSession session : sessions) {
			if (url.equals(session.getUrl())) {
				view.showPage(session);
				return true;
			}
		}
		return false;
	}
	
}
