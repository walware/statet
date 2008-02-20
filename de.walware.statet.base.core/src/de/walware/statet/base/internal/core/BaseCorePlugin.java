/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.internal.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

import de.walware.eclipsecommons.preferences.SettingsChangeNotifier;

import de.walware.statet.base.core.IExtContentTypeManager;
import de.walware.statet.base.core.StatetCore;


/**
 * The activator class controls the plug-in life cycle
 */
public final class BaseCorePlugin extends Plugin {
	
	
	/** The shared instance. */
	private static BaseCorePlugin gPlugin;
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static BaseCorePlugin getDefault() {
		return gPlugin;
	}
	
	public static void log(final IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public static void logError(final int code, final String message, final Throwable e) {
		log(new Status(IStatus.ERROR, StatetCore.PLUGIN_ID, code, message, e));
	}
	
	
	private SettingsChangeNotifier fSettingsNotifier;
	private ExtContentTypeServices fContentTypeServices;
	
	
	/**
	 * The constructor
	 */
	public BaseCorePlugin() {
		gPlugin = this;
	}
	
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		fSettingsNotifier = new SettingsChangeNotifier();
		fContentTypeServices = new ExtContentTypeServices();
	}
	
	@Override
	public void stop(final BundleContext context) throws Exception {
		gPlugin = null;
		super.stop(context);
		
		if (fSettingsNotifier != null) {
			fSettingsNotifier.dispose();
			fSettingsNotifier = null;
		}
		if (fContentTypeServices != null) {
			fContentTypeServices.dispose();
			fContentTypeServices = null;
		}
	}
	
	
	public SettingsChangeNotifier getSettingsChangeNotifier() {
		return fSettingsNotifier;
	}
	
	public IExtContentTypeManager getContentTypeServices() {
		return fContentTypeServices;
	}
	
}
