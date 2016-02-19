/*=============================================================================#
 # Copyright (c) 2015-2016 Stephan Wahlbrink (WalWare.de) and others.
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
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS12_SYNTAX_TOKEN_NOT_CLOSED;
import static org.junit.Assert.assertEquals;

import org.junit.FixMethodOrder;
import org.junit.Test;

import de.walware.ecommons.text.core.input.StringParserInput;

import de.walware.statet.r.core.rlang.RTerminal;


@FixMethodOrder
public class RLexerTerminalTest {
	
	
	private final StringParserInput input= new StringParserInput();
	
	
	protected int getConfig() {
		return 0;
	}
	
	
	@Test
	public void empty() {
		final RLexer lexer= new RLexer(getConfig());
		lexer.reset(this.input.reset("").init());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchWhitespace() {
		final RLexer lexer= new RLexer(getConfig());
		lexer.reset(this.input.reset(" \t").init());
		
		assertEquals(RTerminal.BLANK, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void skipWhitespace() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset("  \t").init());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchLinebreak() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" \n ").init());
		
		assertEquals(RTerminal.LINEBREAK, lexer.next());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchComment() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset("  # comment").init());
		
		assertEquals(RTerminal.COMMENT, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchComment_Linebreak() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset("  # comment\n  ").init());
		
		assertEquals(RTerminal.COMMENT, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals(RTerminal.LINEBREAK, lexer.next());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchRoxygenComment() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset("#' comment").init());
		
		assertEquals(RTerminal.ROXYGEN_COMMENT, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void skipComment() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE | RLexer.SKIP_COMMENT);
		lexer.reset(this.input.reset("  # comment\n  ").init());
		
		assertEquals(RTerminal.LINEBREAK, lexer.next());
		assertEquals(RTerminal.EOF, lexer.next());
		
		lexer.reset(this.input.reset("  #' comment").init());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	
	@Test
	public void matchBlockOpen() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" { ").init());
		
		assertEquals(RTerminal.BLOCK_OPEN, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchBlockClose() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" } ").init());
		
		assertEquals(RTerminal.BLOCK_CLOSE, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchGroupOpen() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" ( ").init());
		
		assertEquals(RTerminal.GROUP_OPEN, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchGroupClose() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" ) ").init());
		
		assertEquals(RTerminal.GROUP_CLOSE, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSubIndexedSOpen() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" [ ").init());
		
		assertEquals(RTerminal.SUB_INDEXED_S_OPEN, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSubIndexedDOpen() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" [[ ").init());
		
		assertEquals(RTerminal.SUB_INDEXED_D_OPEN, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSubIndexedClose() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" ] ").init());
		
		assertEquals(RTerminal.SUB_INDEXED_CLOSE, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSubNamedPart() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" $ ").init());
		
		assertEquals(RTerminal.SUB_NAMED_PART, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSubSlotPart() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" @ ").init());
		
		assertEquals(RTerminal.SUB_NAMED_SLOT, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNsGet() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" :: ").init());
		
		assertEquals(RTerminal.NS_GET, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNsGetInt() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" ::: ").init());
		
		assertEquals(RTerminal.NS_GET_INT, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchPlus() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" + ").init());
		
		assertEquals(RTerminal.PLUS, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchMinus() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" - ").init());
		
		assertEquals(RTerminal.MINUS, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchMult() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" * ").init());
		
		assertEquals(RTerminal.MULT, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchDiv() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" / ").init());
		
		assertEquals(RTerminal.DIV, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchOr() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" | ").init());
		
		assertEquals(RTerminal.OR, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchOrD() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" || ").init());
		
		assertEquals(RTerminal.OR_D, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchAnd() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" & ").init());
		
		assertEquals(RTerminal.AND, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchAndD() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" && ").init());
		
		assertEquals(RTerminal.AND_D, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNot() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" ! ").init());
		
		assertEquals(RTerminal.NOT, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchPower() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" ^ ").init());
		
		assertEquals(RTerminal.POWER, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSeq() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" : ").init());
		
		assertEquals(RTerminal.SEQ, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSpecial() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" %/% ").init());
		
		assertEquals(RTerminal.SPECIAL, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("/", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSpecial_empty() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" %% ").init());
		
		assertEquals(RTerminal.SPECIAL, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSpecial_notClosed() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" %/ ").init());
		
		assertEquals(RTerminal.SPECIAL, lexer.next());
		assertEquals(STATUS12_SYNTAX_TOKEN_NOT_CLOSED, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
		
		lexer.reset(this.input.reset(" %/ \n%").init());
		
		assertEquals(RTerminal.SPECIAL, lexer.next());
		assertEquals(STATUS12_SYNTAX_TOKEN_NOT_CLOSED, lexer.getFlags());
		
		assertEquals(RTerminal.LINEBREAK, lexer.next());
	}
	
	@Test
	public void matchQuestionmark() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" ? ").init());
		
		assertEquals(RTerminal.QUESTIONMARK, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchComma() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" , ").init());
		
		assertEquals(RTerminal.COMMA, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSemi() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" ; ").init());
		
		assertEquals(RTerminal.SEMI, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchArrowLeftS() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" <- ").init());
		
		assertEquals(RTerminal.ARROW_LEFT_S, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchArrowLeftD() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" <<- ").init());
		
		assertEquals(RTerminal.ARROW_LEFT_D, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchArrowRightS() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" -> ").init());
		
		assertEquals(RTerminal.ARROW_RIGHT_S, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchArrowRightD() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" ->> ").init());
		
		assertEquals(RTerminal.ARROW_RIGHT_D, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchEqual() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" = ").init());
		
		assertEquals(RTerminal.EQUAL, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchColonEqual() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE | RLexer.ENABLE_COLON_EQUAL);
		lexer.reset(this.input.reset(" := ").init());
		
		assertEquals(RTerminal.COLON_EQUAL, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchColonEqual_disabled() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" := ").init());
		
		assertEquals(RTerminal.SEQ, lexer.next());
		assertEquals(RTerminal.EQUAL, lexer.next());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchTilde() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" ~ ").init());
		
		assertEquals(RTerminal.TILDE, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchRelNE() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" != ").init());
		
		assertEquals(RTerminal.REL_NE, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchRelEQ() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" == ").init());
		
		assertEquals(RTerminal.REL_EQ, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchRelLT() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" < ").init());
		
		assertEquals(RTerminal.REL_LT, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchRelLE() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" <= ").init());
		
		assertEquals(RTerminal.REL_LE, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchRelGT() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" > ").init());
		
		assertEquals(RTerminal.REL_GT, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchRelGE() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" >= ").init());
		
		assertEquals(RTerminal.REL_GE, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	
	@Test
	public void matchKey_if() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" if ").init());
		
		assertEquals(RTerminal.IF, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchKey_else() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" else ").init());
		
		assertEquals(RTerminal.ELSE, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchKey_for() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" for ").init());
		
		assertEquals(RTerminal.FOR, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchKey_in() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" in ").init());
		
		assertEquals(RTerminal.IN, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchKey_while() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" while ").init());
		
		assertEquals(RTerminal.WHILE, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchKey_repeat() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" repeat ").init());
		
		assertEquals(RTerminal.REPEAT, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchKey_next() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" next ").init());
		
		assertEquals(RTerminal.NEXT, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchKey_break() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" break ").init());
		
		assertEquals(RTerminal.BREAK, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchKey_function() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" function ").init());
		
		assertEquals(RTerminal.FUNCTION, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchKey_TRUE() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" TRUE ").init());
		
		assertEquals(RTerminal.TRUE, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchKey_FALSE() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" FALSE ").init());
		
		assertEquals(RTerminal.FALSE, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchKey_NA() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" NA ").init());
		
		assertEquals(RTerminal.NA, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchKey_NA_real() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" NA_real_ ").init());
		
		assertEquals(RTerminal.NA_REAL, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchKey_NA_integer() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" NA_integer_ ").init());
		
		assertEquals(RTerminal.NA_INT, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchKey_NA_complex() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" NA_complex_ ").init());
		
		assertEquals(RTerminal.NA_CPLX, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchKey_NA_character() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" NA_character_ ").init());
		
		assertEquals(RTerminal.NA_CHAR, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchKey_NULL() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" NULL ").init());
		
		assertEquals(RTerminal.NULL, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchKey_NaN() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" NaN ").init());
		
		assertEquals(RTerminal.NAN, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchKey_Inf() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" Inf ").init());
		
		assertEquals(RTerminal.INF, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	
	@Test
	public void matchStringS() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 'abc' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringS_notClosed() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 'abc ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(STATUS12_SYNTAX_TOKEN_NOT_CLOSED, lexer.getFlags());
		assertEquals("abc ", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringS_with_Linebreak() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 'abc\nefg' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc\nefg", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringD() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" \"abc\" ").init());
		
		assertEquals(RTerminal.STRING_D, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringD_notClosed() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" \"abc ").init());
		
		assertEquals(RTerminal.STRING_D, lexer.next());
		assertEquals(STATUS12_SYNTAX_TOKEN_NOT_CLOSED, lexer.getFlags());
		assertEquals("abc ", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringD_with_Linebreak() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" \"abc\nefg\" ").init());
		
		assertEquals(RTerminal.STRING_D, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc\nefg", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	
	@Test
	public void matchSymbol() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" abc ").init());
		
		assertEquals(RTerminal.SYMBOL, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbol_Dot() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" . ").init());
		
		assertEquals(RTerminal.SYMBOL, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals(".", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbol_contains_Dot() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" abc.efg ").init());
		
		assertEquals(RTerminal.SYMBOL, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbol_Ellipsis() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" ... ").init());
		
		assertEquals(RTerminal.SYMBOL, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("...", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbol_contains_Dots() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" abc...efg ").init());
		
		assertEquals(RTerminal.SYMBOL, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc...efg", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbol_beginsWith_Dots() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" ...abc ").init());
		
		assertEquals(RTerminal.SYMBOL, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("...abc", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbol_endsWith_Dots() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" abc... ").init());
		
		assertEquals(RTerminal.SYMBOL, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc...", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbol_beginWith_Underscore() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" _abc ").init());
		
		assertEquals(RTerminal.UNKNOWN, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals(RTerminal.SYMBOL, lexer.next());
		assertEquals("abc", lexer.getText());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbol_contains_Underscore() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" abc_efg ").init());
		
		assertEquals(RTerminal.SYMBOL, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc_efg", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbol_contains_Underscores() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" abc___efg ").init());
		
		assertEquals(RTerminal.SYMBOL, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc___efg", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbol_endsWith_Underscores() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" abc___ ").init());
		
		assertEquals(RTerminal.SYMBOL, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc___", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbolG() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" `abc` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbolG_notClosed() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" `abc ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(STATUS12_SYNTAX_TOKEN_NOT_CLOSED, lexer.getFlags());
		assertEquals("abc ", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbolG_with_Linebreak() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" `abc\nefg` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc\nefg", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	
	@Test
	public void matchNumReal_Dec() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 01234567890 ").init());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("01234567890", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumReal_Dec_1_0() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 1 2 3 4 5 6 7 8 9 0 ").init());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("1", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("2", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("3", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("4", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("5", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("6", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("7", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("8", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("9", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumReal_Dec_with_Point() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 123.456 ").init());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("123.456", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumReal_Dec_startsWith_Point() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" .123 ").init());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals(".123", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumReal_Dec_endsWith_Point() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 123. ").init());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("123.", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumReal_Dec_withExp() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 123.4e0 123.e10 ").init());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("123.4e0", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("123.e10", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumReal_Dec_withExp_withSign() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 123.4e+1 123.4e-1 ").init());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("123.4e+1", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("123.4e-1", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumReal_Dec_withExp_missingExpDigit() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 123.4e 123.4e+ 123e- ").init());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_EXP_DIGIT_MISSING, lexer.getFlags());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_EXP_DIGIT_MISSING, lexer.getFlags());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_EXP_DIGIT_MISSING, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumReal_Hex() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0x01234567890 0xabcdef 0xABCDEF ").init());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x01234567890", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0xabcdef", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0xABCDEF", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumReal_Hex_0_F() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0x0 0x1 0x2 0x3 0x4 0x5 0x6 0x7 0x8 0x9 0xA 0xB 0xC 0xD 0xE 0xF 0xa 0xb 0xc 0xd 0xe 0xf ").init());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x0", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x1", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x2", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x3", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x4", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x5", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x6", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x7", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x8", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x9", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0xA", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0xB", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0xC", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0xD", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0xE", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0xF", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0xa", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0xb", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0xc", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0xd", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0xe", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0xf", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumReal_Hex_missingDigit() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0x 0xp ").init());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_HEX_DIGIT_MISSING, lexer.getFlags());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_HEX_DIGIT_MISSING, lexer.getFlags());
	}
	
	@Test
	public void matchNumReal_HexFloat_withoutPoint() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0x123ABCp10 0x0FFp01 ").init());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x123ABCp10", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x0FFp01", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumReal_HexFloat_with_Point() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0x123.456p0 0x.ABCDEFp10 0x123.456 0xA.A ").init());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x123.456p0", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x.ABCDEFp10", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_HEX_FLOAT_EXP_MISSING, lexer.getFlags());
		assertEquals("0x123.456", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_HEX_FLOAT_EXP_MISSING, lexer.getFlags());
		assertEquals("0xA.A", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumReal_HexFloat_startsWith_Point() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0x.123p0 0x.123 ").init());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x.123p0", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_HEX_FLOAT_EXP_MISSING, lexer.getFlags());
		assertEquals("0x.123", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumReal_HexFloat_endsWith_Point() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0x123.p0 0x123. ").init());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x123.p0", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_HEX_FLOAT_EXP_MISSING, lexer.getFlags());
		assertEquals("0x123.", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumReal_HexFloat_withExpSign() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0x123.4p+10 0x123.4p-01 ").init());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x123.4p+10", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x123.4p-01", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumReal_HexFloat_missingExpDigit() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0x123.4p 0x123.4p+ 0x123p- 0x123.4pA ").init());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_EXP_DIGIT_MISSING, lexer.getFlags());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_EXP_DIGIT_MISSING, lexer.getFlags());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_EXP_DIGIT_MISSING, lexer.getFlags());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_EXP_DIGIT_MISSING, lexer.getFlags());
		
		assertEquals(RTerminal.SYMBOL, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("A", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	
	@Test
	public void matchNumInt_Dec() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 123L ").init());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("123L", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumInt_Dec_check() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 2147483647L 2147483648L ").init());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_NON_INT_WITH_L, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumInt_Dec_with_Point() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 2147483647.0L .1L ").init());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_INT_WITH_DEC_POINT, lexer.getFlags());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_NON_INT_WITH_L, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumInt_Dec_withExp() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 1e9L 1.5e9L ").init());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("1e9L", lexer.getText());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("1.5e9L", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumInt_Dec_withExpSign() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0.5e+9L 100e-1L ").init());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0.5e+9L", lexer.getText());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("100e-1L", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumInt_Dec_withExp_check() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 2147483647e0L 2147483647.0e0L 2147483648.0e0L 1e10L ").init());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_NON_INT_WITH_L, lexer.getFlags());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_NON_INT_WITH_L, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumInt_Dec_missingExpDigit() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 123.4eL 123e+L 123e-L ").init());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_EXP_DIGIT_MISSING, lexer.getFlags());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_EXP_DIGIT_MISSING, lexer.getFlags());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_EXP_DIGIT_MISSING, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumInt_Hex() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0x0123ABCL ").init());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x0123ABCL", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumInt_Hex_check() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0x7FFFFFFFL 0x8FFFFFFFL 0xfffffffffffffffffffffffffffffffffffffffL ").init());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(0, lexer.getFlags());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_NON_INT_WITH_L, lexer.getFlags());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_NON_INT_WITH_L, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumInt_Hex_missingDigit() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0xL ").init());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_HEX_DIGIT_MISSING, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumInt_HexFloat() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0x123p0L 0x.Ap10L 0x123.456L 0xA.AL ").init());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x123p0L", lexer.getText());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x.Ap10L", lexer.getText());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_HEX_FLOAT_EXP_MISSING, lexer.getFlags());
		assertEquals("0x123.456L", lexer.getText());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_HEX_FLOAT_EXP_MISSING, lexer.getFlags());
		assertEquals("0xA.AL", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumInt_HexFloat_check() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0x123.4p0L 0x.ABCEp10L ").init());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_NON_INT_WITH_L, lexer.getFlags());
		assertEquals("0x123.4p0L", lexer.getText());
		
		assertEquals(RTerminal.NUM_NUM, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_NON_INT_WITH_L, lexer.getFlags());
		assertEquals("0x.ABCEp10L", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumInt_HexFloat_withExpSign() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0x123.4p+10L 0x1200p-01L ").init());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x123.4p+10L", lexer.getText());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x1200p-01L", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumInt_HexFloat_missingExpDigit() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0x123.4pL 0x123.4p+L 0x123p-L ").init());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_EXP_DIGIT_MISSING, lexer.getFlags());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_EXP_DIGIT_MISSING, lexer.getFlags());
		
		assertEquals(RTerminal.NUM_INT, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_EXP_DIGIT_MISSING, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	
	@Test
	public void matchNumCplx() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 123i ").init());
		
		assertEquals(RTerminal.NUM_CPLX, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("123i", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumCplx_Dec() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 123.456i .456i 123.i").init());
		
		assertEquals(RTerminal.NUM_CPLX, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("123.456i", lexer.getText());
		
		assertEquals(RTerminal.NUM_CPLX, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals(".456i", lexer.getText());
		
		assertEquals(RTerminal.NUM_CPLX, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("123.i", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumCplx_Hex() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0x0123ABCi ").init());
		
		assertEquals(RTerminal.NUM_CPLX, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x0123ABCi", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumCplx_Hex_missingDigit() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0xi ").init());
		
		assertEquals(RTerminal.NUM_CPLX, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_HEX_DIGIT_MISSING, lexer.getFlags());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumCplx_HexFloat() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0x0123A.BCp0i ").init());
		
		assertEquals(RTerminal.NUM_CPLX, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("0x0123A.BCp0i", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchNumCplx_HexFloat_missingExp() {
		final RLexer lexer= new RLexer(getConfig() | RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 0x0123A.BCi ").init());
		
		assertEquals(RTerminal.NUM_CPLX, lexer.next());
		assertEquals(STATUS123_SYNTAX_NUMBER_HEX_FLOAT_EXP_MISSING, lexer.getFlags());
		assertEquals("0x0123A.BCi", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
}
