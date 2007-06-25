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

package de.walware.statet.r.core;

import de.walware.statet.r.core.renv.IREnvManager;
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 * Provides r core services.
 */
public class RCore {

	
	public static final String PLUGIN_ID = "de.walware.statet.r.core"; //$NON-NLS-1$


	/**
	 * Usually used, if no other context (e.g. project) specified.
	 */
	public static IRCoreAccess getWorkbenchAccess() {
		return RCorePlugin.getDefault().getWorkspaceRCoreAccess();
	}
	
	/**
	 * Usually only used in special cases like preference dialogs.
	 */
	public static IRCoreAccess getDefaultsAccess() {
		return RCorePlugin.getDefault().getDefaultsRCoreAccess();
	}
	
	/**
	 * Returns the manager with shared configurations.
	 */
	public static IREnvManager getREnvManager() {
		return RCorePlugin.getDefault().getREnvManager();
	}
	
}
