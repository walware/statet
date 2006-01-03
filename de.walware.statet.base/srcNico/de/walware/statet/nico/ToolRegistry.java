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

package de.walware.statet.nico;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleView;

import de.walware.statet.nico.console.NIConsole;
import de.walware.statet.nico.runtime.ToolController;


/**
 * 
 * TODO: remove debug messages
 */
public class ToolRegistry {

	
	private static Map<IWorkbenchPage, ToolRegistry> fgRegistries = new HashMap<IWorkbenchPage, ToolRegistry>();

	public static ToolRegistry getRegistry(IWorkbenchPage page) {
		
		if (page == null) {
			return null;
		}
		ToolRegistry reg = fgRegistries.get(page); // usual in UI thread. so no synchronization (?)
		if (reg == null) {
			reg = new ToolRegistry(page);
			fgRegistries.put(page, reg);
		}
		
		return reg;
	}
	
	
	private class ConsoleRemoveListener implements IConsoleListener {
		
		public void consolesAdded(IConsole[] consoles) {
			
		};
		
		public void consolesRemoved(IConsole[] consoles) {
			for (IConsole console : consoles) {
				if (console instanceof NIConsole) {
					if (console.equals(fActiveConsole)) {
						doActiveConsoleChanged(null, null);
					}
					ToolController controller = ((NIConsole) console).getController();
					if (controller != null) {
						notifyToolSessionClosed(controller);
					}
				}
			}
		};
	}
	
	
	private IWorkbenchPage fPage;
	private ConsoleRemoveListener fConsoleListener;
	
	private ListenerList fListeners = new ListenerList();
	private ToolController fActiveController;
	private NIConsole fActiveConsole;
	
	private ToolRegistry(IWorkbenchPage page) {
		
		fPage = page;
		fConsoleListener = new ConsoleRemoveListener();
		
		final IWorkbenchWindow window = page.getWorkbenchWindow();
		window.addPageListener(new IPageListener() {
			public void pageOpened(IWorkbenchPage page) {
			}
			public void pageActivated(IWorkbenchPage page) {
			}
			
			public void pageClosed(IWorkbenchPage page) {

				fgRegistries.remove(ToolRegistry.this);
				window.removePageListener(this);
				ConsolePlugin.getDefault().getConsoleManager().removeConsoleListener(fConsoleListener);
				fConsoleListener = null;
				fPage = null;
				
				System.out.println("page closed");
			}
		});
		
		ConsolePlugin.getDefault().getConsoleManager().addConsoleListener(fConsoleListener);
		
		doActiveConsoleChanged(null, null);
		
		System.out.println("page registry created");
	}
	
	
	public void addListener(IToolRegistryListener listener) {
		
		fListeners.add(listener);
	}
	
	public void removeListener(IToolRegistryListener listener) {
		
		fListeners.remove(listener);
	}
	
	public void consoleActivated(IConsoleView consoleView, NIConsole console) {

		doActiveConsoleChanged(console, consoleView);
	}
	
	/**
	 * 
	 * @return never <code>null</code>, but the fields can be <code>null</code>.
	 */
	public ToolSessionInfo getActiveToolSession() {
		
		ToolSessionInfo info = new ToolSessionInfo();
		info.fController = fActiveController;
		info.fConsole = fActiveConsole;
		return info;
	}

	
	private void doActiveConsoleChanged(NIConsole console, IViewPart source) {
		
		if (console == null) {
			console = searchConsole();
		}
		if ((console == fActiveConsole)
				|| (console != null && console.equals(fActiveConsole)) ) {
			return;
		}
		
		fActiveController = console.getController();
		fActiveConsole = console;
		
		notifyActiveToolSessionChanged(source);
	}
	
	private void notifyActiveToolSessionChanged(IViewPart source) {
		
		ToolSessionInfo info = new ToolSessionInfo();
		info.fController = fActiveController;
		info.fConsole = fActiveConsole;
		info.fSource = source;
		
		System.out.println("activated: " + info.toString());
		
		Object[] listeners = fListeners.getListeners();
		for (Object obj : listeners) {
			((IToolRegistryListener) obj).toolSessionActivated(info);
		}
	}
	
	private void notifyToolSessionClosed(ToolController controller) {
		
		ToolSessionInfo info = new ToolSessionInfo();
		info.fController = controller;
		
		System.out.println("closed: " + info.toString());
		
		Object[] listeners = fListeners.getListeners();
		for (Object obj : listeners) {
			((IToolRegistryListener) obj).toolSessionClosed(info);
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
