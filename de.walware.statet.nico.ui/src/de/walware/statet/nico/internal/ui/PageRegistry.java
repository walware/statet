/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.progress.WorkbenchJob;

import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.IToolRegistryListener;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.ToolSessionUIData;
import de.walware.statet.nico.ui.console.NIConsole;


/**
 * part of tool registry per workbench
 */
class PageRegistry {
	
	static final String SHOW_CONSOLE_JOB_NAME = "Show NIConsole"; //$NON-NLS-1$
	
	private static List<IConsoleView> getConsoleViews(final IWorkbenchPage page) {
		final List<IConsoleView> consoleViews = new ArrayList<IConsoleView>();
		
		final IViewReference[] allReferences = page.getViewReferences();
		for (final IViewReference reference : allReferences) {
			if (reference.getId().equals(IConsoleConstants.ID_CONSOLE_VIEW)) {
				final IViewPart view = reference.getView(true);
				if (view != null) {
					final IConsoleView consoleView = (IConsoleView) view;
					if (!consoleView.isPinned()) {
						consoleViews.add(consoleView);
					}
					else if (consoleView.getConsole() instanceof NIConsole) {
						consoleViews.add(0, consoleView);
					}
				}
			}
		}
		return consoleViews;
	}
	
	
	private IWorkbenchPage fPage;
	private IDebugContextListener fDebugContextListener;
	
	private ToolProcess fActiveProcess;
	private NIConsole fActiveConsole;
	
	final ListenerList fListeners = new ListenerList(ListenerList.IDENTITY);
	
	
	private class ShowConsoleViewJob extends WorkbenchJob {
		
		private final int fDelay;
		
		private volatile NIConsole fConsoleToShow;
		private volatile boolean fActivate;
		
		ShowConsoleViewJob(final int delay) {
			super(SHOW_CONSOLE_JOB_NAME);
			setSystem(true);
			setPriority(Job.SHORT);
			fDelay = delay;
		}
		
		void schedule(final NIConsole console, final boolean activate) {
			cancel();
			fConsoleToShow = console;
			fActivate = activate;
			schedule(fDelay);
		}
		
		@Override
		public IStatus runInUIThread(final IProgressMonitor monitor) {
			final IWorkbenchPage page = getPage();
			final NIConsole console = fConsoleToShow;
			if (page == null || console == null) {
				return Status.CANCEL_STATUS;
			}
			try {
				final IWorkbenchPart activePart = page.getActivePart();
				if (activePart instanceof IConsoleView) {
					if (console == ((IConsoleView) activePart).getConsole()) {
						((IConsoleView) activePart).setFocus();
						return Status.OK_STATUS;
					}
				}
				// Search the console view
				final List<IConsoleView> views = getConsoleViews(page);
				
				final IConsoleView[] preferedView = new IConsoleView[10];
				for (final IConsoleView view : views) {
					final IConsole consoleInView = view.getConsole();
					if (consoleInView == console) {
						if (page.isPartVisible(view)) {		// already visible
							preferedView[view.isPinned() ? 0 : 1] = view;
							continue;
						}
						else {								// already selected
							preferedView[view.isPinned() ? 2 : 3] = view;
							continue;
						}
					}
					if (consoleInView == null) {
						if (page.isPartVisible(view)) {
							preferedView[4] = view;
							continue;
						}
						else {
							preferedView[5] = view;
							continue;
						}
					}
					if (!view.isPinned()) {					// for same type created view
						final String secId = view.getViewSite().getSecondaryId();
						if (secId != null && secId.startsWith(console.getType())) {
							preferedView[6] = view;
							continue;
						}
						if (page.isPartVisible(view)) { 	// visible views
							preferedView[7] = view;
							continue;
						}
						else {								// other views
							preferedView[8] = view;
							continue;
						}
					}
				}
				for (int i = 0; i < preferedView.length; i++) {
					if (preferedView[i] != null) {
						return showInView(preferedView[i], monitor);
					}
				}
				return showInView(null, monitor);
			} catch (final PartInitException e) {
				NicoUIPlugin.logError(NicoUIPlugin.INTERNAL_ERROR, "Error of unexpected type occured, when showing a console view.", e); //$NON-NLS-1$
				return Status.OK_STATUS;
			} finally {
				fConsoleToShow = null;
			}
		}
		
		private IStatus showInView(IConsoleView view, final IProgressMonitor monitor) throws PartInitException {
			final NIConsole console = fConsoleToShow;
			final boolean activate = fActivate;
			final IWorkbenchPage page = getPage();
			if (page == null || monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			
			if (view == null) {
				final String secId = console.getType() + System.currentTimeMillis(); // force creation
				view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW, secId, IWorkbenchPage.VIEW_CREATE);
			}
			view.display(console);
			if (activate) {
				page.activate(view);
			}
			else {
				page.bringToTop(view);
			}
			finish(view);
			return Status.OK_STATUS;
		}
		
		protected void finish(final IConsoleView view) {
		}
		
	}
	private ShowConsoleViewJob fShowConsoleViewJob = new ShowConsoleViewJob(100);
	
	private class UpdateConsoleJob extends Job {
		
		private volatile NIConsole fConsole;
		private volatile IViewPart fSource;
		private volatile List<ToolProcess> fExclude;
		
		public UpdateConsoleJob() {
			super("Update Console");
			setSystem(true);
			setPriority(Job.SHORT);
		}
		
