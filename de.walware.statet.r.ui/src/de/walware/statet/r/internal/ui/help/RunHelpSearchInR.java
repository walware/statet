/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.help;


/**
 * Command handler for help.search("...")
 */
public class RunHelpSearchInR extends RunHelpHandler {

	
	public static final String COMMAND_ID = "de.walware.statet.r.ui.commands.RunHelpSearchInR"; //$NON-NLS-1$
	
	
	public RunHelpSearchInR() {
		super("help.search", true); //$NON-NLS-1$
	}
	
}
