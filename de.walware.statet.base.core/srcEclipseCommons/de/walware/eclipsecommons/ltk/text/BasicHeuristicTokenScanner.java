/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedRegion;



/**
 * Utility methods for heuristic based R manipulations in an incomplete source file.
 *
 * <p>An instance holds some internal position in the document and is therefore not threadsafe.</p>
 *
 * @since 0.2
 */
public class BasicHeuristicTokenScanner implements ITokenScanner {
	
	
	/**
	 * Specifies the stop condition, upon which the <code>scan...</code> methods will decide whether
	 * to keep scanning or not. This interface may implemented by clients.
	 */
	protected static abstract class StopCondition {
		/**
		 * Instructs the scanner to return the current position.
		 *
		 * @param ch the char at the current position
		 * @param position the current position
		 * @param forward the iteration direction
		 * @return <code>true</code> if the stop condition is met.
		 */
		public abstract boolean stop(boolean forward);
		
		/**
		 * Asks the condition to return the next position to query. The default
		 * is to return the next/previous position.
		 *
		 * @return the next position to scan
		 */
		public int nextPosition(int position, boolean forward) {
			return forward ? position + 1 : position - 1;
		}
	}
	
	protected static abstract class PartitionMatcher {
		
		public abstract boolean matches(String partitionId);
	}
	
	protected static class SinglePartitionMatcher extends PartitionMatcher {
		
		private String fPartitionId;
		
		public SinglePartitionMatcher(String partitionId) {
			fPartitionId = partitionId;
		}
		
		@Override
		public boolean matches(String partitionId) {
			return fPartitionId.equals(partitionId);
		}
	}
	
	protected static final PartitionMatcher ALL_PARTITIONS_MATCHER = new PartitionMatcher() {
		
		@Override
		public boolean matches(String partitionId) {
			return true;
		}
	};
	
	/**
	 * Stops upon a character in the default partition that matches the given character list.
	 */
	protected class CharacterMatchCondition extends StopCondition {

		protected final char[] fChars;

		/**
		 * Creates a new instance.
		 * @param ch the single character to match
		 */
		public CharacterMatchCondition(char ch) {
			this(new char[] {ch});
		}

		/**
		 * Creates a new instance.
		 * @param chars the chars to match.
		 */
		public CharacterMatchCondition(char[] chars) {
			Assert.isNotNull(chars);
			Assert.isTrue(chars.length > 0);
			fChars = chars;
			Arrays.sort(chars);
		}

		@Override
		public boolean stop(boolean forward) {
			return (Arrays.binarySearch(fChars, fChar) >= 0 && (fPartition.matches(getContentType(fPos))) );
		}
		
		@Override
		public int nextPosition(int position, boolean forward) {
			ITypedRegion partition = getPartition(position);
			if (fPartition.matches(partition.getType()))
				return super.nextPosition(position, forward);

			if (forward) {
				int end = partition.getOffset() + partition.getLength();
				if (position < end)
					return end;
			} else {
				int offset = partition.getOffset();
				if (position > offset)
					return offset - 1;
			}
			return super.nextPosition(position, forward);
		}
	}

	protected class ExtCharacterMatchCondition extends CharacterMatchCondition {
		
		private final char fEscapeChar;
		private int fLastEscapeOffset = -100;
		
		ExtCharacterMatchCondition(char[] chars, char escapeChar) {
			super(chars);
			fEscapeChar = escapeChar;
		}

		@Override
		public boolean stop(boolean forward) {
			if (fPos == fLastEscapeOffset+1)
				return false;
			if (fChar == fEscapeChar) {
				fLastEscapeOffset = fPos;
				return false;
			}
			return (Arrays.binarySearch(fChars, fChar) >= 0 && (fPartition.matches(getContentType(fPos))) );
		}
		
	}
	
	
	/** The document being scanned. */
	private IDocument fDocument;
	/** The partitioning being used for scanning. */
	private String fPartitioning;
	/** The partition to scan in. */
	private PartitionMatcher fPartition;

	/* internal scan state */

