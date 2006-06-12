/*******************************************************************************
 * Copyright (c) 2005-2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
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

import de.walware.eclipsecommon.ui.dialogs.Layouter;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.nico.core.runtime.History;
import de.walware.statet.nico.core.runtime.IHistoryListener;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.History.Entry;
import de.walware.statet.nico.ui.IToolRegistry;
import de.walware.statet.nico.ui.IToolRegistryListener;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.ToolSessionInfo;
import de.walware.statet.nico.ui.console.ScrollLockAction;
import de.walware.statet.nico.ui.console.ScrollLockAction.Receiver;
import de.walware.statet.nico.ui.internal.actions.HistoryCopyAction;
import de.walware.statet.nico.ui.internal.actions.HistoryDragAdapter;
import de.walware.statet.nico.ui.internal.actions.HistorySubmitAction;
import de.walware.statet.ui.SharedMessages;
import de.walware.statet.ui.StatetImages;


/**
 * A view for the history of a tool process.
 * 
 * Usage: This class is not intend to be subclassed.
 */
public class HistoryView extends ViewPart {
	
	
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
	

	private class ViewContentProvider implements IStructuredContentProvider, IHistoryListener {
		
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {

			if (oldInput != null) {
				((History) oldInput).removeListener(this);
			}
			if (newInput != null) {
				((History) newInput).addListener(this);
			}
		}
		
		public Object[] getElements(Object parent) {
			
			if (fProcess == null) {
				return new History.Entry[0];
			}
			return fProcess.getHistory().toArray();
		}

		public void dispose() {
		}

		
		public void entryAdded(final Entry e) {
			// history event
			
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					if (fProcess.getHistory() != e.getHistory()
							|| !Layouter.isOkToUse(fTableViewer)) {
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

		public void entryRemoved(final Entry e) {
			// history event. 
			
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					if (!Layouter.isOkToUse(fTableViewer)) {
						return;
					}
					fTableViewer.remove(e);
				}
			});
		}

