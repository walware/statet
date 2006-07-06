/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.preferences;

import de.walware.eclipsecommons.preferences.Preference.Type;


public class PreferenceKey {


	public final String fKey;
	public final Type fType;

	
	public PreferenceKey(String key, Type type) {

		fKey = key;
		fType = type;
	}

}
