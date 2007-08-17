/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource;

import org.eclipse.core.runtime.IStatus;

import de.walware.eclipsecommons.ltk.text.SourceParseInput;

import de.walware.statet.r.core.rlang.RTerminal;



public class RSourceTokenLexer extends RLexer {
	
	
	private RSourceToken fNextToken;
	private CharSequence fSource;

	
	public RSourceTokenLexer(SourceParseInput input, CharSequence source) {
		super(input);
		fSource = source;
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
	protected void createFix(RTerminal type) {
		fNextToken = RSourceToken.createFix(type, fNextIndex, STATUS_OK);
	}
	
	@Override
	protected void createSpecialToken(IStatus status) {
		fNextToken = RSourceToken.create(RTerminal.SPECIAL, fNextIndex, fInput.substring(1, fNextNum), status);
	}
	
	@Override
	protected void createSymbolToken() {
		fNextToken = RSourceToken.create(RTerminal.SYMBOL, fNextIndex, fInput.substring(1, fNextNum), STATUS_OK);
	}
	
	@Override
	protected void createStringToken(RTerminal type, IStatus status) {
		fNextToken = RSourceToken.create(type, fNextIndex, fInput.substring(1, fNextNum), status);
	}

	@Override
	protected void createNumberToken(RTerminal type, IStatus status) {
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
	protected void createLinebreakToken(String text) {
		fNextToken = RSourceToken.create(RTerminal.LINEBREAK, fNextIndex, text, STATUS_OK);
	}
	
	@Override
	protected void createUnknownToken(String text) {
		fNextToken = RSourceToken.create(RTerminal.UNKNOWN, fNextIndex, text, STATUS_OK);
	}
		
}
