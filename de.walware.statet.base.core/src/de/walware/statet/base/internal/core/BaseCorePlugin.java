/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.statet.base.core.StatetCore;


/**
 * The activator class controls the plug-in life cycle
 */
public class BaseCorePlugin extends Plugin {


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

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public static void logError(int code, String message, Throwable e) {
		log(new Status(IStatus.ERROR, StatetCore.PLUGIN_ID, code, message, e)); 
	}

	
	private SettingsChangeNotifier fSettingsNotifier;
	
	
	/**
	 * The constructor
	 */
	public BaseCorePlugin() {
		gPlugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		fSettingsNotifier = new SettingsChangeNotifier();
	}

	public void stop(BundleContext context) throws Exception {
		gPlugin = null;
		super.stop(context);
		
		if (fSettingsNotifier != null) {
			fSettingsNotifier.dispose();
			fSettingsNotifier = null;
		}
	}
	
	
	public SettingsChangeNotifier getSettingsChangeNotifier() {
		return fSettingsNotifier;
	}

}
