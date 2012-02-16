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

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_OK;

import java.util.EnumMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IToken;

import de.walware.ecommons.text.SourceParseInput;
import de.walware.ecommons.ui.ColorManager;

import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.ui.text.r.IRTextTokens;
import de.walware.statet.r.ui.text.r.RCodeScanner2;


/**
 * Scanner to hightlight chunk control partitions.
 */
public class RChunkControlCodeScanner extends RCodeScanner2 {
	
	
	private static class RChunkControlLexer extends RTokenScannerLexer {
		
		private RChunkControlCodeScanner fDocumentInput;
		
		public RChunkControlLexer() {
			super();
		}
		
		@Override
		public void reset(final SourceParseInput input) {
			fDocumentInput = (RChunkControlCodeScanner) input;
			super.reset(input);
		}
		
		@Override
		protected void searchNext1(final int c1) {
			switch (c1) {
			case '<':
				if (fInput.get(2) == '<' && isNewLine()) {
					fFoundNum = 2;
					createChunk("<<"); //$NON-NLS-1$
					return;
				}
				break;
			case '>':
				if (fInput.get(2) == '>') {
					if (fInput.get(3) == '=') {
						fFoundNum = 3;
						createChunk(">>="); //$NON-NLS-1$
						return;
					}
					fFoundNum = 2;
					createChunk(">>"); //$NON-NLS-1$
					return;
				}
				break;
			case '@':
				if (isNewLine()) {
					fFoundNum = 1;
					createChunk("@"); //$NON-NLS-1$
					return;
				}
			}
			super.searchNext1(c1);
		}
		
		private boolean isNewLine() {
			final int offset = fFoundOffset-1;
			if (offset < 0) {
				return true;
			}
			final int c = fDocumentInput.getDocumentChar(offset);
			return (c == '\n' || c == '\r');
		}
		
		protected void createChunk(final String text) {
			fFoundType = RTerminal.OTHER;
			fFoundText = text;
			fFoundStatus = STATUS_OK;
		}
		
	}
	
	
	public RChunkControlCodeScanner(final ColorManager colorManager, final IPreferenceStore preferenceStore) {
		super(new RChunkControlLexer(), createDefaultTextStyleManager(colorManager, preferenceStore));
	}
	
	
	@Override
	protected void registerTokens(final EnumMap<RTerminal, IToken> map) {
		super.registerTokens(map);
		map.put(RTerminal.OTHER, getToken(IRTextTokens.UNDEFINED_KEY));
	}
	
}
