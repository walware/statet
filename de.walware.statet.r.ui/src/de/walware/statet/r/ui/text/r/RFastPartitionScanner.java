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

package de.walware.statet.r.ui.text.r;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import de.walware.eclipsecommons.ui.text.BufferedDocumentScanner;

import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.internal.ui.RUIPlugin;


/**
 * This scanner recognizes the comments, platform specif., verbatim-like section
 * (and other/usual Rd code).
 */
public class RFastPartitionScanner implements IPartitionTokenScanner {
	
	
	/**
	 * Enum of states of the scanner.
	 * Note: id is index in array of tokens
	 * 0-7 are reserved.
	 **/
	protected static final int S_DEFAULT = 0;
	protected static final int S_INFIX = 1;
	protected static final int S_STRING = 2;
	protected static final int S_COMMENT = 3;
	
	protected final static IToken T_DEFAULT = new Token(null);
	protected final static IToken T_INFIX = new Token(IRDocumentPartitions.R_INFIX_OPERATOR);
	protected final static IToken T_STRING = new Token(IRDocumentPartitions.R_STRING);
	protected final static IToken T_COMMENT = new Token(IRDocumentPartitions.R_COMMENT);
	
	
	/** Enum of last significant characters read. */
	protected static final int LAST_OTHER = 0;
	protected static final int LAST_BACKSLASH = 1;
	protected static final int LAST_NEWLINE = 2;
	
	
	/** The scanner. */
	private final BufferedDocumentScanner fScanner = new BufferedDocumentScanner(1000);	// faster implementation
	
	private IDocument fDocument;
	
	private IToken fToken;
	/** The offset of the last returned token. */
	private int fTokenOffset;
	/** The length of the last returned token. */
	private int fTokenLength;
	
	private int fStartPartitionState = S_DEFAULT;
	/** The current state of the scanner. */
	private int fState;
	/** The last significant characters read. */
	protected int fLast;
	/** The amount of characters already read on first call to nextToken(). */
	private int fPrefixLength;
	
	private char fEndChar;
	
	private final IToken[] fTokens;
	
	
	public RFastPartitionScanner() {
		final Map<Integer, IToken> list = new HashMap<Integer, IToken>();
		initTokens(list);
		final int count = maxState(list.keySet())+1;
		fTokens = new IToken[count];
		for (int i = 0; i < count; i++) {
			fTokens[i] = list.get(i);
		}
	}
	
	private int maxState(final Set<Integer> states) {
		int max = 0;
		final Iterator<Integer> iter = states.iterator();
		while (iter.hasNext()) {
			final int state = iter.next().intValue();
			if (state > max) {
				max = state;
			}
		}
		return max;
	}
	
	protected void initTokens(final Map<Integer, IToken> states) {
		states.put(S_DEFAULT, T_DEFAULT);
		states.put(S_INFIX, T_INFIX);
		states.put(S_STRING, T_STRING);
		states.put(S_COMMENT, T_COMMENT);
	}
	
	/**
	 * Sets explicitly the partition type on position 0.
	 * 
	 * @param contentType
	 */
	public void setStartPartitionType(final String contentType) {
		fStartPartitionState = getState(contentType);
	}
	
	public void setRange(final IDocument document, final int offset, final int length) {
		setPartialRange(document, offset, length, null, -1);
	}
	
