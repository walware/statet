/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.views;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.StatetImages;
import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.Queue;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.internal.ui.Messages;
import de.walware.statet.nico.ui.IToolRegistry;
import de.walware.statet.nico.ui.IToolRegistryListener;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.ToolSessionUIData;
import de.walware.statet.nico.ui.actions.PauseAction;
import de.walware.statet.nico.ui.util.ToolProgressGroup;


/**
 * A view for the queue of a tool process.
 * 
 * Usage: This class is not intended to be subclassed.
 */
public class QueueView extends ViewPart {
	
	
	private class ViewContentProvider implements IStructuredContentProvider, IDebugEventSetListener {
		
		private volatile boolean fExpectInfoEvent = false;
		private IToolRunnable[] fRefreshData;
		
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			if (oldInput != null && newInput == null) {
				unregister();
				fPauseAction.setTool(null);
			}
			if (newInput != null) {
				final ToolProcess newProcess = (ToolProcess) newInput;
				
				fPauseAction.setTool(newProcess);
				
				final DebugPlugin manager = DebugPlugin.getDefault();
				if (manager != null) {
					manager.addDebugEventListener(this);
				}
			}
		}
		
		public Object[] getElements(final Object inputElement) {
			IToolRunnable[] elements;
			if (fRefreshData != null) {
				elements = fRefreshData;
				fRefreshData = null;
			}
			else {
				elements = new IToolRunnable[0];
				final Queue queue = getQueue();
				if (queue != null) {
					fExpectInfoEvent = true;
					queue.sendElements();
				}
			}
			return elements;
		}
		
