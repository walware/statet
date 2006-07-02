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

import de.walware.statet.base.IStatetStatusConstants;


/**
 * The activator class controls the plug-in life cycle
 */
public class NicoPlugin extends Plugin {

	/**
	 * Plugin-ID
	 * Value: @value
	 */
	public static final String PLUGIN_ID = "de.walware.statet.nico.core"; //$NON-NLS-1$

	/** The shared instance. */
	private static NicoPlugin gPlugin;
	
	/**
	 * Returns the shared instance.
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

	
	public static void logUnexpectedError(Throwable e) {
		
		getDefault().getLog().log(new Status(
				IStatus.ERROR, 
				PLUGIN_ID, 
				IStatetStatusConstants.INTERNAL_ERROR, 
				Messages.InternalError_UnexpectedException_message, 
				e)); 
	}

}
