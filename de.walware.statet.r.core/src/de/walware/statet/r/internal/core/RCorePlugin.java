/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.PreferencesUtil;

import de.walware.statet.base.core.preferences.PreferencesManageListener;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;


/**
 * The main plugin class to be used in the desktop.
 */
public class RCorePlugin extends Plugin {

	
	//The shared instance.
	private static RCorePlugin gPlugin;
	
	/**
	 * Returns the shared instance.
	 */
	public static RCorePlugin getDefault() {
		return gPlugin;
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	
	private class CoreAccess implements IRCoreAccess {
		
		private IPreferenceAccess fPrefs;
		private RCodeStyleSettings fRCodeStyle;
		private PreferencesManageListener fListener;
		
		private CoreAccess(IPreferenceAccess prefs) {
			fPrefs = prefs;
			fRCodeStyle = new RCodeStyleSettings();
			fListener = new PreferencesManageListener(fRCodeStyle, fPrefs, RCodeStyleSettings.CONTEXT_ID);
		}
		
		public IPreferenceAccess getPrefs() {
			return fPrefs;
		}
		
		public RCodeStyleSettings getRCodeStyle() {
			return fRCodeStyle;
		};
		
		private void dispose() {
			fListener.dispose();
		}
	};

	
	private CoreAccess fWorkspaceCoreAccess;
	private CoreAccess fDefaultsCoreAccess;
	
	
	/**
	 * The constructor.
	 */
	public RCorePlugin() {
		gPlugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		gPlugin = null;

		if (fWorkspaceCoreAccess != null) {
			fWorkspaceCoreAccess.dispose();
			fWorkspaceCoreAccess = null;
		}
		if (fDefaultsCoreAccess!= null) {
			fDefaultsCoreAccess.dispose();
			fDefaultsCoreAccess = null;
		}
	}

	public IRCoreAccess getWorkspaceRCoreAccess() {
		if (fWorkspaceCoreAccess != null) {
			return fWorkspaceCoreAccess;
		}
		synchronized (this) {
			if (fWorkspaceCoreAccess == null) {
				fWorkspaceCoreAccess = new CoreAccess(PreferencesUtil.getInstancePrefs());
			}
			return fWorkspaceCoreAccess;
		}
	}

	public IRCoreAccess getDefaultsRCoreAccess() {
		if (fDefaultsCoreAccess != null) {
			return fDefaultsCoreAccess;
		}
		synchronized (this) {
			if (fDefaultsCoreAccess == null) {
				fDefaultsCoreAccess = new CoreAccess(PreferencesUtil.getDefaultPrefs());
			}
			return fDefaultsCoreAccess;
		}
	}
}
