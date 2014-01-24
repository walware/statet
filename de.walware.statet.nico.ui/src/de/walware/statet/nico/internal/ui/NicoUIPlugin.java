/*=============================================================================#
 # Copyright (c) 2006-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;

import de.walware.ecommons.ui.actions.WindowContributionsProvider;
import de.walware.ecommons.ui.util.ImageRegistryUtil;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.internal.ui.actions.NicoWindowContributions;
import de.walware.statet.nico.ui.IToolRegistryListener;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.ToolSessionUIData;


/**
 * The activator class controls the plug-in life cycle
 */
public final class NicoUIPlugin extends AbstractUIPlugin {
	
	
	public static final int INTERNAL_ERROR = 100;
	
	/** The shared instance. */
	private static NicoUIPlugin gPlugin;
	
	/**
	 * Returns the shared instance.
	 */
	public static NicoUIPlugin getDefault() {
		return gPlugin;
	}
	
	
	public static void logError(final int code, final String message, final Throwable e) {
		StatusManager.getManager().handle(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, code, message, e));
	}
	
	
	private ToolRegistry fToolRegistry;
	private WindowContributionsProvider fContributionProvider;
	
	private DecoratorsRegistry fUIDecoratorsRegistry;
	
	
	/**
	 * The constructor
	 */
	public NicoUIPlugin() {
		gPlugin = this;
	}
	
	
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		
		fToolRegistry = new ToolRegistry();
		fToolRegistry.addListener(new IToolRegistryListener() {
			@Override
			public void toolSessionActivated(final ToolSessionUIData sessionData) {
				if (sessionData.getProcess() != null) {
					fToolRegistry.removeListener(this);
					
					if (fContributionProvider == null) {
						fContributionProvider = new NicoWindowContributions();
					}
				}
			}
			@Override
			public void toolTerminated(final ToolSessionUIData sessionData) {
			}
		}, null);
	}
	
	@Override
	public void stop(final BundleContext context) throws Exception {
		try {
			if (fToolRegistry != null) {
				fToolRegistry.dispose();
				fToolRegistry = null;
			}
			if (fContributionProvider != null) {
				fContributionProvider.dispose();
				fContributionProvider = null;
			}
			if (fUIDecoratorsRegistry != null) {
				fUIDecoratorsRegistry = null;
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
		
		util.register(NicoUI.LOCTOOL_CANCEL_IMAGE_ID, ImageRegistryUtil.T_LOCTOOL, "cancel.png"); //$NON-NLS-1$
		util.register(NicoUI.LOCTOOLD_CANCEL_IMAGE_ID, ImageRegistryUtil.T_LOCTOOL_D, "cancel.png"); //$NON-NLS-1$
		util.register(NicoUI.LOCTOOL_PAUSE_IMAGE_ID, ImageRegistryUtil.T_LOCTOOL, "pause.png"); //$NON-NLS-1$
		util.register(NicoUI.LOCTOOLD_PAUSE_IMAGE_ID, ImageRegistryUtil.T_LOCTOOL_D, "pause.png"); //$NON-NLS-1$
		util.register(NicoUI.LOCTOOL_TERMINATE_IMAGE_ID, ImageRegistryUtil.T_LOCTOOL, "terminate.png"); //$NON-NLS-1$
		util.register(NicoUI.LOCTOOLD_TERMINATE_IMAGE_ID, ImageRegistryUtil.T_LOCTOOL_D, "terminate.png"); //$NON-NLS-1$
		
		util.register(NicoUI.OBJ_TASK_CONSOLECOMMAND_IMAGE_ID, ImageRegistryUtil.T_OBJ, "task-consolecommand.png"); //$NON-NLS-1$
		util.register(NicoUI.OBJ_TASK_DUMMY_IMAGE_ID, ImageRegistryUtil.T_OBJ, "task-dummy.png"); //$NON-NLS-1$
		util.register(NicoUI.OBJ_CONSOLECOMMAND_IMAGE_ID, ImageRegistryUtil.T_OBJ, "consolecommand.png"); //$NON-NLS-1$
	}
	
	public ToolRegistry getToolRegistry() {
		return fToolRegistry;
	}
	
	public synchronized DecoratorsRegistry getUIDecoratorsRegistry() {
		if (fUIDecoratorsRegistry == null) {
			fUIDecoratorsRegistry = new DecoratorsRegistry();
		}
		return fUIDecoratorsRegistry;
	}
	
}
