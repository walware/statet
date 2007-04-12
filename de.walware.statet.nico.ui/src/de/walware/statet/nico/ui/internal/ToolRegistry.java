/*******************************************************************************
 * Copyright (c) 2005-2007 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.internal;

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
 * TODO: remove debug messages, also in PageRegistry
 */
public class ToolRegistry implements IToolRegistry {

	
	private class LaunchesListener implements ILaunchesListener {

		private class RemoveToolsJob extends Job {
			
			private List<ToolProcess> fList = new ArrayList<ToolProcess>();
			
			public RemoveToolsJob() {
				super("Remove Tools"); //$NON-NLS-1$
			}
			
			public synchronized void schedule(List<ToolProcess> list) {
				
				cancel();
				fList.addAll(list);
				schedule(100);
			}
			
			public synchronized IStatus run(IProgressMonitor monitor) {
				
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				
				PageRegistry[] regs = getPageRegistries();
				for (PageRegistry reg : regs) {
					reg.reactOnConsolesRemoved(fList);
				}
				for (ToolProcess process : fList) {
					notifyToolSessionClosed(process);
				}
				fList.clear();
				return Status.OK_STATUS;
			}
		};
		
		RemoveToolsJob fRemoveJob = new RemoveToolsJob();

		public void launchesAdded(ILaunch[] launches) {
		}
		public void launchesChanged(ILaunch[] launches) {
		}

		public void launchesRemoved(ILaunch[] launches) {
			
			final List<ToolProcess> list = new ArrayList<ToolProcess>();
			for (ILaunch launch : launches) {
				IProcess[] processes = launch.getProcesses();
				for (IProcess process : processes) {
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
		
		private void removeConsoles(List<ToolProcess> processes) {
			
			List<IConsole> toRemove = new ArrayList<IConsole>();
			IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
			IConsole[] consoles = manager.getConsoles();
			for (IConsole console : consoles) {
				if ((console instanceof NIConsole) &&
						processes.contains(((NIConsole) console).getProcess()) ) {
					toRemove.add(console);
				}
			}
			manager.removeConsoles(toRemove.toArray(new IConsole[toRemove.size()]));
		}

	}
	
	private class PageListener implements IPageListener {
		
		public void pageOpened(IWorkbenchPage page) {
		}
		public void pageActivated(IWorkbenchPage page) {
		}
		
		public void pageClosed(IWorkbenchPage page) {

			PageRegistry reg;
			synchronized (fPageRegistries) {
				page.getWorkbenchWindow().removePageListener(this);
				reg = fPageRegistries.remove(page);
			}
			for (Object obj : reg.fListeners.getListeners()) {
				fListeners.remove(obj);
			}
			reg.dispose();
		}
	};

	private class JobListener implements IJobChangeListener {
		
		private AtomicInteger fOwnJobs = new AtomicInteger(0);

		public void scheduled(IJobChangeEvent event) {
			if (event.getJob().getName() == PageRegistry.SHOW_CONSOLE_JOB_NAME) {
				fOwnJobs.incrementAndGet();
			}
			else {
				checkJob(event.getJob());
			}
		}
		public void aboutToRun(IJobChangeEvent event) {
			checkJob(event.getJob());
		}
		public void done(IJobChangeEvent event) {
			if (event.getJob().getName() == PageRegistry.SHOW_CONSOLE_JOB_NAME) {
				fOwnJobs.decrementAndGet();
			}
		}
		private void checkJob(Job eventJob) {
			if (fOwnJobs.get() > 0
					&& eventJob.getName().startsWith("Show Console View")) { //$NON-NLS-1$)
				eventJob.cancel();
				System.out.println("show job cancel");
			}
		}

		public void sleeping(IJobChangeEvent event) {
		}
		public void awake(IJobChangeEvent event) {
		}
		public void running(IJobChangeEvent event) {
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
			
			for (IWorkbenchPage page : fPageRegistries.keySet()) {
				page.getWorkbenchWindow().removePageListener(fPagesListener);
				PageRegistry reg = fPageRegistries.get(page);
				reg.dispose();
			}
			fPageRegistries.clear();
			isDisposed = true;
		}
		
		Job.getJobManager().addJobChangeListener(fJobListener);
		fJobListener = null;
		System.out.println("Registry closed.");
	}

	private PageRegistry getPageRegistry(IWorkbenchPage page) {
		
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
			Collection<PageRegistry> collection = fPageRegistries.values();
			return collection.toArray(new PageRegistry[collection.size()]);
		}
	}
	
	
	public void addListener(IToolRegistryListener listener, IWorkbenchPage page) {
		
		fListeners.add(listener);
		PageRegistry reg = getPageRegistry(page);
		if (reg != null) {
			reg.fListeners.add(listener);
		}
	}
	
	public void removeListener(IToolRegistryListener listener) {
		
		fListeners.remove(listener);
		synchronized (fPageRegistries) {
			for (PageRegistry reg : fPageRegistries.values()) {
				reg.fListeners.remove(listener);
			}
		}
	}
	
	public void consoleActivated(IConsoleView consoleView, NIConsole console) {
		
		IWorkbenchPage page = consoleView.getViewSite().getPage();
		PageRegistry reg = getPageRegistry(page);
		if (reg != null) {
			reg.doActiveConsoleChanged(console, consoleView, Collections.EMPTY_LIST);
		}
	}
	
	public ToolSessionUIData getActiveToolSession(IWorkbenchPage page) {
		
		if (page == null) {
			return null;
		}
		
		PageRegistry reg = getPageRegistry(page);
		return reg.createSessionInfo(null);
	}
	
	public IWorkbenchPage findWorkbenchPage(ToolProcess process) {

		IWorkbenchPage activePage = UIAccess.getActiveWorkbenchPage(false);
		IWorkbenchPage page = null;
		synchronized (fPageRegistries) {
			for (PageRegistry reg : fPageRegistries.values()) {
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

	private void notifyToolSessionClosed(ToolProcess process) {
		
		ToolSessionUIData info = new ToolSessionUIData(process, null, null);
		
		System.out.println("closed: " + info.toString());
		
		Object[] listeners = fListeners.getListeners();
		for (Object obj : listeners) {
			((IToolRegistryListener) obj).toolSessionClosed(info);
		}
	}
	
	
	public void showConsole(NIConsole console, IWorkbenchPage page,
			boolean activate) {
		
		PageRegistry reg = getPageRegistry(page);
		reg.showConsole(console, activate);
	}
}
