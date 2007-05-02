/*******************************************************************************
 * Copyright (c) 2005-2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.text.r;

import java.util.EnumMap;
import java.util.Map;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import de.walware.eclipsecommons.ui.text.BufferedDocumentScanner;

import de.walware.statet.r.ui.IRDocumentPartitions;


/**
 * This scanner recognizes the comments, platform specif., verbatim-like section
 * (and other/usual Rd code).
 */
public class RFastPartitionScanner implements IPartitionTokenScanner {	
	
	
	/** Enum of states of the scanner. */	
	private static enum State { DEFAULT, INFIX, STRING, COMMENT };
	
	/** Enum of last significant characters read. */
	private static enum Last { NONE, BACKSLASH };
	
	
	/** The scanner. */
	private final BufferedDocumentScanner fScanner = new BufferedDocumentScanner(1000);	// faster implementation
	
	/** The offset of the last returned token. */
	private int fTokenOffset;
	/** The length of the last returned token. */
	private int fTokenLength;
	
	private State fStartPartitionState = State.DEFAULT;
	/** The current state of the scanner. */	
	private State fState;
	/** The new state switch of the scanner. */	
	private State fNewState;
	/** The last significant characters read. */
	private Last fLast;
	/** The amount of characters already read on first call to nextToken(). */
	private int fPrefixLength;
	
	private char fEndChar;
	
	private final Map<State, IToken> fTokens = new EnumMap<State, IToken>(State.class);

	{	
		fTokens.put(State.DEFAULT, new Token(null));
		fTokens.put(State.INFIX, new Token(IRDocumentPartitions.R_INFIX_OPERATOR));
		fTokens.put(State.STRING, new Token(IRDocumentPartitions.R_STRING));
		fTokens.put(State.COMMENT, new Token(IRDocumentPartitions.R_COMMENT));
	}
	
	public RFastPartitionScanner() {
	}

	/**
	 * Sets explicitly the partition type on position 0.
	 * 
	 * @param contentType
	 */
	public void setStartPartitionType(String contentType) {
		
		fStartPartitionState = getState(contentType);
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
		fLast = Last.NONE;
		if (offset == 0) {
			fState = fStartPartitionState;
		}
		else {
			fState = (fPrefixLength == 0) ? State.DEFAULT : getState(contentType);
		}
	}
	

	public IToken nextToken() {
		
		fTokenOffset += fTokenLength;
		fTokenLength = fPrefixLength;
		
		CHECK_NEXT: while (true) {
			final int c = fScanner.read();
			
			// characters
	 		switch (c) {
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
				case INFIX:
					fNewState = State.DEFAULT;
					return preFinish(Last.NONE, 1);

				default:
					consume(Last.NONE);
					continue CHECK_NEXT;
				}
	
	 		}
	 		
	 		switch (fState) {
	 		case DEFAULT:
	 			switch (c) {
	 			case '"':
	 				fEndChar = '"';
	 				fNewState = State.STRING;
	 				break;
	 			case '\'':
	 				fEndChar = '\'';
	 				fNewState = State.STRING;
	 				break;
	 			case '#':
	 				fNewState = State.COMMENT;
	 				break;
	 			case '%':
	 				fNewState = State.INFIX;
	 				break;
	 			default:
		 			// Standard
					consume(Last.NONE);
					continue CHECK_NEXT;
	 			}
				if (fTokenLength > 0) {
					return preFinish(Last.NONE, 1);
				} else {
					prepareNew(Last.NONE, 1);
					continue CHECK_NEXT;
				}
			
	 		case INFIX:
	 			if (c == '%') {
	 				return postFinish();
	 			}
				consume(Last.NONE);
				continue CHECK_NEXT;
	 			
	 		case STRING:
	 			// Escaped?
		 	    if (fLast == Last.BACKSLASH) {
					consume(Last.NONE);
					continue CHECK_NEXT;
	 			}
		 	    // Backslash?
		 	    if (c == '\\') {
		 	    	consume(Last.BACKSLASH);
		 	    	continue CHECK_NEXT;
		 	    }
		 	    // String Ende?
		 	    if (c == fEndChar) {
		 	    	return postFinish();
		 	    }
	 			// Standard
				consume(Last.NONE);
				continue CHECK_NEXT;
			
	 		default: // COMMENT
				consume(Last.NONE);
				continue CHECK_NEXT;
		 	    
	 		}
		} 
 	}

	private final void consume(Last last) {
		
		fTokenLength++;
		fLast = last;	
	}
	
	private final IToken postFinish() {
		
		IToken token = fTokens.get(fState);
		fTokenLength++;
		fLast = Last.NONE;
		fState = State.DEFAULT;
		fPrefixLength = 0;		
		return token;
	}

	private final IToken preFinish(Last last, int prefixLength) {
		
		IToken token = fTokens.get(fState);
		fLast = last;
		fState = fNewState;
		fTokenLength -= (prefixLength-1);
		fPrefixLength = prefixLength;
		return token;
	}

	private final void prepareNew(Last last, int prefixLength) {
		
		fLast = last;
		fState = fNewState;
		fTokenOffset += fTokenLength - (prefixLength-1);
		fTokenLength = prefixLength;
		fPrefixLength = 0;
	}

	private static State getState(String contentType) {

		if (contentType == null) {
			return State.DEFAULT;
		}
		else if (contentType.equals(IRDocumentPartitions.R_STRING)) {
			return State.STRING;
		}
		else if (contentType.equals(IRDocumentPartitions.R_COMMENT)) {
			return State.COMMENT;
		}
		else if (contentType.equals(IRDocumentPartitions.R_INFIX_OPERATOR)) {
			return State.INFIX;
		}
		return State.DEFAULT;
	}
	
	
	public int getTokenLength() {
		
		return fTokenLength;
	}

	public int getTokenOffset() {
		
		return fTokenOffset;
	}

}
