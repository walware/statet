/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core;

import de.walware.eclipsecommons.preferences.PreferencesUtil;


/**
 *
 */
public class RCore {

	
	public static final String PLUGIN_ID = "de.walware.statet.r.core";

	
	private static IRCoreAccess WORKSPACE_ACCESS = new IRCoreAccess() {
		public RCodeStyleSettings getRCodeStyle() {
			return new RCodeStyleSettings(PreferencesUtil.getInstancePrefs());
		};
	};
	public static IRCoreAccess getWorkbenchAccess(){
		
		return WORKSPACE_ACCESS;
	}
}
