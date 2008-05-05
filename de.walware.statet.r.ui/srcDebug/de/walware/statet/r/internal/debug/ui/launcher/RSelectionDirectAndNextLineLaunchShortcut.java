/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
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
	
	
	public void launch(final ISelection selection, final String mode) {
		// not supported
	}
	
	public void launch(final IEditorPart editor, final String mode) {
		assert mode.equals("run"); //$NON-NLS-1$
		
		try {
			final AbstractTextEditor redt = (AbstractTextEditor) editor;
			final IDocument doc = redt.getDocumentProvider().getDocument(editor.getEditorInput() );
			final ITextSelection selection = (ITextSelection) redt.getSelectionProvider().getSelection();
			
			final String[] lines = LaunchShortcutUtil.listLines(doc, selection);
			
			if (lines == null) {
				return;
			}
			
			RCodeLaunchRegistry.runRCodeDirect(lines, false);
			
			final int newOffset = getNextLineOffset(doc, selection.getEndLine());
			if (newOffset >= 0) {
				redt.selectAndReveal(newOffset, 0);
			}
		}
		catch (final CoreException e) {
			LaunchShortcutUtil.handleRLaunchException(e,
					RLaunchingMessages.RSelectionLaunch_error_message);
		}
	}
	
	
	private int getNextLineOffset(final IDocument doc, final int endLine) {
		try {
			if (endLine >= 0 && endLine+1 < doc.getNumberOfLines()) {
				return doc.getLineOffset(endLine+1);
			}
			else {
				return -1;
			}
		}
		catch (final BadLocationException e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "Error while find next line.", e); //$NON-NLS-1$
			return -1;
		}
	}
	
}
