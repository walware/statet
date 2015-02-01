/*=============================================================================#
 # Copyright (c) 2011-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.console.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.walware.ecommons.ui.util.ImageRegistryUtil;

import de.walware.statet.r.internal.console.ui.snippets.RSnippets;


public class RConsoleUIPlugin extends AbstractUIPlugin {
	
	
	public static final String PLUGIN_ID = "de.walware.statet.r.console.ui"; //$NON-NLS-1$
	
	public static final String IMG_OBJ_SNIPPETS = PLUGIN_ID + "/image/obj/snippets"; //$NON-NLS-1$
	
	
	/** The shared instance */
	private static RConsoleUIPlugin gPlugin;
	
	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static RConsoleUIPlugin getDefault() {
		return gPlugin;
	}
	
	public static final void log(final IStatus status) {
		final Plugin plugin = getDefault();
		if (plugin != null) {
			plugin.getLog().log(status);
		}
	}
	
	
	private boolean fStarted;
	
	
	private RSnippets fRSnippets;
	
	
	/** Created via framework */
	public RConsoleUIPlugin() {
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
	
	
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		final ImageRegistryUtil util = new ImageRegistryUtil(this);
		
		util.register(IMG_OBJ_SNIPPETS, ImageRegistryUtil.T_OBJ, "snippets.png"); //$NON-NLS-1$
	}
	
	
	public synchronized RSnippets getRSnippets() {
		if (fRSnippets == null) {
			if (!fStarted) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			fRSnippets = new RSnippets();
		}
		return fRSnippets;
	}
	
}
