/*=============================================================================#
 # Copyright (c) 2011-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.launcher;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.ui.actions.TogglePreferenceEnablementHandler;

import de.walware.statet.r.launching.RCodeLaunching;


public class ToggleRunEchoHandler extends TogglePreferenceEnablementHandler {
	
	
	/** Created via extension point */
	public ToggleRunEchoHandler() {
		super(RCodeLaunching.ECHO_ENABLED_PREF, PreferencesUtil.getInstancePrefs(),
				LaunchShortcutUtil.TOGGLE_ECHO_COMMAND_ID );
	}
	
	
}
