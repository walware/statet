/*******************************************************************************
 * Copyright (c) 2005-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.launching.winrgui;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;


/**
 */
public class WinRGuiConnectorPlugin extends Plugin {
	
	
	public static final String PLUGIN_ID = "de.walware.statet.r.debug.winrgui"; //$NON-NLS-1$
	
	
	//The shared instance.
	private static WinRGuiConnectorPlugin gPlugin;
	
	/**
	 * Returns the shared instance
	 * 
	 * @return the plug-in instance
	 */
	public static WinRGuiConnectorPlugin getDefault() {
		return gPlugin;
	}
	
	
	/**
	 * The constructor.
	 */
	public WinRGuiConnectorPlugin() {
		gPlugin = this;
	}
	
	
	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
	}
	
	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		gPlugin = null;
		super.stop(context);
	}
	
}
