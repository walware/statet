/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launcher;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.menus.UIElement;

import de.walware.eclipsecommons.ui.util.WorkbenchUIUtil;

import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.RCodeLaunching;


/**
 * 
 */
public class RunFileViaCommandHandler extends AbstractHandler implements IElementUpdater {
	
	
	private boolean fGotoConsole;
	
	
	public RunFileViaCommandHandler() {
		this(false);
	}
	
	public RunFileViaCommandHandler(final boolean gotoConsole) {
		fGotoConsole = gotoConsole;
	}
	
	
	public void updateElement(final UIElement element, final Map parameters) {
		// TODO
	}
	
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final String fileCommandId = event.getParameter(RCodeLaunching.FILE_COMMAND_ID_PARAMTER_ID);
		try {
			final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
			if (activePart instanceof IEditorPart) {
				final IEditorPart editor = (IEditorPart) activePart;
				final IEditorInput input = editor.getEditorInput();
				final IFile file = ResourceUtil.getFile(input);
				if (file != null) {
					final String command = (fileCommandId != null) ?
							RCodeLaunching.getFileCommand(fileCommandId) :
							RCodeLaunching.getPreferredFileCommand(LaunchShortcutUtil.getContentTypeId(file));
					RCodeLaunching.runFileUsingCommand(command, file, fGotoConsole);
					return null;
				}
				else if (input instanceof IPathEditorInput) {
					final IPath path = ((IPathEditorInput) input).getPath();
					final String command = (fileCommandId != null) ?
							RCodeLaunching.getFileCommand(fileCommandId) :
							RCodeLaunching.getPreferredFileCommand(LaunchShortcutUtil.getContentTypeId(path));
					RCodeLaunching.runFileUsingCommand(command, path, fGotoConsole);
					return null;
				}
				else if (input instanceof IURIEditorInput) {
					final URI uri = ((IURIEditorInput) input).getURI();
					final String command = (fileCommandId != null) ?
							RCodeLaunching.getFileCommand(fileCommandId) :
							RCodeLaunching.getPreferredFileCommand(LaunchShortcutUtil.getContentTypeId(uri));
					RCodeLaunching.runFileUsingCommand(command, uri, fGotoConsole);
					return null;
				}
			}
			final ISelection selection = WorkbenchUIUtil.getCurrentSelection(event.getApplicationContext());
			if (selection instanceof IStructuredSelection) {
				final IStructuredSelection sel = (IStructuredSelection) selection;
				if (sel.size() == 1) {
					final Object object = sel.getFirstElement();
					IFile file = null;
					if (object instanceof IFile) {
						file = (IFile) object;
					}
					else if (object instanceof IAdaptable) {
						file = (IFile) ((IAdaptable) object).getAdapter(IFile.class);
					}
					if (file != null) {
						final String command = (fileCommandId != null) ?
								RCodeLaunching.getFileCommand(fileCommandId) :
								RCodeLaunching.getPreferredFileCommand(LaunchShortcutUtil.getContentTypeId(file));
						RCodeLaunching.runFileUsingCommand(command, file, fGotoConsole);
						return null;
					}
				}
			}
		}
		catch (final Exception e) {
			LaunchShortcutUtil.handleRLaunchException(e,
					RLaunchingMessages.RScriptLaunch_error_message, event);
			return null;
		}
		
		LaunchShortcutUtil.handleUnsupportedExecution(event);
		return null;
	}
	
}
