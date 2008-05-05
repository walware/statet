/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.ide.ResourceUtil;

import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.RCodeLaunchRegistry;


/**
 * Launch shortcut, which submits the whole script (file)
 * using the <code>source</code> command to R
 * and does not change the focus.
 */
public class RScriptViaSourceLaunchShortcut implements ILaunchShortcut {
	
	
	protected boolean fGotoConsole = false;
	
	
	public void launch(final ISelection selection, final String mode) {
		assert mode.equals("run"); //$NON-NLS-1$
		
		try {
			final IStructuredSelection sel = (IStructuredSelection) selection;
			final Object firstElement = sel.getFirstElement();
			if (firstElement instanceof IFile) {
				final IFile file = (IFile) firstElement;
				final String command = RCodeLaunchRegistry.getPreferredFileCommand(LaunchShortcutUtil.getContentTypeId(file));
				RCodeLaunchRegistry.runFileUsingCommand(command, file, fGotoConsole);
			}
		}
		catch (final Exception e) {
			LaunchShortcutUtil.handleRLaunchException(e,
					RLaunchingMessages.RScriptLaunch_error_message);
		}
	}
	
	public void launch(final IEditorPart editor, final String mode) {
		assert mode.equals("run"); //$NON-NLS-1$
		
		try {
			final IEditorInput input = editor.getEditorInput();
			final IFile file = ResourceUtil.getFile(input);
			if (file != null) {
				final String command = RCodeLaunchRegistry.getPreferredFileCommand(LaunchShortcutUtil.getContentTypeId(file));
				RCodeLaunchRegistry.runFileUsingCommand(command, file, fGotoConsole);
			}
			else if (input instanceof IPathEditorInput) {
				final IPath path = ((IPathEditorInput) input).getPath();
				final String command = RCodeLaunchRegistry.getPreferredFileCommand(LaunchShortcutUtil.getContentTypeId(path));
				RCodeLaunchRegistry.runFileUsingCommand(command, path, fGotoConsole);
			}
			else if (input instanceof IURIEditorInput) {
				final URI uri = ((IURIEditorInput) input).getURI();
				final String command = RCodeLaunchRegistry.getPreferredFileCommand(LaunchShortcutUtil.getContentTypeId(uri));
				RCodeLaunchRegistry.runFileUsingCommand(command, uri, fGotoConsole);
			}
			else {
				throw new UnsupportedOperationException("Unsupported editor input: "+input.getClass().getName());
			}
		}
		catch (final Exception e) {
			LaunchShortcutUtil.handleRLaunchException(e,
					RLaunchingMessages.RScriptLaunch_error_message);
		}
	}
	
}
