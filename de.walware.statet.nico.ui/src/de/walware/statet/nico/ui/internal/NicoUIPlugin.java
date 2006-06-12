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

package de.walware.statet.nico.ui.internal;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class NicoUIPlugin extends AbstractUIPlugin {

	/**
	 * Plugin-ID
	 * Value: @value
	 */
	public static final String ID = "de.walware.statet.nico.ui";

	/** The shared instance. */
	private static NicoUIPlugin gPlugin;
	
	/**
	 * Returns the shared instance.
	 */
	public static NicoUIPlugin getDefault() {
		
		return gPlugin;
	}


	private ToolRegistry fToolRegistry;

	
	/**
	 * The constructor
	 */
	public NicoUIPlugin() {
		
		gPlugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		
		super.start(context);
		
		fToolRegistry = new ToolRegistry();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		
		gPlugin = null;
		super.stop(context);
		
		fToolRegistry.dispose();
		fToolRegistry = null;
	}

	
	public ToolRegistry getToolRegistry() {
		
		return fToolRegistry;
	}
}
