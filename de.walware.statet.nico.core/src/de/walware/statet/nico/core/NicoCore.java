/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.PreferencesUtil;


/**
 * 
 */
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
	
	public static IPreferenceAccess getDefaultConsolePreferences() {
		return CONSOLE_PREFS;
	}
	
}
