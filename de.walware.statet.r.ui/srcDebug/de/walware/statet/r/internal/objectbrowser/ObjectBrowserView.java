/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.objectbrowser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.CollapseAllHandler;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import de.walware.ecommons.FastList;
import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.ui.IElementNameProvider;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorCommandIds;
import de.walware.ecommons.ltk.ui.util.ViewerDragSupport;
import de.walware.ecommons.models.core.util.IElementPartition;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.actions.HandlerContributionItem;
import de.walware.ecommons.ui.actions.SearchContributionItem;
import de.walware.ecommons.ui.components.StatusInfo;
import de.walware.ecommons.ui.util.ColumnHoverManager;
import de.walware.ecommons.ui.util.ColumnHoverStickyManager;
import de.walware.ecommons.ui.util.ColumnWidgetTokenOwner;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.InformationDispatchHandler;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.PostSelectionProviderProxy;
import de.walware.ecommons.ui.util.StatusLineMessageManager;
import de.walware.ecommons.ui.util.TreeSelectionProxy;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.StatetImages;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.util.IToolProvider;
import de.walware.statet.nico.core.util.IToolRetargetable;
import de.walware.statet.nico.ui.IToolRegistry;
import de.walware.statet.nico.ui.IToolRegistryListener;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.ToolSessionUIData;
import de.walware.statet.nico.ui.actions.ToolRetargetableHandler;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;

import de.walware.statet.r.console.core.AbstractRDataRunnable;
import de.walware.statet.r.console.core.IRDataAdapter;
import de.walware.statet.r.console.core.RConsoleTool;
import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.console.core.RWorkspace.ICombinedREnvironment;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementComparator;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.internal.ui.rtools.RunPrintInR;
import de.walware.statet.r.ui.RLabelProvider;
import de.walware.statet.r.ui.rtool.RElementInfoHoverCreator;
import de.walware.statet.r.ui.rtool.RElementInfoTask;


public class ObjectBrowserView extends ViewPart implements IToolProvider {
	
	
	private static final String FILTER_USERSPACEONLY_SETTINGS_KEY = "filter.only_userspace.enabled"; //$NON-NLS-1$
	private static final String FILTER_INTERNALINCLUDE_SETTINGS_KEY = "filter.include_internal.enabled"; //$NON-NLS-1$
	private static final String SORT_BYTYPE_SETTINGS_KEY = "sort.by_name.enabled"; //$NON-NLS-1$
	
	static final RElementComparator ELEMENTNAME_COMPARATOR = new RElementComparator();
	private static final ViewerComparator TYPE_COMPARATOR = new SortByTypeComparator();
	
	private static final String OPEN_COMMAND_ID = "org.eclipse.jdt.ui.edit.text.java.open.editor"; //$NON-NLS-1$
	
	
	private class RefreshWorkspaceR extends AbstractRDataRunnable {
		
		public RefreshWorkspaceR() {
			super("r/objectbrowser/refreshWorkspace.force", "Update Object Browser"); //$NON-NLS-1$
		}
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			if (event == MOVING_FROM) {
				return false;
			}
			return true;
		}
		
