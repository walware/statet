/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.IUpdate;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;

import de.walware.statet.base.ui.IStatetUICommandIds;

import de.walware.statet.r.core.rlang.RTokens;
import de.walware.statet.r.internal.ui.RUIPlugin;


/**
 * Inserts the R assignment &lt;-
 */
public class InsertAssignmentAction extends Action implements IUpdate {
	
	
	public static final String ACTION_ID = "de.walware.statet.r.ui.actions.InsertAssignment"; //$NON-NLS-1$
	
	
	private ISourceEditor fEditor;
	
	
	public InsertAssignmentAction(final ISourceEditor editor) {
		assert (editor != null);
		fEditor = editor;
		
		setId(ACTION_ID);
		setActionDefinitionId(IStatetUICommandIds.INSERT_ASSIGNMENT);
		
		update();
	}
	
	public void update() {
		setEnabled(fEditor.isEditable(false));
	}
	
	@Override
	public void run() {
		if (!fEditor.isEditable(true)) {
			return;
		}
		insertSequence();
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
