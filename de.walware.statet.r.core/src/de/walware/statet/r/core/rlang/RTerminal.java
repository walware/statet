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

package de.walware.statet.r.core.rlang;


public enum RTerminal {
	
	EOF (""), //$NON-NLS-1$
	BLANK (" "), //$NON-NLS-1$
	LINEBREAK ("\n"), //$NON-NLS-1$
	COMMENT ("#"), //$NON-NLS-1$
	ROXYGEN_COMMENT ("#'"), //$NON-NLS-1$
	UNKNOWN (""), //$NON-NLS-1$
	OTHER (""), //$NON-NLS-1$
	
	BLOCK_OPEN ("{"), //$NON-NLS-1$
	BLOCK_CLOSE ("}"), //$NON-NLS-1$
	GROUP_OPEN ("("), //$NON-NLS-1$
	GROUP_CLOSE (")"), //$NON-NLS-1$
	
	SUB_INDEXED_S_OPEN ("["), //$NON-NLS-1$
	SUB_INDEXED_D_OPEN ("[["), //$NON-NLS-1$
	SUB_INDEXED_CLOSE ("]"), //$NON-NLS-1$
	SUB_NAMED_PART ("$"), //$NON-NLS-1$
	SUB_NAMED_SLOT ("@"), //$NON-NLS-1$
	
	NS_GET ("::"), //$NON-NLS-1$
	NS_GET_INT (":::"), //$NON-NLS-1$
	
	PLUS ("+"), //$NON-NLS-1$
	MINUS ("-"), //$NON-NLS-1$
	MULT ("*"), //$NON-NLS-1$
	DIV ("/"), //$NON-NLS-1$
	OR ("|"), //$NON-NLS-1$
	OR_D ("||"), //$NON-NLS-1$
	AND ("&"), //$NON-NLS-1$
	AND_D ("&&"), //$NON-NLS-1$
	NOT ("!"), //$NON-NLS-1$
	POWER ("^"), //$NON-NLS-1$
	SEQ (":"), //$NON-NLS-1$
	SPECIAL ("%"), //$NON-NLS-1$
	
	QUESTIONMARK ("?"), //$NON-NLS-1$
	COMMA (","), //$NON-NLS-1$
	SEMI (";"), //$NON-NLS-1$
	
	ARROW_LEFT_S ("<-"), //$NON-NLS-1$
	ARROW_LEFT_D ("<<-"), //$NON-NLS-1$
	ARROW_RIGHT_S ("->"), //$NON-NLS-1$
	ARROW_RIGHT_D ("->>"), //$NON-NLS-1$
	EQUAL ("="), //$NON-NLS-1$
	COLON_EQUAL (":="), //$NON-NLS-1$
	TILDE ("~"), //$NON-NLS-1$
	REL_NE ("!="), //$NON-NLS-1$
	REL_EQ ("=="), //$NON-NLS-1$
	REL_LT ("<"), //$NON-NLS-1$
	REL_LE ("<="), //$NON-NLS-1$
	REL_GT (">"), //$NON-NLS-1$
	REL_GE (">="), //$NON-NLS-1$
	
	IF ("if"), //$NON-NLS-1$
	ELSE ("else"), //$NON-NLS-1$
	FOR ("for"), //$NON-NLS-1$
	IN ("in"), //$NON-NLS-1$
	WHILE ("while"), //$NON-NLS-1$
	REPEAT ("repeat"), //$NON-NLS-1$
	NEXT ("next"), //$NON-NLS-1$
	BREAK ("break"), //$NON-NLS-1$
	FUNCTION ("function"), //$NON-NLS-1$
	
	SYMBOL (""), //$NON-NLS-1$
	SYMBOL_G ("`"), //$NON-NLS-1$
	NUM_INT (""), //$NON-NLS-1$
	NUM_NUM (""), //$NON-NLS-1$
	NUM_CPLX (""), //$NON-NLS-1$
	STRING_S ("\'"), //$NON-NLS-1$
	STRING_D ("\""), //$NON-NLS-1$
	
	NULL ("NULL"), //$NON-NLS-1$
	TRUE ("TRUE"), //$NON-NLS-1$
	FALSE ("FALSE"), //$NON-NLS-1$
	NA ("NA"), //$NON-NLS-1$
	NA_INT ("NA_integer_"), //$NON-NLS-1$
	NA_REAL ("NA_real_"), //$NON-NLS-1$
	NA_CPLX ("NA_complex_"), //$NON-NLS-1$
	NA_CHAR ("NA_character_"), //$NON-NLS-1$
	NAN ("NaN"), //$NON-NLS-1$
	INF ("Inf"), //$NON-NLS-1$
	
