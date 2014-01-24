/*=============================================================================#
 # Copyright (c) 2007-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.rtools;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.common.NotDefinedException;

import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.launching.AbstractRCommandHandler;


/**
 * Command handler for help("...")
 */
public class RunHelpInR extends AbstractRCommandHandler {
	
	
	public static final String COMMAND_ID = "de.walware.statet.r.ui.commands.RunHelpInR"; //$NON-NLS-1$
	private static final String PAR_TOPIC = "topic"; //$NON-NLS-1$
	
	
	public static String createCommandString(final String topic) throws NotDefinedException {
		return createCommandString(COMMAND_ID, new String[][] {{ PAR_TOPIC, topic }});
	}
	
	
	public RunHelpInR() {
		super(Messages.HelpCommand_name);
	}
	
	
	@Override
	public Object execute(final ExecutionEvent arg) throws ExecutionException {
		String topic = arg.getParameter(PAR_TOPIC);
		if (topic == null) {
			topic = getRSelection();
			if (topic == null) {
				return null;
			}
		}
		runCommand("help(\""+RUtil.escapeDoubleQuote(topic)+"\")", false); //$NON-NLS-1$ //$NON-NLS-2$
		return null;
	}
	
}
