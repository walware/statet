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

package de.walware.statet.r.internal.debug.ui.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.launching.RCodeLaunchRegistry;


/**
 * Launch shortcut, which submits the current line/selection 
 * directly to R and moves the curser to the next line.
 * 
 * Supports only text editors.
 */
public class RSelectionDirectAndNextLineLaunchShortcut implements ILaunchShortcut {
	
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
			
			RCodeLaunchRegistry.runRCodeDirect(lines, false);
			
			int newOffset = getNextLineOffset(doc, selection.getEndLine());
			if (newOffset >= 0) {
				redt.selectAndReveal(newOffset, 0);
			}
		}
		catch (CoreException e) {
			LaunchShortcutUtil.handleRLaunchException(e,
					RLaunchingMessages.RSelectionLaunch_error_message);
		}
	}
	
	
	private int getNextLineOffset(IDocument doc, int endLine) {
		
		try {
			if (endLine >= 0 && endLine+1 < doc.getNumberOfLines()) {
				return doc.getLineOffset(endLine+1);
			}
			else {
				return -1;
			}
		} catch (BadLocationException e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "Error while find next line.", e);
			return -1;
		}
	}
	
}
