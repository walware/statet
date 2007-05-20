/*******************************************************************************
 * Copyright (c) 2006-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
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
 * Util to create combination of preference stores.
 */
public class CombinedPreferenceStore {

	public static IPreferenceStore createStore(
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
			return new ScopedPreferenceStore(mainScope, coreQualifier[0]);
		}
		
		List<IPreferenceStore> stores = new ArrayList<IPreferenceStore>();
		for (String qualifier : coreQualifier) {
			ScopedPreferenceStore store = new ScopedPreferenceStore(mainScope, qualifier);
			store.setSearchContexts(contexts);
			stores.add(store);
		}
		stores.addAll(Arrays.asList(preferenceStores));
		return new ChainedPreferenceStore(stores.toArray(new IPreferenceStore[stores.size()]));
	}
	
	public static IPreferenceStore createStore(IPreferenceAccess corePrefs, String coreQualifier) {
		return createStore(new IPreferenceStore[0], corePrefs, new String[] { coreQualifier });
	}
	
}
