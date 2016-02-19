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

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_HEX_DIGIT_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNKOWN;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_TEXT_NULLCHAR;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.walware.ecommons.ltk.ast.StatusDetail;
import de.walware.ecommons.text.core.input.StringParserInput;

import de.walware.statet.r.core.rlang.RTerminal;


public class RLexerTextEncodingTest {
	
	
	private static void assertDetail(final int expectedOffset, final int expectedLength,
			final String expectedText, final StatusDetail actual) {
		assertEquals(expectedOffset, actual.getOffset());
		assertEquals(expectedLength, actual.getLength());
		assertEquals(expectedText, actual.getText());
	}
	
	
	private final StringParserInput input= new StringParserInput();
	
	
	@Test
	public void matchStringS_with_SpecialCharEscape() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 'abc \\\' \\\" \\` \\a \\b \\f \\n \\r \\t \\v \\\\ \\\n e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc \' \" ` \u0007 \u0008 \f \n \r \t \u000B \\ \n e", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringS_with_OctalEscapeSequence() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 'abc \\1 \\02 \\003 \\777 \\7777 e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc \u0001 \u0002 \u0003 \u01FF \u01FF7 e", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringS_with_xHexEscapeSequence() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 'abc \\x1 \\x02 \\xAA \\xff \\x0ff e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc \u0001 \u0002 \u00AA \u00FF \u000Ff e", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringS_with_xHexEscapeSequence_missingDigit() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 'abc \\x e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_HEX_DIGIT_MISSING, lexer.getFlags());
		assertEquals("abc \\x e", lexer.getText());
		assertDetail(6, 2, "\\x", lexer.getStatusDetail());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringS_with_uHexEscapeSequence() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 'abc \\u1 \\u02 \\u003 \\uAAAA \\ufffa \\u0fffa e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc \u0001 \u0002 \u0003 \uAAAA \uFFFa \u0FFFa e", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringS_with_uHexEscapeSequence_missingDigit() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 'abc \\u e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_HEX_DIGIT_MISSING, lexer.getFlags());
		assertEquals("abc \\u e", lexer.getText());
		assertDetail(6, 2, "\\uxxxx", lexer.getStatusDetail());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringS_with_uHexBracketEscapeSequence() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 'abc \\u{1} \\u{02} \\u{003} \\u{AAAA} \\u{FFF0} e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc \u0001 \u0002 \u0003 \uAAAA \uFFF0 e", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringS_with_uHexBracketEscapeSequence_missingDigit() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 'abc \\u{} e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_HEX_DIGIT_MISSING, lexer.getFlags());
		assertEquals("abc \\u{} e", lexer.getText());
		assertDetail(6, 4, "\\u{xxxx}", lexer.getStatusDetail());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringS_with_uHexBracketEscapeSequence_notClosed() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 'abc \\u{1 e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_NOT_CLOSED, lexer.getFlags());
		assertEquals("abc \\u{1 e", lexer.getText());
		assertDetail(6, 4, "\\u{xxxx}", lexer.getStatusDetail());
		
		lexer.reset(this.input.reset(" 'abc \\u{FFFFF} e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_NOT_CLOSED, lexer.getFlags());
		assertEquals("abc \\u{FFFFF} e", lexer.getText());
		assertDetail(6, 7, "\\u{xxxx}", lexer.getStatusDetail());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringS_with_UHexEscapeSequence() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 'abc \\U1 \\U02 \\U003 \\UAAAA \\UFFF0 \\U1D11E \\U24F5C \\U00000aaaa e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc \u0001 \u0002 \u0003 \uAAAA \uFFF0 \uD834\uDD1E \uD853\uDF5C \u0AAAa e", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringS_with_UHexEscapeSequence_missingDigit() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 'abc \\U e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_HEX_DIGIT_MISSING, lexer.getFlags());
		assertEquals("abc \\U e", lexer.getText());
		assertDetail(6, 2, "\\Uxxxxxxxx", lexer.getStatusDetail());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringS_with_UHexBracketEscapeSequence() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 'abc \\U{1} \\U{02} \\U{003} \\U{AAAA} \\U{FFF0} \\U{1D11E} \\U{24F5C} e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc \u0001 \u0002 \u0003 \uAAAA \uFFF0 \uD834\uDD1E \uD853\uDF5C e", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringS_with_UHexBracketEscapeSequence_missingDigit() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 'abc \\U{} e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_HEX_DIGIT_MISSING, lexer.getFlags());
		assertEquals("abc \\U{} e", lexer.getText());
		assertDetail(6, 4, "\\U{xxxxxxxx}", lexer.getStatusDetail());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringS_with_UHexBracketEscapeSequence_notClosed() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 'abc \\U{1 e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_NOT_CLOSED, lexer.getFlags());
		assertEquals("abc \\U{1 e", lexer.getText());
		assertDetail(6, 4, "\\U{xxxxxxxx}", lexer.getStatusDetail());
		
		lexer.reset(this.input.reset(" 'abc \\U{FFFFFFFFF} e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_NOT_CLOSED, lexer.getFlags());
		assertEquals("abc \\U{FFFFFFFFF} e", lexer.getText());
		assertDetail(6, 11, "\\U{xxxxxxxx}", lexer.getStatusDetail());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringS_with_NullChar() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 'abc \u0000 e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_NULLCHAR, lexer.getFlags());
		assertEquals("abc \u0000 e", lexer.getText());
		assertDetail(6, 1, null, lexer.getStatusDetail());
		
		lexer.reset(this.input.reset(" 'abc \\0 e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_NULLCHAR, lexer.getFlags());
		assertEquals("abc \\0 e", lexer.getText());
		assertDetail(6, 2, null, lexer.getStatusDetail());
		
		lexer.reset(this.input.reset(" 'abc \\x00 e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_NULLCHAR, lexer.getFlags());
		assertEquals("abc \\x00 e", lexer.getText());
		assertDetail(6, 4, null, lexer.getStatusDetail());
		
		lexer.reset(this.input.reset(" 'abc \\u0 e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_NULLCHAR, lexer.getFlags());
		assertEquals("abc \\u0 e", lexer.getText());
		assertDetail(6, 3, null, lexer.getStatusDetail());
		
		lexer.reset(this.input.reset(" 'abc \\u{0} e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_NULLCHAR, lexer.getFlags());
		assertEquals("abc \\u{0} e", lexer.getText());
		assertDetail(6, 5, null, lexer.getStatusDetail());
		
		lexer.reset(this.input.reset(" 'abc \\U0 e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_NULLCHAR, lexer.getFlags());
		assertEquals("abc \\U0 e", lexer.getText());
		assertDetail(6, 3, null, lexer.getStatusDetail());
		
		lexer.reset(this.input.reset(" 'abc \\U{0000} e' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_NULLCHAR, lexer.getFlags());
		assertEquals("abc \\U{0000} e", lexer.getText());
		assertDetail(6, 8, null, lexer.getStatusDetail());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchStringS_with_Escape_unknown() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" 'abc \\c' ").init());
		
		assertEquals(RTerminal.STRING_S, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNKOWN, lexer.getFlags());
		assertEquals("abc \\c", lexer.getText());
		assertDetail(6, 2, "\\c", lexer.getStatusDetail());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	
	@Test
	public void matchStringD_with_SpecialCharEscape() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" \"abc \\\' \\\" \\` \\a \\b \\f \\n \\r \\t \\v \\\\ \\\n e\" ").init());
		
		assertEquals(RTerminal.STRING_D, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc \' \" ` \u0007 \u0008 \f \n \r \t \u000B \\ \n e", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	
	@Test
	public void matchSymbolG_with_SingleCharEscape() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" `abc \\\' \\\" \\` \\a \\b \\f \\n \\r \\t \\v \\\\ \\\n e` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc \' \" ` \u0007 \u0008 \f \n \r \t \u000B \\ \n e", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbolG_with_OctalEscapeSequence() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" `abc \\1 \\02 \\003 \\777 e` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc \u0001 \u0002 \u0003 \u01FF e", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbolG_with_xHexEscapeSequence() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" `abc \\x1 \\x02 \\xAA \\xff e` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(0, lexer.getFlags());
		assertEquals("abc \u0001 \u0002 \u00AA \u00FF e", lexer.getText());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbolG_with_uHexEscapeSequence_unexpected() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" `abc \\u1 \\u02 \\u003 \\uAAAA \\uFFF0 e` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED, lexer.getFlags());
		assertEquals("abc \\u1 \\u02 \\u003 \\uAAAA \\uFFF0 e", lexer.getText());
		assertDetail(6, 3, "\\uxxxx", lexer.getStatusDetail());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbolG_with_uHexBracketEscapeSequence_unexpected() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" `abc \\u{1} \\u{02} \\u{003} \\u{AAAA} \\u{FFF0} e` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED, lexer.getFlags());
		assertEquals("abc \\u{1} \\u{02} \\u{003} \\u{AAAA} \\u{FFF0} e", lexer.getText());
		assertDetail(6, 5, "\\u{xxxx}", lexer.getStatusDetail());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbolG_with_uHexBracketEscapeSequence_notClosed() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" `abc \\u{1 e` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED, lexer.getFlags());
		assertEquals("abc \\u{1 e", lexer.getText());
		assertDetail(6, 4, "\\u{xxxx}", lexer.getStatusDetail());
		
		lexer.reset(this.input.reset(" `abc \\u{FFFFF} e` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED, lexer.getFlags());
		assertEquals("abc \\u{FFFFF} e", lexer.getText());
		assertDetail(6, 7, "\\u{xxxx}", lexer.getStatusDetail());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbolG_with_UHexEscapeSequence() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" `abc \\U1 \\U02 \\U003 \\UAAAA \\UFFF0 \\U1D11E \\U24F5C e` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED, lexer.getFlags());
		assertEquals("abc \\U1 \\U02 \\U003 \\UAAAA \\UFFF0 \\U1D11E \\U24F5C e", lexer.getText());
		assertDetail(6, 3, "\\Uxxxxxxxx", lexer.getStatusDetail());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbolG_with_UHexBracketEscapeSequence() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" `abc \\U{1} \\U{02} \\U{003} \\U{AAAA} \\U{FFF0} \\U{1D11E} \\U{24F5C} e` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED, lexer.getFlags());
		assertEquals("abc \\U{1} \\U{02} \\U{003} \\U{AAAA} \\U{FFF0} \\U{1D11E} \\U{24F5C} e", lexer.getText());
		assertDetail(6, 5, "\\U{xxxxxxxx}", lexer.getStatusDetail());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbolG_with_UHexBracketEscapeSequence_notClosed() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" `abc \\U{1 e` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED, lexer.getFlags());
		assertEquals("abc \\U{1 e", lexer.getText());
		assertDetail(6, 4, "\\U{xxxxxxxx}", lexer.getStatusDetail());
		
		lexer.reset(this.input.reset(" `abc \\U{FFFFFFFFF} e` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED, lexer.getFlags());
		assertEquals("abc \\U{FFFFFFFFF} e", lexer.getText());
		assertDetail(6, 11, "\\U{xxxxxxxx}", lexer.getStatusDetail());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbolG_with_NullChar() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" `abc \u0000 e` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_NULLCHAR, lexer.getFlags());
		assertEquals("abc \u0000 e", lexer.getText());
		assertDetail(6, 1, null, lexer.getStatusDetail());
		
		lexer.reset(this.input.reset(" `abc \\0 e` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_NULLCHAR, lexer.getFlags());
		assertEquals("abc \\0 e", lexer.getText());
		assertDetail(6, 2, null, lexer.getStatusDetail());
		
		lexer.reset(this.input.reset(" `abc \\x00 e` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_NULLCHAR, lexer.getFlags());
		assertEquals("abc \\x00 e", lexer.getText());
		assertDetail(6, 4, null, lexer.getStatusDetail());
		
		lexer.reset(this.input.reset(" `abc \\u0 e` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED, lexer.getFlags());
		assertEquals("abc \\u0 e", lexer.getText());
		assertDetail(6, 3, "\\uxxxx", lexer.getStatusDetail());
		
		lexer.reset(this.input.reset(" `abc \\u{0} e` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED, lexer.getFlags());
		assertEquals("abc \\u{0} e", lexer.getText());
		assertDetail(6, 5, "\\u{xxxx}", lexer.getStatusDetail());
		
		lexer.reset(this.input.reset(" `abc \\U0 e` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED, lexer.getFlags());
		assertEquals("abc \\U0 e", lexer.getText());
		assertDetail(6, 3, "\\Uxxxxxxxx", lexer.getStatusDetail());
		
		lexer.reset(this.input.reset(" `abc \\U{0000} e` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED, lexer.getFlags());
		assertEquals("abc \\U{0000} e", lexer.getText());
		assertDetail(6, 8, "\\U{xxxxxxxx}", lexer.getStatusDetail());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	@Test
	public void matchSymbolG_with_Escape_unknown() {
		final RLexer lexer= new RLexer(RLexer.SKIP_WHITESPACE);
		lexer.reset(this.input.reset(" `abc \\c` ").init());
		
		assertEquals(RTerminal.SYMBOL_G, lexer.next());
		assertEquals(STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNKOWN, lexer.getFlags());
		assertEquals("abc \\c", lexer.getText());
		assertDetail(6, 2, "\\c", lexer.getStatusDetail());
		
		assertEquals(RTerminal.EOF, lexer.next());
	}
	
	
}
