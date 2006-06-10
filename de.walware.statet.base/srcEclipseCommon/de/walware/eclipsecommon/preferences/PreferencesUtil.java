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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;


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
			storedValue = Boolean.valueOf(service.getBoolean(key.getQualifier(), key.getKey(), IPreferenceStore.BOOLEAN_DEFAULT_DEFAULT, contexts));
			break;
		case INT:
			storedValue = Integer.valueOf(service.getInt(key.getQualifier(), key.getKey(), IPreferenceStore.INT_DEFAULT_DEFAULT, contexts));
			break;
		case LONG:
			storedValue = Long.valueOf(service.getLong(key.getQualifier(), key.getKey(), IPreferenceStore.LONG_DEFAULT_DEFAULT, contexts));
			break;
		case DOUBLE:
			storedValue = Double.valueOf(service.getDouble(key.getQualifier(), key.getKey(), IPreferenceStore.DOUBLE_DEFAULT_DEFAULT, contexts));
			break;
		case FLOAT:
			storedValue = Float.valueOf(service.getFloat(key.getQualifier(), key.getKey(), IPreferenceStore.FLOAT_DEFAULT_DEFAULT, contexts));
			break;
		default:
			storedValue = service.getString(key.getQualifier(), key.getKey(), IPreferenceStore.STRING_DEFAULT_DEFAULT, contexts);
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
	

//-- ICombinedPreferenceStore -----------------------------------------------//
	
	public static ICombinedPreferenceStore createCombindedPreferenceStore(
			IPreferenceStore[] preferenceStores, IPreferenceAccess corePrefs, String[] coreQualifier) {
		
		
		IScopeContext[] contexts = corePrefs.getPreferenceContexts();
		// default scope must not be included (will be automatically added)
		if (contexts.length > 0 && contexts[contexts.length-1] instanceof DefaultScope) {
			IScopeContext[] newContexts = new IScopeContext[contexts.length-1];
			System.arraycopy(contexts, 0, newContexts, 0, contexts.length-1);
			contexts = newContexts;
		}
		IScopeContext mainScope = (contexts.length > 0) ? contexts[0] : new InstanceScope();

		if (preferenceStores.length == 0 && contexts.length <= 1 && coreQualifier.length == 1) {
			return new ScopedCombinedStore(mainScope, coreQualifier[0], corePrefs);
		}
		
		List<IPreferenceStore> stores = new ArrayList<IPreferenceStore>(Arrays.asList(preferenceStores));
		for (String qualifier : coreQualifier) {
			ScopedPreferenceStore store = new ScopedPreferenceStore(mainScope, qualifier);
			store.setSearchContexts(contexts);
			stores.add(store);
		}
		return new ChainedCombinedStore(stores.toArray(new IPreferenceStore[stores.size()]), corePrefs);
	}
	
	
	public static ICombinedPreferenceStore createCombindedPreferenceStore(
			IPreferenceAccess corePrefs, String coreQualifier) {
		
		return createCombindedPreferenceStore(new IPreferenceStore[0], corePrefs, new String[] { coreQualifier });
	}
	
	private static class ChainedCombinedStore extends ChainedPreferenceStore implements ICombinedPreferenceStore {
		
		private IPreferenceAccess fCorePrefs;
		
		ChainedCombinedStore(IPreferenceStore[] stores, IPreferenceAccess core) {
			
			super(stores);
			fCorePrefs = core;
		}
	
		public IPreferenceAccess getCorePreferences() {
			
			return fCorePrefs;
		}
	}
	
	private static class ScopedCombinedStore extends ScopedPreferenceStore implements ICombinedPreferenceStore {
		
		private IPreferenceAccess fCorePrefs;
		
		ScopedCombinedStore(IScopeContext context, String qualifier, IPreferenceAccess core) {
			
			super(context, qualifier);
			fCorePrefs = core;
		}
	
		public IPreferenceAccess getCorePreferences() {
			
			return fCorePrefs;
		}
	}
}
