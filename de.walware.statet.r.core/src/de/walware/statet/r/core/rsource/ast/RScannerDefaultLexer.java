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

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_TOKEN_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_MASK_12;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_OK;

import de.walware.ecommons.ltk.text.IStringCache;
import de.walware.ecommons.ltk.text.SourceParseInput;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * 
 */
final class RScannerDefaultLexer extends RScannerLexer {
	
	
	private final IStringCache fStringCache;
	
	
	public RScannerDefaultLexer(final SourceParseInput input, final IStringCache cache) {
		super(input);
		fStringCache = cache;
	}
	
	
	@Override
	protected void reset(final SourceParseInput input) {
		super.reset(input);
	}
	
	@Override
	protected void createNumberToken(final RTerminal type, final int status) {
		fNextToken.type = type;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = fInput.substring(1, fNextNum);
		fNextToken.status = status;
	}
	
	@Override
	protected void createSymbolToken() {
		fNextToken.type = RTerminal.SYMBOL;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = fInput.substring(1, fNextNum);
		if (fStringCache != null && fNextToken.text.length() <= 20) {
			fNextToken.text = fStringCache.get(fNextToken.text);
		}
		fNextToken.status = STATUS_OK;
	}
	
	@Override
	protected void createQuotedSymbolToken(final RTerminal type, final int status) {
		fNextToken.type = type;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = ((status & STATUS_MASK_12) != STATUS2_SYNTAX_TOKEN_NOT_CLOSED) ?
				fInput.substring(2, fNextNum-2) : fInput.substring(2, fNextNum-1);
		if (fStringCache != null && fNextToken.text.length() <= 20) {
			fNextToken.text = fStringCache.get(fNextToken.text);
		}
		fNextToken.status = status;
	}
	
	@Override
	protected void createStringToken(final RTerminal type, final int status) {
		fNextToken.type = type;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = ((status & STATUS_MASK_12) != STATUS2_SYNTAX_TOKEN_NOT_CLOSED) ?
				fInput.substring(2, fNextNum-2) : fInput.substring(2, fNextNum-1);
		fNextToken.status = status;
	}
	
	@Override
	protected void createSpecialToken(final int status) {
		fNextToken.type = RTerminal.SPECIAL;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = ((status & STATUS_MASK_12) != STATUS2_SYNTAX_TOKEN_NOT_CLOSED) ?
				fInput.substring(2, fNextNum-2) : fInput.substring(2, fNextNum-1);
		if (fStringCache != null && fNextToken.text.length() <= 3) {
			fNextToken.text = fStringCache.get(fNextToken.text);
		}
		fNextToken.status = status;
	}
	
}
