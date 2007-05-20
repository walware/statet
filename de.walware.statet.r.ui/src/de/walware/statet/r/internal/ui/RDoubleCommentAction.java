/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.text.edits.TextEdit;

import de.walware.statet.base.ui.IStatetUICommandIds;
import de.walware.statet.ext.ui.editors.IEditorAdapter;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.ui.text.r.RHeuristicTokenScanner;
import de.walware.statet.r.ui.text.r.RIndentation;
import de.walware.statet.r.ui.text.r.RIndentation.IndentEditAction;


/**
 * Add '## '-prefix to the current selection.
 */
public class RDoubleCommentAction extends Action {

	public static final String ACTION_ID = "de.walware.statet.r.actions.AddDoubleComment"; //$NON-NLS-1$

	
	private IEditorAdapter fEditor;
	private IRCoreAccess fCore;
	
	
	/**
	 * 
	 */
	public RDoubleCommentAction(IEditorAdapter editor, IRCoreAccess core) {
		fEditor = editor; 
		fCore = core;
		setId(ACTION_ID);
		setActionDefinitionId(IStatetUICommandIds.ADD_DOC_COMMENT);
		
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
		try {
			addComment();
		} catch (BadLocationException e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while running RDoubleClickAction.", e); //$NON-NLS-1$
		}
	}
	
	private void addComment() throws BadLocationException {
		ISourceViewer sourceViewer = fEditor.getSourceViewer();
		IDocument document = sourceViewer.getDocument();
		ITextSelection selection = (ITextSelection) sourceViewer.getSelectionProvider().getSelection();
		int offset = selection.getOffset();
		
		RHeuristicTokenScanner scanner = new RHeuristicTokenScanner();
		scanner.configure(document, null);
		if (selection.getLength() == 0 && scanner.isBlankLine(selection.getOffset())) {
			document.replace(offset, 0, "## "); //$NON-NLS-1$
			sourceViewer.setSelectedRange(offset+3, 0);
			sourceViewer.revealRange(offset+3, 0);
			return;
		}
		
		IRegion textBlock = scanner.getTextBlock(selection.getOffset(), selection.getOffset()+selection.getLength());
		RIndentation indent = new RIndentation(document, fCore.getRCodeStyle());
		IDocumentExtension4 doc4 = null;
		DocumentRewriteSession rewriteSession = null;
		try {
			if (document instanceof IDocumentExtension4) {
				doc4 = (IDocumentExtension4) document;
				rewriteSession = doc4.startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
			}
			int startLine = scanner.getFirstLineOfRegion(textBlock);
			int endLine = scanner.getLastLineOfRegion(textBlock);
			int depth = indent.getMultilineIndentationDepth(startLine, endLine);
			IndentEditAction action = indent.new IndentEditAction(depth) {
				public TextEdit createEdit(int offset, int length, StringBuilder text) throws BadLocationException {
					getDocument().replace(offset, length, text.toString());
					return null;
				}
			};
			for (int line = startLine; line <= endLine; line++) {
				indent.edit(line, action);
				document.replace(indent.getIndentedOffset(line, depth), 0, "## "); //$NON-NLS-1$
			}
		}
		finally {
			if (doc4 != null) {
				doc4.stopRewriteSession(rewriteSession);
			}
		}
	}
}
