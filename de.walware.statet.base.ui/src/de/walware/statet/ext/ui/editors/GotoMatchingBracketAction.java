/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.ISourceViewer;

import de.walware.eclipsecommons.ltk.text.ITokenScanner;

import de.walware.statet.base.ui.IStatetUICommandIds;
import de.walware.statet.ext.ui.text.PairMatcher;


public class GotoMatchingBracketAction extends Action {
	
	
	public static final String ACTION_ID = "de.walware.statet.ui.actions.GotoMatchingBracket"; //$NON-NLS-1$

	
	private PairMatcher fPairMatcher;
	private IEditorAdapter fEditor;

	
	public GotoMatchingBracketAction(PairMatcher pairMatcher, IEditorAdapter editor) {
		
		assert (pairMatcher != null);
		assert (editor != null);
		fPairMatcher = pairMatcher;
		fEditor = editor;
		
		setText(EditorMessages.GotoMatchingBracketAction_label);
		setToolTipText(EditorMessages.GotoMatchingBracketAction_tooltip);
		setDescription(EditorMessages.GotoMatchingBracketAction_description);
		setId(ACTION_ID);
		setActionDefinitionId(IStatetUICommandIds.GOTO_MATCHING_BRACKET);

		setEnabled(true);
	}
	
	@Override
	public void run() {
		
		gotoMatchingBracket();
	}

	/**
	 * Jumps to the matching bracket.
	 */
	public void gotoMatchingBracket() {
		
		ISourceViewer sourceViewer = fEditor.getSourceViewer();
		if (sourceViewer == null) {
			return;
		}
		IDocument document = sourceViewer.getDocument();
		
		ITextSelection selection = (ITextSelection) sourceViewer.getSelectionProvider().getSelection();
		int offset = selection.getOffset();
		int selectionLength = selection.getLength();
		char[][] brackets = fPairMatcher.getPairs();

		if (selectionLength == 1) {
			try {
				char c = document.getChar(offset);
				for (int i = 0; i < brackets.length; i++) {
					if (c == brackets[i][ITokenScanner.OPENING_PEER]) {
						offset++;
						selectionLength = 0;
						break;
					}
					if (c == brackets[i][ITokenScanner.CLOSING_PEER]) {
						selectionLength = 0;
						break;
					}
				}
			} catch (BadLocationException e) {
			}
		}
		
		if (selectionLength > 0) {
			fEditor.setStatusLineErrorMessage(EditorMessages.GotoMatchingBracketAction_error_InvalidSelection);
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}

		IRegion region = fPairMatcher.match(document, offset);
		if (region == null) {
			fEditor.setStatusLineErrorMessage(EditorMessages.GotoMatchingBracketAction_error_NoMatchingBracket);
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}
		
		int matchingOffset = region.getOffset();
		int matchingLength = region.getLength();
		
		if (matchingLength < 1)
			return;
			
		int targetOffset = (fPairMatcher.getAnchor() == PairMatcher.RIGHT) ? matchingOffset+1 : matchingOffset+matchingLength-1;
		
		boolean visible = false;
		if (sourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
			visible = (extension.modelOffset2WidgetOffset(targetOffset) > -1);
		} else {
			IRegion visibleRegion = sourceViewer.getVisibleRegion();
			visible = (targetOffset >= visibleRegion.getOffset() && targetOffset <= visibleRegion.getOffset() + visibleRegion.getLength());
		}
		
		if (!visible) {
			fEditor.setStatusLineErrorMessage(EditorMessages.GotoMatchingBracketAction_error_BracketOutsideSelectedElement);
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}
		
		sourceViewer.setSelectedRange(targetOffset, 0);
		sourceViewer.revealRange(targetOffset, 0);
	}
}