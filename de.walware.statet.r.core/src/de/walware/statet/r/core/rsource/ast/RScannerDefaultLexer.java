/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource.ast;

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_TOKEN_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_MASK_12;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_OK;

import de.walware.ecommons.text.IStringCache;
import de.walware.ecommons.text.SourceParseInput;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * Lexer for RScanner
 */
class RScannerDefaultLexer extends RScannerLexer {
	
	
	private final IStringCache fStringCache;
	
	
	public RScannerDefaultLexer(final SourceParseInput input, final IStringCache cache) {
		super(input);
		fStringCache = cache;
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
		fFoundText = fInput.substring(1, fFoundNum);
		if (fStringCache != null && fFoundText.length() <= 20) {
			fFoundText = fStringCache.get(fFoundText);
		}
		fFoundStatus = STATUS_OK;
	}
	
	@Override
	protected void createQuotedSymbolToken(final RTerminal type, final int status) {
		fFoundType = type;
		fFoundText = ((status & STATUS_MASK_12) != STATUS2_SYNTAX_TOKEN_NOT_CLOSED) ?
				fInput.substring(2, fFoundNum-2) : fInput.substring(2, fFoundNum-1);
		if (fStringCache != null && fFoundText.length() <= 20) {
			fFoundText = fStringCache.get(fFoundText);
		}
		fFoundStatus = status;
	}
	
	@Override
	protected void createStringToken(final RTerminal type, final int status) {
		fFoundType = type;
		fFoundText = ((status & STATUS_MASK_12) != STATUS2_SYNTAX_TOKEN_NOT_CLOSED) ?
				fInput.substring(2, fFoundNum-2) : fInput.substring(2, fFoundNum-1);
		fFoundStatus = status;
	}
	
	@Override
	protected void createSpecialToken(final int status) {
		fFoundType = RTerminal.SPECIAL;
		fFoundText = ((status & STATUS_MASK_12) != STATUS2_SYNTAX_TOKEN_NOT_CLOSED) ?
				fInput.substring(2, fFoundNum-2) : fInput.substring(2, fFoundNum-1);
		if (fStringCache != null && fFoundText.length() <= 3) {
			fFoundText = fStringCache.get(fFoundText);
		}
		fFoundStatus = status;
	}
	
}
