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

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.util.ExceptionHandler;
import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.launching.RCodeLaunchRegistry;


/**
 * Abstract command handler submitting help commands to R.
 */
public abstract class RunHelpHandler extends AbstractHandler {

	
	private static final String PAR_TOPIC = "topic"; //$NON-NLS-1$
	
	public static String createCommandString(String commandId, String text) throws NotDefinedException {
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command base = service.getCommand(commandId);
		if (base == null) {
			throw new NotDefinedException("No command registered with the requested id: " + commandId);
		}
		ParameterizedCommand configured = new ParameterizedCommand(base, new Parameterization[] {
				new Parameterization(base.getParameter(PAR_TOPIC), text),
		});
		return configured.serialize();
	}

	
	private String fFunction;
	private boolean fGotoConsole;
	
	
	protected RunHelpHandler(String function, boolean gotoConsole) {
		fFunction = function;
		fGotoConsole = gotoConsole;
	}
	
	@Override
	public Object execute(final ExecutionEvent arg) throws ExecutionException {
		final AtomicReference<String> topic = new AtomicReference<String>(arg.getParameter(PAR_TOPIC));
		if (topic.get() == null) {
			UIAccess.getDisplay().syncExec(new Runnable() {
				public void run() {
					topic.set(EnrichedRHelpContext.searchContextInfo(UIAccess.getActiveWorkbenchPage(true).getActivePart()));
					if (topic.get() == null) {
						Display.getCurrent().beep();
					}
				}
			});
			if (topic.get() == null) {
				return null;
			}
		}
		String command = fFunction + "(\""+RUtil.escapeDoubleQuote(topic.get())+"\")"; //$NON-NLS-1$ //$NON-NLS-2$
		try {
			RCodeLaunchRegistry.runRCodeDirect(new String[] { command }, fGotoConsole);
		} catch (CoreException e) {
			ExceptionHandler.handle(e, Messages.RHelp_Run_Help_error_message);
		}
		return null;
	}
	
}
