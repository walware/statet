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

package de.walware.statet.r.internal.debug.ui.launcher;


/**
 * Launch shortcut, which submits the whole script (file)
 * using the <code>source</code> command to R
 * and goes to the console.
 */
public class RScriptViaSourceAndConsoleLaunchShortcut extends
		RScriptViaSourceLaunchShortcut {
	
	
	public RScriptViaSourceAndConsoleLaunchShortcut() {
		
		fGotoConsole = true;
	}
}
