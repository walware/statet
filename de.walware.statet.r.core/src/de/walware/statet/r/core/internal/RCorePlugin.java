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

package de.walware.statet.r.core.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class RCorePlugin extends Plugin {

	
	//The shared instance.
	private static RCorePlugin gPlugin;
	
	/**
	 * The constructor.
	 */
	public RCorePlugin() {
		
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
		
		super.stop(context);
		gPlugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static RCorePlugin getDefault() {
		
		return gPlugin;
	}

	
	public static void log(IStatus status) {
		
		getDefault().getLog().log(status);
	}
}