	/** the most recently read character. */
	protected char fChar;
	/** the most recently read position. */
	protected int fPos;
	/** the most recently read line of position (only if used). */
	private int fLine;
	
	private StopCondition fNonWSCondition;
	private StopCondition fNonWSorLRCondition;


	/**
	 * Creates a new instance.
	 * Before using scan..., you have to call configure...
	 * 
	 * @param partitioning the partitioning to use for scanning
	 */
	public BasicHeuristicTokenScanner(String partitioning) {
		Assert.isNotNull(partitioning);
		fPartitioning = partitioning;
	}
	
	protected StopCondition getNonWSCondition() {
		if (fNonWSCondition == null) {
			fNonWSCondition = new StopCondition() {
				@Override
				public boolean stop(boolean forward) {
					return (Character.getType(fChar) != Character.SPACE_SEPARATOR && fChar != '\t');
				}
			};
		}
		return fNonWSCondition;
	}
	
	protected StopCondition getNonWSorLRCondition() {
		if (fNonWSorLRCondition == null) {
			fNonWSorLRCondition = new StopCondition() {
				@Override
				public boolean stop(boolean forward) {
					return (Character.getType(fChar) != Character.SPACE_SEPARATOR && fChar != '\t'
						&& fChar != '\r' && fChar != '\n');
				}
			};
		}
		return fNonWSorLRCondition;
	}
	
	/**
	 * 
	 * @param document the document to scan
	 * @param partition the partition to scan in
	 */
	public void configure(IDocument document, String partition) {
		Assert.isNotNull(document);
		fDocument = document;
		fPartition = (partition != null) ? new SinglePartitionMatcher(partition) : ALL_PARTITIONS_MATCHER;
	}
	
//	public void configure(IDocument document, int offset) throws BadLocationException {
//		configure(document, TextUtilities.getContentType(
//				document, fPartitioning, offset, false));
//	}
	
	
	/**
	 * Returns the most recent internal scan position.
	 *
	 * @return the most recent internal scan position.
	 */
	public int getPosition() {
		return fPos;
	}

	/*
	 * @see de.walware.statet.ext.ui.text.ITokenScanner#findClosingPeer(int, char[])
	 */
	public int findClosingPeer(int start, char[] pair) {
		return findClosingPeer(start, pair, (char)0);
	}

	/*
	 * @see de.walware.statet.ext.ui.text.ITokenScanner#findClosingPeer(int, char[], char)
	 */
	public int findClosingPeer(int start, char[] pair, char escapeChar) {
		
		Assert.isNotNull(fDocument);
		Assert.isTrue(start >= 0);
		
		StopCondition cond = (escapeChar == (char)0) ?
				new CharacterMatchCondition(pair) : new ExtCharacterMatchCondition(pair, escapeChar);

		try {
			int depth = 1;
			start -= 1;
			while (true) {
				start = scanForward(start + 1, UNBOUND, cond);
				if (start == NOT_FOUND)
					return NOT_FOUND;

				if (fDocument.getChar(start) == pair[OPENING_PEER])
					depth++;
				else
					depth--;

				if (depth == 0)
					return start;
			}

		} catch (BadLocationException e) {
			return NOT_FOUND;
		}
	}

	/*
	 * @see de.walware.statet.ext.ui.text.ITokenScanner#findOpeningPeer(int, char[])
	 */
	public int findOpeningPeer(int start, char[] pair) {
		StopCondition cond = new CharacterMatchCondition(pair);

		if (start >= fDocument.getLength()) {
			start = fDocument.getLength()-1;
		}
		try {
			int depth= 1;
			start += 1;
			while (true) {
				start = scanBackward(start - 1, UNBOUND, cond);
				if (start == NOT_FOUND)
					return NOT_FOUND;

				if (fDocument.getChar(start) == pair[CLOSING_PEER])
					depth++;
				else
					depth--;

				if (depth == 0)
					return start;
			}

		} catch (BadLocationException e) {
			return NOT_FOUND;
		}
	}

