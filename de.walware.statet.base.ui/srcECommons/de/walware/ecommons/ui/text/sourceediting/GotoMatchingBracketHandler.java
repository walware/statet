/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.text.sourceediting;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.IEditorStatusLine;

import de.walware.ecommons.internal.ui.text.EditingMessages;
import de.walware.ecommons.text.ITokenScanner;
import de.walware.ecommons.ui.text.PairMatcher;


public class GotoMatchingBracketHandler extends AbstractHandler {
	
	
	private ISourceEditor fSourceEditor;
	
	private PairMatcher fPairMatcher;
	
	
	public GotoMatchingBracketHandler(final PairMatcher pairMatcher, final ISourceEditor editor) {
		assert (pairMatcher != null);
		assert (editor != null);
		fSourceEditor = editor;
		fPairMatcher = pairMatcher;
		
//		setBaseEnabled(true);
	}
	
	
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		gotoMatchingBracket();
		
		return null;
	}
	
	/**
	 * Jumps to the matching bracket.
	 */
	public void gotoMatchingBracket() {
		final ISourceViewer sourceViewer = fSourceEditor.getViewer();
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
			final IEditorStatusLine statusLine = (IEditorStatusLine) fSourceEditor.getAdapter(IEditorStatusLine.class);
			if (statusLine != null) {
				statusLine.setMessage(true, EditingMessages.GotoMatchingBracketAction_error_InvalidSelection, null);
			}
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}
		
		final IRegion region = fPairMatcher.match(document, offset);
		if (region == null) {
			final IEditorStatusLine statusLine = (IEditorStatusLine) fSourceEditor.getAdapter(IEditorStatusLine.class);
			if (statusLine != null) {
				statusLine.setMessage(true, EditingMessages.GotoMatchingBracketAction_error_NoMatchingBracket, null);
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
			final IEditorStatusLine statusLine = (IEditorStatusLine) fSourceEditor.getAdapter(IEditorStatusLine.class);
			if (statusLine != null) {
				statusLine.setMessage(true, EditingMessages.GotoMatchingBracketAction_error_BracketOutsideSelectedElement, null);
			}
			Display.getCurrent().beep();
			return;
		}
		
		sourceViewer.setSelectedRange(targetOffset, 0);
		sourceViewer.revealRange(targetOffset, 0);
	}
	
}
