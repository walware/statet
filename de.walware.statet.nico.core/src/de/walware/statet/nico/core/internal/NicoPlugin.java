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

package de.walware.statet.nico.core.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

import de.walware.statet.nico.core.NicoCore;


/**
 * The activator class controls the plug-in life cycle
 */
public class NicoPlugin extends Plugin {


	public static final int INTERNAL_ERROR = 100;
	public static final int EXTERNAL_ERROR = 105;
	

	/** The shared instance. */
	private static NicoPlugin gPlugin;
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static NicoPlugin getDefault() {
		
		return gPlugin;
	}

	
	/**
	 * The constructor
	 */
	public NicoPlugin() {
		
		gPlugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		
		super.start(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		
		gPlugin = null;
		super.stop(context);
	}

	
	public static void log(IStatus status) {
		
		getDefault().getLog().log(status);
	}
	
	public static void log(int code, String message, Throwable e) {
		
		log(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, code, message, e)); 
	}

}
