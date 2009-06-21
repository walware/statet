/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource.ast;

import de.walware.ecommons.text.IStringCache;
import de.walware.ecommons.text.SourceParseInput;


public class RRoxygenScannerDefaultLexer extends RScannerDefaultLexer {
	
	
	public RRoxygenScannerDefaultLexer(final SourceParseInput input, final IStringCache cache) {
		super(input, cache);
	}
	
	
	@Override
	protected void searchNext1(final int c1) {
		final int c2;
		switch (c1) {
		case '\\':
			c2 = fInput.get(2);
			if (Character.isLetterOrDigit(c2)) {
				fNextNum++;
				scanTexIdentifier();
				fNextToken.type = null;
				return;
			}
			else {
				fNextIndex++;
				super.searchNext1(c2);
				return;
			}
		case '@':
			c2 = fInput.get(2);
			fNextIndex++;
			super.searchNext1(c2);
			return;
		}
		super.searchNext1(c1);
	}
	
	private final void scanTexIdentifier() {
		scanIdentifier();
	}
	
}
