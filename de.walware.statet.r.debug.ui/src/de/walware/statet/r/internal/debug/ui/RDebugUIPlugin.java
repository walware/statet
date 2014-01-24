/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.walware.ecommons.ui.util.ImageDescriptorRegistry;
import de.walware.ecommons.ui.util.ImageRegistryUtil;


public class RDebugUIPlugin extends AbstractUIPlugin {
	
	
	public static final String PLUGIN_ID = "de.walware.statet.r.debug.ui"; //$NON-NLS-1$
	
	
	private static final String R_IMAGES_ID = "de.walware.statet.r.images"; //$NON-NLS-1$
	
	public static final String IMG_OBJ_R_BREAKPOINT = R_IMAGES_ID + "/obj/r_breakpoint"; //$NON-NLS-1$
	public static final String IMG_OBJ_R_BREAKPOINT_DISABLED = R_IMAGES_ID + "/obj/r_breakpoint.disabled"; //$NON-NLS-1$
	public static final String IMG_OBJ_R_TOPLEVEL_BREAKPOINT = R_IMAGES_ID + "/obj/r_toplevel_breakpoint"; //$NON-NLS-1$
	public static final String IMG_OBJ_R_TOPLEVEL_BREAKPOINT_DISABLED = R_IMAGES_ID + "/obj/r_toplevel_breakpoint.disabled"; //$NON-NLS-1$
	
	public static final String IMG_OBJ_R_SOURCE_FROM_RUNTIME = R_IMAGES_ID + "/obj/r_source.runtime"; //$NON-NLS-1$
	
	
	/** The shared instance */
	private static RDebugUIPlugin gPlugin;
	
	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static RDebugUIPlugin getDefault() {
		return gPlugin;
	}
	
	
	private boolean fStarted;
	
	private ImageDescriptorRegistry fImageDescriptorRegistry;
	
	
	public RDebugUIPlugin() {
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
				
				fImageDescriptorRegistry = null;
			}
		}
		finally {
			gPlugin = null;
			super.stop(context);
		}
	}
	
	
	@Override
	protected void initializeImageRegistry(final ImageRegistry reg) {
		final ImageRegistryUtil util = new ImageRegistryUtil(this);
		
		util.register(IMG_OBJ_R_BREAKPOINT, ImageRegistryUtil.T_OBJ, "r_breakpoint.png"); //$NON-NLS-1$
		util.register(IMG_OBJ_R_BREAKPOINT_DISABLED, ImageRegistryUtil.T_OBJ, "r_breakpoint-disabled.png"); //$NON-NLS-1$
		util.register(IMG_OBJ_R_TOPLEVEL_BREAKPOINT, ImageRegistryUtil.T_OBJ, "r_toplevel_breakpoint.png"); //$NON-NLS-1$
		util.register(IMG_OBJ_R_TOPLEVEL_BREAKPOINT_DISABLED, ImageRegistryUtil.T_OBJ, "r_toplevel_breakpoint-disabled.png"); //$NON-NLS-1$
		
		util.register(IMG_OBJ_R_SOURCE_FROM_RUNTIME, ImageRegistryUtil.T_OBJ, "r_source-runtime.png"); //$NON-NLS-1$
	}
	
	public synchronized ImageDescriptorRegistry getImageDescriptorRegistry() {
		if (fImageDescriptorRegistry == null) {
			if (!fStarted) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			fImageDescriptorRegistry = new ImageDescriptorRegistry();
		}
		return fImageDescriptorRegistry;
	}
	
}
