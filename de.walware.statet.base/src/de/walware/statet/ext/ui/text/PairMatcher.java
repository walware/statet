/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.text;

import static de.walware.statet.ext.ui.text.ITokenScanner.CLOSING_PEER;
import static de.walware.statet.ext.ui.text.ITokenScanner.OPENING_PEER;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ICharacterPairMatcher;


/**
 * Helper class for match pairs of characters.
 *
 * @author Stephan Wahlbrink
 */
public class PairMatcher implements ICharacterPairMatcher {
	
	
	protected char[][] fPairs;
	protected IDocument fDocument;
	protected int fOffset;
	
	protected int fStartPos;
	protected int fEndPos;
	protected int fAnchor;
	
	protected String fPartitioning;
	protected String fPartition;
	protected char fEscapeChar;

	protected ITokenScanner fScanner;
	
	public PairMatcher(char[][] pairs, String partitioning, String partition, ITokenScanner scanner, char escapeChar) {
		
		fPairs = pairs;
		fScanner = scanner;
		fPartitioning = partitioning;
		fPartition = partition;
		fEscapeChar = escapeChar;
	}
	
	/**
	 * constructor using <code>BasicHeuristicTokenScanner</code>, without escapeChar.
	 */
	public PairMatcher(char[][] pairs, String partitioning, String partition) {
	
		this(pairs, partitioning, partition, new BasicHeuristicTokenScanner(partitioning), (char)0);
	}

	/**
	 * Constructor using <code>BasicHeuristicTokenScanner</code>.
	 */
	public PairMatcher(char[][] pairs, String partitioning, String partition, char escapeChar) {
	
		this(pairs, partitioning, partition, new BasicHeuristicTokenScanner(partitioning), escapeChar);
	}

	/*
	 * @see org.eclipse.jface.text.source.ICharacterPairMatcher#match(org.eclipse.jface.text.IDocument, int)
	 */
	public IRegion match(IDocument document, int offset) {

		fOffset = offset;

		if (fOffset < 0)
			return null;

		fDocument = document;
		if (document != null && matchPairsAt() && fStartPos != fEndPos) {
			return new Region(fStartPos, fEndPos - fStartPos + 1);
		}

		return null;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.ICharacterPairMatcher#getAnchor()
	 */
	public int getAnchor() {
		return fAnchor;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.ICharacterPairMatcher#dispose()
	 */
	public void dispose() {
		clear();
	}
	
	/*
	 * @see org.eclipse.jface.text.source.ICharacterPairMatcher#clear()
	 */
	public void clear() {
		fDocument = null;
	}
	
	/**
	 * Search Pairs
	 */
	protected boolean matchPairsAt() {

		int i;
		int pairIndex = -1;

		fStartPos = -1;
		fEndPos = -1;

		// get the chars preceding and following the start position
		try {

			char prevChar = (fOffset > 0 && fPartition.equals(TextUtilities.getPartition(fDocument, fPartitioning, fOffset-1, false).getType())) ? 
							fDocument.getChar(fOffset-1) : '\n';
			char thisChar = (fOffset == fDocument.getLength()) ? '\n' : fDocument.getChar(fOffset);

			boolean escaped = (fEscapeChar == prevChar);
			// search for opening peer character next to the activation point
			for (i = 0; i < fPairs.length; i++) {
				if (prevChar == fPairs[i][OPENING_PEER]) {
					fStartPos = fOffset - 1;
					pairIndex = i;
					break;	// ignore currentChar
				}
				else if (thisChar == fPairs[i][OPENING_PEER] && !escaped) {
					fStartPos = fOffset;
					pairIndex = i;
				} 
			}
			
			// search for closing peer character next to the activation point
			for (i = 0; i < fPairs.length; i++) {
				if (thisChar == fPairs[i][CLOSING_PEER] && !escaped) {
					fEndPos = fOffset;
					pairIndex = i;
					break;  // ignore prevChar
				}
				else if (prevChar == fPairs[i][CLOSING_PEER]) {
					fEndPos = fOffset - 1;
					pairIndex = i;
				} 
			}

			if (fStartPos > -1) {  		// closing peer
				fAnchor = LEFT;
				fEndPos = searchForClosingPeer(fStartPos, fPairs[pairIndex]);
				if (fEndPos > -1)
					return true;
				else
					fStartPos = -1;
			}
			else if (fEndPos > -1) {  	// opening peer
				fAnchor = RIGHT;
				fStartPos = searchForOpeningPeer(fEndPos, fPairs[pairIndex]);
				if (fStartPos > -1)
					return true;
				else
					fEndPos = -1;
			}

		} catch (BadLocationException x) {
		} // ignore

		return false;
	}
	
	protected int searchForClosingPeer(int offset, char[] pair) throws BadLocationException {
		
		fScanner.configure(fDocument, fPartition);
		return fScanner.findClosingPeer(offset + 1, pair, fEscapeChar);
	}
	
	protected int searchForOpeningPeer(int offset, char[] pair) throws BadLocationException {
		
		fScanner.configure(fDocument, fPartition);
		return fScanner.findOpeningPeer(offset - 1, pair, fEscapeChar);
	}
	
	
	/**
	 * @return Returns the fPairs.
	 */
	public char[][] getPairs() {
		return fPairs;
	}
}


