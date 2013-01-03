/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.lauching;

import de.walware.statet.r.internal.console.ui.launching.RRemoteConsoleLaunchDelegate;


public class RRemoteConsoleDebugLaunchDelegate extends RRemoteConsoleLaunchDelegate {
	
	
	public RRemoteConsoleDebugLaunchDelegate() {
		super(new DebugLaunchDelegateAddon());
	}
	
	
}
