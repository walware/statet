/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import de.walware.statet.r.internal.debug.RLaunchingMessages;
import de.walware.statet.r.launching.RCodeLaunchRegistry;


/**
 * Launch shortcut, which submits the current line/selection directly to R
 * and does not change the focus.
 * 
 * Supports only text editors.
 */
public class RSelectionDirectLaunchShortcut implements ILaunchShortcut {
	
	
	protected boolean fGotoConsole = false;
	
	
	public void launch(ISelection selection, String mode) {
		
		// not supported
	}

	public void launch(IEditorPart editor, String mode) {
		
		assert mode.equals("run"); //$NON-NLS-1$
		
		try {
			AbstractTextEditor redt = (AbstractTextEditor) editor;
			IDocument doc = redt.getDocumentProvider().getDocument(editor.getEditorInput() );
			ITextSelection selection = (ITextSelection) redt.getSelectionProvider().getSelection();
			
			String[] lines = LaunchShortcutUtil.listLines(doc, selection);

			if (lines == null) {
				return;
			}
			
			RCodeLaunchRegistry.runRCodeDirect(lines, fGotoConsole);
		}
		catch (CoreException e) {
			LaunchShortcutUtil.handleRLaunchException(e,
					RLaunchingMessages.RSelectionLaunch_error_message);
		}
	}
	
	
//	private void reportError(AbstractTextEditor editor, String msg) {
//
//		IEditorStatusLine statusLine = (IEditorStatusLine) editor.getAdapter(IEditorStatusLine.class);
//		if (statusLine != null)
//			statusLine.setMessage(true, msg, null);
//	}
}
