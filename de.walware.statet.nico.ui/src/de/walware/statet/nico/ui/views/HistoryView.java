/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.views;

import java.util.Date;

import com.ibm.icu.text.DateFormat;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.SearchPattern;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

import de.walware.ecommons.FastArrayCacheList;
import de.walware.ecommons.FastList;
import de.walware.ecommons.ui.HandlerContributionItem;
import de.walware.ecommons.ui.SearchContributionItem;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.StatetImages;

import de.walware.statet.nico.core.runtime.History;
import de.walware.statet.nico.core.runtime.IHistoryListener;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.History.Entry;
import de.walware.statet.nico.core.util.IToolProvider;
import de.walware.statet.nico.core.util.IToolRetargetable;
import de.walware.statet.nico.internal.ui.Messages;
import de.walware.statet.nico.internal.ui.actions.HistoryCopyAction;
import de.walware.statet.nico.internal.ui.actions.HistoryDragAdapter;
import de.walware.statet.nico.internal.ui.actions.HistorySubmitAction;
import de.walware.statet.nico.ui.IToolRegistry;
import de.walware.statet.nico.ui.IToolRegistryListener;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.ToolSessionUIData;
import de.walware.statet.nico.ui.actions.LoadHistoryAction;
import de.walware.statet.nico.ui.actions.SaveHistoryAction;
import de.walware.statet.nico.ui.console.ScrollLockAction;
import de.walware.statet.nico.ui.console.ScrollLockAction.Receiver;


/**
 * A view for the history of a tool process.
 * 
 * Usage: This class is not intend to be subclassed.
 */
public class HistoryView extends ViewPart implements IToolProvider {
	
	
	public static interface EntryFilter {
		
		public boolean select(Entry e);
		
	}
	
	
	/**
	 * Converts the selection of this view/viewer into a commmand text block.
	 * 
	 * @param selection a selection with history entries.
	 * @return command block.
	 */
	public static String createTextBlock(final Entry[] selection) {
		final StringBuilder text = new StringBuilder(selection.length * 8);
		for (int i = 0; i < selection.length; i++) {
			text.append(selection[i].getCommand());
			text.append('\n');
		}
		
		return text.toString();
	}
	
	public static String[] createTextArray(final Entry[] selection) {
		final String[] array = new String[selection.length];
		for (int i = 0; i < selection.length; i++) {
			array[i] = selection[i].getCommand();
		}
		
		return array;
	}
	
	
	private static final EntryFilter EMPTY_FILTER = new EntryFilter() {
		
		public boolean select(final Entry e) {
			return (e.getCommandMarker() >= 0);
		}
		
	};
	
	
	private class ViewReloadJob extends Job {
		
