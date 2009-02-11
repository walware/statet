/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launcher;

import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;


public class RunSelectionAndGotoConsoleHandler extends RunSelectionHandler {
	
	
	public RunSelectionAndGotoConsoleHandler() {
		super(true);
	}
	
	
	@Override
	protected String appendVariant(final String label) {
		return label + RLaunchingMessages.RunCode_GotoConsole_affix;
	}
	
}
