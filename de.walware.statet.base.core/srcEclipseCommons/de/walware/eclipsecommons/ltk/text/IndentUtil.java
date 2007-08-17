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

package de.walware.eclipsecommons.ltk.text;

import java.util.Arrays;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.osgi.util.NLS;


/**
 *
 */
public class IndentUtil {

	public static final int COLUMN_IDX = 0;
	public static final int OFFSET_IDX = 1;

	public static final int CONSERVE_STRATEGY = 1;
	public static final int CORRECT_STRATEGY = 2;
	
	public static final char[] repeat(char c, int n) {
		char[] chars = new char[n];
		Arrays.fill(chars, c);
		return chars;
	}
	
	
	public static abstract class IndentEditAction {
		
		private int fIndentColumn;
		
		public IndentEditAction() {
		}
		public IndentEditAction(int indentColumn) {
			fIndentColumn = indentColumn;
		}
		public int getIndentColumn(int line, int lineOffset)
				throws BadLocationException {
			return fIndentColumn;
		}
		public abstract void doEdit(int line, int lineOffset, int length, StringBuilder text)
				throws BadLocationException;
	}
	
	
	private static interface EditStrategy {

		public void editInIndent(int firstLine, int lastLine, IndentEditAction action)
				throws BadLocationException;
	
		public void changeIndent(final int firstLine, final int lastLine, IndentEditAction action)
				throws BadLocationException;
	}
	
	private class ConserveStrategy implements EditStrategy {

		public void editInIndent(final int firstLine, final int lastLine, IndentEditAction action)
				throws BadLocationException {
			final StringBuilder replacement = new StringBuilder(20);
			ITER_LINES : for (int line = firstLine; line <= lastLine; line++) {
				final IRegion lineInfo = fDocument.getLineInformation(line);
				final int indentColumn = action.getIndentColumn(line, lineInfo.getOffset());
				if (indentColumn < 0) {
					continue ITER_LINES;
				}
				if (indentColumn > 0) {
					replacement.setLength(0);
					int indentation = 0;
					int offset = lineInfo.getOffset();
					boolean changed = false;
					
					ITER_CHARS : while (indentation < indentColumn) {
						int c = getDocumentChar(offset);
						int tabStart, tabEnd, spaceCount;
						switch (c) {
						case ' ':
							indentation++;
							offset++;
							replacement.append(' ');
							continue ITER_CHARS;
						case '\t':
							tabStart = (indentation/fTabWidth) * fTabWidth;
							tabEnd = tabStart + fTabWidth;
							if (tabEnd > indentColumn) {
								spaceCount = tabEnd - indentation;
								replacement.append(repeat(' ', spaceCount));
								changed = true;
							}
							else {
								replacement.append('\t');
							}
							indentation = tabEnd;
							offset++;
							continue ITER_CHARS;
						case '\r':
						case '\n':
						case -1:
							tabStart = (indentation/fTabWidth) * fTabWidth;
							tabEnd = tabStart + fTabWidth;
							if (fTabAsDefault && (tabEnd <= indentColumn)) {
								spaceCount = indentation-tabStart;
								replacement.delete(replacement.length()-spaceCount, replacement.length());
								replacement.append('\t');
								indentation = tabEnd;
								changed = true;
							}
							else {
								spaceCount = indentColumn-indentation;
								replacement.append(repeat(' ', spaceCount));
								indentation += spaceCount;
								changed = true;
							}
							continue ITER_CHARS;
						default:
							throw new IllegalArgumentException(createNoIndentationCharMessage(c));
						}
					}
					if (changed) {
						action.doEdit(line, lineInfo.getOffset(), offset-lineInfo.getOffset(), replacement);
						continue ITER_LINES;
					}
				}
				action.doEdit(line, lineInfo.getOffset(), 0, null);
				continue ITER_LINES;
			}
		}

