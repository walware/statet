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

package de.walware.statet.r.core.rsource;

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_NUMBER_EXP_DIGIT_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_NUMBER_HEX_DIGIT_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_NUMBER_HEX_FLOAT_EXP_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_NUMBER_INT_WITH_DEC_POINT;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_NUMBER_NON_INT_WITH_L;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_HEX_DIGIT_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNKOWN;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_TEXT_NULLCHAR;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS12_SYNTAX_TOKEN_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_MASK_12;

import de.walware.jcommons.string.IStringFactory;

import de.walware.ecommons.ltk.ast.StatusDetail;
import de.walware.ecommons.text.core.input.TextParserInput;

import de.walware.statet.r.core.rlang.RTerminal;


public class RLexer {
	
	
/*[ Config ]===================================================================*/
	
	public static final int SKIP_WHITESPACE=                0b0_0000_0000_0000_0001;
	public static final int SKIP_LINEBREAK=                 0b0_0000_0000_0000_0010;
	public static final int SKIP_COMMENT=                   0b0_0000_0000_0000_0100;
	
	public static final int ENABLE_QUICK_CHECK=             0b0_0000_0000_0001_0000;
	
	public static final int ENABLE_NUM_VALUE=               0b0_0000_0000_0010_0000;
	
	public static final int ENABLE_COLON_EQUAL=             0b0_0000_0001_0000_0000;
	
	public static final int DEFAULT=                        0b0_1000_0000_0000_0000;
	
	private static final int checkConfig(int flags) {
		if ((flags & DEFAULT) != 0) {
			flags|= ENABLE_COLON_EQUAL;
		}
		return flags;
	}
	
/*=============================================================================*/
	
	private static final char[] C1_FALSE= RTerminal.S_FALSE.substring(1).toCharArray();
	private static final char[] C1_NA_real_= RTerminal.S_NA_REAL.substring(1).toCharArray();
	private static final char[] C1_NA_integer_= RTerminal.S_NA_INT.substring(1).toCharArray();
	private static final char[] C1_NA_complex_= RTerminal.S_NA_CPLX.substring(1).toCharArray();
	private static final char[] C1_NA_character_= RTerminal.S_NA_CHAR.substring(1).toCharArray();
	private static final char[] C1_break= RTerminal.S_BREAK.substring(1).toCharArray();
	private static final char[] C1_function= RTerminal.S_FUNCTION.substring(1).toCharArray();
	private static final char[] C1_repeat= RTerminal.S_REPEAT.substring(1).toCharArray();
	private static final char[] C1_while= RTerminal.S_WHILE.substring(1).toCharArray();
	
/*=============================================================================*/
	
	private static StatusDetail createDetail(final TextParserInput in,
			final int beginOffset, final int endOffset, final String text) {
		final int beginIndex= in.getIndex(beginOffset);
		return new StatusDetail(beginIndex, in.getIndex() + in.getLengthInSource(endOffset) - beginIndex,
				text );
	}
	
	private static boolean isLessEqual(final TextParserInput in, final int beginOffset, final String than) {
		for (int i= 0; i < than.length(); i++) {
			if (in.get(beginOffset + i) > than.charAt(i)) {
				return false;
			}
		}
		return true;
	}
	
	
	private TextParserInput input;
	
	private final int configFlags;
	
	private RTerminal foundType;
	private int foundFlags;
	private int foundOffset;
	private int foundNum;
	private int foundLength;
	private StatusDetail foundDetail;
	
	private StringBuilder textBuilder;
	private boolean textBuilderText;
	
	private double numValue;
	
	
	/**
	 * Creates and initializes new lexer
	 */
	public RLexer(final TextParserInput input) {
		this(DEFAULT);
		
		reset(input);
	}
	
	/**
	 * Creates new lexer
	 * 
	 * Use {@link #reset(TextParserInput)} to initialize the lexer
	 */
	public RLexer(final int configFlags) {
		this.configFlags= checkConfig(configFlags);
	}
	
	public RLexer() {
		this(DEFAULT);
	}
	
	
	public void reset() {
		this.foundType= null;
		this.foundFlags= 0;
		this.foundDetail= null;
		this.foundOffset= this.input.getIndex();
		this.foundNum= 0;
		this.foundLength= 0;
		
		this.textBuilderText= false;
	}
	
	public void reset(final TextParserInput input) {
		this.input= input;
		reset();
	}
	
	public final TextParserInput getInput() {
		return this.input;
	}
	
	
	public RTerminal next() {
		this.foundType= null;
		while (this.foundType == null) {
			this.input.consume(this.foundNum);
			this.foundOffset= this.input.getIndex();
			
			searchNext();
		}
		return this.foundType;
	}
	
	public final RTerminal getType() {
		return this.foundType;
	}
	
	public final int getOffset() {
		return this.foundOffset;
	}
	
	public final int getLength() {
		return this.foundLength;
	}
	
	public final String getText() {
		switch (this.foundType) {
		case EOF:
			return null;
		case SPECIAL:
			return this.input.getString(1,
					((this.foundFlags & STATUS_MASK_12) != STATUS12_SYNTAX_TOKEN_NOT_CLOSED) ?
							(this.foundNum - 2) : (this.foundNum - 1) );
		case STRING_S:
		case STRING_D:
		case SYMBOL_G:
			if (this.textBuilderText) {
				return this.textBuilder.toString();
			}
			return this.input.getString(1,
					((this.foundFlags & STATUS_MASK_12) != STATUS12_SYNTAX_TOKEN_NOT_CLOSED) ?
							(this.foundNum - 2) : (this.foundNum - 1) );
		case SYMBOL:
		case NUM_NUM:
		case NUM_INT:
		case NUM_CPLX:
		case UNKNOWN:
			return this.input.getString(0, this.foundNum);
		default:
			return this.foundType.text;
		}
	}
	
