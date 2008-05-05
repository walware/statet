/*******************************************************************************
 * Copyright (c) 2005-2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License 
 * v2.1 or newer, which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.rserve;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class RServePlugin extends AbstractUIPlugin {
	
	
	public static final String PLUGIN_ID = "de.walware.statet.r.rserve";
	
	
	/** The shared instance. */
	private static RServePlugin gPlugin;
	
	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance.
	 */
	public static RServePlugin getDefault() {
		
		return gPlugin;
	}
	
	
	/**
	 * The constructor.
	 */
	public RServePlugin() {
		
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
