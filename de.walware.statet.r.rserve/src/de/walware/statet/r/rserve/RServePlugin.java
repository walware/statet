/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License v2.0
 * or newer, which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.rserve;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class RServePlugin extends AbstractUIPlugin {

	
	public static final String ID = "de.walware.statet.r.rserve";
		
	
	/** The shared instance. */
	private static RServePlugin fgPlugin;
	
	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance.
	 */
	public static RServePlugin getDefault() {
		
		return fgPlugin;
	}

	
	/**
	 * The constructor.
	 */
	public RServePlugin() {
		
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
		
		super.stop(context);
		fgPlugin = null;
	}

}