	public final String getText(final IStringFactory textFactory) {
		switch (this.foundType) {
		case EOF:
			return null;
		case SPECIAL:
			return this.input.getString(1,
					((this.foundFlags & STATUS_MASK_12) != STATUS12_SYNTAX_TOKEN_NOT_CLOSED) ?
							(this.foundNum - 2) : (this.foundNum - 1),
					textFactory );
		case STRING_S:
		case STRING_D:
		case SYMBOL_G:
			if (this.textBuilderText) {
				return textFactory.get(this.textBuilder);
			}
			return this.input.getString(1,
					((this.foundFlags & STATUS_MASK_12) != STATUS12_SYNTAX_TOKEN_NOT_CLOSED) ?
							(this.foundNum - 2) : (this.foundNum - 1),
					textFactory );
		case SYMBOL:
		case NUM_NUM:
		case NUM_INT:
		case NUM_CPLX:
		case UNKNOWN:
			return this.input.getString(0, this.foundNum, textFactory);
		default:
			return this.foundType.text;
		}
	}
	
	public final int getFlags() {
		return this.foundFlags;
	}
	
	public final StatusDetail getStatusDetail() {
		return this.foundDetail;
	}
	
	
	private void foundEOF(final TextParserInput in) {
		this.foundType= RTerminal.EOF;
		this.foundFlags= 0;
		this.foundDetail= null;
		this.foundLength= in.getLengthInSource(this.foundNum= 0);
	}
	
	private void foundLinebreak(final TextParserInput in, final int n) {
		if ((this.configFlags & SKIP_LINEBREAK) == 0) {
			this.foundType= RTerminal.LINEBREAK;
			this.foundFlags= 0;
			this.foundDetail= null;
		}
		this.foundLength= in.getLengthInSource(this.foundNum= n);
		handleNewLine(this.foundOffset + this.foundLength);
	}
	
	private void foundWhitespace(final TextParserInput in, final int n) {
		if ((this.configFlags & SKIP_WHITESPACE) == 0) {
			this.foundType= RTerminal.BLANK;
			this.foundFlags= 0;
			this.foundDetail= null;
		}
		this.foundLength= in.getLengthInSource(this.foundNum= n);
	}
	
	private void foundComment(final TextParserInput in, final RTerminal type, final int n) {
		if ((this.configFlags & SKIP_COMMENT) == 0) {
			this.foundType= type;
			this.foundFlags= 0;
			this.foundDetail= null;
		}
		this.foundLength= in.getLengthInSource(this.foundNum= n);
	}
	
	private void found1(final TextParserInput in, final RTerminal type) {
		this.foundType= type;
		this.foundFlags= 0;
		this.foundDetail= null;
		this.foundLength= in.getLengthInSource(this.foundNum= 1);
	}
	
	private void found2(final TextParserInput in, final RTerminal type) {
		this.foundType= type;
		this.foundFlags= 0;
		this.foundDetail= null;
		this.foundLength= in.getLengthInSource(this.foundNum= 2);
	}
	
	private void found3(final TextParserInput in, final RTerminal type) {
		this.foundType= type;
		this.foundFlags= 0;
		this.foundDetail= null;
		this.foundLength= in.getLengthInSource(this.foundNum= 3);
	}
	
	private void found(final TextParserInput in, final RTerminal type, final int n) {
		this.foundType= type;
		this.foundFlags= 0;
		this.foundDetail= null;
		this.foundLength= in.getLengthInSource(this.foundNum= n);
	}
	
	private void found(final TextParserInput in, final RTerminal type, final int flags, final int n) {
		this.foundType= type;
		this.foundFlags= flags;
		this.foundDetail= null;
		this.foundLength= in.getLengthInSource(this.foundNum= n);
	}
	
