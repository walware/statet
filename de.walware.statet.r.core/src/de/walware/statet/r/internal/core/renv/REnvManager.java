/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.renv;

import static de.walware.statet.r.core.RCorePreferenceNodes.CAT_R_ENVIRONMENTS_QUALIFIER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ibm.icu.text.Collator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import de.walware.ecommons.preferences.AbstractPreferencesModelObject;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.Preference.IntPref;
import de.walware.ecommons.preferences.Preference.StringPref;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.SettingsChangeNotifier;

import de.walware.rj.rsetups.RSetup;
import de.walware.rj.rsetups.RSetupUtil;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RCorePreferenceNodes;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.renv.IREnvConfiguration.WorkingCopy;
import de.walware.statet.r.core.renv.IREnvManager;
import de.walware.statet.r.internal.core.Messages;
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 * Implementation of IREnvManager
 */
public class REnvManager implements IREnvManager {
	
	private static final StringPref PREF_DEFAULT_CONFIGURATION_NAME = new StringPref(
			RCorePreferenceNodes.CAT_R_ENVIRONMENTS_QUALIFIER, "default_configuration.name"); //$NON-NLS-1$
	
	private static final IntPref PREF_VERSION = new IntPref(
			RCorePreferenceNodes.CAT_R_ENVIRONMENTS_QUALIFIER, "version"); //$NON-NLS-1$
	
	
	private volatile int fState;
	private final ReadWriteLock fLock;
	private final SettingsChangeNotifier fNotifier;
	
	private Set<String> fNames;
	private Map<String, IREnv> fNameMap;
	private Map<String, IREnvConfiguration> fIdMap;
	private final REnvProxy fDefaultEnv = new REnvProxy(IREnv.DEFAULT_WORKBENCH_ENV_ID);
	
	public REnvManager(final SettingsChangeNotifier notifier) {
		fState = 0;
		fNotifier = notifier;
		fLock = new ReentrantReadWriteLock(true);
	}
	
	public Lock getReadLock() {
		return fLock.readLock();
	}
	
	public void dispose() {
		fLock.writeLock().lock();
		try {
			fState = 2;
			if (fNames != null) {
				fNames.clear();
				fNames = null;
			}
			if (fIdMap != null) {
				fIdMap.clear();
				fIdMap = null;
			}
			if (fNameMap != null) {
				fNameMap.clear();
				fNameMap = null;
			}
		}
		finally {
			fLock.writeLock().unlock();
		}
	}
	