	public void setPartialRange(final IDocument document, final int offset, final int length, final String contentType, int partitionOffset) {
		if (partitionOffset < 0) {
			partitionOffset = offset;
		}
		fDocument = document;
		fScanner.setRange(document, offset, length);
		fTokenOffset = partitionOffset;
		fTokenLength = 0;
		fPrefixLength = offset - partitionOffset;
		fLast = LAST_OTHER;
		if (offset > 0) {
			try {
				final char c = document.getChar(offset-1);
				switch (c) {
				case '\r':
				case '\n':
					fLast = LAST_NEWLINE;
					break;
				}
			} catch (final BadLocationException e) {
				RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "Error occured when detect last char.", e);
			}
			fState = (fPrefixLength == 0) ? S_DEFAULT : getState(contentType);
		}
		else {
			fLast = LAST_NEWLINE;
			fState = fStartPartitionState;
		}
		if (fPrefixLength > 0 && fState == S_STRING) {
			try {
				fEndChar = document.getChar(fTokenOffset);
			} catch (final BadLocationException e) {
				RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "Error occured when detect start char.", e);
			}
		}
	}
	
	
	public IToken nextToken() {
		fToken = null;
		fTokenOffset += fTokenLength;
		fTokenLength = fPrefixLength;
		
		CHECK_NEXT: while (fToken == null) {
			final int c = fScanner.read();
			
			// characters
			if (c == ICharacterScanner.EOF) {
				fPrefixLength = 0;
				if (fTokenLength > 0) {
					handleEOF(fState);
					fToken = fTokens[fState];
					break CHECK_NEXT;
				}
				fToken = Token.EOF;
				break CHECK_NEXT;
			}
			
			fTokenLength++;
			handleChar(fState, c);
			continue CHECK_NEXT;
		}
		return fToken;
	}
	
	protected void handleChar(final int state, final int c) {
		switch (state) {
		case S_DEFAULT:
			fLast = LAST_OTHER;
			switch (c) {
			case '\r':
			case '\n':
				fLast = LAST_NEWLINE;
				return;
			case '"':
				fEndChar = '"';
				newState(S_STRING, 1);
				return;
			case '\'':
				fEndChar = '\'';
				newState(S_STRING, 1);
				return;
			case '`':
				fEndChar = '`';
				newState(S_STRING, 1);
				return;
			case '#':
				newState(S_COMMENT, 1);
				return;
			case '%':
				newState(S_INFIX, 1);
				return;
			default: // Standard
				return;
			}
		
		case S_INFIX:
			if (c == '%') {
				newState(S_DEFAULT, 0);
				return;
			}
			if (c == '\r' || c == '\n') {
				fLast = LAST_NEWLINE;
				newState(S_DEFAULT, 1);
				return;
			}
			return;
			
		case S_STRING:
			// Escaped?
			if (fLast == LAST_BACKSLASH) {
				fLast = LAST_OTHER;
				return;
			}
			// Backslash?
			if (c == '\\') {
				fLast = LAST_BACKSLASH;
				return;
			}
			if (c == '\r' || c == '\n') {
				fLast = LAST_NEWLINE;
				return;
			}
			fLast = LAST_OTHER;
			// String Ende?
			if (c == fEndChar) {
				newState(S_DEFAULT, 0);
				return;
			}
			// Standard
			return;
		
		case S_COMMENT:
			if (c == '\r' || c == '\n') {
				fLast = LAST_NEWLINE;
				newState(S_DEFAULT, 1);
				return;
			}
			return;
			
		default:
			handleExtState(state, c);
			return;
		}
	}
	
	protected void handleEOF(final int state) {
	}
	
	protected void handleExtState(final int state, final int c) {
		if (c == '\r' || c == '\n') {
			fLast = LAST_NEWLINE;
			return;
		}
	}
	
	protected final void newState(final int newState, final int prefixLength) {
		if (fTokenLength-prefixLength > 0) {
			fToken = fTokens[fState];
			fState = newState;
			fTokenLength -= prefixLength;
			fPrefixLength = prefixLength;
			return;
		}
//		assert (fTokenLength == 0);
		fState = newState;
		fTokenLength = prefixLength;
		fPrefixLength = 0;
	}
	
	protected final int getState(String contentType) {
		if (contentType == null) {
			return S_DEFAULT;
		}
		contentType = contentType.intern();
		if (contentType == IRDocumentPartitions.R_DEFAULT || contentType == IRDocumentPartitions.R_DEFAULT_EXPL) {
			return S_DEFAULT;
		}
		if (contentType == IRDocumentPartitions.R_STRING) {
			return S_STRING;
		}
		if (contentType == IRDocumentPartitions.R_COMMENT) {
			return S_COMMENT;
		}
		if (contentType == IRDocumentPartitions.R_INFIX_OPERATOR) {
			return S_INFIX;
		}
		return getExtState(contentType);
	}
	
	protected int getExtState(final String contentType) {
		return S_DEFAULT;
	}
	
	
	public int getTokenLength() {
		return fTokenLength;
	}
	
	public int getTokenOffset() {
		return fTokenOffset;
	}
	
	
	protected final boolean readChar(final char c1) {
		final int c = fScanner.read();
		if (c == c1) {
			fTokenLength ++;
			return true;
		}
		if (c >= 0) fScanner.unread();
		return false;
	}
	
	protected final IDocument getDocument() {
		return fDocument;
	}
	
}
