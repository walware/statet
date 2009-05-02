/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.statet.r.launching.AbstractRCommandHandler;


/**
 * Command handler for print(...)
 */
public class RunPrintInR extends AbstractRCommandHandler {
	
	
	public static final String COMMAND_ID = "de.walware.statet.r.ui.commands.RunPrintInR"; //$NON-NLS-1$
	private static final String PAR_VAR = "var"; //$NON-NLS-1$
	
	
	public static String createCommandString(final String var) throws NotDefinedException {
		return createCommandString(COMMAND_ID, new String[][] {{ PAR_VAR, var }});
	}
	
	
	public RunPrintInR() {
		super(Messages.PrintCommand_name);
	}
	
	
	public Object execute(final ExecutionEvent arg) throws ExecutionException {
		String var = arg.getParameter(PAR_VAR);
		if (var == null) {
			var = getRSelection();
			if (var == null) {
				return null;
			}
		}
//		"print(\""+RUtil.escapeDoubleQuote(var)+"\")"
		runCommand(var, false);
		return null;
	}
	
}
