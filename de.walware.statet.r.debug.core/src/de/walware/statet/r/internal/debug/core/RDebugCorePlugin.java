/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;


public class RDebugCorePlugin extends Plugin {
	
	
	public static final String PLUGIN_ID = "de.walware.statet.r.debug.core"; //$NON-NLS-1$
	
	
	/** The shared instance */
	private static RDebugCorePlugin gPlugin;
	
	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static RDebugCorePlugin getDefault() {
		return gPlugin;
	}
	
	public static final void log(final IStatus status) {
		final Plugin plugin = getDefault();
		if (plugin != null) {
			plugin.getLog().log(status);
		}
	}
	
	
	private boolean fStarted;
	
	
	/** Created via framework */
	public RDebugCorePlugin() {
	}
	
	
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		gPlugin = this;
		
		fStarted = true;
	}
	
	@Override
	public void stop(final BundleContext context) throws Exception {
		try {
			synchronized (this) {
				fStarted = false;
			}
		}
		finally {
			gPlugin = null;
			super.stop(context);
		}
	}
	
}
