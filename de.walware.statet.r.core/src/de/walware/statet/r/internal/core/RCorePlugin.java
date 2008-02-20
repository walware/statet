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

package de.walware.statet.r.internal.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.PreferencesUtil;

import de.walware.statet.base.core.StatetCore;
import de.walware.statet.base.core.preferences.PreferencesManageListener;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnvManager;
import de.walware.statet.r.internal.core.rmodel.RModelManager;


/**
 * The main plug-in class to be used in the desktop.
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
	
	public static final void log(final IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public static final void logError(final int code, final String message, final Throwable e) {
		getDefault().getLog().log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, code, message, e));
	}
	
	
	private class CoreAccess implements IRCoreAccess {
		
		private IPreferenceAccess fPrefs;
		private RCodeStyleSettings fRCodeStyle;
		private PreferencesManageListener fListener;
		
		private CoreAccess(final IPreferenceAccess prefs) {
			fPrefs = prefs;
			fRCodeStyle = new RCodeStyleSettings();
			fRCodeStyle.load(prefs);
			fRCodeStyle.resetDirty();
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
	private REnvManager fREnvManager;
	private RModelManager fRModelManager;
	
	
	/**
	 * The constructor.
	 */
	public RCorePlugin() {
		gPlugin = this;
	}
	
	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		
		fREnvManager = new REnvManager(StatetCore.getSettingsChangeNotifier());
		fWorkspaceCoreAccess = new CoreAccess(PreferencesUtil.getInstancePrefs());
		fRModelManager = new RModelManager();
	}
	
	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		super.stop(context);
		gPlugin = null;
		
		if (fWorkspaceCoreAccess != null) {
			fWorkspaceCoreAccess.dispose();
			fWorkspaceCoreAccess = null;
		}
		if (fDefaultsCoreAccess != null) {
			fDefaultsCoreAccess.dispose();
			fDefaultsCoreAccess = null;
		}
		if (fREnvManager != null) {
			fREnvManager.dispose();
			fREnvManager = null;
		}
	}
	
	public IREnvManager getREnvManager() {
		return fREnvManager;
	}
	
	public RModelManager getRModelManager() {
		return fRModelManager;
	}
	
	public IRCoreAccess getWorkspaceRCoreAccess() {
		return fWorkspaceCoreAccess;
	}
	
	public synchronized IRCoreAccess getDefaultsRCoreAccess() {
		if (fDefaultsCoreAccess == null) {
			fDefaultsCoreAccess = new CoreAccess(PreferencesUtil.getDefaultPrefs());
		}
		return fDefaultsCoreAccess;
	}
	
}
