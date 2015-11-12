/*=============================================================================#
 # Copyright (c) 2006-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

import de.walware.statet.r.core.pkgmanager.IRPkgManager;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvManager;
import de.walware.statet.r.core.rhelp.IRHelpManager;
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 * Provides r core services.
 */
public class RCore {
	
	
	public static final String PLUGIN_ID= "de.walware.statet.r.core"; //$NON-NLS-1$
	
	
	/**
	 * Content type id for R sources
	 */
	public static final String R_CONTENT_ID= "de.walware.statet.r.contentTypes.R"; //$NON-NLS-1$
	
	public static final IContentType R_CONTENT_TYPE;
	
	/**
	 * Content type id for Rd sources
	 */
	public static final String RD_CONTENT_ID= "de.walware.statet.r.contentTypes.Rd"; //$NON-NLS-1$
	
	public static final IContentType RD_CONTENT_TYPE;
	
	static {
		final IContentTypeManager contentTypeManager= Platform.getContentTypeManager();
		R_CONTENT_TYPE= contentTypeManager.getContentType(R_CONTENT_ID);
		RD_CONTENT_TYPE= contentTypeManager.getContentType(RD_CONTENT_ID);
	}
	
	
	public static final IRCoreAccess WORKBENCH_ACCESS= RCorePlugin.getDefault().getWorkspaceRCoreAccess();
	
	
	/**
	 * Usually used, if no other context (e.g. project) specified.
	 */
	public static IRCoreAccess getWorkbenchAccess() {
		return WORKBENCH_ACCESS;
	}
	
	/**
	 * Usually only used in special cases like preference dialogs.
	 */
	public static IRCoreAccess getDefaultsAccess() {
		return RCorePlugin.getDefault().getDefaultsRCoreAccess();
	}
	
	/**
	 * @return the manager with with shared configurations of the R environments.
	 */
	public static IREnvManager getREnvManager() {
		return RCorePlugin.getDefault().getREnvManager();
	}
	
	public static IRPkgManager getRPkgManager(final IREnv env) {
		return RCorePlugin.getDefault().getREnvPkgManager().getManager(env);
	}
	
	public static IRHelpManager getRHelpManager() {
		return RCorePlugin.getDefault().getRHelpManager();
	}
	
}
