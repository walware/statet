/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.walware.eclipsecommons.ui.util.ColorManager;

import de.walware.statet.base.internal.ui.StatetMessages;


/**
 * The main plugin class to be used in the desktop.
 */
public class StatetPlugin extends AbstractUIPlugin {

	/**
	 * Plugin-ID
	 * Value: @value
	 */
	public static final String PLUGIN_ID = "de.walware.statet.base.ui";
	
	/** The shared instance. */
	private static StatetPlugin gPlugin;
	
	
	private ColorManager fColorManager;
	
	
	/**
	 * The constructor.
	 */
	public StatetPlugin() {
		gPlugin = this;
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
			gPlugin = null;
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
		return gPlugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	
	public static void log(IStatus status) {
		
		if (status != null) {
			getDefault().getLog().log(status);
		}
	}
	public static void logUnexpectedError(Throwable e) {
		
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatetStatusConstants.INTERNAL_ERROR, StatetMessages.InternalError_UnexpectedException, e)); 
	}

}
