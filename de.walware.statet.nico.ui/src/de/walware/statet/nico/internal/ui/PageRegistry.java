/*******************************************************************************
 * Copyright (c) 2005-2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
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

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.IToolRegistryListener;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.ToolSessionUIData;
import de.walware.statet.nico.ui.console.NIConsole;


/**
 *
 */
class PageRegistry {
	
	static final String SHOW_CONSOLE_JOB_NAME = "Show NIConsole"; //$NON-NLS-1$
	
	private static List<IConsoleView> getConsoleViews(IWorkbenchPage page) {
		List<IConsoleView> consoleViews = new ArrayList<IConsoleView>();
		
		IViewReference[] allReferences = page.getViewReferences();
		for (IViewReference reference : allReferences) {
			if (reference.getId().equals(IConsoleConstants.ID_CONSOLE_VIEW)) {
				IViewPart view = reference.getView(true);
				if (view != null) {
					IConsoleView consoleView = (IConsoleView) view;
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
		
		private volatile NIConsole fConsoleToShow;
		private volatile boolean fActivate;
		
		ShowConsoleViewJob() {
			super(SHOW_CONSOLE_JOB_NAME);
			setSystem(true);
			setPriority(Job.SHORT);
		}
		
		void schedule(NIConsole console, boolean activate) {
			cancel();
			fConsoleToShow = console;
			fActivate = activate;
			schedule(100);
		}
		
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			IWorkbenchPage page = getPage();
			NIConsole console = fConsoleToShow;
			if (page == null || console == null) {
				return Status.CANCEL_STATUS;
			}
			try {
				IWorkbenchPart activePart = page.getActivePart();
				if (activePart instanceof IConsoleView) {
					if (console == ((IConsoleView) activePart).getConsole()) {
						return Status.OK_STATUS;
					}
				}
				// Search the console view
				List<IConsoleView> views = getConsoleViews(page);
				ListIterator<IConsoleView> iter = views.listIterator();
				while (iter.hasNext()) {
					IConsoleView view = iter.next();
					if (view.isPinned()) {					// visible and pinned
						if (console == view.getConsole()) {
							return showInView(view, monitor);
						} else {
							iter.remove();
						}
					}
				}
				IConsoleView[] preferedView = new IConsoleView[4];
				for (IConsoleView view : views) {
					if (console == view.getConsole()) {
						if (page.isPartVisible(view)) {	// already visible
							return showInView(view, monitor);
						}
						else { 								// already selected
							preferedView[0] = view;
						}
					}
					else {									// for same type created view
						String secId = view.getViewSite().getSecondaryId();
						if (secId != null && secId.startsWith(console.getType())) {
							preferedView[1] = view;
						}
						else if (page.isPartVisible(view)) { // visible views
							preferedView[2] = view;
						}
						else {								// other views
							preferedView[3] = view;
						}
					}
				}
				for (int i = 0; i < preferedView.length; i++) {
					if (preferedView[i] != null) {
						return showInView(preferedView[i], monitor);
					}
				}
				return showInView(null, monitor);
			} catch (PartInitException e) {
				NicoUIPlugin.logError(NicoUIPlugin.INTERNAL_ERROR, "Error of unexpected type occured, when showing a console view.", e); //$NON-NLS-1$
				return Status.OK_STATUS;
			} finally {
				fConsoleToShow = null;
			}
		}
		
		private IStatus showInView(IConsoleView view, IProgressMonitor monitor) throws PartInitException {
			NIConsole console = fConsoleToShow;
			boolean activate = fActivate;
			IWorkbenchPage page = getPage();
			if (page == null || monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			if (view == null) {
				String secId = console.getType() + System.currentTimeMillis(); // force creation
				view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW, secId, IWorkbenchPage.VIEW_CREATE);
			}
			view.display(console);
			if (activate) {
				page.activate(view);
			}
			else {
				page.bringToTop(view);
			}
			return Status.OK_STATUS;
		}
		
	}
	private ShowConsoleViewJob fShowConsoleViewJob = new ShowConsoleViewJob();

	private class UpdateConsoleJob extends Job {
		
		private volatile NIConsole fConsole;
		private volatile IViewPart fSource;
		private volatile List<ToolProcess> fExclude;
		
		public UpdateConsoleJob() {
			super("Update Console");
			setSystem(true);
			setPriority(Job.SHORT);
		}
		
		void schedule(NIConsole console, IViewPart source, List<ToolProcess> exclude) {
			cancel();
			fConsole = console;
			fSource = source;
			fExclude = exclude;
			schedule(50);
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
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

		private void notifyActiveToolSessionChanged(IViewPart source) {
			ToolSessionUIData info = new ToolSessionUIData(fActiveProcess,
					fActiveConsole, source);
//			System.out.println("activated: " + info.toString());
			
			Object[] listeners = fListeners.getListeners();
			for (Object obj : listeners) {
				((IToolRegistryListener) obj).toolSessionActivated(info);
			}
		}

	}
	private UpdateConsoleJob fConsoleUpdateJob = new UpdateConsoleJob();
	
	PageRegistry(IWorkbenchPage page) {
		fPage = page;
		
		fDebugContextListener = new IDebugContextListener() {
			public void debugContextChanged(DebugContextEvent event) {
				ISelection selection = event.getContext();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) selection;
					if (sel.size() == 1) {
						Object element = sel.getFirstElement();
						ToolProcess tool = null;
						if (element instanceof IAdaptable) {
							IProcess process = (IProcess) ((IAdaptable) element).getAdapter(IProcess.class);
							if (process instanceof ToolProcess) {
								tool = (ToolProcess) process;
							}
						}
						if (tool == null && element instanceof ILaunch) {
							IProcess[] processes = ((ILaunch) element).getProcesses();
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
	
	public synchronized ToolSessionUIData createSessionInfo(IViewPart source) {
		return new ToolSessionUIData(fActiveProcess,
				fActiveConsole, null);
	}
	
	public synchronized void reactOnConsolesRemoved(List<ToolProcess> consoles) {
		if (consoles.contains(fActiveProcess)) {
			doActiveConsoleChanged(null, null, consoles);
		}
	}
	
	public synchronized void doActiveConsoleChanged(NIConsole console, IViewPart source, List<ToolProcess> exclude) {
		fConsoleUpdateJob.schedule(console, source, exclude);
	}
	
	public synchronized void showConsole(NIConsole console, boolean activate) {
		fShowConsoleViewJob.schedule(console, activate);
	}

	/**
	 * only in UI thread
	 */
	private static NIConsole searchConsole(IWorkbenchPage page, List<ToolProcess> exclude) {
		// Search NIConsole in
		// 1. active part
		// 2. visible part
		// 3. all

		NIConsole nico = null;
		IWorkbenchPart part = page.getActivePart();
		if (part instanceof IConsoleView) {
			IConsole console = ((IConsoleView) part).getConsole();
			if (console instanceof NIConsole && !exclude.contains((nico = (NIConsole) console))) {
				return nico;
			}
		}

		List<IConsoleView> consoleViews = getConsoleViews(page);
		NIConsole secondChoice = null;
		for (IConsoleView view : consoleViews) {
			IConsole console = view.getConsole();
			if (console instanceof NIConsole && !exclude.contains((nico = (NIConsole) console))) {
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
