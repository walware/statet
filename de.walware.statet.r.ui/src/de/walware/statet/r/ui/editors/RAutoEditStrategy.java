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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.texteditor.ITextEditorExtension3;

import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.text.ITokenScanner;
import de.walware.eclipsecommons.ltk.text.StringParseInput;
import de.walware.eclipsecommons.ltk.text.TextUtil;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCodeStyleSettings.IndentationType;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.core.rsource.RIndentUtil;
import de.walware.statet.r.core.rsource.RSourceIndenter;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.RScanner;
import de.walware.statet.r.internal.ui.RUIPlugin;


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
			fEditor3 = editor;
		}
		fScanner = new RHeuristicTokenScanner();
	}
	
	
	@Override
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
			ITypedRegion partition = TextUtilities.getPartition(fDocument, IRDocumentPartitions.R_DOCUMENT_PARTITIONING,
					c.offset, false);
			if (c.offset != partition.getOffset() && partition != null && partition.getType().equals(IRDocumentPartitions.R_STRING)) {
				return; // not inside strings
			}
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

	private void smartIndentAfterNewLine(DocumentCommand c) throws BadLocationException, BadPartitioningException, CoreException {
		try {
			smartIndentLine2(c, true);
		}
		catch (Exception e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while customize a command in R auto edit strategy (algorithm 2).", e); //$NON-NLS-1$
			smartIndentAfterNewLine1(c);
		}
	}
	
	private void smartIndentAfterNewLine1(DocumentCommand c) throws BadLocationException, BadPartitioningException, CoreException {
		final int line = fDocument.getLineOfOffset(c.offset);
		int checkOffset = Math.max(0, c.offset-1);
		ITypedRegion partition = ((IDocumentExtension3) fDocument).getPartition(IRDocumentPartitions.R_DOCUMENT_PARTITIONING, checkOffset, true);
		if (partition.getType().equals(IRDocumentPartitions.R_COMMENT)) {
			checkOffset = partition.getOffset()-1;
		}
		RIndentUtil util = new RIndentUtil(fDocument, fRCodeStyle);
		int column = util.getLineIndent(line, false)[RIndentUtil.COLUMN_IDX];
		// new block?:
		fScanner.configure(fDocument, null);
		int match = fScanner.findNonBlankBackward(checkOffset, fDocument.getLineOffset(line), false);
		if (match >= 0 && fDocument.getChar(match) == '{') {
			column = util.getNextLevelColumn(column, 1);
		}

		c.text += util.createIndentString(column);
	}
	
	private void smartIndentLine2(DocumentCommand c, boolean setCaret) throws BadLocationException, BadPartitioningException, CoreException {
		// new algorithm using RSourceIndenter
		String append;
		int cEnd = c.offset+c.length;
		IRegion cEndLine = fDocument.getLineInformationOfOffset(cEnd);
		int tempEnd = cEndLine.getOffset()+cEndLine.getLength();
		fScanner.configure(fDocument, null);
		if (endsWithNewLine(c.text)
				&& (cEnd >= tempEnd
						|| fScanner.findNonBlankForward(cEnd, tempEnd, false) == RHeuristicTokenScanner.NOT_FOUND)) {
			append = fDocument.get(cEnd, tempEnd-cEnd)+"+DUMMY+";
			cEnd = tempEnd;
		}
		else {
			append = "";
		}
		
		int shift = 0;
		if (c.offset > 2500) {
			shift = fDocument.getLineOfOffset(c.offset-2500);
			ITypedRegion partition = ((IDocumentExtension3) fDocument).getPartition(IRDocumentPartitions.R_DOCUMENT_PARTITIONING, shift, true);
			if (partition.getType().equals(IRDocumentPartitions.R_STRING)) {
				shift = partition.getOffset();
			}
		}
		tempEnd = cEnd+1500;
		if (fDocument.getLength() <= tempEnd) {
			tempEnd = fDocument.getLength();
		}
		String text = fDocument.get(shift, c.offset-shift)
				+ c.text + append + fDocument.get(cEnd, tempEnd-cEnd);

		AstInfo<RAstNode> ast = new AstInfo<RAstNode>(RAst.LEVEL_MINIMAL, 0);
		RScanner scanner = new RScanner(new StringParseInput(text), ast);
		int dummyCoffset = c.offset-shift;
		int dummyCend = dummyCoffset+c.text.length();
		final AbstractDocument dummyDoc = new Document(text);
		text = null;

		final int dummyFirstLine = dummyDoc.getLineOfOffset(dummyCend);
		final int dummyLastLine = dummyFirstLine;
		ast.root = scanner.scanSourceUnit();
		RSourceIndenter indenter = new RSourceIndenter();
		TextEdit edit = indenter.getIndentEdits(dummyDoc, ast,
				dummyFirstLine, dummyLastLine, fRCoreAccess);

		Position cPos = new Position(dummyCoffset, c.text.length());
		dummyDoc.addPosition(cPos);
		
//		TextEdit.getCoverage(edit.getChildren())
		c.length = c.length+edit.getLength()
				// add space between two replacement regions
				// minus overlaps with c.text
				-TextUtil.overlaps(edit.getOffset(), edit.getExclusiveEnd(), dummyCoffset, dummyCend);
		if (edit.getOffset() < dummyCoffset) { // move offset, if edit begins before c
			dummyCoffset = edit.getOffset();
			c.offset = shift+dummyCoffset;
		}
		edit.apply(dummyDoc);
		
		tempEnd = edit.getExclusiveEnd();
		dummyCend = cPos.getOffset()+cPos.getLength();
		if (!cPos.isDeleted && dummyCend > tempEnd) {
			tempEnd = dummyCend;
		}
		c.text = dummyDoc.get(dummyCoffset, tempEnd-dummyCoffset);
		if (setCaret) {
			c.caretOffset = shift+indenter.getNewIndentOffset(dummyLastLine);
			c.shiftsCaret = false;
		}
	}
	

	private final boolean endsWithNewLine(String text) {
		for (int i = text.length()-1; i >= 0; i--) {
			char c = text.charAt(i);
			if (c == '\r' || c == '\n') {
				return true;
			}
			if (c != ' ' && c != '\t') {
				return false;
			}
		}
		return false;
	}

	private void smartIndentOnKeypress(DocumentCommand c) throws BadLocationException {
		switch (c.text.charAt(0)) {
		case '\t':
			smartIndentOnTab(c);
			break;
		case '}':
			smartIndentOnClosingBracket(c);
			break;
		case '{':
			smartIndentOnOpeningBracket(c);
			break;
		case ')':
			smartIndentOnClosingParenthesis(c);
			break;
		}
	}

	private void smartIndentOnTab(DocumentCommand c) throws BadLocationException {
		if (fRCodeStyle.getIndentDefaultType() != IndentationType.SPACES) {
			return;
		}
		int lineOffset = fDocument.getLineOffset(fDocument.getLineOfOffset(c.offset));
		fScanner.configure(fDocument, null);
		if (fScanner.findNonBlankBackward(c.offset-1, lineOffset, false) != ITokenScanner.NOT_FOUND) {
			// not first char
			return;
		}
		RIndentUtil indentation = new RIndentUtil(fDocument, fRCodeStyle);
		int column = indentation.getColumnAtOffset(c.offset);
		c.text = indentation.createIndentCompletionString(column);
	}
	
	private void smartIndentOnClosingBracket(DocumentCommand c) throws BadLocationException {
		if (!fDocument.getPartition(c.offset).getType().equals(IRDocumentPartitions.R_DEFAULT)) {
			return;
		}
		int lineOffset = fDocument.getLineOffset(fDocument.getLineOfOffset(c.offset));
		fScanner.configure(fDocument, null);
		if (fScanner.findNonBlankBackward(c.offset-1, lineOffset, false) != ITokenScanner.NOT_FOUND) {
			// not first char
			return;
		}

		try {
			smartIndentLine2(c, false);
		}
		catch (Exception e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while customize a command in R auto edit strategy (algorithm 2).", e); //$NON-NLS-1$
			smartIndentOnClosingBracket1(c);
		}
	}
	
	private void smartIndentOnClosingBracket1(DocumentCommand c) throws BadLocationException {
		int lineOffset = fDocument.getLineOffset(fDocument.getLineOfOffset(c.offset));
		int blockStart = fScanner.findOpeningPeer(lineOffset, BRACKETS);
		if (blockStart == ITokenScanner.NOT_FOUND) {
			return;
		}
		RIndentUtil util = new RIndentUtil(fDocument, fRCodeStyle);
		int column = util.getLineIndent(fDocument.getLineOfOffset(blockStart), false)[RIndentUtil.COLUMN_IDX];
		c.text = util.createIndentString(column) + c.text;
		c.length += c.offset - lineOffset;
		c.offset = lineOffset;
	}
	
	private void smartIndentOnOpeningBracket(DocumentCommand c) throws BadLocationException {
		if (!fDocument.getPartition(c.offset).getType().equals(IRDocumentPartitions.R_DEFAULT)) {
			return;
		}
		int lineOffset = fDocument.getLineOffset(fDocument.getLineOfOffset(c.offset));
		fScanner.configure(fDocument, null);
		if (fScanner.findNonBlankBackward(c.offset-1, lineOffset, false) != ITokenScanner.NOT_FOUND) {
			// not first char
			return;
		}

		try {
			smartIndentLine2(c, false);
		}
		catch (Exception e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while customize a command in R auto edit strategy (algorithm 2).", e); //$NON-NLS-1$
		}
	}

	private void smartIndentOnClosingParenthesis(DocumentCommand c) throws BadLocationException {
		if (!fDocument.getPartition(c.offset).getType().equals(IRDocumentPartitions.R_DEFAULT)) {
			return;
		}
		int lineOffset = fDocument.getLineOffset(fDocument.getLineOfOffset(c.offset));
		fScanner.configure(fDocument, null);
		if (fScanner.findNonBlankBackward(c.offset-1, lineOffset, false) != ITokenScanner.NOT_FOUND) {
			// not first char
			return;
		}

		try {
			smartIndentLine2(c, false);
		}
		catch (Exception e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while customize a command in R auto edit strategy (algorithm 2).", e); //$NON-NLS-1$
		}
	}
	
}