	public int findOpeningPeer(int start, char[] pair, char escapeChar) {
		Assert.isTrue(start < fDocument.getLength());
		if (escapeChar == (char)0)
			return findOpeningPeer(start, pair);

		StopCondition cond = new ExtCharacterMatchCondition(pair, escapeChar);

		try {
			int depth= 1;
			start += 1;
			fLine = fDocument.getLineOfOffset(start);
			while (true) {
				int[] list = preScanBackward(start - 1, UNBOUND, cond);
				if (list == null)
					return NOT_FOUND;
				
				for (int i = list.length-1; i >= 0; i--) {
					start = list[i];
					if (fDocument.getChar(start) == pair[CLOSING_PEER])
						depth++;
					else
						depth--;
					
					if (depth == 0)
						return start;
				}
				start = fDocument.getLineOffset(fLine);
			}

		} catch (BadLocationException e) {
			return NOT_FOUND;
		}
	}
	
	/**
	 * Finds the smallest position in <code>fDocument</code> such that the position is &gt;= <code>position</code>
	 * and &lt; <code>bound</code> and <code>Character.isWhitespace(fDocument.getChar(pos))</code> evaluates to <code>false</code>.
	 *
	 * @param position the first character position in <code>fDocument</code> to be considered
	 * @param bound the first position in <code>fDocument</code> to not consider any more, with <code>bound</code> &gt; <code>position</code>, or <code>UNBOUND</code>
	 * @return the smallest position of a non-whitespace character in [<code>position</code>, <code>bound</code>), or <code>NOT_FOUND</code> if none can be found
	 */
	public int findNonBlankForward(int position, int bound, boolean linebreakIsBlank) {
		
		return scanForward(position, bound, linebreakIsBlank ?
				getNonWSorLRCondition() : getNonWSCondition());
	}

	public int findNonBlankBackward(int position, int bound, boolean linebreakIsBlank) {
		
		return scanBackward(position, bound, linebreakIsBlank ?
				getNonWSorLRCondition() : getNonWSCondition());
	}
	
	public IRegion findBlankRegion(int position, boolean linebreakIsBlank) {
		
		return findRegion(position, linebreakIsBlank ?
				getNonWSorLRCondition() : getNonWSCondition());
	}
		
	public boolean isBlankLine(int position) throws BadLocationException {
		
		IRegion line = fDocument.getLineInformationOfOffset(position);
		if (line.getLength() > 0) {
			int nonWhitespace = findNonBlankForward(line.getOffset(), line.getOffset()+line.getLength(), false);
			return (nonWhitespace == NOT_FOUND);
		}
		return true;
	}
	
	public IRegion findCommonWord(int position) {
		
		return findRegion(position, new StopCondition() {
			@Override
			public boolean stop(boolean forward) {
				return (!Character.isLetterOrDigit(fChar));
			}
		});
	}
	
	public IRegion getTextBlock(int position1, int position2) throws BadLocationException {
		
		int line1 = fDocument.getLineOfOffset(position1);
		int line2 = fDocument.getLineOfOffset(position2);
		if (line1 < line2 && fDocument.getLineOffset(line2) == position2) {
			line2--;
		}
		int start = fDocument.getLineOffset(line1);
		int length = fDocument.getLineOffset(line2)+fDocument.getLineLength(line2)-start;
		return new Region(start, length);
	}
	
	public int getFirstLineOfRegion(IRegion region) throws BadLocationException {

		return fDocument.getLineOfOffset(region.getOffset());
	}
	
	public int getLastLineOfRegion(IRegion region) throws BadLocationException {
		
		if (region.getLength() == 0) {
			return fDocument.getLineOfOffset(region.getOffset());
		}
		return fDocument.getLineOfOffset(region.getOffset()+region.getLength()-1);
	}
	

	private int[] preScanBackward(int start, int bound, StopCondition condition) throws BadLocationException {

		IntList list = new ArrayIntList();
		int scanEnd = start;

		NEXT_LINE: while (list.isEmpty() && fLine >= 0) {
			int lineOffset = fDocument.getLineOffset(fLine);
			int next = lineOffset - 1;
			while ((next = scanForward(next + 1, scanEnd, condition)) != NOT_FOUND) {
				if (bound < next)
					list.add(next);
			}
			
			if (lineOffset <= bound)
				break NEXT_LINE;
			
			fLine--;
			scanEnd = lineOffset - 1;
		}
		
		if (!list.isEmpty())
			return list.toArray();
		else
			return null;
	}