		public void completeChange() { 
			// history event
			// Not tested yet!
			
			fTableViewer.refresh(true);
//			packTable(); //???
		}
	}
	
	private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		
		public String getColumnText(Object obj, int index) {
			
			Entry e = (Entry) obj;
			return e.getCommand();
		}
		
		public Image getColumnImage(Object obj, int index) {
			
			if (index != 0)
				return null;
			
			return StatetImages.getDefault().getImage(StatetImages.IMG_OBJ_COMMAND);
		}
	}
	
	private class NameSorter extends ViewerSorter {
		
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			
			return collator.compare(
					((Entry) e1).getCommand(), 
					((Entry) e2).getCommand() );
		}
	}

	private class ToggleSortAction extends Action {
		
		ToggleSortAction() {
			
			setText(SharedMessages.ToggleSortAction_name);
			setToolTipText(SharedMessages.ToggleSortAction_tooltip);
			setImageDescriptor(StatetImages.getDefault().getDescriptor(
					StatetImages.IMG_LOCTOOL_SORT_ALPHA) );
			
			setChecked(fDoSortAlpha);
		}
		
		@Override
		public void run() {
			
			boolean switchOn = isChecked();
			fDoSortAlpha = switchOn;
			if (switchOn)
				fTableViewer.setSorter(fNameSorter);
			else
				fTableViewer.setSorter(null);
		}
	}
	

	private TableViewer fTableViewer;
	private Clipboard fClipboard;

	private static final String M_SORT_ALPHA = "HistoryView.SortAlpha";
	private boolean fDoSortAlpha;
	private NameSorter fNameSorter = new NameSorter();
	private Action fToggleSortAction;

	private static final String M_AUTOSCROLL = "HistoryView.Autoscroll";
	private boolean fDoAutoscroll;
	private Action fScrollLockAction;
	
	private Action fSelectAllAction;
	private HistoryCopyAction fCopyAction;
	
	private Action fSubmitAction;

	private ToolProcess fProcess; // für submit
	private IToolRegistryListener fToolRegistryListener;

	
	/**
	 * The constructor.
	 */
	public HistoryView() {
	}
	
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		
		super.init(site, memento);
		
		String autoscroll = (memento != null) ? memento.getString(M_AUTOSCROLL) : null;
		if (autoscroll != null && autoscroll.equals("off")) {
			fDoAutoscroll = false;
		} else { // default
			fDoAutoscroll = true;
		}

		String sortAlpha = (memento != null) ? memento.getString(M_SORT_ALPHA) : null;
		if (sortAlpha != null && sortAlpha.equals("on")) {
			fDoSortAlpha = true;
		} else { // default
			fDoSortAlpha = false;
		}
	}
	
	@Override
	public void saveState(IMemento memento) {
		
		super.saveState(memento);
		
		memento.putString(M_AUTOSCROLL, (fDoAutoscroll) ? "on" : "off");
		memento.putString(M_SORT_ALPHA, (fDoSortAlpha) ? "on" : "off");
	}

	public void createPartControl(Composite parent) {
		
		fTableViewer = new TableViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION);
		fTableViewer.getTable().setLinesVisible(false);
		fTableViewer.getTable().setHeaderVisible(false);
		new TableColumn(fTableViewer.getTable(), SWT.DEFAULT);
		fTableViewer.getTable().addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				// adapt the column width to the width of the table 
				Table table = fTableViewer.getTable();
				Rectangle area = table.getClientArea();
				TableColumn column = table.getColumn(0);
				column.setWidth(area.width-3); // it looks better with a small gap
			}
		});
		
		fTableViewer.setContentProvider(new ViewContentProvider());
		fTableViewer.setLabelProvider(new TableLabelProvider());
		
		createActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();

		fTableViewer.addDragSupport(
				DND.DROP_COPY, 
				new Transfer[] { TextTransfer.getInstance() }, 
				new HistoryDragAdapter(this));

		// listen on console changes
		IToolRegistry toolRegistry = NicoUITools.getRegistry();
		connect(toolRegistry.getActiveToolSession(getViewSite().getPage()).getProcess());
		fToolRegistryListener = new IToolRegistryListener() {
			public void toolSessionActivated(ToolSessionInfo info) {
				final ToolProcess process = info.getProcess();
				StatetPlugin.getDisplay().asyncExec(new Runnable() {
					public void run() {
						connect(process);
					}
				});
			}
			public void toolSessionClosed(ToolSessionInfo info) { }
		};
		toolRegistry.addListener(fToolRegistryListener, getViewSite().getPage());
	}

	private void createActions() {
		
		fToggleSortAction = new ToggleSortAction();
		fScrollLockAction = new ScrollLockAction(new Receiver() {
			public void setAutoScroll(boolean enabled) {
				fDoAutoscroll = enabled;
			}
		}, !fDoAutoscroll);
		fSelectAllAction = new Action() {
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
	}

	protected void enabledSelectionActions(boolean enable) {
		
		fCopyAction.setEnabled(enable);
		fSubmitAction.setEnabled(enable);
	}


	private void hookContextMenu() {
		
		MenuManager menuMgr = new MenuManager("#PopupMenu");
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
		
		manager.add(fToggleSortAction);
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
		
		Runnable runnable = new Runnable() {
			public void run() {
				if (!Layouter.isOkToUse(fTableViewer)) {
					return;
				}
				fProcess = process;
				fTableViewer.setInput((fProcess != null) ? 
						fProcess.getHistory() : null);
			}
		};
		BusyIndicator.showWhile(StatetPlugin.getDisplay(), runnable);
	}
	
	/**
	 * Returns the tool process, which this view is connected to.
	 * 
	 * @return a tool process or <code>null</code>, if no process is connected.
	 */
	public ToolProcess getProcess() {
		
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
			NicoUITools.getRegistry().removeListener(fToolRegistryListener);
			fToolRegistryListener = null;
		}
		if (fCopyAction != null) {
			fCopyAction.dispose();
			fCopyAction = null;
		}
		
		super.dispose();

		if (fClipboard != null) {
			fClipboard.dispose();
			fClipboard = null;
		}
		
		fTableViewer = null;
	}
	
}
