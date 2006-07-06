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

package de.walware.eclipsecommons.ui.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import de.walware.eclipsecommons.preferences.IPreferenceAccess;


/**
 *
 */
public class CombinedPreferenceStore {

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

	
	public static ICombinedPreferenceStore createStore(
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
	
	public static ICombinedPreferenceStore createStore(
			IPreferenceAccess corePrefs, String coreQualifier) {
		
		return createStore(new IPreferenceStore[0], corePrefs, new String[] { coreQualifier });
	}
	
}
