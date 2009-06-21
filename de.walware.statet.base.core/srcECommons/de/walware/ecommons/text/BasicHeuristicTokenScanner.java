/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.text;

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
	
	
	protected static final IPartitionConstraint ALL_PARTITIONS_CONSTRAINT = new IPartitionConstraint() {
		
		public boolean matches(final String partitionType) {
			return true;
		}
		
	};
	
	
	/**
	 * Specifies the stop condition, upon which the <code>scan...</code> methods will decide whether
	 * to keep scanning or not. This interface may implemented by clients.
	 */
	protected abstract class StopCondition {
		
		/**
		 * Instructs the scanner to return the current position.
		 * 
		 * @return <code>true</code> if the stop condition is met.
		 */
		public abstract boolean stop();
		
		/**
		 * Asks the condition to return the next position to query. The default
		 * is to return the next/previous position.
		 * 
		 * @return the next position to scan
		 */
		public int nextPositionForward() {
			return fPos + 1;
		}
		
		public int nextPositionBackward() {
			return fPos - 1;
		}
		
	}
	
	/**
	 * Stops upon a character in the default partition that matches the given character list.
	 */
	protected abstract class PartitionBasedCondition extends StopCondition {
		
		private ITypedRegion fCurrentPartition;
		private boolean fCurrentPartitionMatched;
		private int fCurrentPartitionStart;
		private int fCurrentPartitionEnd;
		
		public PartitionBasedCondition() {
			fCurrentPartitionMatched = false;
		}
		
		@Override
		public boolean stop() {
			if (fCurrentPartitionMatched && fCurrentPartitionStart <= fPos && fPos < fCurrentPartitionEnd) {
				return matchesChar();
			}
			fCurrentPartition = getPartition();
			fCurrentPartitionStart = fCurrentPartition.getOffset();
			fCurrentPartitionEnd = fCurrentPartitionStart+fCurrentPartition.getLength();
			if (fPartitionConstraint.matches(fCurrentPartition.getType())) {
				fCurrentPartitionMatched = true;
				return matchesChar();
			}
			else {
				fCurrentPartitionMatched = false;
				return false;
			}
			
		}
		
		protected abstract boolean matchesChar();
		
		@Override
		public int nextPositionForward() {
			if (fCurrentPartitionMatched) {
				return fPos + 1;
			}
			if (fPos < fCurrentPartitionEnd) {
				return fCurrentPartitionEnd;
			}
			return fPos + 1;
		}
		
		@Override
		public int nextPositionBackward() {
			if (fCurrentPartitionMatched) {
				return fPos - 1;
			}
			if (fPos >= fCurrentPartitionStart) {
				return fCurrentPartitionStart - 1;
			}
			return fPos - 1;
		}
		
	}
	
	
	protected class CharacterMatchCondition extends PartitionBasedCondition {
		
		protected final char[] fChars;
		
		/**
		 * Creates a new instance.
		 * @param ch the single character to match
		 */
		public CharacterMatchCondition(final char ch) {
			this(new char[] { ch });
		}
		
		/**
		 * Creates a new instance.
		 * @param chars the chars to match.
		 */
		public CharacterMatchCondition(final char[] chars) {
			assert (chars != null);
			fChars = chars;
		}
		
		@Override
		protected boolean matchesChar() {
			for (int i = 0; i < fChars.length; i++) {
				if (fChars[i] == fChar) {
					return true;
				}
			}
			return false;
		}
		
	}
	
	
	protected class ExtCharacterMatchCondition extends CharacterMatchCondition {
		
		private final char fEscapeChar;
		private int fLastEscapeOffset = -100;
		
		ExtCharacterMatchCondition(final char[] chars, final char escapeChar) {
			super(chars);
			fEscapeChar = escapeChar;
			Arrays.sort(chars);
		}
		
		@Override
		public boolean stop() {
			if (fPos == fLastEscapeOffset+1)
				return false;
			if (fChar == fEscapeChar) {
				fLastEscapeOffset = fPos;
				return false;
			}
			return (Arrays.binarySearch(fChars, fChar) >= 0 && (fPartitionConstraint.matches(getContentType())) );
		}
		
	}
	
	
	/** The partitioning being used for scanning. */
	private final PartitioningConfiguration fPartitioning;
	
	/** The document being scanned. */
	protected IDocument fDocument;
	/** The partition to scan in. */
	private IPartitionConstraint fPartitionConstraint;
	
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
	public BasicHeuristicTokenScanner(final PartitioningConfiguration partitioning) {
		assert (partitioning != null);
		
		fPartitioning = partitioning;
	}
	
	
	public final PartitioningConfiguration getPartitioningConfig() {
		return fPartitioning;
	}
	
	protected final String getPartitioning() {
		return fPartitioning.getPartitioning();
	}
	
	protected final IPartitionConstraint getDefaultPartitionConstraint() {
		return fPartitioning.getDefaultPartitionConstraint();
	}
	
	protected final IPartitionConstraint getPartitionConstraint() {
		return fPartitionConstraint;
	}
	
	public final char getChar() {
		return fChar;
	}
	
	protected boolean isWhitespace() {
		return (Character.getType(fChar) == Character.SPACE_SEPARATOR || fChar == '\t');
	}
	
	protected final StopCondition getAnyNonWSCondition() {
		if (fNonWSCondition == null) {
			fNonWSCondition = new StopCondition() {
				@Override
				public boolean stop() {
					return (!isWhitespace());
				}
			};
		}
		return fNonWSCondition;
	}
	
	protected final StopCondition getAnyNonWSorLRCondition() {
		if (fNonWSorLRCondition == null) {
			fNonWSorLRCondition = new StopCondition() {
				@Override
				public boolean stop() {
					return (!isWhitespace() && fChar != '\r' && fChar != '\n');
				}
			};
		}
		return fNonWSorLRCondition;
	}
	
	protected final StopCondition getNonWSCondition() {
		if (fNonWSCondition == null) {
			fNonWSCondition = new PartitionBasedCondition() {
				@Override
				protected boolean matchesChar() {
					return (!isWhitespace());
				}
			};
		}
		return fNonWSCondition;
	}
	
	protected final StopCondition getNonWSorLRCondition() {
		if (fNonWSorLRCondition == null) {
			fNonWSorLRCondition = new PartitionBasedCondition() {
				@Override
				protected boolean matchesChar() {
					return (!isWhitespace() && fChar != '\r' && fChar != '\n');
				}
			};
		}
		return fNonWSorLRCondition;
	}
	
	/**
	 * Configures the scanner for the given document
	 * and the given partition type as partition constraint
	 * 
	 * @param document the document to scan
	 * @param partition the partition to scan in
	 */
	public void configure(final IDocument document, final String partitionType) {
		assert (document != null && partitionType != null);
		fDocument = document;
		fPartitionConstraint = new IPartitionConstraint() {
			public boolean matches(final String partitionTypeToTest) {
				return partitionType == partitionTypeToTest;
			}
		};
	}
	
	/**
	 * Configures the scanner for the given document
	 * and no partition constraint
	 * 
	 * @param document the document to scan
	 */
	public void configure(final IDocument document) {
		assert (document != null);
		fDocument = document;
		fPartitionConstraint = ALL_PARTITIONS_CONSTRAINT;
	}
	
	/**
	 * Configures the scanner for the given document
	 * and the partition constraint for default partitions
	 * 
	 * @param document the document to scan
	 */
	public void configureDefaultParitions(final IDocument document) {
		assert (document != null);
		fDocument = document;
		fPartitionConstraint = getDefaultPartitionConstraint();
	}
	
	/**
	 * Configures the scanner for the given document
	 * and the given partition constraint
	 * 
	 * @param document the document to scan
	 * @param partition the partition to scan in
	 */
	public void configure(final IDocument document, final IPartitionConstraint partitionConstraint) {
		assert (document != null && partitionConstraint != null);
		fDocument = document;
		fPartitionConstraint = partitionConstraint;
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
	
	
	protected StopCondition createFindPeerStopCondition(final int start, final char[] pair, final char escapeChar) {
		return (escapeChar == (char)0) ?
				new CharacterMatchCondition(pair) : new ExtCharacterMatchCondition(pair, escapeChar);
	}
	
	protected int createForwardBound(final int start) throws BadLocationException {
		return fDocument.getLength();
	}
	
	protected int createBackwardBound(final int start) throws BadLocationException {
		return -1;
	}
	
	public int findClosingPeer(final int start, final char[] pair) {
		return findClosingPeer(start, pair, (char) 0);
	}
	
	public int findClosingPeer(int start, final char[] pair, final char escapeChar) {
		Assert.isNotNull(fDocument);
		Assert.isTrue(start >= 0);
		
		try {
			final StopCondition cond = createFindPeerStopCondition(start, pair, escapeChar);
			final int bound = createForwardBound(start);
			
			int depth = 1;
			start -= 1;
			while (true) {
				start = scanForward(start + 1, bound, cond);
				if (start == NOT_FOUND)
					return NOT_FOUND;
				
				if (fDocument.getChar(start) == pair[OPENING_PEER])
					depth++;
				else
					depth--;
				
				if (depth == 0)
					return start;
			}
			
		}
		catch (final BadLocationException e) {
			return NOT_FOUND;
		}
	}
	
	public int findOpeningPeer(int start, final char[] pair) {
		if (start >= fDocument.getLength()) {
			start = fDocument.getLength()-1;
		}
		try {
			final StopCondition cond = createFindPeerStopCondition(start, pair, (char) 0);
			final int bound = createBackwardBound(start);
			
			int depth= 1;
			start += 1;
			while (true) {
				start = scanBackward(start - 1, bound, cond);
				if (start == NOT_FOUND)
					return NOT_FOUND;
				
				if (fDocument.getChar(start) == pair[CLOSING_PEER])
					depth++;
				else
					depth--;
				
				if (depth == 0)
					return start;
			}
			
		}
		catch (final BadLocationException e) {
			return NOT_FOUND;
		}
	}
	
	public int findOpeningPeer(int start, final char[] pair, final char escapeChar) {
		Assert.isTrue(start < fDocument.getLength());
		if (escapeChar == (char) 0) {
			return findOpeningPeer(start, pair);
		}
		
		try {
			final StopCondition cond = createFindPeerStopCondition(start, pair, escapeChar);
			final int bound = createBackwardBound(start);
			
			int depth= 1;
			start += 1;
			fLine = fDocument.getLineOfOffset(start);
			while (true) {
				final int[] list = preScanBackward(start - 1, bound, cond);
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
				start = fDocument.getLineOffset(fLine+1);
			}
			
		}
		catch (final BadLocationException e) {
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
	public final int findAnyNonBlankForward(final int position, final int bound, final boolean linebreakIsBlank) {
		return scanForward(position, bound, linebreakIsBlank ?
				getAnyNonWSorLRCondition() : getAnyNonWSCondition());
	}
	
	public final int findAnyNonBlankBackward(final int position, final int bound, final boolean linebreakIsBlank) {
		return scanBackward(position-1, bound, linebreakIsBlank ?
				getAnyNonWSorLRCondition() : getAnyNonWSCondition());
	}
	
	public final int findNonBlankForward(final int position, final int bound, final boolean linebreakIsBlank) {
		return scanForward(position, bound, linebreakIsBlank ?
				getNonWSorLRCondition() : getNonWSCondition());
	}
	
	public final int findNonBlankBackward(final int position, final int bound, final boolean linebreakIsBlank) {
		return scanBackward(position-1, bound, linebreakIsBlank ?
				getNonWSorLRCondition() : getNonWSCondition());
	}
	
	public IRegion findBlankRegion(final int position, final boolean linebreakIsBlank) {
		return findRegion(position, linebreakIsBlank ?
				getAnyNonWSorLRCondition() : getAnyNonWSCondition());
	}
		
	public boolean isBlankLine(final int position) throws BadLocationException {
		final IRegion line = fDocument.getLineInformationOfOffset(position);
		if (line.getLength() > 0) {
			final int nonWhitespace = findAnyNonBlankForward(line.getOffset(), line.getOffset()+line.getLength(), false);
			return (nonWhitespace == NOT_FOUND);
		}
		return true;
	}
	
	public final IRegion findCommonWord(final int position) {
		return findRegion(position, new StopCondition() {
			@Override
			public boolean stop() {
				return (!Character.isLetterOrDigit(fChar));
			}
		});
	}
	
	
	public final int getFirstLineOfRegion(final IRegion region) throws BadLocationException {
		return fDocument.getLineOfOffset(region.getOffset());
	}
	
	public final int getLastLineOfRegion(final IRegion region) throws BadLocationException {
		if (region.getLength() == 0) {
			return fDocument.getLineOfOffset(region.getOffset());
		}
		return fDocument.getLineOfOffset(region.getOffset()+region.getLength()-1);
	}
	
	
	private final int[] preScanBackward(final int start, final int bound, final StopCondition condition) throws BadLocationException {
		final IntList list = new ArrayIntList();
		int scanEnd = start+1;
		
		NEXT_LINE: while (list.isEmpty() && fLine >= 0) {
			final int lineOffset = fDocument.getLineOffset(fLine);
			int next = lineOffset - 1;
			while ((next = scanForward(next + 1, scanEnd, condition)) != NOT_FOUND) {
				if (bound < next)
					list.add(next);
			}
			
			fLine--;
			if (lineOffset <= bound) {
				break NEXT_LINE;
			}
			
			scanEnd = lineOffset;
		}
		
		if (!list.isEmpty()) {
			return list.toArray();
		}
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
	protected final int scanForward(final int start, int bound, final StopCondition condition) {
		if (bound == UNBOUND) {
			bound = fDocument.getLength();
		}
		assert(bound <= fDocument.getLength());
		assert(start >= 0);
		
		try {
			fPos = start;
			while (fPos < bound) {
				fChar = fDocument.getChar(fPos);
				if (condition.stop()) {
					return fPos;
				}
				fPos = condition.nextPositionForward();
			}
			fPos = bound;
			fChar = (fPos >= 0 && fPos < fDocument.getLength()) ? fDocument.getChar(fPos) : (char) -1;
		}
		catch (final BadLocationException e) {
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
	public final int scanForward(final int position, final int bound, final char ch) {
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
	public final int scanForward(final int position, final int bound, final char[] chars) {
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
	protected final int scanBackward(final int start, int bound, final StopCondition condition) {
		if (bound == UNBOUND) {
			bound = -1;
		}
		assert(bound >= -1);
//		assert(start == 0 || start < fDocument.getLength() );
		
		try {
			if (fDocument.getLength() > 0) {
				fPos = start;
				while (fPos > bound) {
					fChar = fDocument.getChar(fPos);
					if (condition.stop()) {
						return fPos;
					}
					fPos = condition.nextPositionBackward();
				}
			}
			fPos = bound;
			fChar = (fPos >= 0 && fPos < fDocument.getLength()) ? fDocument.getChar(fPos) : (char) -1;
		}
		catch (final BadLocationException e) {
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
	public final int scanBackward(final int position, final int bound, final char ch) {
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
	public final int scanBackward(final int position, final int bound, final char[] chars) {
		return scanBackward(position, bound, new CharacterMatchCondition(chars));
	}
	
	
	public final int count(int start, final int stop, final char c) {
		int count = 0;
		final CharacterMatchCondition condition = new CharacterMatchCondition(c);
		while (start < stop && (start = scanForward(start, stop, condition)) != NOT_FOUND) {
			count++;
			start++;
		}
		return count;
	}
	
	
	protected final IRegion findRegion(final int position, final StopCondition condition) {
		return findRegion(position, condition, false);
	}
	
	protected final IRegion findRegion(final int position, final StopCondition condition, final boolean allowClosing) {
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
	 *     partition if accessing the document fails
	 */
	protected final String getContentType() {
		try {
			return TextUtilities.getContentType(fDocument, fPartitioning.getPartitioning(), fPos, false);
		}
		catch (final BadLocationException e) {
			return null; // ?
		}
	}
	
	/**
	 * Returns the partition at current position of the scanner (#fPos).
	 * 
	 * @param position the position to get the partition for
	 * @return the partition at <code>position</code> or a dummy zero-length
	 *     partition if accessing the document fails
	 */
	protected final ITypedRegion getPartition() {
		try {
			return TextUtilities.getPartition(fDocument, fPartitioning.getPartitioning(), fPos, false);
		}
		catch (final BadLocationException e) {
			return new TypedRegion(fPos, 0, "__no_partition_at_all"); //$NON-NLS-1$
		}
	}
	
	/**
	 * Returns the partition at <code>position</code>.
	 * 
	 * @param position the position to get the partition for
	 * @return the partition at <code>position</code> or a dummy zero-length
	 *     partition if accessing the document fails
	 */
	public final ITypedRegion getPartition(final int position) {
		try {
			return TextUtilities.getPartition(fDocument, fPartitioning.getPartitioning(), position, false);
		}
		catch (final BadLocationException e) {
			return new TypedRegion(fPos, 0, "__no_partition_at_all"); //$NON-NLS-1$
		}
	}
	
}
