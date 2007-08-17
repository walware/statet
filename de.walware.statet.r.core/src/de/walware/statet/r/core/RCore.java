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

import org.eclipse.core.resources.IFile;

import de.walware.eclipsecommons.ltk.IElementChangedListener;
import de.walware.eclipsecommons.ltk.WorkingContext;

import de.walware.statet.r.core.renv.IREnvManager;
import de.walware.statet.r.core.rmodel.IRSourceUnit;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.rmodel.RSourceUnit;


/**
 * Provides r core services.
 */
public class RCore {

	
	public static final String PLUGIN_ID = "de.walware.statet.r.core"; //$NON-NLS-1$

	public static final WorkingContext PERSISTENCE_CONTEXT = RCorePlugin.getDefault().createContext(0);
	public static final WorkingContext PRIMARY_WORKING_CONTEXT = RCorePlugin.getDefault().createContext(1);


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
	
	public static void addRElementChangedListener(IElementChangedListener listener, WorkingContext context) {
		RCorePlugin.getDefault().getRModelManager().addElementChangedListener(listener, context);
	}
	
	public static void removeRElementChangedListener(IElementChangedListener listener, WorkingContext context) {
		RCorePlugin.getDefault().getRModelManager().removeElementChangedListener(listener, context);
	}
	
	public static IRSourceUnit getUnit(IFile file) {
		String id = RSourceUnit.createResourceId(file);
		if (id == null) {
			return null;
		}
		synchronized (RCore.PERSISTENCE_CONTEXT) {
			IRSourceUnit u = RCorePlugin.getDefault().getRModelManager().getWorkingCopy(id, RCore.PERSISTENCE_CONTEXT);
			if (u == null) {
				u = new RSourceUnit(file);
			}
			u.connect();
			return u;
		}
	}

}
