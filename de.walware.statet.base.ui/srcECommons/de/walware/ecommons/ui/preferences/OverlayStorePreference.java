/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.preferences;

import de.walware.ecommons.preferences.Preference;


public class OverlayStorePreference {
	
	
	public static OverlayStorePreference create(final Preference pref) {
		return new OverlayStorePreference(pref.getKey(), pref.getStoreType());
	}
	
	
	public final String fKey;
	public final Preference.Type fType;
	
	
	public OverlayStorePreference(final String key, final Preference.Type type) {
		fKey = key;
		fType = type;
	}
	
}