		public void changeIndent(final int firstLine, final int lastLine, IndentEditAction action)
				throws BadLocationException {
			final StringBuilder replacement = new StringBuilder(20);
			ITER_LINES : for (int line = firstLine; line <= lastLine; line++) {
				final IRegion lineInfo = fDocument.getLineInformation(line);
				final int indentColumn = action.getIndentColumn(line, lineInfo.getOffset());
				if (indentColumn < 0) {
					continue ITER_LINES;
				}
				replacement.setLength(0);
				int column = 0;
				int offset = lineInfo.getOffset();
				
				ITER_CHARS : while (column < indentColumn) {
					final int c = getDocumentChar(offset);
					int tabStart, tabEnd, spaceCount;
					switch (c) {
					case ' ':
						column++;
						offset++;
						replacement.append(' ');
						continue ITER_CHARS;
					case '\t':
						tabStart = (column/fTabWidth) * fTabWidth;
						tabEnd = tabStart + fTabWidth;
						if (tabEnd > indentColumn) {
							spaceCount = indentColumn - column;
							replacement.append(repeat(' ', spaceCount));
							column = indentColumn;
						}
						else {
							replacement.append('\t');
							column = tabEnd;
						}
						offset++;
						continue ITER_CHARS;
					default:
						break ITER_CHARS;
					}
				}
				ITER_CHARS : while (true) {
					final int c = getDocumentChar(offset);
					if (c != ' ' && c != '\t') {
						break ITER_CHARS;
					}
					offset++;
				}
				if (column < indentColumn) {
					appendIndentation(replacement, column, indentColumn);
				}

				action.doEdit(line, lineInfo.getOffset(), offset-lineInfo.getOffset(), replacement);
				continue ITER_LINES;
			}
		}
	}
	
	private class CorrectStrategy implements EditStrategy {
		
		public void editInIndent(final int firstLine, final int lastLine, IndentEditAction action)
				throws BadLocationException {
			final StringBuilder replacement = new StringBuilder(20);
			ITER_LINES : for (int line = firstLine; line <= lastLine; line++) {
				final int lineOffset = fDocument.getLineOffset(line);
				final int indentColumn = action.getIndentColumn(line, lineOffset);
				if (indentColumn < 0) {
					continue ITER_LINES;
				}
				final int[] current = getLineIndent(line, true);
				replacement.setLength(0);
				appendIndentation(replacement, indentColumn);
				if (current[COLUMN_IDX] >= 0) {
					appendSpaces(replacement, current[COLUMN_IDX]-indentColumn);
				}
				action.doEdit(line, lineOffset, current[OFFSET_IDX]-lineOffset, replacement);
				continue ITER_LINES;
			}
		}
		
		public void changeIndent(final int firstLine, final int lastLine, IndentEditAction action)
				throws BadLocationException {
			final StringBuilder replacement = new StringBuilder(20);
			ITER_LINES : for (int line = firstLine; line <= lastLine; line++) {
				final int lineOffset = fDocument.getLineOffset(line);
				final int indentColumn = action.getIndentColumn(line, lineOffset);
				if (indentColumn < 0) {
					continue ITER_LINES;
				}
				final int[] current = getLineIndent(line, false);
				replacement.setLength(0);
				appendIndentation(replacement, indentColumn);
				action.doEdit(line, lineOffset, current[OFFSET_IDX]-lineOffset, replacement);
				continue ITER_LINES;
			}
			
		}

	}
	
	
	private IDocument fDocument;
	private int fTabWidth;
	private boolean fTabAsDefault;
	private int fNumOfSpaces;
	private EditStrategy fEditStrategy;
	
	
	public IndentUtil(IDocument document, int editStrategy, boolean tabsAsDefault, int tabWidth, int numOfSpaces) {
		fDocument = document;
		switch (editStrategy) {
		case CONSERVE_STRATEGY:
			fEditStrategy = new ConserveStrategy();
			break;
		case CORRECT_STRATEGY:
			fEditStrategy = new CorrectStrategy();
			break;
		}
		fTabAsDefault = tabsAsDefault;
		fTabWidth = tabWidth;
		fNumOfSpaces = numOfSpaces;
	}
	
