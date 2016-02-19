/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.text.r;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

import de.walware.ecommons.text.core.input.DocumentParserInput;
import de.walware.ecommons.text.ui.presentation.ITextPresentationConstants;
import de.walware.ecommons.text.ui.settings.TextStyleManager;
import de.walware.ecommons.ui.ISettingsChangedHandler;

import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.RLexer;
import de.walware.statet.r.internal.ui.RIdentifierGroups;
import de.walware.statet.r.internal.ui.RUIPlugin;


/**
 * Text token scanner for R code.
 * Mainly for R default partitions, but also works for other partitions (without special styles).
 * 
 * Version 2 uses RLexer instead of rules to parse the sources.
 */
public class RDefaultTextStyleScanner extends DocumentParserInput implements ITokenScanner, ISettingsChangedHandler {
	
	
	protected static void putAll(final Map<String, IToken> map, final String[] symbols, final IToken token) {
		for (int i= 0; i < symbols.length; i++) {
			map.put(symbols[i], token);
		}
	}
	
	protected static void putAll(final Map<RTerminal, IToken> map, final RTerminal[] types, final IToken token) {
		for (int i= 0; i < types.length; i++) {
			map.put(types[i], token);
		}
	}
	
	
	private final RLexer lexer;
	private RTerminal lexerToken;
	
	private final EnumMap<RTerminal, IToken> tokens;
	private final IToken defaultToken;
	private final Map<String, IToken> specialSymbols;
	private final TextStyleManager textStyles;
	
	private int currentOffset;
	private int currentLength;
	
	
	public RDefaultTextStyleScanner(final TextStyleManager textStyles) {
		this.lexer= createLexer();
		this.lexer.reset(this);
		this.textStyles= textStyles;
		
		this.defaultToken= getToken(IRTextTokens.SYMBOL_KEY);
		this.tokens= new EnumMap<>(RTerminal.class);
		registerTokens(this.tokens);
//		checkTokenMap();
		this.specialSymbols= new HashMap<>();
		updateSymbols(this.specialSymbols);
	}
	
	
	protected RLexer createLexer() {
		return new RLexer((RLexer.DEFAULT | RLexer.SKIP_WHITESPACE | RLexer.SKIP_LINEBREAK |
				RLexer.ENABLE_QUICK_CHECK ));
	}
	
	protected void checkTokenMap() {
		final RTerminal[] all= RTerminal.values();
		for (final RTerminal t : all) {
			if (this.tokens.get(t) == null) {
				System.out.println("Style Missing for: " + t.name()); //$NON-NLS-1$
			}
		}
	}
	
	
	protected final RLexer getLexer() {
		return this.lexer;
	}
	
	protected final TextStyleManager getTextStyles() {
		return this.textStyles;
	}
	
	@Override
	public void setRange(final IDocument document, final int offset, final int length) {
		reset(document);
		init(offset, offset + length);
		this.lexer.reset(this);
		
		this.currentOffset= offset;
		this.currentLength= 0;
	}
	
	protected void resetSpecialSymbols() {
		this.specialSymbols.clear();
		updateSymbols(this.specialSymbols);
	}
	
	
	@Override
	public IToken nextToken() {
		this.currentOffset+= this.currentLength;
		if (this.lexerToken == null) {
			this.lexerToken= this.lexer.next();
		}
		this.currentLength= this.lexer.getOffset() - this.currentOffset;
		if (this.currentLength != 0) {
			return this.defaultToken;
		}
		this.currentLength= this.lexer.getLength();
		return getTokenFromScannerToken();
	}
	
	protected IToken getTokenFromScannerToken() {
		IToken token;
		if (this.lexerToken == RTerminal.SYMBOL) {
			final String text= this.lexer.getText();
			if (text != null) {
				token= this.specialSymbols.get(text);
				if (token != null) {
					this.lexerToken= null;
					return token;
				}
			}
			this.lexerToken= null;
			return this.defaultToken;
		}
		token= this.tokens.get(this.lexerToken);
		this.lexerToken= null;
		return token;
	}
	
	@Override
	public int getTokenOffset() {
		return this.currentOffset;
	}
	
	@Override
	public int getTokenLength() {
		return this.currentLength;
	}
	
	
	protected IToken getToken(final String key) {
		return this.textStyles.getToken(key);
	}
	
	@Override
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
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
		final RIdentifierGroups groups= RUIPlugin.getDefault().getRIdentifierGroups();
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
