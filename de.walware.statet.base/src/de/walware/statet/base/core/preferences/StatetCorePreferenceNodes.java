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

package de.walware.statet.base.core.preferences;

import org.eclipse.core.runtime.preferences.DefaultScope;


public class StatetCorePreferenceNodes {

	
	private static final String PLUGIN_QUALIFIER = "de.walware.statet.base";
	public static final String CORE_QUALIFIER = PLUGIN_QUALIFIER + "/core";
	
	public static final String CAT_MANAGMENT_QUALIFIER = CORE_QUALIFIER + "/managment";
	
	/**
	 * Initializes the default values.
	 */
	public static void initializeDefaultValues(DefaultScope scope) {

		TaskTagsPreferences.setDefaultValues(scope);
	}
	
}