	;
	
	
	public static final String S_WHITESPACE = " "; //$NON-NLS-1$
	public static final String S_TAB = "\t"; //$NON-NLS-1$
	public static final String S_LINEBREAK_CRLF ="\r\n"; //$NON-NLS-1$
	public static final String S_LINEBREAK_LF ="\r"; //$NON-NLS-1$
	public static final String S_LINEBREAK_CR ="\n"; //$NON-NLS-1$
	public static final String S_COMMENT = "#"; //$NON-NLS-1$
	public static final String S_BLOCK_OPEN = "{"; //$NON-NLS-1$
	public static final String S_BLOCK_CLOSE = "}"; //$NON-NLS-1$
	public static final String S_GROUP_OPEN = "("; //$NON-NLS-1$
	public static final String S_GROUP_CLOSE = ")"; //$NON-NLS-1$
	public static final String S_SUB_INDEXED_S_OPEN = "["; //$NON-NLS-1$
	public static final String S_SUB_INDEXED_D_OPEN = "[["; //$NON-NLS-1$
	public static final String S_SUB_INDEXED_CLOSE = "]"; //$NON-NLS-1$
	public static final String S_SUB_NAMED = "$"; //$NON-NLS-1$
	public static final String S_SUB_AT = "@"; //$NON-NLS-1$
	public static final String S_NS_GET = "::"; //$NON-NLS-1$
	public static final String S_NS_GET_INT = ":::"; //$NON-NLS-1$
	public static final String S_PLUS = "+"; //$NON-NLS-1$
	public static final String S_MINUS = "-"; //$NON-NLS-1$
	public static final String S_MULT = "*"; //$NON-NLS-1$
	public static final String S_DIV = "/"; //$NON-NLS-1$
	public static final String S_OR = "|"; //$NON-NLS-1$
	public static final String S_OR_D = "||"; //$NON-NLS-1$
	public static final String S_AND = "&"; //$NON-NLS-1$
	public static final String S_AND_D = "&&"; //$NON-NLS-1$
	public static final String S_NOT = "!"; //$NON-NLS-1$
	public static final String S_POWER = "^"; //$NON-NLS-1$
	public static final String S_SEQ = ":"; //$NON-NLS-1$
	public static final String S_SPECIAL = "%"; //$NON-NLS-1$
	public static final String S_HELP = "?"; //$NON-NLS-1$
	public static final String S_COMMA = ","; //$NON-NLS-1$
	public static final String S_SEMI = ";"; //$NON-NLS-1$
	public static final String S_ASSIGN_LEFT = "<-"; //$NON-NLS-1$
	public static final String S_ASSIGN_LEFT_D = "<<-"; //$NON-NLS-1$
	public static final String S_ASSIGN_RIGHT = "->"; //$NON-NLS-1$
	public static final String S_ASSIGN_RIGHT_D = "->>"; //$NON-NLS-1$
	public static final String S_EQUAL = "="; //$NON-NLS-1$
	public static final String S_MODEL = "~"; //$NON-NLS-1$
	public static final String S_REL_NE = "!="; //$NON-NLS-1$
	public static final String S_REL_EQ = "=="; //$NON-NLS-1$
	public static final String S_REL_LT = "<"; //$NON-NLS-1$
	public static final String S_REL_LE = "<="; //$NON-NLS-1$
	public static final String S_REL_GT = ">"; //$NON-NLS-1$
	public static final String S_REL_GE = ">="; //$NON-NLS-1$
	public static final String S_IF = "if"; //$NON-NLS-1$
	public static final String S_ELSE = "else"; //$NON-NLS-1$
	public static final String S_FOR = "for"; //$NON-NLS-1$
	public static final String S_IN = "in"; //$NON-NLS-1$
	public static final String S_WHILE = "while"; //$NON-NLS-1$
	public static final String S_REPEAT = "repeat"; //$NON-NLS-1$
	public static final String S_NEXT = "next"; //$NON-NLS-1$
	public static final String S_BREAK = "break"; //$NON-NLS-1$
	public static final String S_FUNCTION = "function"; //$NON-NLS-1$
	public static final String S_ELLIPSIS = "..."; //$NON-NLS-1$
	public static final String S_STRING_S = "\'"; //$NON-NLS-1$
	public static final String S_STRING_D = "\""; //$NON-NLS-1$
	public static final String S_TRUE = "TRUE"; //$NON-NLS-1$
	public static final String S_FALSE = "FALSE"; //$NON-NLS-1$
	public static final String S_NA = "NA"; //$NON-NLS-1$
	public static final String S_NA_INT = "NA_integer_"; //$NON-NLS-1$
	public static final String S_NA_REAL = "NA_real_"; //$NON-NLS-1$
	public static final String S_NA_CPLX = "NA_complex_"; //$NON-NLS-1$
	public static final String S_NA_CHAR = "NA_character_"; //$NON-NLS-1$
	public static final String S_NULL = "NULL"; //$NON-NLS-1$
	public static final String S_NAN = "NaN"; //$NON-NLS-1$
	public static final String S_INF = "Inf"; //$NON-NLS-1$
	
	
	public static String[] textArray(final RTerminal[] list) {
		final String[] texts = new String[list.length];
		for (int i = 0; i < texts.length; i++) {
			texts[i] = list[i].text;
		}
		return texts;
	};
	
	
	public final String text;
	
	RTerminal(final String text) {
		this.text = text;
	}
	
}