		@Override
		protected void run(final IRDataAdapter r,
				final IProgressMonitor monitor) throws CoreException {
			boolean current;
			synchronized (processLock) {
				current = (r.getTool() != getTool());
			}
			final IWorkbenchSiteProgressService progressService = (IWorkbenchSiteProgressService) getViewSite().getService(IWorkbenchSiteProgressService.class);
			if (current && progressService != null) {
				progressService.incrementBusy();
			}
			try {
				fInputUpdater.forceOnWorkspaceChange();
				r.refreshWorkspaceData(RWorkspace.REFRESH_COMPLETE, monitor);
			}
			finally {
				if (current && progressService != null) {
					progressService.decrementBusy();
				}
			}
		}
		
	}
	
	private class RefreshHandler extends ToolRetargetableHandler {
		
		private final ElementUpdater fUpdater = new ElementUpdater(IWorkbenchCommandConstants.FILE_REFRESH);
		
		public RefreshHandler() {
			super(ObjectBrowserView.this, ObjectBrowserView.this.getSite());
			init();
		}
		
		@Override
		protected Object doExecute(final ExecutionEvent event) {
			scheduleUpdateAll();
			return null;
		}
		
		@Override
		protected void doRefresh() {
			super.doRefresh();
			fUpdater.schedule();
		}
		
	}
	
	private class FilterUserspaceHandler extends AbstractHandler implements IElementUpdater {
		
		@Override
		public Object execute(final ExecutionEvent event) throws ExecutionException {
			fFilterUserspace = !fFilterUserspace;
			fSettings.put(FILTER_USERSPACEONLY_SETTINGS_KEY, fFilterUserspace);
			fInputUpdater.forceUpdate(process);
			fInputUpdater.schedule();
			return null;
		}
		
		@Override
		public void updateElement(final UIElement element, final Map parameters) {
			element.setChecked(fFilterUserspace);
		}
		
	}
	
	private class FilterInternalHandler extends AbstractHandler implements IElementUpdater {
		
		@Override
		public Object execute(final ExecutionEvent event) throws ExecutionException {
			fFilterIncludeInternal = !fFilterIncludeInternal;
			fSettings.put(FILTER_INTERNALINCLUDE_SETTINGS_KEY, fFilterIncludeInternal);
			updateFilter();
			return null;
		}
		
		@Override
		public void updateElement(final UIElement element, final Map parameters) {
			element.setChecked(fFilterIncludeInternal);
		}
		
	}
	
	private class SortByTypeHandler extends AbstractHandler implements IElementUpdater {
		
		@Override
		public Object execute(final ExecutionEvent event) throws ExecutionException {
			fSortByType = !fSortByType;
			fSettings.put(SORT_BYTYPE_SETTINGS_KEY, fSortByType);
			updateSorter();
			return null;
		}
		
		@Override
		public void updateElement(final UIElement element, final Map parameters) {
			element.setChecked(fSortByType);
		}
		
	}
	
	private class TreeElementSelection extends TreeSelectionProxy implements IElementNameProvider {
		
		public TreeElementSelection(final ITreeSelection selection) {
			super(selection);
		}
		
		@Override
		public IElementName getElementName(final Object selectionElement) {
			if (selectionElement instanceof TreePath) { 
				return ObjectBrowserView.this.getElementName((TreePath) selectionElement);
			}
			return null;
		}
		
	}
	
	private class TreeSelectionProvider extends PostSelectionProviderProxy {
		
		public TreeSelectionProvider() {
			super(fTreeViewer);
		}
		
		@Override
		protected ISelection getSelection(final ISelection originalSelection) {
			return new TreeElementSelection((ITreeSelection) originalSelection);
		}
		
	}
	
	private class HoverManager extends ColumnHoverManager {
		
		HoverManager() {
			super(fTreeViewer, fTokenOwner, new RElementInfoHoverCreator());
			
			final ColumnHoverStickyManager stickyManager = new ColumnHoverStickyManager(fTokenOwner, this);
			getInternalAccessor().setInformationControlReplacer(stickyManager);
		}
		
		@Override
		protected Object prepareHoverInformation(final ViewerCell cell) {
			final TreePath treePath = cell.getViewerRow().getTreePath();
			final RElementName elementName = getElementName(treePath);
			if (elementName != null && elementName.getScope() != null) {
				return elementName;
			}
			return null;
		}
		
		@Override
		protected Object getHoverInformation(final Object element) {
			if (element instanceof RElementName && process != null) {
				final RElementInfoTask updater = new RElementInfoTask((RElementName) element);
				return updater.load(process, getSubjectControl());
			}
			return null;
		}
		
	}
	
	
	private TreeViewer fTreeViewer;
	private TreeSelectionProvider fTreeSelectionProvider;
	private ColumnWidgetTokenOwner fTokenOwner;
	private Clipboard fClipboard;
	private StatusLineMessageManager statusLine;
	
	private IDialogSettings fSettings;
	
	final Object processLock = new Object();
	private RProcess process; // note: we write only in ui thread
	
	private IToolRegistryListener fToolRegistryListener;
	private final FastList<IToolRetargetable> fToolListenerList= new FastList<>(IToolRetargetable.class);
	
	private final RefreshWorkspaceR fManualRefreshRunnable = new RefreshWorkspaceR();
	
	private final ContentJob fInputUpdater = new ContentJob(this);
	
	private ContentProvider contentProvider;
	
	private boolean fFilterUserspace;
	private boolean fFilterIncludeInternal;
	private String fFilterText;
	private boolean fSortByType;
	
	private SearchContributionItem fSearchTextItem;
	
	private boolean fFilterUserspaceActivated;
	
	private CopyElementNameHandler fCopyElementNameHandler;
	private DeleteHandler fDeleteElementHandler;
	private PrintHandler fPrintElementHandler;
	private IHandler2 fOpenInEditorHandler;
	
	private Object fCurrentInfoObject;
	
	private ColumnHoverManager fHoveringController;
	
	private AbstractHandler fSearchStartHandler;
	
	private HandlerContributionItem fRefreshToolbarItem;
	private HandlerContributionItem fRefreshMenuItem;
	private boolean fRefreshDirtyIndicator;
	
	
	public ObjectBrowserView() {
	}
	
	@Override
	public void init(final IViewSite site, final IMemento memento) throws PartInitException {
		super.init(site, memento);
		
		this.statusLine = new StatusLineMessageManager(site.getActionBars().getStatusLineManager());
		
		fSettings = DialogUtil.getDialogSettings(RUIPlugin.getDefault(), "ObjectBrowser");
		
		fFilterUserspaceActivated = fFilterUserspace = fSettings.getBoolean(FILTER_USERSPACEONLY_SETTINGS_KEY);
		fFilterIncludeInternal = fSettings.getBoolean(FILTER_INTERNALINCLUDE_SETTINGS_KEY);
		fSortByType = fSettings.getBoolean(SORT_BYTYPE_SETTINGS_KEY);
	}
	
	@Override
	public void saveState(final IMemento memento) {
		super.saveState(memento);
		
		fSettings = DialogUtil.getDialogSettings(RUIPlugin.getDefault(), "ObjectBrowser");
	}
	
	
	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(LayoutUtil.applySashDefaults(new GridLayout(), 1));
		
		fTreeViewer = new TreeViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
		fTreeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3));
		fTreeViewer.setUseHashlookup(true);
		
		fTreeSelectionProvider = new TreeSelectionProvider();
		
		fTreeViewer.setLabelProvider(new RLabelProvider(RLabelProvider.COUNT) {
			@Override
			protected String getEnvCountInfo(final ICombinedREnvironment envir) {
				final StringBuilder textBuilder = getTextBuilder();
				textBuilder.append(" ("); //$NON-NLS-1$
				final ContentInput input = ObjectBrowserView.this.contentProvider.getInput();
				if (input != null && input.hasEnvFilter()) {
					final Object[] children = input.getEnvFilterChildren(envir);
					if (children != null) {
						textBuilder.append(children.length);
					}
					else {
						textBuilder.append('-');
					}
					textBuilder.append('/');
				}
				textBuilder.append(envir.getLength());
				textBuilder.append(')');
				return textBuilder.toString();
			}
		});
		fTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(final DoubleClickEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.size() != 1) {
					return;
				}
				final Object element = selection.getFirstElement();
				if (element instanceof RObject) {
					final RObject object = (RObject) element;
					switch (object.getRObjectType()) {
					case RObject.TYPE_ENV:
					case RObject.TYPE_LIST:
					case RObject.TYPE_DATAFRAME:
					case RObject.TYPE_S4OBJECT:
					case RObject.TYPE_REFERENCE:
						fTreeViewer.setExpandedState(element, !fTreeViewer.getExpandedState(element));
					}
				}
			}
		});
		fTreeSelectionProvider.addPostSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				updateSelectionInfo((ITreeSelection) event.getSelection());
			}
		});
		this.contentProvider = new ContentProvider();
		fTreeViewer.setContentProvider(this.contentProvider);
		updateSorter();
		fTreeViewer.setInput(this);
		
		fTokenOwner = new ColumnWidgetTokenOwner(fTreeViewer);
		fHoveringController = new HoverManager();
		fHoveringController.setSizeConstraints(100, 12, false, true);
		fHoveringController.install(fTreeViewer.getTree());
		
		createActions();
		hookContextMenu();
		getSite().setSelectionProvider(fTreeSelectionProvider);
		
		final ViewerDragSupport dragSupport = new ViewerDragSupport(fTreeViewer);
		dragSupport.addDragSourceListener(new ViewerDragSupport.TextDragSourceListener(fTreeViewer));
		dragSupport.init();
		
		// listen on console changes
		final IToolRegistry toolRegistry = NicoUI.getToolRegistry();
		fToolRegistryListener = new IToolRegistryListener() {
			@Override
			public void toolSessionActivated(final ToolSessionUIData sessionData) {
				final ToolProcess process = sessionData.getProcess();
				UIAccess.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						connect(process);
					}
				});
			}
			@Override
			public void toolTerminated(final ToolSessionUIData sessionData) {
				final ToolProcess process = sessionData.getProcess();
				UIAccess.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						if (process == getTool()) {
							connect(null);
						}
					}
				});
			}
		};
		toolRegistry.addListener(fToolRegistryListener, getViewSite().getPage());
		connect(toolRegistry.getActiveToolSession(getViewSite().getPage()).getProcess());
	}
	
	TreeViewer getViewer() {
		return fTreeViewer;
	}
	
	private void createActions() {
		final IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
		final IContextService contexts = (IContextService) getSite().getService(IContextService.class);
		
		contexts.activateContext("de.walware.ecommons.base.contexts.StructuredElementViewer"); //$NON-NLS-1$
		
		final RefreshHandler refreshHandler = new RefreshHandler();
		handlerService.activateHandler(IWorkbenchCommandConstants.FILE_REFRESH, refreshHandler);
		final CollapseAllHandler collapseAllHandler = new CollapseAllHandler(fTreeViewer);
		handlerService.activateHandler(CollapseAllHandler.COMMAND_ID, collapseAllHandler);
		fCopyElementNameHandler = new CopyElementNameHandler(this);
		handlerService.activateHandler(ISourceEditorCommandIds.COPY_ELEMENT_NAME, fCopyElementNameHandler);
		handlerService.activateHandler(IWorkbenchCommandConstants.EDIT_COPY, fCopyElementNameHandler);
		fDeleteElementHandler = new DeleteHandler(this);
		handlerService.activateHandler(IWorkbenchCommandConstants.EDIT_DELETE, fDeleteElementHandler);
		fPrintElementHandler = new PrintHandler(this);
		handlerService.activateHandler(RunPrintInR.COMMAND_ID, fPrintElementHandler);
		final InformationDispatchHandler infoHandler = new InformationDispatchHandler(fTokenOwner);
		handlerService.activateHandler(InformationDispatchHandler.COMMAND_ID, infoHandler);
		fOpenInEditorHandler = new OpenInEditorHandler();
		handlerService.activateHandler(OPEN_COMMAND_ID, fOpenInEditorHandler);
		
		fSearchTextItem = new SearchContributionItem("search.text", //$NON-NLS-1$
				SearchContributionItem.VIEW_TOOLBAR, true ) {
			@Override
			protected void search() {
				fFilterText = getText();
				updateFilter();
			}
		};
		fSearchTextItem.setToolTip("Filter Elements");
		fSearchTextItem.setSizeControl(fTreeViewer.getControl().getParent());
		fSearchTextItem.setResultControl(fTreeViewer.getTree());
		
		fSearchStartHandler = new AbstractHandler() {
			@Override
			public Object execute(final ExecutionEvent arg0) {
				fSearchTextItem.show();
				return null;
			}
		};
		handlerService.activateHandler(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE, fSearchStartHandler);
		// add next/previous handler
		
		final ToggleAutoRefreshHandler autoRefreshHandler = new ToggleAutoRefreshHandler(this);
		
		final IActionBars actionBars = getViewSite().getActionBars();
		final IMenuManager viewMenu = actionBars.getMenuManager();
		final IToolBarManager viewToolbar = actionBars.getToolBarManager();
		
		viewMenu.add(new HandlerContributionItem(new CommandContributionItemParameter(getSite(),
				"Filter.OnlyUserspace", HandlerContributionItem.NO_COMMAND_ID, null, //$NON-NLS-1$
				null, null, null,
				"Show non-&package Variables only", null, null,
				HandlerContributionItem.STYLE_CHECK, null, false),
				new FilterUserspaceHandler() ));
		viewMenu.add(new HandlerContributionItem(new CommandContributionItemParameter(getSite(),
				"Filter.IncludeInternal", HandlerContributionItem.NO_COMMAND_ID, null, //$NON-NLS-1$
				null, null, null,
				"Show &Internal Variables ('.*')", null, null,
				HandlerContributionItem.STYLE_CHECK, null, false),
				new FilterInternalHandler() ));
		viewMenu.add(new HandlerContributionItem(new CommandContributionItemParameter(getSite(),
				"Sort.ByType", HandlerContributionItem.NO_COMMAND_ID, null, //$NON-NLS-1$
				null, null, null,
				"Sort by &Type", null, null,
				HandlerContributionItem.STYLE_CHECK, null, false),
				new SortByTypeHandler() ));
		
		viewMenu.add(new Separator());
		final HandlerContributionItem autoRefreshItem = new HandlerContributionItem(new CommandContributionItemParameter(getSite(),
				null, HandlerContributionItem.NO_COMMAND_ID, null, 
				null, null, null,
				"Refresh &automatically", null, null,
				HandlerContributionItem.STYLE_CHECK, null, false),
				autoRefreshHandler );
		viewMenu.add(autoRefreshItem);
		fRefreshMenuItem = new HandlerContributionItem(new CommandContributionItemParameter(getSite(),
				null, IWorkbenchCommandConstants.FILE_REFRESH, null, 
				StatetImages.getDescriptor(StatetImages.TOOL_REFRESH), StatetImages.getDescriptor(StatetImages.TOOLD_REFRESH), null,
				"&Refresh", null, null,
				HandlerContributionItem.STYLE_PUSH, null, false),
				refreshHandler );
		viewMenu.add(fRefreshMenuItem);
		
		viewMenu.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(final IMenuManager manager) {
				autoRefreshItem.update();
			}
		});
		
		viewToolbar.add(fSearchTextItem);
		viewToolbar.add(new Separator());
		fRefreshToolbarItem = new HandlerContributionItem(new CommandContributionItemParameter(getSite(),
				"Refresh", IWorkbenchCommandConstants.FILE_REFRESH, null, //$NON-NLS-1$
				StatetImages.getDescriptor(StatetImages.TOOL_REFRESH), StatetImages.getDescriptor(StatetImages.TOOLD_REFRESH), null,
				null, null, null,
				HandlerContributionItem.STYLE_PUSH, null, false),
				refreshHandler);
		fRefreshToolbarItem.setVisible(false);
		viewToolbar.add(fRefreshToolbarItem);
		viewToolbar.add(new HandlerContributionItem(new CommandContributionItemParameter(getSite(),
				"Collapse.All", CollapseAllHandler.COMMAND_ID, null, //$NON-NLS-1$
				null, null, null,
				null, null, null,
				HandlerContributionItem.STYLE_PUSH, null, false),
				collapseAllHandler));
	}
	
	void updateAutoRefresh(final boolean enabled) {
		if (this.process == null || this.process.isTerminated()) {
			return;
		}
		
		if (enabled) {
			updateDirty(false);
		}
		else {
			updateDirty(this.process.getWorkspaceData().isROBjectDBDirty());
		}
		
		if (fRefreshToolbarItem.isVisible() != enabled) {
			return;
		}
		fRefreshToolbarItem.setVisible(!enabled);
		final IContributionManager manager = fRefreshToolbarItem.getParent();
		manager.update(true);
		fSearchTextItem.resize();
	}
	
	void updateDirty(final boolean enabled) {
		if (fRefreshDirtyIndicator == enabled) {
			return;
		}
		final ImageDescriptor icon = (enabled) ?
				RUIPlugin.getDefault().getImageRegistry().getDescriptor(RUIPlugin.IMG_LOCTOOL_REFRESH_RECOMMENDED) :
				StatetImages.getDescriptor(StatetImages.TOOL_REFRESH);
		fRefreshToolbarItem.setIcon(icon);
		fRefreshMenuItem.setIcon(icon);
		fRefreshDirtyIndicator = enabled;
	}
	
	
	private void hookContextMenu() {
		final MenuManager menuManager = new MenuManager("ContextMenu", //$NON-NLS-1$
				"de.walware.statet.r.menus.RObjectBrowserContextMenu" ); //$NON-NLS-1$
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(final IMenuManager m) {
				contextMenuAboutToShow(m);
			}
		});
		final Menu contextMenu = menuManager.createContextMenu(fTreeViewer.getTree());
		fTreeViewer.getTree().setMenu(contextMenu);
		getSite().registerContextMenu(menuManager, fTreeViewer);
	}
	
	private void contextMenuAboutToShow(final IMenuManager m) {
		m.add(new HandlerContributionItem(new CommandContributionItemParameter(getSite(),
				null, OPEN_COMMAND_ID, null,
				null, null, null,
				"Open in Data &Viewer", null, null,
				HandlerContributionItem.STYLE_PUSH, null, true), fOpenInEditorHandler));
		
		m.add(new Separator("edit"));
		m.add(new HandlerContributionItem(new CommandContributionItemParameter(getSite(),
				"Copy.ElementName", ISourceEditorCommandIds.COPY_ELEMENT_NAME, null, //$NON-NLS-1$
				null, null, null,
				null, null, null,
				HandlerContributionItem.STYLE_PUSH, null, false),
				fCopyElementNameHandler));
		m.add(new HandlerContributionItem(new CommandContributionItemParameter(getSite(),
				"Delete", IWorkbenchCommandConstants.EDIT_DELETE, null, //$NON-NLS-1$
				null, null, null,
				null, null, null,
				HandlerContributionItem.STYLE_PUSH, null, false),
				fDeleteElementHandler));
		m.add(new Separator());
		m.add(new HandlerContributionItem(new CommandContributionItemParameter(getSite(),
				null, RunPrintInR.COMMAND_ID, null, 
				null, null, null,
				null, null, null,
				HandlerContributionItem.STYLE_PUSH, null, false),
				fPrintElementHandler));
		
		m.add(new Separator(SharedUIResources.ADDITIONS_MENU_ID));
	}
	
	/** May only be called in UI thread */
	public void connect(final ToolProcess tool) {
		if (this.process == tool) {
			return;
		}
		final RProcess oldProcess = this.process;
		if (oldProcess != null) {
			oldProcess.getWorkspaceData().removePropertyListener(fInputUpdater);
		}
		synchronized (this.processLock) {
			this.process = (RProcess) 
					((tool != null && tool.isProvidingFeatureSet(RConsoleTool.R_DATA_FEATURESET_ID)) ?
							tool : null);
		}
		if (fHoveringController != null) {
			fHoveringController.stop();
		}
		if (oldProcess != null) {
			clearInfo();
			oldProcess.getQueue().remove(new IToolRunnable[] { fManualRefreshRunnable });
		}
		if (!UIAccess.isOkToUse(fTreeViewer)) {
			return;
		}
		for (final IToolRetargetable listener : fToolListenerList.toArray()) {
			listener.setTool(this.process);
		}
		fInputUpdater.forceUpdate(this.process);
		if (this.process != null) {
			this.process.getWorkspaceData().addPropertyListener(fInputUpdater);
			updateAutoRefresh(this.process.getWorkspaceData().isAutoRefreshEnabled());
		}
		fInputUpdater.schedule();
	}
	
	@Override
	public RProcess getTool() {
		return process;
	}
	
	public ITreeSelection getSelection() {
		return (ITreeSelection) fTreeViewer.getSelection();
	}
	
	
	public boolean getShowInternal() {
		return fFilterIncludeInternal;
	}
	
	public boolean getShowConsenseUserspace() {
		return fFilterUserspace;
	}
	
	public String getSearchText() {
		return fFilterText;
	}
	
	
	@Override
	public void addToolRetargetable(final IToolRetargetable action) {
		fToolListenerList.add(action);
	}
	
	@Override
	public void removeToolRetargetable(final IToolRetargetable action) {
		fToolListenerList.remove(action);
	}
	
	@Override
	public Object getAdapter(final Class required) {
		if (required.equals(ITool.class)) {
			return process;
		}
		return super.getAdapter(required);
	}
	
	private void scheduleUpdateAll() {
		if (process != null) {
			process.getQueue().add(fManualRefreshRunnable);
		}
	}
	
	/** May only be called in UI thread (called by update job) */
	void updateViewer(final List<ICombinedREnvironment> updateEnvirs, final ContentInput input) {
		if (!UIAccess.isOkToUse(fTreeViewer)) {
			return;
		}
		fHoveringController.stop();
		
		this.contentProvider.setInput(input);
		
		final boolean changed = input.processChanged;
		ISelection selection = null;
		if (changed) {
			input.processChanged = false;
		}
		/*else*/ if (input.showCondensedUserspace != fFilterUserspaceActivated) {
			// If filter is changed, we have to retain the selection manually
			selection = fTreeViewer.getSelection();
		}
		fFilterUserspaceActivated = input.showCondensedUserspace;
		
		final Set<RReference> previousReferences = this.contentProvider.resetUsedReferences();
		if (updateEnvirs != null) {
			for (final ICombinedREnvironment entry : updateEnvirs) {
				fTreeViewer.refresh(entry, true);
			}
			if (!previousReferences.isEmpty()) {
				final Set<RReference> usedReferences = this.contentProvider.getUsedReferences();
				ITER_REFS: for (final RReference reference : previousReferences) {
					if (!usedReferences.contains(reference)) {
						// Update the envir copy in the viewer, if it refers to an updated envir
						for (final ICombinedREnvironment entry : updateEnvirs) {
							if (entry.getHandle() == reference.getHandle()) {
								fTreeViewer.refresh(reference, true);
								// reference is readded automatically to new set, if necessary
								continue ITER_REFS;
							}
						}
						// Keep/readd the reference, if it refers to an envir in the search path
						for (final ICombinedREnvironment entry : input.searchEnvirs) {
							if (entry.getHandle() == reference.getHandle()) {
								usedReferences.add(reference);
								continue ITER_REFS;
							}
						}
					}
				}
			}
		}
		else {
			fTreeViewer.refresh(true);
		}
		
		// Adapt and set selection
		if (selection != null && !selection.isEmpty()) {
			final ITreeSelection s = (ITreeSelection) selection;
			TreePath[] paths = s.getPaths();
			int j = 0;
			for (int i = 0; i < paths.length; i++) {
				final TreePath oldPath = paths[i];
				final int count = oldPath.getSegmentCount();
				final int shift = (input.showCondensedUserspace) ? -1 : +1;
				if (count + shift < 1) {
					continue;
				}
				final Object[] newPath = new Object[count + shift];
				for (int k = (shift == -1) ? +1 : 0; k < count; k++) {
					newPath[k + shift] = oldPath.getSegment(k);
				}
				if (shift == +1) {
					newPath[0] = ContentProvider.getCombinedRElement(newPath[1]).getModelParent();
				}
				paths[j++] = new TreePath(newPath);
			}
			fTreeViewer.setSelection(new TreeSelection(
					(j < paths.length) ? Arrays.copyOf(paths, j) : paths),
					true );
		}
		
		// Expand Global_Env, if it is a new process and has valid input
		EXPAND_GLOBALENV : if (changed && !input.showCondensedUserspace 
				&& input.rootElements != null && input.rootElements.length > 0) {
			for (Object element : input.rootElements) {
				if (fTreeViewer.getExpandedState(element)) {
					break EXPAND_GLOBALENV;
				}
			}
			fTreeViewer.expandToLevel(new TreePath(new Object[] { input.rootElements[0] }), 1);
		}
	}
	
	private void updateFilter() {
		fInputUpdater.schedule();
	}
	
	private void updateSorter() {
		fTreeViewer.setComparator(fSortByType ? TYPE_COMPARATOR : null);
	}
	
	private void updateSelectionInfo(final ITreeSelection selection) {
		final Object previousInfoObject = fCurrentInfoObject;
		fCurrentInfoObject = null;
		
		final RProcess tool = getTool();
		if (tool == null) {
			return;
		}
		if (selection.size() == 1) {
			fCurrentInfoObject = selection.getFirstElement();
			final TreePath treePath = selection.getPaths()[0];
			final IElementName elementName = getElementName(treePath);
			final String name = (elementName != null) ? elementName.getDisplayName() : null;
			if (name != null) {
				if (fCurrentInfoObject.equals(previousInfoObject)) {
					clearInfo();
				}
				this.statusLine.setSelectionMessage(new StatusInfo(IStatus.OK,
						NLS.bind("{0}  \u2012  {1}", name, tool.getLabel(ITool.DEFAULT_LABEL)) )); //$NON-NLS-1$
				return;
			}
		}
		clearInfo();
		if (selection.size() > 1) {
			this.statusLine.setSelectionMessage(new StatusInfo(IStatus.OK,
					NLS.bind("{0} items selected", selection.size()) ));
			return;
		}
		this.statusLine.setSelectionMessage(null);
	}
	
	private void clearInfo() {
	}
	
	/**
	 * Returns a shared clipboard resource, which can be used by actions of this view.
	 * 
	 * @return a clipboard object.
	 */
	Clipboard getClipboard() {
		if (fClipboard == null) {
			fClipboard = new Clipboard(Display.getCurrent());
		}
		return fClipboard;
	}
	
	StatusLineMessageManager getStatusLine() {
		return this.statusLine;
	}
	
	@Override
	public void setFocus() {
		fTreeViewer.getControl().setFocus();
	}
	
	@Override
	public void dispose() {
		if (fToolRegistryListener != null) {
			NicoUI.getToolRegistry().removeListener(fToolRegistryListener);
			fToolRegistryListener = null;
		}
		if (this.process != null) {
			this.process.getWorkspaceData().removePropertyListener(fInputUpdater);
			this.process.getQueue().remove(new IToolRunnable[] { fManualRefreshRunnable });
		}
		fInputUpdater.cancel();
		if (fHoveringController != null) {
			fHoveringController.dispose();
			fHoveringController = null;
		}
		if (fSearchStartHandler != null) {
			fSearchStartHandler.dispose();
			fSearchStartHandler = null;
		}
		if (fCopyElementNameHandler != null) {
			fCopyElementNameHandler.dispose();
			fCopyElementNameHandler = null;
		}
		if (fPrintElementHandler != null) {
			fPrintElementHandler.dispose();
			fPrintElementHandler = null;
		}
		
		for (final IToolRetargetable listener : fToolListenerList.toArray()) {
			listener.setTool(null);
		}
		
		super.dispose();
		
		if (fClipboard != null) {
			fClipboard.dispose();
			fClipboard = null;
		}
	}
	
	
	public RElementName getElementName(final TreePath treePath) {
		if (treePath.getSegmentCount() == 0) {
			return null;
		}
		int segmentIdx = 0;
		if (!fFilterUserspaceActivated) {
			if (treePath.getSegmentCount() == 1) { // search path item
				return ContentProvider.getCombinedRElement(treePath.getFirstSegment()).getElementName();
			}
			else { // main name at 1
				segmentIdx = 1;
			}
		}
		final List<RElementName> names= new ArrayList<>(treePath.getSegmentCount() - segmentIdx + 1);
		final ICombinedRElement first= ContentProvider.getCombinedRElement(treePath.getSegment(segmentIdx++));
		names.add(first.getModelParent().getElementName());
		names.add(first.getElementName());
		while (segmentIdx < treePath.getSegmentCount()) {
			final Object object= treePath.getSegment(segmentIdx++);
			if (object instanceof IElementPartition) {
				continue;
			}
			final ICombinedRElement rElement= ContentProvider.getCombinedRElement(object);
			names.add(rElement.getElementName());
		}
		return RElementName.create(names);
	}
	
}
