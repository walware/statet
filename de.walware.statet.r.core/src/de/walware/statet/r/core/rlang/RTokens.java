/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rlang;

import java.util.Arrays;


/**
 * Provides definition and util-method for tokens of the R-language.
 * 'Tokes' means tokens according to R-language definition, they defines not
 * directly <code>Token</code>, implementions of <code>IToken</code>.
 *
 * @author Stephan Wahlbrink
 */
public class RTokens {

	public static final String[] SPECIAL_CONSTANTS = new String[] {
			RTerminal.S_NULL,
			RTerminal.S_NA,
			RTerminal.S_NA_REAL,
			RTerminal.S_NA_INT,
			RTerminal.S_NA_CPLX,
			RTerminal.S_NA_CHAR,
			RTerminal.S_INF,
			RTerminal.S_NAN,
	};
	
	public static final String[] LOGICAL_CONSTANTS = new String[] {
			RTerminal.S_TRUE,
			RTerminal.S_FALSE,
	};
	
	public static final String[] FLOWCONTROL_RESERVED_WORDS = new String[] {
			RTerminal.S_IF,
			RTerminal.S_ELSE,
			RTerminal.S_FOR,
			RTerminal.S_IN,
			RTerminal.S_WHILE,
			RTerminal.S_REPEAT,
			RTerminal.S_NEXT,
			RTerminal.S_BREAK,
			RTerminal.S_FUNCTION,
	};
	
	public static final String[] SEPARATORS = new String[] {
			RTerminal.S_COMMA,
			RTerminal.S_SEMI,
	};
	
	public static final String[] ASSIGNMENT_OPERATORS = new String[] {
			RTerminal.S_ASSIGN_LEFT,
			RTerminal.S_ASSIGN_LEFT_D,
			RTerminal.S_ASSIGN_RIGHT,
			RTerminal.S_ASSIGN_RIGHT_D,
			RTerminal.S_EQUAL,
	};
	
	public static final String[] OPERATORS_LOGICAL = new String[] {
			RTerminal.S_NOT,
			RTerminal.S_AND,
			RTerminal.S_AND_D,
			RTerminal.S_OR,
			RTerminal.S_OR_D,
	};
	
	public static final String[] OPERATORS_RELATIONAL = new String[] {
			RTerminal.S_REL_EQ,
			RTerminal.S_REL_GT,
			RTerminal.S_REL_GE,
			RTerminal.S_REL_LE,
			RTerminal.S_REL_LT,
			RTerminal.S_REL_NE,
	};
	
	public static final String[] DEFAULT_OPERATORS = new String[] {
			RTerminal.S_PLUS,
			RTerminal.S_MINUS,
			RTerminal.S_MULT,
			RTerminal.S_DIV,
			RTerminal.S_POWER,
			RTerminal.S_SEQ,
			RTerminal.S_MODEL,
	};
	
	public static final String[] PREDIFINED_INFIX_OPERATORS = {
			"%%",
			"%*%", "%/%", "%in%", "%o%", "%x%",
	};
	
	public static final String[] GROUPING = new String[] {
			RTerminal.S_BLOCK_OPEN,
			RTerminal.S_BLOCK_CLOSE,
			RTerminal.S_GROUP_OPEN,
			RTerminal.S_GROUP_CLOSE,
	};
	
	public static final String[] SUBELEMENT_ACCESS = new String[] {
			RTerminal.S_SUB_INDEXED_S_OPEN,
			RTerminal.S_SUB_INDEXED_D_OPEN,
			RTerminal.S_SUB_INDEXED_CLOSE,
			RTerminal.S_SUB_NAMED,
			RTerminal.S_SUB_AT,
	};
	
	public static final String[] NAMESPACE_ACCESS = new String[] {
			RTerminal.S_NS_GET,
			RTerminal.S_NS_GET_INT,
	};

	
	// ----
	
	public static final char[] SEPARATOR_CHARS = {
			'!', '$', '%', '&', '(', ')',
			'*', '+', ',', '-', '/',
			':', ';' , '<', '=', '>',
			'[', ']', '^', '{', '|', '}', '~',
			'@',
	};
	
	public static final char[] WHITESPACE_CHARS = { // on changes, note methods below
			' ', '\t',
	};
	
	public static final char[] NEWLINE_CHARS = {
			'\r', '\n',
	};
	
	private static boolean[] SEPARATOR_MAP;

	static {
		SEPARATOR_MAP = new boolean[128];
		Arrays.fill(SEPARATOR_MAP, false);
		for (int i = 0; i < RTokens.SEPARATOR_CHARS.length; i++) {
			SEPARATOR_MAP[RTokens.SEPARATOR_CHARS[i]] = true;
		}
		for (int i = 0; i < RTokens.WHITESPACE_CHARS.length; i++) {
			SEPARATOR_MAP[RTokens.WHITESPACE_CHARS[i]] = true;
		}
		for (int i = 0; i < RTokens.NEWLINE_CHARS.length; i++) {
			SEPARATOR_MAP[RTokens.NEWLINE_CHARS[i]] = true;
		}
	}

	public static boolean isSeparator(int c) {
		return (c < 0   // EOF
				|| (c <= 127 && SEPARATOR_MAP[c]) );
	}
	
	public static boolean isRobustSeparator(int c, boolean isDotSeparator) {
		return ( (c == '.')? isDotSeparator :
				(!Character.isLetter(c) && c != '_' && !isDigit(c)) );
	}

	public static boolean isDigit(int c) {
		return (c >= 0x30 && c <= 0x39);
	}
	
	public static boolean isHexDigit(int c) {
		return ( c >= 0x30 && (c <= 0x39 || 		// 0-9
				(c >= 0x41 && (c <= 0x46 ||			// A-F
				(c >= 0x61 && c <= 0x66) ))));		// a-f
	}
	
	public static boolean isWhitespace(int c) {
		return (c == WHITESPACE_CHARS[0] || c == WHITESPACE_CHARS[1]);
	}

}
