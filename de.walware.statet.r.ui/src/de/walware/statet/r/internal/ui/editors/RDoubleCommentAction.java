/*******************************************************************************
 * Copyright (c) 2007-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.ecommons.ltk.ui.LTKUI;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.text.IndentUtil;
import de.walware.ecommons.text.IndentUtil.IndentEditAction;
import de.walware.ecommons.text.TextUtil;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.internal.ui.RUIPlugin;


/**
 * Add '## '-prefix to the current selection.
 */
public class RDoubleCommentAction extends Action implements IUpdate {
	
	public static final String ACTION_ID = "de.walware.statet.r.actions.AddDoubleComment"; //$NON-NLS-1$
	
	
	private final ISourceEditor fEditor;
	private final IRCoreAccess fCore;
	
	
	public RDoubleCommentAction(final ISourceEditor editor, final IRCoreAccess core) {
		fEditor = editor;
		fCore = core;
		setId(ACTION_ID);
		setActionDefinitionId(LTKUI.ADD_DOC_COMMENT_COMMAND_ID);
		
		update();
	}
	
	
	@Override
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
		} catch (final BadLocationException e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while running RDoubleClickAction.", e); //$NON-NLS-1$
		}
	}
	
	private void addComment() throws BadLocationException {
		final ISourceViewer sourceViewer = fEditor.getViewer();
		final IDocument document = sourceViewer.getDocument();
		final ITextSelection selection = (ITextSelection) sourceViewer.getSelectionProvider().getSelection();
		final int offset = selection.getOffset();
		
		final RHeuristicTokenScanner scanner = new RHeuristicTokenScanner();
		scanner.configure(document);
		if (selection.getLength() == 0 && scanner.isBlankLine(selection.getOffset())) {
			document.replace(offset, 0, "## "); //$NON-NLS-1$
			sourceViewer.setSelectedRange(offset+3, 0);
			sourceViewer.revealRange(offset+3, 0);
			return;
		}
		
		final IRegion textBlock = TextUtil.getBlock(document, selection.getOffset(), selection.getOffset()+selection.getLength());
		final IndentUtil util = new IndentUtil(document, fCore.getRCodeStyle());
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
			final IndentEditAction action = new IndentEditAction(column) {
				@Override
				public void doEdit(final int line, final int offset, final int length, final StringBuilder text) throws BadLocationException {
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