	private void found(final TextParserInput in, final RTerminal type, final int flags, final StatusDetail detail, final int n) {
		this.foundType= type;
		this.foundFlags= flags;
		this.foundDetail= detail;
		this.foundLength= in.getLengthInSource(this.foundNum= n);
	}
	
	
	private void searchNext() {
		final TextParserInput in= this.input;
		int n;
		final int c0= in.get(0);
		C0: switch(c0) {
		case TextParserInput.EOF:
			foundEOF(in);
			return;
		case '\r':
			if (in.get(1) == '\n') {
				foundLinebreak(in, 2);
				return;
			}
			foundLinebreak(in, 1);
			return;
		case '\n':
			foundLinebreak(in, 1);
			return;
		case ' ':
		case '\t':
			n= 1;
			ITER_CN: while (true) {
				switch (in.get(n++)) {
				case ' ':
				case '\t':
					continue ITER_CN;
				default:
					foundWhitespace(in, n - 1);
					return;
				}
			}
		
		case '#':
			if (in.get(1) == '\'') {
				n= 2;
				ITER_CN: while (true) {
					switch (in.get(n++)) {
					case TextParserInput.EOF:
					case '\r':
					case '\n':
						foundComment(in, RTerminal.ROXYGEN_COMMENT, n - 1);
						return;
					default:
						continue ITER_CN;
					}
				}
			}
			n= 1;
			ITER_CN: while (true) {
				switch (in.get(n++)) {
				case TextParserInput.EOF:
				case '\r':
				case '\n':
					foundComment(in, RTerminal.COMMENT, n - 1);
					return;
				default:
					continue ITER_CN;
				}
			}
		
		case '{':
			found1(in, RTerminal.BLOCK_OPEN);
			return;
		case '}':
			found1(in, RTerminal.BLOCK_CLOSE);
			return;
		case '(':
			found1(in, RTerminal.GROUP_OPEN);
			return;
		case ')':
			found1(in, RTerminal.GROUP_CLOSE);
			return;
		case '[':
			if (in.get(1) == '[') {
				found2(in, RTerminal.SUB_INDEXED_D_OPEN);
				return;
			}
			found1(in, RTerminal.SUB_INDEXED_S_OPEN);
			return;
		case ']':
			found1(in, RTerminal.SUB_INDEXED_CLOSE);
			return;
		case ',':
			found1(in, RTerminal.COMMA);
			return;
		case ';':
			found1(in, RTerminal.SEMI);
			return;
		case '$':
			found1(in, RTerminal.SUB_NAMED_PART);
			return;
		case '@':
			found1(in, RTerminal.SUB_NAMED_SLOT);
			return;
		
		case ':':
			switch (in.get(1)) {
			case ':':
				if (in.get(2) == ':') {
					found3(in, RTerminal.NS_GET_INT);
					return;
				}
				found2(in, RTerminal.NS_GET);
				return;
			case '=':
				if ((this.configFlags & ENABLE_COLON_EQUAL) != 0) {
					found2(in, RTerminal.COLON_EQUAL);
					return;
				}
				found1(in, RTerminal.SEQ);
				return;
			default:
				found1(in, RTerminal.SEQ);
				return;
			}
		case '=':
			if (in.get(1) == '=') {
				found2(in, RTerminal.REL_EQ);
				return;
			}
			found1(in, RTerminal.EQUAL);
			return;
		case '<':
			switch (in.get(1)) {
			case '=':
				found2(in, RTerminal.REL_LE);
				return;
			case '-':
				found2(in, RTerminal.ARROW_LEFT_S);
				return;
			case '<':
				if (in.get(2) == '-') {
					found3(in, RTerminal.ARROW_LEFT_D);
					return;
				}
				//$FALL-THROUGH$
			default:
				found1(in, RTerminal.REL_LT);
				return;
			}
		case '>':
			if (in.get(1) == '=') {
				found2(in, RTerminal.REL_GE);
				return;
			}
			found1(in, RTerminal.REL_GT);
			return;
		case '!':
			if (in.get(1) == '=') {
				found2(in, RTerminal.REL_NE);
				return;
			}
			found1(in, RTerminal.NOT);
			return;
		case '&':
			if (in.get(1) == '&') {
				found2(in, RTerminal.AND_D);
				return;
			}
			found1(in, RTerminal.AND);
			return;
		case '|':
			if (in.get(1) == '|') {
				found2(in, RTerminal.OR_D);
				return;
			}
			found1(in, RTerminal.OR);
			return;
		case '+':
			found1(in, RTerminal.PLUS);
			return;
		case '-':
			if (in.get(1) == '>') {
				if (in.get(2) == '>') {
					found3(in, RTerminal.ARROW_RIGHT_D);
					return;
				}
				found2(in, RTerminal.ARROW_RIGHT_S);
				return;
			}
			found1(in, RTerminal.MINUS);
			return;
		case '*':
			found1(in, RTerminal.MULT);
			return;
		case '/':
			found1(in, RTerminal.DIV);
			return;
		case '^':
			found1(in, RTerminal.POWER);
			return;
		case '~':
			found1(in, RTerminal.TILDE);
			return;
		case '%':
			n= 1;
			ITER_CN: while (true) {
				switch (in.get(n++)) {
				case '%':
					found(in, RTerminal.SPECIAL, n);
					return;
				case '\n':
				case '\r':
				case TextParserInput.EOF:
					found(in, RTerminal.SPECIAL, STATUS12_SYNTAX_TOKEN_NOT_CLOSED, n - 1);
					return;
				default:
					continue ITER_CN;
				}
			}
		case '?':
			found1(in, RTerminal.QUESTIONMARK);
			return;
		
		
		case '0':
			if (in.get(1) == 'x') {
				readNumberHex(in);
				return;
			}
			readNumberDec(in);
			return;
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			readNumberDec(in);
			return;
		case '.':
			switch (in.get(1)) {
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
				readNumberDecPoint(in, 2);
				return;
			default:
				n= nIdentifier(in, 1);
				found(in, RTerminal.SYMBOL, n);
				return;
			}
		
		case '\"':
			if ((this.configFlags & ENABLE_QUICK_CHECK) != 0) {
				n= 1;
				ITER_CN: while (true) {
					switch (in.get(n++)) {
					case TextParserInput.EOF:
						found(in, RTerminal.STRING_D, STATUS12_SYNTAX_TOKEN_NOT_CLOSED, n - 1);
						return;
					case '\\':
						if (in.get(n++) == TextParserInput.EOF) {
							found(in, RTerminal.STRING_D, STATUS12_SYNTAX_TOKEN_NOT_CLOSED, n - 1);
							return;
						}
						continue ITER_CN;
					case '\"':
						found(in, RTerminal.STRING_D, n);
						return;
					case '\r':
						if (in.get(n++) != '\n') {
							n--;
						}
						handleNewLine(in.getIndex() + in.getLengthInSource(n));
						continue ITER_CN;
					case '\n':
						handleNewLine(in.getIndex() + in.getLengthInSource(n));
						continue ITER_CN;
					default:
						continue ITER_CN;
					}
				}
			}
			readQuoted(in, RTerminal.STRING_D, '\"');
			return;
		case '\'':
			if ((this.configFlags & ENABLE_QUICK_CHECK) != 0) {
				n= 1;
				ITER_CN: while (true) { // quick check
					switch (in.get(n++)) {
					case TextParserInput.EOF:
						found(in, RTerminal.STRING_S, STATUS12_SYNTAX_TOKEN_NOT_CLOSED, n - 1);
						return;
					case '\\':
						if (in.get(n++) == TextParserInput.EOF) {
							found(in, RTerminal.STRING_S, STATUS12_SYNTAX_TOKEN_NOT_CLOSED, n - 1);
							return;
						}
						continue ITER_CN;
					case '\'':
						found(in, RTerminal.STRING_S, n);
						return;
					case '\r':
						if (in.get(n++) != '\n') {
							n--;
						}
						handleNewLine(in.getIndex() + in.getLengthInSource(n));
						continue ITER_CN;
					case '\n':
						handleNewLine(in.getIndex() + in.getLengthInSource(n));
						continue ITER_CN;
					default:
						continue ITER_CN;
					}
				}
			}
			readQuoted(in, RTerminal.STRING_S, '\'');
			return;
		case '`':
			if ((this.configFlags & ENABLE_QUICK_CHECK) != 0) {
				n= 1;
				ITER_CN: while (true) {
					switch (in.get(n++)) {
					case TextParserInput.EOF:
						found(in, RTerminal.SYMBOL_G, STATUS12_SYNTAX_TOKEN_NOT_CLOSED, n - 1);
						return;
					case '\\':
						if (in.get(n++) == TextParserInput.EOF) {
							found(in, RTerminal.SYMBOL_G, STATUS12_SYNTAX_TOKEN_NOT_CLOSED, n - 1);
							return;
						}
						continue ITER_CN;
					case '`':
						found(in, RTerminal.SYMBOL_G, n);
						return;
					case '\r':
						if (in.get(n++) != '\n') {
							n--;
						}
						handleNewLine(in.getIndex() + in.getLengthInSource(n));
						continue ITER_CN;
					case '\n':
						handleNewLine(in.getIndex() + in.getLengthInSource(n));
						continue ITER_CN;
					default:
						continue ITER_CN;
					}
				}
			}
			readQuoted(in, RTerminal.SYMBOL_G, '`');
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
			n= nIdentifier(in, 1);
			found(in, RTerminal.SYMBOL, n);
			return;
		case 'F':
			n= nIdentifier(in, 1);
			if (n == 5
					&& in.matches(1, C1_FALSE)) {
				found(in, RTerminal.FALSE, 5);
				return;
			}
			found(in, RTerminal.SYMBOL, n);
			return;
		case 'I':
			n= nIdentifier(in, 1);
			if (n == 3
					&& in.matches(1, 'n', 'f')) {
				this.numValue= Double.POSITIVE_INFINITY;
				found(in, RTerminal.INF, 3);
				return;
			}
			found(in, RTerminal.SYMBOL, n);
			return;
		case 'N':
			n= nIdentifier(in, 1);
			switch(n) {
			case 2:
				if (in.get(1) == 'A') {
					found(in, RTerminal.NA, 2);
					return;
				}
				break;
			case 3:
				if (in.matches(1, 'a', 'N')) {
					found(in, RTerminal.NAN, 3);
					return;
				}
				break;
			case 4:
				if (in.matches(1, 'U', 'L', 'L')) {
					found(in, RTerminal.NULL, 4);
					return;
				}
				break;
			case 8:
				if (in.matches(1, C1_NA_real_)) {
					found(in, RTerminal.NA_REAL, 8);
					return;
				}
				break;
			case 11:
				if (in.matches(1, C1_NA_integer_)) {
					found(in, RTerminal.NA_INT, 11);
					return;
				}
				if (in.matches(1, C1_NA_complex_)) {
					found(in, RTerminal.NA_CPLX, 11);
					return;
				}
				break;
			case 13:
				if (in.matches(1, C1_NA_character_)) {
					found(in, RTerminal.NA_CHAR, 13);
					return;
				}
				break;
			default:
				break;
			}
			found(in, RTerminal.SYMBOL, n);
			return;
		case 'T':
			n= nIdentifier(in, 1);
			if (n == 4
					&& in.matches(1, 'R', 'U', 'E')) {
				found(in, RTerminal.TRUE, 4);
				return;
			}
			found(in, RTerminal.SYMBOL, n);
			return;
		case 'b':
			n= nIdentifier(in, 1);
			if (n == 5
					&& in.matches(1, C1_break)) {
				found(in, RTerminal.BREAK, 5);
				return;
			}
			found(in, RTerminal.SYMBOL, n);
			return;
		case 'i':
			n= nIdentifier(in, 1);
			if (n == 2) {
				C1: switch (in.get(1)) {
				case 'f':
					found(in, RTerminal.IF, 2);
					return;
				case 'n':
					found(in, RTerminal.IN, 2);
					return;
				default:
					break C1;
				}
			}
			found(in, RTerminal.SYMBOL, n);
			return;
		case 'e':
			n= nIdentifier(in, 1);
			if (n == 4
					&& in.matches(1, 'l', 's', 'e')) {
				found(in, RTerminal.ELSE, 4);
				return;
			}
			found(in, RTerminal.SYMBOL, n);
			return;
		case 'f':
			n= nIdentifier(in, 1);
			switch (n) {
			case 3:
				if (in.matches(1, 'o', 'r')) {
					found(in, RTerminal.FOR, 3);
					return;
				}
				break;
			case 8:
				if (in.matches(1, C1_function)) {
					found(in, RTerminal.FUNCTION, 8);
					return;
				}
				break;
			default:
				break;
			}
			found(in, RTerminal.SYMBOL, n);
			return;
		case 'n':
			n= nIdentifier(in, 1);
			if (n == 4
					&& in.matches(1, 'e', 'x', 't')) {
				found(in, RTerminal.NEXT, 4);
				return;
			}
			found(in, RTerminal.SYMBOL, n);
			return;
		case 'r':
			n= nIdentifier(in, 1);
			if (n == 6
					&& in.matches(1, C1_repeat)) {
				found(in, RTerminal.REPEAT, 6);
				return;
			}
			found(in, RTerminal.SYMBOL, n);
			return;
		case 'w':
			n= nIdentifier(in, 1);
			if (n == 5
					&& in.matches(1, C1_while)) {
				found(in, RTerminal.WHILE, 5);
				return;
			}
			found(in, RTerminal.SYMBOL, n);
			return;
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
			break C0;
		default:
			if (Character.isLetterOrDigit(c0)) {
				n= nIdentifier(in, 1);
				found(in, RTerminal.SYMBOL, n);
				return;
			}
			break C0;
		}
		
		found1(in, RTerminal.UNKNOWN);
	}
	
