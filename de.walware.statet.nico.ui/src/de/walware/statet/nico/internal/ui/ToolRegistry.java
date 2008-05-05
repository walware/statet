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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.IToolRegistry;
import de.walware.statet.nico.ui.IToolRegistryListener;
import de.walware.statet.nico.ui.ToolSessionUIData;
import de.walware.statet.nico.ui.console.NIConsole;


/**
 * 
 */
public class ToolRegistry implements IToolRegistry {
	
	
	static boolean DEBUG = false;
	
	
	private class LaunchesListener implements ILaunchesListener {
		
		private class RemoveToolsJob extends Job {
			
			private List<ToolProcess> fList = new ArrayList<ToolProcess>();
			
			public RemoveToolsJob() {
				super("Remove Tools"); //$NON-NLS-1$
				setSystem(true);
				setPriority(Job.SHORT);
			}
			
			public synchronized void schedule(final List<ToolProcess> list) {
				cancel();
				fList.addAll(list);
				schedule(100);
			}
			
			@Override
			public synchronized IStatus run(final IProgressMonitor monitor) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				
				final PageRegistry[] regs = getPageRegistries();
				for (final PageRegistry reg : regs) {
					reg.reactOnConsolesRemoved(fList);
				}
				for (final ToolProcess process : fList) {
					notifyToolSessionClosed(process);
				}
				fList.clear();
				return Status.OK_STATUS;
			}
		};
		
		RemoveToolsJob fRemoveJob = new RemoveToolsJob();
		
		public void launchesAdded(final ILaunch[] launches) {
		}
		public void launchesChanged(final ILaunch[] launches) {
		}
		
		public void launchesRemoved(final ILaunch[] launches) {
			final List<ToolProcess> list = new ArrayList<ToolProcess>();
			for (final ILaunch launch : launches) {
				final IProcess[] processes = launch.getProcesses();
				for (final IProcess process : processes) {
					if (process instanceof ToolProcess) {
						list.add((ToolProcess) process);
					}
				}
			}
			
			if (list.isEmpty()) {
				return;
			}
			
			// Because debug plugin removes only ProcessConsoles, we have to do...
			removeConsoles(list);
			
			fRemoveJob.schedule(list);
		}
		
