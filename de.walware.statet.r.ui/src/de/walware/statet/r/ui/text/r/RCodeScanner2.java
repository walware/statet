/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.text.r;

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_OK;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

import de.walware.ecommons.text.BufferedDocumentParseInput;
import de.walware.ecommons.text.ui.presentation.ITextPresentationConstants;
import de.walware.ecommons.text.ui.settings.TextStyleManager;
import de.walware.ecommons.ui.ColorManager;
import de.walware.ecommons.ui.ISettingsChangedHandler;

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
	
	
	protected static void putAll(final Map<String, IToken> map, final String[] symbols, final IToken token) {
		for (int i = 0; i < symbols.length; i++) {
			map.put(symbols[i], token);
		}
	}
	
	protected static void putAll(final Map<RTerminal, IToken> map, final RTerminal[] types, final IToken token) {
		for (int i = 0; i < types.length; i++) {
			map.put(types[i], token);
		}
	}
	
	protected static TextStyleManager createDefaultTextStyleManager(final ColorManager colorManager, final IPreferenceStore preferenceStore) {
		return new TextStyleManager(colorManager, preferenceStore, RUIPreferenceConstants.R.TS_GROUP_ID);
	}
	
	
	protected static class RTokenScannerLexer extends RLexer {
		
		
		public RTokenScannerLexer() {
			super();
		}
		
		
		@Override
		protected final void createSymbolToken() {
			fFoundType = RTerminal.SYMBOL;
			fFoundText = (fFoundNum < 50) ? fInput.substring(1, fFoundNum) : null;
			fFoundStatus = STATUS_OK;
		}
		
		@Override
		protected final void createWhitespaceToken() {
			fFoundType = null;
		}
		
		@Override
		protected void createLinebreakToken(final String text) {
			fFoundType = null;
		}
		
	}
	
	
	private final RTokenScannerLexer fLexer;
	private RTerminal fLexerToken;
	
	private final EnumMap<RTerminal, IToken> fTokens;
	private final IToken fDefaultToken;
	private final Map<String, IToken> fSpecialSymbols;
	private final TextStyleManager fTextStyles;
	
	private int fCurrentOffset;
	private int fCurrentLength;
	
	
	public RCodeScanner2(final ColorManager colorManager, final IPreferenceStore preferenceStore) {
		this(new RTokenScannerLexer(), createDefaultTextStyleManager(colorManager, preferenceStore));
	}
	
	protected RCodeScanner2(final RTokenScannerLexer lexer, final TextStyleManager textStyles) {
		fLexer = lexer;
		fTextStyles = textStyles;
		
		fDefaultToken = getToken(IRTextTokens.SYMBOL_KEY);
		fTokens = new EnumMap<RTerminal, IToken>(RTerminal.class);
		registerTokens(fTokens);
//		checkTokenMap();
		fSpecialSymbols = new HashMap<String, IToken>();
		updateSymbols(fSpecialSymbols);
	}
	
	protected void checkTokenMap() {
		final RTerminal[] all = RTerminal.values();
		for (final RTerminal t : all) {
			if (fTokens.get(t) == null) {
				System.out.println("Style Missing for: " + t.name()); //$NON-NLS-1$
			}
		}
	}
	
	
	protected RTokenScannerLexer getLexer() {
		return fLexer;
	}
	
	protected TextStyleManager getTextStyleManager() {
		return fTextStyles;
	}
	
	@Override
	public void setRange(final IDocument document, final int offset, final int length) {
		super.setRange(document, offset, length);
		fCurrentOffset = offset;
		fCurrentLength = 0;
		fLexer.reset(this);
	}
	
	protected void resetSpecialSymbols() {
		fSpecialSymbols.clear();
		updateSymbols(fSpecialSymbols);
	}
	
	
	public IToken nextToken() {
		fCurrentOffset += fCurrentLength;
		if (fLexerToken == null) {
			fLexerToken = fLexer.next();
		}
		fCurrentLength = fLexer.getOffset()-fCurrentOffset;
		if (fCurrentLength != 0) {
			return fDefaultToken;
		}
		fCurrentLength = fLexer.getLength();
		return getTokenFromScannerToken();
	}
	
	protected IToken getTokenFromScannerToken() {
		IToken token;
		if (fLexerToken == RTerminal.SYMBOL) {
			final String text = fLexer.getText();
			if (text != null) {
				token = fSpecialSymbols.get(text);
				if (token != null) {
					fLexerToken = null;
					return token;
				}
			}
			fLexerToken = null;
			return fDefaultToken;
		}
		token = fTokens.get(fLexerToken);
		fLexerToken = null;
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
	
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
		fTextStyles.handleSettingsChanged(groupIds, options);
		if (groupIds.contains(RIdentifierGroups.GROUP_ID)) {
			resetSpecialSymbols();
			options.put(ITextPresentationConstants.SETTINGSCHANGE_AFFECTSPRESENTATION_KEY, Boolean.TRUE);
		}
	}
	
	
	//-- Concrete associations
	
	protected void registerTokens(final EnumMap<RTerminal,IToken> map) {
		map.put(RTerminal.EOF, Token.EOF);
		
		putAll(map, IRTextTokens.FLOWCONTROL, getToken(IRTextTokens.FLOWCONTROL_KEY));
		putAll(map, IRTextTokens.GROUPING, getToken(IRTextTokens.GROUPING_KEY));
		putAll(map, IRTextTokens.SEPARATOR, getToken(IRTextTokens.SEPARATOR_KEY));
		putAll(map, IRTextTokens.ASSIGN, getToken(IRTextTokens.ASSIGN_KEY));
		putAll(map, IRTextTokens.ASSIGN_SUB_EQUAL, getToken(IRTextTokens.ASSIGN_SUB_EQUAL_KEY));
		putAll(map, IRTextTokens.OP, getToken(IRTextTokens.OP_KEY));
		putAll(map, IRTextTokens.OP_SUB_LOGICAL, getToken(IRTextTokens.OP_SUB_LOGICAL_KEY));
		putAll(map, IRTextTokens.OP_SUB_RELATIONAL, getToken(IRTextTokens.OP_SUB_RELATIONAL_KEY));
		putAll(map, IRTextTokens.SUBACCESS, getToken(IRTextTokens.SUBACCESS_KEY));
		putAll(map, IRTextTokens.NSGET, getToken(IRTextTokens.SUBACCESS_KEY));
		
		putAll(map, IRTextTokens.SPECIALCONST, getToken(IRTextTokens.SPECIALCONST_KEY));
		putAll(map, IRTextTokens.LOGICALCONST, getToken(IRTextTokens.LOGICALCONST_KEY));
		putAll(map, IRTextTokens.SYMBOL, getToken(IRTextTokens.SYMBOL_KEY));
		
		putAll(map, IRTextTokens.NUM, getToken(IRTextTokens.NUM_KEY));
		putAll(map, IRTextTokens.NUM_SUB_INT, getToken(IRTextTokens.NUM_SUB_INT_KEY));
		putAll(map, IRTextTokens.NUM_SUB_CPLX, getToken(IRTextTokens.NUM_SUB_CPLX_KEY));
		putAll(map, IRTextTokens.UNDEFINED, getToken(IRTextTokens.UNDEFINED_KEY));
		
		// usually not in default partition
		putAll(map, IRTextTokens.STRING, getToken(IRTextTokens.STRING_KEY));
		map.put(RTerminal.SYMBOL_G, getToken(IRTextTokens.STRING_KEY));
		map.put(RTerminal.SPECIAL, getToken(IRTextTokens.OP_KEY));
		putAll(map, IRTextTokens.COMMENT, getToken(IRTextTokens.COMMENT_KEY));
	}
	
	protected void updateSymbols(final Map<String, IToken> map) {
		final RIdentifierGroups groups = RUIPlugin.getDefault().getRIdentifierGroups();
		groups.getReadLock().lock();
		try {
			putAll(map, groups.getAssignmentIdentifiers(),
					getToken(IRTextTokens.SYMBOL_SUB_ASSIGN_KEY));
			putAll(map, groups.getLogicalIdentifiers(),
					getToken(IRTextTokens.SYMBOL_SUB_LOGICAL_KEY));
			putAll(map, groups.getFlowcontrolIdentifiers(),
					getToken(IRTextTokens.SYMBOL_SUB_FLOWCONTROL_KEY));
			putAll(map, groups.getCustom1Identifiers(),
					getToken(IRTextTokens.SYMBOL_SUB_CUSTOM1_KEY));
			putAll(map, groups.getCustom2Identifiers(),
					getToken(IRTextTokens.SYMBOL_SUB_CUSTOM2_KEY));
		}
		finally {
			groups.getReadLock().unlock();
		}
	}
	
}