	private StringBuilder getTextBuilder() {
		if (this.textBuilder == null) {
			this.textBuilder= new StringBuilder(0x40);
		}
		else {
			this.textBuilder.setLength(0);
		}
		return this.textBuilder;
	}
	
	
	private void readNumberHex(final TextParserInput in) {
		// after: 0x
		int n= 2;
		DIGIT_0: switch (in.get(n++)) {
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
			break DIGIT_0;
		case '.':
			switch (in.get(n++)) {
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
				readNumberHexPoint(in, n);
				return;
			case 'p':
			case 'P':
				readNumberExp(in, n);
				return;
			case 'L':
				found(in, RTerminal.NUM_INT, STATUS123_SYNTAX_NUMBER_HEX_DIGIT_MISSING, n);
				return;
			case 'i':
				found(in, RTerminal.NUM_CPLX, STATUS123_SYNTAX_NUMBER_HEX_DIGIT_MISSING, n);
				return;
			default:
				found(in, RTerminal.NUM_NUM, STATUS123_SYNTAX_NUMBER_HEX_DIGIT_MISSING, n - 1);
				return;
			}
		case 'L':
			found(in, RTerminal.NUM_INT, STATUS123_SYNTAX_NUMBER_HEX_DIGIT_MISSING, n);
			return;
		case 'i':
			found(in, RTerminal.NUM_CPLX, STATUS123_SYNTAX_NUMBER_HEX_DIGIT_MISSING, n);
			return;
		default:
			found(in, RTerminal.NUM_NUM, STATUS123_SYNTAX_NUMBER_HEX_DIGIT_MISSING, n - 1);
			return;
		}
		ITER_CN: while (true) {
			switch (in.get(n++)) {
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
				continue ITER_CN;
			case '.':
				readNumberHexPoint(in, n);
				return;
			case 'p':
			case 'P':
				readNumberExp(in, n);
				return;
			case 'L':
				if (n < 3 + 8 || (n == 3 + 8 && in.get(2) <= '7')) { // 7FFFFFFF
					if ((this.configFlags & ENABLE_NUM_VALUE) != 0) {
						this.numValue= parseHexIntValue(in, 2, n - 1);
					}
					found(in, RTerminal.NUM_INT, n);
					return;
				}
				else {
					if ((this.configFlags & ENABLE_NUM_VALUE) != 0) {
						this.numValue= Double.parseDouble(in.getString(0, n - 1) + "p0"); //$NON-NLS-1$
					}
					found(in, RTerminal.NUM_NUM, STATUS123_SYNTAX_NUMBER_NON_INT_WITH_L, n);
					return;
				}
			case 'i':
				if ((this.configFlags & ENABLE_NUM_VALUE) != 0) {
					this.numValue= (n < 3 + 8 || (n == 3 + 8 && in.get(2) <= '7')) ?
							parseHexIntValue(in, 2, n - 1) :
							Double.parseDouble(in.getString(0, n - 1) + "p0"); //$NON-NLS-1$
				}
				found(in, RTerminal.NUM_CPLX, n);
				return;
			default:
				if ((this.configFlags & ENABLE_NUM_VALUE) != 0) {
					this.numValue= (n < 3 + 8 || (n == 3 + 8 && in.get(2) <= '7')) ?
							parseHexIntValue(in, 2, n - 1) :
							Double.parseDouble(in.getString(0, n - 1) + "p0"); //$NON-NLS-1$
				}
				found(in, RTerminal.NUM_NUM, n - 1);
				return;
			}
		}
	}
	
