/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.preferences;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import de.walware.ecommons.preferences.Preference;


/**
 * Provides PropertyChangeSupport (Java Bean) for bean/preferences in
 * a IPreferenceStore (JFace), e.g. OverlayPreferenceStore.
 * 
 * All property-preference-pairs must be registered using {@link #addPreference(String, Preference)}.
 */
public class PreferenceStoreBeanWrapper {
	
	private class PrefData {
		
		final String beanProperty;
		final Preference converter;
		
		public PrefData(final String beanProperty, final Preference converter) {
			this.beanProperty = beanProperty;
			this.converter = converter;
		}
	}
	
	private IPreferenceStore fStore;
	private PropertyChangeSupport fBeanSupport;
	
	private HashMap<String, PrefData> fPreferenceMap = new HashMap<String, PrefData>();
	
	/**
	 * 
	 */
	public PreferenceStoreBeanWrapper(final IPreferenceStore store, final Object bean) {
		fStore = store;
		fBeanSupport = new PropertyChangeSupport(bean);
		
		fStore.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent event) {
				final String storeKey = event.getProperty();
				final PrefData pref = fPreferenceMap.get(storeKey);
				if (pref != null) {
					Object oldValue = event.getOldValue();
					Object newValue = event.getNewValue();
					if (pref.converter != null) {
						if (!pref.converter.getUsageType().isInstance(oldValue)) {
							oldValue = pref.converter.store2Usage(oldValue);
						}
						if (!pref.converter.getUsageType().isInstance(newValue)) {
							newValue = pref.converter.store2Usage(newValue);
						}
					}
					fBeanSupport.firePropertyChange(pref.beanProperty, 
							oldValue, newValue);
				}
			}
		});
	}
	
	/**
	 * Registers a property/preference, which should be managed.
	 * 
	 * @param beanProperty name of bean property
	 * @param preference identifier for preference store (qualifier not necessary, because not supported by PreferenceStore)
	 */
	public void addPreference(final String beanProperty, final Preference preference) {
		fPreferenceMap.put(preference.getKey(), new PrefData(beanProperty, preference));
	}
	
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		fBeanSupport.addPropertyChangeListener(listener);
	}
	
	public void addPropertyChangeListener(final String propertyName,
			final PropertyChangeListener listener) {
		fBeanSupport.addPropertyChangeListener(propertyName, listener);
	}
	
	public void removePropertyChangeListener(final PropertyChangeListener listener) {
		fBeanSupport.removePropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(final String propertyName,
			final PropertyChangeListener listener) {
		fBeanSupport.removePropertyChangeListener(propertyName, listener);
	}
	
}
