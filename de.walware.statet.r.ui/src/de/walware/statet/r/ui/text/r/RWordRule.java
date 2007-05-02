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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import de.walware.statet.r.core.rlang.RTokens;


/**
 * Rule to find correct identifiers in R.
 * 
 * R-Version: 2.5.0
 */
public class RWordRule implements IRule {
	
	private static final int S_START = 0;
	private static final int S_PERIOD_START = 1;
	private static final int S_WORD = 10;
	private static final int S_INVALID = 100;
	private static final int S_STOP = 1000;
	private static final int S_CANCEL = 1100;
	private static final int S_FINISH = 2000;
	private static final int S_FINISHED_VALID = 2100;
	private static final int S_FINISHED_INVALID = 2200;
	
	private IToken fDefaultToken; 
	private IToken fInvalidIdentifierToken;
	private Map<String, IToken> fSpecialWords;
	
	private StringBuilder fBuffer;
	
	
	public RWordRule(IToken defaultToken, IToken invalidIdentifierToken) {
		fDefaultToken = defaultToken;
		fInvalidIdentifierToken = invalidIdentifierToken;
		fSpecialWords = new HashMap<String, IToken>();
		fBuffer = new StringBuilder();
	}
	
	public void addSpecialWords(String[] word, IToken token) {
		for (int i = 0; i < word.length; i++) {
			addSpecialWord(word[i], token);
		}
	}
	public void addSpecialWord(String word, IToken token) {
		fSpecialWords.put(word, token);
	}
	

	public IToken evaluate(ICharacterScanner scanner) {
		int status = S_START;
		int readed = 0;
		
		ITERATE_CHARS : while(status < S_STOP) {
			int c = scanner.read();
			readed++;
			
			switch (status) {
			case S_START:
				switch (c) {
				case '.':
					status = S_PERIOD_START;
					break; // start pattern
				case '_':
					status = S_INVALID;
					continue ITERATE_CHARS;
				default:
					if (Character.isLetter(c)) {
						status = S_WORD;
						break; // start pattern
					}
					status = S_CANCEL;
					continue ITERATE_CHARS;
				}
				fBuffer.append((char) c);
				continue ITERATE_CHARS;
				
			case S_PERIOD_START:
				if (RTokens.isDigit(c)) {
					status = S_CANCEL;
					continue ITERATE_CHARS;
				}
				status = S_WORD;
				// continue WORD
			case S_WORD:
				if (Character.isLetter(c) || RTokens.isDigit(c)
						|| c== '_' || c == '.') {
					fBuffer.append((char) c);
					continue ITERATE_CHARS;
				}
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
			if (status == S_FINISHED_VALID) {
				return succeed();
			} else
				fBuffer.setLength(0);
				return fInvalidIdentifierToken;
		}
						
		for (; readed > 0; readed--) {
			scanner.unread();
		}
		fBuffer.setLength(0);
		return Token.UNDEFINED;
	}
	
	private IToken succeed() {
		
		IToken token = fSpecialWords.get(fBuffer.toString());
		fBuffer.setLength(0);
		return (token != null)? token: fDefaultToken;
	}
	
}