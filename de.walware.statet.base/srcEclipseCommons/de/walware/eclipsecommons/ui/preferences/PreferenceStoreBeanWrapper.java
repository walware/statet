/*******************************************************************************
 * Copyright (c) 2007 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.preferences;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * Provides PropertyChangeSupport (Java Bean) for bean/preferences in
 * a IPreferenceStore (JFace), e.g. OverlayPreferenceStore.
 */
public class PreferenceStoreBeanWrapper {
	
	private class Preference {
		
		final String storeKey;
		final String beanProperty;
		final IConverter converter;

		public Preference(final String storeKey, final String beanProperty, final IConverter converter) {

			this.storeKey = storeKey;
			this.beanProperty = beanProperty;
			this.converter = converter;
		}
	}
	
	private IPreferenceStore fStore;
	private PropertyChangeSupport fBeanSupport;
	
	private HashMap<String, Preference> fPreferenceMap = new HashMap<String, Preference>();
	
	/**
	 * 
	 */
	public PreferenceStoreBeanWrapper(IPreferenceStore store, Object bean) {
		
		fStore = store;
		fBeanSupport = new PropertyChangeSupport(bean);
		
		fStore.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String storeKey = event.getProperty();
				Preference pref = fPreferenceMap.get(storeKey);
				if (pref != null) {
					Object oldValue = event.getOldValue();
					Object newValue = event.getNewValue();
					if (pref.converter != null && oldValue instanceof String) {
						oldValue = pref.converter.convert(oldValue);
						newValue = pref.converter.convert(newValue);
					}
					fBeanSupport.firePropertyChange(pref.beanProperty, 
							oldValue, newValue);
				}
			}
		});
	}
	
	public void addPreference(String storeKey, String beanProperty, IConverter converter) {
		
		fPreferenceMap.put(storeKey, new Preference(storeKey, beanProperty, converter));
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		
		fBeanSupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		
		fBeanSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		
		fBeanSupport.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		
		fBeanSupport.removePropertyChangeListener(propertyName, listener);
	}
	
}
