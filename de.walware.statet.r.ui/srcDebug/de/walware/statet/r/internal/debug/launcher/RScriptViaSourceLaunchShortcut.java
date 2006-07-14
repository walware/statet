/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.launcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.ide.ResourceUtil;

import de.walware.statet.r.internal.debug.RLaunchingMessages;
import de.walware.statet.r.launching.RCodeLaunchRegistry;


public class RScriptViaSourceLaunchShortcut implements ILaunchShortcut {

	
	private static final String COMMAND = "source(\"${file}\")"; //$NON-NLS-1$
	
	
	public void launch(ISelection selection, String mode) {

		assert mode.equals("run"); //$NON-NLS-1$
		
		try {
			IStructuredSelection sel = (IStructuredSelection) selection;
			Object firstElement = sel.getFirstElement();
			if (firstElement instanceof IFile) {
				RCodeLaunchRegistry.runFileUsingCommand(COMMAND,
						((IFile) firstElement) );
			}
		}
		catch (Exception e) {
			LaunchShortcutUtil.handleRLaunchException(e, 
					RLaunchingMessages.RScriptLaunch_error_message);
		}
	}

	public void launch(IEditorPart editor, String mode) {

		assert mode.equals("run"); //$NON-NLS-1$
		
		try {
			IEditorInput input = editor.getEditorInput();
			IFile file = ResourceUtil.getFile(input);
			if (file != null) {
				RCodeLaunchRegistry.runFileUsingCommand(COMMAND, file);
			}
			else if (input instanceof IPathEditorInput) {
				RCodeLaunchRegistry.runFileUsingCommand(COMMAND, 
						((IPathEditorInput) input).getPath() );
			}
			else {
				throw new Exception("Unsupported editor input: "+input.getClass().getName());
			}
		}
		catch (Exception e) {
			LaunchShortcutUtil.handleRLaunchException(e, 
					RLaunchingMessages.RScriptLaunch_error_message);
		}
	}
	
}