	/**
	 * Return the indentation indentColumn of the specified line.
	 * 
	 * @param line line to check
	 * @param markBlankLine if true, empty lines have are marked with a indentColumn of -1
	 * @return
	 * @throws BadLocationException
	 */
	public int[] getLineIndent(int line, boolean markBlankLine) throws BadLocationException {
		IRegion lineInfo = fDocument.getLineInformation(line);
		int column = 0;
		int offset = lineInfo.getOffset();
		ITERATE_CHAR : while (true) {
			int c = getDocumentChar(offset++);
			switch (c) {
			case ' ':
				column++;
				continue ITERATE_CHAR;
			case '\t':
				column += fTabWidth - (column % fTabWidth);
				continue ITERATE_CHAR;
			case '\r':
			case '\n':
			case -1:
				if (markBlankLine) {
					return new int[] { -1, --offset };
				}
			default:
				return new int[] { column, --offset };
			}
		}
	}
	
//	public int getIndentationindentColumn(String chars) throws BadLocationException {
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
	 * Returns the common (min) indentation indentColumn of all lines. Empty lines are ignored.
	 * @param startLine line index of first line
	 * @param endLine line index of last line
	 * @return
	 * @throws BadLocationException
	 */
	public int getMultilineIndentColumn(int startLine, int endLine) throws BadLocationException {
		int indentation = Integer.MAX_VALUE;
		for (int line = startLine; line <= endLine; line++) {
			int[] lineIndent = getLineIndent(line, true);
			if (lineIndent[COLUMN_IDX] >= 0) {
				indentation = Math.min(indentation, lineIndent[COLUMN_IDX]);
			}
		}
		if (indentation == Integer.MAX_VALUE) {
			indentation = 0;
		}
		return indentation;
	}
	
	/**
	 * Prepares the indentation of the line, so you can insert text at the
	 * given indentation indentColumn.
	 * 
	 * @param line line index
	 * @return the returned object of your action
	 * @throws BadLocationException
	 */
	public void editInIndent(final int firstLine, final int lastLine, final IndentEditAction action)
			throws BadLocationException {
		fEditStrategy.editInIndent(firstLine, lastLine, action);
	}
	
	public void changeIndent(final int firstLine, final int lastLine, final IndentEditAction action)
			throws BadLocationException {
		fEditStrategy.changeIndent(firstLine, lastLine, action);
	}
	
	/**
	 * Returns the index, at which the indentation indentColumn is reached.
	 * @param line text, like content of a line, to check
	 * @param indentColumn indentColumn to search for
	 * @return
	 */
	public int getIndentedIndex(CharSequence line, int indentColumn) {
		int position = 0;
		int current = 0;
		ITERATE_CHARS : for (; position < line.length() && current < indentColumn; position++) {
			char c = line.charAt(position);
			switch (c) {
			case ' ':
				current++;
				continue ITERATE_CHARS;
			case '\t':
				current += fTabWidth - (current % fTabWidth);
				continue ITERATE_CHARS;
			default:
				throw new IllegalArgumentException(createNoIndentationCharMessage(c));
			}
		}
		return position;
	}
	
	/**
	 * Return the document offset, at which the indentation indentColumn in the specified line is reached.
	 * @param line index, to check
	 * @param column
	 * @return
	 * @throws BadLocationException
	 */
	public int getIndentedOffsetAt(final int line, final int column) throws BadLocationException {
		IRegion lineInfo = fDocument.getLineInformation(line);
		int offset = lineInfo.getOffset();
		int current = 0;
		ITERATE_CHARS : while (current < column) {
			char c = fDocument.getChar(offset++);
			switch (c) {
			case ' ':
				current++;
				continue ITERATE_CHARS;
			case '\t':
				current += fTabWidth - (current % fTabWidth);
				continue ITERATE_CHARS;
			default:
				throw new IllegalArgumentException(createNoIndentationCharMessage(c));
			}
		}
		return offset;
	}