	private void checkAndLock(final boolean writeLock) {
		// check, if lazy loading is required
		if (fState < 1) {
			synchronized (this) {
				try {
					if (fState < 1) {
						load();
						fState = 1;
					}
				}
				catch (final BackingStoreException e) {
					fState = 101;
					RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, Messages.REnvManager_error_Accessing_message, e));
				}
			}
		}
		
		// lock and check ready
		if (writeLock) {
			fLock.writeLock().lock();
		}
		else {
			fLock.readLock().lock();
		}
		if (fState > 1) {
			throw new IllegalStateException(Messages.REnvManager_error_Dispose_message);
		}
	}
	
	private boolean update(final IREnvConfiguration[] configs, final String defaultREnvId) {
		final Set<String> newREnvs = new HashSet<String>();
		for (final IREnvConfiguration config : configs) {
			newREnvs.add(config.getReference().getId());
		}
		final Set<String> managedNames = new TreeSet<String>(Collator.getInstance());
		
		// update or add configurations
		boolean changed = false;
		for (IREnvConfiguration config : configs) {
			config = new REnvConfiguration(config);
			final REnvReference rEnv = (REnvReference) config.getReference();
			IREnvConfiguration oldConfig = fIdMap.put(rEnv.getId(), config);
			if (oldConfig == null) {
				final IREnv altREnv = fNameMap.get(config.getName());
				if (altREnv != null && !newREnvs.contains(altREnv.getId())) {
					oldConfig = fIdMap.remove(altREnv.getId());
				}
			}
			if (!changed && (oldConfig == null || !oldConfig.equals(config))) {
				changed = true;
			}
			rEnv.fName = config.getName();
			rEnv.fConfig = config;
			fNameMap.put(rEnv.fName, rEnv);
			managedNames.add(rEnv.fName);
		}
		// remove old configurations
		changed |= fIdMap.keySet().retainAll(newREnvs);
		changed |= fNameMap.keySet().retainAll(managedNames);
		fNames = managedNames;
		
		final IREnv oldDefault = (fDefaultEnv.getConfig() != null) ? fDefaultEnv.getConfig().getReference() : null;
		updateDefault(defaultREnvId);
		final IREnv newDefault = (fDefaultEnv.getConfig() != null) ? fDefaultEnv.getConfig().getReference() : null;
		changed |= !(((oldDefault != null) ? oldDefault.equals(newDefault) : null == newDefault)); 
		
		// dirty?
		return changed;
	}
	
	private void updateDefault(final String defaultConfigId) {
		IREnvConfiguration config = null;
		if (defaultConfigId != null && defaultConfigId.length() > 0) {
			config = fIdMap.get(defaultConfigId);
			if (config == null) {
				// migrate old preference (name -> id)
				final IREnv rEnv = fNameMap.get(defaultConfigId);
				if (rEnv != null) {
					config = rEnv.getConfig();
				}
			}
		}
		String name;
		IREnv rEnv;
		if (config != null) {
			name = config.getName();
			rEnv = config.getReference();
		}
		else if (fNames.size() > 0) {
			name = Messages.REnvManager_status_NoDefault_label;
			rEnv = null;
		}
		else {
			name = Messages.REnvManager_status_NotAny_label;
			rEnv = null;
		}
		fDefaultEnv.fName = name;
		fDefaultEnv.fLink = rEnv;
	}
	
	private void load() throws BackingStoreException {
		fNameMap = new HashMap<String, IREnv>();
		fIdMap = new HashMap<String, IREnvConfiguration>();
		fNames = new HashSet<String>();
		
		loadFromRegistry();
		loadFromWorkspace();
	}
	
	private void loadFromWorkspace() throws BackingStoreException {
		final IPreferenceAccess prefs = PreferencesUtil.getInstancePrefs();
		final List<IREnvConfiguration>configs = new ArrayList<IREnvConfiguration>();
		final IEclipsePreferences[] scopes = prefs.getPreferenceNodes(CAT_R_ENVIRONMENTS_QUALIFIER);
		final Integer version = prefs.getPreferenceValue(PREF_VERSION);
		if (version == null || version.intValue() == 0) {
			int i = 0;
			while (configs.isEmpty() && i < scopes.length) {
				final String[] names = scopes[i].childrenNames();
				for (final String name : names) {
					final Preferences node = scopes[i].node(name);
					String id = node.get("id", null); //$NON-NLS-1$
					if (id != null && id.length() > 0) {
						id = REnvReference.updateId(id);
						final REnvReference rEnv = new REnvReference(id);
						final REnvConfiguration config = new REnvConfiguration(
								IREnvConfiguration.USER_LOCAL_TYPE, rEnv, prefs, name);
						if (config.getName() != null) {
							config.upgradePref();
							rEnv.fName = config.getName();
							rEnv.fConfig = config;
							fNameMap.put(rEnv.fName, rEnv);
							fIdMap.put(id, config);
							fNames.add(rEnv.fName);
						}
					}
				}
				i++;
			}
		}
		else if (version.intValue() == 2) {
			int i = 0;
			while (configs.isEmpty() && i < scopes.length) {
				final String[] names = scopes[i].childrenNames();
				for (String id : names) {
					if (id != null && id.length() > 0) {
						id = REnvReference.updateId(id);
						final REnvReference rEnv = new REnvReference(id);
						final REnvConfiguration config = new REnvConfiguration(
								IREnvConfiguration.USER_LOCAL_TYPE, rEnv, prefs, null);
						if (config.getName() != null) {
							rEnv.fName = config.getName();
							rEnv.fConfig = config;
							fNameMap.put(rEnv.fName, rEnv);
							fIdMap.put(id, config);
							fNames.add(rEnv.fName);
						}
					}
				}
				i++;
			}
		}
		
		// init default config
		updateDefault(prefs.getPreferenceValue(PREF_DEFAULT_CONFIGURATION_NAME));
	}
	
	private void saveToWorkspace() throws BackingStoreException {
		final IScopeContext context = PreferencesUtil.getInstancePrefs().getPreferenceContexts()[0];
		final IEclipsePreferences node = context.getNode(RCorePreferenceNodes.CAT_R_ENVIRONMENTS_QUALIFIER);
		final List<String> oldNames = new ArrayList<String>(Arrays.asList(node.childrenNames()));
		oldNames.removeAll(fIdMap.keySet());
		for (final String name : oldNames) {
			if (node.nodeExists(name)) {
				node.node(name).removeNode();
			}
		}
		final Map<Preference, Object>map = new HashMap<Preference, Object>();
		for (final IREnvConfiguration config : fIdMap.values()) {
			if (config instanceof AbstractPreferencesModelObject) {
				((AbstractPreferencesModelObject) config).deliverToPreferencesMap(map);
			}
		}
		map.put(PREF_DEFAULT_CONFIGURATION_NAME, (fDefaultEnv.getConfig() != null) ?
				fDefaultEnv.getConfig().getReference().getId() : null);
		map.put(PREF_VERSION, 2);
		
		PreferencesUtil.setPrefValues(new InstanceScope(), map);
		node.flush();
	}
	
	private void loadFromRegistry() {
		final List<RSetup> setups = RSetupUtil.loadAvailableSetups(null);
		for (final RSetup setup : setups) {
			final REnvReference rEnv = new REnvReference(IREnvConfiguration.EPLUGIN_LOCAL_TYPE + '-' + setup.getId());
			final REnvConfiguration config = new REnvConfiguration(rEnv, setup);
			
			rEnv.fName = config.getName();
			rEnv.fConfig = config;
			
			fNames.add(config.getName());
			fNameMap.put(config.getName(), rEnv);
			fIdMap.put(rEnv.getId(), config);
		}
	}
	
	
	public String[] set(final IREnvConfiguration[] configs, final String defaultConfigId) throws CoreException {
		checkAndLock(true);
		try {
			final boolean changed = update(configs, defaultConfigId);
			if (!changed) {
				return null;
			}
			saveToWorkspace();
			return new String[] { SETTINGS_GROUP_ID };
		}
		catch (final BackingStoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, Messages.REnvManager_error_Saving_message, e));
		}
		finally {
			fLock.writeLock().unlock();
		}
	}
	
