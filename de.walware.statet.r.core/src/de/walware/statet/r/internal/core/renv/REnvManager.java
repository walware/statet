/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.renv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.preferences.AbstractPreferencesModelObject;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.core.IPreferenceAccess;
import de.walware.ecommons.preferences.core.IPreferenceSetService;
import de.walware.ecommons.preferences.core.Preference;
import de.walware.ecommons.preferences.core.Preference.IntPref;
import de.walware.ecommons.preferences.core.Preference.StringPref;
import de.walware.ecommons.preferences.core.util.PreferenceUtils;

import de.walware.rj.rsetups.RSetup;
import de.walware.rj.rsetups.RSetupUtil;

import de.walware.statet.r.core.RCore;
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
	
	private static final StringPref PREF_DEFAULT_CONFIGURATION_NAME= new StringPref(
			IREnvManager.PREF_QUALIFIER, "default_configuration.name"); //$NON-NLS-1$
	
	private static final IntPref PREF_VERSION= new IntPref(
			IREnvManager.PREF_QUALIFIER, "version"); //$NON-NLS-1$
	
	private static final IntPref STAMP_PREF= new IntPref(
			IREnvManager.PREF_QUALIFIER, "stamp"); //$NON-NLS-1$
	
	
	private volatile int state;
	private final ReadWriteLock lock;
	
	private Set<String> names;
	private Map<String, IREnv> nameMap;
	private Map<String, REnvConfiguration> idMap;
	private final REnvProxy defaultEnv= new REnvProxy(IREnv.DEFAULT_WORKBENCH_ENV_ID);
	
	
	public REnvManager() {
		this.state= 0;
		this.lock= new ReentrantReadWriteLock(true);
	}
	
	@Override
	public Lock getReadLock() {
		return this.lock.readLock();
	}
	
	public void dispose() {
		this.lock.writeLock().lock();
		try {
			this.state= 2;
			if (this.names != null) {
				this.names.clear();
				this.names= null;
			}
			if (this.idMap != null) {
				this.idMap.clear();
				this.idMap= null;
			}
			if (this.nameMap != null) {
				this.nameMap.clear();
				this.nameMap= null;
			}
		}
		finally {
			this.lock.writeLock().unlock();
		}
	}
	
	private void checkAndLock(final boolean writeLock) {
		// check, if lazy loading is required
		if (this.state < 1) {
			synchronized (this) {
				try {
					if (this.state < 1) {
						load();
						this.state= 1;
					}
				}
				catch (final BackingStoreException e) {
					this.state= 101;
					RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, Messages.REnvManager_error_Accessing_message, e));
				}
			}
		}
		
		// lock and check ready
		if (writeLock) {
			this.lock.writeLock().lock();
		}
		else {
			this.lock.readLock().lock();
		}
		if (this.state > 1) {
			throw new IllegalStateException(Messages.REnvManager_error_Dispose_message);
		}
	}
	
	private boolean update(final ImList<IREnvConfiguration> configs, final String defaultREnvId) {
		final Set<String> newREnvs= new HashSet<>();
		for (final IREnvConfiguration config : configs) {
			newREnvs.add(config.getReference().getId());
		}
		final Set<String> managedNames= new TreeSet<>(Collator.getInstance());
		final Set<String> oldIds= new HashSet<>(this.idMap.keySet());
		
		// update or add configurations
		boolean changed= false;
		for (final IREnvConfiguration config : configs) {
			final REnvConfiguration newConfig= new REnvConfiguration(config);
			final REnvReference rEnv= (REnvReference) newConfig.getReference();
			IREnvConfiguration oldConfig= this.idMap.put(rEnv.getId(), newConfig);
			if (oldConfig == null) {
				final IREnv altREnv= this.nameMap.get(newConfig.getName());
				if (altREnv != null && !newREnvs.contains(altREnv.getId())) {
					oldConfig= this.idMap.remove(altREnv.getId());
				}
			}
			if (!changed && (oldConfig == null || !oldConfig.equals(newConfig))) {
				changed= true;
			}
			rEnv.fName= newConfig.getName();
			rEnv.fConfig= newConfig;
			this.nameMap.put(rEnv.fName, rEnv);
			managedNames.add(rEnv.fName);
			oldIds.remove(rEnv.getId());
		}
		// remove old configurations
		for (final String id : oldIds) {
			final REnvConfiguration rConfig= this.idMap.remove(id);
			if (rConfig != null) {
				changed |= true;
				rConfig.markDeleted();
			}
		}
//		changed |= fIdMap.keySet().retainAll(newREnvs);
		changed |= this.nameMap.keySet().retainAll(managedNames);
		this.names= managedNames;
		
		final IREnv oldDefault= (this.defaultEnv.getConfig() != null) ? this.defaultEnv.getConfig().getReference() : null;
		updateDefault(defaultREnvId);
		final IREnv newDefault= (this.defaultEnv.getConfig() != null) ? this.defaultEnv.getConfig().getReference() : null;
		changed |= !Objects.equals(oldDefault, newDefault); 
		
		// dirty?
		return changed;
	}
	
	private void updateDefault(final String defaultConfigId) {
		IREnvConfiguration config= null;
		if (defaultConfigId != null && defaultConfigId.length() > 0) {
			config= this.idMap.get(defaultConfigId);
			if (config == null) {
				// migrate old preference (name -> id)
				final IREnv rEnv= this.nameMap.get(defaultConfigId);
				if (rEnv != null) {
					config= rEnv.getConfig();
				}
			}
		}
		String name;
		IREnv rEnv;
		if (config != null) {
			name= config.getName();
			rEnv= config.getReference();
		}
		else if (this.names.size() > 0) {
			name= Messages.REnvManager_status_NoDefault_label;
			rEnv= null;
		}
		else {
			name= Messages.REnvManager_status_NotAny_label;
			rEnv= null;
		}
		this.defaultEnv.fName= name;
		this.defaultEnv.fLink= rEnv;
	}
	
	private void load() throws BackingStoreException {
		this.nameMap= new HashMap<>();
		this.idMap= new HashMap<>();
		this.names= new HashSet<>();
		
		loadFromRegistry();
		loadFromWorkspace();
	}
	
	private void loadFromWorkspace() throws BackingStoreException {
		final IPreferenceAccess prefs= PreferencesUtil.getInstancePrefs();
		final List<IREnvConfiguration>configs= new ArrayList<>();
		
		final Integer version= prefs.getPreferenceValue(PREF_VERSION);
		if (version == null || version.intValue() == 0) {
			for (final Iterator<IScopeContext> iter= prefs.getPreferenceContexts().iterator();
					configs.isEmpty() && iter.hasNext(); ) {
				final IEclipsePreferences prefNode= iter.next().getNode(IREnvManager.PREF_QUALIFIER);
				if (prefNode == null) {
					continue;
				}
				final String[] names= prefNode.childrenNames();
				for (final String name : names) {
					final Preferences node= prefNode.node(name);
					String id= node.get("id", null); //$NON-NLS-1$
					if (id != null && id.length() > 0 && id.startsWith(IREnv.USER_ENV_ID_PREFIX)) {
						id= REnvReference.updateId(id);
						final REnvReference rEnv= new REnvReference(id);
						final REnvConfiguration config= new REnvConfiguration(
								null, rEnv, prefs, name);
						if (config.getName() != null) {
							config.upgradePref();
							rEnv.fName= config.getName();
							rEnv.fConfig= config;
							this.nameMap.put(rEnv.fName, rEnv);
							this.idMap.put(id, config);
							this.names.add(rEnv.fName);
						}
					}
				}
			}
		}
		else if (version.intValue() == 2) {
			for (final Iterator<IScopeContext> iter= prefs.getPreferenceContexts().iterator();
					configs.isEmpty() && iter.hasNext(); ) {
				final IEclipsePreferences prefNode= iter.next().getNode(IREnvManager.PREF_QUALIFIER);
				if (prefNode == null) {
					continue;
				}
				final String[] names= prefNode.childrenNames();
				for (String id : names) {
					if (id != null && id.length() > 0 && id.startsWith(IREnv.USER_ENV_ID_PREFIX)) {
						id= REnvReference.updateId(id);
						final REnvReference rEnv= new REnvReference(id);
						final REnvConfiguration config= new REnvConfiguration(
								rEnv, prefs);
						if (config.getName() != null) {
							rEnv.fName= config.getName();
							rEnv.fConfig= config;
							this.nameMap.put(rEnv.fName, rEnv);
							this.idMap.put(id, config);
							this.names.add(rEnv.fName);
						}
					}
				}
			}
		}
		
		// init default config
		updateDefault(prefs.getPreferenceValue(PREF_DEFAULT_CONFIGURATION_NAME));
	}
	
	private void saveToWorkspace() throws BackingStoreException {
		final IScopeContext context= PreferencesUtil.getInstancePrefs().getPreferenceContexts().get(0);
		final IEclipsePreferences node= context.getNode(IREnvManager.PREF_QUALIFIER);
		final List<String> oldNames= new ArrayList<>(Arrays.asList(node.childrenNames()));
		oldNames.removeAll(this.idMap.keySet());
		for (final String name : oldNames) {
			if (node.nodeExists(name)) {
				node.node(name).removeNode();
			}
		}
		final Map<Preference<?>, Object> map= new HashMap<>();
		map.put(PREF_VERSION, 2);
		map.put(STAMP_PREF, System.currentTimeMillis());
		
		for (final IREnvConfiguration config : this.idMap.values()) {
			if (config instanceof AbstractPreferencesModelObject) {
				((AbstractPreferencesModelObject) config).deliverToPreferencesMap(map);
			}
		}
		map.put(PREF_DEFAULT_CONFIGURATION_NAME, (this.defaultEnv.getConfig() != null) ?
				this.defaultEnv.getConfig().getReference().getId() : null);
		
		PreferencesUtil.setPrefValues(InstanceScope.INSTANCE, map);
		node.flush();
	}
	
	private void loadFromRegistry() {
		final IPreferenceAccess prefs= PreferencesUtil.getInstancePrefs();
		final List<RSetup> setups= RSetupUtil.loadAvailableSetups(null);
		for (final RSetup setup : setups) {
			final REnvReference rEnv= new REnvReference(IREnvConfiguration.EPLUGIN_LOCAL_TYPE + '-' + setup.getId());
			final REnvConfiguration config= new REnvConfiguration(
					IREnvConfiguration.EPLUGIN_LOCAL_TYPE, rEnv, setup, prefs );
			
			rEnv.fName= config.getName();
			rEnv.fConfig= config;
			
			this.names.add(config.getName());
			this.nameMap.put(config.getName(), rEnv);
			this.idMap.put(rEnv.getId(), config);
		}
	}
	
	
	@Override
	public void set(final ImList<IREnvConfiguration> configs, final String defaultConfigId)
			throws CoreException {
		checkAndLock(true);
		try {
			final boolean changed= update(configs, defaultConfigId);
			if (!changed) {
				return;
			}
			
			final IPreferenceSetService preferenceSetService= PreferenceUtils.getPreferenceSetService();
			final String sourceId= "REnv" + System.identityHashCode(this); //$NON-NLS-1$
			final boolean resume= preferenceSetService.pause(sourceId);
			try {
				saveToWorkspace();
				return;
			}
			finally {
				if (resume) {
					preferenceSetService.resume(sourceId);
				}
			}
		}
		catch (final BackingStoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, Messages.REnvManager_error_Saving_message, e));
		}
		finally {
			this.lock.writeLock().unlock();
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
	
	@Override
	public List<IREnvConfiguration> getConfigurations() {
		final List<IREnvConfiguration> list;
		checkAndLock(false);
		try {
			list= new ArrayList<IREnvConfiguration>(this.idMap.values());
		}
		finally {
			this.lock.readLock().unlock();
		}
		final Iterator<IREnvConfiguration> iter= list.iterator();
		while (iter.hasNext()) {
			if (iter.next().isDeleted()) {
				iter.remove();
			}
		}
		return list;
	}
	
	public String[] getIds() {
		checkAndLock(false);
		try {
			final Collection<String> keys= this.idMap.keySet();
			return keys.toArray(new String[keys.size()]);
		}
		finally {
			this.lock.readLock().unlock();
		}
	}
	
	@Override
	public synchronized IREnv get(String id, final String name) {
		id= resolveId(id);
		checkAndLock(false);
		try {
			if (id != null) {
				if (id.equals(IREnv.DEFAULT_WORKBENCH_ENV_ID)) {
					return this.defaultEnv;
				}
				IREnvConfiguration config= this.idMap.get(id);
				if (config == null) {
					id= REnvReference.updateId(id);
					config= this.idMap.get(id);
				}
				if (config != null) {
					return config.getReference();
				}
			}
			if (name != null) {
				return this.nameMap.get(name);
			}
			return null;
		}
		finally {
			this.lock.readLock().unlock();
		}
	}
	
	public synchronized IREnvConfiguration getConfig(String id, final String name) {
		id= resolveId(id);
		checkAndLock(false);
		try {
			if (id != null) {
				if (id.equals(IREnv.DEFAULT_WORKBENCH_ENV_ID)) {
					return this.defaultEnv.getConfig();
				}
				IREnvConfiguration config= this.idMap.get(id);
				if (config == null) {
					id= REnvReference.updateId(id);
					config= this.idMap.get(id);
				}
				if (config != null) {
					return config;
				}
			}
			if (name != null) {
				final IREnv rEnv= this.nameMap.get(name);
				if (rEnv != null) {
					return getConfig(rEnv.getId(), null);
				}
			}
			return null;
		}
		finally {
			this.lock.readLock().unlock();
		}
	}
	
	private String resolveId(final String id) {
		if (id == null) {
			return null;
		}
		IREnv rEnv= null;
		if (id.equals(IREnv.DEFAULT_WORKBENCH_ENV_ID)) {
			rEnv= this.defaultEnv.resolve();
		}
		if (rEnv != null) {
			return rEnv.getId();
		}
		return id;
	}
	
	@Override
	public IREnv getDefault() {
		checkAndLock(false);
		try {
			return this.defaultEnv;
		}
		finally {
			this.lock.readLock().unlock();
		}
	}
	
	@Override
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
