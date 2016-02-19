/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.sourceediting;

import static de.walware.ecommons.text.ui.BracketLevel.AUTODELETE;
import static de.walware.statet.r.core.source.IRDocumentConstants.R_DEFAULT_CONTENT_CONSTRAINT;
import static de.walware.statet.r.core.source.RHeuristicTokenScanner.CURLY_BRACKET_TYPE;
import static de.walware.statet.r.core.source.RHeuristicTokenScanner.ROUND_BRACKET_TYPE;
import static de.walware.statet.r.core.source.RHeuristicTokenScanner.SQUARE_BRACKET_TYPE;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.texteditor.ITextEditorExtension3;

import de.walware.jcommons.collections.ImCollections;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.ui.sourceediting.ISmartInsertSettings;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorAddon;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1;
import de.walware.ecommons.text.ITokenScanner;
import de.walware.ecommons.text.IndentUtil;
import de.walware.ecommons.text.TextUtil;
import de.walware.ecommons.text.core.input.StringParserInput;
import de.walware.ecommons.text.core.input.TextParserInput;
import de.walware.ecommons.text.core.sections.IDocContentSections;
import de.walware.ecommons.text.core.treepartitioner.TreePartition;
import de.walware.ecommons.text.ui.BracketLevel.InBracketPosition;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.ui.console.InputSourceViewer;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.rsource.RSourceIndenter;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.RScanner;
import de.walware.statet.r.core.source.IRDocumentConstants;
import de.walware.statet.r.core.source.RHeuristicTokenScanner;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.editors.REditorOptions;


/**
 * Auto edit strategy for R code:
 *  - auto indent on keys
 *  - special indent with tab key
 *  - auto indent on paste
 *  - auto close of pairs
 */
