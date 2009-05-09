/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.preferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.IWorkingCopyManager;
import org.osgi.service.prefs.BackingStoreException;

import de.walware.ecommons.CoreUtility;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.SettingsChangeNotifier;
import de.walware.ecommons.ui.dialogs.IStatusChangeListener;


/**
 * Allows load, save, restore of managed preferences, including:
 * <p><ul>
 * <li>Connected databinding context:<ul>
 *     <li>use {@link #initBindings()} to create dbc</li>
 *     <li>use {@link #createObservable(Object)} to create observables for model</li>
 *     <li>override {@link #addBindings(DataBindingContext, Realm)}) to register bindings</li>
 *   </ul></li>
 *   <li>optional project scope</li>
 *   <li>change settings groups ({@link SettingsChangeNotifier})</li>
 * </ul>
 * Instead of data binding, it is possible to overwrite
 *   {@link #updatePreferences()} and {@link #updateControls()}
 * to map the preferences to UI.
 */
public abstract class ManagedConfigurationBlock extends ConfigurationBlock
		implements IPreferenceAccess, IObservableFactory {
	
	
	protected class PreferenceManager {
		
		private IScopeContext[] fLookupOrder;
		protected final Map<Preference, String> fPreferences;
		
		/** Manager for a working copy of the preferences */
		private final IWorkingCopyManager fManager;
		/** Map saving the project settings, if disabled */
		private Map<Preference, Object> fDisabledProjectSettings;
		
		
		PreferenceManager(final Map<Preference, String> prefs) {
			fManager = getContainer().getWorkingCopyManager();
			fPreferences = prefs;
			
			fPreferenceManager = this;
			
			if (fProject != null) {
				fLookupOrder = new IScopeContext[] {
						new ProjectScope(fProject),
						new InstanceScope(),
						new DefaultScope()
				};
			} else {
				fLookupOrder = new IScopeContext[] {
						new InstanceScope(),
						new DefaultScope()
				};
			}
			
			// testIfOptionsComplete();
			
			// init disabled settings, if required
			if (fProject == null || hasProjectSpecificSettings(fProject)) {
				fDisabledProjectSettings = null;
			} else {
				saveDisabledProjectSettings();
			}
		}
		
		
/* Managing methods ***********************************************************/
		
		/**
		 * Checks, if project specific options exists
		 * 
		 * @param project to look up
		 * @return
		 */
		boolean hasProjectSpecificSettings(final IProject project) {
			final IScopeContext projectContext = new ProjectScope(project);
			for (final Preference key : fPreferences.keySet()) {
				if (getInternalValue(key, projectContext, true) != null)
					return true;
			}
			return false;
		}
		
		void setUseProjectSpecificSettings(final boolean enable) {
			final boolean hasProjectSpecificOption = (fDisabledProjectSettings == null);
			if (enable != hasProjectSpecificOption) {
				if (enable) {
					loadDisabledProjectSettings();
				} else {
					saveDisabledProjectSettings();
				}
			}
		}
		
		private void saveDisabledProjectSettings() {
			fDisabledProjectSettings = new IdentityHashMap<Preference, Object>();
			for (final Preference key : fPreferences.keySet()) {
				fDisabledProjectSettings.put(key, getValue(key));
				setInternalValue(key, null); // clear project settings
			}
			
		}
		
		private void loadDisabledProjectSettings() {
			for (final Preference key : fPreferences.keySet()) {
				// Copy values from saved disabled settings to working store
				setValue(key, fDisabledProjectSettings.get(key));
			}
			fDisabledProjectSettings = null;
		}
		
		boolean processChanges(final boolean saveStore) {
			final List<Preference> changedPrefs = new ArrayList<Preference>();
			final boolean needsBuild = getChanges(changedPrefs);
			if (changedPrefs.isEmpty()) {
				return true;
			}
			
			boolean doBuild = false;
			if (needsBuild) {
				final String[] strings = getFullBuildDialogStrings(fProject == null);
				if (strings != null) {
					final MessageDialog dialog = new MessageDialog(getShell(),
							strings[0], null, strings[1],
							MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL }, 2);
					final int res = dialog.open();
					if (res == 0) {
						doBuild = true;
					}
					else if (res != 1) {
						return false; // cancel pressed
					}
				}
			}
			if (saveStore) {
				try {
					fManager.applyChanges();
				} catch (final BackingStoreException e) {
					logSaveError(e);
					return false;
				}
				if (doBuild) {
					CoreUtility.getBuildJob(fProject).schedule();
				}
			}
			else {
				if (doBuild) {
					getContainer().registerUpdateJob(CoreUtility.getBuildJob(fProject));
				}
			}
			final Set<String> groupIds = new HashSet<String>();
			for (final Preference pref : changedPrefs) {
				final String groupId = fPreferences.get(pref);
				if (groupId != null) {
					groupIds.add(groupId);
				}
			}
			scheduleChangeNotification(groupIds, saveStore);
			return true;
		}
		
		/**
		 * 
		 * @param currContext
		 * @param changedSettings
		 * @return true, if rebuild is required.
		 */
		private boolean getChanges(final List<Preference> changedSettings) {
			final IScopeContext currContext = fLookupOrder[0];
			boolean needsBuild = false;
			for (final Preference key : fPreferences.keySet()) {
				final String oldVal = getInternalValue(key, currContext, false);
				final String val = getInternalValue(key, currContext, true);
				if (val == null) {
					if (oldVal != null) {
						changedSettings.add(key);
						needsBuild |= !oldVal.equals(getInternalValue(key, true));
					}
				}
				else if (!val.equals(oldVal)) {
					changedSettings.add(key);
					needsBuild |= oldVal != null || !val.equals(getInternalValue(key, true));
				}
			}
			return needsBuild;
		}
		
		
		void loadDefaults() {
			final DefaultScope defaultScope = new DefaultScope();
			for (final Preference key : fPreferences.keySet()) {
				final String defValue = getInternalValue(key, defaultScope, false);
				setInternalValue(key, defValue);
			}
			
		}
		
		// DEBUG
		private void testIfOptionsComplete() {
			for (final Preference key : fPreferences.keySet()) {
				if (getInternalValue(key, false) == null) {
					System.out.println("preference option missing: " + key + " (" + this.getClass().getName() +')');  //$NON-NLS-1$//$NON-NLS-2$
				}
			}
		}
		
		private IEclipsePreferences getNode(final IScopeContext context, final String qualifier, final boolean useWorkingCopy) {
			final IEclipsePreferences node = context.getNode(qualifier);
			if (useWorkingCopy) {
				return fManager.getWorkingCopy(node);
			}
			return node;
		}
		
		private <T> String getInternalValue(final Preference<T> key, final IScopeContext context, final boolean useWorkingCopy) {
			return getNode(context, key.getQualifier(), useWorkingCopy).get(key.getKey(), null);
		}
		
		private <T> String getInternalValue(final Preference<T> key, final boolean ignoreTopScope) {
			for (int i = ignoreTopScope ? 1 : 0; i < fLookupOrder.length; i++) {
				final String value = getInternalValue(key, fLookupOrder[i], true);
				if (value != null) {
					return value;
				}
			}
			return null;
		}
		
		private <T> void setInternalValue(final Preference<T> key, final String value) {
			if (value != null) {
				getNode(fLookupOrder[0], key.getQualifier(), true).put(key.getKey(), value);
			} else {
				getNode(fLookupOrder[0], key.getQualifier(), true).remove(key.getKey());
			}
		}
		
		
		private <T> void setValue(final Preference<T> key, final T value) {
			final IEclipsePreferences node = getNode(fLookupOrder[0], key.getQualifier(), true);
			if (value == null) {
				node.remove(key.getKey());
				return;
			}
			
			final Object valueToStore = key.usage2Store(value);
			switch (key.getStoreType()) {
			case BOOLEAN:
				node.putBoolean(key.getKey(), (Boolean) valueToStore);
				break;
			case INT:
				node.putInt(key.getKey(), (Integer) valueToStore);
				break;
			case LONG:
				node.putLong(key.getKey(), (Long) valueToStore);
				break;
			case DOUBLE:
				node.putDouble(key.getKey(), (Double) valueToStore);
				break;
			case FLOAT:
				node.putFloat(key.getKey(), (Float) valueToStore);
				break;
			default:
				node.put(key.getKey(), (String) valueToStore);
				break;
			}
		}
		
		@SuppressWarnings("unchecked")
		private <T> T getValue(final Preference<T> key) {
			IEclipsePreferences node = null;
			int lookupIndex = 0;
			for (; lookupIndex < fLookupOrder.length; lookupIndex++) {
				final IEclipsePreferences nodeToCheck = getNode(fLookupOrder[lookupIndex], key.getQualifier(), true);
				if (nodeToCheck.get(key.getKey(), null) != null) {
					node = nodeToCheck;
					break;
				}
			}
			if (node == null)
				return null;
			
			Object storedValue;
			switch (key.getStoreType()) {
			case BOOLEAN:
				storedValue = Boolean.valueOf(node.getBoolean(key.getKey(), IPreferenceStore.BOOLEAN_DEFAULT_DEFAULT));
				break;
			case INT:
				storedValue = Integer.valueOf(node.getInt(key.getKey(), IPreferenceStore.INT_DEFAULT_DEFAULT));
				break;
			case LONG:
				storedValue = Long.valueOf(node.getLong(key.getKey(), IPreferenceStore.LONG_DEFAULT_DEFAULT));
				break;
			case DOUBLE:
				storedValue = Double.valueOf(node.getDouble(key.getKey(), IPreferenceStore.DOUBLE_DEFAULT_DEFAULT));
				break;
			case FLOAT:
				storedValue = Float.valueOf(node.getFloat(key.getKey(), IPreferenceStore.FLOAT_DEFAULT_DEFAULT));
				break;
			default:
				storedValue = node.get(key.getKey(), null);
				break;
			}
			if (storedValue == null) {
				return null;
			}
			return key.store2Usage(storedValue);
		}
	}
	
	
	protected IProject fProject;
	protected PreferenceManager fPreferenceManager;
	
	private DataBindingContext fDbc;
	private IStatusChangeListener fStatusListener;
	
	
	protected ManagedConfigurationBlock(final IProject project, final IStatusChangeListener statusListener) {
		super();
		fProject = project;
		fStatusListener = statusListener;
	}
	
	protected ManagedConfigurationBlock(final IProject project) {
		this(project, null);
	}
	
	
	/**
	 * initialize preference management
	 * 
	 * @param container
	 * @param prefs map with preference objects as key and their settings group id as optional value
	 */
	protected void setupPreferenceManager(final Map<Preference, String> prefs) {
		new PreferenceManager(prefs);
	}
	
	protected void initBindings() {
		final Realm realm = Realm.getDefault();
		fDbc = new DataBindingContext(realm);
		addBindings(fDbc, realm);
		
		final AggregateValidationStatus validationStatus = new AggregateValidationStatus(fDbc, AggregateValidationStatus.MAX_SEVERITY);
		validationStatus.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(final ValueChangeEvent event) {
				final IStatus currentStatus = (IStatus) event.diff.getNewValue();
				fStatusListener.statusChanged(currentStatus);
			}
		});
		