		ViewReloadJob() {
			super("Update History View"); //$NON-NLS-1$
			setPriority(SHORT);
			setUser(false);
		}
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			final ToolProcess process = fProcess;
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			if (process == null) {
				fContentProvider.setNewSource(null, new Entry[0]);
				return Status.OK_STATUS;
			}
			final History history = process.getHistory();
			history.getReadLock().lock();
			final Entry[] entries;
			try {
				entries = history.toArray();
			}
			finally {
				history.getReadLock().unlock();
			}
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			fContentProvider.setNewSource(history, entries);
			return Status.OK_STATUS;
		}
		
	}
	
	private class ViewContentProvider implements IHistoryListener, Runnable {
		
		private static final int REMOVE_THRESHOLD = 25;
		
		private History fCurrentSource;
		private boolean fIsScheduled;
		private final FastArrayCacheList<Entry> fToAdd = new FastArrayCacheList<Entry>(Entry.class, 16);
		private final FastArrayCacheList<Entry> fToRemove = new FastArrayCacheList<Entry>(Entry.class, 16);
		private Entry[] fNewEntrys;
		
		public synchronized void disable() {
			fCurrentSource = null;
			
			fNewEntrys = null;
			fToAdd.clear();
			fToRemove.clear();
		}
		
		public synchronized void setNewSource(final History source, final Entry[] es) {
			fCurrentSource = source;
			
			fNewEntrys = es;
			fToAdd.clear();
			fToRemove.clear();
			
			if (!fIsScheduled) {
				fIsScheduled = true;
				UIAccess.getDisplay().asyncExec(this);
			}
		}
		
		public synchronized void completeChange(final History source, final Entry[] es) {
			if (fCurrentSource != source) {
				return;
			}
			fNewEntrys = es;
			fToAdd.clear();
			fToRemove.clear();
			
			if (!fIsScheduled) {
				fIsScheduled = true;
				UIAccess.getDisplay().asyncExec(this);
			}
		}
		
		public synchronized void entryAdded(final History source, final Entry e) {
			if (fCurrentSource != source) {
				return;
			}
			fToAdd.add(e);
			
			if (!fIsScheduled) {
				fIsScheduled = true;
				UIAccess.getDisplay().asyncExec(this);
			}
		}
		
		public synchronized void entryRemoved(final History source, final Entry e) {
			if (fCurrentSource != source) {
				return;
			}
			fToRemove.add(e);
			
			if (!fIsScheduled) {
				fIsScheduled = true;
				UIAccess.getDisplay().asyncExec(this);
			}
		}
		
		public void run() {
			final Entry[] newEntries;
			final int toAdd;
			final Entry[] toAddEntries;
			final int toRemove;
			final Entry[] toRemoveEntries;
			final EntryFilter[] filter;
			synchronized (this) {
				fIsScheduled = false;
				if (!UIAccess.isOkToUse(fTable)) {
					return;
				}
				if ((fProcess != null && fCurrentSource != fProcess.getHistory())
						|| (fProcess == null && fCurrentSource != null)) {
					return;
				}
				
				newEntries = fNewEntrys;
				fNewEntrys = null;
				toAdd = fToAdd.size();
				toAddEntries = (toAdd > 0) ? fToAdd.removeAll() : null;
				toRemove = fToRemove.size();
				toRemoveEntries = (toRemove > REMOVE_THRESHOLD) ? fToRemove.removeAll() : null;
				filter = fFilter.toArray();
			}
			fTable.setRedraw(false);
			
			TableItem addedItem = null;
			if (newEntries != null) {
				fTable.deselectAll();
				
				final int reusableItemCount = fTable.getItemCount();
				int reuseItemIdx = 0;
				final int n = newEntries.length;
				ITER_ENTRY : for (int i = 0; i < n; i++) {
					final Entry e = newEntries[i];
					for (int f = 0; f < filter.length; f++) {
						if (!filter[f].select(e)) {
							continue ITER_ENTRY;
						}
					}
					if (reuseItemIdx < reusableItemCount) {
						addedItem = fTable.getItem(reuseItemIdx++);
					}
					else {
						addedItem = new TableItem(fTable, SWT.NONE);
					}
					addedItem.setData(e);
					updateItem(addedItem);
				}
				if (reuseItemIdx < reusableItemCount) {
					fTable.remove(reuseItemIdx, reusableItemCount-1);
				}
				
				if (addedItem != null) {
					fTable.showItem(addedItem);
				}
			}
			
			if (toAdd > 0) {
				ITER_ENTRY : for (int i = 0; i < toAdd; i++) {
					final Entry e = toAddEntries[i];
					for (int f = 0; f < filter.length; f++) {
						if (!filter[f].select(e)) {
							continue ITER_ENTRY;
						}
					}
					addedItem = new TableItem(fTable, SWT.NONE);
					addedItem.setData(e);
					updateItem(addedItem);
				}
			}
			if (toRemove > REMOVE_THRESHOLD) {
				final int itemCount = fTable.getItemCount();
				int[] removeIdxs = new int[toRemove];
				int count = 0;
				for (int i = 0; i < toRemove; i++) {
					for (int j = 0; j < itemCount; j++) {
						final TableItem removedItem = fTable.getItem(j);
						if (removedItem.getData() == toRemoveEntries[i]) {
							removedItem.setData(null);
							removeIdxs[count++] = j;
						}
					}
				}
				if (count > 0) {
					if (count < removeIdxs.length) {
						System.arraycopy(removeIdxs, 0, removeIdxs = new int[count], 0, count);
					}
					fTable.remove(removeIdxs);
				}
			}
			
			if (fDoAutoscroll && addedItem != null) {
				fTable.showItem(addedItem);
			}
			fTable.setRedraw(true);
		}
	}
	
	
	private class FilterEmptyAction extends Action {
		
		FilterEmptyAction() {
			setText(Messages.FilterEmptyAction_name);
			setToolTipText(Messages.FilterEmptyAction_tooltip);
			setImageDescriptor(StatetImages.getDescriptor(StatetImages.LOCTOOL_FILTER));
			
			setChecked(fDoFilterEmpty);
			run();
		}
		
		@Override
		public void run() {
			final boolean switchOn = isChecked();
			fDoFilterEmpty = switchOn;
			if (switchOn) {
				addFilter(EMPTY_FILTER); 
			}
			else {
				removeFilter(EMPTY_FILTER);
			}
		}
		
	}
	
	
	private volatile ToolProcess fProcess; // note: we write only in ui thread
	private IToolRegistryListener fToolRegistryListener;
	private final ViewContentProvider fContentProvider;
	
	private Table fTable;
	private Clipboard fClipboard;
	
	private static final String M_FILTER_EMPTY = "HistoryView.FilterEmpty"; //$NON-NLS-1$
	private boolean fDoFilterEmpty;
	private Action fFilterEmptyAction;
	private final FastList<EntryFilter> fFilter = new FastList<EntryFilter>(EntryFilter.class, FastList.IDENTITY);
	
	private static final String M_AUTOSCROLL = "HistoryView.Autoscroll"; //$NON-NLS-1$
	private boolean fDoAutoscroll;
	private Action fScrollLockAction;
	
	private final FastList<IToolRetargetable> fToolListenerList = new FastList<IToolRetargetable>(IToolRetargetable.class);
	
	private Action fSelectAllAction;
	private Action fCopyAction;
	private Action fSubmitAction;
	
	private IHandler2 fSearchStartHandler;
	private IHandler2 fSearchNextHandler;
	private IHandler2 fSearchPrevHandler;
	private SearchContributionItem fSearchTextItem;
	private final SearchPattern fSearchPattern = new SearchPattern(SearchPattern.RULE_EXACT_MATCH
			| SearchPattern.RULE_PREFIX_MATCH | SearchPattern.RULE_CAMELCASE_MATCH
			| SearchPattern.RULE_PATTERN_MATCH | SearchPattern.RULE_BLANK_MATCH);
	
	private LoadHistoryAction fLoadHistoryAction;
	private SaveHistoryAction fSaveHistoryAction;
	
	private final ViewReloadJob fReloadJob;
	
	
	/**
	 * The constructor.
	 */
	public HistoryView() {
		fReloadJob = new ViewReloadJob();
		fContentProvider = new ViewContentProvider();
	}
	
	
	@Override
	public void init(final IViewSite site, final IMemento memento) throws PartInitException {
		super.init(site, memento);
		
		final String autoscroll = (memento != null) ? memento.getString(M_AUTOSCROLL) : null;
		if (autoscroll == null || autoscroll.equals("on")) { // default  //$NON-NLS-1$
			fDoAutoscroll = true;
		} else {
			fDoAutoscroll = false;
		}
		
		final String filterEmpty = (memento != null) ? memento.getString(M_FILTER_EMPTY) : null;
		if (filterEmpty == null || filterEmpty.equals("off")) { // default  //$NON-NLS-1$
			fDoFilterEmpty = false;
		} else {
			fDoFilterEmpty = true;
		}
		
	}
	
	@Override
	public void saveState(final IMemento memento) {
		super.saveState(memento);
		
		memento.putString(M_AUTOSCROLL, (fDoAutoscroll) ? "on" : "off"); //$NON-NLS-1$ //$NON-NLS-2$
		memento.putString(M_FILTER_EMPTY, (fDoFilterEmpty) ? "on" : "off"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(LayoutUtil.applySashDefaults(new GridLayout(), 1));
		
		fTable = new Table(parent, SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION);
		fTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fTable.setLinesVisible(false);
		fTable.setHeaderVisible(false);
		new DefaultToolTip(fTable) {
			
			private DateFormat fFormat = DateFormat.getDateTimeInstance();
			
			@Override
			protected String getText(final Event event) {
				final TableItem item = fTable.getItem(new Point(event.x, event.y));
				if (item != null) {
					final Entry e = (Entry) item.getData();
					if (e.getTimeStamp() < 0) {
						return "[-]\n"+e.getCommand(); //$NON-NLS-1$
					}
					else {
						return "["+fFormat.format(new Date(e.getTimeStamp()))+"]\n" + e.getCommand(); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				return null;
			}
			
		};
		final TableColumn column = new TableColumn(fTable, SWT.DEFAULT);
		fTable.addListener(SWT.Resize, new Listener() {
			public void handleEvent(final Event event) {
				// adapt the column width to the width of the table
				final int tableWidth = fTable.getClientArea().width;
				if (tableWidth == 0) {
					return;
				}
				column.setWidth(tableWidth);
			}
		});
		fTable.addKeyListener(new KeyListener() {
			public void keyPressed(final KeyEvent e) {
				if (e.keyCode == SWT.ARROW_UP && 
						fTable.getSelectionCount() == 1 && fTable.getSelectionIndex() == 0) {
					fSearchTextItem.show();
					fTable.deselectAll();
					e.doit = false;
				}
			}
			public void keyReleased(final KeyEvent e) {
			}
		});
		
		createActions();
		hookContextMenu();
		contributeToActionBars();
		
		final DragSource dragSource = new DragSource(fTable, DND.DROP_COPY);
		dragSource.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		dragSource.addDragListener(new HistoryDragAdapter(this));
		
		// listen on console changes
		final IToolRegistry toolRegistry = NicoUI.getToolRegistry();
		fToolRegistryListener = new IToolRegistryListener() {
			public void toolSessionActivated(final ToolSessionUIData sessionData) {
				final ToolProcess process = sessionData.getProcess();
				UIAccess.getDisplay().syncExec(new Runnable() {
					public void run() {
						connect(process);
					}
				});
			}
			public void toolTerminated(final ToolSessionUIData sessionData) {
				final ToolProcess process = sessionData.getProcess();
				UIAccess.getDisplay().syncExec(new Runnable() {
					public void run() {
						if (fProcess != null && fProcess == process) {
							connect(null);
						}
					}
				});
			}
		};
		toolRegistry.addListener(fToolRegistryListener, getViewSite().getPage());
		connect(toolRegistry.getActiveToolSession(getViewSite().getPage()).getProcess());
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "de.walware.statet.nico.ui.cmd_history_view"); //$NON-NLS-1$
	}
	
	private void updateItem(final TableItem item) {
		final Entry e = (Entry) item.getData();
		item.setImage(StatetImages.getImage(StatetImages.OBJ_CONSOLECOMMAND));
		item.setText(e.getCommand());
	}
	
	private void createActions() {
		final IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
		
		fFilterEmptyAction = new FilterEmptyAction();
		fScrollLockAction = new ScrollLockAction(new Receiver() {
			public void setAutoScroll(final boolean enabled) {
				fDoAutoscroll = enabled;
			}
		}, !fDoAutoscroll);
		fSelectAllAction = new Action() {
			@Override
			public void run() {
				fTable.selectAll();
			}
		};
		
		fCopyAction = new HistoryCopyAction(this);
		fSubmitAction = new HistorySubmitAction(this);
		
		enabledSelectionActions(false);
		fTable.addSelectionListener(new SelectionListener() {
			public void widgetSelected(final SelectionEvent e) {
				if (fTable.getSelectionCount() > 0) {
					enabledSelectionActions(true);
				}
				else {
					enabledSelectionActions(false);
				}
			};
			public void widgetDefaultSelected(final SelectionEvent e) {
				fSubmitAction.run();
			}
		} );
		
		fLoadHistoryAction = new LoadHistoryAction(this);
		fSaveHistoryAction = new SaveHistoryAction(this);
		
		fSearchStartHandler = new AbstractHandler() {
			public Object execute(final ExecutionEvent arg0) {
				fSearchTextItem.show();
				return null;
			}
		};
		handlerService.activateHandler(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE, fSearchStartHandler);
		
		fSearchPrevHandler = new AbstractHandler() {
			public Object execute(final ExecutionEvent arg0) {
				search(false, -1);
				return null;
			}
		};
		handlerService.activateHandler(IWorkbenchActionDefinitionIds.FIND_PREVIOUS, fSearchPrevHandler);
		handlerService.activateHandler("org.eclipse.ui.navigate.previous", fSearchPrevHandler); //$NON-NLS-1$
		
		fSearchNextHandler = new AbstractHandler() {
			public Object execute(final ExecutionEvent arg0) {
				search(true, -1);
				return null;
			}
		};
		handlerService.activateHandler(IWorkbenchActionDefinitionIds.FIND_NEXT, fSearchNextHandler);
		handlerService.activateHandler("org.eclipse.ui.navigate.next", fSearchNextHandler); //$NON-NLS-1$
	}
	
	protected void enabledSelectionActions(final boolean enable) {
		fCopyAction.setEnabled(enable);
		fSubmitAction.setEnabled(enable);
	}
	
	
	private void hookContextMenu() {
		final MenuManager menuMgr = new MenuManager("ContextMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(final IMenuManager manager) {
				HistoryView.this.fillContextMenu(manager);
			}
		});
		final Menu menu = menuMgr.createContextMenu(fTable);
		fTable.setMenu(menu);
//		getSite().registerContextMenu(menuMgr, fTableViewer);
	}
	
	private void contributeToActionBars() {
		final IActionBars bars = getViewSite().getActionBars();
		
		bars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), fSelectAllAction);
		bars.setGlobalActionHandler(ActionFactory.COPY.getId(), fCopyAction);
				
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}
	
	private void fillLocalPullDown(final IMenuManager manager) {
		manager.add(fLoadHistoryAction);
		manager.add(fSaveHistoryAction);
		manager.add(new Separator());
		manager.add(fFilterEmptyAction);
		manager.add(fScrollLockAction);
		manager.add(new Separator());
	}
	
	private void fillContextMenu(final IMenuManager manager) {
		manager.add(fCopyAction);
		manager.add(fSubmitAction);
		
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(final IToolBarManager manager) {
		fSearchTextItem = new SearchContributionItem("search.text", false) { //$NON-NLS-1$
			@Override
			protected void search() {
				HistoryView.this.search(true, -1);
			}
		};
		fSearchTextItem.setToolTip(Messages.HistorySearch_Pattern_tooltip);
		fSearchTextItem.setSizeControl(fTable.getParent());
		fSearchTextItem.setResultControl(fTable);
		
		manager.add(fSearchTextItem);
		
		manager.add(new HandlerContributionItem(new CommandContributionItemParameter(
				getSite(), "search.next", IWorkbenchActionDefinitionIds.FIND_NEXT, null, //$NON-NLS-1$
				StatetImages.getDescriptor(StatetImages.LOCTOOL_DOWN), null, StatetImages.getDescriptor(StatetImages.LOCTOOL_DOWN_H),
				Messages.HistorySearch_NextMatch_tooltip, null, null, SWT.PUSH, null, false), fSearchNextHandler));
		manager.add(new HandlerContributionItem(new CommandContributionItemParameter(
				getSite(), "search.previous", IWorkbenchActionDefinitionIds.FIND_PREVIOUS, null, //$NON-NLS-1$
				StatetImages.getDescriptor(StatetImages.LOCTOOL_UP), null, StatetImages.getDescriptor(StatetImages.LOCTOOL_UP_H),
				Messages.HistorySearch_PreviousMatch_tooltip, null, null, SWT.PUSH, null, false), fSearchPrevHandler));
		
		manager.add(new Separator());
		
		manager.add(fScrollLockAction);
	}
	
	/** May only be called in UI thread */
	public void connect(final ToolProcess process) {
		if (fProcess == process) {
			return;
		}
		if (fProcess != null) {
			fProcess.getHistory().removeListener(fContentProvider);
		}
		fProcess = process;
		if (fProcess != null) {
			fProcess.getHistory().addListener(fContentProvider);
		}
		scheduleRefresh(true);
		for (final IToolRetargetable action : fToolListenerList.toArray()) {
			action.setTool(fProcess);
		}
	}
	
	private void scheduleRefresh(final boolean change) {
		final IWorkbenchSiteProgressService context = (IWorkbenchSiteProgressService) getSite().getAdapter(IWorkbenchSiteProgressService.class);
		if (change) {
			fReloadJob.cancel();
			context.schedule(fReloadJob, 200);
		}
		else {
			context.schedule(fReloadJob, 0);
		}
	}
	
	public void addToolRetargetable(final IToolRetargetable action) {
		fToolListenerList.add(action);
	}
	
	public void removeToolRetargetable(final IToolRetargetable action) {
		fToolListenerList.remove(action);
	}
	
	public void addFilter(final EntryFilter filter) {
		fFilter.add(filter);
		scheduleRefresh(false);
	}
	
	public void removeFilter(final EntryFilter filter) {
		fFilter.remove(filter);
		scheduleRefresh(false);
	}
	
	public void search(final String pattern, final boolean forward) {
		fSearchTextItem.getSearchText().setText(pattern);
		search(forward, forward ? 0 : fTable.getItemCount()-1);
	}
	
	
	/**
	 * Returns the tool process, which this view is connected to.
	 * 
	 * @return a tool process or <code>null</code>, if no process is connected.
	 */
	public ToolProcess getTool() {
		return fProcess;
	}
	
	/**
	 * Returns a shared clipboard resource, which can be used by actions of this view.
	 * 
	 * @return a clipboard object.
	 */
	public Clipboard getClipboard() {
		if (fClipboard == null)
			fClipboard = new Clipboard(Display.getCurrent());
		
		return fClipboard;
	}
	
	public Entry[] getSelection() {
		final TableItem[] items = fTable.getSelection();
		final int n = items.length;
		final Entry[] selection = new Entry[n];
		for (int i = 0; i < n; i++) {
			selection[i] = (Entry) items[i].getData();
		}
		return selection;
	}
	
	@Override
	public void setFocus() {
		// Passing the focus request to the viewer's control.
		fTable.setFocus();
	}
	
	private void search(final boolean forward, final int startIdx) {
		if (!UIAccess.isOkToUse(fTable)) {
			return;
		}
		
		final int itemCount = fTable.getItemCount();
		final String text = fSearchTextItem.getText();
		if (itemCount == 0 || text.length() == 0) {
			return;
		}
		
		int start = 0;
		do {
			final char c = text.charAt(start);
			if (c == ' ' || c == '\t') {
				start++;
			}
			else {
				break;
			}
		} while (start < text.length());
		fSearchPattern.setPattern(text.substring(start));
		
		int idx;
		if (startIdx < 0) {
			idx = fTable.getSelectionIndex();
		}
		else {
			idx = (forward) ? startIdx-1 : startIdx+1;
		}
		if (forward) {
			idx++;
			while (idx < itemCount) {
				final Entry e = (Entry) fTable.getItem(idx).getData();
				final int offset = e.getCommandMarker();
				if (fSearchPattern.matches(e.getCommand().substring(
						offset >= 0 ? offset : -1-offset))) {
					fTable.setSelection(idx);
					return;
				}
				idx++;
			}
		}
		else {
			idx--;
			while (idx >= 0) {
				final Entry e = (Entry) fTable.getItem(idx).getData();
				final int offset = e.getCommandMarker();
				if (fSearchPattern.matches(e.getCommand().substring(
						offset >= 0 ? offset : -1-offset))) {
					fTable.setSelection(idx);
					return;
				}
				idx--;
			}
		}
		Display.getCurrent().beep();
	}
	
	@Override
	public void dispose() {
		if (fToolRegistryListener != null) {
			NicoUI.getToolRegistry().removeListener(fToolRegistryListener);
			fToolRegistryListener = null;
		}
		fReloadJob.cancel();
		final ToolProcess process = fProcess;
		if (process != null) {
			process.getHistory().removeListener(fContentProvider);
		}
		fToolListenerList.clear();
		fProcess = null;
		fCopyAction = null;
		fSubmitAction = null;
		fLoadHistoryAction = null;
		fSaveHistoryAction = null;
		if (fSearchStartHandler != null) {
			fSearchStartHandler.dispose();
			fSearchStartHandler = null;
		}
		if (fSearchPrevHandler != null) {
			fSearchPrevHandler.dispose();
			fSearchPrevHandler = null;
		}
		if (fSearchNextHandler != null) {
			fSearchNextHandler.dispose();
			fSearchNextHandler = null;
		}
		
		super.dispose();
		
		if (fClipboard != null) {
			fClipboard.dispose();
			fClipboard = null;
		}
		
		fTable = null;
	}
	
}
