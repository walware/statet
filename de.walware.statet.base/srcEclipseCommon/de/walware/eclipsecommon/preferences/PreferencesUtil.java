/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommon.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;


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
		switch (key.getType()) {
		case BOOLEAN:
			return (T) Boolean.valueOf(service.getBoolean(key.getQualifier(), key.getKey(), IPreferenceStore.BOOLEAN_DEFAULT_DEFAULT, contexts));
		case INT:
			return (T) Integer.valueOf(service.getInt(key.getQualifier(), key.getKey(), IPreferenceStore.INT_DEFAULT_DEFAULT, contexts));
		case LONG:
			return (T) Long.valueOf(service.getLong(key.getQualifier(), key.getKey(), IPreferenceStore.LONG_DEFAULT_DEFAULT, contexts));
		case DOUBLE:
			return (T) Double.valueOf(service.getDouble(key.getQualifier(), key.getKey(), IPreferenceStore.DOUBLE_DEFAULT_DEFAULT, contexts));
		case FLOAT:
			return (T) Float.valueOf(service.getFloat(key.getQualifier(), key.getKey(), IPreferenceStore.FLOAT_DEFAULT_DEFAULT, contexts));
		default:
			return (T) service.getString(key.getQualifier(), key.getKey(), IPreferenceStore.STRING_DEFAULT_DEFAULT, contexts);
		}
	}

	public static <T> void setPrefValue(IScopeContext context, Preference<T> key, T value) {
		
		IEclipsePreferences node = context.getNode(key.getQualifier());
		if (value == null)
			node.remove(key.getKey());
		
		switch (key.getType()) {
		case BOOLEAN:
			node.putBoolean(key.getKey(), (Boolean) value);
			break;
		case INT:
			node.putInt(key.getKey(), (Integer) value);
			break;
		case LONG:
			node.putLong(key.getKey(), (Long) value);
			break;
		case DOUBLE:
			node.putDouble(key.getKey(), (Double) value);
			break;
		case FLOAT:
			node.putFloat(key.getKey(), (Float) value);
			break;
		default:
			node.put(key.getKey(), (String) value);
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
