/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import de.walware.eclipsecommons.ui.SharedMessages;
import de.walware.eclipsecommons.ui.util.UIAccess;

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
	
	
	/**
	 * Converts the selection of this view/viewer into a commmand text block.
	 * 
	 * @param selection a selection with history entries.
	 * @return command block.
	 */
	public static String createTextBlock(IStructuredSelection selection) {
		Object[] elements = selection.toArray();
		StringBuilder text = new StringBuilder(elements.length * 8);
		for (Object obj : elements) {
			Entry e = (Entry) obj;
			text.append(e.getCommand());
			text.append('\n');
		}
		
		return text.toString();
	}
	
	
	private static class TableLabelProvider extends CellLabelProvider {
		
		private DateFormat fFormat = DateFormat.getDateTimeInstance();
		
		@Override
		public void update(ViewerCell cell) {
			cell.setImage(StatetImages.getImage(StatetImages.OBJ_COMMAND));
			cell.setText(((Entry) cell.getElement()).getCommand());
		}
		
		@Override
		public boolean useNativeToolTip(Object object) {
			return true;
		}
		
		@Override
		public String getToolTipText(Object element) {
			Entry entry = (Entry) element;
			if (entry.getTimeStamp() < 0) {
				return " - | "+entry.getCommand(); //$NON-NLS-1$
			}
			return fFormat.format(new Date(entry.getTimeStamp())) + " | " + entry.getCommand(); //$NON-NLS-1$
		}
	}
	
	private static class EntryComparator extends ViewerComparator {
		@SuppressWarnings("unchecked")
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			return getComparator().compare(((Entry) e1).getCommand(), ((Entry) e2).getCommand());
		}
	}
	
	
	private class ViewJob extends Job {
		
		ViewJob() {
			super("Update History View"); //$NON-NLS-1$
			setPriority(SHORT);
			setUser(false);
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			ToolProcess process = fProcess;
			if (process == null || monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
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
			UIAccess.getDisplay().syncExec(new Runnable() {
				public void run() {
					if (fProcess == null || fProcess.getHistory() != history || !UIAccess.isOkToUse(fTableViewer)) {
						return;
					}
					fNewEntrys = entries;
					fTableViewer.refresh(false);
					fNewEntrys = null;
					fReloadScheduled = false;
				}
			});
			return Status.OK_STATUS;
		}
	}
	
	private class ViewContentProvider implements IStructuredContentProvider, IHistoryListener {
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		
		public Object[] getElements(Object parent) {
			if (fNewEntrys != null) {
				return fNewEntrys;
			}
			if (fProcess != null) {
				scheduleRefresh(false);
			}
			return new History.Entry[0];
		}
		
		public void dispose() {
		}
		
		
		public void entryAdded(final History source, final Entry e) {
			// history event
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (fReloadScheduled || fProcess == null || fProcess.getHistory() != source || !UIAccess.isOkToUse(fTableViewer)) {
						return;
					}
					
					fTableViewer.add(e);

					if (fDoAutoscroll)
						fTableViewer.reveal(e);
					else {
//						Table table = (Table) fViewer.getControl();
//						TableItem item = table.getItem(new Point(0, 0));
//						if (item != null)
//							table.showItem(item);
					}
				}
			});
		}
		
		public void entryRemoved(final History source, final Entry e) {
			// history event.
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (fReloadScheduled || fProcess == null || fProcess.getHistory() != source || !UIAccess.isOkToUse(fTableViewer)) {
						return;
					}
					fTableViewer.remove(e);
				}
			});
		}
		
		public void completeChange(final History source, final Entry[] es) {
			// history event
			UIAccess.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (fReloadScheduled || fProcess == null || fProcess.getHistory() != source || !UIAccess.isOkToUse(fTableViewer)) {
						return;
					}
					fNewEntrys = es;
					fTableViewer.refresh(false);
					fNewEntrys = null;
					//			packTable(); //???
				}
			});
		}
	}
	
	
	private class ToggleSortAction extends Action {
		
		ToggleSortAction() {
			setText(SharedMessages.ToggleSortAction_name);
			setToolTipText(SharedMessages.ToggleSortAction_tooltip);
			setImageDescriptor(StatetImages.getDescriptor(StatetImages.LOCTOOL_SORT_ALPHA));
			
			setChecked(fDoSortAlpha);
		}
		
		@Override
		public void run() {
			boolean switchOn = isChecked();
			fDoSortAlpha = switchOn;
			if (switchOn)
				fTableViewer.setComparator(fEntryComparator);
			else
				fTableViewer.setComparator(null);
		}
		
	}
	
	private class FilterEmptyAction extends Action {
		
		FilterEmptyAction() {
			setText(Messages.FilterEmptyAction_name);
			setToolTipText(Messages.FilterEmptyAction_tooltip);
			setImageDescriptor(StatetImages.getDescriptor(StatetImages.LOCTOOL_FILTER));
			
			setChecked(fDoFilterEmpty);
		}
		
		@Override
		public void run() {
			boolean switchOn = isChecked();
			fDoFilterEmpty = switchOn;
			if (switchOn) {
				ViewerFilter emptyFilter = new ViewerFilter() {
					@Override
					public boolean select(Viewer viewer, Object parentElement, Object element) {
						return !((History.Entry) element).isEmpty();
					}
				};
				fTableViewer.setFilters(new ViewerFilter[] { emptyFilter });
			}
			else {
				fTableViewer.setFilters(new ViewerFilter[0]);
			}
		}
		
	}
	
	
	private volatile ToolProcess fProcess;
	private IToolRegistryListener fToolRegistryListener;
	private ViewContentProvider fContentProvider;
	
	private TableViewer fTableViewer;
	private Clipboard fClipboard;
	
	private static final String M_SORT_ALPHA = "HistoryView.SortAlpha"; //$NON-NLS-1$
	private boolean fDoSortAlpha;
	private EntryComparator fEntryComparator = new EntryComparator();
	private Action fToggleSortAction;
	private static final String M_FILTER_EMPTY = "HistoryView.FilterEmpty"; //$NON-NLS-1$
	private boolean fDoFilterEmpty;
	private Action fFilterEmptyAction;
	
	private static final String M_AUTOSCROLL = "HistoryView.Autoscroll"; //$NON-NLS-1$
	private boolean fDoAutoscroll;
	private Action fScrollLockAction;
	
	private ListenerList fToolActions = new ListenerList();
	
	private Action fSelectAllAction;
	private Action fCopyAction;
	private Action fSubmitAction;
	
	private LoadHistoryAction fLoadHistoryAction;
	private SaveHistoryAction fSaveHistoryAction;
	
	private ToolProcess fNewProcess;
	private final ViewJob fReloadJob;
	private volatile boolean fReloadScheduled;
	private Entry[] fNewEntrys;
	
	/**
	 * The constructor.
	 */
	public HistoryView() {
		fReloadJob = new ViewJob();
		fContentProvider = new ViewContentProvider();
	}
	
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		
		String autoscroll = (memento != null) ? memento.getString(M_AUTOSCROLL) : null;
		if (autoscroll == null || autoscroll.equals("on")) { // default  //$NON-NLS-1$
			fDoAutoscroll = true;
		} else {
			fDoAutoscroll = false;
		}
		
		String sortAlpha = (memento != null) ? memento.getString(M_SORT_ALPHA) : null;
		if (sortAlpha == null || sortAlpha.equals("off")) { // default  //$NON-NLS-1$
			fDoSortAlpha = false;
		} else {
			fDoSortAlpha = true;
		}
		
		String filterEmpty = (memento != null) ? memento.getString(M_SORT_ALPHA) : null;
		if (filterEmpty == null || filterEmpty.equals("off")) { // default  //$NON-NLS-1$
			fDoFilterEmpty = false;
		} else {
			fDoFilterEmpty = true;
		}
	}
	
	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		
		memento.putString(M_AUTOSCROLL, (fDoAutoscroll) ? "on" : "off"); //$NON-NLS-1$ //$NON-NLS-2$
		memento.putString(M_SORT_ALPHA, (fDoSortAlpha) ? "on" : "off"); //$NON-NLS-1$ //$NON-NLS-2$
		memento.putString(M_FILTER_EMPTY, (fDoFilterEmpty) ? "on" : "off"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public void createPartControl(Composite parent) {
		fTableViewer = new TableViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION) {
			// we avoid refresh if no entries are available (e.g. switching sorting/filtering)
			@Override
			public void refresh() {
				if (fProcess == null || fNewEntrys != null) {
					super.refresh();
				}
				else {
					scheduleRefresh(false);
				}
			}
			@Override
			public void refresh(boolean updateLabels) {
				if (fProcess == null || fNewEntrys != null) {
					super.refresh(updateLabels);
				}
				else {
					scheduleRefresh(false);
				}
			}
		};
		fTableViewer.getTable().setLinesVisible(false);
		fTableViewer.getTable().setHeaderVisible(false);
		fTableViewer.setUseHashlookup(true);
		ColumnViewerToolTipSupport.enableFor(fTableViewer);
		TableViewerColumn column = new TableViewerColumn(fTableViewer, SWT.DEFAULT);
		column.setLabelProvider(new TableLabelProvider());
		fTableViewer.getTable().addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				// adapt the column width to the width of the table
				Table table = fTableViewer.getTable();
				Rectangle area = table.getClientArea();
				TableColumn column = table.getColumn(0);
				column.setWidth(area.width);
			}
		});
		
		fTableViewer.setContentProvider(new ViewContentProvider());
		fTableViewer.setInput(new Object());
		createActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
		fTableViewer.addDragSupport(
				DND.DROP_COPY,
				new Transfer[] { TextTransfer.getInstance() },
				new HistoryDragAdapter(this));
		
		// listen on console changes
		IToolRegistry toolRegistry = NicoUI.getToolRegistry();
		fToolRegistryListener = new IToolRegistryListener() {
			public void toolSessionActivated(ToolSessionUIData info) {
				final ToolProcess process = info.getProcess();
				UIAccess.getDisplay().syncExec(new Runnable() {
					public void run() {
						connect(process);
					}
				});
			}
			public void toolSessionClosed(ToolSessionUIData info) {
				final ToolProcess process = info.getProcess();
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
	}
	
	private void createActions() {
		fToggleSortAction = new ToggleSortAction();
		fFilterEmptyAction = new FilterEmptyAction();
		fScrollLockAction = new ScrollLockAction(new Receiver() {
			public void setAutoScroll(boolean enabled) {
				fDoAutoscroll = enabled;
			}
		}, !fDoAutoscroll);
		fSelectAllAction = new Action() {
			@Override
			public void run() {
				fTableViewer.getTable().selectAll();
			}
		};
		
		fCopyAction = new HistoryCopyAction(this);
		fSubmitAction = new HistorySubmitAction(this);
		
		enabledSelectionActions(false);
		fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection != null && !selection.isEmpty()) {
					enabledSelectionActions(true);
				}
				else {
					enabledSelectionActions(false);
				}
			}
		} );
		
		fLoadHistoryAction = new LoadHistoryAction(this);
		fSaveHistoryAction = new SaveHistoryAction(this);
	}
	
	protected void enabledSelectionActions(boolean enable) {
		
		fCopyAction.setEnabled(enable);
		fSubmitAction.setEnabled(enable);
	}
	
	
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("ContextMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				HistoryView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(fTableViewer.getControl());
		fTableViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, fTableViewer);
	}
	
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		
		bars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), fSelectAllAction);
		bars.setGlobalActionHandler(ActionFactory.COPY.getId(), fCopyAction);
				
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}
	
	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(fLoadHistoryAction);
		manager.add(fSaveHistoryAction);
		manager.add(new Separator());
		manager.add(fToggleSortAction);
		manager.add(fFilterEmptyAction);
		manager.add(fScrollLockAction);
		manager.add(new Separator());
	}
	
	private void fillContextMenu(IMenuManager manager) {
		manager.add(fCopyAction);
		manager.add(fSubmitAction);
		
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(fToggleSortAction);
		manager.add(fScrollLockAction);
	}
	
	private void hookDoubleClickAction() {
		fTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				fSubmitAction.run();
			}
		});
	}
	
	/** Should only be called inside UI Thread */
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
		if (fProcess != null) {
			scheduleRefresh(true);
		}
		else {
			fTableViewer.refresh();
		}
		for (Object action : fToolActions.getListeners()) {
			((IToolRetargetable) action).setTool(fProcess);
		}
	}
	
	private void scheduleRefresh(boolean change) {
		IWorkbenchSiteProgressService context = (IWorkbenchSiteProgressService) getSite().getAdapter(IWorkbenchSiteProgressService.class);
		fReloadScheduled = true;
		if (change) {
			fReloadJob.cancel();
			context.schedule(fReloadJob, 200);
		}
		else {
			context.schedule(fReloadJob, 0);
		}
	}
	
	public void addToolRetargetable(IToolRetargetable action) {
		fToolActions.add(action);
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
	 * Returns the table viewer, containing the entries of the history.
	 * 
	 * @return a table viewer.
	 */
	public TableViewer getTableViewer() {
		return fTableViewer;
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
	
	
	@Override
	public void setFocus() {
		// Passing the focus request to the viewer's control.
		fTableViewer.getControl().setFocus();
	}
	
	@Override
	public void dispose() {
		if (fToolRegistryListener != null) {
			NicoUI.getToolRegistry().removeListener(fToolRegistryListener);
			fToolRegistryListener = null;
		}
		fReloadJob.cancel();
		ToolProcess process = fProcess;
		if (process != null) {
			process.getHistory().removeListener(fContentProvider);
		}
		fToolActions.clear();
		fProcess = null;
		fCopyAction = null;
		fSubmitAction = null;
		fLoadHistoryAction = null;
		fSaveHistoryAction = null;
		
		super.dispose();
		
		if (fClipboard != null) {
			fClipboard.dispose();
			fClipboard = null;
		}
		
		fTableViewer = null;
	}
	
}
