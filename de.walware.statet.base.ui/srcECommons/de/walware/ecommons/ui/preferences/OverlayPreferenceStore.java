/*******************************************************************************
 * Copyright (c) 2000-2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import de.walware.ecommons.preferences.Preference.Type;


/**
 * An overlaying preference store.
 */
public class OverlayPreferenceStore implements IPreferenceStore {
	
	private class PropertyListener implements IPropertyChangeListener {
				
		/*
		 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		public void propertyChange(final PropertyChangeEvent event) {
			final OverlayStorePreference key= findPreferenceKey(event.getProperty());
			if (key != null)
				propagateProperty(fParent, key, fStore); 
		}
	}
	
	
	private IPreferenceStore fParent;
	private IPreferenceStore fStore;
	private OverlayStorePreference[] fPreferenceKeys;
	
	private PropertyListener fPropertyListener;
	private boolean fLoaded;
	
	
	public OverlayPreferenceStore(final IPreferenceStore parent, final OverlayStorePreference[] PreferenceKeys) {
		fParent= parent;
		fPreferenceKeys= PreferenceKeys;
		fStore= new PreferenceStore();
	}
	
	
	private OverlayStorePreference findPreferenceKey(final String key) {
		for (int i= 0; i < fPreferenceKeys.length; i++) {
			if (fPreferenceKeys[i].fKey.equals(key))
				return fPreferenceKeys[i];
		}
		return null;
	}
	
	private boolean covers(final String key) {
		return (findPreferenceKey(key) != null);
	}
	
	private void propagateProperty(final IPreferenceStore orgin, final OverlayStorePreference key, final IPreferenceStore target) {
		
		if (orgin.isDefault(key.fKey)) {
			if (!target.isDefault(key.fKey))
				target.setToDefault(key.fKey);
			return;
		}
		
		final Type type = key.fType;
		switch (type) {
		
		case BOOLEAN: {
			final boolean originValue= orgin.getBoolean(key.fKey);
			final boolean targetValue= target.getBoolean(key.fKey);
			if (targetValue != originValue)
				target.setValue(key.fKey, originValue);
			break; }
				
		case DOUBLE: {
			final double originValue= orgin.getDouble(key.fKey);
			final double targetValue= target.getDouble(key.fKey);
			if (targetValue != originValue)
				target.setValue(key.fKey, originValue);
			break; }
		
		case FLOAT: {
			final float originValue= orgin.getFloat(key.fKey);
			final float targetValue= target.getFloat(key.fKey);
			if (targetValue != originValue)
				target.setValue(key.fKey, originValue);
			break; }
		
		case INT: {
			final int originValue= orgin.getInt(key.fKey);
			final int targetValue= target.getInt(key.fKey);
			if (targetValue != originValue)
				target.setValue(key.fKey, originValue);
			break; }
		
		case LONG: {
			final long originValue= orgin.getLong(key.fKey);
			final long targetValue= target.getLong(key.fKey);
			if (targetValue != originValue)
				target.setValue(key.fKey, originValue);
			break; }
		
		case STRING: {
			final String originValue= orgin.getString(key.fKey);
			final String targetValue= target.getString(key.fKey);
			if (targetValue != null && originValue != null && !targetValue.equals(originValue))
				target.setValue(key.fKey, originValue);
			break; }
		}
	}
	
	public void propagate() {
		for (int i= 0; i < fPreferenceKeys.length; i++)
			propagateProperty(fStore, fPreferenceKeys[i], fParent);
	}
	
	private void loadProperty(final IPreferenceStore orgin, final OverlayStorePreference key, final IPreferenceStore target, final boolean forceInitialization) {
		
		final Type type = key.fType;
		switch (type) {
		
		case BOOLEAN: {
			if (forceInitialization)
				target.setValue(key.fKey, true);
			target.setValue(key.fKey, orgin.getBoolean(key.fKey));
			target.setDefault(key.fKey, orgin.getDefaultBoolean(key.fKey));
			break; }
		
		case DOUBLE: {
			if (forceInitialization)
				target.setValue(key.fKey, 1.0D);
			target.setValue(key.fKey, orgin.getDouble(key.fKey));
			target.setDefault(key.fKey, orgin.getDefaultDouble(key.fKey));
			break; }
			
		case FLOAT: {
			if (forceInitialization)
				target.setValue(key.fKey, 1.0F);
			target.setValue(key.fKey, orgin.getFloat(key.fKey));
			target.setDefault(key.fKey, orgin.getDefaultFloat(key.fKey));
			break; }
		
		case INT: {
			if (forceInitialization)
				target.setValue(key.fKey, 1);
			target.setValue(key.fKey, orgin.getInt(key.fKey));
			target.setDefault(key.fKey, orgin.getDefaultInt(key.fKey));
			break; }
		
		case LONG: {
			if (forceInitialization)
				target.setValue(key.fKey, 1L);
			target.setValue(key.fKey, orgin.getLong(key.fKey));
			target.setDefault(key.fKey, orgin.getDefaultLong(key.fKey));
			break; }
			
		case STRING: {
			if (forceInitialization)
				target.setValue(key.fKey, "1"); //$NON-NLS-1$
			target.setValue(key.fKey, orgin.getString(key.fKey));
			target.setDefault(key.fKey, orgin.getDefaultString(key.fKey));
			break; }
		}
	}
	
	public void load() {
		for (int i= 0; i < fPreferenceKeys.length; i++)
			loadProperty(fParent, fPreferenceKeys[i], fStore, true);
		
		fLoaded= true;
		
	}
	
	public void loadDefaults() {
		for (int i= 0; i < fPreferenceKeys.length; i++)
			setToDefault(fPreferenceKeys[i].fKey);
	}
	
	public void start() {
		if (fPropertyListener == null) {
			fPropertyListener= new PropertyListener();
			fParent.addPropertyChangeListener(fPropertyListener);
		}
	}
	
	public void stop() {
		if (fPropertyListener != null)  {
			fParent.removePropertyChangeListener(fPropertyListener);
			fPropertyListener= null;
		}
	}
	
	
	public void addPropertyChangeListener(final IPropertyChangeListener listener) {
		fStore.addPropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(final IPropertyChangeListener listener) {
		fStore.removePropertyChangeListener(listener);
	}
	
	public void firePropertyChangeEvent(final String name, final Object oldValue, final Object newValue) {
		fStore.firePropertyChangeEvent(name, oldValue, newValue);
	}
	
	public boolean contains(final String name) {
		return fStore.contains(name);
	}
	
	public boolean getBoolean(final String name) {
		return fStore.getBoolean(name);
	}
	
	public boolean getDefaultBoolean(final String name) {
		return fStore.getDefaultBoolean(name);
	}
	
	public double getDefaultDouble(final String name) {
		return fStore.getDefaultDouble(name);
	}
	
	public float getDefaultFloat(final String name) {
		return fStore.getDefaultFloat(name);
	}
	
	public int getDefaultInt(final String name) {
		return fStore.getDefaultInt(name);
	}
	
	public long getDefaultLong(final String name) {
		return fStore.getDefaultLong(name);
	}
	
	public String getDefaultString(final String name) {
		return fStore.getDefaultString(name);
	}
	
	public double getDouble(final String name) {
		return fStore.getDouble(name);
	}
	
	public float getFloat(final String name) {
		return fStore.getFloat(name);
	}
	
	public int getInt(final String name) {
		return fStore.getInt(name);
	}
	
	public long getLong(final String name) {
		return fStore.getLong(name);
	}
	
	public String getString(final String name) {
		return fStore.getString(name);
	}
	
	public boolean isDefault(final String name) {
		return fStore.isDefault(name);
	}
	
	public boolean needsSaving() {
		return fStore.needsSaving();
	}
	
	public void putValue(final String name, final String value) {
		if (covers(name))
			fStore.putValue(name, value);
	}
	
	public void setDefault(final String name, final double value) {
		if (covers(name))
			fStore.setDefault(name, value);
	}
	
	public void setDefault(final String name, final float value) {
		if (covers(name))
			fStore.setDefault(name, value);
	}
	
	public void setDefault(final String name, final int value) {
		if (covers(name))
			fStore.setDefault(name, value);
	}
	
	public void setDefault(final String name, final long value) {
		if (covers(name))
			fStore.setDefault(name, value);
	}
	
	public void setDefault(final String name, final String value) {
		if (covers(name))
			fStore.setDefault(name, value);
	}
	
	public void setDefault(final String name, final boolean value) {
		if (covers(name))
			fStore.setDefault(name, value);
	}
	
	public void setToDefault(final String name) {
		fStore.setToDefault(name);
	}
	
	public void setValue(final String name, final double value) {
		if (covers(name))
			fStore.setValue(name, value);
	}
	
	public void setValue(final String name, final float value) {
		if (covers(name))
			fStore.setValue(name, value);
	}
	
	public void setValue(final String name, final int value) {
		if (covers(name))
			fStore.setValue(name, value);
	}
	
	public void setValue(final String name, final long value) {
		if (covers(name))
			fStore.setValue(name, value);
	}
	
	public void setValue(final String name, final String value) {
		if (covers(name))
			fStore.setValue(name, value);
	}
	
	public void setValue(final String name, final boolean value) {
		if (covers(name))
			fStore.setValue(name, value);
	}
	
	/**
	 * The keys to add to the list of overlay keys.
	 * <p>
	 * Note: This method must be called before {@link #load()} is called. 
	 * </p>
	 * 
	 * @param keys
	 * @since 3.0
	 */
	public void addKeys(final OverlayStorePreference[] keys) {
		assert (!fLoaded);
		assert (keys != null);
		
		final int PreferenceKeysLength= fPreferenceKeys.length;
		final OverlayStorePreference[] result= new OverlayStorePreference[keys.length + PreferenceKeysLength];
		
		for (int i= 0, length= PreferenceKeysLength; i < length; i++)
			result[i]= fPreferenceKeys[i];
		
		for (int i= 0, length= keys.length; i < length; i++)
			result[PreferenceKeysLength + i]= keys[i];
		
		fPreferenceKeys= result;
		
		if (fLoaded)
			load();
	}
	
}