	/**
	 * Finds the lowest position <code>p</code> in <code>fDocument</code> such that <code>start</code> &lt;= p &lt;
	 * <code>bound</code> and <code>condition.stop(fDocument.getChar(p), p)</code> evaluates to <code>true</code>.
	 *
	 * @param start the first character position in <code>fDocument</code> to be considered
	 * @param bound the first position in <code>fDocument</code> to not consider any more, with <code>bound</code> &gt; <code>start</code>, or <code>UNBOUND</code>
	 * @param condition the <code>StopCondition</code> to check
	 * @return the lowest position in [<code>start</code>, <code>bound</code>) for which <code>condition</code> holds, or <code>NOT_FOUND</code> if none can be found
	 */
	protected int scanForward(int start, int bound, StopCondition condition) {
		if (bound == UNBOUND) {
			bound = fDocument.getLength();
		}
		assert(bound <= fDocument.getLength());
		assert(start >= 0);

		try {
			fPos = start;
			while (fPos < bound) {
				fChar = fDocument.getChar(fPos);
				if (condition.stop(true)) {
					return fPos;
				}
				fPos = condition.nextPosition(fPos, true);
			}
			fPos = bound;
		} catch (BadLocationException e) {
		}
		return NOT_FOUND;
	}


	/**
	 * Finds the lowest position in <code>fDocument</code> such that the position is &gt;= <code>position</code>
	 * and &lt; <code>bound</code> and <code>fDocument.getChar(position) == ch</code> evaluates to <code>true</code>
	 * and the position is in the default partition.
	 *
	 * @param position the first character position in <code>fDocument</code> to be considered
	 * @param bound the first position in <code>fDocument</code> to not consider any more, with <code>bound</code> &gt; <code>position</code>, or <code>UNBOUND</code>
	 * @param ch the <code>char</code> to search for
	 * @return the lowest position of <code>ch</code> in (<code>bound</code>, <code>position</code>] that resides in a Java partition, or <code>NOT_FOUND</code> if none can be found
	 */
	public int scanForward(int position, int bound, char ch) {
		return scanForward(position, bound, new CharacterMatchCondition(ch));
	}

	/**
	 * Finds the lowest position in <code>fDocument</code> such that the position is &gt;= <code>position</code>
	 * and &lt; <code>bound</code> and <code>fDocument.getChar(position) == ch</code> evaluates to <code>true</code> for at least one
	 * ch in <code>chars</code> and the position is in the default partition.
	 *
	 * @param position the first character position in <code>fDocument</code> to be considered
	 * @param bound the first position in <code>fDocument</code> to not consider any more, with <code>bound</code> &gt; <code>position</code>, or <code>UNBOUND</code>
	 * @param chars an array of <code>char</code> to search for
	 * @return the lowest position of a non-whitespace character in [<code>position</code>, <code>bound</code>) that resides in a Java partition, or <code>NOT_FOUND</code> if none can be found
	 */
	public int scanForward(int position, int bound, char[] chars) {
		return scanForward(position, bound, new CharacterMatchCondition(chars));
	}

