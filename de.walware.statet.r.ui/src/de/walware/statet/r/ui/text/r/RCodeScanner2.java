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

package de.walware.statet.r.ui.text.r;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

import de.walware.eclipsecommons.ltk.text.BufferedDocumentParseInput;
import de.walware.eclipsecommons.ltk.text.SourceParseInput;
import de.walware.eclipsecommons.ui.util.ColorManager;

import de.walware.statet.base.ui.util.ISettingsChangedHandler;
import de.walware.statet.ext.ui.text.TextStyleManager;
import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.RLexer;
import de.walware.statet.r.internal.ui.RIdentifierGroups;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.RUIPreferenceConstants;


/**
 * Text token scanner for R code.
 * Mainly for R default partitions, but also works for other partitions (without special styles).
 * 
 * Version 2 uses RLexer instead of rules to parse the sources.
 */
public class RCodeScanner2 extends BufferedDocumentParseInput implements ITokenScanner, ISettingsChangedHandler {
	
	protected static final class ScannerToken {
		public RTerminal type;
		public int offset;
		public int length;
		public String text;
	}
	
	protected static class RTokenScannerLexer extends RLexer {
		
		protected final ScannerToken fNextToken;
		
		
		public RTokenScannerLexer(final SourceParseInput input) {
			super(input);
			fNextToken = new ScannerToken();
		}
		
		@Override
		protected final void reset(final SourceParseInput input) {
			super.reset(input);
		}
		
		public final ScannerToken nextToken() {
			do {
				searchNext();
			} while (fNextToken.type == null);
			return fNextToken;
		}
		
		
		@Override
		protected final void createFix(final RTerminal type) {
			fNextToken.type = type;
			fNextToken.offset = fNextIndex;
			fNextToken.length = fNextNum;
		}
		
		@Override
		protected final void createSpecialToken(final int status) {
			fNextToken.type = RTerminal.SPECIAL;
			fNextToken.offset = fNextIndex;
			fNextToken.length = fNextNum;
		}
		
		@Override
		protected final void createSymbolToken() {
			fNextToken.type = RTerminal.SYMBOL;
			fNextToken.offset = fNextIndex;
			fNextToken.length = fNextNum;
			if (fNextNum < 50) {
				fNextToken.text = fInput.substring(1, fNextNum);
			}
		}
		
		@Override
		protected final void createQuotedSymbolToken(final RTerminal type, final int status) {
			fNextToken.type = type;
			fNextToken.offset = fNextIndex;
			fNextToken.length = fNextNum;
		}
		
		@Override
		protected final void createStringToken(final RTerminal type, final int status) {
			fNextToken.type = type;
			fNextToken.offset = fNextIndex;
			fNextToken.length = fNextNum;
		}
		
		@Override
		protected final void createNumberToken(final RTerminal type, final int status) {
			fNextToken.type = type;
			fNextToken.offset = fNextIndex;
			fNextToken.length = fNextNum;
		}
		
		@Override
		protected final void createWhitespaceToken() {
			fNextToken.type = null;
		}
		
		@Override
		protected void createLinebreakToken(final String text) {
			fNextToken.type = null;
		}
		
		@Override
		protected final void createCommentToken() {
			fNextToken.type = RTerminal.COMMENT;
			fNextToken.offset = fNextIndex;
			fNextToken.length = fNextNum;
		}
		
		@Override
		protected final void createUnknownToken(final String text) {
			fNextToken.type = RTerminal.UNKNOWN;
			fNextToken.offset = fNextIndex;
			fNextToken.length = fNextNum;
		}
		
	}
	
	
	private final RTokenScannerLexer fLexer;
	private ScannerToken fScannerToken;
	
	private final EnumMap<RTerminal, IToken> fTokens;
	private final IToken fDefaultToken;
	private final Map<String, IToken> fSpecialSymbols;
	private final TextStyleManager fTextStyles;
	
	private int fCurrentOffset;
	private int fCurrentLength;
	
	
	public RCodeScanner2(final ColorManager colorManager, final IPreferenceStore preferenceStore) {
		fTokens = new EnumMap<RTerminal, IToken>(RTerminal.class);
		fSpecialSymbols = new HashMap<String, IToken>();
		fLexer = createLexer();
		fTextStyles = new TextStyleManager(colorManager, preferenceStore, RUIPreferenceConstants.R.TS_GROUP_ID);
		
		fTokens.put(RTerminal.EOF, Token.EOF);
		registerTokens();
		fDefaultToken = fTokens.get(RTerminal.SYMBOL);
//		checkTokenMap();
	}
	
	protected RTokenScannerLexer createLexer() {
		return new RTokenScannerLexer(this);
	}
	
	protected void checkTokenMap() {
		final RTerminal[] all = RTerminal.values();
		for (final RTerminal t : all) {
			if (fTokens.get(t) == null) {
				System.out.println("Style Missing for: " + t.name()); //$NON-NLS-1$
			}
		}
	}
	
	
	@Override
	public void setRange(final IDocument document, final int offset, final int length) {
		super.setRange(document, offset, length);
		fScannerToken = null;
		fCurrentOffset = offset;
		fCurrentLength = 0;
		fLexer.reset(this);
	}
	
	protected void registerTerminal(final RTerminal type, final IToken token) {
		fTokens.put(type, token);
	}
	protected void registerTerminals(final RTerminal[] types, final IToken token) {
		for (int i = 0; i < types.length; i++) {
			fTokens.put(types[i], token);
		}
	}
	
