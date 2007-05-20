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

package de.walware.statet.r.ui.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ui.texteditor.ITextEditorExtension3;

import de.walware.statet.ext.ui.text.ITokenScanner;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCodeStyleSettings.IndentationType;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.IRDocumentPartitions;
import de.walware.statet.r.ui.text.r.RHeuristicTokenScanner;
import de.walware.statet.r.ui.text.r.RIndentation;


/**
 *
 */
public class RAutoEditStrategy extends DefaultIndentLineAutoEditStrategy {

	private static final char[] BRACKETS = new char[] { '{', '}' };
	
	
	private ITextEditorExtension3 fEditor3;
	private IRCoreAccess fRCoreAccess;
	private RHeuristicTokenScanner fScanner;
	
	private RCodeStyleSettings fRCodeStyle;
	private IDocument fDocument;
	
	
	public RAutoEditStrategy(IRCoreAccess rCoreAccess, REditor editor) {
		fRCoreAccess = rCoreAccess;
		if (editor instanceof ITextEditorExtension3) {
			fEditor3 = (ITextEditorExtension3) editor;
		}
		fScanner = new RHeuristicTokenScanner();
	}
	
	
	public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
		if (c.doit == false || c.text == null) {
			return;
		}
		if (fEditor3 == null || fEditor3.getInsertMode() != ITextEditorExtension3.SMART_INSERT) {
			super.customizeDocumentCommand(d, c);
			return;
		}

		assert(fDocument != null);
		fDocument = d;
		fRCodeStyle = fRCoreAccess.getRCodeStyle();
		try {
			if (c.length == 0 && TextUtilities.endsWith(d.getLegalLineDelimiters(), c.text) != -1) {
				smartIndentAfterNewLine(c);
			}
			else if (c.text.length() == 1) {
				smartIndentOnKeypress(c);
			}
//			else if (c.text.length() > 1 
//					&& getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SMART_PASTE))
//				smartPaste(d, c); // no smart backspace for paste
//			}
		}
		catch (Exception e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while customize a command in R auto edit strategy.", e); //$NON-NLS-1$
		}
		finally {
			fDocument = null;
			fRCodeStyle = null;
		}
	}

	private void smartIndentAfterNewLine(DocumentCommand c) throws BadLocationException, BadPartitioningException {
		int line = fDocument.getLineOfOffset(c.offset);
		int checkOffset = Math.max(0, c.offset-1);
		ITypedRegion partition = ((IDocumentExtension3) fDocument).getPartition(IRDocumentPartitions.R_DOCUMENT_PARTITIONING, checkOffset, true);
		if (partition.getType().equals(IRDocumentPartitions.R_COMMENT)) {
			checkOffset = partition.getOffset()-1;
		}
		RIndentation indentation = new RIndentation(fDocument, fRCodeStyle);
		int depth = indentation.getLineIndentationDepth(line, false);
		// new block?:
		fScanner.configure(fDocument, null);
		int match = fScanner.findNonWhitespaceBackward(checkOffset, fDocument.getLineOffset(line));
		if (match >= 0 && fDocument.getChar(match) == '{') {
			depth = indentation.getNextLevelDepth(depth, 1);
		}
		
		c.text += indentation.createIndentationString(depth);
	}

	private void smartIndentOnKeypress(DocumentCommand c) throws BadLocationException {
		switch (c.text.charAt(0)) {
		case '\t':
			smartIndentOnTab(c);
			break;
		case '}':
			smartIndentOnClosingBracket(c);
		}
	}

	private void smartIndentOnTab(DocumentCommand c) throws BadLocationException {
		if (fRCodeStyle.getIndentDefaultType() != IndentationType.SPACES) {
			return;
		}
		int lineOffset = fDocument.getLineOffset(fDocument.getLineOfOffset(c.offset));
		fScanner.configure(fDocument, null);
		if (fScanner.findNonWhitespaceBackward(c.offset-1, lineOffset) != ITokenScanner.NOT_FOUND) {
			return;
		}
		RIndentation indentation = new RIndentation(fDocument, fRCodeStyle);
		int column = indentation.getColumnAtOffset(c.offset);
		c.text = indentation.createIndentCompletionString(column);
	}
	
	private void smartIndentOnClosingBracket(DocumentCommand c) throws BadLocationException {
		if (!fDocument.getPartition(c.offset).getType().equals(IRDocumentPartitions.R_DEFAULT)) {
			return;
		}
		int lineOffset = fDocument.getLineOffset(fDocument.getLineOfOffset(c.offset));
		fScanner.configure(fDocument, IRDocumentPartitions.R_DEFAULT);
		if (fScanner.findNonWhitespaceBackward(c.offset-1, lineOffset) != ITokenScanner.NOT_FOUND) {
			return;
		}
		int blockStart = fScanner.findOpeningPeer(lineOffset, BRACKETS);
		if (blockStart == ITokenScanner.NOT_FOUND) {
			return;
		}
		RIndentation indentation = new RIndentation(fDocument, fRCodeStyle);
		int depth = indentation.getLineIndentationDepth(fDocument.getLineOfOffset(blockStart), false);
		c.text = indentation.createIndentationString(depth) + c.text;
		c.length += c.offset - lineOffset;
		c.offset = lineOffset;
	}
	
}
