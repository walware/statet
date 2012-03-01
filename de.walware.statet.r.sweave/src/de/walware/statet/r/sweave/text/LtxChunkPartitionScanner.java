/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.sweave.text;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import de.walware.ecommons.collections.IntMap;

import de.walware.docmlet.tex.ui.text.LtxFastPartitionScanner;

import de.walware.statet.r.core.rsource.IRDocumentPartitions;


/**
 * Paritition scanner for LaTeX chunks (stops if find '&lt;&lt;' at column 0).
 */
public class LtxChunkPartitionScanner extends LtxFastPartitionScanner
		implements ICatPartitionTokenScanner {
	
	
	private static final int S_SEXPR = 12;
	
	private static final char[] SEQ_Sexpr = "Sexpr".toCharArray();
	
	private final static IToken T_RCODE = new Token(IRDocumentPartitions.R_DEFAULT_EXPL);
	
	private int fSavedLineState;
	
	private boolean fIsInChunk;
	
	
	public LtxChunkPartitionScanner() {
		super(Rweave.LTX_R_PARTITIONING);
	}
	
	public LtxChunkPartitionScanner(final boolean templateMode) {
		super(Rweave.LTX_R_PARTITIONING, templateMode);
	}
	
	
	@Override
	protected void initTokens(final IntMap<IToken> states) {
		super.initTokens(states);
		states.put(S_SEXPR, T_RCODE);
	}
	
	
	@Override
	public void setParent(final MultiCatPartitionScanner parent) {
	}
	
	@Override
	public String[] getContentTypes() {
		return Rweave.LTX_PARTITION_TYPES;
	}
	
	@Override
	public void setPartialRange(final IDocument document, final int offset, final int length, final String contentType, final int partitionOffset) {
		fIsInChunk = true;
		super.setPartialRange(document, offset, length, contentType, partitionOffset);
	}
	
	@Override
	protected void searchDefault() {
		if (fLast == LAST_NEWLINE) {
			if (readCharsTemp('<', '<')) {
				fIsInChunk = false;
				forceReturn(0);
				return;
			}
		}
		super.searchDefault();
	}
	
	@Override
	protected void searchMathSpecial() {
		if (fLast == LAST_NEWLINE) {
			if (readCharsTemp('<', '<')) {
				fIsInChunk = false;
				forceReturn(0);
				return;
			}
		}
		super.searchMathSpecial();
	}
	
	@Override
	protected void searchMathEnv() {
		if (fLast == LAST_NEWLINE) {
			if (readCharsTemp('<', '<')) {
				fIsInChunk = false;
				forceReturn(0);
				return;
			}
		}
		super.searchMathEnv();
	}
	
	@Override
	public boolean isInCat() {
		return fIsInChunk;
	}
	
	
	@Override
	protected int getExtState(final String contentType) {
		return S_SEXPR;
	}
	
	@Override
	protected boolean searchExtCommand(final int c) {
		if (c == 'S' && readSeq2Consuming(SEQ_Sexpr)) {
			readWhitespaceConsuming();
			if (readChar('{')) {
				fSavedLineState = getState();
				newState(S_SEXPR);
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void searchExtState(final int state) {
		if (state == S_SEXPR) {
			searchVerbatimLine('}', fSavedLineState);
		}
		else {
			super.searchExtState(state);
		}
	}
	
}
