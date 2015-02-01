/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.rsource;

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_FLOAT_EXP_INVALID;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_FLOAT_WITH_L;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_TOKEN_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_OK;

import de.walware.ecommons.text.SourceParseInput;

import de.walware.statet.r.core.rlang.RTerminal;


public class RLexer {
	
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
	
	protected RTerminal fFoundType;
	protected int fFoundOffset;
	protected int fFoundNum;
	private int fFoundLength;
	protected String fFoundText;
	protected int fFoundStatus;
	
	
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
	public RLexer() {
	}
	
	
	public void reset(final SourceParseInput input) {
		fInput = input;
		fFoundOffset = input.getIndex();
		fFoundNum = 0;
		fFoundLength = 0;
	}
	
	
	public void setFull() {
		fInput.init();
		reset(fInput);
	}
	
	public void setRange(final int offset, final int length) {
		fInput.init(offset, offset+length);
		reset(fInput);
	}
	
	public RTerminal next() {
		fFoundType = null;
		while (fFoundType == null) {
			searchNext();
		}
		return fFoundType;
	}
	
	public final RTerminal getType() {
		return fFoundType;
	}
	
	public final int getOffset() {
		return fFoundOffset;
	}
	
	public final int getLength() {
		return fFoundLength;
	}
	
	public final String getText() {
		return fFoundText;
	}
	
	public final int getStatusCode() {
		return fFoundStatus;
	}
	
	protected final void searchNext() {
		fInput.consume(fFoundNum, fFoundLength);
		fFoundOffset = fInput.getIndex();
		fFoundNum = 1;
		final int c1 = fInput.get(1);
		
		searchNext1(c1);
		fFoundLength = fInput.getLength(fFoundNum);
	}
	
