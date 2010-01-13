/*******************************************************************************
 * Copyright (c) 2006-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;

import de.walware.ecommons.ui.util.ImageRegistryUtil;
import de.walware.ecommons.ui.util.WindowContributionsProvider;

import de.walware.statet.base.ui.StatetUIServices;

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
			public void toolSessionActivated(final ToolSessionUIData sessionData) {
				if (sessionData.getProcess() != null) {
					fToolRegistry.removeListener(this);
					
					if (fContributionProvider == null) {
						fContributionProvider = new NicoWindowContributions();
					}
				}
			}
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
		}
		finally {
			gPlugin = null;
			super.stop(context);
		}
	}
	
	
	@Override
	protected ImageRegistry createImageRegistry() {
		return StatetUIServices.getSharedImageRegistry();
	}
	
	@Override
	protected void initializeImageRegistry(final ImageRegistry reg) {
		final ImageRegistryUtil util = new ImageRegistryUtil(this);
		util.register(NicoUI.IMG_LOCTOOL_CANCEL, ImageRegistryUtil.T_LOCTOOL, "cancel.png"); //$NON-NLS-1$
		util.register(NicoUI.IMG_LOCTOOLD_CANCEL, ImageRegistryUtil.T_LOCTOOL_D, "cancel.png"); //$NON-NLS-1$
	}
	
	public ToolRegistry getToolRegistry() {
		return fToolRegistry;
	}
	
}
