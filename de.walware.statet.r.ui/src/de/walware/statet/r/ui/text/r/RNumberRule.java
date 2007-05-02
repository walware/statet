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

package de.walware.statet.r.ui.text.r;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import de.walware.statet.r.core.rlang.*;

/**
 * A code-scanner-rule (an implementation of <code>IRule</code>) capable of 
 * detecting number-constants in R-scripts.
 * The rule provides tokens for correct number-constants (according to R-lang), 
 * number-like "words", which are not correct numbers are detected as invalid
 * words.
 *
 * R-Version: 2.5.0
 */
public class RNumberRule implements IRule {
	
	private static final int S_START = 0;
	private static final int S_LEADING_ZERO = 8;
	private static final int S_LEADING_PERIOD = 9;
	private static final int S_INT_PART = 10;
//	private static final int S_PERIOD = 11;
	private static final int S_FRAC_PART = 12;
	private static final int S_E_START = 20;
	private static final int S_E_SIGN = 21;
	private static final int S_E_PART = 22;
	private static final int S_HEX_START = 30;
	private static final int S_HEX_PART = 31;
	private static final int S_IMAGINARY_SUFFIX = 90;
	private static final int S_LONG_SUFFIX = 91;
	private static final int S_INVALID = 100;
	private static final int S_STOP = 1000;
	private static final int S_CANCEL = 1100;
	private static final int S_FINISH = 2000;
	private static final int S_FINISHED_VALID = 2100;
	private static final int S_FINISHED_INVALID = 2200;
	
	
	private IToken fValidNumberToken;
	private IToken fInvalidNumberToken;
	
	
	public RNumberRule(IToken validNumberToken, IToken invalidNumberToken) {
		fValidNumberToken = validNumberToken;
		fInvalidNumberToken = invalidNumberToken;
	}
	

	public IToken evaluate(ICharacterScanner scanner) {
		int status = S_START;
		int readed = 0;
					
		ITERATE_CHARS : while(status < S_STOP) {
			int c = scanner.read();
			readed++;
			
			switch(status) {
			case S_START:
				switch (c) {
				case '0':
					status = S_LEADING_ZERO;
					continue ITERATE_CHARS;
				case '.':
					status = S_LEADING_PERIOD;
					continue ITERATE_CHARS;
				default:
					if (RTokens.isDigit(c)) {
						status = S_INT_PART;
						continue ITERATE_CHARS;
					}
				}
				status = S_CANCEL;
				continue ITERATE_CHARS;
				
			case S_LEADING_PERIOD:
				if (RTokens.isDigit(c)) {
					status = S_FRAC_PART;
					continue ITERATE_CHARS;
				}
				status = S_CANCEL;
				continue ITERATE_CHARS;
				
			case S_LEADING_ZERO:
				switch (c) {
				case 'x':
				case 'X':
					status = S_HEX_START;
					continue ITERATE_CHARS;
				}
				status = S_INT_PART; // continue int part handling
			case S_INT_PART:
				switch (c) {
				case '.':
					status = S_FRAC_PART;
					continue ITERATE_CHARS;
				}
//			case S_PERIOD:
			case S_FRAC_PART:
				switch (c) {
				case 'e':
				case 'E':
					status = S_E_START;
					continue ITERATE_CHARS;
				case 'i':
					status = S_IMAGINARY_SUFFIX;
					continue ITERATE_CHARS;
				case 'L':
					status = S_LONG_SUFFIX;
					continue ITERATE_CHARS;
				default:
					if (RTokens.isDigit(c)) {
						// status = dont change
						continue ITERATE_CHARS;
					}
				}
				if (RTokens.isSeparator(c)) {
					status = S_FINISHED_VALID;
					continue ITERATE_CHARS;
				}
				status = S_INVALID;
				continue ITERATE_CHARS;

			case S_E_START:
				switch (c) {
				case '+':
				case '-':
					status = S_E_SIGN;
					continue ITERATE_CHARS;
				}
				// no break
			case S_E_SIGN:
				if (RTokens.isDigit(c)) {
					status = S_E_PART;
					continue ITERATE_CHARS;
				}
				status = S_INVALID;
				continue ITERATE_CHARS;
				
			case S_E_PART:
				switch (c) {
				case 'i':
					status = S_IMAGINARY_SUFFIX;
					continue ITERATE_CHARS;
				case 'L':
					status = S_LONG_SUFFIX;
					continue ITERATE_CHARS;
				default:
					if (RTokens.isDigit(c)) {
						continue ITERATE_CHARS;
					}
				}
				if (RTokens.isSeparator(c)) {
					status = S_FINISHED_VALID;
					continue ITERATE_CHARS;
				}
				status = S_INVALID;
				continue ITERATE_CHARS;
			
			case S_HEX_START:
				if (RTokens.isHexDigit(c)) {
					status = S_HEX_PART;
					continue ITERATE_CHARS;
				}
				status = S_INVALID;
				continue ITERATE_CHARS;
				
			case S_HEX_PART:
				switch (c) {
				case 'L':
					status = S_LONG_SUFFIX;
					continue ITERATE_CHARS;
				default:
					if (RTokens.isHexDigit(c)) {
						// status = dont change
						continue ITERATE_CHARS;
					}
				}
				if (RTokens.isSeparator(c)) {
					status = S_FINISHED_VALID;
					continue ITERATE_CHARS;
				}
				status = S_INVALID;
				continue ITERATE_CHARS;
				
			case S_IMAGINARY_SUFFIX:
			case S_LONG_SUFFIX:
				if (RTokens.isSeparator(c)) {
					status = S_FINISHED_VALID;
					continue ITERATE_CHARS;
				}
				status = S_INVALID;
				continue ITERATE_CHARS;
				
			case S_INVALID:
				if (RTokens.isSeparator(c)) {
					status = S_FINISHED_INVALID;
					continue ITERATE_CHARS;
				}
				continue ITERATE_CHARS;
			}
		}
		
		if (status >= S_FINISH) {
			scanner.unread();
			return (status == S_FINISHED_VALID)? fValidNumberToken : fInvalidNumberToken;
		}
						
		for (; readed > 0; readed--) {
			scanner.unread();
		}
		return Token.UNDEFINED;
	}
}