	protected void registerSpecialSymbol(final String symbol, final IToken token) {
		fSpecialSymbols.put(symbol, token);
	}
	protected void registerSpecialSymbols(final String[] symbols, final IToken token) {
		for (int i = 0; i < symbols.length; i++) {
			fSpecialSymbols.put(symbols[i], token);
		}
	}
	protected void resetSpecialSymbols() {
		fSpecialSymbols.clear();
	}
	
	
	public IToken nextToken() {
		fCurrentOffset += fCurrentLength;
		if (fScannerToken == null) {
			fScannerToken = fLexer.nextToken();
		}
		fCurrentLength = fScannerToken.offset-fCurrentOffset;
		if (fCurrentLength != 0) {
			return fDefaultToken;
		}
		fCurrentLength = fScannerToken.length;
		return getTokenFromScannerToken();
	}
	
	protected IToken getTokenFromScannerToken() {
		IToken token;
		if (fScannerToken.type == RTerminal.SYMBOL) {
			if (fScannerToken.text != null) {
				token = fSpecialSymbols.get(fScannerToken.text);
				fScannerToken.text = null;
				if (token != null) {
					fScannerToken = null;
					return token;
				}
			}
			fScannerToken = null;
			return fDefaultToken;
		}
		token = fTokens.get(fScannerToken.type);
		fScannerToken = null;
		return token;
	}
	
	public int getTokenOffset() {
		return fCurrentOffset;
	}
	
	public int getTokenLength() {
		return fCurrentLength;
	}
	
	
	protected IToken getToken(final String key) {
		return fTextStyles.getToken(key);
	}
	
	public boolean handleSettingsChanged(final Set<String> groupIds, final Object options) {
		return fTextStyles.handleSettingsChanged(groupIds, options);
	}
	
	
	//-- Concrete associations
	
	protected void registerTokens() {
		registerTerminals(IRTextTokens.FLOWCONTROL, getToken(IRTextTokens.FLOWCONTROL_KEY));
		registerTerminals(IRTextTokens.GROUPING, getToken(IRTextTokens.GROUPING_KEY));
		registerTerminals(IRTextTokens.SEPARATOR, getToken(IRTextTokens.SEPARATOR_KEY));
		registerTerminals(IRTextTokens.ASSIGN, getToken(IRTextTokens.ASSIGN_KEY));
		registerTerminals(IRTextTokens.ASSIGN_SUB_EQUAL, getToken(IRTextTokens.ASSIGN_SUB_EQUAL_KEY));
		registerTerminals(IRTextTokens.OP, getToken(IRTextTokens.OP_KEY));
		registerTerminals(IRTextTokens.OP_SUB_LOGICAL, getToken(IRTextTokens.OP_SUB_LOGICAL_KEY));
		registerTerminals(IRTextTokens.OP_SUB_RELATIONAL, getToken(IRTextTokens.OP_SUB_RELATIONAL_KEY));
		registerTerminals(IRTextTokens.SUBACCESS, getToken(IRTextTokens.SUBACCESS_KEY));
		registerTerminals(IRTextTokens.NSGET, getToken(IRTextTokens.SUBACCESS_KEY));
		
		registerTerminals(IRTextTokens.SPECIALCONST, getToken(IRTextTokens.SPECIALCONST_KEY));
		registerTerminals(IRTextTokens.LOGICALCONST, getToken(IRTextTokens.LOGICALCONST_KEY));
		registerTerminals(IRTextTokens.SYMBOL, getToken(IRTextTokens.SYMBOL_KEY));
		updateSymbols();
		registerTerminals(IRTextTokens.NUM, getToken(IRTextTokens.NUM_KEY));
		registerTerminals(IRTextTokens.NUM_SUB_INT, getToken(IRTextTokens.NUM_SUB_INT_KEY));
		registerTerminals(IRTextTokens.NUM_SUB_CPLX, getToken(IRTextTokens.NUM_SUB_CPLX_KEY));
		registerTerminals(IRTextTokens.UNDEFINED, getToken(IRTextTokens.UNDEFINED_KEY));
		
		// usually not in default partition
		registerTerminals(IRTextTokens.STRING, getToken(IRTextTokens.STRING_KEY));
		registerTerminal(RTerminal.SYMBOL_G, getToken(IRTextTokens.STRING_KEY));
		registerTerminal(RTerminal.SPECIAL, getToken(IRTextTokens.OP_KEY));
		registerTerminals(IRTextTokens.COMMENT, getToken(IRTextTokens.COMMENT_KEY));
	}
	
	private void updateSymbols() {
		final RIdentifierGroups groups = RUIPlugin.getDefault().getRIdentifierGroups();
		groups.getReadLock().lock();
		try {
			registerSpecialSymbols(groups.getAssignmentIdentifiers(),
					getToken(IRTextTokens.SYMBOL_SUB_ASSIGN_KEY));
			registerSpecialSymbols(groups.getLogicalIdentifiers(),
					getToken(IRTextTokens.SYMBOL_SUB_LOGICAL_KEY));
			registerSpecialSymbols(groups.getFlowcontrolIdentifiers(),
					getToken(IRTextTokens.SYMBOL_SUB_FLOWCONTROL_KEY));
			registerSpecialSymbols(groups.getCustom1Identifiers(),
					getToken(IRTextTokens.SYMBOL_SUB_CUSTOM1_KEY));
			registerSpecialSymbols(groups.getCustom2Identifiers(),
					getToken(IRTextTokens.SYMBOL_SUB_CUSTOM2_KEY));
		}
		finally {
			groups.getReadLock().unlock();
		}
	}
	
}
