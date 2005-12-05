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

import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import de.walware.statet.r.internal.debug.RLaunchingMessages;
import de.walware.statet.r.launching.RCodeLaunchRegistry;


public class RSelectionDirectLaunchShortcut implements ILaunchShortcut {

	public void launch(ISelection selection, String mode) {

		// not supported
	}

	public void launch(IEditorPart editor, String mode) {

		assert mode.equals("run");
		
		try {
			AbstractTextEditor redt = (AbstractTextEditor) editor;
			IDocument doc = redt.getDocumentProvider().getDocument(editor.getEditorInput() );
			ITextSelection selection = (ITextSelection) redt.getSelectionProvider().getSelection();

//			if (selection.isEmpty()) {
//				reportError(redt, "No Text Selected.");
//				return;
//			}
			
			int line = selection.getStartLine();
			int endLine = selection.getEndLine();
			if (line == -1 || endLine == -1) {
				// error
				return;
			}

			String[] lines = new String[endLine - line +1];
			
			if (line == endLine) {
				lines[0] = (selection.getLength() > 0) ?
						doc.get(selection.getOffset(), getEndLineLength(doc, selection)-(selection.getOffset()-doc.getLineOffset(line))) :
						doc.get(doc.getLineOffset(line), getLineLength(doc, endLine));	
			}
			else {
				int i = 0;
				lines[i++] = doc.get(selection.getOffset(), 
						getLineLength(doc, line) - (selection.getOffset()-doc.getLineOffset(line)) );
				line++;
			
				while (line < endLine) {
					lines[i++] = doc.get(doc.getLineOffset(line), getLineLength(doc, line));
					line++;
				}
			
				lines[i] = doc.get(doc.getLineOffset(line), getEndLineLength(doc, selection) );
			}
			
//			for (int i = 0; i < lines.length; i++) {
//				System.out.println(lines[i] +  "[LINEEND]");
//			}
//			System.out.println("[END]");

			RCodeLaunchRegistry.runRCodeDirect(lines);
		}
		catch (Exception e) {
			LaunchShortcutUtil.handleRLaunchException(e, 
					RLaunchingMessages.RSelectionLaunch_error_message);
		}
	}

	private int getLineLength(IDocument doc, int line) throws BadLocationException {
		
		int lineLength = doc.getLineLength(line);
		String lineDelimiter = doc.getLineDelimiter(line);
		if (lineDelimiter != null)
			lineLength -= lineDelimiter.length();
		
		return lineLength;
	}
	
	private int getEndLineLength(IDocument doc, ITextSelection selection) throws BadLocationException {
		
		int endLine = selection.getEndLine(); 
		return Math.min(
				getLineLength(doc, endLine),
				selection.getOffset()+selection.getLength() - doc.getLineOffset(endLine) );
	}
	
//	private void reportError(AbstractTextEditor editor, String msg) {
//
//		IEditorStatusLine statusLine = (IEditorStatusLine) editor.getAdapter(IEditorStatusLine.class);
//		if (statusLine != null)
//			statusLine.setMessage(true, msg, null);
//	}
}