	protected void searchNext1(final int c1) {
		switch(c1) { // tableswitch for ascii chars
		case SourceParseInput.EOF:
			fFoundNum--;
			createFix(RTerminal.EOF);
			return;
		case '\r':
			if (fInput.get(2) == '\n') {
				fFoundNum++;
				handleNewLine(fInput.getIndex()+fInput.getLength(2));
				createLinebreakToken("\r\n"); //$NON-NLS-1$
				return;
			}
			handleNewLine(fInput.getIndex()+fInput.getLength(1));
			createLinebreakToken("\r"); //$NON-NLS-1$
			return;
		case '\n':
		case '\f':
			handleNewLine(fInput.getIndex()+fInput.getLength(1));
			createLinebreakToken("\n"); //$NON-NLS-1$
			return;
		case ' ':
		case '\t':
			LOOP: while (true) {
				switch (fInput.get(++fFoundNum)) {
				case ' ':
				case '\t':
					continue LOOP;
				default:
					fFoundNum--;
					createWhitespaceToken();
					return;
				}
			}
		case '!':
			if (fInput.get(2) == '=') {
				fFoundNum++;
				createFix(RTerminal.REL_NE);
				return;
			}
			createFix(RTerminal.NOT);
			return;
		case '\"':
			LOOP: while (true) {
				switch (fInput.get(++fFoundNum)) {
				case '\\':
					if (fInput.get(++fFoundNum) == SourceParseInput.EOF) {
						fFoundNum--;
					}
					continue LOOP;
				case '\"':
					createStringToken(RTerminal.STRING_D, STATUS_OK);
					return;
				case '\r':
					if (fInput.get(++fFoundNum) != '\n') {
						fFoundNum--;
					}
					handleNewLine(fInput.getIndex()+fInput.getLength(fFoundNum));
					continue LOOP;
				case '\n':
					handleNewLine(fInput.getIndex()+fInput.getLength(fFoundNum));
					continue LOOP;
				case SourceParseInput.EOF:
					fFoundNum--;
					createStringToken(RTerminal.STRING_D, STATUS2_SYNTAX_TOKEN_NOT_CLOSED);
					return;
				default:
					continue LOOP;
				}
			}
		case '#':
			final RTerminal type;
			if (fInput.get(2) == '\'') {
				type = RTerminal.ROXYGEN_COMMENT;
				fFoundNum++;
			}
			else {
				type = RTerminal.COMMENT;
			}
			LOOP: while (true) {
				switch (fInput.get(++fFoundNum)) {
				case SourceParseInput.EOF:
				case '\r':
				case '\n':
					fFoundNum--;
					createCommentToken(type);
					return;
				default:
					continue LOOP;
				}
			}
		case '$':
			createFix(RTerminal.SUB_NAMED_PART);
			return;
		case '%':
			LOOP: while (true) {
				switch (fInput.get(++fFoundNum)) {
				case '%':
					createSpecialToken(STATUS_OK);
					return;
				case '\n':
				case '\r':
				case SourceParseInput.EOF:
					fFoundNum--;
					createSpecialToken(STATUS2_SYNTAX_TOKEN_NOT_CLOSED);
					return;
				default:
					continue LOOP;
				}
			}
		case '&':
			if (fInput.get(2) == '&') {
				fFoundNum++;
				createFix(RTerminal.AND_D);
				return;
			}
			createFix(RTerminal.AND);
			return;
		case '\'':
			LOOP: while (true) {
				switch (fInput.get(++fFoundNum)) {
				case '\\':
					if (fInput.get(++fFoundNum) == SourceParseInput.EOF) {
						fFoundNum--;
					}
					continue LOOP;
				case '\'':
					createStringToken(RTerminal.STRING_S, STATUS_OK);
					return;
				case '\r':
					if (fInput.get(++fFoundNum) != '\n') {
						fFoundNum--;
					}
					handleNewLine(fInput.getIndex()+fInput.getLength(fFoundNum));
					continue LOOP;
				case '\n':
					handleNewLine(fInput.getIndex()+fInput.getLength(fFoundNum));
					continue LOOP;
				case SourceParseInput.EOF:
					fFoundNum--;
					createStringToken(RTerminal.STRING_S, STATUS2_SYNTAX_TOKEN_NOT_CLOSED);
					return;
				default:
					continue LOOP;
				}
			}
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
				fFoundNum++;
				if (fInput.get(3) == '>') {
					fFoundNum++;
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
				fFoundNum++;
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
				fFoundNum++;
				consumeNumberInHex();
				return;
			}
			//$FALL-THROUGH$
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
				fFoundNum++;
				if (fInput.get(3) == ':') {
					fFoundNum++;
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
				fFoundNum++;
				createFix(RTerminal.REL_LE);
				return;
			case '-':
				fFoundNum++;
				createFix(RTerminal.ARROW_LEFT_S);
				return;
			case '<':
				if (fInput.get(3) == '-') {
					fFoundNum++;
					fFoundNum++;
					createFix(RTerminal.ARROW_LEFT_D);
					return;
				}
				//$FALL-THROUGH$
			default:
				createFix(RTerminal.REL_LT);
				return;
			}
		case '=':
			if (fInput.get(2) == '=') {
				fFoundNum++;
				createFix(RTerminal.REL_EQ);
				return;
			}
			createFix(RTerminal.EQUAL);
			return;
		case '>':
			if (fInput.get(2) == '=') {
				fFoundNum++;
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
				fFoundNum++;
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
			if (fFoundNum == 5
					&& fInput.subequals(2, C1_FALSE)) {
				createFix(RTerminal.FALSE);
				return;
			}
			createSymbolToken();
			return;
		case 'I':
			scanIdentifier();
			if (fFoundNum == 3
					&& fInput.subequals(2, 'n', 'f')) {
				createFix(RTerminal.INF);
				return;
			}
			createSymbolToken();
			return;
		case 'N':
			scanIdentifier();
			switch(fFoundNum) {
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
			case 8:
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
			if (fFoundNum == 4
					&& fInput.subequals(2, 'R', 'U', 'E')) {
				createFix(RTerminal.TRUE);
				return;
			}
			createSymbolToken();
			return;
		case 'b':
			scanIdentifier();
			if (fFoundNum == 5
					&& fInput.subequals(2, C1_break)) {
				createFix(RTerminal.BREAK);
				return;
			}
			createSymbolToken();
			return;
		case 'i':
			scanIdentifier();
			if (fFoundNum == 2) {
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
			if (fFoundNum == 4
					&& fInput.subequals(2, 'l', 's', 'e')) {
				createFix(RTerminal.ELSE);
				return;
			}
			createSymbolToken();
			return;
		case 'n':
			scanIdentifier();
			if (fFoundNum == 4
					&& fInput.subequals(2, 'e', 'x', 't')) {
				createFix(RTerminal.NEXT);
				return;
			}
			createSymbolToken();
			return;
		case 'f':
			scanIdentifier();
			switch (fFoundNum) {
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
			if (fFoundNum == 6
					&& fInput.subequals(2, C1_repeat)) {
				createFix(RTerminal.REPEAT);
				return;
			}
			createSymbolToken();
			return;
		case 'w':
			scanIdentifier();
			if (fFoundNum == 5
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
				fFoundNum++;
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
			LOOP: while (true) {
				switch (fInput.get(++fFoundNum)) {
				case '\\':
					if (fInput.get(++fFoundNum) == SourceParseInput.EOF) {
						fFoundNum--;
					}
					continue LOOP;
				case '`':
					createQuotedSymbolToken(RTerminal.SYMBOL_G, STATUS_OK);
					return;
				case '\r':
					if (fInput.get(++fFoundNum) != '\n') {
						fFoundNum--;
					}
					handleNewLine(fInput.getIndex()+fInput.getLength(fFoundNum));
					continue LOOP;
				case '\n':
					handleNewLine(fInput.getIndex()+fInput.getLength(fFoundNum));
					continue LOOP;
				case SourceParseInput.EOF:
					fFoundNum--;
					createQuotedSymbolToken(RTerminal.SYMBOL_G, STATUS2_SYNTAX_TOKEN_NOT_CLOSED);
					return;
				default:
					continue LOOP;
				}
			}
		case 0x000:
		case 0x001:
		case 0x002:
		case 0x003:
		case 0x004:
		case 0x005:
		case 0x006:
		case 0x007:
		case 0x008:
//		case 0x009:
//		case 0x00A:
		case 0x00B:
//		case 0x00C:
//		case 0x00D:
		case 0x00E:
		case 0x00F:
		case 0x010:
		case 0x011:
		case 0x012:
		case 0x013:
		case 0x014:
		case 0x015:
		case 0x016:
		case 0x017:
		case 0x018:
		case 0x019:
		case 0x01A:
		case 0x01B:
		case 0x01C:
		case 0x01D:
		case 0x01E:
		case 0x01F:
//		case 0x020:
//		case 0x07E:
		case 0x07F:
		case 0x080:
		case 0x081:
		case 0x082:
		case 0x083:
		case 0x084:
		case 0x085:
		case 0x086:
		case 0x087:
		case 0x088:
		case 0x089:
		case 0x08A:
		case 0x08B:
		case 0x08C:
		case 0x08D:
		case 0x08E:
		case 0x08F:
		case 0x090:
		case 0x091:
		case 0x092:
		case 0x093:
		case 0x094:
		case 0x095:
		case 0x096:
		case 0x097:
		case 0x098:
		case 0x099:
		case 0x09A:
		case 0x09B:
		case 0x09C:
		case 0x09D:
		case 0x09E:
		case 0x09F:
		case 0x0A0:
		case 0x0A1:
		case 0x0A2:
		case 0x0A3:
		case 0x0A4:
		case 0x0A5:
		case 0x0A6:
		case 0x0A7:
		case 0x0A8:
		case 0x0A9:
		case 0x0AA:
		case 0x0AB:
		case 0x0AC:
		case 0x0AD:
		case 0x0AE:
		case 0x0AF:
		case 0x0B0:
		case 0x0B1:
		case 0x0B2:
		case 0x0B3:
		case 0x0B4:
		case 0x0B5:
		case 0x0B6:
		case 0x0B7:
		case 0x0B8:
		case 0x0B9:
		case 0x0BA:
		case 0x0BB:
		case 0x0BC:
		case 0x0BD:
		case 0x0BE:
		case 0x0BF:
			break;
		default:
			if (Character.isLetterOrDigit(c1)) {
				scanIdentifier();
				createSymbolToken();
				return;
			}
			break;
		}
		
		consumeUnknown();
	}
	
	private final void consumeUnknown() {
		// TODO: consume multiple unknown chars
		createUnknownToken(fInput.substring(1, fInput.getLength(fFoundNum)));
		return;
	}
	
	private final void consumeNumberInHex() {
		// 1 == '0'; 2 == 'x'
		LOOP: while (true) {
			switch (fInput.get(++fFoundNum)) {
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
				fFoundNum--;
				createNumberToken(RTerminal.NUM_NUM, STATUS_OK);
				return;
			}
		}
	}
	
	private final void consumeNumberInDec() {
		// only dec digits
		int status = STATUS_OK;
		LOOP: while (true) {
			switch(fInput.get(++fFoundNum)) {
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
				continue LOOP;
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
				fFoundNum--;
				createNumberToken(RTerminal.NUM_NUM, status);
				return;
			}
		}
	}
	
	private final void consumeNumberInFloat() {
		// after .
		int status = STATUS_OK;
		LOOP: while (true) {
			switch(fInput.get(++fFoundNum)) {
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
				continue LOOP;
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
				fFoundNum--;
				createNumberToken(RTerminal.NUM_NUM, status);
				return;
			}
		}
	}
	
	private final int scanNumberInExp() {
		// after e // w/o i/L
		final int start = fInput.get(++fFoundNum);
		START: if (start < '0' || start > '9') {
			if (start == '+' || start == '-') {
				final int start2 = fInput.get(++fFoundNum);
				if (start2 >= '0' && start2 <= '9') {
					break START;
				}
			}
			fFoundNum--;
			return STATUS2_SYNTAX_FLOAT_EXP_INVALID;
		}
		LOOP: while (true) {
			final int next = fInput.get(++fFoundNum);
			if (next >= '0' && next <= '9') {
				continue LOOP;
			}
			fFoundNum--;
			return STATUS_OK;
		}
	}
	
	protected final void scanIdentifier() {
		// after legal start
		LOOP: while (true) {
			final int next = fInput.get(++fFoundNum);
			switch (next) {
			case '.':
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
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
			case 'g':
			case 'h':
			case 'i':
			case 'j':
			case 'k':
			case 'l':
			case 'm':
			case 'n':
			case 'o':
			case 'p':
			case 'q':
			case 'r':
			case 's':
			case 't':
			case 'u':
			case 'v':
			case 'w':
			case 'x':
			case 'y':
			case 'z':
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'G':
			case 'H':
			case 'I':
			case 'J':
			case 'K':
			case 'L':
			case 'M':
			case 'N':
			case 'O':
			case 'P':
			case 'Q':
			case 'R':
			case 'S':
			case 'T':
			case 'U':
			case 'V':
			case 'W':
			case 'X':
			case 'Y':
			case 'Z':
			case '_':
				continue LOOP;
			case SourceParseInput.EOF:
			case 0x000:
			case 0x001:
			case 0x002:
			case 0x003:
			case 0x004:
			case 0x005:
			case 0x006:
			case 0x007:
			case 0x008:
			case 0x009:
			case 0x00A:
			case 0x00B:
			case 0x00C:
			case 0x00D:
			case 0x00E:
			case 0x00F:
			case 0x010:
			case 0x011:
			case 0x012:
			case 0x013:
			case 0x014:
			case 0x015:
			case 0x016:
			case 0x017:
			case 0x018:
			case 0x019:
			case 0x01A:
			case 0x01B:
			case 0x01C:
			case 0x01D:
			case 0x01E:
			case 0x01F:
			case 0x020:
			case 0x021:
			case 0x022:
			case 0x023:
			case 0x024:
			case 0x025:
			case 0x026:
			case 0x027:
			case 0x028:
			case 0x029:
			case 0x02A:
			case 0x02B:
			case 0x02C:
			case 0x02D:
//			case 0x02E: .
			case 0x02F:
//			case 0x030: 0 ...
//			case 0x039: 9
			case 0x03A:
			case 0x03B:
			case 0x03C:
			case 0x03D:
			case 0x03E:
			case 0x03F:
			case 0x040:
//			case 0x041: A
//			case 0x05A: Z
			case 0x05B:
			case 0x05C:
			case 0x05D:
			case 0x05E:
//			case 0x05F: _
			case 0x060:
//			case 0x061: a
//			case 0x07A: z
			case 0x07B:
			case 0x07C:
			case 0x07D:
			case 0x07E:
			case 0x07F:
			case 0x080:
			case 0x081:
			case 0x082:
			case 0x083:
			case 0x084:
			case 0x085:
			case 0x086:
			case 0x087:
			case 0x088:
			case 0x089:
			case 0x08A:
			case 0x08B:
			case 0x08C:
			case 0x08D:
			case 0x08E:
			case 0x08F:
			case 0x090:
			case 0x091:
			case 0x092:
			case 0x093:
			case 0x094:
			case 0x095:
			case 0x096:
			case 0x097:
			case 0x098:
			case 0x099:
			case 0x09A:
			case 0x09B:
			case 0x09C:
			case 0x09D:
			case 0x09E:
			case 0x09F:
			case 0x0A0:
			case 0x0A1:
			case 0x0A2:
			case 0x0A3:
			case 0x0A4:
			case 0x0A5:
			case 0x0A6:
			case 0x0A7:
			case 0x0A8:
			case 0x0A9:
			case 0x0AA:
			case 0x0AB:
			case 0x0AC:
			case 0x0AD:
			case 0x0AE:
			case 0x0AF:
			case 0x0B0:
			case 0x0B1:
			case 0x0B2:
			case 0x0B3:
			case 0x0B4:
			case 0x0B5:
			case 0x0B6:
			case 0x0B7:
			case 0x0B8:
			case 0x0B9:
			case 0x0BA:
			case 0x0BB:
			case 0x0BC:
			case 0x0BD:
			case 0x0BE:
			case 0x0BF:
				fFoundNum--;
				return;
			default:
				if (Character.isLetterOrDigit(next)) {
					continue LOOP;
				}
				fFoundNum--;
				return;
			}
		}
	}
	
	
	protected void createFix(final RTerminal type) {
		fFoundType = type;
		fFoundText = null;
		fFoundStatus = STATUS_OK;
	}
	
	protected void createSpecialToken(final int status) {
		fFoundType = RTerminal.SPECIAL;
		fFoundText = null;
		fFoundStatus = status;
	}
	
	protected void createSymbolToken() {
		fFoundType = RTerminal.SYMBOL;
		fFoundText = null;
		fFoundStatus = STATUS_OK;
	}
	
	protected void createQuotedSymbolToken(final RTerminal type, final int status) {
		fFoundType = type;
		fFoundText = null;
		fFoundStatus = status;
	}
	
	protected void createStringToken(final RTerminal type, final int status) {
		fFoundType = type;
		fFoundText = null;
		fFoundStatus = status;
	}
	
	protected void createNumberToken(final RTerminal type, final int status) {
		fFoundType = type;
		fFoundText = null;
		fFoundStatus = status;
	}
	
	protected void createWhitespaceToken() {
		fFoundType = RTerminal.BLANK;
		fFoundText = null;
		fFoundStatus = STATUS_OK;
	}
	
	protected void createCommentToken(final RTerminal type) {
		fFoundType = type;
		fFoundText = null;
		fFoundStatus = STATUS_OK;
	}
	
	protected void createLinebreakToken(final String text) {
		fFoundType = RTerminal.LINEBREAK;
		fFoundText = text;
		fFoundStatus = STATUS_OK;
	}
	
	protected void createUnknownToken(final String text) {
		fFoundType = RTerminal.UNKNOWN;
		fFoundText = text;
		fFoundStatus = STATUS_OK;
	}
	
	protected void handleNewLine(final int offset) {
	}
	
}
