/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import de.walware.eclipsecommons.preferences.PreferencesUtil;

import de.walware.statet.base.core.preferences.PreferenceManageListener;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;


/**
 * The main plugin class to be used in the desktop.
 */
public class RCorePlugin extends Plugin {

	
	//The shared instance.
	private static RCorePlugin gPlugin;
	
	/**
	 * Returns the shared instance.
	 */
	public static RCorePlugin getDefault() {
		return gPlugin;
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	
	private class WorkspaceCoreAccess implements IRCoreAccess {
		
		private RCodeStyleSettings fRCodeStyle;
		
		private WorkspaceCoreAccess() {
			fRCodeStyle = new RCodeStyleSettings();
			new PreferenceManageListener(fRCodeStyle, PreferencesUtil.getInstancePrefs(), RCodeStyleSettings.CONTEXT_ID);
		}
		
		public RCodeStyleSettings getRCodeStyle() {
			return fRCodeStyle;
		};
	};

	
	private IRCoreAccess fWorkspaceCoreAccess;
	
	
	/**
	 * The constructor.
	 */
	public RCorePlugin() {
		gPlugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		gPlugin = null;
	}

	public IRCoreAccess getWorkspaceRCoreAccess() {
		if (fWorkspaceCoreAccess == null) {
			synchronized (this) {
				if (fWorkspaceCoreAccess == null) {
					fWorkspaceCoreAccess = new WorkspaceCoreAccess();
				}
			}
		}
		return fWorkspaceCoreAccess;
	}
}
