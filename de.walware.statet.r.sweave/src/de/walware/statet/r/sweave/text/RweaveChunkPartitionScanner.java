/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.sweave.text;

import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.internal.sweave.Rweave;
import de.walware.statet.r.ui.text.r.RFastPartitionScanner;


/**
 * Partition scanner for R chunks (stops if find '@' at column 0).
 */
public class RweaveChunkPartitionScanner extends RFastPartitionScanner implements ICatPartitionTokenScanner {
	
	
	private static final int S_CHUNKCONTROL = 8;
	private static final int S_CHUNKCOMMENT = 9;
	
	private static final IToken T_R_DEFAULT = new Token(IRDocumentPartitions.R_DEFAULT_EXPL);
	private static final IToken T_CHUNKCONTROL = new Token(Rweave.CHUNK_CONTROL_CONTENT_TYPE);
	private static final IToken T_CHUNKCOMMENT = new Token(Rweave.CHUNK_COMMENT_CONTENT_TYPE);
	
	private static final int LAST_CC_END1 = 9;
	private static final int LAST_CC_END2 = 10;
	private static final int LAST_CC_AT = 11;
	private static final int LAST_CC_BEHINDAT = 12;
	
	
	private MultiCatPartitionScanner fParent;
	private boolean fStopChunkDetected;
	private boolean fIsInChunk;
	
	
	public RweaveChunkPartitionScanner() {
	}
	
	
	public void setParent(final MultiCatPartitionScanner parent) {
		fParent = parent;
	}
	
	@Override
	protected void initTokens(final Map<Integer, IToken> states) {
		super.initTokens(states);
		states.put(S_DEFAULT, T_R_DEFAULT);
		states.put(S_CHUNKCONTROL, T_CHUNKCONTROL);
		states.put(S_CHUNKCOMMENT, T_CHUNKCOMMENT);
	}
	
	@Override
	public void setPartialRange(final IDocument document, final int offset, final int length, final String contentType, final int partitionOffset) {
		fStopChunkDetected = false;
		fIsInChunk = true;
		super.setPartialRange(document, offset, length, contentType, partitionOffset);
	}
	
	@Override
	protected void handleChar(final int state, final int c) {
		if (fLast == LAST_NEWLINE) {
			if (c == '<' && readChar('<')) {
				fLast = LAST_OTHER;
				createStartMarker();
				newState(S_CHUNKCONTROL, 2);
				return;
			}
			if (c == '@') {
				fLast = LAST_CC_AT;
				createStopMarker();
				newState(S_CHUNKCONTROL, 1);
				return;
			}
		}
		super.handleChar(state, c);
	}
	
	private void createStartMarker() {
		fIsInChunk = true;
		try {
			final int lineOffset = getTokenOffset() + getTokenLength() - 2;
			final IDocument document = getDocument();
			final int line = document.getLineOfOffset(lineOffset);
			final int stopOffset = lineOffset + document.getLineLength(line);
			fParent.setControlMarker(lineOffset, 2, stopOffset, 1);
		}
		catch (final BadLocationException e) {
		}
	}
	
	private void createStopMarker() {
		fStopChunkDetected = true;
		try {
			final int lineOffset = getTokenOffset() + getTokenLength() - 1;
			final IDocument document = getDocument();
			final int line = document.getLineOfOffset(lineOffset);
			final int stopOffset = lineOffset + document.getLineLength(line);
			fParent.setControlMarker(lineOffset, 1, stopOffset, 0);
		}
		catch (final BadLocationException e) {
		}
	}
	
	@Override
	protected void handleExtState(final int state, final int c) {
		if (state == S_CHUNKCONTROL) {
			switch (fLast) {
			case LAST_CC_END1:
				fLast = (c == '>') ? LAST_CC_END2 : LAST_OTHER;
				return;
			case LAST_CC_END2:
				fLast = LAST_OTHER;
				newState(S_CHUNKCOMMENT, (c == '=') ? 0 : 1);
				return;
			case LAST_CC_AT:
				fLast = LAST_CC_BEHINDAT;
				newState(S_CHUNKCOMMENT, 0);
				return;
			default:
				switch (c) {
				case '>':
					fLast = LAST_CC_END1;
					return;
				case '\r':
					readChar('\n');
					if (fStopChunkDetected) {
						fIsInChunk = false;
					}
					newState(S_DEFAULT, 0);
					return;
				case '\n':
					readChar('\r');
					if (fStopChunkDetected) {
						fIsInChunk = false;
					}
					newState(S_DEFAULT, 0);
					return;
				default:
					fLast = LAST_OTHER;
					return;
				}
			}
		}
		if (state == S_CHUNKCOMMENT) {
			switch (c) {
			case '\r':
				readChar('\n');
				if (fStopChunkDetected) {
					fIsInChunk = false;
				}
				newState(S_DEFAULT, 0);
				return;
			case '\n':
				readChar('\r');
				if (fStopChunkDetected) {
					fIsInChunk = false;
				}
				newState(S_DEFAULT, 0);
				return;
			default:
				return;
			}
		}
	}
	
	@Override
	protected int getExtState(final String contentType) {
		if (contentType == Rweave.CHUNK_CONTROL_CONTENT_TYPE) {
			return S_CHUNKCONTROL;
		}
		return S_DEFAULT;
	}
	
	
	public String[] getContentTypes() {
		return Rweave.R_CHUNK_PARTITIONS;
	}
	
	public boolean isInCat() {
		return fIsInChunk;
	}
	
}
