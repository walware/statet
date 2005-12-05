/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.preferences.IWorkingCopyManager;
import org.osgi.service.prefs.BackingStoreException;

import de.walware.eclipsecommon.ui.preferences.AbstractConfigurationBlock;
import de.walware.eclipsecommon.ui.preferences.PreferenceKey;
import de.walware.statet.base.CoreUtility;
import de.walware.statet.base.StatetPlugin;


public class ManagedConfigurationBlock extends AbstractConfigurationBlock {

	
	public static final class Key extends PreferenceKey {
	
		private String fQualifier;
		
		public Key(String qualifier, String key) {
			
			this(qualifier, key, Type.STRING);
		}
		
		public Key(String qualifier, String key, Type type) {
			
			super(key, type);
			fQualifier = qualifier;
		}
		
		public String getQualifier() {
			
			return fQualifier;
		}

		public String getName() {
			
			return fKey;
		}
		
		private IEclipsePreferences getNode(IScopeContext context, IWorkingCopyManager manager) {
			
			IEclipsePreferences node = context.getNode(fQualifier);
			if (manager != null) {
				return manager.getWorkingCopy(node);
			}
			return node;
		}
		
		public String getStoredString(IScopeContext context, IWorkingCopyManager manager) {
			
			return getNode(context, manager).get(fKey, null);
		}
		
		public String getStoredValue(IScopeContext[] lookupOrder, boolean ignoreTopScope, IWorkingCopyManager manager) {
			
			for (int i = ignoreTopScope ? 1 : 0; i < lookupOrder.length; i++) {
				String value = getStoredString(lookupOrder[i], manager);
				if (value != null) {
					return value;
				}
			}
			return null;
		}
		
		public void setStoredValue(IScopeContext context, String value, IWorkingCopyManager manager) {
			
			if (value != null) {
				getNode(context, manager).put(fKey, value);
			} else {
				getNode(context, manager).remove(fKey);
			}
		}
			
		public String toString() {
			return fQualifier + '/' + fKey;
		}
	}
	
	protected class PreferenceManager {
		
		private IScopeContext[] fLookupOrder;
		protected final Key[] fPreferenceKeys;
		
