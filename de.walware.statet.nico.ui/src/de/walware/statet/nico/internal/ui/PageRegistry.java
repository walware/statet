/*******************************************************************************
 * Copyright (c) 2005-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
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

import de.walware.ecommons.FastList;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.IToolRegistryListener;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.ToolSessionUIData;
import de.walware.statet.nico.ui.console.NIConsole;


/**
 * part of tool registry per workbench
 */
class PageRegistry implements IDebugEventSetListener, IDebugContextListener {
	
	
	static final String SHOW_CONSOLE_JOB_NAME = "Show NIConsole"; //$NON-NLS-1$
	
	
	private class ShowConsoleViewJob extends WorkbenchJob {
		
		private final int fDelay;
		
		private volatile NIConsole fConsoleToShow;
		private volatile boolean fActivate;
		
		
		public ShowConsoleViewJob(final int delay) {
			super(SHOW_CONSOLE_JOB_NAME);
			setSystem(true);
			setPriority(Job.SHORT);
			fDelay = delay;
		}
		
		
		public void schedule(final NIConsole console, final boolean activate) {
			cancel();
			fConsoleToShow = console;
			fActivate = activate;
			schedule(fDelay);
		}
		
		@Override
		public IStatus runInUIThread(final IProgressMonitor monitor) {
			final NIConsole console = fConsoleToShow;
			if (fClosed || console == null) {
				return Status.CANCEL_STATUS;
			}
			try {
				final IWorkbenchPart activePart = fPage.getActivePart();
				if (activePart instanceof IConsoleView) {
					if (console == ((IConsoleView) activePart).getConsole()) {
						((IConsoleView) activePart).setFocus();
						return Status.OK_STATUS;
					}
				}
				final IConsoleView view = searchView(console);
				return showInView(view, monitor);
			}
			catch (final PartInitException e) {
				NicoUIPlugin.logError(NicoUIPlugin.INTERNAL_ERROR, "Error of unexpected type occured, when showing a console view.", e); //$NON-NLS-1$
				return Status.OK_STATUS;
			}
			finally {
				fConsoleToShow = null;
			}
		}
		
		private IStatus showInView(IConsoleView view, final IProgressMonitor monitor) throws PartInitException {
			final NIConsole console = fConsoleToShow;
			final boolean activate = fActivate;
			if (fClosed || monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			
			if (view == null) {
				final String secId = console.getType() + System.currentTimeMillis(); // force creation
				view = (IConsoleView) fPage.showView(IConsoleConstants.ID_CONSOLE_VIEW, secId, IWorkbenchPage.VIEW_CREATE);
			}
			view.display(console);
			if (activate) {
				fPage.activate(view);
			}
			else {
				fPage.bringToTop(view);
			}
			finish(view);
			return Status.OK_STATUS;
		}
		
		protected void finish(final IConsoleView view) {
		}
		
	}
	
	private class OnConsoleChangedJob extends Job implements ISchedulingRule {
		
		private volatile NIConsole fConsole;
		private volatile IViewPart fSource;
		private volatile List<ToolProcess> fExclude;
		
		public OnConsoleChangedJob() {
			super("NicoUI Registry - On Console Changed");
			setSystem(true);
			setPriority(Job.SHORT);
		}
		
		@Override
		public boolean belongsTo(final Object family) {
			return (family == PageRegistry.this);
		}
		
		@Override
		public boolean contains(final ISchedulingRule rule) {
			return false;
		}
		
		@Override
		public boolean isConflicting(final ISchedulingRule rule) {
			if (rule instanceof Job) {
				return ((Job) rule).belongsTo(PageRegistry.this);
			}
			return false;
		}
		
		
		public void scheduleActivated(final NIConsole console, final IViewPart source) {
			synchronized (PageRegistry.this) {
				if (fExclude != null && console != null && fExclude.contains(console.getProcess())) {
					return;
				}
				cancel(); // ensure delay
				fConsole = console;
				fSource = source;
				schedule(50);
			}
		}
		
		public void scheduleRemoved(final List<ToolProcess> exclude) {
			synchronized (PageRegistry.this) {
				if (fConsole != null && exclude.contains(fConsole.getProcess())) {
					fConsole = null;
				}
				else if (fActiveProcess == null && !exclude.contains(fActiveProcess)) {
					return;
				}
				cancel(); // ensure delay
				fExclude = exclude;
				schedule(200);
			}
		}
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			if (fClosed) {
				return Status.OK_STATUS;
			}
			NIConsole console = fConsole;
			final List<ToolProcess> exclude = fExclude;
			
