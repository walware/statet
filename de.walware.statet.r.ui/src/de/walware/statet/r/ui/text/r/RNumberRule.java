/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
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
 * @author Stephan Wahlbrink
 */
public class RNumberRule implements IRule {
	
	private static final int S_START = 0;
	private static final int S_SINGLE_PERIOD = 9;
	private static final int S_INT_PART = 10;
	private static final int S_PERIOD = 11;
	private static final int S_DEC_PART = 12;
	private static final int S_E_START = 20;
	private static final int S_E_PART = 21;
	private static final int S_INVALID = 100;
	private static final int S_BREAK = 1000;
	private static final int S_FINISHED = 10000;
	private static final int S_FINISHED_VALID = 10001;
	private static final int S_FINISHED_INVALID = 10100;
	
	private IToken fValidNumberToken;
	private IToken fInvalidNumberToken;
	
	public RNumberRule(IToken validNumberToken, IToken invalidNumberToken) {
		fValidNumberToken = validNumberToken;
		fInvalidNumberToken = invalidNumberToken;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
	 */
	public IToken evaluate(ICharacterScanner scanner) {
		int status = S_START;
		int readed = 0;
					
		while(status < S_BREAK) {
			int c = scanner.read();
			readed++;
			
			switch(status) {
				case S_START:
					if (RTokens.isDigit(c))
						status = S_INT_PART;
					else
					if (c == RTokens.PERIOD)
						status = S_SINGLE_PERIOD;
					else
						status = S_BREAK;
					break;
					
				case S_SINGLE_PERIOD:
					if (RTokens.isDigit(c))
						status = S_DEC_PART;
					else
						status = S_BREAK;
					break;
					
				case S_INT_PART:
					if (!RTokens.isDigit(c)) {
						if (c == RTokens.PERIOD)
							status = S_PERIOD;
						else
						if (c == 'E' || c == 'e')
							status = S_E_START;
						else
						if (RTokens.isSeparator(c))
							status = S_FINISHED_VALID;
						else
							status = S_INVALID;
					}
					break;
					
				case S_PERIOD:
				case S_DEC_PART:
					if (RTokens.isDigit(c))
						status = S_DEC_PART;
					else
					if (c == 'E' || c == 'e')
						status = S_E_START;
					else
					if (RTokens.isSeparator(c))
						status = S_FINISHED_VALID;
					else
						status = S_INVALID;
					break;
					
				case S_E_START:
					if (c == '+' || c == '-' || RTokens.isDigit(c))
						status = S_E_PART;
					else
						status = S_BREAK;
					break;
					
				case S_E_PART:
					if (!RTokens.isDigit(c)) {
						if (RTokens.isSeparator(c))
							status = S_FINISHED_VALID;
						else
							status = S_INVALID;
					}
					break;
				
				case S_INVALID:
					if (RTokens.isSeparator(c))
						status = S_FINISHED_INVALID;
					break;
			}
		}
		
		if (status >= S_FINISHED) {
			scanner.unread();
			return (status == S_FINISHED_VALID)? fValidNumberToken : fInvalidNumberToken;
		}
						
		for (; readed > 0; readed--) {
			scanner.unread();
		}
		return Token.UNDEFINED;
	}
}