	/**
	 * Computes the column for the specified offset.
	 * Linebreak are not specially handled.
	 * @param offset offset in document
	 * @return char column of offset
	 * @throws BadLocationException
	 */
	public int getColumnAtOffset(int offset) throws BadLocationException {
		int checkOffset = fDocument.getLineOffset(fDocument.getLineOfOffset(offset));
		int column = 0;
		ITERATE_CHARS : while (checkOffset < offset) {
			if (fDocument.getChar(checkOffset++) == '\t') {
				column += fTabWidth - (column % fTabWidth);
			}
			else {
				column ++;
			}
		}
		return column;
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
	 * Computes the indentation column adding the specified levels to the current indentColumn.
	 * @param currentColumn indentColumn in visual char columns
	 * @param levels number of indentation levels
	 * @return indentColumn in visual char columns
	 */
	public int getNextLevelColumn(int currentColumn, int levels) {
		int columns = getLevelColumns();
		return ((currentColumn / columns + levels) * columns);
	}
	
	/**
	 * Creates a string for indentation of specified indentColumn (respects the preferences).
	 * @param indentColumn
	 * @return
	 */
	public String createIndentString(int indentColumn) {
		if (fTabAsDefault) {
			return new StringBuilder(indentColumn)
					.append(repeat('\t', indentColumn / fTabWidth))
					.append(repeat(' ', indentColumn % fTabWidth))
					.toString();
		}
		else {
			return new String(repeat(' ', indentColumn));
		}
	}
	
	public String createIndentCompletionString(int currentColumn) {
		if (fTabAsDefault) {
			return "\t"; //$NON-NLS-1$
		}
		else {
			int rest = currentColumn % fNumOfSpaces;
			return new String(repeat(' ', fNumOfSpaces-rest));
		}
	}
	
	
	protected final int getDocumentChar(int idx) throws BadLocationException {
		if (idx >= 0 && idx < fDocument.getLength()) {
			return fDocument.getChar(idx);
		}
		if (idx == -1 || idx == fDocument.getLength()) {
			return -1;
		}
		throw new BadLocationException();
	}
	
	protected final void appendIndentation(final StringBuilder s, final int indentColumn) {
		if (fTabAsDefault) {
			s.append(repeat('\t', indentColumn / fTabWidth));
			s.append(repeat(' ', indentColumn % fTabWidth));
		}
		else {
			s.append(repeat(' ', indentColumn));
		}
	}
	
	protected final void appendIndentation(final StringBuilder s, final int currentColumn, final int indentColumn) {
		if (fTabAsDefault) {
			final int tabDiff = (indentColumn/fTabWidth) - (currentColumn/fTabWidth) ;
			if (tabDiff > 0) {
				final int spaces = currentColumn % fTabWidth;
				if (spaces > 0) {
					s.append(repeat(' ', fTabWidth-spaces));
					s.append(repeat('\t', tabDiff-1));
				}
				else {
					s.append(repeat('\t', tabDiff));
				}
				s.append(repeat(' ', indentColumn % fTabWidth));
			}
			else {
				s.append(repeat(' ', indentColumn-currentColumn));
			}
		}
		else {
			s.append(repeat(' ', indentColumn));
		}
	}

	//	protected final void appendIndentCompletion(final StringBuilder s, final int currentColumn) {
//		if (fTabAsDefault) {
//			s.append('\t');
//		}
//		else {
//			s.append(repeat(' ', fNumOfSpaces-(currentColumn % fNumOfSpaces)));
//		}
//	}
	
	protected final void appendSpaces(final StringBuilder s, final int num) {
		s.append(repeat(' ', num));
	}
	
	private String createNoIndentationCharMessage(int c) {
		return NLS.bind("No indentation char: ''{0}''.", ((char) c)); //$NON-NLS-1$
	}
}
