/*=============================================================================#
 # Copyright (c) 2011-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.net.resourcemapping;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.util.tracker.ServiceTracker;

import de.walware.statet.base.internal.core.BaseCorePlugin;


public class ResourceMappingUtils {
	
	
	private static class ManagerBundleListener implements BundleListener {
		
		private final long fBundleId;
		
		public ManagerBundleListener(final Bundle bundle) {
			fBundleId = bundle.getBundleId();
		}
		
		@Override
		public void bundleChanged(final BundleEvent event) {
			if (event.getBundle().getBundleId() == fBundleId
					&& event.getType() == BundleEvent.STOPPED) {
				final BundleContext context = event.getBundle().getBundleContext();
				if (context != null) {
					context.removeBundleListener(this);
				}
				synchronized (fManagerLock) {
					if (fManagerTracker != null) {
						fManagerTracker.close();
						fManagerTracker = null;
					}
				}
			}
		}
	};
	
	private static final Object fManagerLock = new Object();
	
	private static ServiceTracker fManagerTracker;
	
	
	/**
	 * Returns the resource mapping manager service, if available
	 * 
	 * @return the manager if available, otherwise <code>null</code>
	 */
	public static IResourceMappingManager getManager() {
		synchronized (fManagerLock) {
			if (fManagerTracker == null) {
				final Bundle bundle = BaseCorePlugin.getDefault().getBundle();
				if (bundle.getState() != Bundle.ACTIVE) {
					return null;
				}
				final BundleContext context = bundle.getBundleContext();
				context.addBundleListener(new ManagerBundleListener(bundle));
				fManagerTracker = new ServiceTracker(context,
						IResourceMappingManager.class.getName(), null );
				fManagerTracker.open();
			}
			return (IResourceMappingManager) fManagerTracker.getService();
		}
	}
	
	
	private ResourceMappingUtils() {
	}
	
}
