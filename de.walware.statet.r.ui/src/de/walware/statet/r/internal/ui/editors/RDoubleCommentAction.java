/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.IUpdate;

import de.walware.eclipsecommons.ltk.text.IndentUtil.IndentEditAction;

import de.walware.statet.base.ui.IStatetUICommandIds;
import de.walware.statet.base.ui.sourceeditors.IEditorAdapter;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.core.rsource.RIndentUtil;
import de.walware.statet.r.internal.ui.RUIPlugin;


/**
 * Add '## '-prefix to the current selection.
 */
public class RDoubleCommentAction extends Action implements IUpdate {
	
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
		final IDocument document = sourceViewer.getDocument();
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
		final RIndentUtil util = new RIndentUtil(document, fCore.getRCodeStyle());
		IDocumentExtension4 doc4 = null;
		DocumentRewriteSession rewriteSession = null;
		try {
			if (document instanceof IDocumentExtension4) {
				doc4 = (IDocumentExtension4) document;
				rewriteSession = doc4.startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
			}
			final int firstLine = scanner.getFirstLineOfRegion(textBlock);
			final int lastLine = scanner.getLastLineOfRegion(textBlock);
			final int column = util.getMultilineIndentColumn(firstLine, lastLine);
			IndentEditAction action = new IndentEditAction(column) {
				@Override
				public void doEdit(int line, int offset, int length, StringBuilder text) throws BadLocationException {
					if (text != null) {
						document.replace(offset, length, text.toString());
					}
					document.replace(util.getIndentedOffsetAt(line, column), 0, "## "); //$NON-NLS-1$
				}
			};
			util.editInIndent(firstLine, lastLine, action);
		}
		finally {
			if (doc4 != null) {
				doc4.stopRewriteSession(rewriteSession);
			}
		}
	}
	
}
