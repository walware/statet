/*******************************************************************************
 * Copyright (c) 2005-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.IDisposable;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.PreferencesManageListener;
import de.walware.ecommons.preferences.PreferencesUtil;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.core.renv.REnvManager;
import de.walware.statet.r.internal.core.rhelp.RHelpManager;
import de.walware.statet.r.internal.core.sourcemodel.RModelManager;


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
		final Plugin plugin = getDefault();
		if (plugin != null) {
			plugin.getLog().log(status);
		}
	}
	
	public static final void logError(final int code, final String message, final Throwable e) {
		final Plugin plugin = getDefault();
		if (plugin != null) {
			plugin.getLog().log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, code, message, e));
		}
	}
	
	
	private static class CoreAccess implements IRCoreAccess {
		
		private final IPreferenceAccess fPrefs;
		private final RCodeStyleSettings fRCodeStyle;
		private final PreferencesManageListener fListener;
		
		private CoreAccess(final IPreferenceAccess prefs) {
			fPrefs = prefs;
			
			fRCodeStyle = new RCodeStyleSettings(1);
			fRCodeStyle.load(prefs);
			fRCodeStyle.resetDirty();
			
			fListener = new PreferencesManageListener(fRCodeStyle, fPrefs, RCodeStyleSettings.ALL_GROUP_IDS);
		}
		
		@Override
		public IPreferenceAccess getPrefs() {
			return fPrefs;
		}
		
		@Override
		public RCodeStyleSettings getRCodeStyle() {
			return fRCodeStyle;
		};
		
		private void dispose() {
			fListener.dispose();
		}
	};
	
	
	private boolean fStarted;
	
	private final List<IDisposable> fDisposables = new ArrayList<IDisposable>();
	
	private CoreAccess fWorkspaceCoreAccess;
	private CoreAccess fDefaultsCoreAccess;
	private REnvManager fREnvManager;
	private RModelManager fRModelManager;
	private RHelpManager fRHelpManager;
	
	private ServiceTracker fProxyService;
	
	
	/**
	 * The constructor.
	 */
	public RCorePlugin() {
	}
	
	
	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		gPlugin = this;
		
		fREnvManager = new REnvManager(PreferencesUtil.getSettingsChangeNotifier());
		fWorkspaceCoreAccess = new CoreAccess(PreferencesUtil.getInstancePrefs());
		fRModelManager = new RModelManager();
		fRHelpManager = new RHelpManager();
		fDisposables.add(fRHelpManager);
		
		fStarted = true;
	}
	
	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		try {
			synchronized (this) {
				fStarted = false;
			}
			if (fRModelManager != null) {
				fRModelManager.dispose();
				fRModelManager = null;
			}
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
			
			for (final IDisposable listener : fDisposables) {
				try {
					listener.dispose();
				}
				catch (final Throwable e) {
					getLog().log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, ICommonStatusConstants.INTERNAL_PLUGGED_IN, "Error occured when dispose module", e)); 
				}
			}
			fDisposables.clear();
		}
		finally {
			gPlugin = null;
			super.stop(context);
		}
	}
	
	public REnvManager getREnvManager() {
		return fREnvManager;
	}
	
	public RModelManager getRModelManager() {
		return fRModelManager;
	}
	
	public RHelpManager getRHelpManager() {
		return fRHelpManager;
	}
	
	public IRCoreAccess getWorkspaceRCoreAccess() {
		return fWorkspaceCoreAccess;
	}
	
	public synchronized IRCoreAccess getDefaultsRCoreAccess() {
		if (fDefaultsCoreAccess == null) {
			if (!fStarted) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			fDefaultsCoreAccess = new CoreAccess(PreferencesUtil.getDefaultPrefs());
		}
		return fDefaultsCoreAccess;
	}
	
	public synchronized IProxyService getProxyService() {
		if (fProxyService == null) {
			if (!fStarted) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			fProxyService = new ServiceTracker(getBundle().getBundleContext(),
					IProxyService.class.getName(), null );
			fProxyService.open();
		}
		return (IProxyService) fProxyService.getService();
	}
	
}
