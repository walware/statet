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

package de.walware.statet.base;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.walware.eclipsecommon.ui.util.ColorManager;
import de.walware.statet.internal.ui.StatetMessages;


/**
 * The main plugin class to be used in the desktop.
 */
public class StatetPlugin extends AbstractUIPlugin {

	/**
	 * Plugin-ID
	 * Value: @value
	 */
	public static final String ID = "de.walware.statet.base";
	
	/** The shared instance. */
	private static StatetPlugin fgPlugin;
	
	
	private ColorManager fColorManager;
	
	
	/**
	 * The constructor.
	 */
	public StatetPlugin() {
		fgPlugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		
		try {
			if (fColorManager != null) {
				fColorManager.dispose();
				fColorManager = null;
			}
		
		} finally {
			super.stop(context);
			fgPlugin = null;
		}
	}

	public synchronized ColorManager getColorManager() {
		
		if (fColorManager == null)
			fColorManager = new ColorManager();
		
		return fColorManager;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Returns the shared instance.
	 */
	public static StatetPlugin getDefault() {
		return fgPlugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("de.walware.statet.base", path);
	}

	
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}
	
	public static Shell getActiveWorkbenchShell() {
		
		 IWorkbenchWindow window = getActiveWorkbenchWindow();
		 if (window != null) {
		 	return window.getShell();
		 }
		 
		 return null;
	}
	
	public static IWorkbenchPage getActivePage() {
		
		IWorkbenchWindow window = getDefault().getWorkbench().getActiveWorkbenchWindow();
		if (window != null)
			return window.getActivePage();
		return null;
	}
	
	
	public static void log(IStatus status) {
		
		getDefault().getLog().log(status);
	}
	public static void logUnexpectedError(Throwable e) {
		
		log(new Status(IStatus.ERROR, ID, IStatetStatusConstants.INTERNAL_ERROR, StatetMessages.InternalError_UnexpectedException, e)); 
	}

}
