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

package de.walware.statet.r.core.rsource.ast;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.eclipsecommons.ltk.text.SourceParseInput;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 *
 */
public final class RScannerDefaultLexer extends RScannerLexer {
	
	
	public RScannerDefaultLexer(final SourceParseInput input) {
		super(input);
	}

	@Override
	protected void createSymbolToken() {
		fNextToken.type = RTerminal.SYMBOL;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = fInput.substring(1, fNextNum);
		fNextToken.status = STATUS_OK;
	}
	
	@Override
	protected void createStringToken(final RTerminal type, final IStatus status) {
		fNextToken.type = type;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = fInput.substring(1, fNextNum);
		fNextToken.status = status;
	}
	
	@Override
	protected void createSpecialToken(final IStatus status) {
		fNextToken.type = RTerminal.SPECIAL;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = (status == Status.OK_STATUS) ?
				fInput.substring(2, fNextNum-2) : fInput.substring(2, fNextNum-1);
		fNextToken.status = status;
	}
	
}