	private void readNumberDec(final TextParserInput in) {
		// after: [0-9]
		int n= 1;
		ITER_CN: while (true) {
			switch(in.get(n++)) {
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
				continue ITER_CN;
			case '.':
				readNumberDecPoint(in, n);
				return;
			case 'e':
			case 'E':
				readNumberExp(in, n);
				return;
			case 'L':
				if (n < 1 + 10 || (n == 1 + 10 && isLessEqual(in, 0, "2147483647"))) {
					if ((this.configFlags & ENABLE_NUM_VALUE) != 0) {
						this.numValue= parseDecIntValue(in, 0, n - 1);
					}
					found(in, RTerminal.NUM_INT, n);
					return;
				}
				else {
					if ((this.configFlags & ENABLE_NUM_VALUE) != 0) {
						this.numValue= Double.parseDouble(in.getString(0, n - 1));
					}
					found(in, RTerminal.NUM_NUM, STATUS123_SYNTAX_NUMBER_NON_INT_WITH_L, n);
					return;
				}
			case 'i':
				if ((this.configFlags & ENABLE_NUM_VALUE) != 0) {
					this.numValue= (n < 1 + 10) ?
							parseDecIntValue(in, 0, n - 1) :
							Double.parseDouble(in.getString(0, n - 1));
				}
				found(in, RTerminal.NUM_CPLX, n);
				return;
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'F':
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'f':
			default:
				if ((this.configFlags & ENABLE_NUM_VALUE) != 0) {
					this.numValue= (n < 1 + 10) ?
							parseDecIntValue(in, 0, n - 1) :
							Double.parseDouble(in.getString(0, n - 1));
				}
				found(in, RTerminal.NUM_NUM, n - 1);
				return;
			}
		}
	}
	
	private boolean isValidInt(final String s) {
		try {
			final double d= this.numValue= Double.parseDouble(s);
			final int i= (int) d;
			return (d == i);
		}
		catch (final NumberFormatException e) {
			// ?
			this.numValue= Double.NaN;
			return false;
		}
	}
	
	private void readNumberDecPoint(final TextParserInput in, int n) {
		// after: [0-9]+. or .[0-9]
		ITER_CN: while (true) {
			switch(in.get(n++)) {
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
				continue ITER_CN;
			case 'e':
			case 'E':
				readNumberExp(in, n);
				return;
			case 'L':
				if (isValidInt(in.getString(0, n - 1))) {
					found(in, RTerminal.NUM_INT, STATUS123_SYNTAX_NUMBER_INT_WITH_DEC_POINT, n);
					return;
				}
				else {
					found(in, RTerminal.NUM_NUM, STATUS123_SYNTAX_NUMBER_NON_INT_WITH_L, n);
					return;
				}
			case 'i':
				if ((this.configFlags & ENABLE_NUM_VALUE) != 0) {
					this.numValue= Double.parseDouble(in.getString(0, n - 1));
				}
				found(in, RTerminal.NUM_CPLX, n);
				return;
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'F':
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'f':
			default:
				if ((this.configFlags & ENABLE_NUM_VALUE) != 0) {
					this.numValue= Double.parseDouble(in.getString(0, n - 1));
				}
				found(in, RTerminal.NUM_NUM, n - 1);
				return;
			}
		}
	}
	
