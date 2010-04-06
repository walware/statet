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

package de.walware.statet.r.core.rsource;

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_FLOAT_EXP_INVALID;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_FLOAT_WITH_L;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_TOKEN_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_OK;

import de.walware.ecommons.text.SourceParseInput;

import de.walware.statet.r.core.rlang.RTerminal;


public abstract class RLexer {
	
	private final static char[] C1_FALSE = RTerminal.S_FALSE.substring(1).toCharArray();
	private final static char[] C1_NA_real_ = RTerminal.S_NA_REAL.substring(1).toCharArray();
	private final static char[] C1_NA_integer_ = RTerminal.S_NA_INT.substring(1).toCharArray();
	private final static char[] C1_NA_complex_ = RTerminal.S_NA_CPLX.substring(1).toCharArray();
	private final static char[] C1_NA_character_ = RTerminal.S_NA_CHAR.substring(1).toCharArray();
	private final static char[] C1_break = RTerminal.S_BREAK.substring(1).toCharArray();
	private final static char[] C1_function = RTerminal.S_FUNCTION.substring(1).toCharArray();
	private final static char[] C1_repeat = RTerminal.S_REPEAT.substring(1).toCharArray();
	private final static char[] C1_while = RTerminal.S_WHILE.substring(1).toCharArray();
	
	
	protected SourceParseInput fInput;
	
	protected int fNextIndex;
	protected int fNextNum;
	private int fUnknownState = -1;
	private StringBuilder fUnknownString = new StringBuilder();
	
	
	/**
	 * Creates and initializes new lexer
	 */
	public RLexer(final SourceParseInput input) {
		reset(input);
	}
	
	/**
	 * Creates new lexer
	 * 
	 * Use {@link #reset(SourceParseInput)} to initialize the lexer
	 */
	protected RLexer() {
	}
	
	
	protected void reset(final SourceParseInput input) {
		fInput = input;
		fNextIndex = input.getIndex();
		fNextNum = 0;
		fUnknownState = -1;
	}
	
	
	public void setFull() {
		fInput.init();
		reset(fInput);
	}
	
	public void setRange(final int offset, final int length) {
		fInput.init(offset, offset+length);
		reset(fInput);
	}
	
	protected final void searchNext() {
		fInput.consume(fNextNum);
		fNextIndex = fInput.getIndex();
		fNextNum = 1;
		final int c1 = fInput.get(1);
		
		searchNext1(c1);
	}
	
