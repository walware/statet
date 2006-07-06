/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.views;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

import de.walware.eclipsecommons.ui.dialogs.Layouter;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.Queue;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.IToolRegistry;
import de.walware.statet.nico.ui.IToolRegistryListener;
import de.walware.statet.nico.ui.IToolRunnableAdapter;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.ToolSessionUIData;
import de.walware.statet.nico.ui.actions.PauseAction;
import de.walware.statet.ui.StatetImages;


/**
 * A view for the queue of a tool process.
 *
 * Usage: This class is not intended to be subclassed.
 */
public class QueueView extends ViewPart {

	
	private class ViewContentProvider implements IStructuredContentProvider, IDebugEventSetListener {

		private volatile boolean fExpectInfoEvent = false;
		private IToolRunnable[] fRefreshData;
		
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
			if (oldInput != null) {
				if (newInput == null) {
					unregister();
				}

//				ToolProcess oldProcess = (ToolProcess) oldInput;

				fPauseAction.disconnect();
			}
			if (newInput != null) {
				ToolProcess newProcess = (ToolProcess) newInput;

				fPauseAction.connect(newProcess);
				
				DebugPlugin manager = DebugPlugin.getDefault();
				if (manager != null) {
					manager.addDebugEventListener(this);
				}
			}
		}

		public Object[] getElements(Object inputElement) {

			IToolRunnable[] elements;
			if (fRefreshData != null) {
				elements = fRefreshData;
				fRefreshData = null;
			}
			else {
				elements = new IToolRunnable[0];
				Queue queue = getQueue();
				if (queue != null) {
					fExpectInfoEvent = true;
					queue.sendElements();
				}
			}
			return elements;
		}
		
		private void unregister() {
			
			DebugPlugin manager = DebugPlugin.getDefault();
			if (manager != null) {
				manager.removeDebugEventListener(this);
			}
		}

		public void dispose() {
			
			unregister();
		}
		
		private void setElements(final IToolRunnable[] elements) {
			
			UIAccess.getDisplay().syncExec(new Runnable() {
				public void run() {
					if (!Layouter.isOkToUse(fTableViewer)) {
						return;
					}
					fRefreshData = elements;
					fTableViewer.refresh();
				}
			});
		}
		