	private void readNumberHexPoint(final TextParserInput in, int n) {
		// after: [0-9]+. or .[0-9]
		ITER_CN: while (true) {
			switch(in.get(n++)) {
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
				continue ITER_CN;
			case 'p':
			case 'P':
				readNumberExp(in, n);
				return;
			case 'L':
				found(in, RTerminal.NUM_INT, STATUS123_SYNTAX_NUMBER_HEX_FLOAT_EXP_MISSING, n);
				return;
			case 'i':
				found(in, RTerminal.NUM_CPLX, STATUS123_SYNTAX_NUMBER_HEX_FLOAT_EXP_MISSING, n);
				return;
			default:
				found(in, RTerminal.NUM_NUM, STATUS123_SYNTAX_NUMBER_HEX_FLOAT_EXP_MISSING, n - 1);
				return;
			}
		}
	}
	
	private void readNumberExp(final TextParserInput in, int n) {
		// after: e
		int c= in.get(n++);
		SIGN: switch (c) {
		case '+':
		case '-':
			c= in.get(n++);
			break SIGN;
		default:
			break SIGN;
		}
		FIRST_DIGIT: switch (c) {
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
			break FIRST_DIGIT;
		case 'L':
			found(in, RTerminal.NUM_INT, STATUS123_SYNTAX_NUMBER_EXP_DIGIT_MISSING, n);
			return;
		case 'i':
			found(in, RTerminal.NUM_CPLX, STATUS123_SYNTAX_NUMBER_EXP_DIGIT_MISSING, n);
			return;
		default:
			found(in, RTerminal.NUM_NUM, STATUS123_SYNTAX_NUMBER_EXP_DIGIT_MISSING, n - 1);
			return;
		}
		ITER_CN: while (true) {
			switch(in.get(n++)) {
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
				continue ITER_CN;
			case 'L':
				if (isValidInt(in.getString(0, n - 1))) {
					found(in, RTerminal.NUM_INT, n);
					return;
				}
				else {
					found(in, RTerminal.NUM_NUM, STATUS123_SYNTAX_NUMBER_NON_INT_WITH_L, n);
					return;
				}
			case 'i':
				if ((this.configFlags & ENABLE_NUM_VALUE) != 0) {
					this.numValue= Double.parseDouble(in.getString(0, n - 1));
				}
				found(in, RTerminal.NUM_CPLX, n);
				return;
			default:
				if ((this.configFlags & ENABLE_NUM_VALUE) != 0) {
					this.numValue= Double.parseDouble(in.getString(0, n - 1));
				}
				found(in, RTerminal.NUM_NUM, n - 1);
				return;
			}
		}
	}
	
	
	private int nIdentifier(final TextParserInput in, int n) {
		// after legal start
		ITER_CN: while (true) {
			final int next= in.get(n++);
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
			case '_':
				continue ITER_CN;
			case TextParserInput.EOF:
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
//			case 0x030: 0-9
			case 0x03A:
			case 0x03B:
			case 0x03C:
			case 0x03D:
			case 0x03E:
			case 0x03F:
			case 0x040:
//			case 0x041: A-Z
			case 0x05B:
			case 0x05C:
			case 0x05D:
			case 0x05E:
//			case 0x05F: _
			case 0x060:
//			case 0x061: a-z
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
				return n - 1;
			default:
				if (Character.isLetterOrDigit(next)) {
					continue ITER_CN;
				}
				return n - 1;
			}
		}
	}
	
	
	private void readQuoted(final TextParserInput in, final RTerminal type, final char cQuote) {
		final StringBuilder text= getTextBuilder();
		// after: ['"]
		int n= 1;
		int textOffset= 1;
		ITER_CN: while (true) {
			final int cn;
			switch (cn= in.get(n++)) {
			case '\\':
				switch (in.get(n)) {
				case TextParserInput.EOF:
					continue ITER_CN;
				case '\'':
				case '\"':
				case '`':
				case '\\':
					in.appendTo(textOffset, n - textOffset - 1, text);
					textOffset= n++; 
					continue ITER_CN;
				case '\n':
					in.appendTo(textOffset, n - textOffset - 1, text);
					textOffset= n++;
					handleNewLine(in.getIndex() + in.getLengthInSource(n));
					continue ITER_CN;
				case 'a':
					in.appendTo(textOffset, n - textOffset - 1, text);
					text.append('\u0007');
					textOffset= ++n;
					continue ITER_CN;
				case 'b':
					in.appendTo(textOffset, n - textOffset - 1, text);
					text.append('\u0008');
					textOffset= ++n;
					continue ITER_CN;
				case 'f':
					in.appendTo(textOffset, n - textOffset - 1, text);
					text.append('\u000C');
					textOffset= ++n;
					continue ITER_CN;
				case 'n':
					in.appendTo(textOffset, n - textOffset - 1, text);
					text.append('\n');
					textOffset= ++n;
					continue ITER_CN;
				case 'r':
					in.appendTo(textOffset, n - textOffset - 1, text);
					text.append('\r');
					textOffset= ++n;
					continue ITER_CN;
				case 't':
					in.appendTo(textOffset, n - textOffset - 1, text);
					text.append('\t');
					textOffset= ++n;
					continue ITER_CN;
				case 'v':
					in.appendTo(textOffset, n - textOffset - 1, text);
					text.append('\u000B');
					textOffset= ++n;
					continue ITER_CN;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7': {
					int digits= 1;
					ITER_DIGITS: while (digits < 3) {
						switch (in.get(n + digits)) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
							digits++;
							continue ITER_DIGITS;
						default:
							break ITER_DIGITS;
						}
					}
					final long c= (char) Long.parseLong(in.getString(n, digits), 8);
					if (c == 0) {
						readQuoted(in, type, cQuote, n + digits,
								STATUS123_SYNTAX_TEXT_NULLCHAR,
								createDetail(in, n - 1, n + digits, null) );
						return;
					}
					else {
						in.appendTo(textOffset, n - textOffset - 1, text);
						text.append((char) c);
						textOffset= (n+= digits);
						continue ITER_CN;
					}
				}
				case 'x': {
					int digits= 0;
					ITER_DIGITS: while (digits < 2) {
						switch (in.get(n + digits + 1)) {
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
							digits++;
							continue ITER_DIGITS;
						default:
							break ITER_DIGITS;
						}
					}
					if (digits == 0) {
						readQuoted(in, type, cQuote, n + digits + 1,
								STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_HEX_DIGIT_MISSING,
								createDetail(in, n - 1, n + digits + 1, "\\x" ) );
						return;
					}
					final int c= (int) Long.parseLong(in.getString(n + 1, digits), 16);
					if (c == 0) {
						readQuoted(in, type, cQuote, n + digits + 1,
								STATUS123_SYNTAX_TEXT_NULLCHAR,
								createDetail(in, n - 1, n + digits + 1, null) );
						return;
					}
					else {
						in.appendTo(textOffset, n - textOffset - 1, text);
						text.append((char) c);
						textOffset= (n+= digits + 1);
						continue ITER_CN;
					}
				}
				case 'u':
					if (in.get(n + 1) == '{') {
						int digits= 0;
						ITER_DIGITS: while (digits < 4) {
							switch (in.get(n + digits + 2)) {
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
								digits++;
								continue ITER_DIGITS;
							default:
								break ITER_DIGITS;
							}
						}
						if (type == RTerminal.SYMBOL_G) {
							if (in.get(n + digits + 2) != '}') {
								readQuoted(in, type, cQuote, n + digits + 2,
										IRSourceConstants.STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED,
										createDetail(in, n - 1, n + digits + 2, "\\u{xxxx}") );
								return;
							}
							else {
								readQuoted(in, type, cQuote, n + digits + 3,
										IRSourceConstants.STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED,
										createDetail(in, n - 1, n + digits + 3, "\\u{xxxx}") );
								return;
							}
						}
						if (in.get(n + digits + 2) != '}') {
							readQuoted(in, type, cQuote, n + digits + 2,
									STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_NOT_CLOSED,
									createDetail(in, n - 1, n + digits + 2, "\\u{xxxx}" ));
							return;
						}
						if (digits == 0) {
							readQuoted(in, type, cQuote, n + digits + 3,
									STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_HEX_DIGIT_MISSING,
									createDetail(in, n - 1, n + digits + 3, "\\u{xxxx}" ));
							return;
						}
						final int c= (int) Long.parseLong(in.getString(n + 2, digits), 16);
						if (c == 0) {
							readQuoted(in, type, cQuote, n + digits + 3,
									STATUS123_SYNTAX_TEXT_NULLCHAR,
									createDetail(in, n - 1, n + digits + 3, null ));
							return;
						}
						else {
							in.appendTo(textOffset, n - textOffset - 1, text);
							text.append((char) c);
							textOffset= (n+= digits + 3);
							continue ITER_CN;
						}
					}
					else {
						int digits= 0;
						ITER_DIGITS: while (digits < 4) {
							switch (in.get(n + digits + 1)) {
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
								digits++;
								continue ITER_DIGITS;
							default:
								break ITER_DIGITS;
							}
						}
						if (type == RTerminal.SYMBOL_G) {
							readQuoted(in, type, cQuote, n + digits + 1,
									IRSourceConstants.STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED,
									createDetail(in, n - 1, n + digits + 1, "\\uxxxx") );
							return;
						}
						if (digits == 0) {
							readQuoted(in, type, cQuote, n + digits + 1,
									STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_HEX_DIGIT_MISSING,
									createDetail(in, n - 1, n + digits + 1, "\\uxxxx") );
							return;
						}
						final int c= (int) Long.parseLong(in.getString(n + 1, digits), 16);
						if (c == 0) {
							readQuoted(in, type, cQuote, n + digits + 1,
									STATUS123_SYNTAX_TEXT_NULLCHAR,
									createDetail(in, n - 1, n + digits + 1, null) );
							return;
						}
						else {
							in.appendTo(textOffset, n - textOffset - 1, text);
							text.append((char) c);
							textOffset= (n+= digits + 1);
							continue ITER_CN;
						}
					}
				case 'U':
					if (in.get(n + 1) == '{') {
						int digits= 0;
						ITER_DIGITS: while (digits < 8) {
							switch (in.get(n + digits + 2)) {
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
								digits++;
								continue ITER_DIGITS;
							default:
								break ITER_DIGITS;
							}
						}
						if (type == RTerminal.SYMBOL_G) {
							if (in.get(n + digits + 2) != '}') {
								readQuoted(in, type, cQuote, n + digits + 2,
										IRSourceConstants.STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED,
										createDetail(in, n - 1, n + digits + 2, "\\U{xxxxxxxx}") );
								return;
							}
							else {
								readQuoted(in, type, cQuote, n + digits + 3,
										IRSourceConstants.STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED,
										createDetail(in, n - 1, n + digits + 3, "\\U{xxxxxxxx}") );
								return;
							}
						}
						if (in.get(n + digits + 2) != '}') {
							readQuoted(in, type, cQuote, n + digits + 2,
									STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_NOT_CLOSED,
									createDetail(in, n - 1, n + digits + 2, "\\U{xxxxxxxx}") );
							return;
						}
						if (digits == 0) {
							readQuoted(in, type, cQuote, n + digits + 3,
									STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_HEX_DIGIT_MISSING,
									createDetail(in, n - 1 , n + digits + 3, "\\U{xxxxxxxx}") );
							return;
						}
						final int c= (int) Long.parseLong(in.getString(n + 2, digits), 16);
						if (c == 0) {
							readQuoted(in, type, cQuote, n + digits + 3,
									STATUS123_SYNTAX_TEXT_NULLCHAR,
									createDetail(in, n - 1, n + digits + 3, null) );
							return;
						}
						else if (Character.isBmpCodePoint(c)) {
							in.appendTo(textOffset, n - textOffset - 1, text);
							text.append((char) c);
							textOffset= (n+= digits + 3);
							continue ITER_CN;
						}
						else if (Character.isValidCodePoint(c)) {
							in.appendTo(textOffset, n - textOffset - 1, text);
							text.append(Character.highSurrogate(c)); 
							text.append(Character.lowSurrogate(c));
							textOffset= (n+= digits + 3);
							continue ITER_CN;
						}
						else {
							// warning?
							n+= digits + 3;
							continue ITER_CN;
						}
					}
					else {
						int digits= 0;
						ITER_DIGITS: while (digits < 8) {
							switch (in.get(n + digits + 1)) {
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
								digits++;
								continue ITER_DIGITS;
							default:
								break ITER_DIGITS;
							}
						}
						if (type == RTerminal.SYMBOL_G) {
							readQuoted(in, type, cQuote, n + digits + 1,
									IRSourceConstants.STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED,
									createDetail(in, n - 1, n + digits + 1, "\\Uxxxxxxxx") );
							return;
						}
						if (digits == 0) {
							readQuoted(in, type, cQuote, n + digits + 1,
									STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_HEX_DIGIT_MISSING,
									createDetail(in, n - 1, n + digits + 1, "\\Uxxxxxxxx") );
							return;
						}
						final int c= (int) Long.parseLong(in.getString(n + 1, digits), 16);
						if (c == 0) {
							readQuoted(in, type, cQuote, n + digits + 1,
									STATUS123_SYNTAX_TEXT_NULLCHAR,
									createDetail(in, n - 1, n + digits + 1, null) );
							return;
						}
						else if (Character.isBmpCodePoint(c)) {
							in.appendTo(textOffset, n - textOffset - 1, text);
							text.append((char) c);
							textOffset= (n+= digits + 1);
							continue ITER_CN;
						}
						else if (Character.isValidCodePoint(c)) {
							in.appendTo(textOffset, n - textOffset - 1, text);
							text.append(Character.highSurrogate(c)); 
							text.append(Character.lowSurrogate(c));
							textOffset= (n+= digits + 1);
							continue ITER_CN;
						}
						else {
							// warning?
							n+= digits + 1;
							continue ITER_CN;
						}
					}
					
				default:
					readQuoted(in, type, cQuote, n,
							STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNKOWN,
							createDetail(in, n - 1, n + 1, in.getString(n - 1, 2)) );
					return;
				}
			case TextParserInput.EOF:
				if (textOffset > 1) {
					in.appendTo(textOffset, n - textOffset - 1, text);
					this.textBuilderText= true;
				}
				else {
					this.textBuilderText= false;
				}
				found(in, type, STATUS12_SYNTAX_TOKEN_NOT_CLOSED, n - 1);
				return;
			case '\r':
				if (in.get(n++) != '\n') {
					n--;
				}
				handleNewLine(in.getIndex() + in.getLengthInSource(n));
				continue ITER_CN;
			case '\n':
				handleNewLine(in.getIndex() + in.getLengthInSource(n));
				continue ITER_CN;
			case 0:
				readQuoted(in, type, cQuote, n,
						STATUS123_SYNTAX_TEXT_NULLCHAR,
						createDetail(in, n - 1, n, null) );
				return;
			default:
				if (cn == cQuote) {
					if (textOffset > 1) {
						in.appendTo(textOffset, n - textOffset - 1, text);
						this.textBuilderText= true;
					}
					else {
						this.textBuilderText= false;
					}
					found(in, type, n);
					return;
				}
				continue ITER_CN;
			}
		}
	}
	
	private void readQuoted(final TextParserInput in, final RTerminal type, final char cQuote,
			int n, final int status, final StatusDetail detail) {
		this.textBuilderText= false;
		ITER_CN: while (true) {
			final int cn;
			switch (cn= in.get(n++)) {
			case TextParserInput.EOF:
				found(in, type, STATUS12_SYNTAX_TOKEN_NOT_CLOSED, n - 1);
				return;
			case '\\':
				if (in.get(n++) == TextParserInput.EOF) {
					found(in, type, STATUS12_SYNTAX_TOKEN_NOT_CLOSED, n - 1);
					return;
				}
				continue ITER_CN;
			case '\r':
				if (in.get(n++) != '\n') {
					n--;
				}
				handleNewLine(in.getIndex() + in.getLengthInSource(n));
				continue ITER_CN;
			case '\n':
				handleNewLine(in.getIndex() + in.getLengthInSource(n));
				continue ITER_CN;
			default:
				if (cn == cQuote) {
					found(in, type, status, detail, n);
					return;
				}
				continue ITER_CN;
			}
		}
	}
	
	
	protected void handleNewLine(final int offset) {
	}
	
	
	private int decDigit(final int c) {
		return c - '0';
	}
	
	private int hexDigit(final int c) {
		switch (c) {
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
			return c - '0';
		case 'A':
		case 'B':
		case 'C':
		case 'D':
		case 'E':
		case 'F':
			return c - ('A' - 10);
		case 'a':
		case 'b':
		case 'c':
		case 'd':
		case 'e':
		case 'f':
			return c - ('a' - 10);
		default:
			throw new IllegalStateException();
		}
	}
	
	private int parseDecIntValue(final TextParserInput in, int i, final int n) {
		int result= 0;
		while (i < n) {
			result*= 10;
			result+= decDigit(in.get(i++));
		}
		return result;
	}
	
	private int parseHexIntValue(final TextParserInput in, int i, final int n) {
		int result= 0;
		while (i < n) {
			result*= 16;
			result+= hexDigit(in.get(i++));
		}
		return result;
	}
	
	public double getNumValue() {
		return this.numValue;
	}
	
}
