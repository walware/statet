/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launchconfigs;

import java.io.File;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.services.IDisposable;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.Preference.BooleanPref;
import de.walware.ecommons.preferences.Preference.IntPref;

import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.RUI;


/**
 * Utils for local RMI registry
 */
public class RMIUtil {
	
	public static final String QUALIFIER = RUI.PLUGIN_ID + "/rmi"; //$NON-NLS-1$
	public static final BooleanPref PREF_LOCAL_REGISTRY_AUTOSTART_ENABLED = new BooleanPref(QUALIFIER, "LocalRegistryAutostart.enabled"); //$NON-NLS-1$
	public static final IntPref PREF_LOCAL_REGISTRY_PORT = new IntPref(QUALIFIER, "LocalRegistry.port"); //$NON-NLS-1$
	
	
	private static ManagedRegistries gManagedRegistries;
	
	public static void initializeDefaultValues(final IScopeContext context) {
		PreferencesUtil.setPrefValue(context, PREF_LOCAL_REGISTRY_AUTOSTART_ENABLED, true);
		PreferencesUtil.setPrefValue(context, PREF_LOCAL_REGISTRY_PORT, Registry.REGISTRY_PORT);
	}
	
	private static class Port {
		
		
		private int fPort;
		
		
		Port(final int port) {
			fPort = port;
		}
		
		
		public int getPort() {
			return fPort;
		}
		
		@Override
		public int hashCode() {
			return fPort << 8;
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (!(obj instanceof Port)) {
				return false;
			}
			return (fPort == ((Port) obj).fPort);
		}
		
	}
	
	private static class ManagedRegistries implements IDisposable {
		
		
		private Map<Port, Process> fStartedRegistries = new HashMap<Port, Process>();
		
		
		public ManagedRegistries() {
			RUIPlugin.getDefault().registerPluginDisposable(this);
		}
		
		
		public IStatus start(final int port) {
			try {
				final Registry registry = LocateRegistry.getRegistry(port);
				registry.list();
				return new Status(IStatus.INFO, RUI.PLUGIN_ID, NLS.bind(RLaunchingMessages.RMI_status_RegistryAlreadyStarted_message, port));
			} catch (final AccessException e) {
			} catch (final RemoteException e) {
			}
			final Process process;
			try {
				final String registryExe = System.getProperty("java.home") + File.separator + "bin" + File.separator + "rmiregistry"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				process = Runtime.getRuntime().exec(registryExe);
			} catch (final Exception e) {
				return new Status(IStatus.ERROR, RUI.PLUGIN_ID, NLS.bind(RLaunchingMessages.RMI_status_RegistryStartFailed_message, port), e);
			}
			for (int i = 0; i < 5; i++) {
				try {
					final int exit = process.exitValue();
					return new Status(IStatus.ERROR, RUI.PLUGIN_ID, NLS.bind(RLaunchingMessages.RMI_status_RegistryStartFailedWithExitValue_message, port, exit));
				}
				catch (final IllegalThreadStateException e) {
				}
				try {
					final Registry registry = LocateRegistry.getRegistry(port);
					registry.list();
					break;
				} catch (final AccessException e) {
				} catch (final RemoteException e) {
				}
				try {
					Thread.sleep(50);
				} catch (final InterruptedException e) {
					Thread.interrupted();
				}
			}
			fStartedRegistries.put(new Port(port), process);
			return Status.OK_STATUS;
		}
		
		public IStatus stop(final int port) {
			final Port key = new Port(port);
			final Process process = fStartedRegistries.get(key);
			if (process == null) {
				return new Status(IStatus.ERROR, RUI.PLUGIN_ID, NLS.bind(RLaunchingMessages.RMI_status_RegistryStopFailedNotFound_message, port));
			}
			process.destroy();
			fStartedRegistries.remove(key);
			return Status.OK_STATUS;
		}
		
		public void dispose() {
			for (final Map.Entry<Port, Process> reg : fStartedRegistries.entrySet()) {
				try {
					final Registry registry = LocateRegistry.getRegistry(reg.getKey().getPort());
					if (registry.list().length == 0) {
						reg.getValue().destroy();
					}
				} catch (final RemoteException e) {
				}
			}
		}
		
	}
	
	
	private RMIUtil() {
	}
	
	
	public static IStatus startRegistry(int port) {
		port = checkPort(port);
		synchronized (RMIUtil.class) {
			if (gManagedRegistries == null) {
				gManagedRegistries = new ManagedRegistries();
			}
			return gManagedRegistries.start(port);
		}
	}
	
	public static IStatus stopRegistry(int port) {
		port = checkPort(port);
		synchronized (RMIUtil.class) {
			if (gManagedRegistries == null) {
				gManagedRegistries = new ManagedRegistries();
			}
			return gManagedRegistries.stop(port);
		}
	}
	
	private static int checkPort(int port) {
		if (port < 0) {
			port = PreferencesUtil.getInstancePrefs().getPreferenceValue(PREF_LOCAL_REGISTRY_PORT);
		}
		return port;
	}
	
}
