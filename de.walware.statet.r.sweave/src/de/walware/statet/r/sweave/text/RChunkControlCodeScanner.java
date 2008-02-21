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

import org.eclipse.jface.preference.IPreferenceStore;

import de.walware.eclipsecommons.ui.util.ColorManager;

import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.ui.text.r.IRTextTokens;
import de.walware.statet.r.ui.text.r.RCodeScanner2;


/**
 * Scanner to hightlight chunk control partitions.
 */
public class RChunkControlCodeScanner extends RCodeScanner2 {
	
	
	private static class RChunkControlLexer extends RTokenScannerLexer {
		
		private RChunkControlCodeScanner fDocumentInput;
		
		public RChunkControlLexer(final RChunkControlCodeScanner input) {
			super(input);
			fDocumentInput = input;
		}
		
		@Override
		protected void searchNext1(final int c1) {
			switch (c1) {
			case '<':
				if (fInput.get(2) == '<' && isNewLine()) {
					fNextNum = 2;
					createChunk("<<"); //$NON-NLS-1$
					return;
				}
				break;
			case '>':
				if (fInput.get(2) == '>') {
					if (fInput.get(3) == '=') {
						fNextNum = 3;
						createChunk(">>="); //$NON-NLS-1$
						return;
					}
					fNextNum = 2;
					createChunk(">>"); //$NON-NLS-1$
					return;
				}
				break;
			case '@':
				if (isNewLine()) {
					fNextNum = 1;
					createChunk("@"); //$NON-NLS-1$
					return;
				}
			}
			super.searchNext1(c1);
		}
			
		private boolean isNewLine() {
			final int offset = fNextIndex-1;
			if (offset < 0) {
				return true;
			}
			final int c = fDocumentInput.getDocumentChar(offset);
			return (c == '\n' || c == '\r');
		}
		
		protected void createChunk(final String text) {
			fNextToken.type = RTerminal.OTHER;
			fNextToken.offset = fNextIndex;
			fNextToken.length = fNextNum;
		}
		
	}
	
	
	public RChunkControlCodeScanner(final ColorManager colorManager, final IPreferenceStore preferenceStore) {
		super(colorManager, preferenceStore);
	}
	
	@Override
	protected RTokenScannerLexer createLexer() {
		return new RChunkControlLexer(this);
	}
	
	@Override
	protected void registerTokens() {
		super.registerTokens();
		registerTerminal(RTerminal.OTHER, getToken(IRTextTokens.UNDEFINED_KEY));
	}
	
}
