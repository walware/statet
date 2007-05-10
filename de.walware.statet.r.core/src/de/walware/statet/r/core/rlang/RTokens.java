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

	public static final char COMMENT_CHAR = '#';
	
	// Reserved Words ---------------------------------------------------------
	
	// Special Constants
	public static final String NULL = "NULL";
	public static final String NA = "NA";
	public static final String Inf = "Inf";
	public static final String NaN = "NaN";
	public static final String[] SPECIAL_CONSTANTS = {
			NULL, NA, Inf, NaN,
	};
	
	// Logical Constants
	public static final String TRUE = "TRUE";
	public static final String FALSE = "FALSE";
	public static final String[] LOGICAL_CONSTANTS = { 
			TRUE, FALSE,
	};
	
	// 
	public static final String[] FLOWCONTROL_RESERVED_WORDS = {
			"if", "else", "repeat", "while", "function", "for", "in", "next", "break",
	};
	
	// Operators --------------------------------------------------------------
	
	public static final String[] SEPARATORS = {
			";", ",",
	};
	
	public static final String[] ASSIGNMENT_OPERATORS = {
			"<-", "->", "<<-", "->>", "=",
	};
	
	public static final String[] DEFAULT_OPERATORS = {
			"+", "-", "*", "/", "^", "%%", "%/%",	// arithmetic
			">", ">=", "<", "<=", "==", "!=", 		// relational
			"!", "&", "&&", "|", "||",				// logical
			"~", 									// model formulae
			":",									// sequence
	};
	
	public static final String[] PREDIFINED_INFIX_OPERATORS = {
			"%%", 
			"%*%", "%/%", "%in%", "%o%", "%x%",
	};
	
	public static final String[] GROUPING = {
			"{", "}", "(", ")",
	};
	
	public static final String[] SUBELEMENT_ACCESS = {
			"[", "]", "$", "@"
	};
	
	public static final String[] NAMESPACE_ACCESS = {
			"::", ":::",
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

	public static int PERIOD = 46;
	
}
