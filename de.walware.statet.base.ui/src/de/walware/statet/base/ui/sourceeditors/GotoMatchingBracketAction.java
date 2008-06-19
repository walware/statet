/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui.sourceeditors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.IEditorStatusLine;

import de.walware.eclipsecommons.ltk.text.ITokenScanner;
import de.walware.eclipsecommons.ui.text.PairMatcher;

import de.walware.statet.base.ui.IStatetUICommandIds;


public class GotoMatchingBracketAction extends Action {
	
	public static final String ACTION_ID = "de.walware.statet.ui.actions.GotoMatchingBracket"; //$NON-NLS-1$
	
	
	private PairMatcher fPairMatcher;
	private IEditorAdapter fEditor;
	
	
	public GotoMatchingBracketAction(final PairMatcher pairMatcher, final IEditorAdapter editor) {
		
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
		final ISourceViewer sourceViewer = fEditor.getSourceViewer();
		if (sourceViewer == null) {
			return;
		}
		final IDocument document = sourceViewer.getDocument();
		
		final ITextSelection selection = (ITextSelection) sourceViewer.getSelectionProvider().getSelection();
		int offset = selection.getOffset();
		int selectionLength = selection.getLength();
		final char[][] brackets = fPairMatcher.getPairs();
		
		if (selectionLength == 1) {
			try {
				final char c = document.getChar(offset);
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
			} catch (final BadLocationException e) {
			}
		}
		
		if (selectionLength > 0) {
			final IEditorStatusLine statusLine = (IEditorStatusLine) fEditor.getAdapter(IEditorStatusLine.class);
			if (statusLine != null) {
				statusLine.setMessage(true, EditorMessages.GotoMatchingBracketAction_error_InvalidSelection, null);
			}
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}
		
		final IRegion region = fPairMatcher.match(document, offset);
		if (region == null) {
			final IEditorStatusLine statusLine = (IEditorStatusLine) fEditor.getAdapter(IEditorStatusLine.class);
			if (statusLine != null) {
				statusLine.setMessage(true, EditorMessages.GotoMatchingBracketAction_error_NoMatchingBracket, null);
			}
			Display.getCurrent().beep();
			return;
		}
		
		final int matchingOffset = region.getOffset();
		final int matchingLength = region.getLength();
		
		if (matchingLength < 1)
			return;
			
		final int targetOffset = (fPairMatcher.getAnchor() == PairMatcher.RIGHT) ? matchingOffset+1 : matchingOffset+matchingLength-1;
		
		boolean visible = false;
		if (sourceViewer instanceof ITextViewerExtension5) {
			final ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
			visible = (extension.modelOffset2WidgetOffset(targetOffset) > -1);
		} else {
			final IRegion visibleRegion = sourceViewer.getVisibleRegion();
			visible = (targetOffset >= visibleRegion.getOffset() && targetOffset <= visibleRegion.getOffset() + visibleRegion.getLength());
		}
		
		if (!visible) {
			final IEditorStatusLine statusLine = (IEditorStatusLine) fEditor.getAdapter(IEditorStatusLine.class);
			if (statusLine != null) {
				statusLine.setMessage(true, EditorMessages.GotoMatchingBracketAction_error_BracketOutsideSelectedElement, null);
			}
			Display.getCurrent().beep();
			return;
		}
		
		sourceViewer.setSelectedRange(targetOffset, 0);
		sourceViewer.revealRange(targetOffset, 0);
	}
	
}
