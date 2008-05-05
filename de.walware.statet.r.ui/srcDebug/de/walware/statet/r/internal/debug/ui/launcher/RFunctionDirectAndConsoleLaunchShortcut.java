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

package de.walware.statet.r.internal.debug.ui.launcher;


/**
 * Launch shortcut, which submits the lowest enclosing function (assign of fdef)
 * and goes to the console.
 * 
 * Supports only text editors.
 */
public class RFunctionDirectAndConsoleLaunchShortcut extends
		RFunctionDirectLaunchShortcut {
	
	
	public RFunctionDirectAndConsoleLaunchShortcut() {
		fGotoConsole = true;
	}
	
}