	/**
	 * Finds the highest position <code>p</code> in <code>fDocument</code> such that <code>bound</code> &lt; <code>p</code> &lt;= <code>start</code>
	 * and <code>condition.stop(fDocument.getChar(p), p)</code> evaluates to <code>true</code>.
	 *
	 * @param start the first character position in <code>fDocument</code> to be considered
	 * @param bound the first position in <code>fDocument</code> to not consider any more, with <code>bound</code> &lt; <code>start</code>, or <code>UNBOUND</code>
	 * @param condition the <code>StopCondition</code> to check
	 * @return the highest position in (<code>bound</code>, <code>start</code> for which <code>condition</code> holds, or <code>NOT_FOUND</code> if none can be found
	 */
	protected int scanBackward(int start, int bound, StopCondition condition) {
		if (bound == UNBOUND) {
			bound = -1;
		}
		assert(bound >= -1);
		assert(start == 0 || start < fDocument.getLength() );

		try {
			if (fDocument.getLength() > 0) {
				fPos = start;
				while (fPos > bound) {
					fChar = fDocument.getChar(fPos);
					if (condition.stop(false)) {
						return fPos;
					}
					fPos = condition.nextPosition(fPos, false);
				}
			}
			fPos = bound;
		} catch (BadLocationException e) {
		}
		return NOT_FOUND;
	}
	
	/**
	 * Finds the highest position in <code>fDocument</code> such that the position is &lt;= <code>position</code>
	 * and &gt; <code>bound</code> and <code>fDocument.getChar(position) == ch</code> evaluates to <code>true</code> for at least one
	 * ch in <code>chars</code> and the position is in the default partition.
	 *
	 * @param position the first character position in <code>fDocument</code> to be considered
	 * @param bound the first position in <code>fDocument</code> to not consider any more, with <code>bound</code> &lt; <code>position</code>, or <code>UNBOUND</code>
	 * @param ch the <code>char</code> to search for
	 * @return the highest position of one element in <code>chars</code> in (<code>bound</code>, <code>position</code>] that resides in a Java partition, or <code>NOT_FOUND</code> if none can be found
	 */
	public int scanBackward(int position, int bound, char ch) {
		return scanBackward(position, bound, new CharacterMatchCondition(ch));
	}

	/**
	 * Finds the highest position in <code>fDocument</code> such that the position is &lt;= <code>position</code>
	 * and &gt; <code>bound</code> and <code>fDocument.getChar(position) == ch</code> evaluates to <code>true</code> for at least one
	 * ch in <code>chars</code> and the position is in the default partition.
	 *
	 * @param position the first character position in <code>fDocument</code> to be considered
	 * @param bound the first position in <code>fDocument</code> to not consider any more, with <code>bound</code> &lt; <code>position</code>, or <code>UNBOUND</code>
	 * @param chars an array of <code>char</code> to search for
	 * @return the highest position of one element in <code>chars</code> in (<code>bound</code>, <code>position</code>] that resides in a Java partition, or <code>NOT_FOUND</code> if none can be found
	 */
	public int scanBackward(int position, int bound, char[] chars) {
		return scanBackward(position, bound, new CharacterMatchCondition(chars));
	}

	protected IRegion findRegion(int position, StopCondition condition) {
		return findRegion(position, condition, false);
	}

	protected IRegion findRegion(int position, StopCondition condition, boolean allowClosing) {
		
		int start = position;
		int end = scanForward(position, UNBOUND, condition);
		if (end == NOT_FOUND) {
			end = fPos;
		}
		if (allowClosing || end > position) {
			start = scanBackward(--start, UNBOUND, condition);
			if (start == NOT_FOUND) {
				start = fPos;
			}
			start++;
		}
		if (start < end) {
			return new Region(start, end-start);
		}
		return null;
	}

	/**
	 * Returns the partition at <code>position</code>.
	 *
	 * @param position the position to get the partition for
	 * @return the content type at <code>position</code> or a dummy zero-length
	 *         partition if accessing the document fails
	 */
	private String getContentType(int position) {
		try {
			return TextUtilities.getContentType(fDocument, fPartitioning, position, false);
		} catch (BadLocationException e) {
			return null; // ?
		}
	}

	/**
	 * Returns the partition at <code>position</code>.
	 *
	 * @param position the position to get the partition for
	 * @return the partition at <code>position</code> or a dummy zero-length
	 *         partition if accessing the document fails
	 */
	private ITypedRegion getPartition(int position) {
		try {
			return TextUtilities.getPartition(fDocument, fPartitioning, position, false);
		} catch (BadLocationException e) {
			return new TypedRegion(position, 0, "__no_partition_at_all"); //$NON-NLS-1$
		}
	}

}
