/*******************************************************************************
 * Copyright (c) 2005-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launcher;

import de.walware.statet.r.launching.SubmitFileViaCommandLaunchShortcut;


/**
 * Launch shortcut, which submits the whole script (file)
 * using the <code>source</code> command to R
 * and does not change the focus.
 */
public class SubmitRScriptViaSourceShortcut extends SubmitFileViaCommandLaunchShortcut {
	
	
	public SubmitRScriptViaSourceShortcut() {
		this(false);
	}
	
	protected SubmitRScriptViaSourceShortcut(final boolean gotoConsole) {
		super("de.walware.statet.r.rFileCommand.SourceRScript", gotoConsole); //$NON-NLS-1$
	}
	
}