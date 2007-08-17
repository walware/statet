/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rlang;



public enum RTerminal {
	
	EOF (""),
	BLANK (" "),
	LINEBREAK ("\n"),
	COMMENT ("#"),
	UNKNOWN ("¡"),

	BLOCK_OPEN ("{"),
	BLOCK_CLOSE ("}"),
	GROUP_OPEN ("("),
	GROUP_CLOSE (")"),

	SUB_INDEXED_S_OPEN ("["),
	SUB_INDEXED_D_OPEN ("[["),
	SUB_INDEXED_CLOSE ("]"),
	SUB_NAMED ("$"),
	SUB_AT ("@"),
	
	NS_GET ("::"),
	NS_GET_INT (":::"),

	PLUS ("+"),
	MINUS ("-"),
	MULT ("*"),
	DIV ("/"),
	OR ("|"),
	OR_D ("||"),
	AND ("&"),
	AND_D ("&&"),
	NOT ("!"),
	POWER ("^"),
	SEQ (":"),
	SPECIAL ("%"),

	QUESTIONMARK ("?"),
	COMMA (","),
	SEMI (";"),
	
	ARROW_LEFT_S ("<-"),
	ARROW_LEFT_D ("<<-"),
	ARROW_RIGHT_S ("->"),
	ARROW_RIGHT_D ("->>"),
	EQUAL ("="),
	TILDE ("~"),
	REL_NE ("!="),
	REL_EQ ("=="),
	REL_LT ("<"),
	REL_LE ("<="),
	REL_GT (">"),
	REL_GE (">="),

	IF ("if"),
	ELSE ("else"),
	FOR ("for"),
	IN ("in"),
	WHILE ("while"),
	REPEAT ("repeat"),
	NEXT ("next"),
	BREAK ("break"),
	FUNCTION ("function"),
	ELLIPSIS ("..."),
	
	STRING_S ("\'"),
	STRING_D ("\""),
	NUM_INT ("int"),
	NUM_COMPLEX ("cplx"),
	NUM_NUM ("num"),
	SYMBOL ("symbol"),
	
	TRUE ("TRUE"),
	FALSE ("FALSE"),
	NA ("NA"),
	NA_INT ("NA_integer_"),
	NA_REAL ("NA_real_"),
	NA_CPLX ("NA_complex_"),
	NA_CHAR ("NA_character_"),
	NULL ("NULL"),
	NAN ("NaN"),
	INF ("Inf"),
	;


	public static final String S_WHITESPACE = " ";
	public static final String S_TAB = "\t";
	public static final String S_LINEBREAK_CRLF ="\r\n";
	public static final String S_LINEBREAK_LF ="\r";
	public static final String S_LINEBREAK_CR ="\n";
	public static final String S_COMMENT = "#";
	public static final String S_BLOCK_OPEN = "{";
	public static final String S_BLOCK_CLOSE = "}";
	public static final String S_GROUP_OPEN = "(";
	public static final String S_GROUP_CLOSE = ")";
	public static final String S_SUB_INDEXED_S_OPEN = "[";
	public static final String S_SUB_INDEXED_D_OPEN = "[[";
	public static final String S_SUB_INDEXED_CLOSE = "]";
	public static final String S_SUB_NAMED = "$";
	public static final String S_SUB_AT = "@";
	public static final String S_NS_GET = "::";
	public static final String S_NS_GET_INT = ":::";
	public static final String S_PLUS = "+";
	public static final String S_MINUS = "-";
	public static final String S_MULT = "*";
	public static final String S_DIV = "/";
	public static final String S_OR = "|";
	public static final String S_OR_D = "||";
	public static final String S_AND = "&";
	public static final String S_AND_D = "&&";
	public static final String S_NOT = "!";
	public static final String S_POWER = "^";
	public static final String S_SEQ = ":";
	public static final String S_SPECIAL = "%";
	public static final String S_HELP = "?";
	public static final String S_COMMA = ",";
	public static final String S_SEMI = ";";
	public static final String S_ASSIGN_LEFT = "<-";
	public static final String S_ASSIGN_LEFT_D = "<<-";
	public static final String S_ASSIGN_RIGHT = "->";
	public static final String S_ASSIGN_RIGHT_D = "->>";
	public static final String S_EQUAL = "=";
	public static final String S_MODEL = "~";
	public static final String S_REL_NE = "!=";
	public static final String S_REL_EQ = "==";
	public static final String S_REL_LT = "<";
	public static final String S_REL_LE = "<=";
	public static final String S_REL_GT = ">";
	public static final String S_REL_GE = ">=";
	public static final String S_IF = "if";
	public static final String S_ELSE = "else";
	public static final String S_FOR = "for";
	public static final String S_IN = "in";
	public static final String S_WHILE = "while";
	public static final String S_REPEAT = "repeat";
	public static final String S_NEXT = "next";
	public static final String S_BREAK = "break";
	public static final String S_FUNCTION = "function";
	public static final String S_ELLIPSIS = "...";
	public static final String S_STRING_S = "\'";
	public static final String S_STRING_D = "\"";
	public static final String S_TRUE = "TRUE";
	public static final String S_FALSE = "FALSE";
	public static final String S_NA = "NA";
	public static final String S_NA_INT = "NA_integer_";
	public static final String S_NA_REAL = "NA_real_";
	public static final String S_NA_CPLX = "NA_complex_";
	public static final String S_NA_CHAR = "NA_character_";
	public static final String S_NULL = "NULL";
	public static final String S_NAN = "NaN";
	public static final String S_INF = "Inf";

	
	public static String[] textArray(RTerminal[] list) {
		String[] texts = new String[list.length];
		for (int i = 0; i < texts.length; i++) {
			texts[i] = list[i].text;
		}
		return texts;
	};
	
	
	
	
	public final String text;
	
	RTerminal(String text) {
		this.text = text;
	}
	
}