/*******************************************************************************
 * Copyright (c) 2005-2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.text.rd;

import static de.walware.statet.r.ui.IRDocumentPartitions.RDOC_COMMENT;
import static de.walware.statet.r.ui.IRDocumentPartitions.RDOC_PLATFORM_SPECIF;

import java.util.EnumMap;
import java.util.Map;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import de.walware.eclipsecommons.ui.text.BufferedDocumentScanner;


/**
 * This scanner recognizes the comments, platform specif., verbatim-like section
 * (and other/usual Rd code).
 */
public class RdFastPartitionScanner implements IPartitionTokenScanner {	
	
	/** Enum of states of the scanner. */	
	private static enum State { DEFAULT, COMMENT, PLATFORM };
	
	/** Enum of last significant characters read. */
	private static enum Last { NONE, BACKSLASH, NEW_LINE };
	
	private static final char[][] PLATFORM_KEYWORDS = {			// without '#'
			"ifdef".toCharArray(), "ifndef".toCharArray(), "endif".toCharArray() };
	
	
	/** The scanner. */
	private final BufferedDocumentScanner fScanner = new BufferedDocumentScanner(1000);	// faster implementation
	
	/** The offset of the last returned token. */
	private int fTokenOffset;
	/** The length of the last returned token. */
	private int fTokenLength;
	
	/** The state of the scanner. */	
	private State fState;
	/** The last significant characters read. */
	private Last fLast;
	/** The amount of characters already read on first call to nextToken(). */
	private int fPrefixLength;
	
	private final Map<State, IToken> fTokens = new EnumMap<State, IToken>(State.class);

	{	
		fTokens.put(State.DEFAULT, new Token(null));
		fTokens.put(State.COMMENT, new Token(RDOC_COMMENT));
		fTokens.put(State.PLATFORM, new Token(RDOC_PLATFORM_SPECIF));
	}
	
	public RdFastPartitionScanner() {
	}


	public void setRange(IDocument document, int offset, int length) {

		setPartialRange(document, offset, length, null, -1);
	}
	
	public void setPartialRange(IDocument document, int offset, int length, String contentType, int partitionOffset) {

		if (partitionOffset < 0) {
			partitionOffset = offset;
		}
		fScanner.setRange(document, offset, length);
		fTokenOffset = partitionOffset;
		fTokenLength = 0;
		fPrefixLength = offset - partitionOffset;
		fLast = (fScanner.getColumn() == 0) ? Last.NEW_LINE : Last.NONE;
		fState = (offset == partitionOffset) ? State.DEFAULT : getState(contentType);
	}
	

	public IToken nextToken() {
		
		fTokenOffset += fTokenLength;
		fTokenLength = fPrefixLength;
		
		CHECK_NEXT: while (true) {
			final int ch = fScanner.read();
			
			// characters
	 		switch (ch) {
	 		case ICharacterScanner.EOF:
	 			fLast = Last.NONE;
	 			fPrefixLength = 0;
		 		if (fTokenLength > 0) {
		 			return fTokens.get(fState);
		 		} else {
					return Token.EOF;
		 		}

	 		case '\r':
	 		case '\n':
				switch (fState) {
				case COMMENT:
				case PLATFORM:
					return preFinish(fState, State.DEFAULT, Last.NEW_LINE, 1);

				default:
					consume(Last.NEW_LINE);
					continue CHECK_NEXT;
				}
	
	 		}
	 		
	 		if (fState == State.DEFAULT) {

		 	    if (fLast == Last.BACKSLASH) {
					consume(Last.NONE);
					continue CHECK_NEXT;
	 			}
		 		
		 		switch (ch) {
		 		case '\\':
					consume(Last.BACKSLASH);
					continue CHECK_NEXT;

		 		case '#':
					if (fLast == Last.NEW_LINE) {
						for (int i = 0; i < PLATFORM_KEYWORDS.length; i++) {
							if (searchWord(PLATFORM_KEYWORDS[i])) {
								int length = PLATFORM_KEYWORDS[i].length + 1;
								if (fTokenLength >= length) {
									return preFinish(fState, State.PLATFORM, Last.NONE, length);
								} else {
									prepareNew(State.PLATFORM, Last.NONE, length);
									continue CHECK_NEXT;
								}
							}
						}
					}
					consume(Last.NONE);
					continue CHECK_NEXT;

		 		case '%':
					if (fTokenLength > 0) {
						return preFinish(fState, State.COMMENT, Last.NONE, 1);
					} else {
						prepareNew(State.COMMENT, Last.NONE, 1);
						continue CHECK_NEXT;
					}
					
	 	    	default:
					consume(Last.NONE);
					continue CHECK_NEXT;
		 		}
		 		
	 		} else {
				consume(Last.NONE);
				continue CHECK_NEXT;
	 		}				
		} 
 	}

	private final void consume(Last last) {
		fTokenLength++;
		fLast = last;	
	}
	
	private final boolean searchWord(char[] word) {
		for (int i = 0; i < word.length; i++) {
			final int ch = fScanner.read();
			if (ch != word[i]) {
				for (;i >= 0; i--) {
					fScanner.unread();
				}
				return false;
			}
		}
		fTokenLength += word.length;
		return true;
	}
	
//	private final IToken postFinish(int state) {
//		
//		fTokenLength++;
//		fLast = NONE;
//		fState = inVerbatim? S_VERBATIM : State.DEFAULT;
//		fPrefixLength = 0;		
//		return fTokens[state];
//	}

	private final IToken preFinish(State state, State newState, Last last, int prefixLength) {
		fLast = last;
		fState = newState;
		fTokenLength -= (prefixLength-1);
		fPrefixLength = prefixLength;
		return fTokens.get(state);
	}

	private final void prepareNew(State newState, Last last, int prefixLength) {
		fLast = last;
		fState = newState;
		fTokenOffset += fTokenLength - (prefixLength-1);
		fTokenLength = prefixLength;
		fPrefixLength = 0;
	}

	private static State getState(String contentType) {

		if (contentType == null)
			return State.DEFAULT;

		else if (contentType.equals(RDOC_COMMENT))
			return State.COMMENT;

		else if (contentType.equals(RDOC_PLATFORM_SPECIF))
			return State.PLATFORM;

		return State.DEFAULT;
	}
	
	
	public int getTokenLength() {
		return fTokenLength;
	}

	public int getTokenOffset() {
		return fTokenOffset;
	}

}
