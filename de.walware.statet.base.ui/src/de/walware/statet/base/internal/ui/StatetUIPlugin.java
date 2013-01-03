/*******************************************************************************
 * Copyright (c) 2005-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.IDisposable;
import de.walware.ecommons.ui.util.ImageRegistryUtil;

import de.walware.statet.base.ui.StatetDebugImages;
import de.walware.statet.base.ui.StatetImages;


/**
 * The main plugin class to be used in the desktop.
 */
public class StatetUIPlugin extends AbstractUIPlugin {
	
	/**
	 * Plugin-ID
	 * Value: @value
	 */
	public static final String PLUGIN_ID = "de.walware.statet.base.ui"; //$NON-NLS-1$
	
	
	public static void log(final IStatus status) {
		if (status != null) {
			getDefault().getLog().log(status);
		}
	}
	
	public static void logError(final int code, final String message, final Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, code, message, e));
	}
	
	public static void logUnexpectedError(final Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, ICommonStatusConstants.INTERNAL_ERROR, StatetMessages.InternalError_UnexpectedException, e));
	}
	
	
	/** The shared instance. */
	private static StatetUIPlugin gPlugin;
	
	/**
	 * Returns the shared instance.
	 */
	public static StatetUIPlugin getDefault() {
		return gPlugin;
	}
	
	
	private List<IDisposable> fDisposables;
	
	
	/**
	 * The constructor.
	 */
	public StatetUIPlugin() {
		gPlugin = this;
	}
	
	
	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		fDisposables = new ArrayList<IDisposable>();
		super.start(context);
	}
	
	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		try {
			for (final IDisposable d : fDisposables) {
				try {
					d.dispose();
				}
				catch (final Throwable e) {
					logError(-1, "Error occured when dispose module", e); //$NON-NLS-1$
				}
			}
			fDisposables = null;
		}
		finally {
			gPlugin = null;
			super.stop(context);
		}
	}
	
	@Override
	protected void initializeImageRegistry(final ImageRegistry reg) {
		final ImageRegistryUtil util = new ImageRegistryUtil(this);
		util.register(StatetImages.LAUNCHCONFIG_MAIN, ImageRegistryUtil.T_OBJ, "main_tab.gif"); //$NON-NLS-1$
		
		util.register(StatetImages.OBJ_IMPORT, ImageRegistryUtil.T_OBJ, "ltk-import.png"); //$NON-NLS-1$
		util.register(StatetImages.OBJ_CLASS, ImageRegistryUtil.T_OBJ, "ltk-class.png"); //$NON-NLS-1$
		util.register(StatetImages.OBJ_CLASS_EXT, ImageRegistryUtil.T_OBJ, "ltk-class_ext.png"); //$NON-NLS-1$
		
		util.register(StatetImages.TOOL_REFRESH, ImageRegistryUtil.T_TOOL, "refresh.png"); //$NON-NLS-1$
		util.register(StatetImages.TOOLD_REFRESH, ImageRegistryUtil.T_TOOLD, "refresh.png"); //$NON-NLS-1$
		
		util.register(StatetDebugImages.OVR_BREAKPOINT_INSTALLED, ImageRegistryUtil.T_OVR, "installed.png"); //$NON-NLS-1$
		util.register(StatetDebugImages.OVR_BREAKPOINT_INSTALLED_DISABLED, ImageRegistryUtil.T_OVR, "installed-disabled.png"); //$NON-NLS-1$
		
		util.register(StatetDebugImages.OVR_BREAKPOINT_CONDITIONAL, ImageRegistryUtil.T_OVR, "conditional.png"); //$NON-NLS-1$
		util.register(StatetDebugImages.OVR_BREAKPOINT_CONDITIONAL_DISABLED, ImageRegistryUtil.T_OVR, "conditional-disabled.png"); //$NON-NLS-1$
		
		util.register(StatetDebugImages.OVR_METHOD_BREAKPOINT_ENTRY, ImageRegistryUtil.T_OVR, "entry.png"); //$NON-NLS-1$
		util.register(StatetDebugImages.OVR_METHOD_BREAKPOINT_ENTRY_DISABLED, ImageRegistryUtil.T_OVR, "entry-disabled.png"); //$NON-NLS-1$
		util.register(StatetDebugImages.OVR_METHOD_BREAKPOINT_EXIT, ImageRegistryUtil.T_OVR, "exit.png"); //$NON-NLS-1$
		util.register(StatetDebugImages.OVR_METHOD_BREAKPOINT_EXIT_DISABLED, ImageRegistryUtil.T_OVR, "exit-disabled.png"); //$NON-NLS-1$
	}
	
	public void registerPluginDisposable(final IDisposable d) {
		final List<IDisposable> disposables = fDisposables;
		if (disposables != null) {
			disposables.add(d);
		}
		else {
			throw new IllegalStateException();
		}
	}
	
}
