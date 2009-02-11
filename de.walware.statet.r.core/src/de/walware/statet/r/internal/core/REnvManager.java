/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core;

import static de.walware.statet.r.core.RCorePreferenceNodes.CAT_R_ENVIRONMENTS_QUALIFIER;

import java.util.ArrayList;
import java.util.Arrays;
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

import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.SettingsChangeNotifier;
import de.walware.ecommons.preferences.Preference.StringPref;
import de.walware.ecommons.preferences.SettingsChangeNotifier.ManageListener;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RCorePreferenceNodes;
import de.walware.statet.r.core.renv.IREnvManager;
import de.walware.statet.r.core.renv.REnvConfiguration;


/**
 * Implementation of IREnvManager
 */
public class REnvManager implements IREnvManager, ManageListener {
	
	private static final StringPref PREF_DEFAULT_CONFIGURATION_NAME = new StringPref(
			RCorePreferenceNodes.CAT_R_ENVIRONMENTS_QUALIFIER, "default_configuration.name"); //$NON-NLS-1$
	
	private class ManagedConfig extends REnvConfiguration {
		
		ManagedConfig(final String id) {
			super();
			setId(id);
		}
		
		/**
		 * Create configuration instance of specified configuration.
		 * @param name
		 * @param prefs
		 */
		ManagedConfig(final String name, final IPreferenceAccess prefs) {
			super(null);
			setName(name);
			load(prefs, true);
			resetDirty();
		}
		
		@Override
		public Lock getReadLock() {
			return fLock.readLock();
		}
		
		@Override
		public Lock getWriteLock() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void loadDefaults() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void load(final IPreferenceAccess prefs) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void load(final REnvConfiguration from) {
			throw new UnsupportedOperationException();
		}
		
		
		void updateReassign(final REnvConfiguration config) {
			super.load(config, true);
		}
		
		void updateFrom(final REnvConfiguration config) {
			super.load(config, false);
		}
		
		void updateDispose(final boolean dispose) {
			if (fDefaultConfig == ManagedConfig.this) {
				if (dispose) {
					setName((fNames.size() > 0) ?
						Messages.REnvManager_status_NoDefault_label :
						Messages.REnvManager_status_NotAny_label);
					setRHome(null);
				}
			}
			super.setDispose(dispose);
		}
	}
	
	
	private volatile int fState;
	private ReadWriteLock fLock;
	private SettingsChangeNotifier fNotifier;
	
	private Set<String> fNames;
	private Map<String, ManagedConfig> fNameMap;
	private Map<String, ManagedConfig> fIdMap;
	private ManagedConfig fDefaultConfig;
	
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
						fNotifier.addManageListener(this);
						loadFromWorkspace();
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
	
	private boolean update(final REnvConfiguration[] configs, final String defaultConfigName) {
		final Set<String> newIds = new HashSet<String>();
		for (final REnvConfiguration config : configs) {
			newIds.add(config.getId());
		}
		final Set<String> managedNames = new HashSet<String>();
		
		// update or add configurations
		for (final REnvConfiguration config : configs) {
			ManagedConfig managedConfig = fIdMap.get(config.getId());
			if (managedConfig == null) {
				final ManagedConfig potentialConfig = fNameMap.get(config.getName());
				if (potentialConfig != null && !newIds.contains(potentialConfig.getId())) {
					managedConfig = potentialConfig;
				}
			}
			if (managedConfig != null) {
				final String oldName = managedConfig.getName();
				final String oldId = managedConfig.getId();
				managedConfig.updateReassign(config);
				if (!oldId.equals(managedConfig.getId())) {
					fIdMap.put(managedConfig.getId(), managedConfig);
					fIdMap.remove(managedConfig.getId());
				}
				if (!oldName.equals(managedConfig.getName())) {
					fNameMap.put(managedConfig.getName(), managedConfig);
					// we keep old names in map
					fNames.add(managedConfig.getName());
					fNames.remove(oldName);
				}
			}
			else {
				managedConfig = new ManagedConfig(config.getId());
				managedConfig.updateFrom(config);
				add(managedConfig);
			}
			managedNames.add(managedConfig.getName());
		}
		// remove old configurations
		final Set<String> diffIds = new HashSet<String>(fIdMap.keySet());
		diffIds.removeAll(newIds);
		for (final String id : diffIds) {
			final ManagedConfig oldConfig = fIdMap.remove(id);
			oldConfig.updateDispose(true);
			if (!managedNames.contains(oldConfig.getName())) {
				fNameMap.remove(oldConfig.getName());
				fNames.remove(oldConfig.getName());
			}
		}
		
		if (fNames.isEmpty() || defaultConfigName == null) {
			fDefaultConfig.updateDispose(true);
		}
		else {
			fDefaultConfig.updateFrom(fNameMap.get(defaultConfigName));
			fDefaultConfig.updateDispose(false);
		}
		
		// dirty?
		if (!diffIds.isEmpty() || fDefaultConfig.isDirty()) {
			return true;
		}
		for (final ManagedConfig config : fIdMap.values()) {
			if (config.isDirty()) {
				return true;
			}
		}
		return false;
		
	}
	
