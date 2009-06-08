/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.ui.ECommonsUI;


/**
 * Allows to create one handler per window
 */
public abstract class WindowContributionsProvider implements IWindowListener {
	
	
	protected static class WindowContributions {
		
		
		private final IWorkbenchWindow fWindow;
		private final List<IHandlerActivation> fHandlerActivations;
		
		
		public WindowContributions(final IWorkbenchWindow window) {
			fWindow = window;
			fHandlerActivations = new ArrayList<IHandlerActivation>();
			synchronized (fHandlerActivations) {
				init();
			}
		}
		
		
		public IWorkbenchWindow getWindow() {
			return fWindow;
		}
		
		protected void init() {
		}
		
		protected void add(final IHandlerActivation activation) {
			fHandlerActivations.add(activation);
		}
		
		protected void dispose() {
			final IWorkbenchWindow window = getWindow();
			final IHandlerService handlerService = (IHandlerService) window.getService(IHandlerService.class);
			
			synchronized (fHandlerActivations) {
				if (fHandlerActivations.isEmpty()) {
					return;
				}
				handlerService.deactivateHandlers(fHandlerActivations);
				for (final IHandlerActivation activation : fHandlerActivations) {
					activation.getHandler().dispose();
				}
				fHandlerActivations.clear();
			}
		}
		
	}
	
	
	private final List<WindowContributions> fList = new ArrayList<WindowContributions>(4);
	
	
	public WindowContributionsProvider() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final Display display = workbench.getDisplay();
		if (display != null) {
			display.asyncExec(new Runnable() {
				public void run() {
					workbench.addWindowListener(WindowContributionsProvider.this);
					final IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
					for (final IWorkbenchWindow window : windows) {
						init(window);
					}
				}
			});
		}
	}
	
	public void dispose() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench.isClosing()) {
			return;
		}
		
		workbench.removeWindowListener(this);
		final Display display = workbench.getDisplay();
		if (display != null) {
			display.asyncExec(new Runnable() {
				public void run() {
					final Iterator<WindowContributions> iter = fList.iterator();
					while (iter.hasNext()) {
						final WindowContributions contributions = iter.next();
						dispose(contributions);
					}
					fList.clear();
				}
			});
		}
	}
	
	
	public void windowOpened(final IWorkbenchWindow window) {
		init(window);
	}
	
	public void windowClosed(final IWorkbenchWindow window) {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench.isClosing()) {
			return;
		}
		
		final Iterator<WindowContributions> iter = fList.iterator();
		while (iter.hasNext()) {
			final WindowContributions contributions = iter.next();
			if (contributions.getWindow() == window) {
				iter.remove();
				dispose(contributions);
			}
		}
	}
	
	public void windowActivated(final IWorkbenchWindow window) {
	}
	
	public void windowDeactivated(final IWorkbenchWindow window) {
	}
	
	
	private void init(final IWorkbenchWindow window) {
		if (window == null || window.getShell() == null) {
			return;
		}
		final WindowContributions contributions;
		try {
			contributions = createWindowContributions(window);
		}
		catch (final Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, ECommonsUI.PLUGIN_ID, ICommonStatusConstants.INTERNAL_PLUGGED_IN,
					"Failed to create contributions for workbench window.", e));
			return;
		}
		fList.add(contributions);
	}
	
	private void dispose(final WindowContributions contributions) {
		try {
			contributions.dispose();
		}
		catch (final Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, ECommonsUI.PLUGIN_ID, ICommonStatusConstants.INTERNAL_PLUGGED_IN,
					"Failed to dipose contributions for workbench window.", e));
		}
	}
	
	
	protected abstract String getPluginId();
	
	protected abstract WindowContributions createWindowContributions(final IWorkbenchWindow window);
	
}
