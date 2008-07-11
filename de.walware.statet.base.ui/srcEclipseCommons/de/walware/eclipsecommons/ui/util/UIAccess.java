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

package de.walware.eclipsecommons.ui.util;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;


/**
 * Access to UI resources from other threads.
 */
public class UIAccess {
	
	/**
	 * Returns the display for this workbench.
	 * 
	 * @return a display.
	 */
	public static Display getDisplay() {
		return PlatformUI.getWorkbench().getDisplay();
	}
	
	/**
	 * Searches a appropriate display.
	 * <p>
	 * Order for search: display of specified shell, display for the workbench.
	 * 
	 * @param shell optional shell
	 * @return display
	 */
	public static Display getDisplay(final Shell shell) {
		Display display = null;
		if (shell != null) {
			display = shell.getDisplay();
		}
		if (display == null) {
			display = PlatformUI.getWorkbench().getDisplay();
		}
		return display;
	}
	
	public static IWorkbenchWindow getActiveWorkbenchWindow(final boolean inUIThread) {
		if (inUIThread) {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		}
		final Display display = getDisplay();
		if (display.getThread() == Thread.currentThread()) {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		}
		final AtomicReference<IWorkbenchWindow> windowRef = new AtomicReference<IWorkbenchWindow>();
		display.syncExec(new Runnable() {
			public void run() {
				windowRef.set(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			}
		});
		return windowRef.get();
	}
	
	public static IWorkbenchPage getActiveWorkbenchPage(final boolean inUIThread) {
		final IWorkbenchWindow window = getActiveWorkbenchWindow(inUIThread);
		if (window != null) {
			return window.getActivePage();
		}
		return null;
	}
	
	public static IWorkbenchPart getActiveWorkbenchPart(final boolean inUIThread) {
		final IWorkbenchWindow window = getActiveWorkbenchWindow(inUIThread);
		if (window != null) {
			final IWorkbenchPage activePage = window.getActivePage();
			if (activePage != null) {
				return activePage.getActivePart();
			}
		}
		return null;
	}
	
	public static Shell getActiveWorkbenchShell(final boolean inUIThread) {
		final IWorkbenchWindow window = getActiveWorkbenchWindow(inUIThread);
		if (window != null) {
			return window.getShell();
		}
		return null;
	}
	
	public static Color getColor(final ColorManager colorManager, final RGB rgb) {
		final AtomicReference<Color> colorRef = new AtomicReference<Color>();
		getDisplay().syncExec(new Runnable() {
			
			public void run() {
				colorRef.set(colorManager.getColor(rgb));
			}
		});
		return colorRef.get();
	}
	
	
	public static interface CheckedRunnable {
		
		public void run() throws CoreException;
	}
	
	
	public static void checkedSyncExec(final CheckedRunnable runnable)
			throws CoreException {
		final AtomicReference<CoreException> error = new AtomicReference<CoreException>();
		UIAccess.getDisplay().syncExec(new Runnable() {
			
			public void run() {
				try {
					runnable.run();
				} catch (final CoreException e) {
					error.set(e);
				}
			}
		});
		if (error.get() != null) {
			throw error.get();
		}
	}
	
	/**
	 * Tests is the control is not <code>null</code> and not disposed.
	 */
	public static final boolean isOkToUse(final Control control) {
		return (control != null) && (Display.getCurrent() != null)
				&& !control.isDisposed();
	}
	
	/**
	 * Tests is the viewer is not <code>null</code> and its control is not
	 * disposed.
	 */
	public static final boolean isOkToUse(final Viewer viewer) {
		Control control;
		return ((viewer != null) && ((control = viewer.getControl()) != null)
				&& !control.isDisposed() && (Display.getCurrent() != null));
	}
	
	
	private UIAccess() {
	}
	
}
