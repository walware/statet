/*******************************************************************************
 * Copyright (c) 2006-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.core;

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
	
	
	private ToolLifecycleManager fTools;
	
	private ResourceMappingManager fResourceMappings;
	
	
	/**
	 * The constructor
	 */
	public NicoPlugin() {
		gPlugin = this;
	}
	
	
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		
		fResourceMappings = new ResourceMappingManager();
	}
	
	@Override
	public void stop(final BundleContext context) throws Exception {
		if (fTools != null) {
			fTools.dispose();
			fTools = null;
		}
		if (fResourceMappings != null) {
			fResourceMappings.dispose();
		}
		
		gPlugin = null;
		super.stop(context);
	}
	
	public synchronized ToolLifecycleManager getToolLifecycle() {
		if (fTools == null) {
			fTools = new ToolLifecycleManager();
		}
		return fTools;
	}
	
	public ResourceMappingManager getMappingManager() {
		return fResourceMappings;
	}
	
	public static void log(final IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public static void logError(final int code, final String message, final Throwable e) {
		log(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, code, message, e));
	}
	
}
