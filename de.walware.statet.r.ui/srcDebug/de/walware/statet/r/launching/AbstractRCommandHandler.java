/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.launching;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.internal.ui.help.EnrichedRHelpContext;
import de.walware.statet.r.ui.RUI;


/**
 * Abstract handler to submit a simple command to R.
 */
public abstract class AbstractRCommandHandler extends AbstractHandler {
	
	
	public static String createCommandString(final String commandId, final String[][] parameters) throws NotDefinedException {
		final ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		final Command base = service.getCommand(commandId);
		if (base == null) {
			throw new NotDefinedException("No command registered with the requested id: " + commandId); //$NON-NLS-1$
		}
		final Parameterization[] par = new Parameterization[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			par[i] = new Parameterization(base.getParameter(parameters[i][0]), parameters[i][1]);
		}
		final ParameterizedCommand configured = new ParameterizedCommand(base, par);
		return configured.serialize();
	}
	
	
	private final String fName;
	
	
	protected AbstractRCommandHandler(final String commandName) {
		fName = commandName;
	}
	
	
	protected String getRSelection() {
		final AtomicReference<String> topic= new AtomicReference<>();
		UIAccess.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				topic.set(EnrichedRHelpContext.searchContextInfo(UIAccess.getActiveWorkbenchPage(true).getActivePart()));
				if (topic.get() == null) {
					Display.getCurrent().beep();
				}
			}
		});
		return topic.get();
	}
	
	protected void runCommand(final String cmd, final boolean gotoConsole) {
		try {
			RCodeLaunching.runRCodeDirect(Collections.singletonList(cmd), gotoConsole, null);
		}
		catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(Status.ERROR, RUI.PLUGIN_ID,
					-1, NLS.bind(RLaunchingMessages.RSpecifiedLaunch_error_message, fName), e),
					StatusManager.LOG | StatusManager.SHOW);
		}
	}
	
}