		public void handleDebugEvents(DebugEvent[] events) {
			for (int i = 0; i < events.length; i++) {
				DebugEvent event = events[i];
				switch (event.getKind()) {
				case DebugEvent.MODEL_SPECIFIC:
					switch (event.getDetail()) {
					
					case ToolProcess.QUEUE_ENTRIES_ADDED:
						if (!fExpectInfoEvent) {
							final IToolRunnable[] entries = (IToolRunnable[]) event.getData();
							
							if (events.length > i+1 && entries.length == 1) {
								// Added and removed in same set
								DebugEvent next = events[i+1];
								if (next.getKind() == DebugEvent.MODEL_SPECIFIC
										&& next.getDetail() == ToolProcess.QUEUE_ENTRY_STARTED_PROCESSING
										&& next.getData() == entries[0]) {
									i++;
									break;
								}
							}
						
							UIAccess.getDisplay().syncExec(new Runnable() {
								public void run() {
									if (!Layouter.isOkToUse(fTableViewer)) {
										return;
									}
									fTableViewer.add(entries);
								}
							});
						}
						break;

					case ToolProcess.QUEUE_ENTRY_STARTED_PROCESSING:
						if (!fExpectInfoEvent) {
							final IToolRunnable entry = (IToolRunnable) event.getData();
							UIAccess.getDisplay().syncExec(new Runnable() {
								public void run() {
									if (!Layouter.isOkToUse(fTableViewer)) {
										return;
									}
									fTableViewer.remove(entry);
								}
							});
						}
						break;
						
					case ToolProcess.QUEUE_COMPLETE_CHANGE:
						if (!fExpectInfoEvent) {
							setElements((IToolRunnable[]) event.getData());
						}
						break;
					
					case ToolProcess.QUEUE_COMPLETE_INFO:
						if (fExpectInfoEvent) {
							fExpectInfoEvent = false;
							setElements((IToolRunnable[]) event.getData());
						}
						break;
						
					default:
						break;
					}
					break;
					
				case DebugEvent.TERMINATE:
					if (fProcess != null && event.getSource() == fProcess) {
						fPauseAction.disconnect();
					}
					break;
				}
			}
		}
	}
	
	private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			
			if (columnIndex == 0) {
				return getImage(element);
			}
			return null;
		}
		
		@Override
		public Image getImage(Object element) {
			
			IToolRunnable runnable = (IToolRunnable) element;
			IToolRunnableAdapter adapter = getAdapter(runnable);
			if (adapter != null) {
				ImageDescriptor descriptor = adapter.getImageDescriptor();
				if (descriptor != null) {
					return StatetImages.getCachedImage(descriptor);
				}
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {

			if (columnIndex == 0) {
				return getText(element);
			}
			return ""; //$NON-NLS-1$
		}
		
		public String getText(Object element) {
			
			IToolRunnable runnable = (IToolRunnable) element;
			return runnable.getLabel();
		}
		
	    protected IToolRunnableAdapter getAdapter(IToolRunnable runnable) {
	    	
	        if (!(runnable instanceof IAdaptable)) {
	            return null;
	        }
	        return (IToolRunnableAdapter) ((IAdaptable) runnable)
	                .getAdapter(IToolRunnableAdapter.class);
	    }
	}
	
	
	private TableViewer fTableViewer;
	
	private ToolProcess fProcess; // f�r submit
	private IToolRegistryListener fToolRegistryListener;
	
	private PauseAction fPauseAction;
	
	private Action fSelectAllAction;
	private Action fDeleteAction;
	
	
	@Override
	public void createPartControl(Composite parent) {
		
		fTableViewer = new TableViewer(parent, SWT.MULTI | SWT.V_SCROLL);
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
		contributeToActionBars();
		
		// listen on console changes
		IToolRegistry toolRegistry = NicoUITools.getRegistry();
		connect(toolRegistry.getActiveToolSession(getViewSite().getPage()).getProcess());
		fToolRegistryListener = new IToolRegistryListener() {
			public void toolSessionActivated(ToolSessionUIData info) {
				final ToolProcess process = info.getProcess();
				UIAccess.getDisplay().asyncExec(new Runnable() {
					public void run() {
						connect(process);
					}
				});
			}
			public void toolSessionClosed(ToolSessionUIData info) { }
		};
		toolRegistry.addListener(fToolRegistryListener, getViewSite().getPage());
	}
	
	private void createActions() {
		
		fPauseAction = new PauseAction();
		
		fSelectAllAction = new Action() {
			public void run() {
				fTableViewer.getTable().selectAll();
			}
		};
		fDeleteAction = new Action() {
			public void run() {
				Queue queue = getQueue();
				if (queue != null) {
					IStructuredSelection selection = (IStructuredSelection) fTableViewer.getSelection();
					Object[] elements = selection.toArray();
					queue.removeElements(elements);
				}
			}
		};
	}

	private void contributeToActionBars() {
		
		IActionBars bars = getViewSite().getActionBars();
		
		bars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), fSelectAllAction);
		bars.setGlobalActionHandler(ActionFactory.DELETE.getId(), fDeleteAction);
		
//		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		
		manager.add(fPauseAction);
	}
	
	/** Should only be called inside UI Thread */
	public void connect(final ToolProcess process) {
		
		Runnable runnable = new Runnable() {
			public void run() {
				if (!Layouter.isOkToUse(fTableViewer)) {
					return;
				}
				fProcess = process;
				fTableViewer.setInput(fProcess);
			}
		};
		BusyIndicator.showWhile(UIAccess.getDisplay(), runnable);
	}
	
	/**
	 * Returns the tool process, which this view is connected to.
	 * 
	 * @return a tool process or <code>null</code>, if no process is connected.
	 */
	public ToolProcess getProcess() {
		
		return fProcess;
	}
	
	public Queue getQueue() {
		
		if (fProcess != null) {
			return fProcess.getQueue();
		}
		return null;
	}
	
	
	@Override
	public void setFocus() {
		// Passing the focus request to the viewer's control.

		fTableViewer.getControl().setFocus();
	}
	
	@Override
	public void dispose() {
		
		fPauseAction.dispose();
		fPauseAction = null;
		
		super.dispose();
	}
	
}
