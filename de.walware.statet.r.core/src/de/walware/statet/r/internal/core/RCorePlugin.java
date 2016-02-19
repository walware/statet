/*=============================================================================#
 # Copyright (c) 2005-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

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
import de.walware.ecommons.preferences.core.util.PreferenceUtils;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.core.pkgmanager.REnvPkgManager;
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
		final Plugin plugin= getDefault();
		if (plugin != null) {
			plugin.getLog().log(status);
		}
	}
	
	public static final void logError(final int code, final String message, final Throwable e) {
		final Plugin plugin= getDefault();
		if (plugin != null) {
			plugin.getLog().log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, code, message, e));
		}
	}
	
	
	private boolean started;
	
	private final List<IDisposable> disposables= new ArrayList<>();
	
	private REnvManager rEnvManager;
	
	private RCoreAccess workspaceCoreAccess;
	private RCoreAccess defaultsCoreAccess;
	
	private RModelManager rModelManager;
	private ResourceTracker resourceTracker;
	
	private REnvPkgManager rEnvPkgManager;
	private RHelpManager rHelpManager;
	
	private ServiceTracker proxyService;
	
	
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
		gPlugin= this;
		
		this.rEnvManager= new REnvManager();
		
		this.workspaceCoreAccess= new RCoreAccess(
				PreferenceUtils.getInstancePrefs(),
				this.rEnvManager.getDefault() );
		
		this.rModelManager= new RModelManager();
		this.resourceTracker= new ResourceTracker(this.rModelManager);
		
		this.rEnvPkgManager= new REnvPkgManager(this.rEnvManager);
		this.rHelpManager= new RHelpManager();
		this.disposables.add(this.rHelpManager);
		
		this.started= true;
	}
	
	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		try {
			synchronized (this) {
				this.started= false;
			}
			if (this.resourceTracker != null) {
				try {
					this.resourceTracker.dispose();
				}
				catch (final Exception e) {}
				this.resourceTracker= null;
			}
			
			if (this.rModelManager != null) {
				this.rModelManager.dispose();
				this.rModelManager= null;
			}
			if (this.workspaceCoreAccess != null) {
				this.workspaceCoreAccess.dispose();
				this.workspaceCoreAccess= null;
			}
			if (this.defaultsCoreAccess != null) {
				this.defaultsCoreAccess.dispose();
				this.defaultsCoreAccess= null;
			}
			if (this.rEnvManager != null) {
				this.rEnvManager.dispose();
				this.rEnvManager= null;
			}
			
			for (final IDisposable listener : this.disposables) {
				try {
					listener.dispose();
				}
				catch (final Throwable e) {
					getLog().log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, ICommonStatusConstants.INTERNAL_PLUGGED_IN, "Error occured when dispose module", e)); 
				}
			}
			this.disposables.clear();
		}
		finally {
			gPlugin= null;
			super.stop(context);
		}
	}
	
	private void checkStarted() {
		if (!this.started) {
			throw new IllegalStateException("Plug-in is not started.");
		}
	}
	
	
	public REnvManager getREnvManager() {
		return this.rEnvManager;
	}
	
	public RModelManager getRModelManager() {
		return this.rModelManager;
	}
	
	public ResourceTracker getResourceTracker() {
		return this.resourceTracker;
	}
	
	public REnvPkgManager getREnvPkgManager() {
		return this.rEnvPkgManager;
	}
	
	public RHelpManager getRHelpManager() {
		return this.rHelpManager;
	}
	
	public synchronized IRCoreAccess getWorkspaceRCoreAccess() {
		if (this.workspaceCoreAccess == null) {
			checkStarted();
		}
		return this.workspaceCoreAccess;
	}
	
	public synchronized IRCoreAccess getDefaultsRCoreAccess() {
		if (this.defaultsCoreAccess == null) {
			checkStarted();
			this.defaultsCoreAccess= new RCoreAccess(
					PreferenceUtils.getDefaultPrefs(),
					this.rEnvManager.getDefault() );
		}
		return this.defaultsCoreAccess;
	}
	
	public synchronized IProxyService getProxyService() {
		if (this.proxyService == null) {
			checkStarted();
			this.proxyService= new ServiceTracker(getBundle().getBundleContext(),
					IProxyService.class.getName(), null );
			this.proxyService.open();
		}
		return (IProxyService) this.proxyService.getService();
	}
	
}
