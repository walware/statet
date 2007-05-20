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

package de.walware.statet.r.ui.text.r;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.TextEdit;

import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCodeStyleSettings.IndentationType;


/**
 *
 */
public class RIndentation {

	
	public static char[] repeat(char c, int n) {
		char[] chars = new char[n];
		Arrays.fill(chars, c);
		return chars;
	}
	
//	public static RIndentation createSave(IDocument document, RCodeStyleSettings style) {
//		Lock lock = style.getReadLock();
//		lock.lock();
//		try {
//			return new RIndentation(document, style);
//		}
//		finally {
//			lock.unlock();
//		}
//	}

	
	public abstract class IndentEditAction {
		
		private int fDepth;
		
		public IndentEditAction(int depth) {
			fDepth = depth;
		}
		public int getDepth() {
			return fDepth;
		}
		protected IDocument getDocument() {
			return fDocument;
		}
		public TextEdit createEdit(int offset) throws BadLocationException {
			return null;
		}
		public abstract TextEdit createEdit(int offset, int length, StringBuilder text) throws BadLocationException;
	}
	
	private IDocument fDocument;
	private int fTabWidth;
	private boolean fTabAsDefault;
	private int fNumOfSpaces;
	
	
	public RIndentation(IDocument document, RCodeStyleSettings style) {
		fDocument = document;
		fTabAsDefault = style.getIndentDefaultType() == IndentationType.TAB; 
		fTabWidth = style.getTabSize();
		fNumOfSpaces = style.getIndentSpacesCount();
	}
	
	/**
	 * Return the indentation depth of the specified line.
	 * 
	 * @param line line to check
	 * @param markBlankLine if true, empty lines have are marked with a depth of -1
	 * @return
	 * @throws BadLocationException
	 */
	public int getLineIndentationDepth(int line, boolean markBlankLine) throws BadLocationException {
		
		IRegion lineInfo = fDocument.getLineInformation(line);
		int indentation = 0;
		int offset = lineInfo.getOffset();
		ITERATE_CHAR : while (true) {
			int c = getDocumentChar(offset++);
			switch (c) {
			case ' ':
				indentation++;
				continue ITERATE_CHAR;
			case '\t':
				indentation = ((indentation/fTabWidth) * fTabWidth) + fTabWidth;
				continue ITERATE_CHAR;
			case '\r':
			case '\n':
			case -1:
				if (markBlankLine) {
					return -1;
				}
			default:
				return indentation;
			}
		}
	}
	
//	public int getIndentationDepth(String chars) throws BadLocationException {
//		
//		int indentation = 0;
//		ITERATE_CHARS : for (int i = 0; i < chars.length(); i++) {
//			char c = fDocument.getChar(i);
//			switch (c) {
//			case ' ':
//				indentation++;
//				continue ITERATE_CHARS;
//			case '\t':
//				indentation += fTabWidth;
//				continue ITERATE_CHARS;
//			default:
//				throw new IllegalArgumentException("No indentation char: '"+c+"'."); //$NON-NLS-1$ //$NON-NLS-2$
//			}
//		}
//		return indentation;
//	}
	
	/**
	 * Returns the common (min) indentation depth of all lines. Empty lines are ignored.
	 * @param startLine line index of first line
	 * @param endLine line index of last line
	 * @return
	 * @throws BadLocationException
	 */
	public int getMultilineIndentationDepth(int startLine, int endLine) throws BadLocationException {
		
		int indentation = Integer.MAX_VALUE;
		for (int line = startLine; line <= endLine; line++) {
			int lineIndentation = getLineIndentationDepth(line, true);
			if (lineIndentation >= 0) {
				indentation = Math.min(indentation, lineIndentation);
			}
		}
		if (indentation == Integer.MAX_VALUE) {
			indentation = 0;
		}
		return indentation;
	}
	
	/**
	 * Prepares the indentation of the line, so you can insert text at the
	 * given indentation depth.
	 * 
	 * @param line line index
	 * @param depth
	 * @return the offset of the depth in this line.
	 * @throws BadLocationException
	 */
	public TextEdit edit(int line, IndentEditAction action) throws BadLocationException {
		
		final int depth = action.getDepth();
		final IRegion lineInfo = fDocument.getLineInformation(line);
		if (depth > 0) {
			int indentation = 0;
			int offset = lineInfo.getOffset();
			StringBuilder replacement = new StringBuilder(depth);
			boolean changed = false;
			
			ITERATE_CHARS : while (indentation < depth) {
				int c = getDocumentChar(offset);
				int tabStart, tabEnd, spaceCount;
				switch (c) {
				case ' ':
					indentation++;
					offset++;
					replacement.append(' ');
					continue ITERATE_CHARS;
				case '\t':
					tabStart = (indentation/fTabWidth) * fTabWidth;
					tabEnd = tabStart + fTabWidth;
					if (tabEnd > depth) {
						spaceCount = tabEnd - indentation;
						replacement.append(repeat(' ', spaceCount));
						changed = true;
					}
					else {
						replacement.append('\t');
					}
					indentation = tabEnd;
					offset++;
					continue ITERATE_CHARS;
				case '\r':
				case '\n':
				case -1:
					tabStart = (indentation/fTabWidth) * fTabWidth;
					tabEnd = tabStart + fTabWidth;
					if (fTabAsDefault && (tabEnd <= depth)) {
						spaceCount = indentation-tabStart;
						replacement.delete(replacement.length()-spaceCount, replacement.length());
						replacement.append('\t');
						indentation = tabEnd;
						changed = true;
					}
					else {
						spaceCount = depth-indentation;
						replacement.append(repeat(' ', spaceCount));
						indentation += spaceCount;
						changed = true;
					}
					continue ITERATE_CHARS;
				default:
					throw new IllegalArgumentException(createNoIndentationCharMessage(c));
				}
			}
			if (changed) {
				return action.createEdit(lineInfo.getOffset(), offset-lineInfo.getOffset(), replacement);
			}
		}
		return action.createEdit(lineInfo.getOffset());
	}
	