		private final IWorkbenchPreferenceContainer fContainer;
		/** Manager for a working copy of the preferences */
		private final IWorkingCopyManager fManager;
		/** Map saving the project settings, if disabled */
		private Map<Key, String> fDisabledProjectSettings;
		
		
		PreferenceManager(IWorkbenchPreferenceContainer container, Key[] keys) {
			
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
			
			testIfOptionsComplete();

			// init disabled settings, if required
			if (fProject == null || hasProjectSpecificSettings(fProject)) {
				fDisabledProjectSettings = null;
			} else {
				fDisabledProjectSettings = new IdentityHashMap<Key, String>();
				for (Key key : fPreferenceKeys) {
					fDisabledProjectSettings.put(key, key.getStoredValue(fLookupOrder, false, fManager));
				}
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
			for (Key key : fPreferenceKeys) {
				if (key.getStoredString(projectContext, fPreferenceManager.fManager) != null)
					return true;
			}
			return false;
		}	

		void setUseProjectSpecificSettings(boolean enable) {
			
			boolean hasProjectSpecificOption = fDisabledProjectSettings == null;
			if (enable != hasProjectSpecificOption) {
				if (enable) {
					for (Key key : fPreferenceKeys) {
						// Copy values from saved disabled settings to working store
						String value = fDisabledProjectSettings.get(key);
						key.setStoredValue(fLookupOrder[0], value, fManager);
					}
					fDisabledProjectSettings = null;
				} else {
					fDisabledProjectSettings = new IdentityHashMap<Key, String>();
					for (Key key : fPreferenceKeys) {
						String oldValue = key.getStoredValue(fLookupOrder, false, fManager);
						fDisabledProjectSettings.put(key, oldValue);
						key.setStoredValue(fLookupOrder[0], null, fManager); // clear project settings
					}
				}
			}
		}

		boolean processChanges(boolean saveStore) {

			IScopeContext currContext = fLookupOrder[0];
		
			List<Key> changedOptions = new ArrayList<Key>();
			boolean needsBuild = getChanges(currContext, changedOptions);
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
					StatetPlugin.logUnexpectedError(e);
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
		 * @param currContext
		 * @param changedSettings
		 * @return true, if rebuild is required.
		 */
		private boolean getChanges(IScopeContext currContext, List<Key> changedSettings) {
			
			boolean needsBuild = false;
			for (Key key : fPreferenceKeys) {
				String oldVal = key.getStoredString(currContext, null);
				String val = key.getStoredString(currContext, fManager);
				if (val == null) {
					if (oldVal != null) {
						changedSettings.add(key);
						needsBuild |= !oldVal.equals(key.getStoredValue(fLookupOrder, true, fManager));
					}
				} else if (!val.equals(oldVal)) {
					changedSettings.add(key);
					needsBuild |= oldVal != null || !val.equals(key.getStoredValue(fLookupOrder, true, fManager));
				}
			}
			return needsBuild;
		}
		
		
		void loadDefaults() {

			DefaultScope defaultScope = new DefaultScope();
			for (Key key : fPreferenceKeys) {
				String defValue = key.getStoredString(defaultScope, null);
				setValue(key, defValue);
			}

		}
		
		// DEBUG
		private void testIfOptionsComplete() {
			
			for (Key key : fPreferenceKeys) {
				if (key.getStoredValue(fLookupOrder, false, fManager) == null) {
					System.out.println("preference option missing: " + key + " (" + this.getClass().getName() +')');  //$NON-NLS-1$//$NON-NLS-2$
				}
			}
		}
	}
	
	
	protected IProject fProject;
	protected PreferenceManager fPreferenceManager;
	

	public ManagedConfigurationBlock(IProject project) {

		super();
		
		fProject = project;
	}

	public ManagedConfigurationBlock() {
		this(null);
	}

	protected void setupPreferenceManager(IWorkbenchPreferenceContainer container, Key[] keys) {
		
		new PreferenceManager(container, keys);
	}
	
	
	@Override
	public void performApply() {
		
		if (fPreferenceManager != null)
			fPreferenceManager.processChanges(true);
	}
	
	public boolean performOk() {
		
		if (fPreferenceManager != null)
			return fPreferenceManager.processChanges(false);
		return false;
	}

	public void performDefaults() {
		
		if (fPreferenceManager != null) {
			fPreferenceManager.loadDefaults();
			updateControls();
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
		
	}

	
	
	protected static Key createKey(String plugin, String key) {

		return new Key(plugin, key);
	}
	
	protected final static Key createStatetCoreKey(String key) {
		
		return new Key(StatetPlugin.ID, key);
	}

	/* Access preference values ***************************************************/
	
	/**
	 * Returns the value for the specified preference.
	 * 
	 * @param key preference key
	 * @return value of the preference
	 */
	protected String getValue(Key key) {
		
		if (fPreferenceManager.fDisabledProjectSettings != null)
			return fPreferenceManager.fDisabledProjectSettings.get(key);
		return key.getStoredValue(fPreferenceManager.fLookupOrder, false, fPreferenceManager.fManager);
	}
	
	/**
	 * Returns the value for the boolean specified preference.
	 * 
	 * @param key preference key
	 * @return value of the preference
	 */
	protected boolean getBooleanValue(Key key) {
		
		return Boolean.valueOf(getValue(key)).booleanValue();
	}
	
	/**
	 * Sets a preference value in the default store.
	 * 
	 * @param key preference key
	 * @param value new value 
	 * @return old value
	 */
	protected String setValue(Key key, String value) {
		
		if (fPreferenceManager.fDisabledProjectSettings != null)
			return (String) fPreferenceManager.fDisabledProjectSettings.put(key, value);
		String oldValue = getValue(key);
		key.setStoredValue(fPreferenceManager.fLookupOrder[0], value, fPreferenceManager.fManager);
		return oldValue;
	}
	
	/**
	 * Sets a boolean preference value in the default store.
	 * 
	 * @param key preference key
	 * @param value new value 
	 * @return old value
	 */
	protected String setValue(Key key, boolean value) {
		
		return setValue(key, String.valueOf(value));
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
