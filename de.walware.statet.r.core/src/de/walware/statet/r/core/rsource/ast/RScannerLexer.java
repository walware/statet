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

package de.walware.statet.r.core.rsource.ast;

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_OK;

import de.walware.ecommons.text.SourceParseInput;

import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.RLexer;


/**
 * Lexer for RScanner.
 */
class RScannerLexer extends RLexer {
	
	
	final class ScannerToken {
		RTerminal type;
		int offset;
		int length;
		String text;
		int status;
	}
	
	
	protected final ScannerToken fNextToken;
	
	
	public RScannerLexer(final SourceParseInput input) {
		super(input);
		fNextToken = new ScannerToken();
	}
	
	public ScannerToken getToken() {
		return fNextToken;
	}
	
	public void nextToken() {
		do {
			searchNext();
		} while (fNextToken.type == null);
	}
	
	
	@Override
	protected void createFix(final RTerminal type) {
		fNextToken.type = type;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = null;
		fNextToken.status = STATUS_OK;
	}
	
	@Override
	protected void createSpecialToken(final int status) {
		fNextToken.type = RTerminal.SPECIAL;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = null;
		fNextToken.status = status;
	}
	
	@Override
	protected void createSymbolToken() {
		fNextToken.type = RTerminal.SYMBOL;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = null;
		fNextToken.status = STATUS_OK;
	}
	
	@Override
	protected void createQuotedSymbolToken(final RTerminal type, final int status) {
		fNextToken.type = type;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = null;
		fNextToken.status = status;
	}
	
	@Override
	protected void createStringToken(final RTerminal type, final int status) {
		fNextToken.type = type;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = null;
		fNextToken.status = status;
	}
	
	@Override
	protected void createNumberToken(final RTerminal type, final int status) {
		fNextToken.type = type;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = null;
		fNextToken.status = status;
	}
	
	@Override
	protected void createWhitespaceToken() {
		fNextToken.type = null;
	}
	
	@Override
	protected void createCommentToken() {
		fNextToken.type = RTerminal.COMMENT;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = null;
		fNextToken.status = STATUS_OK;
	}
	
	@Override
	protected void createLinebreakToken(final String text) {
		fNextToken.type = RTerminal.LINEBREAK;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = text;
		fNextToken.status = STATUS_OK;
	}
	
	@Override
	protected void createUnknownToken(final String text) {
		fNextToken.type = RTerminal.UNKNOWN;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = text;
		fNextToken.status = STATUS_OK;
	}
	
}
