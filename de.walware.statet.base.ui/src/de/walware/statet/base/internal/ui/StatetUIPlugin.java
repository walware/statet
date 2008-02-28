/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.internal.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.walware.eclipsecommons.ICommonStatusConstants;
import de.walware.eclipsecommons.ui.util.ColorManager;
import de.walware.eclipsecommons.ui.util.ImageRegistryUtil;

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
	
	
	private ColorManager fColorManager;
	private ImageRegistry fImageRegistry;
	private WorkbenchLabelProvider fWorkbenchLabelProvider;
	
	
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
		super.start(context);
	}
	
	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		try {
			if (fColorManager != null) {
				fColorManager.dispose();
				fColorManager = null;
			}
			if (fImageRegistry != null) {
				fImageRegistry.dispose();
				fImageRegistry = null;
			}
			if (fWorkbenchLabelProvider != null) {
				fWorkbenchLabelProvider.dispose();
				fWorkbenchLabelProvider = null;
			}
		}
		finally {
			super.stop(context);
			gPlugin = null;
		}
	}
	
	@Override
	protected void initializeImageRegistry(final ImageRegistry reg) {
		fImageRegistry = reg;
		final ImageRegistryUtil util = new ImageRegistryUtil(this);
		util.register(StatetImages.LOCTOOL_FILTER, ImageRegistryUtil.T_LOCTOOL, "filter_view.gif"); //$NON-NLS-1$
		util.register(StatetImages.LOCTOOLD_FILTER, ImageRegistryUtil.T_LOCTOOL_D, "filter_view.gif"); //$NON-NLS-1$
		util.register(StatetImages.LOCTOOL_EXPANDALL, ImageRegistryUtil.T_LOCTOOL, "expandall.gif"); //$NON-NLS-1$
		util.register(StatetImages.LOCTOOL_COLLAPSEALL, ImageRegistryUtil.T_LOCTOOL, "collapseall.gif"); //$NON-NLS-1$
		util.register(StatetImages.LOCTOOL_SCROLLLOCK, ImageRegistryUtil.T_LOCTOOL, "scrolllock.gif"); //$NON-NLS-1$
		util.register(StatetImages.LOCTOOL_PAUSE, ImageRegistryUtil.T_LOCTOOL, "pause.gif"); //$NON-NLS-1$
		util.register(StatetImages.LOCTOOLD_PAUSE, ImageRegistryUtil.T_LOCTOOL_D, "pause.gif"); //$NON-NLS-1$
		
		util.register(StatetImages.CONTENTASSIST_TEMPLATE, ImageRegistryUtil.T_OBJ, "template_proposal.gif"); //$NON-NLS-1$
		util.register(StatetImages.LAUNCHCONFIG_MAIN, ImageRegistryUtil.T_OBJ, "main_tab.gif"); //$NON-NLS-1$
		util.register(StatetImages.OBJ_COMMAND, ImageRegistryUtil.T_OBJ, "command.png"); //$NON-NLS-1$
		util.register(StatetImages.OBJ_COMMAND_DUMMY, ImageRegistryUtil.T_OBJ, "command-dummy.png"); //$NON-NLS-1$
		util.register(StatetImages.LOCTOOL_SORT_ALPHA, ImageRegistryUtil.T_LOCTOOL, "sort_alpha.gif"); //$NON-NLS-1$
		util.register(StatetImages.OVR_DEFAULT_MARKER, ImageRegistryUtil.T_OVR, "default_marker.gif"); //$NON-NLS-1$
	}
	
	public synchronized ColorManager getColorManager() {
		if (fColorManager == null)
			fColorManager = new ColorManager();
		
		return fColorManager;
	}
	
	/**
	 * To access decoration.
	 */
	public WorkbenchLabelProvider getWorkbenchLabelProvider() {
		if (fWorkbenchLabelProvider == null) {
			fWorkbenchLabelProvider = new WorkbenchLabelProvider();
		}
		return fWorkbenchLabelProvider;
	}
	
}