	private void add(final ManagedConfig config) {
		fNameMap.put(config.getName(), config);
		fIdMap.put(config.getId(), config);
		fNames.add(config.getName());
	}
	
	public void beforeSettingsChangeNotification(final Set<String> groupIds) {
	}
	
	public void afterSettingsChangeNotification(final Set<String> groupIds) {
		for (final ManagedConfig config : fIdMap.values()) {
			config.resetDirty();
		}
		fDefaultConfig.resetDirty();
	}
	
	private void loadFromWorkspace() throws BackingStoreException {
		// init config list
		fNameMap = new HashMap<String, ManagedConfig>();
		fIdMap = new HashMap<String, ManagedConfig>();
		fNames = new TreeSet<String>(Collator.getInstance());
		final IPreferenceAccess prefs = PreferencesUtil.getInstancePrefs();
		final List<ManagedConfig>configs = new ArrayList<ManagedConfig>();
		final IEclipsePreferences[] scopes = prefs.getPreferenceNodes(CAT_R_ENVIRONMENTS_QUALIFIER);
		int i = 0;
		while (configs.isEmpty() && i < scopes.length) {
			final String[] names = scopes[i].childrenNames();
			for (final String name : names) {
				add(new ManagedConfig(name, prefs));
			}
			i++;
		}
		
		// init default config
		final String defaultConfigName = prefs.getPreferenceValue(PREF_DEFAULT_CONFIGURATION_NAME);
		ManagedConfig defaultConfigOrg = null;
		if (defaultConfigName != null || defaultConfigName.length() != 0) {
			defaultConfigOrg = fNameMap.get(defaultConfigName);
		}
		fDefaultConfig = new ManagedConfig("default/workbench"); //$NON-NLS-1$
		if (defaultConfigOrg != null) {
			fDefaultConfig.updateFrom(defaultConfigOrg);
		}
		else {
			fDefaultConfig.updateDispose(true);
		}
	}
	
	private void saveToWorkspace() throws BackingStoreException {
		final IScopeContext context = PreferencesUtil.getInstancePrefs().getPreferenceContexts()[0];
		final IEclipsePreferences node = context.getNode(RCorePreferenceNodes.CAT_R_ENVIRONMENTS_QUALIFIER);
		final List<String> oldNames = new ArrayList<String>(Arrays.asList(node.childrenNames()));
		oldNames.removeAll(fNames);
		for (final String name : oldNames) {
			if (node.nodeExists(name)) {
				node.node(name).removeNode();
			}
		}
		final Map<Preference, Object>map = new HashMap<Preference, Object>();
		for (final REnvConfiguration config : fIdMap.values()) {
			config.deliverToPreferencesMap(map);
		}
		map.put(PREF_DEFAULT_CONFIGURATION_NAME, (!fDefaultConfig.isDisposed()) ? fDefaultConfig.getName() : null);
		
		PreferencesUtil.setPrefValues(new InstanceScope(), map);
		node.flush();
	}
	
	
	public String[] set(final REnvConfiguration[] configs, final String defaultConfigName) throws CoreException {
		try {
			checkAndLock(true);
			final boolean changed = update(configs, defaultConfigName);
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
	
	public String[] getNames() {
		try {
			checkAndLock(false);
			return fNames.toArray(new String[fNames.size()]);
		}
		finally {
			fLock.readLock().unlock();
		}
	}
	
	public synchronized REnvConfiguration get(final String id, final String name) {
		try {
			checkAndLock(false);
			REnvConfiguration config = null;
			if (id != null) {
				config = fIdMap.get(id);
			}
			if (config == null && name != null) {
				config = fNameMap.get(name);
			}
			return config;
		}
		finally {
			fLock.readLock().unlock();
		}
	}
	
	public synchronized REnvConfiguration getDefault() {
		try {
			checkAndLock(false);
			return fDefaultConfig;
		}
		finally {
			fLock.readLock().unlock();
		}
	}
	
}
