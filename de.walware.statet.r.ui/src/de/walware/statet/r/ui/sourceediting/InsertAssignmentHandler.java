/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.sourceediting;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ISourceViewer;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;

import de.walware.statet.r.core.rlang.RTokens;
import de.walware.statet.r.internal.ui.RUIPlugin;


/**
 * Inserts the R assignment &lt;-
 */
public class InsertAssignmentHandler extends AbstractHandler {
	
	
	private final ISourceEditor fEditor;
	
	
	public InsertAssignmentHandler(final ISourceEditor editor) {
		fEditor = editor;
	}
	
	
	@Override
	public void setEnabled(final Object evaluationContext) {
		setBaseEnabled(fEditor.isEditable(false));
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		if (fEditor.isEditable(true)) {
			insertSequence();
		}
		return null;
	}
	
	/**
	 * Inserts the assignment char sequence.
	 */
	private void insertSequence() {
		final ISourceViewer sourceViewer = fEditor.getViewer();
		final IDocument document = sourceViewer.getDocument();
		final ITextSelection selection = (ITextSelection) sourceViewer.getSelectionProvider().getSelection();
		final int offset = selection.getOffset();
		final int selectionLength = selection.getLength();
		
		String sequence = "<-"; //$NON-NLS-1$ 
		try {
			if (offset > 0 && RTokens.isWhitespace(document.getChar(offset-1))) {
				sequence = sequence + " "; //$NON-NLS-1$
			}
			else {
				sequence = " " + sequence + " "; //$NON-NLS-1$ //$NON-NLS-2$
			}
			document.replace(offset, selectionLength, sequence);
		} catch (final BadLocationException e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while inserting assignment.", e); //$NON-NLS-1$
		}
		final int targetOffset = offset + sequence.length();
		
		sourceViewer.setSelectedRange(targetOffset, 0);
		sourceViewer.revealRange(targetOffset, 0);
	}
	
}
