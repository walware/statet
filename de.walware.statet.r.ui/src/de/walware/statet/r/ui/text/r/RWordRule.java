/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
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
 * @author Stephan Wahlbrink
 */
public class RWordRule implements IRule {
	
	private static final int S_START = 0;
	private static final int S_PERIOD_START = 1;
	private static final int S_GOOD = 10;
	private static final int S_BAD = 100;
	private static final int S_BREAK = 1000;
	private static final int S_FINISHED_GOOD = 10000;
	private static final int S_FINISHED_BAD = 10100;
	
	private IToken fDefaultToken; 
	private IToken fInvalidIdentifierToken;
	private Map<String, IToken> fSpecialWords;
	
	public RWordRule(IToken defaultToken, IToken invalidIdentifierToken) {
		fDefaultToken = defaultToken;
		fInvalidIdentifierToken = invalidIdentifierToken;
		fSpecialWords = new HashMap<String, IToken>();
	}
	
	public void addSpecialWords(String[] word, IToken token) {
		for (int i = 0; i < word.length; i++) {
			addSpecialWord(word[i], token);
		}
	}
	public void addSpecialWord(String word, IToken token) {
		fSpecialWords.put(word, token);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
	 */
	public IToken evaluate(ICharacterScanner scanner) {
		int status = S_START;
		int readed = 0;
		StringBuffer detectedPattern = null;
		
		while(status < S_BREAK) {
			int c = scanner.read();
			readed++;
			
			switch (status) {
				case S_START:
					if (RTokens.isLetter(c) || c == RTokens.PERIOD) {
						detectedPattern = new StringBuffer();
						detectedPattern.append((char)c);
						if (c == RTokens.PERIOD)
							status = S_PERIOD_START;
						else
							status = S_GOOD;
					} else
						status = S_BREAK;
					break;
					
				case S_PERIOD_START:
					if (RTokens.isLetter(c)) {
						detectedPattern.append((char)c);
						status = S_GOOD;
					} else
					if (c == RTokens.PERIOD)
						detectedPattern.append((char)c);
					else
						status = S_BREAK;
					break;

				case S_GOOD:
					if (RTokens.isLetter(c) || c == RTokens.PERIOD || RTokens.isDigit(c))
						detectedPattern.append((char)c);
					else
					if (RTokens.isSeparator(c))
						status = S_FINISHED_GOOD;
					else
						status = S_BAD;
					break;
				
				case S_BAD:
					if (RTokens.isSeparator(c))
						status = S_FINISHED_BAD;
					break;
			}
		}
		
		if (status >= S_FINISHED_GOOD) {
			scanner.unread();
			if (status == S_FINISHED_GOOD) {
				IToken t = (IToken) fSpecialWords.get(detectedPattern.toString());
				return (t != null)? t: fDefaultToken;
			} else
				return fInvalidIdentifierToken;
		}
						
		for (; readed > 0; readed--) {
			scanner.unread();
		}
		return Token.UNDEFINED;
	}
}