public class RAutoEditStrategy extends DefaultIndentLineAutoEditStrategy
		implements ISourceEditorAddon {
	
	private static final char[] CURLY_BRACKETS= new char[] { '{', '}' };
	
	private static final StringParserInput DEFAULT_PARSER_INPUT= new StringParserInput();
	
	
	private class RealTypeListener implements VerifyKeyListener {
		@Override
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
				event.doit= !customizeKeyPressed(event.character);
				return;
			case '\t':
				if (event.stateMask == 0) {
					event.doit= !customizeKeyPressed(event.character);
				}
				return;
			case 0x0A:
			case 0x0D:
				if (RAutoEditStrategy.this.editor3 != null) {
					event.doit= !customizeKeyPressed('\n');
				}
				return;
			default:
				return;
			}
		}
	};
	
	
	private final ISourceEditor editor;
	private final ITextEditorExtension3 editor3;
	private final IDocContentSections documentContentInfo;
	private final SourceViewer viewer;
	private final RealTypeListener typeListener;
	
	private final IRCoreAccess rCoreAccess;
	private final REditorOptions editorOptions;
	
	private AbstractDocument document;
	private IRegion validRange;
	private RHeuristicTokenScanner scanner;
	private RCodeStyleSettings rCodeStyle;
	private RSourceIndenter indenter;
	
	private boolean ignoreCommands= false;
	
	
	public RAutoEditStrategy(final IRCoreAccess rCoreAccess, final ISourceEditor editor) {
		assert (rCoreAccess != null);
		assert (editor != null);
		
		this.editor= editor;
		this.documentContentInfo= editor.getDocumentContentInfo();
		
		this.rCoreAccess= rCoreAccess;
		this.editorOptions= RUIPlugin.getDefault().getREditorSettings(rCoreAccess.getPrefs());
		assert (this.editorOptions != null);
		
		this.viewer= this.editor.getViewer();
		this.editor3= (editor instanceof SourceEditor1) ? (SourceEditor1) editor : null;
		this.typeListener= new RealTypeListener();
	}
	
	@Override
	public void install(final ISourceEditor editor) {
		assert (editor.getViewer() == this.viewer);
		this.viewer.prependVerifyKeyListener(this.typeListener);
	}
	
	@Override
	public void uninstall() {
		this.viewer.removeVerifyKeyListener(this.typeListener);
	}
	
	
	private final ITypedRegion initCustomization(final int offset, final int c)
			throws BadLocationException, BadPartitioningException {
		assert(this.document != null);
		if (this.scanner == null) {
			this.scanner= createScanner();
		}
		this.rCodeStyle= this.rCoreAccess.getRCodeStyle();
		
		final ITypedRegion partition= this.document.getPartition(
				this.scanner.getDocumentPartitioning(), offset, true );
		// InputDocument of console does not (yet) return a TreePartition
		this.validRange= (partition instanceof TreePartition) ?
				getValidRange(offset, (TreePartition) partition, c) :
				new Region(0, this.document.getLength());
		return (this.validRange != null) ? partition : null;
	}
	
	protected RHeuristicTokenScanner createScanner() {
		return RHeuristicTokenScanner.create(this.documentContentInfo);
	}
	
	protected IRegion getValidRange(final int offset, final TreePartition partition, final int c) {
		return new Region(0, this.document.getLength());
	}
	
	protected final IDocument getDocument() {
		return this.document;
	}
	
	protected final IDocContentSections getDocumentContentInfo() {
		return this.documentContentInfo;
	}
	
	private final void quitCustomization() {
		this.document= null;
		this.rCodeStyle= null;
	}
	
	
	private final boolean isSmartInsertEnabled() {
		return ((this.editor3 != null) ?
				(this.editor3.getInsertMode() == ITextEditorExtension3.SMART_INSERT) :
				this.editorOptions.isSmartInsertEnabledByDefault() );
	}
	
	private final boolean isBlockSelection() {
		final StyledText textWidget= this.viewer.getTextWidget();
		return (textWidget.getBlockSelection() && textWidget.getSelectionRanges().length > 2);
	}
	
	private final boolean isClosedBracket(final int backwardOffset, final int forwardOffset, final int searchType) {
		int[] balance= new int[3];
		balance[searchType]++;
		this.scanner.configureDefaultParitions(this.document);
		balance= this.scanner.computeBracketBalance(backwardOffset, forwardOffset, balance, searchType);
		return (balance[searchType] <= 0);
	}
	
	private final boolean isClosedString(int offset, final int end, final boolean endVirtual, final char sep) {
		this.scanner.configure(this.document);
		boolean in= true; // we start always inside after a sep
		final char[] chars= new char[] { sep, '\\' };
		while (offset < end) {
			offset= this.scanner.scanForward(offset, end, chars);
			if (offset == RHeuristicTokenScanner.NOT_FOUND) {
				offset= end;
				break;
			}
			offset++;
			if (this.scanner.getChar() == '\\') {
				offset++;
			}
			else {
				in= !in;
			}
		}
		return (offset == end) && (!in ^ endVirtual);
	}
	
	private boolean isCharAt(final int offset, final char c) throws BadLocationException {
		return (offset >= this.validRange.getOffset() && offset < this.validRange.getOffset()+this.validRange.getLength()
				&& this.document.getChar(offset) == c);
	}
	
	private boolean isValueChar(final int offset) throws BadLocationException {
		if (offset >= this.validRange.getOffset() && offset < this.validRange.getOffset()+this.validRange.getLength()) {
			final int c= this.document.getChar(offset);
			return (c == '"' || c == '\'' || c == '`' || Character.isLetterOrDigit(c));
		}
		return false;
	}
	
	private boolean isAfterRoxygen(final int offset) throws BadLocationException {
		this.scanner.configure(this.document);
		final int line= this.document.getLineOfOffset(offset);
		if (line > 0 && this.scanner.findAnyNonBlankBackward(offset, this.document.getLineOffset(line)-1, false) == ITokenScanner.NOT_FOUND) {
			final IRegion prevLineInfo= this.document.getLineInformation(line-1);
			if (prevLineInfo.getLength() > 0 && TextUtilities.getPartition(this.document,
					this.scanner.getDocumentPartitioning(),
					prevLineInfo.getOffset()+prevLineInfo.getLength()-1, false).getType() == IRDocumentConstants.R_ROXYGEN_CONTENT_TYPE) {
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public void customizeDocumentCommand(final IDocument d, final DocumentCommand c) {
		if (this.ignoreCommands || c.doit == false || c.text == null) {
			return;
		}
		if (!isSmartInsertEnabled() || isBlockSelection()) {
			super.customizeDocumentCommand(d, c);
			return;
		}
		
		try {
			this.document= (AbstractDocument) d;
			final ITypedRegion partition= initCustomization(c.offset, -1);
			if (partition == null) {
				return;
			}
			final String contentType= partition.getType();
			
			if (R_DEFAULT_CONTENT_CONSTRAINT.matches(contentType)) {
				if (c.length == 0 && TextUtilities.equals(d.getLegalLineDelimiters(), c.text) != -1) {
					smartIndentOnNewLine(c, contentType);
				}
				else if (c.text.length() > 1 && this.editorOptions.isSmartPasteEnabled()) {
					smartPaste(c);
				}
			}
		}
		catch (final Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					"An error occurred when customizing action for document command in R auto edit strategy.", e )); //$NON-NLS-1$
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
		if (!isSmartInsertEnabled() || !UIAccess.isOkToUse(this.viewer) || isBlockSelection()) {
			return false;
		}
		
		try {
			this.document= (AbstractDocument) this.viewer.getDocument();
			ITextSelection selection= (ITextSelection) this.viewer.getSelection();
			final ITypedRegion partition= initCustomization(selection.getOffset(), c);
			if (partition == null) {
				return false;
			}
			final String contentType= partition.getType();
			this.ignoreCommands= true;
			
			final DocumentCommand command= new DocumentCommand() {};
			command.offset= selection.getOffset();
			command.length= selection.getLength();
			command.doit= true;
			command.shiftsCaret= true;
			command.caretOffset= -1;
			int linkedMode= -1;
			int linkedModeOffset= -1;
			boolean contextInfo= false;
			final int cEnd= command.offset + command.length;
			
			KEY: switch (c) {
			case '\t':
				if (R_DEFAULT_CONTENT_CONSTRAINT.matches(contentType)
						|| contentType == IRDocumentConstants.R_COMMENT_CONTENT_TYPE
						|| contentType == IRDocumentConstants.R_ROXYGEN_CONTENT_TYPE ) {
					if (command.length == 0 || this.document.getLineOfOffset(command.offset) == this.document.getLineOfOffset(cEnd)) {
						command.text= "\t"; //$NON-NLS-1$
						switch (smartIndentOnTab(command)) {
						case -1:
							return false;
						case 0:
							break;
						case 1:
							break KEY;
						}
						
						if (this.rCodeStyle.getReplaceOtherTabsWithSpaces()) {
							final IndentUtil indentation= new IndentUtil(this.document, this.rCodeStyle);
							command.text= indentation.createTabSpacesCompletionString(indentation.getColumn(command.offset));
							break KEY;
						}
					}
				}
				return false;
			case '}':
				if (R_DEFAULT_CONTENT_CONSTRAINT.matches(contentType)) {
					command.text= "}"; //$NON-NLS-1$
					smartIndentOnClosingBracket(command);
					break KEY;
				}
				return false;
			case '{':
				if (R_DEFAULT_CONTENT_CONSTRAINT.matches(contentType)) {
					command.text= "{"; //$NON-NLS-1$
					if (this.editorOptions.isSmartCurlyBracketsEnabled() && !isValueChar(cEnd)) {
						if (!isClosedBracket(command.offset, cEnd, CURLY_BRACKET_TYPE)) {
							command.text= "{}"; //$NON-NLS-1$
							linkedMode= 1 | AUTODELETE;
						}
						else if (isCharAt(cEnd, '}')) {
							linkedMode= 1;
						}
					}
					linkedModeOffset= smartIndentOnFirstLineCharDefault2(command);
					break KEY;
				}
				return false;
			case '(':
				if (R_DEFAULT_CONTENT_CONSTRAINT.matches(contentType)) {
					command.text= "("; //$NON-NLS-1$
					if (this.editorOptions.isSmartRoundBracketsEnabled() && !isValueChar(cEnd)) {
						if (!isClosedBracket(command.offset, cEnd, ROUND_BRACKET_TYPE)) {
							command.text= "()"; //$NON-NLS-1$
							linkedMode= 2 | AUTODELETE;
						}
						else if (isCharAt(cEnd, ')')) {
							linkedMode= 2;
						}
					}
					if (isValueChar(command.offset - 1)) {
						contextInfo= true;
					}
					break KEY;
				}
				return false;
			case ')':
				if (R_DEFAULT_CONTENT_CONSTRAINT.matches(contentType)) {
					command.text= ")"; //$NON-NLS-1$
					smartIndentOnFirstLineCharDefault2(command); // required?
					break KEY;
				}
				return false;
			case '[':
				if (R_DEFAULT_CONTENT_CONSTRAINT.matches(contentType)) {
					command.text= "["; //$NON-NLS-1$
					if (this.editorOptions.isSmartSquareBracketsEnabled() && !isValueChar(cEnd)) {
						if (!isClosedBracket(command.offset, cEnd, SQUARE_BRACKET_TYPE)) {
							command.text= "[]"; //$NON-NLS-1$
							if (TextUtil.countBackward(this.document, command.offset, '[') % 2 == 1
									&& isCharAt(cEnd, ']') ) {
								linkedMode= 3 | AUTODELETE;
							}
							else {
								linkedMode= 2 | AUTODELETE;
							}
						}
						else if (isCharAt(cEnd, ']')) {
							linkedMode= 2;
						}
					}
					break KEY;
				}
				return false;
			case '%':
				if (R_DEFAULT_CONTENT_CONSTRAINT.matches(contentType)
						&& this.editorOptions.isSmartSpecialPercentEnabled()) {
					final IRegion line= this.document.getLineInformationOfOffset(cEnd);
					this.scanner.configure(this.document, IRDocumentConstants.R_INFIX_OPERATOR_CONTENT_TYPE);
					if (this.scanner.count(cEnd, line.getOffset()+line.getLength(), '%') % 2 == 0) {
						command.text= "%%"; //$NON-NLS-1$
						linkedMode= 2 | AUTODELETE;
						break KEY;
					}
				}
				return false;
			case '"':
			case '\'':
			case '`':
				if (R_DEFAULT_CONTENT_CONSTRAINT.matches(contentType)
						&& this.editorOptions.isSmartStringsEnabled()
						&& !isValueChar(cEnd) && !isValueChar(command.offset - 1) ) {
					final IRegion line= this.document.getLineInformationOfOffset(cEnd);
					if (!isClosedString(cEnd, line.getOffset() + line.getLength(), false, c)) {
						command.text= new String(new char[] { c, c });
						linkedMode= 2 | AUTODELETE;
						break KEY;
					}
				}
				return false;
			case '\n':
				if (R_DEFAULT_CONTENT_CONSTRAINT.matches(contentType)
						|| contentType == IRDocumentConstants.R_COMMENT_CONTENT_TYPE) {
					command.text= TextUtilities.getDefaultLineDelimiter(this.document);
					smartIndentOnNewLine(command, contentType);
					break KEY;
				}
				else if (contentType == IRDocumentConstants.R_ROXYGEN_CONTENT_TYPE) {
					command.text= TextUtilities.getDefaultLineDelimiter(this.document);
					smartIndentAfterNewLine1(command, command.text);
					break KEY;
				}
				return false;
			case '#':
 				if (R_DEFAULT_CONTENT_CONSTRAINT.matches(contentType)
						&& isAfterRoxygen(command.offset)) {
					command.text= "#' "; //$NON-NLS-1$
					break KEY;
				}
				return false;
			default:
				assert (false);
				return false;
			}
			
			if (command.text.length() > 0 && this.editor.isEditable(true)) {
				this.viewer.getTextWidget().setRedraw(false);
				try {
					this.document.replace(command.offset, command.length, command.text);
					final int cursor= (command.caretOffset >= 0) ? command.caretOffset :
							command.offset+command.text.length();
					selection= new TextSelection(this.document, cursor, 0);
					this.viewer.setSelection(selection, true);
					
					if (linkedMode >= 0) {
						if (linkedModeOffset < 0) {
							linkedModeOffset= command.offset;
						}
						createLinkedMode(linkedModeOffset, c, linkedMode).enter();
					}
				}
				finally {
					this.viewer.getTextWidget().setRedraw(true);
				}
				
				if (contextInfo
						&& this.viewer.canDoOperation(ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION)) {
					viewer.doOperation(ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION);
				}
			}
			return true;
		}
		catch (final Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					"An error occurred when customizing action for pressed key in R auto edit strategy.", e )); //$NON-NLS-1$
		}
		finally {
			this.ignoreCommands= false;
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
		if (this.editor3 == null) {
			return tracePos;
		}
		final IRegion validRegion= this.validRange;
		
		// new algorithm using RSourceIndenter
		final int cEnd= c.offset+c.length;
		if (cEnd > validRegion.getOffset()+validRegion.getLength()) {
			return tracePos;
		}
		this.scanner.configure(this.document);
		final int smartEnd;
		final String smartAppend;
		if (endsWithNewLine(c.text)) {
			final IRegion cEndLine= this.document.getLineInformationOfOffset(cEnd);
			final int validEnd= (cEndLine.getOffset()+cEndLine.getLength() <= validRegion.getOffset()+validRegion.getLength()) ?
					cEndLine.getOffset()+cEndLine.getLength() : validRegion.getOffset()+validRegion.getLength();
			final int next= this.scanner.findAnyNonBlankForward(cEnd, validEnd, false);
			smartEnd= (next >= 0) ? next : validEnd;
			switch(this.scanner.getChar()) {
			case '}':
			case '{':
			case '|':
			case '&':
				smartAppend= ""; //$NON-NLS-1$
				break;
			default:
				smartAppend= "DUMMY+"; //$NON-NLS-1$
				break;
			}
		}
		else {
			smartEnd= cEnd;
			smartAppend= ""; //$NON-NLS-1$
		}
		
		int shift= 0;
		if (c.offset < validRegion.getOffset()
				|| c.offset > validRegion.getOffset()+validRegion.getLength()) {
			return tracePos;
		}
		if (c.offset > 2500) {
			final int line= this.document.getLineOfOffset(c.offset) - 40;
			if (line >= 10) {
				shift= this.document.getLineOffset(line);
				final ITypedRegion partition= this.document.getPartition(
						this.scanner.getDocumentPartitioning(), shift, true );
				if (!R_DEFAULT_CONTENT_CONSTRAINT.matches(partition.getType())) {
					shift= partition.getOffset();
				}
			}
		}
		if (shift < validRegion.getOffset()) {
			shift= validRegion.getOffset();
		}
		int dummyDocEnd= cEnd+1500;
		if (dummyDocEnd > validRegion.getOffset()+validRegion.getLength()) {
			dummyDocEnd= validRegion.getOffset()+validRegion.getLength();
		}
		final String text;
		{	final StringBuilder s= new StringBuilder(
					(c.offset-shift) +
					c.text.length() +
					(smartEnd-cEnd) +
					smartAppend.length() +
					(dummyDocEnd-smartEnd) );
			s.append(this.document.get(shift, c.offset-shift));
			s.append(c.text);
			if (smartEnd-cEnd > 0) {
				s.append(this.document.get(cEnd, smartEnd-cEnd));
			}
			s.append(smartAppend);
			s.append(this.document.get(smartEnd, dummyDocEnd-smartEnd));
			text= s.toString();
		}
		
		// Create temp doc to compute indent
		int dummyCoffset= c.offset-shift;
		int dummyCend= dummyCoffset+c.text.length();
		final AbstractDocument dummyDoc= new Document(text);
		final TextParserInput parserInput= (Display.getCurrent() == Display.getDefault()) ?
				DEFAULT_PARSER_INPUT.reset(text) : new StringParserInput(text);
		
		// Lines to indent
		int dummyFirstLine= dummyDoc.getLineOfOffset(dummyCoffset);
		final int dummyLastLine= dummyDoc.getLineOfOffset(dummyCend);
		if (!indentCurrentLine) {
			dummyFirstLine++;
		}
		if (dummyFirstLine > dummyLastLine) {
			return tracePos;
		}
		
		// Compute indent
		final RScanner scanner= new RScanner(AstInfo.LEVEL_MINIMAL);
		final RAstNode rootNode= scanner.scanSourceUnit(parserInput.init());
		if (this.indenter == null) {
			this.indenter= new RSourceIndenter(this.scanner);
		}
		this.indenter.setup(this.rCoreAccess);
		final TextEdit edit= this.indenter.getIndentEdits(dummyDoc, rootNode, 0, dummyFirstLine, dummyLastLine);
		
		// Apply indent to temp doc
		final Position cPos= new Position(dummyCoffset, c.text.length());
		dummyDoc.addPosition(cPos);
		if (tracePos != null) {
			for (int i= 0; i < tracePos.length; i++) {
				tracePos[i].offset -= shift;
				dummyDoc.addPosition(tracePos[i]);
			}
		}
		
		c.length= c.length+edit.getLength()
				// add space between two replacement regions
				// minus overlaps with c.text
				-TextUtil.overlaps(edit.getOffset(), edit.getExclusiveEnd(), dummyCoffset, dummyCend);
		if (edit.getOffset() < dummyCoffset) { // move offset, if edit begins before c
			dummyCoffset= edit.getOffset();
			c.offset= shift+dummyCoffset;
		}
		edit.apply(dummyDoc, TextEdit.NONE);
		
		// Read indent for real doc
		int dummyChangeEnd= edit.getExclusiveEnd();
		dummyCend= cPos.getOffset()+cPos.getLength();
		if (!cPos.isDeleted && dummyCend > dummyChangeEnd) {
			dummyChangeEnd= dummyCend;
		}
		c.text= dummyDoc.get(dummyCoffset, dummyChangeEnd-dummyCoffset);
		if (setCaret != 0) {
			c.caretOffset= shift+this.indenter.getNewIndentOffset(dummyFirstLine+setCaret-1);
			c.shiftsCaret= false;
		}
		this.indenter.clear();
		if (tracePos != null) {
			for (int i= 0; i < tracePos.length; i++) {
				tracePos[i].offset += shift;
			}
		}
		return tracePos;
	}
	
	private final boolean endsWithNewLine(final String text) {
		for (int i= text.length()-1; i >= 0; i--) {
			final char c= text.charAt(i);
			if (c == '\r' || c == '\n') {
				return true;
			}
			if (c != ' ' && c != '\t') {
				return false;
			}
		}
		return false;
	}
	
	
	private void smartIndentOnNewLine(final DocumentCommand c, final String partitionType)
			throws BadLocationException, BadPartitioningException, CoreException {
		final int before= c.offset - 1;
		final int behind= c.offset + c.length;
		final String lineDelimiter= c.text;
		if (R_DEFAULT_CONTENT_CONSTRAINT.matches(partitionType)
				&& before >= 0 && behind < this.validRange.getOffset() + this.validRange.getLength()
				&& this.document.getChar(before) == '{' && this.document.getChar(behind) == '}') {
			c.text= c.text + c.text;
		}
		try {
			smartIndentLine2(c, false, 1, null);
		}
		catch (final Exception e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while customize a command in R auto edit strategy (algorithm 2).", e); //$NON-NLS-1$
			smartIndentAfterNewLine1(c, lineDelimiter);
		}
	}
	
	private void smartIndentAfterNewLine1(final DocumentCommand c, final String lineDelimiter)
			throws BadLocationException, BadPartitioningException, CoreException {
		final StringBuilder sb= new StringBuilder(c.text);
		int nlIndex= lineDelimiter.length();
		
		final int line= this.document.getLineOfOffset(c.offset);
		int checkOffset= Math.max(0, c.offset);
		
		final ITypedRegion partition= this.document.getPartition(
				this.scanner.getDocumentPartitioning(), checkOffset, true );
		if (partition.getType() == IRDocumentConstants.R_COMMENT_CONTENT_TYPE) {
			checkOffset= partition.getOffset();
		}
		else if (partition.getType() == IRDocumentConstants.R_ROXYGEN_CONTENT_TYPE) {
			checkOffset= -1;
			if (c.length == 0 && line + 1 < this.document.getNumberOfLines()) {
				final int offset= this.document.getLineOffset(line + 1);
				this.scanner.configure(this.document);
				final int next= this.scanner.findAnyNonBlankForward(offset, ITokenScanner.UNBOUND, true);
				if (next >= 0 && this.scanner.getPartition(next).getType() == IRDocumentConstants.R_ROXYGEN_CONTENT_TYPE) {
					sb.append("#' "); //$NON-NLS-1$
				}
			}
			this.scanner.configure(this.document);
		}
		
		final IndentUtil util= new IndentUtil(this.document, this.rCodeStyle);
		final int column= util.getLineIndent(line, false)[IndentUtil.COLUMN_IDX];
		
		if (checkOffset > 0) {
			// new block?:
			this.scanner.configure(this.document);
			final int match= this.scanner.findAnyNonBlankBackward(checkOffset, this.document.getLineOffset(line) - 1, false);
			if (match >= 0 && this.document.getChar(match) == '{') {
				final String indent= util.createIndentString(util.getNextLevelColumn(column, 1));
				sb.insert(nlIndex, indent);
				nlIndex+= indent.length() + lineDelimiter.length();
			}
		}
		
		if (nlIndex <= sb.length()) {
			sb.insert(nlIndex, util.createIndentString(column));
		}
		c.text= sb.toString();
	}
	
	private int smartIndentOnTab(final DocumentCommand c) throws BadLocationException {
		final IRegion line= this.document.getLineInformation(this.document.getLineOfOffset(c.offset));
		int first;
		this.scanner.configure(this.document);
		first= this.scanner.findAnyNonBlankBackward(c.offset, line.getOffset()-1, false);
		if (first != ITokenScanner.NOT_FOUND) { // not first char
			return 0;
		}
//		first= fScanner.findAnyNonBlankForward(c.offset, line.getOffset()+line.getLength(), false);
//		if (c.offset == line.getOffset() || c.offset != first) {
//			try {
//				final Position cursorPos= new Position(first, 0);
//				smartIndentLine2(c, true, 0, new Position[] { cursorPos });
//				c.caretOffset= cursorPos.getOffset();
//				return 1;
//			}
//			catch (final Exception e) {
//				RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while customize a command in R auto edit strategy (algorithm 2).", e); //$NON-NLS-1$
//				return -1;
//			}
//		}
		final IndentUtil indentation= new IndentUtil(this.document, this.rCodeStyle);
		final int column= indentation.getColumn(c.offset);
		if (this.editorOptions.getSmartInsertTabAction() != ISmartInsertSettings.TabAction.INSERT_TAB_CHAR) {
			c.text= indentation.createIndentCompletionString(column);
		}
		return 1;
	}
	
	private void smartIndentOnClosingBracket(final DocumentCommand c) throws BadLocationException {
		final int lineOffset= this.document.getLineOffset(this.document.getLineOfOffset(c.offset));
		this.scanner.configure(this.document);
		if (this.scanner.findAnyNonBlankBackward(c.offset, lineOffset-1, false) != ITokenScanner.NOT_FOUND) {
			// not first char
			return;
		}
		
		try {
			final Position cursorPos= new Position(c.offset+1, 0);
			smartIndentLine2(c, true, 0, new Position[] { cursorPos });
			c.caretOffset= cursorPos.getOffset();
			return;
		}
		catch (final Exception e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while customize a command in R auto edit strategy (algorithm 2).", e); //$NON-NLS-1$
			smartIndentOnClosingBracket1(c);
		}
	}
	
	private void smartIndentOnClosingBracket1(final DocumentCommand c) throws BadLocationException {
		final int lineOffset= this.document.getLineOffset(this.document.getLineOfOffset(c.offset));
		final int blockStart= this.scanner.findOpeningPeer(lineOffset, CURLY_BRACKETS);
		if (blockStart == ITokenScanner.NOT_FOUND) {
			return;
		}
		final IndentUtil util= new IndentUtil(this.document, this.rCodeStyle);
		final int column= util.getLineIndent(this.document.getLineOfOffset(blockStart), false)[IndentUtil.COLUMN_IDX];
		c.text= util.createIndentString(column) + c.text;
		c.length += c.offset - lineOffset;
		c.offset= lineOffset;
	}
	
	private int smartIndentOnFirstLineCharDefault2(final DocumentCommand c) throws BadLocationException {
		final int lineOffset= this.document.getLineOffset(this.document.getLineOfOffset(c.offset));
		this.scanner.configure(this.document);
		if (this.scanner.findAnyNonBlankBackward(c.offset, lineOffset-1, false) != ITokenScanner.NOT_FOUND) {
			// not first char
			return c.offset;
		}
		
		try {
			final Position cursorPos= new Position(c.offset+1, 0);
			smartIndentLine2(c, true, 0, new Position[] { cursorPos });
			return (c.caretOffset= cursorPos.getOffset()) -1;
		}
		catch (final Exception e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while customize a command in R auto edit strategy (algorithm 2).", e); //$NON-NLS-1$
			return -1;
		}
	}
	
	private void smartPaste(final DocumentCommand c) throws BadLocationException {
		final int lineOffset= this.document.getLineOffset(this.document.getLineOfOffset(c.offset));
		this.scanner.configure(this.document);
		final boolean firstLine= (this.scanner.findAnyNonBlankBackward(c.offset, lineOffset-1, false) == ITokenScanner.NOT_FOUND);
		try {
			smartIndentLine2(c, firstLine, 0, null);
		}
		catch (final Exception e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while customize a command in R auto edit strategy (algorithm 2).", e); //$NON-NLS-1$
		}
	}
	
	
	private LinkedModeUI createLinkedMode(final int offset, final char type, final int mode)
			throws BadLocationException {
		final LinkedModeModel model= new LinkedModeModel();
		int pos= 0;
		
		final LinkedPositionGroup group= new LinkedPositionGroup();
		final InBracketPosition position= RBracketLevel.createPosition(type, this.document,
				offset + 1, 0, pos++);
		group.addPosition(position);
		model.addGroup(group);
		
		model.forceInstall();
		
		final RBracketLevel level= new RBracketLevel(model,
				getDocument(), getDocumentContentInfo(),
				ImCollections.<LinkedPosition>newList(position), (mode & 0xffff0000) |
						((this.viewer instanceof InputSourceViewer) ? RBracketLevel.CONSOLE_MODE : 0) );
		
		/* create UI */
		final LinkedModeUI ui= new LinkedModeUI(model, this.viewer);
		ui.setCyclingMode(LinkedModeUI.CYCLE_NEVER);
		ui.setExitPosition(this.viewer, offset + (mode & 0xff), 0, pos);
		ui.setSimpleMode(true);
		ui.setExitPolicy(level);
		return ui;
	}
	
}
