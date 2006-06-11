/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ui.util;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import de.walware.eclipsecommon.ui.util.ColorManager;
import de.walware.statet.base.StatetPlugin;


/**
 * Access to UI resources from other threads.
 */
public class UIAccess {

	private static class GetActiveWorkbenchWindowRunnable implements Runnable {
		
		private IWorkbenchWindow window;
		
		public void run() {
			
			window = StatetPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		}
	}
	
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		
		GetActiveWorkbenchWindowRunnable uiAccess = new GetActiveWorkbenchWindowRunnable();
		StatetPlugin.getDisplay().syncExec(uiAccess);
		return uiAccess.window;
	}
	
	public static IWorkbenchPage getActiveWorkbenchPage() {
		
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getActivePage();
		}
		return null;
	}

	private static final class GetColorRunnable implements Runnable {
		
		private ColorManager fColorManager;
		private RGB fRgb;
		private Color fColor;
		
		GetColorRunnable(ColorManager colorManager, RGB rgb) {

			fColorManager = colorManager;
			fRgb = rgb;
		}
		
		public void run() {
			
			fColor = fColorManager.getColor(fRgb);
		}
	}
	
	public static Color getColor(ColorManager colorManager, RGB rgb) {
		
		GetColorRunnable uiAccess = new GetColorRunnable(colorManager, rgb);
		StatetPlugin.getDisplay().syncExec(uiAccess);
		return uiAccess.fColor;
	}
}
