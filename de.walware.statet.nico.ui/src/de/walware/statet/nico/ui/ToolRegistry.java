/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.console.NIConsole;


/**
 * 
 * TODO: remove debug messages
 */
public class ToolRegistry {

	
	private static ToolRegistry fgRegistry;
	
	public static ToolRegistry getRegistry() {
		
		if (fgRegistry == null) {
			fgRegistry = new ToolRegistry();
		}
		return fgRegistry;
	}
		
	
	private class PageRegistry {
		
		private IWorkbenchPage fPage;
		private IPageListener fPageListener;

		private ToolProcess fActiveProcess;
		private NIConsole fActiveConsole;
		
		private ListenerList fLocalListeners = new ListenerList(ListenerList.IDENTITY);
		
		PageRegistry(IWorkbenchPage page) {
			
			fPage = page;
			
			fPageListener = new IPageListener() {
				public void pageOpened(IWorkbenchPage page) {
				}
				public void pageActivated(IWorkbenchPage page) {
				}
				
				public void pageClosed(IWorkbenchPage page) {
	
					dispose();
				}
			};
			fPage.getWorkbenchWindow().addPageListener(fPageListener);
		}
		
		private void dispose() {
			
			fPageRegistries.remove(ToolRegistry.this);
			fPage.getWorkbenchWindow().removePageListener(fPageListener);
			doActiveConsoleChanged(null, null);
			fLocalListeners.clear();
			fPage = null;
		}

		private void doActiveConsoleChanged(NIConsole console, IViewPart source) {
			
			if (console == null) {
				console = searchConsole();
			}
			if ((console == fActiveConsole)
					|| (console != null && console.equals(fActiveConsole)) ) {
				return;
			}
			
			fActiveProcess = (console != null) ? console.getProcess() : null;
			fActiveConsole = console;
			
			notifyActiveToolSessionChanged(source);
		}
		
		private void notifyActiveToolSessionChanged(IViewPart source) {
			
			ToolSessionInfo info = new ToolSessionInfo();
			info.fProcess = fActiveProcess;
			info.fConsole = fActiveConsole;
			info.fSource = source;
			
			System.out.println("activated: " + info.toString());
			
			Object[] listeners = fLocalListeners.getListeners();
			for (Object obj : listeners) {
				((IToolRegistryListener) obj).toolSessionActivated(info);
			}
		}

		private NIConsole searchConsole() {
			
			IWorkbenchPart part = fPage.getActivePart();
			if (part instanceof IConsoleView) {
				IConsole console = ((IConsoleView) part).getConsole();
				if (console instanceof NIConsole) {
					return ((NIConsole) console);
				}
			}

			List<IConsoleView> consoleViews = NIConsole.getConsoleViews(fPage);
			for (IConsoleView view : consoleViews) {
				IConsole console = view.getConsole();
				if (console instanceof NIConsole) {
					return ((NIConsole) console);
				}
			}
			
			return null;
		}

	}
		
	private class LaunchesListener implements ILaunchesListener {

		public void launchesAdded(ILaunch[] launches) {
		}
		public void launchesChanged(ILaunch[] launches) {
		}

		public void launchesRemoved(ILaunch[] launches) {
			
			List<ToolProcess> list = new ArrayList<ToolProcess>();
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

			for (PageRegistry reg : fPageRegistries.values()) {
				if (list.contains(reg.fActiveProcess)) {
					reg.doActiveConsoleChanged(null, null);
				}
			}
			for (ToolProcess process : list) {
				notifyToolSessionClosed(process);
			}
			
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
	
	
	private Map<IWorkbenchPage, PageRegistry> fPageRegistries = new HashMap<IWorkbenchPage, PageRegistry>();
	
	private LaunchesListener fLaunchesListener;
	
	private ListenerList fListeners = new ListenerList();
	
	private ToolRegistry() {
		
		fLaunchesListener = new LaunchesListener();
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(fLaunchesListener);
	}
	
	private void dispose() {
		
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(fLaunchesListener);
		fLaunchesListener = null;
		
		for (PageRegistry reg : fPageRegistries.values()) {
			reg.dispose();
		}
		
		System.out.println("Registry closed.");
	}

	private PageRegistry getPageRegistry(IWorkbenchPage page) {
		
		PageRegistry reg = fPageRegistries.get(page);
		if (reg == null) {
			reg = new PageRegistry(page);
			fPageRegistries.put(page, reg);
		}
		return reg;
	}
	
	
	
	public void addListener(IToolRegistryListener listener, IWorkbenchPage page) {
		
		fListeners.add(listener);
		if (page != null) {
			getPageRegistry(page).fLocalListeners.add(listener);
		}
	}
	
	public void removeListener(IToolRegistryListener listener) {
		
		fListeners.remove(listener);
		for (PageRegistry reg : fPageRegistries.values()) {
			reg.fLocalListeners.remove(listener);
		}
	}
	
	public void consoleActivated(IConsoleView consoleView, NIConsole console) {
		
		IWorkbenchPage page = consoleView.getViewSite().getPage();
		if (page != null) {
			PageRegistry reg = getPageRegistry(page);
			reg.doActiveConsoleChanged(console, consoleView);
		}
	}
	
	/**
	 * 
	 * @return never <code>null</code>, but the fields can be <code>null</code>.
	 */
	public ToolSessionInfo getActiveToolSession(IWorkbenchPage page) {
		
		if (page == null) {
			return null;
		}
		
		PageRegistry reg = getPageRegistry(page);
		ToolSessionInfo info = new ToolSessionInfo();
		info.fProcess = reg.fActiveProcess;
		info.fConsole = reg.fActiveConsole;
		return info;
	}

	private void notifyToolSessionClosed(ToolProcess process) {
		
		ToolSessionInfo info = new ToolSessionInfo();
		info.fProcess = process;
		
		System.out.println("closed: " + info.toString());
		
		Object[] listeners = fListeners.getListeners();
		for (Object obj : listeners) {
			((IToolRegistryListener) obj).toolSessionClosed(info);
		}
	}
}
