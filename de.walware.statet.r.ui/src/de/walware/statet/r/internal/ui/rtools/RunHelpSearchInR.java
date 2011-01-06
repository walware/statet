/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.rtools;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.common.NotDefinedException;

import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.launching.AbstractRCommandHandler;


/**
 * Command handler for help.search("...")
 */
public class RunHelpSearchInR extends AbstractRCommandHandler {
	
	
	public static final String COMMAND_ID = "de.walware.statet.r.ui.commands.RunHelpSearchInR"; //$NON-NLS-1$
	private static final String PAR_TEXT = "text"; //$NON-NLS-1$
	
	
	public static String createCommandString(final String text) throws NotDefinedException {
		return createCommandString(COMMAND_ID, new String[][] {{ PAR_TEXT, text }});
	}
	
	
	public RunHelpSearchInR() {
		super(Messages.HelpCommand_name);
	}
	
	
	public Object execute(final ExecutionEvent arg) throws ExecutionException {
		String text = arg.getParameter(PAR_TEXT);
		if (text == null) {
			text = getRSelection();
			if (text == null) {
				return null;
			}
		}
		runCommand("help.search(\""+RUtil.escapeDoubleQuote(text)+"\")", false); //$NON-NLS-1$ //$NON-NLS-2$
		return null;
	}
	
}
