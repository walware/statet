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

package de.walware.statet.r.launching;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.internal.debug.ui.launcher.LaunchShortcutUtil;
import de.walware.statet.r.internal.debug.ui.launcher.RunFileViaCommandHandler;


/**
 * Launch Shortcut for {@link RunFileViaCommandHandler}
 */
public class RunFileViaCommandLaunchShortcut implements ILaunchShortcut {
	
	
	private boolean fGotoConsole;
	private String fFileCommandId;
	
	
	public RunFileViaCommandLaunchShortcut(final String fileCommandId, final boolean gotoConsole) {
		fFileCommandId = fileCommandId;
		fGotoConsole = gotoConsole;
	}
	
	
	public void launch(final ISelection selection, final String mode) {
		assert mode.equals("run"); //$NON-NLS-1$
		
		final IWorkbenchPart workbenchPart = UIAccess.getActiveWorkbenchPart(false);
		final IWorkbenchPartSite site = workbenchPart.getSite();
		if (site != null) {
			final IHandlerService handlerService = (IHandlerService) site.getService(IHandlerService.class);
			final ICommandService commandService = (ICommandService) site.getService(ICommandService.class);
			
			final Map<String, String> parameters = new HashMap<String, String>();
			parameters.put(RCodeLaunching.FILE_COMMAND_ID_PARAMTER_ID, fFileCommandId);
			final Command command = commandService.getCommand(!fGotoConsole ?
					RCodeLaunching.RUN_FILEVIACOMMAND_COMMAND_ID : RCodeLaunching.RUN_FILEVIACOMMAND_GOTOCONSOLE_COMMAND_ID);
			final ExecutionEvent executionEvent = new ExecutionEvent(command, parameters, null, handlerService.getCurrentState());
			if (!selection.equals(HandlerUtil.getCurrentSelection(executionEvent))) {
				LaunchShortcutUtil.handleUnsupportedExecution(executionEvent);
				return;
			}
			try {
				command.executeWithChecks(executionEvent);
			}
			catch (final ExecutionException e) {
				LaunchShortcutUtil.handleRLaunchException(e, RLaunchingMessages.RScriptLaunch_error_message, executionEvent); 
			}
			catch (final NotDefinedException e) {
				LaunchShortcutUtil.handleUnsupportedExecution(executionEvent);
			}
			catch (final NotEnabledException e) {
				LaunchShortcutUtil.handleUnsupportedExecution(executionEvent);
			}
			catch (final NotHandledException e) {
				LaunchShortcutUtil.handleUnsupportedExecution(executionEvent);
			}
		}
	}
	
	public void launch(final IEditorPart editor, final String mode) {
		assert mode.equals("run"); //$NON-NLS-1$
		
		final IWorkbenchPart workbenchPart = UIAccess.getActiveWorkbenchPart(false);
		final IWorkbenchPartSite site = workbenchPart.getSite();
		if (site != null) {
			final IHandlerService handlerService = (IHandlerService) site.getService(IHandlerService.class);
			final ICommandService commandService = (ICommandService) site.getService(ICommandService.class);
			
			final Map<String, String> parameters = new HashMap<String, String>();
			parameters.put(RCodeLaunching.FILE_COMMAND_ID_PARAMTER_ID, fFileCommandId);
			final Command command = commandService.getCommand(!fGotoConsole ?
					RCodeLaunching.RUN_FILEVIACOMMAND_COMMAND_ID : RCodeLaunching.RUN_FILEVIACOMMAND_GOTOCONSOLE_COMMAND_ID);
			final ExecutionEvent executionEvent = new ExecutionEvent(command, parameters, null, handlerService.getCurrentState());
			if (!editor.equals(HandlerUtil.getActivePart(executionEvent))) {
				LaunchShortcutUtil.handleUnsupportedExecution(executionEvent);
				return;
			}
			try {
				command.executeWithChecks(executionEvent);
			}
			catch (final ExecutionException e) {
				LaunchShortcutUtil.handleRLaunchException(e, RLaunchingMessages.RScriptLaunch_error_message, executionEvent); 
			}
			catch (final NotDefinedException e) {
				LaunchShortcutUtil.handleUnsupportedExecution(executionEvent);
			}
			catch (final NotEnabledException e) {
				LaunchShortcutUtil.handleUnsupportedExecution(executionEvent);
			}
			catch (final NotHandledException e) {
				LaunchShortcutUtil.handleUnsupportedExecution(executionEvent);
			}
		}
	}
	
}