			if (console == null) {
				final AtomicReference<NIConsole> ref = new AtomicReference<NIConsole>();
				UIAccess.getDisplay(fPage.getWorkbenchWindow().getShell()).syncExec(new Runnable() {
					@Override
					public void run() {
						ref.set(searchConsole(exclude));
					}
				});
				console = ref.get();
			}
			
			synchronized(PageRegistry.this) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				if (fConsole == console) {
					fConsole = null;
					fExclude = null;
				}
				
				if ((console == fActiveConsole) || (console == null && fActiveConsole == null)) {
					return Status.OK_STATUS;
				}
				fActiveProcess = (console != null) ? console.getProcess() : null;
				fActiveConsole = console;
			}
			
			// don't cancel after process is changed
			notifyActiveToolSessionChanged(fSource);
			
			return Status.OK_STATUS;
		}
		
	}
	
	private class OnToolTerminatedJob extends Job implements ISchedulingRule {
		
		private volatile ToolProcess fTool;
		
		public OnToolTerminatedJob() {
			super("NicoUI Registry - On Tool Terminated"); //$NON-NLS-1$
			setSystem(true);
			setPriority(Job.SHORT);
		}
		
		@Override
		public boolean belongsTo(final Object family) {
			return (family == PageRegistry.this);
		}
		
		@Override
		public boolean contains(final ISchedulingRule rule) {
			return false;
		}
		
		@Override
		public boolean isConflicting(final ISchedulingRule rule) {
			if (rule instanceof Job) {
				return ((Job) rule).belongsTo(PageRegistry.this);
			}
			return false;
		}
		
		
		public void scheduleTerminated(final ToolProcess tool) {
			fTool = tool;
			schedule(0);
		}
		
		@Override
		public synchronized IStatus run(final IProgressMonitor monitor) {
			final ToolProcess tool = fTool;
			fTool = null;
			
			if (getActiveProcess() == tool) {
				notifyToolTerminated();
			}
			return Status.OK_STATUS;
		}
		
	};
	
	
	private final IWorkbenchPage fPage;
	private boolean fClosed;
	private IDebugContextListener fDebugContextListener;
	
	private ToolProcess fActiveProcess;
	private NIConsole fActiveConsole;
	
	final FastList<IToolRegistryListener> fListeners;
	
	private final ShowConsoleViewJob fShowConsoleViewJob = new ShowConsoleViewJob(100);
	private final OnConsoleChangedJob fConsoleUpdateJob = new OnConsoleChangedJob();
	private final OnToolTerminatedJob fTerminatedJob = new OnToolTerminatedJob();
	
	
	PageRegistry(final IWorkbenchPage page, final IToolRegistryListener[] initial) {
		fPage = page;
		fListeners = new FastList<IToolRegistryListener>(IToolRegistryListener.class, FastList.IDENTITY, initial);
		
		DebugUITools.getDebugContextManager().getContextService(fPage.getWorkbenchWindow()).addDebugContextListener(this);
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	
	
	public synchronized void dispose() {
		fClosed = true;
		DebugPlugin.getDefault().removeDebugEventListener(this);
		DebugUITools.getDebugContextManager().getContextService(fPage.getWorkbenchWindow()).removeDebugContextListener(this);
		fShowConsoleViewJob.cancel();
		fConsoleUpdateJob.cancel();
		fTerminatedJob.cancel();
		
		fListeners.clear();
		fActiveProcess = null;
		fActiveConsole = null;
	}
	
	
	@Override
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
	
	@Override
	public void handleDebugEvents(final DebugEvent[] events) {
		final ToolProcess tool = fActiveProcess;
		if (tool == null) {
			return;
		}
		for (final DebugEvent event : events) {
			if (event.getSource() == tool && event.getKind() == DebugEvent.TERMINATE) {
				fTerminatedJob.scheduleTerminated(tool);
			}
		}
	}
	
	public IWorkbenchPage getPage() {
		return fPage;
	}
	
	public synchronized ToolProcess getActiveProcess() {
		return fActiveProcess;
	}
	
	public synchronized ToolSessionUIData createSessionInfo(final IViewPart source) {
		return new ToolSessionUIData(fActiveProcess, fActiveConsole, fPage, null);
	}
	
	public IConsoleView getConsoleView(final NIConsole console) {
		if (fClosed) {
			return null;
		}
		return searchView(console);
	}
	
	void handleConsolesRemoved(final List<ToolProcess> tools) {
		fConsoleUpdateJob.scheduleRemoved(tools);
	}
	
	void handleActiveConsoleChanged(final NIConsole console, final IViewPart source) {
		fConsoleUpdateJob.scheduleActivated(console, source);
	}
	
	void showConsole(final NIConsole console, final boolean activate) {
		fShowConsoleViewJob.schedule(console, activate);
	}
	
	void showConsoleExplicitly(final NIConsole console, final boolean pin) {
		fShowConsoleViewJob.cancel();
		new ShowConsoleViewJob(0) {
			@Override
			protected void finish(final IConsoleView view) {
				if (pin) {
					view.setPinned(true);
				}
			}
		}.schedule(console, true);
	}
	
	
	private void notifyActiveToolSessionChanged(final IViewPart source) {
		final ToolSessionUIData sessionData = new ToolSessionUIData(fActiveProcess, fActiveConsole, 
				fPage, source);
		if (ToolRegistry.DEBUG) {
			System.out.println("[tool registry] tool session activated: " + sessionData.toString());
		}
		
		final Object[] listeners = fListeners.toArray();
		for (final Object obj : listeners) {
			((IToolRegistryListener) obj).toolSessionActivated(sessionData);
		}
	}
	
	private void notifyToolTerminated() {
		final ToolSessionUIData sessionData = new ToolSessionUIData(fActiveProcess, fActiveConsole, 
				fPage, null);
		if (ToolRegistry.DEBUG) {
			System.out.println("[tool registry] activate tool terminated: " + sessionData.toString());
		}
		
		final Object[] listeners = fListeners.toArray();
		for (final Object obj : listeners) {
			((IToolRegistryListener) obj).toolTerminated(sessionData);
		}
	}
	
	private List<IConsoleView> getConsoleViews() {
		final List<IConsoleView> consoleViews = new ArrayList<IConsoleView>();
		
		final IViewReference[] allReferences = fPage.getViewReferences();
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
	
	/**
	 * Searches best next console (tool)
	 * 
	 * Must be called only in UI thread
	 */
	private NIConsole searchConsole(final List<ToolProcess> exclude) {
		// Search NIConsole in
		// 1. active part
		// 2. visible part
		// 3. all
		NIConsole nico = null;
		final IWorkbenchPart part = fPage.getActivePart();
		if (part instanceof IConsoleView) {
			final IConsole console = ((IConsoleView) part).getConsole();
			if (console instanceof NIConsole && !exclude.contains((nico = (NIConsole) console).getProcess())) {
				return nico;
			}
		}
		
		final List<IConsoleView> consoleViews = getConsoleViews();
		NIConsole secondChoice = null;
		for (final IConsoleView view : consoleViews) {
			final IConsole console = view.getConsole();
			if (console instanceof NIConsole && !exclude.contains((nico = (NIConsole) console).getProcess())) {
				if (fPage.isPartVisible(view)) {
					return nico;
				}
				else if (secondChoice == null) {
					secondChoice = nico;
				}
			}
		}
		return secondChoice;
	}
	
	/**
	 * Searches the best console view for the specified console (tool)
	 * 
	 * @param console
	 * @return 
	 */
	private IConsoleView searchView(final NIConsole console) {
		// Search the console view
		final List<IConsoleView> views = getConsoleViews();
		
		final IConsoleView[] preferedView = new IConsoleView[10];
		for (final IConsoleView view : views) {
			final IConsole consoleInView = view.getConsole();
			if (consoleInView == console) {
				if (fPage.isPartVisible(view)) {		// already visible
					preferedView[view.isPinned() ? 0 : 1] = view;
					continue;
				}
				else {								// already selected
					preferedView[view.isPinned() ? 2 : 3] = view;
					continue;
				}
			}
			if (consoleInView == null) {
				if (fPage.isPartVisible(view)) {
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
				if (fPage.isPartVisible(view)) { 	// visible views
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
				return preferedView[i];
			}
		}
		return null;
	}
	
}