	/**
	 * Returns the index, at which the indentation depth is reached.
	 * @param line text, like content of a line, to check
	 * @param depth depth to search for
	 * @return
	 */
	public int getIndentedIndex(CharSequence line, int depth) {
		
		int indentation = 0;
		int position = 0;
		ITERATE_CHARS : for (; position < line.length() && indentation < depth; position++) {
			char c = line.charAt(position);
			switch (c) {
			case ' ':
				indentation++;
				continue ITERATE_CHARS;
			case '\t':
				indentation += fTabWidth;
				continue ITERATE_CHARS;
			default:
				throw new IllegalArgumentException(createNoIndentationCharMessage(c));
			}
		}
		return position;
	}
	
	/**
	 * Return the document offset, at which the indentation depth in the specified line is reached.
	 * @param line index, to check 
	 * @param depth
	 * @return
	 * @throws BadLocationException
	 */
	public int getIndentedOffset(int line, int depth) throws BadLocationException {
		
		int indentation = 0;
		IRegion lineInfo = fDocument.getLineInformation(line);
		int offset = lineInfo.getOffset();
		int end = lineInfo.getOffset()+lineInfo.getLength();
		ITERATE_CHARS : for (; offset < end && indentation < depth; offset++) {
			char c = fDocument.getChar(offset);
			switch (c) {
			case ' ':
				indentation++;
				continue ITERATE_CHARS;
			case '\t':
				indentation += fTabWidth;
				continue ITERATE_CHARS;
			default:
				throw new IllegalArgumentException(createNoIndentationCharMessage(c));
			}
		}
		return offset;
	}
	
	/**
	 * Computes the visual char column for the specified offset.
	 * @param offset offset in document
	 * @return char column of offset
	 * @throws BadLocationException
	 */
	public int getColumnAtOffset(int offset) throws BadLocationException {
		int checkOffset = fDocument.getLineOffset(fDocument.getLineOfOffset(offset));
		int depth = 0;
		for (; checkOffset < offset; checkOffset++) {
			if (fDocument.getChar(checkOffset) == '\t') {
				depth += fTabWidth;
			}
			else {
				depth ++;
			}
		}
		return depth;
	}
	
	/**
	 * Returns the configured width of a default indentation.
	 * @return number of visual char columns
	 */
	public int getLevelColumns() {
		if (fTabAsDefault) {
			return fTabWidth;
		}
		else {
			return fNumOfSpaces;
		}
	}
	
	/**
	 * Computes the indentation depth adding the specified levels to the current depth.
	 * @param currentDepth depth in visual char columns
	 * @param levels number of indentation levels
	 * @return depth in visual char columns
	 */
	public int getNextLevelDepth(int currentDepth, int levels) {
		int columns = getLevelColumns();
		return ((currentDepth / columns + levels) * columns);
	}
	
	/**
	 * Creates a string for indentation of specified depth (respects the preferences).
	 * @param depth
	 * @return
	 */
	public String createIndentationString(int depth) {
		if (fTabAsDefault) {
			return new StringBuilder(depth)
					.append(repeat('\t', depth / fTabWidth))
					.append(repeat(' ', depth % fTabWidth))
					.toString(); 
		}
		else {
			return new String(repeat(' ', depth));
		}
	}
	
	public String createIndentCompletionString(int currentDepth) {
		if (fTabAsDefault) {
			return "\t"; //$NON-NLS-1$
		}
		else {
			int rest = currentDepth % fNumOfSpaces;
			return new String(repeat(' ', fNumOfSpaces-rest));
		}
	}
	
	
	private int getDocumentChar(int idx) throws BadLocationException {
		if (idx >= 0 && idx < fDocument.getLength()) {
			return fDocument.getChar(idx);
		}
		if (idx == -1 || idx == fDocument.getLength()) {
			return -1;
		}
		throw new BadLocationException();
	}
	
	private String createNoIndentationCharMessage(int c) {
		return NLS.bind("No indentation char: ''{0}''.", ((char) c)); //$NON-NLS-1$
	}
}