		void schedule(final NIConsole console, final IViewPart source, final List<ToolProcess> exclude) {
			cancel();
			fConsole = console;
			fSource = source;
			fExclude = exclude;
			schedule(50);
		}
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			final IWorkbenchPage page = getPage();
			if (page == null) {
				return Status.OK_STATUS;
			}
			NIConsole console = fConsole;
			final List<ToolProcess> exclude = fExclude;
			
			if (console == null) {
				final AtomicReference<NIConsole> ref = new AtomicReference<NIConsole>();
				UIAccess.getDisplay(page.getWorkbenchWindow().getShell()).syncExec(new Runnable() {
					public void run() {
						ref.set(searchConsole(page, exclude));
					}
				});
				console = ref.get();
			}
			
			synchronized(PageRegistry.this) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				if ((console == fActiveConsole)
						|| (console != null && console.equals(fActiveConsole)) ) {
					return Status.OK_STATUS;
				}
				fActiveProcess = (console != null) ? console.getProcess() : null;
				fActiveConsole = console;
			}
			
			// don't cancel after process is changed
			notifyActiveToolSessionChanged(fSource);
			
			return Status.OK_STATUS;
		}
		
		private void notifyActiveToolSessionChanged(final IViewPart source) {
			final ToolSessionUIData info = new ToolSessionUIData(fActiveProcess, fActiveConsole, source);
			if (ToolRegistry.DEBUG) {
				System.out.println("[tool registry] session activated: " + info.toString());
			}
			
			final Object[] listeners = fListeners.getListeners();
			for (final Object obj : listeners) {
				((IToolRegistryListener) obj).toolSessionActivated(info);
			}
		}
		
	}
	private UpdateConsoleJob fConsoleUpdateJob = new UpdateConsoleJob();
	
	
	PageRegistry(final IWorkbenchPage page) {
		fPage = page;
		
		fDebugContextListener = new IDebugContextListener() {
			public void debugContextChanged(final DebugContextEvent event) {
				final ISelection selection = event.getContext();
				if (selection instanceof IStructuredSelection) {
					final IStructuredSelection sel = (IStructuredSelection) selection;
					if (sel.size() == 1) {
						final Object element = sel.getFirstElement();
						ToolProcess tool = null;
						if (element instanceof IAdaptable) {
							final IProcess process = (IProcess) ((IAdaptable) element).getAdapter(IProcess.class);
							if (process instanceof ToolProcess) {
								tool = (ToolProcess) process;
							}
						}
						if (tool == null && element instanceof ILaunch) {
							final IProcess[] processes = ((ILaunch) element).getProcesses();
							for (int i = 0; i < processes.length; i++) {
								if (processes[i] instanceof ToolProcess) {
									tool = (ToolProcess) processes[i];
									break;
								}
							}
						}
						if (tool != null) {
							showConsole(NicoUITools.getConsole(tool), false);
						}
					}
				}
			}
		};
		DebugUITools.getDebugContextManager().getContextService(fPage.getWorkbenchWindow()).addDebugContextListener(fDebugContextListener);
	}
	
	
	public synchronized void  dispose() {
		DebugUITools.getDebugContextManager().getContextService(fPage.getWorkbenchWindow()).removeDebugContextListener(fDebugContextListener);
		fShowConsoleViewJob.cancel();
		fShowConsoleViewJob = null;
		fConsoleUpdateJob.cancel();
		fConsoleUpdateJob = null;
		fListeners.clear();
		fActiveProcess = null;
		fActiveConsole = null;
		fPage = null;
	}
	
	public synchronized IWorkbenchPage getPage() {
		return fPage;
	}
	
	public synchronized ToolProcess getActiveProcess() {
		return fActiveProcess;
	}
	
	public synchronized ToolSessionUIData createSessionInfo(final IViewPart source) {
		return new ToolSessionUIData(fActiveProcess,
				fActiveConsole, null);
	}
	
	public synchronized void reactOnConsolesRemoved(final List<ToolProcess> consoles) {
		if (consoles.contains(fActiveProcess)) {
			doActiveConsoleChanged(null, null, consoles);
		}
	}
	
	public synchronized void doActiveConsoleChanged(final NIConsole console, final IViewPart source, final List<ToolProcess> exclude) {
		fConsoleUpdateJob.schedule(console, source, exclude);
	}
	
	public synchronized void showConsole(final NIConsole console, final boolean activate) {
		fShowConsoleViewJob.schedule(console, activate);
	}
	
	public void showConsoleExplicitly(final NIConsole console, final boolean pin) {
		new ShowConsoleViewJob(0) {
			@Override
			protected void finish(final IConsoleView view) {
				if (pin) {
					view.setPinned(true);
				}
			}
		}.schedule(console, true);
	}
	
	/**
	 * only in UI thread
	 */
	private static NIConsole searchConsole(final IWorkbenchPage page, final List<ToolProcess> exclude) {
		// Search NIConsole in
		// 1. active part
		// 2. visible part
		// 3. all
		NIConsole nico = null;
		final IWorkbenchPart part = page.getActivePart();
		if (part instanceof IConsoleView) {
			final IConsole console = ((IConsoleView) part).getConsole();
			if (console instanceof NIConsole && !exclude.contains((nico = (NIConsole) console).getProcess())) {
				return nico;
			}
		}
		
		final List<IConsoleView> consoleViews = getConsoleViews(page);
		NIConsole secondChoice = null;
		for (final IConsoleView view : consoleViews) {
			final IConsole console = view.getConsole();
			if (console instanceof NIConsole && !exclude.contains((nico = (NIConsole) console).getProcess())) {
				if (page.isPartVisible(view)) {
					return nico;
				}
				else if (secondChoice == null) {
					secondChoice = nico;
				}
			}
		}
		return secondChoice;
	}
	
}
