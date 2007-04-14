/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;


public class PreferencesUtil {


	private static class DefaultImpl implements IPreferenceAccess {
		
		private IScopeContext[] fContexts;
		
		private DefaultImpl(IScopeContext[] contexts) {
			
			fContexts = contexts;
		}
		
		public <T> T getPreferenceValue(Preference<T> key) {
			
			return PreferencesUtil.getPrefValue(fContexts, key);
		}
		
		public IEclipsePreferences[] getPreferenceNodes(String nodeQualifier) {
			
			return PreferencesUtil.getRelevantNodes(nodeQualifier, fContexts);
		}
		
		public IScopeContext[] getPreferenceContexts() {

			return fContexts;
		} 
	}

	
	private static DefaultImpl fgDefaultPreferences;
	private static DefaultImpl fgInstancePreferences;
	
	
	public static synchronized IPreferenceAccess getInstancePrefs() {
		
		if (fgInstancePreferences == null)
			fgInstancePreferences = new DefaultImpl(new IScopeContext[] {
							new InstanceScope(),
							new DefaultScope(),
			});
		return fgInstancePreferences;
	}
	
	public static synchronized IPreferenceAccess getDefaultPrefs() {
		
		if (fgDefaultPreferences == null)
			fgDefaultPreferences = new DefaultImpl(new IScopeContext[] {
							new DefaultScope(),
			});
		return fgDefaultPreferences;
	}
	
		
	@SuppressWarnings("unchecked")
	public static <T> T getPrefValue(IScopeContext[] contexts, Preference<T> key) {
					
		IPreferencesService service = Platform.getPreferencesService();

		Object storedValue;
		switch (key.getStoreType()) {
		case BOOLEAN:
			storedValue = Boolean.valueOf(service.getBoolean(key.getQualifier(), key.getKey(), Preferences.BOOLEAN_DEFAULT_DEFAULT, contexts));
			break;
		case INT:
			storedValue = Integer.valueOf(service.getInt(key.getQualifier(), key.getKey(), Preferences.INT_DEFAULT_DEFAULT, contexts));
			break;
		case LONG:
			storedValue = Long.valueOf(service.getLong(key.getQualifier(), key.getKey(), Preferences.LONG_DEFAULT_DEFAULT, contexts));
			break;
		case DOUBLE:
			storedValue = Double.valueOf(service.getDouble(key.getQualifier(), key.getKey(), Preferences.DOUBLE_DEFAULT_DEFAULT, contexts));
			break;
		case FLOAT:
			storedValue = Float.valueOf(service.getFloat(key.getQualifier(), key.getKey(), Preferences.FLOAT_DEFAULT_DEFAULT, contexts));
			break;
		default:
			storedValue = service.getString(key.getQualifier(), key.getKey(), Preferences.STRING_DEFAULT_DEFAULT, contexts);
			break;
		}
		return key.store2Usage(storedValue);
	}

	public static <T> void setPrefValue(IScopeContext context, Preference<T> key, T value) {
		
		IEclipsePreferences node = context.getNode(key.getQualifier());
		
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
	
	public static IEclipsePreferences[] getRelevantNodes(String nodeQualifier, IScopeContext[] contexts) {
		
		IEclipsePreferences[] nodes = new IEclipsePreferences[contexts.length - 1];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = contexts[i].getNode(nodeQualifier);
		}
		return nodes;
	}

}
