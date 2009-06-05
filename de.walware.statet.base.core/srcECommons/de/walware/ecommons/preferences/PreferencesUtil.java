/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.preferences;

import java.util.Map;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;

import de.walware.statet.base.internal.core.BaseCorePlugin;


public class PreferencesUtil {
	
	
	private static class DefaultImpl implements IPreferenceAccess {
		
		private final IScopeContext[] fContexts;
		
		private DefaultImpl(final IScopeContext[] contexts) {
			fContexts = contexts;
		}
		
		public <T> T getPreferenceValue(final Preference<T> key) {
			return PreferencesUtil.getPrefValue(fContexts, key);
		}
		
		public IEclipsePreferences[] getPreferenceNodes(final String nodeQualifier) {
			final IEclipsePreferences[] nodes = new IEclipsePreferences[fContexts.length - 1];
			for (int i = 0; i < nodes.length; i++) {
				nodes[i] = fContexts[i].getNode(nodeQualifier);
			}
			return nodes;
		}
		
		public IScopeContext[] getPreferenceContexts() {
			return fContexts;
		}
		
		public void addPreferenceNodeListener(final String nodeQualifier, final IPreferenceChangeListener listener) {
			int i = fContexts.length-1;
			if (fContexts.length >= 0) {
				if ((fContexts[i] instanceof DefaultScope)) {
					i--;
				}
				while (i >= 0) {
					final IEclipsePreferences node = fContexts[i--].getNode(nodeQualifier);
					if (node != null) {
						node.addPreferenceChangeListener(listener);
					}
				}
			}
		}
		
		public void removePreferenceNodeListener(final String nodeQualifier, final IPreferenceChangeListener listener) {
			int i = fContexts.length-1;
			if (fContexts.length >= 0) {
				if ((fContexts[i] instanceof DefaultScope)) {
					i--;
				}
				while (i >= 0) {
					final IEclipsePreferences node = fContexts[i--].getNode(nodeQualifier);
					if (node != null) {
						node.removePreferenceChangeListener(listener);
					}
				}
			}
		}
		
	}
	
	private static class MapImpl implements IPreferenceAccess {
		
		
		private Map<Preference, Object> fPreferencesMap;
		
		MapImpl(final Map<Preference, Object> preferencesMap) {
			fPreferencesMap = preferencesMap;
		}
		
		public IScopeContext[] getPreferenceContexts() {
			return new IScopeContext[0];
		}
		
		public IEclipsePreferences[] getPreferenceNodes(final String nodeQualifier) {
			return new IEclipsePreferences[0];
		}
		
		@SuppressWarnings("unchecked")
		public <T> T getPreferenceValue(final Preference<T> key) {
			return (T) fPreferencesMap.get(key);
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
		
	}
	
	
	private final static DefaultImpl DEFAULT_PREFS = new DefaultImpl(new IScopeContext[] {
			new DefaultScope() });
	private final static DefaultImpl INSTANCE_PREFS = new DefaultImpl(new IScopeContext[] {
			new InstanceScope(), new DefaultScope() });
	
	
	public final static IPreferenceAccess getInstancePrefs() {
		return INSTANCE_PREFS;
	}
	
	public final static IPreferenceAccess getDefaultPrefs() {
		return DEFAULT_PREFS;
	}
	
	public static IPreferenceAccess createAccess(final Map<Preference, Object> preferencesMap) {
		return new MapImpl(preferencesMap);
	}
	
	public static IPreferenceAccess createAccess(final IScopeContext[] contexts) {
		return new DefaultImpl(contexts);
	}
	
	
	@SuppressWarnings("unchecked")
	public static <T> T getPrefValue(final IScopeContext[] contexts, final Preference<T> key) {
		Object storedValue = null;
		for (int i = 0; i < contexts.length && storedValue == null; i++) {
			try {
				storedValue = contexts[i].getNode(key.getQualifier()).get(key.getKey(), null);
			}
			catch (final IllegalStateException e) {
			}
		}
		
		return key.store2Usage(storedValue);
	}
	
	public static <T> T getPrefValue(final IScopeContext context, final Preference<T> key) {
		final IEclipsePreferences node = context.getNode(key.getQualifier());
		return getPrefValue(node, key);
	}
	
	private static <T> T getPrefValue(final IEclipsePreferences node, final Preference<T> key) {
		Object storedValue;
		switch (key.getStoreType()) {
		case BOOLEAN:
			storedValue = node.getBoolean(key.getKey(), Preferences.BOOLEAN_DEFAULT_DEFAULT);
			break;
		case INT:
			storedValue = node.getInt(key.getKey(), Preferences.INT_DEFAULT_DEFAULT);
			break;
		case LONG:
			storedValue = node.getLong(key.getKey(), Preferences.LONG_DEFAULT_DEFAULT);
			break;
		case DOUBLE:
			storedValue = node.getDouble(key.getKey(), Preferences.DOUBLE_DEFAULT_DEFAULT);
			break;
		case FLOAT:
			storedValue = node.getFloat(key.getKey(), Preferences.FLOAT_DEFAULT_DEFAULT);
			break;
		default:
			storedValue = node.get(key.getKey(), Preferences.STRING_DEFAULT_DEFAULT);
			break;
		}
		return key.store2Usage(storedValue);
	}
	
	public static <T> void setPrefValue(final IScopeContext context, final Preference<T> key, final T value) {
		final IEclipsePreferences node = context.getNode(key.getQualifier());
		setPrefValue(node, key, value);
	}
	
	@SuppressWarnings("unchecked")
	public static void setPrefValues(final IScopeContext context, final Map<Preference, Object> preferencesMap) {
		for (final Map.Entry<Preference, Object> pref : preferencesMap.entrySet()) {
			setPrefValue(context, pref.getKey(), pref.getValue());
		}
	}
		
	private static <T> void setPrefValue(final IEclipsePreferences node, final Preference<T> key, final T value) {
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
	
	public static IEclipsePreferences[] getRelevantNodes(final String nodeQualifier, final IScopeContext[] contexts) {
		final IEclipsePreferences[] nodes = new IEclipsePreferences[contexts.length - 1];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = contexts[i].getNode(nodeQualifier);
		}
		return nodes;
	}
	
	
	/**
	 * Returns global instance of notifier service
	 * 
	 * @return the notifier
	 */
	public static SettingsChangeNotifier getSettingsChangeNotifier() {
		// Adapt this if used in other context
		return BaseCorePlugin.getDefault().getSettingsChangeNotifier();
	}
	
}
