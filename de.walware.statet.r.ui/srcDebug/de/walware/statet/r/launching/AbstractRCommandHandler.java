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

package de.walware.statet.r.launching;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.util.ExceptionHandler;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.internal.ui.help.EnrichedRHelpContext;


/**
 *
 */
public abstract class AbstractRCommandHandler extends AbstractHandler {

	public static String createCommandString(String commandId, String[][] parameters) throws NotDefinedException {
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command base = service.getCommand(commandId);
		if (base == null) {
			throw new NotDefinedException("No command registered with the requested id: " + commandId); //$NON-NLS-1$
		}
		Parameterization[] par = new Parameterization[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			par[i] = new Parameterization(base.getParameter(parameters[i][0]), parameters[i][1]);
		}
		ParameterizedCommand configured = new ParameterizedCommand(base, par); 
		return configured.serialize();
	}

	
	private String fName;
	
	
	protected AbstractRCommandHandler(String commandName) {
		fName = commandName;
	}
	
	protected String getRSelection() {
		final AtomicReference<String> topic = new AtomicReference<String>();
		UIAccess.getDisplay().syncExec(new Runnable() {
			public void run() {
				topic.set(EnrichedRHelpContext.searchContextInfo(UIAccess.getActiveWorkbenchPage(true).getActivePart()));
				if (topic.get() == null) {
					Display.getCurrent().beep();
				}
			}
		});
		return topic.get();
	}
	
	protected void runCommand(String cmd, boolean gotoConsole) {
		try {
			RCodeLaunchRegistry.runRCodeDirect(new String[] { cmd }, gotoConsole);
		} catch (CoreException e) {
			ExceptionHandler.handle(e, NLS.bind(RLaunchingMessages.RSpecifiedLaunch_error_message, fName));
		}
	}
	
}
