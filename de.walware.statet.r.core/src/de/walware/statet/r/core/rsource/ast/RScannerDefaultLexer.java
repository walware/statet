/*=============================================================================#
 # Copyright (c) 2007-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.rsource.ast;

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_TOKEN_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_MASK_12;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_OK;

import de.walware.ecommons.text.IStringCache;
import de.walware.ecommons.text.NoStringCache;
import de.walware.ecommons.text.SourceParseInput;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * Lexer for RScanner
 */
class RScannerDefaultLexer extends RScannerLexer {
	
	
	private final IStringCache fStringCache;
	
	
	public RScannerDefaultLexer(final SourceParseInput input, final IStringCache cache) {
		super(input);
		fStringCache = (cache != null) ? cache : NoStringCache.INSTANCE;
	}
	
	
	@Override
	protected void createNumberToken(final RTerminal type, final int status) {
		fFoundType = type;
		fFoundText = fInput.substring(1, fFoundNum);
		fFoundStatus = status;
	}
	
	@Override
	protected void createSymbolToken() {
		fFoundType = RTerminal.SYMBOL;
		fFoundText = fInput.substring(1, fFoundNum, fStringCache);
		fFoundStatus = STATUS_OK;
	}
	
	@Override
	protected void createQuotedSymbolToken(final RTerminal type, final int status) {
		fFoundType = type;
		final int sLength = ((status & STATUS_MASK_12) != STATUS2_SYNTAX_TOKEN_NOT_CLOSED) ?
				(fFoundNum - 2) : (fFoundNum - 1);
		fFoundText = fInput.substring(2, sLength, fStringCache);
		fFoundStatus = status;
	}
	
	@Override
	protected void createStringToken(final RTerminal type, final int status) {
		fFoundType = type;
		final int sLength = ((status & STATUS_MASK_12) != STATUS2_SYNTAX_TOKEN_NOT_CLOSED) ?
				(fFoundNum - 2) : (fFoundNum - 1);
		fFoundText = fInput.substring(2, sLength);
		fFoundStatus = status;
	}
	
	@Override
	protected void createSpecialToken(final int status) {
		fFoundType = RTerminal.SPECIAL;
		final int sLength = ((status & STATUS_MASK_12) != STATUS2_SYNTAX_TOKEN_NOT_CLOSED) ?
				(fFoundNum - 2) : (fFoundNum - 1);
				;
		fFoundText = (sLength <= 3) ?
				fInput.substring(2, sLength, fStringCache) : fInput.substring(2, sLength);
		fFoundStatus = status;
	}
	
}