//		updateStatus((IStatus) validationStatus.getValue());
//		new DirtyTracker(fDbc) { // sets initial status on first change again, because initial errors are suppressed
//			@Override
//			public void handleChange() {
//				if (!isDirty()) {
//					updateStatus((IStatus) validationStatus.getValue());
//					super.handleChange();
//				}
//			}
//		};
	}
	
	protected DataBindingContext getDbc() {
		return fDbc;
	}
	
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
	}
	
	/**
	 * Point to hook, before the managed preference values are saved to store.
	 * E.g. you can set some additional (or all) values.
	 */
	protected void updatePreferences() {
	}
	
	@Override
	public void performApply() {
		if (fPreferenceManager != null) {
			updatePreferences();
			fPreferenceManager.processChanges(true);
		}
	}
	
	@Override
	public boolean performOk() {
		if (fPreferenceManager != null) {
			updatePreferences();
			return fPreferenceManager.processChanges(false);
		}
		return false;
	}
	
	@Override
	public void performDefaults() {
		if (fPreferenceManager != null) {
			fPreferenceManager.loadDefaults();
			updateControls();
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		if (fDbc != null) {
			fDbc.dispose();
			fDbc = null;
		}
	}
	
	
/* */
	
	/**
	 * Checks, if project specific options exists
	 * 
	 * @param project to look up
	 * @return
	 */
	public boolean hasProjectSpecificOptions(final IProject project) {
		if (project != null && fPreferenceManager != null) {
			return fPreferenceManager.hasProjectSpecificSettings(project);
		}
		return false;
	}
	
	@Override
	public void setUseProjectSpecificSettings(final boolean enable) {
		super.setUseProjectSpecificSettings(enable);
		if (fProject != null && fPreferenceManager != null) {
			fPreferenceManager.setUseProjectSpecificSettings(enable);
		}
	}
	
	protected void updateControls() {
		if (fDbc != null) {
			fDbc.updateTargets();
		}
	}
	
	
	/* Access preference values ***************************************************/
	
	/**
	 * Returns the value for the specified preference.
	 * 
	 * @param key preference key
	 * @return value of the preference
	 */
	@SuppressWarnings("unchecked")
	public <T> T getPreferenceValue(final Preference<T> key) {
		assert (fPreferenceManager != null);
		assert (key != null);
		
		if (fPreferenceManager.fDisabledProjectSettings != null)
			return (T) fPreferenceManager.fDisabledProjectSettings.get(key);
		return fPreferenceManager.getValue(key);
	}
	
	public IEclipsePreferences[] getPreferenceNodes(final String nodeQualifier) {
		assert (fPreferenceManager != null);
		assert (nodeQualifier != null);
		
		final IEclipsePreferences[] nodes = new IEclipsePreferences[fPreferenceManager.fLookupOrder.length - 1];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = fPreferenceManager.getNode(fPreferenceManager.fLookupOrder[i], nodeQualifier, true);
		}
		return nodes;
	}
	
	public IScopeContext[] getPreferenceContexts() {
		assert (fPreferenceManager != null);
		
		return fPreferenceManager.fLookupOrder;
	}
	
	/**
	 * Sets a preference value in the default store.
	 * 
	 * @param key preference key
	 * @param value new value
	 * @return old value
	 */
	@SuppressWarnings("unchecked")
	public <T> T setPrefValue(final Preference<T> key, final T value) {
		assert (fPreferenceManager != null);
		assert (value != null);
		
		if (fPreferenceManager.fDisabledProjectSettings != null)
			return (T) fPreferenceManager.fDisabledProjectSettings.put(key, value);
		final T oldValue = getPreferenceValue(key);
		fPreferenceManager.setValue(key, value);
		return oldValue;
	}
	
	public void setPrefValues(final Map<Preference, Object> map) {
		for (final Entry<Preference, Object> entry : map.entrySet()) {
			setPrefValue(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Not (yet) supported
	 * @throws UnsupportedOperationException
	 */
	public void addPreferenceNodeListener(final String nodeQualifier, final IPreferenceChangeListener listener) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Not (yet) supported
	 * @throws UnsupportedOperationException
	 */
	public void removePreferenceNodeListener(final String nodeQualifier, final IPreferenceChangeListener listener) {
		throw new UnsupportedOperationException();
	}
	
	
	public IObservableValue createObservable(final Object target) {
		final Preference pref = (Preference) target;
		return new AbstractObservableValue() {
			public Object getValueType() {
				return pref.getUsageType();
			}
			@Override
			protected void doSetValue(final Object value) {
				setPrefValue(pref, value);
			}
			@Override
			protected Object doGetValue() {
				return getPreferenceValue(pref);
			}
			@Override
			public synchronized void dispose() {
				super.dispose();
			}
		};
	}
	
	/**
	 * Changes requires full build, this method should be overwritten
	 * and return the Strings for the dialog.
	 * 
	 * @param workspaceSettings true, if settings for workspace; false, if settings for project.
	 * @return
	 */
	protected String[] getFullBuildDialogStrings(final boolean workspaceSettings) {
		return null;
	}
	
}