//	public String[] getNames() {
//		checkAndLock(false);
//		try {
//			return fNames.toArray(new String[fNames.size()]);
//		}
//		finally {
//			fLock.readLock().unlock();
//		}
//	}
	
	public IREnvConfiguration[] getConfigurations() {
		checkAndLock(false);
		try {
			final Collection<IREnvConfiguration> values = fIdMap.values();
			return values.toArray(new IREnvConfiguration[values.size()]);
		}
		finally {
			fLock.readLock().unlock();
		}
	}
	
	public String[] getIds() {
		checkAndLock(false);
		try {
			final Collection<String> keys = fIdMap.keySet();
			return keys.toArray(new String[keys.size()]);
		}
		finally {
			fLock.readLock().unlock();
		}
	}
	
	public synchronized IREnv get(String id, final String name) {
		id = resolveId(id);
		checkAndLock(false);
		try {
			if (id != null) {
				if (id.equals(IREnv.DEFAULT_WORKBENCH_ENV_ID)) {
					return fDefaultEnv;
				}
				IREnvConfiguration config = fIdMap.get(id);
				if (config == null) {
					id = REnvReference.updateId(id);
					config = fIdMap.get(id);
				}
				if (config != null) {
					return config.getReference();
				}
			}
			if (name != null) {
				return fNameMap.get(name);
			}
			return null;
		}
		finally {
			fLock.readLock().unlock();
		}
	}
	
	public synchronized IREnvConfiguration getConfig(String id, final String name) {
		id = resolveId(id);
		checkAndLock(false);
		try {
			if (id != null) {
				if (id.equals(IREnv.DEFAULT_WORKBENCH_ENV_ID)) {
					return fDefaultEnv.getConfig();
				}
				IREnvConfiguration config = fIdMap.get(id);
				if (config == null) {
					id = REnvReference.updateId(id);
					config = fIdMap.get(id);
				}
				if (config != null) {
					return config;
				}
			}
			if (name != null) {
				final IREnv rEnv = fNameMap.get(name);
				if (rEnv != null) {
					return getConfig(rEnv.getId(), null);
				}
			}
			return null;
		}
		finally {
			fLock.readLock().unlock();
		}
	}
	
	private String resolveId(final String id) {
		if (id == null) {
			return null;
		}
		IREnv rEnv = null;
		if (id.equals(IREnv.DEFAULT_WORKBENCH_ENV_ID)) {
			rEnv = fDefaultEnv.resolve();
		}
		if (rEnv != null) {
			return rEnv.getId();
		}
		return id;
	}
	
	public IREnv getDefault() {
		checkAndLock(false);
		try {
			return fDefaultEnv;
		}
		finally {
			fLock.readLock().unlock();
		}
	}
	
	public WorkingCopy newConfiguration(final String type) {
		if ("user-remote".equals(type)) {
			return new REnvConfiguration.Editable(IREnvConfiguration.USER_REMOTE_TYPE, newUserRemoteLink());
		}
		return new REnvConfiguration.Editable(IREnvConfiguration.USER_LOCAL_TYPE, newUserLocalLink());
	}
	
	private REnvReference newUserLocalLink() {
		return new REnvReference(IREnv.USER_LOCAL_ENV_ID_PREFIX + Long.toString(
				((long) System.getProperty("user.name").hashCode() << 32) | System.currentTimeMillis(), //$NON-NLS-1$
				36)); 
	}
	
	private REnvReference newUserRemoteLink() {
		return new REnvReference(IREnv.USER_REMOTE_ENV_ID_PREFIX + Long.toString(
				((long) System.getProperty("user.name").hashCode() << 32) | System.currentTimeMillis(), //$NON-NLS-1$
				36)); 
	}
	
	
}
