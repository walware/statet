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

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;


/**
 * Prefence Store with core support.
 * <p>
 * So you can access core preference which use Preference/IPreferenceAccess
 * in UI environment (where usually PreferenceStores are used).
 * 
 */
public class CombinedPreferenceStore extends ChainedPreferenceStore {

	
	private IPreferenceAccess fCorePrefs;
	
	
	public CombinedPreferenceStore(IPreferenceStore[] preferenceStores, IPreferenceAccess corePrefs, String[] coreQualifier) {
		
		super(getStores(preferenceStores, corePrefs, coreQualifier));
		fCorePrefs = corePrefs;
	}

	public IPreferenceAccess getCorePreferences() {
		
		return fCorePrefs;
	}


	// TODO: related to eclipse bug #123903. 
	// if not resolved, we have to create multiple ScopedPreferenceStore (more than one => dummy default context) or implement our own scoped store.
	private static IPreferenceStore[] getStores(IPreferenceStore[] classicStores, IPreferenceAccess corePrefs, String[] coreQualifier) {
		
		List<IPreferenceStore> stores = new ArrayList<IPreferenceStore>(Arrays.asList(classicStores));
	
		IScopeContext[] contexts = corePrefs.getPreferenceContexts();
		// default scope must not be included (will be automatically added)
		if (contexts.length > 0 && contexts[contexts.length-1] instanceof DefaultScope) {
			IScopeContext[] newContexts = new IScopeContext[contexts.length-1];
			System.arraycopy(contexts, 0, newContexts, 0, contexts.length-1);
			contexts = newContexts;
		}
		IScopeContext mainScope = (contexts.length > 0) ? contexts[0] : new InstanceScope();

		for (String qualifier : coreQualifier) {
			ScopedPreferenceStore store = new ScopedPreferenceStore(mainScope, qualifier);
			store.setSearchContexts(contexts);
			stores.add(store);
		}
		
		return stores.toArray(new IPreferenceStore[stores.size()]);
	}
}
