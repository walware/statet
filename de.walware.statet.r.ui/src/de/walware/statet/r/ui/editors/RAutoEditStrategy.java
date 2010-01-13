/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
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
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension3;

import de.walware.ecommons.text.ITokenScanner;
import de.walware.ecommons.text.PartitioningConfiguration;
import de.walware.ecommons.text.StringParseInput;
import de.walware.ecommons.text.TextUtil;
import de.walware.ecommons.ui.text.sourceediting.BracketLevel;
import de.walware.ecommons.ui.text.sourceediting.ISourceEditor;
import de.walware.ecommons.ui.text.sourceediting.ISourceEditorAddon;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.ui.console.InputSourceViewer;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCodeStyleSettings.IndentationType;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.core.rsource.RIndentUtil;
import de.walware.statet.r.core.rsource.RSourceIndenter;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstInfo;
import de.walware.statet.r.core.rsource.ast.RScanner;
import de.walware.statet.r.internal.ui.RUIPlugin;


/**
 * Auto edit strategy for R code:
 *  - auto indent on keys
 *  - special indent with tab key
 *  - auto indent on paste
 *  - auto close of pairs
 */
public class RAutoEditStrategy extends DefaultIndentLineAutoEditStrategy
		implements ISourceEditorAddon {
	
	private static final char[] CURLY_BRACKETS = new char[] { '{', '}' };
	
	
	private class RealTypeListener implements VerifyKeyListener {
		public void verifyKey(final VerifyEvent event) {
			if (!event.doit) {
				return;
			}
			switch (event.character) {
			case '{':
			case '}':
			case '(':
			case ')':
			case '[':
			case '%':
			case '"':
			case '\'':
			case '`':
			case '#':
				event.doit = !customizeKeyPressed(event.character);
				return;
			case '\t':
				if (event.stateMask == 0) {
					event.doit = !customizeKeyPressed(event.character);
				}
				return;
			case 0x0A:
			case 0x0D:
				if (fEditor3 != null) {
					event.doit = !customizeKeyPressed('\n');
				}
				return;
			}
		}
	};
	
	
	private final RealTypeListener fMyListener;
	private ISourceEditor fEditor;
	private ITextEditorExtension3 fEditor3;
	private SourceViewer fViewer;
	private IRCoreAccess fRCoreAccess;
	
	private IRegion fValidRange;
	private RHeuristicTokenScanner fScanner;
	private PartitioningConfiguration fPartitioning;
	private RSourceIndenter fIndenter;
	
	private RCodeStyleSettings fRCodeStyle;
	private AbstractDocument fDocument;
	private boolean fIgnoreCommands = false;
	private REditorOptions fOptions;
	
	
	public RAutoEditStrategy(final IRCoreAccess rCoreAccess, final ISourceEditor sourceEditor, final TextEditor eclipseEditor) {
		assert (rCoreAccess != null);
		assert (sourceEditor != null);
		
		fRCoreAccess = rCoreAccess;
		fEditor = sourceEditor;
		fOptions = RUIPlugin.getDefault().getREditorSettings(rCoreAccess.getPrefs());
		assert (fOptions != null);
		
		fViewer = fEditor.getViewer();
		fEditor3 = eclipseEditor;
		fMyListener = new RealTypeListener();
	}
	
	public void install(final ISourceEditor editor) {
		assert (editor.getViewer() == fViewer);
		fViewer.prependVerifyKeyListener(fMyListener);
	}
	
	public void uninstall() {
		fViewer.removeVerifyKeyListener(fMyListener);
	}
	
	
	private final boolean initCustomization(final int offset, final int c) {
		assert(fDocument != null);
		if (fScanner == null) {
			fScanner = createScanner();
			fPartitioning = fScanner.getPartitioningConfig();
			fIndenter = new RSourceIndenter();
		}
		fRCodeStyle = fRCoreAccess.getRCodeStyle();
		fValidRange = getValidRange(offset, c);
		return (fValidRange != null);
	}
	
	protected RHeuristicTokenScanner createScanner() {
		return new RHeuristicTokenScanner();
	}
	
	protected IRegion getValidRange(final int offset, final int c) {
		return new Region(0, fDocument.getLength());
	}
	
	protected final IDocument getDocument() {
		return fDocument;
	}
	
	private final void quitCustomization() {
		fDocument = null;
		fRCodeStyle = null;
	}
	
	
	private final boolean isSmartInsertEnabled() {
		return ((fEditor3 == null && fOptions.isSmartModeByDefaultEnabled())
				|| (fEditor3 != null && fEditor3.getInsertMode() == ITextEditorExtension3.SMART_INSERT)
				);
	}
	
	private final boolean isClosedString(int offset, final int end, final boolean endVirtual, final char sep) {
		fScanner.configure(fDocument);
		boolean in = true; // we start always inside after a sep
		final char[] chars = new char[] { sep, '\\' };
		while (offset < end) {
			offset = fScanner.scanForward(offset, end, chars);
			if (offset == RHeuristicTokenScanner.NOT_FOUND) {
				offset = end;
				break;
			}
			offset++;
			if (fScanner.getChar() == '\\') {
				offset++;
			}
			else {
				in = !in;
			}
		}
		return (offset == end) && (!in ^ endVirtual);
	}
	
	private final boolean isClosedBracket(final int backwardOffset, final int forwardOffset, final int searchType) {
		int[] balance = new int[3];
		balance[searchType]++;
		fScanner.configureDefaultParitions(fDocument);
		balance = fScanner.computeBracketBalance(backwardOffset, forwardOffset, balance, searchType);
		return (balance[searchType] <= 0);
	}
	
	private boolean isCharAt(final int offset, final char c) throws BadLocationException {
		return (offset >= fValidRange.getOffset() && offset < fValidRange.getOffset()+fValidRange.getLength()
				&& fDocument.getChar(offset) == c);
	}
	
	private boolean isValueChar(final int offset) throws BadLocationException {
		if (offset >= fValidRange.getOffset() && offset < fValidRange.getOffset()+fValidRange.getLength()) {
			final int c = fDocument.getChar(offset);
			return (c == '"' || c == '\'' || c == '`' || Character.isLetterOrDigit(c));
		}
		return false;
	}
	
	private boolean isAfterRoxygen(final int offset) throws BadLocationException {
		fScanner.configure(fDocument);
		final int line = fDocument.getLineOfOffset(offset);
		if (line > 0 && fScanner.findAnyNonBlankBackward(offset, fDocument.getLineOffset(line)-1, false) == ITokenScanner.NOT_FOUND) {
			final IRegion prevLineInfo = fDocument.getLineInformation(line-1);
			if (prevLineInfo.getLength() > 0 && TextUtilities.getPartition(fDocument, fPartitioning.getPartitioning(),
						prevLineInfo.getOffset()+prevLineInfo.getLength()-1, false).getType() == IRDocumentPartitions.R_ROXYGEN) {
				return true;
			}
		}
		return false;
	}
	
	private int countBackward(final char c, int offset) throws BadLocationException {
		int count = 0;
		while (--offset >= 0 && fDocument.getChar(offset) == c) {
			count++;
		}
		return count;
	}
	
	@Override
	public void customizeDocumentCommand(final IDocument d, final DocumentCommand c) {
		if (fIgnoreCommands || c.doit == false || c.text == null) {
			return;
		}
		if (!isSmartInsertEnabled()) {
			super.customizeDocumentCommand(d, c);
			return;
		}
		
		fDocument = (AbstractDocument) d;
		if (!initCustomization(c.offset, -1)) {
			return;
		}
		try {
			final String partitionType = fDocument.getPartition(fPartitioning.getPartitioning(), c.offset, true).getType();
			if (fPartitioning.getDefaultPartitionConstraint().matches(partitionType)) {
				if (c.length == 0 && TextUtilities.equals(d.getLegalLineDelimiters(), c.text) != -1) {
					smartIndentOnNewLine(c);
				}
				else if (c.text.length() > 1 && fOptions.isSmartPasteEnabled()) {
					smartPaste(c);
				}
			}
		}
		catch (final Exception e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while customize a command in R auto edit strategy.", e); //$NON-NLS-1$
		}
		finally {
			quitCustomization();
		}
	}
	
	/**
	 * Second main entry method for real single key presses.
	 * 
	 * @return <code>true</code>, if key was processed by method
	 */
	private boolean customizeKeyPressed(final char c) {
		if (!isSmartInsertEnabled() || !UIAccess.isOkToUse(fViewer)) {
			return false;
		}
		fIgnoreCommands = true;
		fDocument = (AbstractDocument) fViewer.getDocument();
		ITextSelection selection = (ITextSelection) fViewer.getSelection();
		if (!initCustomization(selection.getOffset(), c)) {
			return false;
		}
		try {
			final DocumentCommand command = new DocumentCommand() {};
			command.offset = selection.getOffset();
			command.length = selection.getLength();
			command.doit = true;
			command.shiftsCaret = true;
			command.caretOffset = -1;
			int linkedModeType = -1;
			int linkedModeOffset = -1;
			final int cEnd = command.offset+command.length;
			
			final String partitionType = fDocument.getPartition(fPartitioning.getPartitioning(), command.offset, true).getType();
			switch (c) {
			case '\t':
				if (fPartitioning.getDefaultPartitionConstraint().matches(partitionType)
						// must not span multiple lines
						&& (command.length == 0 || fDocument.getLineOfOffset(command.offset) == fDocument.getLineOfOffset(cEnd))
						) {
					command.text = "\t"; //$NON-NLS-1$
					smartIndentOnTab(command);
					break;
				}
				return false;
			case '}':
				if (fPartitioning.getDefaultPartitionConstraint().matches(partitionType)) {
					command.text = "}"; //$NON-NLS-1$
					smartIndentOnClosingBracket(command);
					break;
				}
				return false;
			case '{':
				if (fPartitioning.getDefaultPartitionConstraint().matches(partitionType)) {
					command.text = "{"; //$NON-NLS-1$
					if (fOptions.isSmartCurlyBracketsEnabled() && !isValueChar(cEnd)) {
						if (!isClosedBracket(command.offset, cEnd, 0)) {
							command.text = "{}"; //$NON-NLS-1$
							linkedModeType = 1;
						}
						else if (isCharAt(cEnd, '}')) {
							linkedModeType = 1;
						}
					}
					linkedModeOffset = smartIndentOnFirstLineCharDefault2(command);
					break;
				}
				return false;
			case '(':
				if (fPartitioning.getDefaultPartitionConstraint().matches(partitionType)) {
					command.text = "("; //$NON-NLS-1$
					if (fOptions.isSmartRoundBracketsEnabled() && !isValueChar(cEnd)) {
						if (!isClosedBracket(command.offset, cEnd, 1)) {
							command.text = "()"; //$NON-NLS-1$
							linkedModeType = 1;
						}
						else if (isCharAt(cEnd, ')')) {
							linkedModeType = 1;
						}
					}
					break;
				}
				return false;
			case ')':
				if (fPartitioning.getDefaultPartitionConstraint().matches(partitionType)) {
					command.text = ")"; //$NON-NLS-1$
					smartIndentOnFirstLineCharDefault2(command); // required?
					break;
				}
				return false;
			case '[':
				if (fPartitioning.getDefaultPartitionConstraint().matches(partitionType)) {
					command.text = "["; //$NON-NLS-1$
					if (fOptions.isSmartSquareBracketsEnabled() && !isValueChar(cEnd)) {
						if (!isClosedBracket(command.offset, cEnd, 2)) {
							command.text = "[]"; //$NON-NLS-1$
							if (countBackward('[', command.offset) % 2 == 1 && isCharAt(cEnd, ']')) {
								linkedModeType = 2;
							}
							else {
								linkedModeType = 1;
							}
						}
						else if (isCharAt(cEnd, ']')) {
							linkedModeType = 1;
						}
					}
					break;
				}
				return false;
			case '%':
				if (fPartitioning.getDefaultPartitionConstraint().matches(partitionType)
						&& fOptions.isSmartSpecialPercentEnabled()) {
					final IRegion line = fDocument.getLineInformationOfOffset(cEnd);
					fScanner.configure(fDocument, IRDocumentPartitions.R_INFIX_OPERATOR);
					if (fScanner.count(cEnd, line.getOffset()+line.getLength(), '%') % 2 == 0) {
						command.text = "%%"; //$NON-NLS-1$
						linkedModeType = 1;
						break;
					}
				}
				return false;
			case '"':
			case '\'':
			case '`':
				if (fPartitioning.getDefaultPartitionConstraint().matches(partitionType)
						&& fOptions.isSmartStringsEnabled()) {
					final IRegion line = fDocument.getLineInformationOfOffset(cEnd);
					if (!isValueChar(cEnd) && !isValueChar(command.offset-1)
							&& !isClosedString(cEnd, line.getOffset()+line.getLength(), false, c)) {
						command.text = new String(new char[] { c, c });
						linkedModeType = 1;
						break;
					}
				}
				return false;
			case '\n':
				if (fPartitioning.getDefaultPartitionConstraint().matches(partitionType)
						|| partitionType == IRDocumentPartitions.R_COMMENT) {
					command.text = TextUtilities.getDefaultLineDelimiter(fDocument);
					smartIndentOnNewLine(command);
					break;
				}
				else if (partitionType == IRDocumentPartitions.R_ROXYGEN) {
					command.text = TextUtilities.getDefaultLineDelimiter(fDocument);
					smartIndentAfterNewLine1(command);
					break;
				}
				return false;
			case '#':
 				if (fPartitioning.getDefaultPartitionConstraint().matches(partitionType)
						&& isAfterRoxygen(command.offset)) {
					command.text = "#' "; //$NON-NLS-1$
					break;
				}
				return false;
			}
			
			if (command.text.length() > 0 && fEditor.isEditable(true)) {
				fViewer.getTextWidget().setRedraw(false);
				try {
					fDocument.replace(command.offset, command.length, command.text);
					final int cursor = (command.caretOffset >= 0) ? command.caretOffset :
							command.offset+command.text.length();
					selection = new TextSelection(fDocument, cursor, 0);
					fViewer.setSelection(selection, true);
					
					if (linkedModeType >= 0) {
						if (linkedModeOffset < 0) {
							linkedModeOffset = command.offset;
						}
						createLinkedMode(linkedModeOffset, c, linkedModeType).enter();
					}
				}
				finally {
					fViewer.getTextWidget().setRedraw(true);
				}
			}
			return true;
		}
		catch (final Exception e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while customize key pressed in R auto edit strategy.", e); //$NON-NLS-1$
		}
		finally {
			fIgnoreCommands = false;
			quitCustomization();
		}
		return false;
	}
	
	/**
	 * Generic method to indent lines using the RSourceIndenter, called algorithm 2.
	 * @param c handle to read and save the document informations
	 * @param indentCurrentLine
	 * @param setCaret positive values indicates the line to set the caret
	 * @param traceCursor offset to update and return (offset at state after insertion of c.text)
	 */
	private Position[] smartIndentLine2(final DocumentCommand c, final boolean indentCurrentLine, final int setCaret, final Position[] tracePos) throws BadLocationException, BadPartitioningException, CoreException {
		if (fEditor3 == null) {
			return tracePos;
		}
		final IRegion validRegion = fValidRange;
		
		// new algorithm using RSourceIndenter
		final int cEnd = c.offset+c.length;
		if (cEnd > validRegion.getOffset()+validRegion.getLength()) {
			return tracePos;
		}
		fScanner.configure(fDocument);
		final int smartEnd;
		final String smartAppend;
		if (endsWithNewLine(c.text)) {
			final IRegion cEndLine = fDocument.getLineInformationOfOffset(cEnd);
			final int validEnd = (cEndLine.getOffset()+cEndLine.getLength() <= validRegion.getOffset()+validRegion.getLength()) ?
					cEndLine.getOffset()+cEndLine.getLength() : validRegion.getOffset()+validRegion.getLength();
			final int next = fScanner.findAnyNonBlankForward(cEnd, validEnd, false);
			smartEnd = (next >= 0) ? next : validEnd;
			switch(fScanner.getChar()) {
			case '}':
			case '{':
			case '|':
			case '&':
				smartAppend = ""; //$NON-NLS-1$
				break;
			default:
				smartAppend = "DUMMY+"; //$NON-NLS-1$
				break;
			}
		}
		else {
			smartEnd = cEnd;
			smartAppend = ""; //$NON-NLS-1$
		}
		
		int shift = 0;
		if (c.offset < validRegion.getOffset()
				|| c.offset > validRegion.getOffset()+validRegion.getLength()) {
			return tracePos;
		}
		if (c.offset > 2500) {
			final int line = fDocument.getLineOfOffset(c.offset)-30;
			if (line >= 5) {
				shift = fDocument.getLineOffset(line);
				final ITypedRegion partition = fDocument.getPartition(fPartitioning.getPartitioning(), shift, true);
				if (!fPartitioning.getDefaultPartitionConstraint().matches(partition.getType())) {
					shift = partition.getOffset();
				}
			}
		}
		if (shift < validRegion.getOffset()) {
			shift = validRegion.getOffset();
		}
		int dummyDocEnd = cEnd+1500;
		if (dummyDocEnd > validRegion.getOffset()+validRegion.getLength()) {
			dummyDocEnd = validRegion.getOffset()+validRegion.getLength();
		}
		final String text;
		{	final StringBuilder s = new StringBuilder(
					(c.offset-shift) +
					c.text.length() +
					(smartEnd-cEnd) +
					smartAppend.length() +
					(dummyDocEnd-smartEnd) );
			s.append(fDocument.get(shift, c.offset-shift));
			s.append(c.text);
			if (smartEnd-cEnd > 0) {
				s.append(fDocument.get(cEnd, smartEnd-cEnd));
			}
			s.append(smartAppend);
			s.append(fDocument.get(smartEnd, dummyDocEnd-smartEnd));
			text = s.toString();
		}
		
		// Create temp doc to compute indent
		int dummyCoffset = c.offset-shift;
		int dummyCend = dummyCoffset+c.text.length();
		final AbstractDocument dummyDoc = new Document(text);
		final StringParseInput parseInput = new StringParseInput(text);
		
		// Lines to indent
		int dummyFirstLine = dummyDoc.getLineOfOffset(dummyCoffset);
		final int dummyLastLine = dummyDoc.getLineOfOffset(dummyCend);
		if (!indentCurrentLine) {
			dummyFirstLine++;
		}
		if (dummyFirstLine > dummyLastLine) {
			return tracePos;
		}
		
		// Compute indent
		final RAstInfo ast = new RAstInfo(RAst.LEVEL_MINIMAL, 0);
		final RScanner scanner = new RScanner(parseInput, ast);
		ast.root = scanner.scanSourceUnit();
		fIndenter.setup(fRCoreAccess);
		final TextEdit edit = fIndenter.getIndentEdits(dummyDoc, ast, 0, dummyFirstLine, dummyLastLine);
		
		// Apply indent to temp doc
		final Position cPos = new Position(dummyCoffset, c.text.length());
		dummyDoc.addPosition(cPos);
		if (tracePos != null) {
			for (int i = 0; i < tracePos.length; i++) {
				tracePos[i].offset -= shift;
				dummyDoc.addPosition(tracePos[i]);
			}
		}
		
		c.length = c.length+edit.getLength()
				// add space between two replacement regions
				// minus overlaps with c.text
				-TextUtil.overlaps(edit.getOffset(), edit.getExclusiveEnd(), dummyCoffset, dummyCend);
		if (edit.getOffset() < dummyCoffset) { // move offset, if edit begins before c
			dummyCoffset = edit.getOffset();
			c.offset = shift+dummyCoffset;
		}
		edit.apply(dummyDoc, TextEdit.NONE);
		
		// Read indent for real doc
		int dummyChangeEnd = edit.getExclusiveEnd();
		dummyCend = cPos.getOffset()+cPos.getLength();
		if (!cPos.isDeleted && dummyCend > dummyChangeEnd) {
			dummyChangeEnd = dummyCend;
		}
		c.text = dummyDoc.get(dummyCoffset, dummyChangeEnd-dummyCoffset);
		if (setCaret != 0) {
			c.caretOffset = shift+fIndenter.getNewIndentOffset(dummyFirstLine+setCaret-1);
			c.shiftsCaret = false;
		}
		fIndenter.clear();
		if (tracePos != null) {
			for (int i = 0; i < tracePos.length; i++) {
				tracePos[i].offset += shift;
			}
		}
		return tracePos;
	}
	
	private final boolean endsWithNewLine(final String text) {
		for (int i = text.length()-1; i >= 0; i--) {
			final char c = text.charAt(i);
			if (c == '\r' || c == '\n') {
				return true;
			}
			if (c != ' ' && c != '\t') {
				return false;
			}
		}
		return false;
	}
	
	
	private void smartIndentOnNewLine(final DocumentCommand c) throws BadLocationException, BadPartitioningException, CoreException {
		final int before = c.offset - 1;
		final int behind = c.offset+c.length;
		if (before >= 0 && behind < fValidRange.getOffset()+fValidRange.getLength()
				&& fDocument.getChar(before) == '{' && fDocument.getChar(behind) == '}') {
			c.text = c.text+c.text;
		}
		try {
			smartIndentLine2(c, false, 1, null);
		}
		catch (final Exception e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while customize a command in R auto edit strategy (algorithm 2).", e); //$NON-NLS-1$
			smartIndentAfterNewLine1(c);
		}
	}
	
	private void smartIndentAfterNewLine1(final DocumentCommand c) throws BadLocationException, BadPartitioningException, CoreException {
		final int line = fDocument.getLineOfOffset(c.offset);
		int checkOffset = Math.max(0, c.offset);
		String append = "";
		final ITypedRegion partition = fDocument.getPartition(fPartitioning.getPartitioning(), checkOffset, true);
		if (partition.getType() == IRDocumentPartitions.R_COMMENT) {
			checkOffset = partition.getOffset();
		}
		else if (partition.getType() == IRDocumentPartitions.R_ROXYGEN) {
			checkOffset = -1;
			if (c.length == 0 && line+1 < fDocument.getNumberOfLines()) {
				int offset = fDocument.getLineOffset(line+1);
				fScanner.configure(fDocument);
				int next = fScanner.findAnyNonBlankForward(offset, ITokenScanner.UNBOUND, true);
				if (next >= 0 && fScanner.getPartition(next).getType() == IRDocumentPartitions.R_ROXYGEN) {
					append = "#' ";
				}
			}
			fScanner.configure(fDocument);
		}
		final RIndentUtil util = new RIndentUtil(fDocument, fRCodeStyle);
		int column = util.getLineIndent(line, false)[RIndentUtil.COLUMN_IDX];
		if (checkOffset > 0) {
			// new block?:
			fScanner.configure(fDocument);
			final int match = fScanner.findAnyNonBlankBackward(checkOffset, fDocument.getLineOffset(line)-1, false);
			if (match >= 0 && fDocument.getChar(match) == '{') {
				column = util.getNextLevelColumn(column, 1);
			}
		}
		c.text += util.createIndentString(column) + append;
	}
	
	private void smartIndentOnTab(final DocumentCommand c) throws BadLocationException {
		if (fRCodeStyle.getIndentDefaultType() != IndentationType.SPACES) {
			return;
		}
		final int lineOffset = fDocument.getLineOffset(fDocument.getLineOfOffset(c.offset));
		fScanner.configure(fDocument);
		if (fScanner.findAnyNonBlankBackward(c.offset, lineOffset-1, false) != ITokenScanner.NOT_FOUND) {
			// not first char
			return;
		}
		final RIndentUtil indentation = new RIndentUtil(fDocument, fRCodeStyle);
		final int column = indentation.getColumnAtOffset(c.offset);
		c.text = indentation.createIndentCompletionString(column);
	}
	
	private void smartIndentOnClosingBracket(final DocumentCommand c) throws BadLocationException {
		final int lineOffset = fDocument.getLineOffset(fDocument.getLineOfOffset(c.offset));
		fScanner.configure(fDocument);
		if (fScanner.findAnyNonBlankBackward(c.offset, lineOffset-1, false) != ITokenScanner.NOT_FOUND) {
			// not first char
			return;
		}
		
		try {
			final Position cursorPos = new Position(c.offset+1, 0);
			smartIndentLine2(c, true, 0, new Position[] { cursorPos });
			c.caretOffset = cursorPos.getOffset();
			return;
		}
		catch (final Exception e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while customize a command in R auto edit strategy (algorithm 2).", e); //$NON-NLS-1$
			smartIndentOnClosingBracket1(c);
		}
	}
	
	private void smartIndentOnClosingBracket1(final DocumentCommand c) throws BadLocationException {
		final int lineOffset = fDocument.getLineOffset(fDocument.getLineOfOffset(c.offset));
		final int blockStart = fScanner.findOpeningPeer(lineOffset, CURLY_BRACKETS);
		if (blockStart == ITokenScanner.NOT_FOUND) {
			return;
		}
		final RIndentUtil util = new RIndentUtil(fDocument, fRCodeStyle);
		final int column = util.getLineIndent(fDocument.getLineOfOffset(blockStart), false)[RIndentUtil.COLUMN_IDX];
		c.text = util.createIndentString(column) + c.text;
		c.length += c.offset - lineOffset;
		c.offset = lineOffset;
	}
	
	private int smartIndentOnFirstLineCharDefault2(final DocumentCommand c) throws BadLocationException {
		final int lineOffset = fDocument.getLineOffset(fDocument.getLineOfOffset(c.offset));
		fScanner.configure(fDocument);
		if (fScanner.findAnyNonBlankBackward(c.offset, lineOffset-1, false) != ITokenScanner.NOT_FOUND) {
			// not first char
			return c.offset;
		}
		
		try {
			final Position cursorPos = new Position(c.offset+1, 0);
			smartIndentLine2(c, true, 0, new Position[] { cursorPos });
			return (c.caretOffset = cursorPos.getOffset()) -1;
		}
		catch (final Exception e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while customize a command in R auto edit strategy (algorithm 2).", e); //$NON-NLS-1$
			return -1;
		}
	}
	
	private void smartPaste(final DocumentCommand c) throws BadLocationException {
		final int lineOffset = fDocument.getLineOffset(fDocument.getLineOfOffset(c.offset));
		fScanner.configure(fDocument);
		final boolean firstLine = (fScanner.findAnyNonBlankBackward(c.offset, lineOffset-1, false) == ITokenScanner.NOT_FOUND);
		try {
			smartIndentLine2(c, firstLine, 0, null);
		}
		catch (final Exception e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while customize a command in R auto edit strategy (algorithm 2).", e); //$NON-NLS-1$
		}
	}
	
	
	private LinkedModeUI createLinkedMode(int offset, final char type, final int stopChars) throws BadLocationException {
		int pos = 0;
		offset++;
		final LinkedPositionGroup group1 = new LinkedPositionGroup();
		final LinkedPosition position = new LinkedPosition(fDocument, offset, pos++);
		group1.addPosition(position);
		
		/* set up linked mode */
		final LinkedModeModel model = new LinkedModeModel();
		model.addGroup(group1);
		model.forceInstall();
		
		final BracketLevel level = RBracketLevel.createBracketLevel(type, fDocument, fPartitioning, position, (fViewer instanceof InputSourceViewer));
		
		/* create UI */
		final LinkedModeUI ui = new LinkedModeUI(model, fViewer);
		ui.setCyclingMode(LinkedModeUI.CYCLE_NEVER);
		ui.setExitPosition(fViewer, offset+stopChars, 0, pos);
		ui.setSimpleMode(true);
		ui.setExitPolicy(level);
		return ui;
	}
	
}