	protected void searchNext1(final int c1) {
		switch(c1) { // tableswitch for ascii chars
		case SourceParseInput.EOF:
			createFix(RTerminal.EOF);
			return;
		case '\r':
			if (fInput.get(2) == '\n') {
				fNextNum++;
				handleNewLine();
				createLinebreakToken("\r\n"); //$NON-NLS-1$
				return;
			}
			handleNewLine();
			createLinebreakToken("\r"); //$NON-NLS-1$
			return;
		case '\n':
		case '\f':
			handleNewLine();
			createLinebreakToken("\n"); //$NON-NLS-1$
			return;
		case ' ':
		case '\t':
			consumeWhitespace();
			return;
		case '!':
			if (fInput.get(2) == '=') {
				fNextNum++;
				createFix(RTerminal.REL_NE);
				return;
			}
			createFix(RTerminal.NOT);
			return;
		case '\"':
			consumeStringDoubleQuote();
			return;
		case '#':
			consumeComment();
			return;
		case '$':
			createFix(RTerminal.SUB_NAMED_PART);
			return;
		case '%':
			consumeSpecial();
			return;
		case '&':
			if (fInput.get(2) == '&') {
				fNextNum++;
				createFix(RTerminal.AND_D);
				return;
			}
			createFix(RTerminal.AND);
			return;
		case '\'':
			consumeStringSingleQuote();
			return;
		case '(':
			createFix(RTerminal.GROUP_OPEN);
			return;
		case ')':
			createFix(RTerminal.GROUP_CLOSE);
			return;
		case '*':
			createFix(RTerminal.MULT);
			return;
		case '+':
			createFix(RTerminal.PLUS);
			return;
		case ',':
			createFix(RTerminal.COMMA);
			return;
		case '-':
			if (fInput.get(2) == '>') {
				fNextNum++;
				if (fInput.get(3) == '>') {
					fNextNum++;
					createFix(RTerminal.ARROW_RIGHT_D);
					return;
				}
				createFix(RTerminal.ARROW_RIGHT_S);
				return;
			}
			createFix(RTerminal.MINUS);
			return;
		case '.':
			switch (fInput.get(2)) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				fNextNum++;
				consumeNumberInFloat();
				return;
			default:
				scanIdentifier();
				createSymbolToken();
				return;
			}
		case '/':
			createFix(RTerminal.DIV);
			return;
		case '0':
			if (fInput.get(2) == 'x') {
				fNextNum++;
				consumeNumberInHex();
				return;
			}
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			consumeNumberInDec();
			return;
		case ':':
			if (fInput.get(2) == ':') {
				fNextNum++;
				if (fInput.get(3) == ':') {
					fNextNum++;
					createFix(RTerminal.NS_GET_INT);
					return;
				}
				createFix(RTerminal.NS_GET);
				return;
			}
			createFix(RTerminal.SEQ);
			return;
		case ';':
			createFix(RTerminal.SEMI);
			return;
		case '<':
			switch (fInput.get(2)) {
			case '=':
				fNextNum++;
				createFix(RTerminal.REL_LE);
				return;
			case '-':
				fNextNum++;
				createFix(RTerminal.ARROW_LEFT_S);
				return;
			case '<':
				if (fInput.get(3) == '-') {
					fNextNum++;
					fNextNum++;
					createFix(RTerminal.ARROW_LEFT_D);
					return;
				}
			default:
				createFix(RTerminal.REL_LT);
				return;
			}
		case '=':
			if (fInput.get(2) == '=') {
				fNextNum++;
				createFix(RTerminal.REL_EQ);
				return;
			}
			createFix(RTerminal.EQUAL);
			return;
		case '>':
			if (fInput.get(2) == '=') {
				fNextNum++;
				createFix(RTerminal.REL_GE);
				return;
			}
			createFix(RTerminal.REL_LT);
			return;
		case '?':
			createFix(RTerminal.QUESTIONMARK);
			return;
		case '@':
			createFix(RTerminal.SUB_NAMED_SLOT);
			return;
		case '[':
			if (fInput.get(2) == '[') {
				fNextNum++;
				createFix(RTerminal.SUB_INDEXED_D_OPEN);
				return;
			}
			createFix(RTerminal.SUB_INDEXED_S_OPEN);
			return;
		case ']':
			createFix(RTerminal.SUB_INDEXED_CLOSE);
			return;
		case '^':
			createFix(RTerminal.POWER);
			return;
		case 'A':
		case 'B':
		case 'C':
		case 'D':
		case 'E':
		case 'G':
		case 'H':
		case 'J':
		case 'K':
		case 'L':
		case 'M':
		case 'O':
		case 'P':
		case 'Q':
		case 'R':
		case 'S':
		case 'U':
		case 'V':
		case 'W':
		case 'X':
		case 'Y':
		case 'Z':
		case 'a':
		case 'c':
		case 'd':
		case 'g':
		case 'h':
		case 'j':
		case 'k':
		case 'l':
		case 'm':
		case 'o':
		case 'p':
		case 'q':
		case 's':
		case 't':
		case 'u':
		case 'v':
		case 'x':
		case 'y':
		case 'z':
			scanIdentifier();
			createSymbolToken();
			return;
		case 'F':
			scanIdentifier();
			if (fNextNum == 5
					&& fInput.subequals(2, C1_FALSE)) {
				createFix(RTerminal.FALSE);
				return;
			}
			createSymbolToken();
			return;
		case 'I':
			scanIdentifier();
			if (fNextNum == 3
					&& fInput.subequals(2, 'n', 'f')) {
				createFix(RTerminal.INF);
				return;
			}
			createSymbolToken();
			return;
		case 'N':
			scanIdentifier();
			switch(fNextNum) {
			case 2:
				if (fInput.get(2) == 'A') {
					createFix(RTerminal.NA);
					return;
				}
				break;
			case 3:
				if (fInput.subequals(2, 'a', 'N')) {
					createFix(RTerminal.NAN);
					return;
				}
				break;
			case 4:
				if (fInput.subequals(2, 'U', 'L', 'L')) {
					createFix(RTerminal.NULL);
					return;
				}
				break;
			case 7:
				if (fInput.subequals(2, C1_NA_real_)) {
					createFix(RTerminal.NA_REAL);
					return;
				}
				break;
			case 11:
				if (fInput.subequals(2, C1_NA_integer_)) {
					createFix(RTerminal.NA_INT);
					return;
				}
				if (fInput.subequals(2, C1_NA_complex_)) {
					createFix(RTerminal.NA_CPLX);
					return;
				}
				break;
			case 13:
				if (fInput.subequals(2, C1_NA_character_)) {
					createFix(RTerminal.NA_CHAR);
					return;
				}
				break;
			}
			createSymbolToken();
			return;
		case 'T':
			scanIdentifier();
			if (fNextNum == 4
					&& fInput.subequals(2, 'R', 'U', 'E')) {
				createFix(RTerminal.TRUE);
				return;
			}
			createSymbolToken();
			return;
		case 'b':
			scanIdentifier();
			if (fNextNum == 5
					&& fInput.subequals(2, C1_break)) {
				createFix(RTerminal.BREAK);
				return;
			}
			createSymbolToken();
			return;
		case 'i':
			scanIdentifier();
			if (fNextNum == 2) {
				if (fInput.get(2) == 'f') {
					createFix(RTerminal.IF);
					return;
				}
				if (fInput.get(2) == 'n') {
					createFix(RTerminal.IN);
					return;
				}
			}
			createSymbolToken();
			return;
		case 'e':
			scanIdentifier();
			if (fNextNum == 4
					&& fInput.subequals(2, 'l', 's', 'e')) {
				createFix(RTerminal.ELSE);
				return;
			}
			createSymbolToken();
			return;
		case 'n':
			scanIdentifier();
			if (fNextNum == 4
					&& fInput.subequals(2, 'e', 'x', 't')) {
				createFix(RTerminal.NEXT);
				return;
			}
			createSymbolToken();
			return;
		case 'f':
			scanIdentifier();
			switch (fNextNum) {
			case 3:
				if (fInput.subequals(2, 'o', 'r')) {
					createFix(RTerminal.FOR);
					return;
				}
				break;
			case 8:
				if (fInput.subequals(2, C1_function)) {
					createFix(RTerminal.FUNCTION);
					return;
				}
				break;
			}
			createSymbolToken();
			return;
		case 'r':
			scanIdentifier();
			if (fNextNum == 6
					&& fInput.subequals(2, C1_repeat)) {
				createFix(RTerminal.REPEAT);
				return;
			}
			createSymbolToken();
			return;
		case 'w':
			scanIdentifier();
			if (fNextNum == 5
					&& fInput.subequals(2, C1_while)) {
				createFix(RTerminal.WHILE);
				return;
			}
			createSymbolToken();
			return;
		case '{':
			createFix(RTerminal.BLOCK_OPEN);
			return;
		case '|':
			if (fInput.get(2) == '|') {
				fNextNum++;
				createFix(RTerminal.OR_D);
				return;
			}
			createFix(RTerminal.OR);
			return;
		case '}':
			createFix(RTerminal.BLOCK_CLOSE);
			return;
		case '~':
			createFix(RTerminal.TILDE);
			return;
		case '`':
			consumeSymbolGraveQuote();
			return;
		default:
			if (Character.isLetterOrDigit(c1)) {
				scanIdentifier();
				createSymbolToken();
				return;
			}
			break;
		}
		