		private void unregister() {
			final DebugPlugin manager = DebugPlugin.getDefault();
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
					if (!UIAccess.isOkToUse(fTableViewer)) {
						return;
					}
					fRefreshData = elements;
					fTableViewer.refresh();
				}
			});
		}
		
		public void handleDebugEvents(final DebugEvent[] events) {
			final ToolProcess process = fProcess;
			if (process == null) {
				return;
			}
			boolean updateProgress = false;
			final Queue queue = process.getQueue();
			EVENT: for (int i = 0; i < events.length; i++) {
				final DebugEvent event = events[i];
				final Object source = event.getSource();
				if (source == process) {
					if (event.getKind() == DebugEvent.TERMINATE) {
						fPauseAction.setTool(null);
					}
					continue EVENT;
				}
				if (source == queue) {
					switch (event.getKind()) {
					
					case DebugEvent.CHANGE:
						if (event.getDetail() != DebugEvent.CONTENT) {
							continue EVENT;
						}
						final Queue.Delta delta = (Queue.Delta) event.getData();
						switch (delta.type) {
						case Queue.ENTRIES_ADD:
							if (!fExpectInfoEvent) {
								if (events.length > i+1 && delta.data.length == 1) {
									// Added and removed in same set
									final DebugEvent next = events[i+1];
									if (next.getSource() == queue
											&& next.getKind() == DebugEvent.CHANGE
											&& next.getDetail() == DebugEvent.CONTENT) {
										final Queue.Delta nextDelta = (Queue.Delta) next.getData();
										if (nextDelta.type == Queue.ENTRY_START_PROCESSING
												&& delta.data[0] == nextDelta.data[0]) {
											updateProgress = true;
											i++;
											continue EVENT;
										}
									}
								}
								UIAccess.getDisplay().syncExec(new Runnable() {
									public void run() {
										if (!UIAccess.isOkToUse(fTableViewer)) {
											return;
										}
										fTableViewer.add(delta.data);
									}
								});
							}
							continue EVENT;
						
						case Queue.ENTRY_START_PROCESSING:
							updateProgress = true;
							// no break, continue with delete
						case Queue.ENTRIES_DELETE:
							if (!fExpectInfoEvent) {
								UIAccess.getDisplay().syncExec(new Runnable() {
									public void run() {
										if (!UIAccess.isOkToUse(fTableViewer)) {
											return;
										}
										fTableViewer.remove(delta.data);
									}
								});
							}
							continue EVENT;
						
//						case Queue.QUEUE_CHANGE:
//							if (!fExpectInfoEvent) {
//								setElements((IToolRunnable[]) event.getData());
//							}
//							continue EVENT;
						}
						continue EVENT;
						
					case DebugEvent.MODEL_SPECIFIC:
						if (event.getDetail() == Queue.QUEUE_INFO && fExpectInfoEvent) {
							fExpectInfoEvent = false;
							setElements((IToolRunnable[]) event.getData());
						}
						continue EVENT;
						
					case DebugEvent.TERMINATE:
						disconnect(process);
						continue EVENT;
						
					default:
						continue EVENT;
					}
				}
			}
			if (updateProgress && fShowProgress) {
				final ToolProgressGroup progress = fProgressControl;
				if (progress != null) {
					progress.refresh(false);
				}
			}
		}
	}
	
	private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		
		public Image getColumnImage(final Object element, final int columnIndex) {
			if (columnIndex == 0) {
				return getImage(element);
			}
			return null;
		}
		
		@Override
		public Image getImage(final Object element) {
			final ImageDescriptor descriptor = NicoUITools.getImageDescriptor((IToolRunnable) element);
			if (descriptor != null) {
				return StatetImages.getCachedImage(descriptor);
			}
			return null;
		}
		
		public String getColumnText(final Object element, final int columnIndex) {
			if (columnIndex == 0) {
				return getText(element);
			}
			return ""; //$NON-NLS-1$
		}
		
		@Override
		public String getText(final Object element) {
			final IToolRunnable runnable = (IToolRunnable) element;
			return runnable.getLabel();
		}
	}
	
	private class ShowDescriptionAction extends Action {
		
		public ShowDescriptionAction() {
			setText(Messages.ShowToolDescription_name);
			setToolTipText(Messages.ShowToolDescription_tooltip);
			setChecked(fShowDescription);
		}
		
		@Override
		public void run() {
			fShowDescription = isChecked();
			updateContentDescription(fProcess);
		}
	}
	
	private class ShowProgressAction extends Action {
		
		public ShowProgressAction() {
			setText(Messages.ShowProgress_name);
			setToolTipText(Messages.ShowProgress_tooltip);
			setChecked(fShowProgress);
		}
		
		@Override
		public void run() {
			fShowProgress = isChecked();
			if (fShowProgress) {
				createProgressControl();
				fProgressControl.setTool(fProcess, true);
				fProgressControl.getControl().moveAbove(fTableViewer.getControl());
			}
			else {
				if (fProgressControl != null) {
					fProgressControl.getControl().dispose();
					fProgressControl = null;
				}
			}
			fComposite.layout(true);
		}
	}
	
	
	private Composite fComposite;
	private ToolProgressGroup fProgressControl;
	private TableViewer fTableViewer;
	
	private ToolProcess fProcess;
	private IToolRegistryListener fToolRegistryListener;
	
	private PauseAction fPauseAction;
	
	private static final String M_SHOW_DESCRIPTION = "QueueView.ShowDescription"; //$NON-NLS-1$
	private boolean fShowDescription;
	private Action fShowDescriptionAction;
	
	private static final String M_SHOW_PROGRESS = "QueueView.ShowProgress"; //$NON-NLS-1$
	private boolean fShowProgress;
	private Action fShowProgressAction;
	
	private Action fSelectAllAction;
	private Action fDeleteAction;
	
	
	public QueueView() {
	}
	
	
	@Override
	public void init(final IViewSite site, final IMemento memento) throws PartInitException {
		super.init(site, memento);
		
		final String showDescription = (memento != null) ? memento.getString(M_SHOW_DESCRIPTION) : null;
		if (showDescription == null || showDescription.equals("off")) { // default  //$NON-NLS-1$
			fShowDescription = false;
		} else {
			fShowDescription = true;
		}
		
		final String showProgress = (memento != null) ? memento.getString(M_SHOW_PROGRESS) : null;
		if (showProgress== null || showProgress.equals("on")) { // default  //$NON-NLS-1$
			fShowProgress = true;
		} else {
			fShowProgress = false;
		}
	}
	
	@Override
	public void saveState(final IMemento memento) {
		super.saveState(memento);
		
		memento.putString(M_SHOW_DESCRIPTION, (fShowDescription) ? "on" : "off"); //$NON-NLS-1$ //$NON-NLS-2$
		memento.putString(M_SHOW_PROGRESS, (fShowProgress) ? "on" : "off"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public void createPartControl(final Composite parent) {
		updateContentDescription(null);
		
		fComposite = parent;
		final GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		parent.setLayout(layout);
		
		if (fShowProgress) {
			createProgressControl();
		}
		
		fTableViewer = new TableViewer(parent, SWT.MULTI | SWT.V_SCROLL);
		fTableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fTableViewer.getTable().setLinesVisible(false);
		fTableViewer.getTable().setHeaderVisible(false);
		new TableColumn(fTableViewer.getTable(), SWT.DEFAULT);
		fTableViewer.getTable().addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(final ControlEvent e) {
				// adapt the column width to the width of the table
				final Table table = fTableViewer.getTable();
				final Rectangle area = table.getClientArea();
				final TableColumn column = table.getColumn(0);
				column.setWidth(area.width-3); // it looks better with a small gap
			}
		});
		
		fTableViewer.setContentProvider(new ViewContentProvider());
		fTableViewer.setLabelProvider(new TableLabelProvider());
		
		createActions();
		contributeToActionBars();
		
		// listen on console changes
		final IToolRegistry toolRegistry = NicoUI.getToolRegistry();
		connect(toolRegistry.getActiveToolSession(getViewSite().getPage()).getProcess());
		fToolRegistryListener = new IToolRegistryListener() {
			public void toolSessionActivated(final ToolSessionUIData info) {
				final ToolProcess process = info.getProcess();
				UIAccess.getDisplay().syncExec(new Runnable() {
					public void run() {
						connect(process);
					}
				});
			}
			public void toolSessionClosed(final ToolSessionUIData info) {
				disconnect(info.getProcess());
			}
		};
		toolRegistry.addListener(fToolRegistryListener, getViewSite().getPage());
	}
	
	
	private void createProgressControl() {
		fProgressControl = new ToolProgressGroup(fComposite);
		fProgressControl.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, false));
	}
	
	protected void updateContentDescription(final ToolProcess process) {
		if (fShowDescription) {
			setContentDescription(process != null ? process.getToolLabel(false) : " "); //$NON-NLS-1$
		}
		else {
			setContentDescription(""); //$NON-NLS-1$
		}
	}
	
	private void createActions() {
		fPauseAction = new PauseAction();
		
		fShowDescriptionAction = new ShowDescriptionAction();
		fShowProgressAction = new ShowProgressAction();
		
		fSelectAllAction = new Action() {
			@Override
			public void run() {
				fTableViewer.getTable().selectAll();
			}
		};
		fDeleteAction = new Action() {
			@Override
			public void run() {
				final Queue queue = getQueue();
				if (queue != null) {
					final IStructuredSelection selection = (IStructuredSelection) fTableViewer.getSelection();
					final Object[] elements = selection.toArray();
					queue.removeElements(elements);
				}
			}
		};
	}
	
	private void contributeToActionBars() {
		final IActionBars bars = getViewSite().getActionBars();
		
		bars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), fSelectAllAction);
		bars.setGlobalActionHandler(ActionFactory.DELETE.getId(), fDeleteAction);
		
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}
	
	private void fillLocalPullDown(final IMenuManager manager) {
		manager.add(fShowDescriptionAction);
		manager.add(fShowProgressAction);
	}
	
	private void fillLocalToolBar(final IToolBarManager manager) {
		manager.add(fPauseAction);
	}
	
	private void disconnect(final ToolProcess process) {
		UIAccess.getDisplay().syncExec(new Runnable() {
			public void run() {
				if (fProcess != null && fProcess == process) {
					connect(null);
				}
			}
		});
	}
	
	/** Should only be called inside UI Thread */
	public void connect(final ToolProcess process) {
		final Runnable runnable = new Runnable() {
			public void run() {
				if (!UIAccess.isOkToUse(fTableViewer)) {
					return;
				}
				fProcess = process;
				updateContentDescription(process);
				fPauseAction.setTool(process);
				if (fProgressControl != null) {
					fProgressControl.setTool(process, true);
				}
				fTableViewer.setInput(process);
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
