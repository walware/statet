/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.preferences;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.preferences.IWorkingCopyManager;
import org.osgi.service.prefs.BackingStoreException;

import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.ui.dialogs.IStatusChangeListener;
import de.walware.eclipsecommons.ui.preferences.AbstractConfigurationBlock;

import de.walware.statet.base.core.CoreUtility;
import de.walware.statet.base.internal.ui.StatetUIPlugin;


public class ManagedConfigurationBlock extends AbstractConfigurationBlock 
		implements IPreferenceAccess, IObservableFactory {

	
	protected class PreferenceManager {
		
		private IScopeContext[] fLookupOrder;
		protected final Preference[] fPreferenceKeys;
		
		private final IWorkbenchPreferenceContainer fContainer;
		/** Manager for a working copy of the preferences */
		private final IWorkingCopyManager fManager;
		/** Map saving the project settings, if disabled */
		private Map<Preference, Object> fDisabledProjectSettings;
		
		
		PreferenceManager(IWorkbenchPreferenceContainer container, Preference[] keys) {
			
			fContainer = container;
			fManager = fContainer.getWorkingCopyManager();
			fPreferenceKeys = keys;
			
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
			
			// XXX
			testIfOptionsComplete();

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
		boolean hasProjectSpecificSettings(IProject project) {
			
			IScopeContext projectContext = new ProjectScope(project);
			for (Preference<Object> key : fPreferenceKeys) {
				if (getInternalValue(key, projectContext, true) != null)
					return true;
			}
			return false;
		}	

		void setUseProjectSpecificSettings(boolean enable) {
			
			boolean hasProjectSpecificOption = (fDisabledProjectSettings == null);
			
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
			for (Preference<Object> key : fPreferenceKeys) {
				fDisabledProjectSettings.put(key, getValue(key));
				setInternalValue(key, null); // clear project settings
			}
			
		}
		
		private void loadDisabledProjectSettings() {

			for (Preference<Object> key : fPreferenceKeys) {
				// Copy values from saved disabled settings to working store
				setValue(key, fDisabledProjectSettings.get(key));
			}
			fDisabledProjectSettings = null;
		}

		boolean processChanges(boolean saveStore) {

			List<Preference> changedOptions = new ArrayList<Preference>();
			boolean needsBuild = getChanges(changedOptions);
			if (changedOptions.isEmpty()) {
				return true;
			}
			
			boolean doBuild = false;
			if (needsBuild) {
				String[] strings = getFullBuildDialogStrings(fProject == null);
				if (strings != null) {
					MessageDialog dialog = new MessageDialog(getShell(), 
							strings[0], null, strings[1],	
							MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL }, 2);
					int res = dialog.open();
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
				} catch (BackingStoreException e) {
					StatetUIPlugin.logUnexpectedError(e);
					return false;
				}
				if (doBuild) {
					CoreUtility.getBuildJob(fProject).schedule();
				}
			} else
				if (doBuild) {
					fContainer.registerUpdateJob(CoreUtility.getBuildJob(fProject));
				}
			return true;
		}
		
		/**
		 * 
		 * @param currContext
		 * @param changedSettings
		 * @return true, if rebuild is required.
		 */
		private boolean getChanges(List<Preference> changedSettings) {
			
			IScopeContext currContext = fLookupOrder[0];
			boolean needsBuild = false;
			for (Preference<Object> key : fPreferenceKeys) {
				String oldVal = getInternalValue(key, currContext, false);
				String val = getInternalValue(key, currContext, true);
				if (val == null) {
					if (oldVal != null) {
						changedSettings.add(key);
						needsBuild |= !oldVal.equals(getInternalValue(key, true));
					}
				} else if (!val.equals(oldVal)) {
					changedSettings.add(key);
					needsBuild |= oldVal != null || !val.equals(getInternalValue(key, true));
				}
			}
			return needsBuild;
		}
		
		
		void loadDefaults() {

			DefaultScope defaultScope = new DefaultScope();
			for (Preference<Object> key : fPreferenceKeys) {
				String defValue = getInternalValue(key, defaultScope, false);
				setInternalValue(key, defValue);
			}

		}
		
		// DEBUG
		private void testIfOptionsComplete() {
			
			for (Preference<Object> key : fPreferenceKeys) {
				if (getInternalValue(key, false) == null) {
					System.out.println("preference option missing: " + key + " (" + this.getClass().getName() +')');  //$NON-NLS-1$//$NON-NLS-2$
				}
			}
		}

		private IEclipsePreferences getNode(IScopeContext context, String qualifier, boolean useWorkingCopy) {
			
			IEclipsePreferences node = context.getNode(qualifier);
			if (useWorkingCopy) {
				return fManager.getWorkingCopy(node);
			}
			return node;
		}

		private String getInternalValue(Preference<Object> key, IScopeContext context, boolean useWorkingCopy) {
			
			return getNode(context, key.getQualifier(), useWorkingCopy).get(key.getKey(), null);
		}
		
		private String getInternalValue(Preference<Object> key, boolean ignoreTopScope) {
			
			for (int i = ignoreTopScope ? 1 : 0; i < fLookupOrder.length; i++) {
				String value = getInternalValue(key, fLookupOrder[i], true);
				if (value != null) {
					return value;
				}
			}
			return null;
		}
		
		private void setInternalValue(Preference<Object> key, String value) {
			
			if (value != null) {
				getNode(fLookupOrder[0], key.getQualifier(), true).put(key.getKey(), value);
			} else {
				getNode(fLookupOrder[0], key.getQualifier(), true).remove(key.getKey());
			}
		}
		
		
		private <T> void setValue(Preference<T> key, T value) {
			
			IEclipsePreferences node = getNode(fLookupOrder[0], key.getQualifier(), true);
			if (value == null) {
				node.remove(key.getKey());
				return;
			}
			
			Object valueToStore = key.usage2Store(value);
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
		private <T> T getValue(Preference<T> key) {
			
			IEclipsePreferences node = null;
			int lookupIndex = 0;
			for (; lookupIndex < fLookupOrder.length; lookupIndex++) {
				IEclipsePreferences nodeToCheck = getNode(fLookupOrder[lookupIndex], key.getQualifier(), true);
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
	private AggregateValidationStatus fAggregateStatus;
	private IStatusChangeListener fStatusListener;


	protected ManagedConfigurationBlock(IProject project, IStatusChangeListener statusListener) {
		super();
		fProject = project;
		fStatusListener = statusListener;
	}
	
	protected ManagedConfigurationBlock(IProject project) {
		this(project, null);
	}

	protected void setupPreferenceManager(IWorkbenchPreferenceContainer container, Preference[] keys) {
		
		new PreferenceManager(container, keys);
	}
	
	protected void createDbc() {
		
		Realm realm = Realm.getDefault();
		fDbc = new DataBindingContext(realm);
		addBindings(fDbc, realm);
		
		fAggregateStatus = new AggregateValidationStatus(fDbc.getBindings(),
				AggregateValidationStatus.MAX_SEVERITY);
		fAggregateStatus.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(ValueChangeEvent event) {
				IStatus currentStatus = (IStatus) event.diff.getNewValue();
				fStatusListener.statusChanged(currentStatus);
			}
		});
	}
	
	protected DataBindingContext getDbc() {
		
		return fDbc;
	}
	
	protected void addBindings(DataBindingContext dbc, Realm realm) {
	}
	
	/**
	 * Point to hook, before the managed preference values are saved to store.
	 * E.g. you can set some additional (or all) values.
	 */
	protected void onBeforeSave() {
		
	}
	
	@Override
	public void performApply() {
		
		if (fPreferenceManager != null) {
			onBeforeSave();
			fPreferenceManager.processChanges(true);
		}
	}
	
	public boolean performOk() {
		
		if (fPreferenceManager != null) {
			onBeforeSave();
			return fPreferenceManager.processChanges(false);
		}
		return false;
	}

	public void performDefaults() {
		
		if (fPreferenceManager != null) {
			fPreferenceManager.loadDefaults();
			updateControls();
		}
	}
	
	@Override
	public void dispose() {
		
		super.dispose();

		if (fAggregateStatus != null) {
			fAggregateStatus.dispose();
			fAggregateStatus = null;
		}
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
	public boolean hasProjectSpecificOptions(IProject project) {
		
		if (project != null && fPreferenceManager != null)
			return fPreferenceManager.hasProjectSpecificSettings(project);
		return false;
	}
	
	public void setUseProjectSpecificSettings(boolean enable) {
		
		super.setUseProjectSpecificSettings(enable);
		
		if (fProject != null && fPreferenceManager != null)
			fPreferenceManager.setUseProjectSpecificSettings(enable);
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
	public <T> T getPreferenceValue(Preference<T> key) {

		assert (fPreferenceManager != null);
		assert (key != null);
		
		if (fPreferenceManager.fDisabledProjectSettings != null)
			return (T) fPreferenceManager.fDisabledProjectSettings.get(key);
		return (T) fPreferenceManager.getValue(key);
	}
	
	public IEclipsePreferences[] getPreferenceNodes(String nodeQualifier) {
		
		assert (fPreferenceManager != null);
		assert (nodeQualifier != null);
		
		IEclipsePreferences[] nodes = new IEclipsePreferences[fPreferenceManager.fLookupOrder.length - 1];
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
	public <T> T setPrefValue(Preference<T> key, T value) {
		
		assert (fPreferenceManager != null);
		assert (value != null);
		
		if (fPreferenceManager.fDisabledProjectSettings != null)
			return (T) fPreferenceManager.fDisabledProjectSettings.put(key, value);
		T oldValue = (T) getPreferenceValue(key);
		fPreferenceManager.setValue(key, value);
		return oldValue;
	}
	
	public void setPrefValues(Map<Preference, Object> map) {
		
		for (Preference<Object> unit : map.keySet()) {
			setPrefValue(unit, map.get(unit));
		}
	}
	
	public IObservableValue createObservable(Object target) {
		
		final Preference pref = (Preference) target;
		return new AbstractObservableValue() {
			public Object getValueType() {
				return pref.getUsageType();
			}
			@Override
			protected void doSetValue(Object value) {
				setPrefValue(pref, value);
			}
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
	protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
		
		return null;
	}
}
