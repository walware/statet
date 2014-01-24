/*=============================================================================#
 # Copyright (c) 2006-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.core;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.PreferencesUtil;

import de.walware.statet.nico.internal.core.NicoPlugin;


public class NicoCore {
	
	/**
	 * Plugin-ID
	 * Value: @value
	 */
	public static final String PLUGIN_ID = "de.walware.statet.nico.core"; //$NON-NLS-1$
	
	public static final int STATUS_CATEGORY = (3 << 16);
	
	/** Status Code for errors when handle Threads/Runnables */
	public static final int STATUSCODE_RUNTIME_ERROR = STATUS_CATEGORY | (2 << 8);
	
	public static final int EXITVALUE_CORE_EXCEPTION = STATUSCODE_RUNTIME_ERROR | 1;
	public static final int EXITVALUE_RUNTIME_EXCEPTION = STATUSCODE_RUNTIME_ERROR | 2;
	
	
	private static IPreferenceAccess CONSOLE_PREFS = PreferencesUtil.createAccess(new IScopeContext[] {
			new ConsoleInstanceScope(), new ConsoleDefaultScope(), new InstanceScope(), new DefaultScope() });
	
	/**
	 * The instance preferences for consoles with the scope search path:
	 * <ol>
	 *     <li>ConsoleInstance</li>
	 *     <li>ConsoleDefault</li>
	 *     <li>Instance</li>
	 *     <li>Default</li>
	 * @return shared preference access to the preferences
	 */
	public static IPreferenceAccess getInstanceConsolePreferences() {
		return CONSOLE_PREFS;
	}
	
	public static void addToolLifeListener(final IToolLifeListener listener) {
		NicoPlugin.getDefault().getToolLifecycle().addToolLifeListener(listener);
	}
	
	public static void removeToolLifeListener(final IToolLifeListener listener) {
		NicoPlugin.getDefault().getToolLifecycle().removeToolLifeListener(listener);
	}
	
	
	private NicoCore() {}
	
}
