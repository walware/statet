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

package de.walware.statet.r.core.rsource;

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_OK;

import de.walware.ecommons.text.SourceParseInput;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * Lexer creating RSourceToken instances
 */
public class RSourceTokenLexer extends RLexer {
	
	
	private RSourceToken fNextToken;
	private CharSequence fSource;
	
	
	public RSourceTokenLexer() {
		super(null);
	}
	
	
	public void reset(final SourceParseInput input, final CharSequence source) {
		super.reset(input);
		fSource = source;
		setFull();
	}
	
	
	public RSourceToken nextToken() {
		RSourceToken t;
		while (fNextToken == null) {
			searchNext();
		}
		t = fNextToken;
		fNextToken = null;
		return t;
	}
	
	
	@Override
	protected void createFix(final RTerminal type) {
		fNextToken = RSourceToken.createFix(type, fNextIndex, STATUS_OK);
	}
	
	@Override
	protected void createSpecialToken(final int status) {
		fNextToken = RSourceToken.create(RTerminal.SPECIAL, fNextIndex, fInput.substring(1, fNextNum), status);
	}
	
	@Override
	protected void createSymbolToken() {
		fNextToken = RSourceToken.create(RTerminal.SYMBOL, fNextIndex, fInput.substring(1, fNextNum), STATUS_OK);
	}
	
	@Override
	protected void createQuotedSymbolToken(final RTerminal type, final int status) {
		fNextToken = RSourceToken.create(type, fNextIndex, fInput.substring(1, fNextNum), status);
	}
	
	@Override
	protected void createStringToken(final RTerminal type, final int status) {
		fNextToken = RSourceToken.create(type, fNextIndex, fInput.substring(1, fNextNum), status);
	}
	
	@Override
	protected void createNumberToken(final RTerminal type, final int status) {
		fNextToken = RSourceToken.create(type, fNextIndex, fInput.substring(1, fNextNum), status);
	}
	
	@Override
	protected void createWhitespaceToken() {
		fNextToken = RSourceToken.create(RTerminal.BLANK, fNextIndex, fNextNum, fSource, STATUS_OK);
	}
	
	@Override
	protected void createCommentToken() {
		fNextToken = RSourceToken.create(RTerminal.COMMENT, fNextIndex, fNextNum, fSource, STATUS_OK);
	}
	
	@Override
	protected void createLinebreakToken(final String text) {
		fNextToken = RSourceToken.create(RTerminal.LINEBREAK, fNextIndex, text, STATUS_OK);
	}
	
	@Override
	protected void createUnknownToken(final String text) {
		fNextToken = RSourceToken.create(RTerminal.UNKNOWN, fNextIndex, text, STATUS_OK);
	}
	
}