		if (fUnknownState == 0) {
			fUnknownState = 1;
			return;
		}
		consumeUnknown();
	}
	
	private final void consumeUnknown() {
		final int unknownIndex = fNextIndex;
		int unknownNum = 0;
		do {
			unknownNum += fNextNum;
			fUnknownString.append(fInput.substring(1, fNextNum));
			
			fUnknownState = 0;
			searchNext();
		}
		while (fUnknownState == 1);
		fUnknownState = -1;
		
		fNextIndex = unknownIndex;
		fNextNum = unknownNum;
		createUnknownToken(fUnknownString.toString());
		
		if (fUnknownString.length() > 40 || fUnknownString.capacity() > 40) {
			fUnknownString.setLength(16);
			fUnknownString.trimToSize();
		}
		fUnknownString.setLength(0);
		fNextNum = 0;
		return;
	}
	
	private final void consumeWhitespace() {
		// 1 == ' ' | '\t'
		LOOP : while (true) {
			switch (fInput.get(++fNextNum)) {
			case ' ':
			case '\t':
				continue LOOP;
			default:
				fNextNum--;
				createWhitespaceToken();
				return;
			}
		}
	}
	
	private final void consumeComment() {
		// 1 == '#'
		final RTerminal type;
		if (fInput.get(2) == '\'') {
			type = RTerminal.ROXYGEN_COMMENT;
			fNextNum++;
		}
		else {
			type = RTerminal.COMMENT;
		}
		LOOP : while (true) {
			switch (fInput.get(++fNextNum)) {
			case SourceParseInput.EOF:
			case '\r':
			case '\n':
				fNextNum--;
				createCommentToken(type);
				return;
			default:
				continue LOOP;
			}
		}
	}
	
	private final void consumeStringDoubleQuote() {
		// 1 == '\"'
		LOOP : while (true) {
			switch (fInput.get(++fNextNum)) {
			case '\\':
				if (fInput.get(++fNextNum) == SourceParseInput.EOF) {
					fNextNum--;
				}
				continue LOOP;
			case '\"':
				createStringToken(RTerminal.STRING_D, STATUS_OK);
				return;
			case '\r':
				if (fInput.get(++fNextNum) != '\n') {
					fNextNum--;
				}
				handleNewLine();
				continue LOOP;
			case '\n':
				handleNewLine();
				continue LOOP;
			case SourceParseInput.EOF:
				fNextNum--;
				createStringToken(RTerminal.STRING_D, STATUS2_SYNTAX_TOKEN_NOT_CLOSED);
				return;
			default:
				continue LOOP;
			}
		}
	}
	
	private final void consumeStringSingleQuote() {
		// 1 == '\''
		LOOP : while (true) {
			switch (fInput.get(++fNextNum)) {
			case '\\':
				if (fInput.get(++fNextNum) == SourceParseInput.EOF) {
					fNextNum--;
				}
				continue LOOP;
			case '\'':
				createStringToken(RTerminal.STRING_S, STATUS_OK);
				return;
			case '\r':
				if (fInput.get(++fNextNum) != '\n') {
					fNextNum--;
				}
				handleNewLine();
				continue LOOP;
			case '\n':
				handleNewLine();
				continue LOOP;
			case SourceParseInput.EOF:
				fNextNum--;
				createStringToken(RTerminal.STRING_S, STATUS2_SYNTAX_TOKEN_NOT_CLOSED);
				return;
			default:
				continue LOOP;
			}
		}
	}
	
	private final void consumeSymbolGraveQuote() {
		// 1 == '`'
		LOOP : while (true) {
			switch (fInput.get(++fNextNum)) {
			case '\\':
				if (fInput.get(++fNextNum) == SourceParseInput.EOF) {
					fNextNum--;
				}
				continue LOOP;
			case '`':
				createQuotedSymbolToken(RTerminal.SYMBOL_G, STATUS_OK);
				return;
			case '\r':
				if (fInput.get(++fNextNum) != '\n') {
					fNextNum--;
				}
				handleNewLine();
				continue LOOP;
			case '\n':
				handleNewLine();
				continue LOOP;
			case SourceParseInput.EOF:
				fNextNum--;
				createQuotedSymbolToken(RTerminal.SYMBOL_G, STATUS2_SYNTAX_TOKEN_NOT_CLOSED);
				return;
			default:
				continue LOOP;
			}
		}
	}
	
	private final void consumeNumberInHex() {
		// 1 == '0'; 2 == 'x'
		LOOP : while (true) {
			switch (fInput.get(++fNextNum)) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
				continue LOOP;
			case 'L':
				createNumberToken(RTerminal.NUM_INT, STATUS_OK);
				return;
			case 'i':
				createNumberToken(RTerminal.NUM_COMPLEX, STATUS_OK);
				return;
			default:
				fNextNum--;
				createNumberToken(RTerminal.NUM_NUM, STATUS_OK);
				return;
			}
		}
	}
	
	private final void consumeNumberInDec() {
		// only dec digits
		int status = STATUS_OK;
		LOOP : while (true) {
			final int next = fInput.get(++fNextNum);
			if (next >= '0' && next <= '9') {
				continue LOOP;
			}
			switch(next) {
			case '.':
				consumeNumberInFloat();
				return;
			case 'e':
			case 'E':
				status = scanNumberInExp();
				continue LOOP;
			case 'L':
				createNumberToken(RTerminal.NUM_INT, status);
				return;
			case 'i':
				createNumberToken(RTerminal.NUM_COMPLEX, status);
				return;
			default:
				fNextNum--;
				createNumberToken(RTerminal.NUM_NUM, status);
				return;
			}
		}
	}
	
	private final void consumeNumberInFloat() {
		// after .
		int status = STATUS_OK;
		LOOP : while (true) {
			final int next = fInput.get(++fNextNum);
			if (next >= '0' && next <= '9') {
				continue LOOP;
			}
			switch (next) {
			case 'E':
			case 'e':
				status = scanNumberInExp();
				continue LOOP;
			case 'L':
				createNumberToken(RTerminal.NUM_NUM, status != STATUS_OK ? status : STATUS2_SYNTAX_FLOAT_WITH_L);
				return;
			case 'i':
				createNumberToken(RTerminal.NUM_COMPLEX, status);
				return;
			default:
				fNextNum--;
				createNumberToken(RTerminal.NUM_NUM, status);
				return;
			}
		}
	}
	
	private final int scanNumberInExp() {
		// after e // w/o i/L
		final int start = fInput.get(++fNextNum);
		START : if (start < '0' || start > '9') {
			if (start == '+' || start == '-') {
				final int start2 = fInput.get(++fNextNum);
				if (start2 >= '0' && start2 <= '9') {
					break START;
				}
			}
			fNextNum--;
			return STATUS2_SYNTAX_FLOAT_EXP_INVALID;
		}
		LOOP : while (true) {
			final int next = fInput.get(++fNextNum);
			if (next >= '0' && next <= '9') {
				continue LOOP;
			}
			fNextNum--;
			return STATUS_OK;
		}
	}
	
	protected final void scanIdentifier() {
		// after legal start
		LOOP : while (true) {
			final int next = fInput.get(++fNextNum);
			if (next <= 'z' && (
					(next >= 'a')
					|| (next <= 'Z' && (
							(next >= 'A')
							||	(next <= '9' && (next >= '0' || next == '.')) ))
					|| (next == '_')
					)) {
				continue LOOP;
			}
			if (Character.isLetterOrDigit(next)) {
				continue LOOP;
			}
			fNextNum--;
			return;
		}
	}
	
	private final void consumeSpecial() {
		// after %
		LOOP : while (true) {
			switch (fInput.get(++fNextNum)) {
			case '%':
				createSpecialToken(STATUS_OK);
				return;
			case '\n':
			case '\r':
			case SourceParseInput.EOF:
				fNextNum--;
				createSpecialToken(STATUS2_SYNTAX_TOKEN_NOT_CLOSED);
				return;
			default:
				continue LOOP;
			}
		}
	}
	
	
	protected abstract void createFix(RTerminal type);
	protected abstract void createSpecialToken(int status);
	protected abstract void createSymbolToken();
	protected abstract void createQuotedSymbolToken(RTerminal type, int status);
	protected abstract void createStringToken(RTerminal type, int status);
	protected abstract void createNumberToken(RTerminal type, int status);
	protected abstract void createWhitespaceToken();
	protected abstract void createCommentToken(RTerminal type);
	protected abstract void createLinebreakToken(String text);
	protected abstract void createUnknownToken(String text);
	
	protected void handleNewLine() {
	}
	
}
