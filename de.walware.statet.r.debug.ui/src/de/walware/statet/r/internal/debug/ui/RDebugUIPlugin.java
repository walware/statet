/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.IDisposable;
import de.walware.ecommons.ui.util.ImageDescriptorRegistry;
import de.walware.ecommons.ui.util.ImageRegistryUtil;

import de.walware.statet.r.internal.debug.ui.breakpoints.BreakpointsHelper;


public class RDebugUIPlugin extends AbstractUIPlugin {
	
	
	public static final String PLUGIN_ID= "de.walware.statet.r.debug.ui"; //$NON-NLS-1$
	
	
	private static final String R_IMAGES_ID= "de.walware.statet.r.images"; //$NON-NLS-1$
	
	public static final String IMG_OBJ_R_BREAKPOINT= R_IMAGES_ID + "/obj/r_breakpoint"; //$NON-NLS-1$
	public static final String IMG_OBJ_R_BREAKPOINT_DISABLED= R_IMAGES_ID + "/obj/r_breakpoint.disabled"; //$NON-NLS-1$
	public static final String IMG_OBJ_R_TOPLEVEL_BREAKPOINT= R_IMAGES_ID + "/obj/r_toplevel_breakpoint"; //$NON-NLS-1$
	public static final String IMG_OBJ_R_TOPLEVEL_BREAKPOINT_DISABLED= R_IMAGES_ID + "/obj/r_toplevel_breakpoint.disabled"; //$NON-NLS-1$
	
	public static final String IMG_OBJ_R_SOURCE_FROM_RUNTIME= R_IMAGES_ID + "/obj/r_source.runtime"; //$NON-NLS-1$
	
	
	/** The shared instance */
	private static RDebugUIPlugin instance;
	
	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static RDebugUIPlugin getDefault() {
		return instance;
	}
	
	
	private boolean started;
	
	private List<IDisposable> disposables;
	
	private ImageDescriptorRegistry imageDescriptorRegistry;
	private BreakpointsHelper breakpointsHelper;
	
	
	public RDebugUIPlugin() {
	}
	
	
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		instance= this;
		
		this.disposables= new ArrayList<>();
		
		this.breakpointsHelper= new BreakpointsHelper();
		this.disposables.add(this.breakpointsHelper);
		
		synchronized (this) {
			this.started= true;
		}
	}
	
	@Override
	public void stop(final BundleContext context) throws Exception {
		try {
			synchronized (this) {
				this.started= false;
				
				this.imageDescriptorRegistry= null;
				this.breakpointsHelper= null;
			}
			
			for (final IDisposable listener : this.disposables) {
				try {
					listener.dispose();
				}
				catch (final Throwable e) {
					getLog().log(new Status(IStatus.ERROR, RDebugUIPlugin.PLUGIN_ID, ICommonStatusConstants.INTERNAL_PLUGGED_IN,
							"Error occured while disposing a module.", e )); 
				}
			}
			this.disposables= null;
		}
		finally {
			instance= null;
			super.stop(context);
		}
	}
	
	
	@Override
	protected void initializeImageRegistry(final ImageRegistry reg) {
		final ImageRegistryUtil util= new ImageRegistryUtil(this);
		
		util.register(IMG_OBJ_R_BREAKPOINT, ImageRegistryUtil.T_OBJ, "r_breakpoint.png"); //$NON-NLS-1$
		util.register(IMG_OBJ_R_BREAKPOINT_DISABLED, ImageRegistryUtil.T_OBJ, "r_breakpoint-disabled.png"); //$NON-NLS-1$
		util.register(IMG_OBJ_R_TOPLEVEL_BREAKPOINT, ImageRegistryUtil.T_OBJ, "r_toplevel_breakpoint.png"); //$NON-NLS-1$
		util.register(IMG_OBJ_R_TOPLEVEL_BREAKPOINT_DISABLED, ImageRegistryUtil.T_OBJ, "r_toplevel_breakpoint-disabled.png"); //$NON-NLS-1$
		
		util.register(IMG_OBJ_R_SOURCE_FROM_RUNTIME, ImageRegistryUtil.T_OBJ, "r_source-runtime.png"); //$NON-NLS-1$
	}
	
	public synchronized ImageDescriptorRegistry getImageDescriptorRegistry() {
		if (this.imageDescriptorRegistry == null) {
			if (!this.started) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			this.imageDescriptorRegistry= new ImageDescriptorRegistry();
		}
		return this.imageDescriptorRegistry;
	}
	
}
