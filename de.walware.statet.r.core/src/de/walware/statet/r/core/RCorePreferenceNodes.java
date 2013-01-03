/*******************************************************************************
 * Copyright (c) 2007-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core;

import de.walware.ecommons.preferences.Preference.BooleanPref;


/**
 * Preference Nodes for the preferences of 'StatET R Core' plug-in
 */
public class RCorePreferenceNodes {
	
	
	public static final String CAT_R_CODESTYLE_QUALIFIER = RCore.PLUGIN_ID + "/codestyle/r"; //$NON-NLS-1$
	
	public static final String CAT_R_ENVIRONMENTS_QUALIFIER = RCore.PLUGIN_ID + "/r.environments"; //$NON-NLS-1$
	
	public static final BooleanPref PREF_RENV_NETWORK_USE_ECLIPSE = new BooleanPref(CAT_R_ENVIRONMENTS_QUALIFIER, "network.use_eclipse"); //$NON-NLS-1$
	
}
