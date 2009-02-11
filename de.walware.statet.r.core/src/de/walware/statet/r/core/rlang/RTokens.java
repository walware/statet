/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rlang;

import java.util.Arrays;


/**
 * Provides util-method for tokens of the R-language.
 * 'Tokes' means tokens according to R-language definition, they defines not
 * directly <code>Token</code>, implementions of <code>IToken</code>.
 */
public class RTokens {
	
	public static final String[] CONSTANT_WORDS = new String[] {
			RTerminal.S_NULL,
			RTerminal.S_NA,
			RTerminal.S_NA_REAL,
			RTerminal.S_NA_INT,
			RTerminal.S_NA_CPLX,
			RTerminal.S_NA_CHAR,
			RTerminal.S_INF,
			RTerminal.S_NAN,
			RTerminal.S_TRUE,
			RTerminal.S_FALSE,
	};
	
	public static final String[] FLOWCONTROL_WORDS = new String[] {
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
	
	public static final String[] PREDIFINED_INFIX_OPERATORS = {
			"%%", //$NON-NLS-1$
			"%*%", "%/%", "%in%", "%o%", "%x%", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
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
	
	public static boolean isSeparator(final int c) {
		return (c < 0   // EOF
				|| (c <= 127 && SEPARATOR_MAP[c]) );
	}
	
	public static boolean isRobustSeparator(final int c, final boolean isDotSeparator) {
		return ( (c == '.')? isDotSeparator :
				(!Character.isLetterOrDigit(c) && c != '_') );
	}
	
	public static boolean isDigit(final int c) {
		return (c >= 0x30 && c <= 0x39);
	}
	
	public static boolean isHexDigit(final int c) {
		return ( c >= 0x30 && (c <= 0x39 || 		// 0-9
				(c >= 0x41 && (c <= 0x46 ||			// A-F
				(c >= 0x61 && c <= 0x66) ))));		// a-f
	}
	
	public static boolean isWhitespace(final int c) {
		return (c == WHITESPACE_CHARS[0] || c == WHITESPACE_CHARS[1]);
	}
	
}
