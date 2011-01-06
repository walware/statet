/*******************************************************************************
 * Copyright (c) 2005-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui;

import java.rmi.registry.Registry;

import org.eclipse.core.runtime.preferences.IScopeContext;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.Preference.BooleanPref;
import de.walware.ecommons.preferences.Preference.IntPref;
import de.walware.ecommons.preferences.Preference.StringPref;

import de.walware.statet.r.internal.nico.ui.RControllerCodeLaunchConnector;


public class RDebugPreferenceConstants {
	
	
	public static final String ROOT_QUALIFIER = "de.walware.statet.r.ui"; // 'ui', because at moment in 'ui' plugin //$NON-NLS-1$
	
	public static final String CAT_CODELAUNCH_CONTENTHANDLER_QUALIFIER = ROOT_QUALIFIER + "/CodeLaunchContentHandler"; //$NON-NLS-1$
	
	public static final String CAT_RCONNECTOR_QUALIFIER = ROOT_QUALIFIER + "/RConnector"; //$NON-NLS-1$
	public static final StringPref PREF_R_CONNECTOR = new StringPref(CAT_RCONNECTOR_QUALIFIER, "rconnector.id"); //$NON-NLS-1$
	
	public static final String CAT_RREMOTE_LAUNCHING_QUALIFIER = ROOT_QUALIFIER + "/r.remote.launching"; //$NON-NLS-1$
	
	public static final String CAT_RMI_QUALIFIER = ROOT_QUALIFIER + "/rmi"; //$NON-NLS-1$
	public static final BooleanPref PREF_LOCAL_REGISTRY_AUTOSTART_ENABLED = new BooleanPref(CAT_RMI_QUALIFIER, "LocalRegistryAutostart.enabled"); //$NON-NLS-1$
	public static final IntPref PREF_LOCAL_REGISTRY_PORT = new IntPref(CAT_RMI_QUALIFIER, "LocalRegistry.port"); //$NON-NLS-1$
	
	public static final String CAT_RENV_QUALIFIER = ROOT_QUALIFIER + "/renv"; //$NON-NLS-1$
	public static final StringPref PREF_RENV_CHECK_UPDATE = new StringPref(CAT_RENV_QUALIFIER, "index.console_update.mode"); //$NON-NLS-1$
	
	/** Run update automatically */
	public static final String AUTO = "auto"; //$NON-NLS-1$
	/** Ask if run update */
	public static final String ASK = "ask"; //$NON-NLS-1$
	/** Do not run update */
	public static final String DISABLED = "no"; //$NON-NLS-1$
	
	
	/**
	 * Initializes the default values.
	 */
	public static void initializeDefaultValues(final IScopeContext context) {
		PreferencesUtil.setPrefValue(context, RDebugPreferenceConstants.PREF_R_CONNECTOR, RControllerCodeLaunchConnector.ID);
		
		PreferencesUtil.setPrefValue(context, PREF_LOCAL_REGISTRY_AUTOSTART_ENABLED, true);
		PreferencesUtil.setPrefValue(context, PREF_LOCAL_REGISTRY_PORT, Registry.REGISTRY_PORT);
		PreferencesUtil.setPrefValue(context, PREF_RENV_CHECK_UPDATE, ASK);
	}
	
}
