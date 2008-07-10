/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.text;

import static de.walware.eclipsecommons.ltk.text.ITokenScanner.CLOSING_PEER;
import static de.walware.eclipsecommons.ltk.text.ITokenScanner.OPENING_PEER;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ICharacterPairMatcher;

import de.walware.eclipsecommons.ltk.text.BasicHeuristicTokenScanner;
import de.walware.eclipsecommons.ltk.text.ITokenScanner;
import de.walware.eclipsecommons.ltk.text.PartitioningConfiguration;


/**
 * Helper class for match pairs of characters.
 */
public class PairMatcher implements ICharacterPairMatcher {
	
	
	private static char IGNORE = '\n';
	
	protected char[][] fPairs;
	protected IDocument fDocument;
	protected int fOffset;
	
	protected int fStartPos;
	protected int fEndPos;
	protected int fAnchor;
	protected String fPartition;
	
	protected String fPartitioning;
	protected String[] fApplicablePartitions;
	protected char fEscapeChar;
	
	protected ITokenScanner fScanner;
	
	
	public PairMatcher(final char[][] pairs, final String partitioning, final String[] partitions, final ITokenScanner scanner, final char escapeChar) {
		fPairs = pairs;
		fScanner = scanner;
		fPartitioning = partitioning;
		fApplicablePartitions = partitions;
		fEscapeChar = escapeChar;
	}
	
	/**
	 * constructor using <code>BasicHeuristicTokenScanner</code>, without escapeChar.
	 */
	public PairMatcher(final char[][] pairs, final PartitioningConfiguration partitioning, final String[] partitions) {
		this(pairs, partitioning.getPartitioning(), partitions, new BasicHeuristicTokenScanner(partitioning), (char) 0);
	}
	
	/**
	 * Constructor using <code>BasicHeuristicTokenScanner</code>.
	 */
	public PairMatcher(final char[][] pairs, final PartitioningConfiguration partitioning, final String[] partitions, final char escapeChar) {
		this(pairs, partitioning.getPartitioning(), partitions, new BasicHeuristicTokenScanner(partitioning), escapeChar);
	}
	
	
	public IRegion match(final IDocument document, final int offset) {
		fOffset = offset;
		if (fOffset < 0) {
			return null;
		}
		
		fDocument = document;
		if (document != null && matchPairsAt() && fStartPos != fEndPos) {
			fDocument = null;
			return new Region(fStartPos, fEndPos - fStartPos + 1);
		}
		
		fDocument = null;
		return null;
	}
	
	public int getAnchor() {
		return fAnchor;
	}
	
	public void dispose() {
		clear();
	}
	
	public void clear() {
	}
	
	/**
	 * Search Pairs
	 */
	protected boolean matchPairsAt() {
		fStartPos = -1;
		fEndPos = -1;
		
		// get the chars preceding and following the start position
		try {
			final ITypedRegion thisPartition = TextUtilities.getPartition(fDocument, fPartitioning, fOffset, false);
			final ITypedRegion prevPartition = (fOffset > 0) ? TextUtilities.getPartition(fDocument, fPartitioning, fOffset-1, false) : null;
			
			char thisChar = IGNORE;
			char prevChar = IGNORE;
			final int thisPart = checkPartition(thisPartition.getType());
			if (thisPart >= 0 && fOffset < fDocument.getLength()) {
				thisChar = fDocument.getChar(fOffset);
			}
			
			// check, if escaped
			int prevPart = -1;
			if (prevPartition != null) {
				prevPart = checkPartition(prevPartition.getType());
				if (prevPart >= 0) {
					prevChar = fDocument.getChar(fOffset-1);
					final int partitionOffset = prevPartition.getOffset();
					int checkOffset = fOffset-2;
					final char escapeChar = getEscapeChar(prevPartition.getType());
					while (checkOffset >= partitionOffset) {
						if (fDocument.getChar(checkOffset) == escapeChar) {
							checkOffset--;
						}
						else {
							break;
						}
					}
					if ( (fOffset - checkOffset) % 2 == 1) {
						// prev char is escaped
						prevChar = IGNORE;
					}
					else if (prevPart == thisPart && prevChar == escapeChar) {
						// this char is escaped
						thisChar = IGNORE;
					}
				}
			}
			
			final int pairIdx = findChar(prevChar, prevPart, thisChar, thisPart);
			
			if (fStartPos > -1) {  		// closing peer
				fAnchor = LEFT;
				fEndPos = searchForClosingPeer(fPairs[pairIdx]);
				if (fEndPos > -1)
					return true;
				else
					fStartPos = -1;
			}
			else if (fEndPos > -1) {  	// opening peer
				fAnchor = RIGHT;
				fStartPos = searchForOpeningPeer(fPairs[pairIdx]);
				if (fStartPos > -1)
					return true;
				else
					fEndPos = -1;
			}
			
		} catch (final BadLocationException x) {
		} // ignore
		
		return false;
	}
	
	private int checkPartition(final String id) {
		for (int i = 0; i < fApplicablePartitions.length; i++) {
			if (fApplicablePartitions[i].equals(id)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * @param prevChar
	 * @param thisChar
	 * @return
	 */
	private int findChar(final char prevChar, final int prevPart, final char thisChar, final int thisPart) {
		// search order 3{2 1}4
		for (int i = 0; i < fPairs.length; i++) {
			if (thisChar == fPairs[i][CLOSING_PEER]) {
				fEndPos = fOffset;
				fPartition = fApplicablePartitions[thisPart];
				return i;
			}
		}
		for (int i = 0; i < fPairs.length; i++) {
			if (prevChar == fPairs[i][OPENING_PEER]) {
				fStartPos = fOffset-1;
				fPartition = fApplicablePartitions[prevPart];
				return i;
			}
		}
		for (int i = 0; i < fPairs.length; i++) {
			if (thisChar == fPairs[i][OPENING_PEER]) {
				fStartPos = fOffset;
				fPartition = fApplicablePartitions[thisPart];
				return i;
			}
		}
		for (int i = 0; i < fPairs.length; i++) {
			if (prevChar == fPairs[i][CLOSING_PEER]) {
				fEndPos = fOffset-1;
				fPartition = fApplicablePartitions[prevPart];
				return i;
			}
		}
		fPartition = null;
		return -1;
	}
	
	protected int searchForClosingPeer(final char[] pair) throws BadLocationException {
		fScanner.configure(fDocument, fPartition);
		return fScanner.findClosingPeer(fStartPos + 1, pair, getEscapeChar(fPartition));
	}
	
	protected int searchForOpeningPeer(final char[] pair) throws BadLocationException {
		fScanner.configure(fDocument, fPartition);
		return fScanner.findOpeningPeer(fEndPos - 1, pair, getEscapeChar(fPartition));
	}
	
	protected char getEscapeChar(final String contentType) {
		return fEscapeChar;
	}
	
	/**
	 * @return Returns the fPairs.
	 */
	public char[][] getPairs() {
		return fPairs;
	}
	
}