		private void removeConsoles(final List<ToolProcess> processes) {
			final List<IConsole> toRemove = new ArrayList<IConsole>();
			final IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
			final IConsole[] consoles = manager.getConsoles();
			for (final IConsole console : consoles) {
				if ((console instanceof NIConsole) &&
						processes.contains(((NIConsole) console).getProcess()) ) {
					toRemove.add(console);
				}
			}
			manager.removeConsoles(toRemove.toArray(new IConsole[toRemove.size()]));
		}
		
	}
	
	private class PageListener implements IPageListener {
		
		public void pageOpened(final IWorkbenchPage page) {
		}
		public void pageActivated(final IWorkbenchPage page) {
		}
		
		public void pageClosed(final IWorkbenchPage page) {
			PageRegistry reg;
			synchronized (fPageRegistries) {
				page.getWorkbenchWindow().removePageListener(this);
				reg = fPageRegistries.remove(page);
			}
			for (final Object obj : reg.fListeners.getListeners()) {
				fListeners.remove(obj);
			}
			reg.dispose();
		}
	};
	
	private class JobListener implements IJobChangeListener {
		
		private AtomicInteger fOwnJobs = new AtomicInteger(0);
		
		public void scheduled(final IJobChangeEvent event) {
			if (event.getJob().getName() == PageRegistry.SHOW_CONSOLE_JOB_NAME) {
				fOwnJobs.incrementAndGet();
			}
			else {
				checkJob(event.getJob());
			}
		}
		public void aboutToRun(final IJobChangeEvent event) {
			checkJob(event.getJob());
		}
		public void done(final IJobChangeEvent event) {
			if (event.getJob().getName() == PageRegistry.SHOW_CONSOLE_JOB_NAME) {
				fOwnJobs.decrementAndGet();
			}
		}
		private void checkJob(final Job eventJob) {
			if (fOwnJobs.get() > 0
					&& eventJob.getName().startsWith("Show Console View")) { //$NON-NLS-1$)
				eventJob.cancel();
				if (DEBUG) {
					System.out.println("[tool registry] show job cancel"); //$NON-NLS-1$
				}
			}
		}
		
		public void sleeping(final IJobChangeEvent event) {
		}
		public void awake(final IJobChangeEvent event) {
		}
		public void running(final IJobChangeEvent event) {
		}
	}
	
	private Map<IWorkbenchPage, PageRegistry> fPageRegistries = new HashMap<IWorkbenchPage, PageRegistry>();
	private boolean isDisposed = false;
	
	private LaunchesListener fLaunchesListener;
	private IPageListener fPagesListener;
	private JobListener fJobListener;
	
	private ListenerList fListeners = new ListenerList();
	
	
	public ToolRegistry() {
		fLaunchesListener = new LaunchesListener();
		fPagesListener = new PageListener();
		fJobListener = new JobListener();
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(fLaunchesListener);
		Job.getJobManager().addJobChangeListener(fJobListener);
	}
	
	public void dispose() {
		synchronized (fPageRegistries) {
			DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(fLaunchesListener);
			fLaunchesListener = null;
			
			for (final IWorkbenchPage page : fPageRegistries.keySet()) {
				page.getWorkbenchWindow().removePageListener(fPagesListener);
				final PageRegistry reg = fPageRegistries.get(page);
				reg.dispose();
			}
			fPageRegistries.clear();
			isDisposed = true;
		}
		
		Job.getJobManager().addJobChangeListener(fJobListener);
		fJobListener = null;
		if (DEBUG) {
			System.out.println("[tool registry] registry closed."); //$NON-NLS-1$
		}
	}
	
	private PageRegistry getPageRegistry(final IWorkbenchPage page) {
		if (page == null) {
			return null;
		}
		
		synchronized (fPageRegistries) {
			PageRegistry reg = fPageRegistries.get(page);
			if (reg == null && !isDisposed) {
				page.getWorkbenchWindow().addPageListener(fPagesListener);
				reg = new PageRegistry(page);
				fPageRegistries.put(page, reg);
			}
			return reg;
		}
	}
	
	private PageRegistry[] getPageRegistries() {
		synchronized (fPageRegistries) {
			final Collection<PageRegistry> collection = fPageRegistries.values();
			return collection.toArray(new PageRegistry[collection.size()]);
		}
	}
	
	
	public void addListener(final IToolRegistryListener listener, final IWorkbenchPage page) {
		fListeners.add(listener);
		final PageRegistry reg = getPageRegistry(page);
		if (reg != null) {
			reg.fListeners.add(listener);
		}
	}
	
	public void removeListener(final IToolRegistryListener listener) {
		fListeners.remove(listener);
		synchronized (fPageRegistries) {
			for (final PageRegistry reg : fPageRegistries.values()) {
				reg.fListeners.remove(listener);
			}
		}
	}
	
	public void consoleActivated(final IConsoleView consoleView, final NIConsole console) {
		final IWorkbenchPage page = consoleView.getViewSite().getPage();
		final PageRegistry reg = getPageRegistry(page);
		if (reg != null) {
			reg.doActiveConsoleChanged(console, consoleView, Collections.EMPTY_LIST);
		}
	}
	
	public ToolSessionUIData getActiveToolSession(final IWorkbenchPage page) {
		if (page == null) {
			return null;
		}
		
		final PageRegistry reg = getPageRegistry(page);
		return reg.createSessionInfo(null);
	}
	
	public IWorkbenchPage findWorkbenchPage(final ToolProcess process) {
		final IWorkbenchPage activePage = UIAccess.getActiveWorkbenchPage(false);
		IWorkbenchPage page = null;
		synchronized (fPageRegistries) {
			for (final PageRegistry reg : fPageRegistries.values()) {
				if (reg.getActiveProcess() == process) {
					page = reg.getPage();
					if (page == activePage) {
						return page;
					}
				}
			}
		}
		if (page != null) {
			return page;
		}
		return activePage;
	}
	
	private void notifyToolSessionClosed(final ToolProcess process) {
		final ToolSessionUIData info = new ToolSessionUIData(process, null, null);
		
		if (DEBUG) {
			System.out.println("[tool registry] session closed: " + info.toString()); //$NON-NLS-1$
		}
		final Object[] listeners = fListeners.getListeners();
		for (final Object obj : listeners) {
			((IToolRegistryListener) obj).toolSessionClosed(info);
		}
	}
	
	
	public void showConsole(final NIConsole console, final IWorkbenchPage page,
			final boolean activate) {
		final PageRegistry reg = getPageRegistry(page);
		reg.showConsole(console, activate);
	}
	
	public void showConsoleExplicitly(final NIConsole console, final IWorkbenchPage page,
			final boolean pin) {
		final PageRegistry reg = getPageRegistry(page);
		reg.showConsoleExplicitly(console, pin);
	}
	
}
