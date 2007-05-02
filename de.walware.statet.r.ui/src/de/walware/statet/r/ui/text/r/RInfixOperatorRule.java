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

package de.walware.statet.r.ui.text.r;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import de.walware.statet.r.core.rlang.RTokens;


/**
 * Rule to find user-defined infix operators.
 * 
 * R-Version: 2.5.0
 */
public class RInfixOperatorRule implements IRule {

	private IToken fDefaultOpToken;
	private IToken fPredefinedOpToken;
	private IToken fInvalidOpToken;
	
	private StringBuilder fBuffer;
	private Map<String, IToken> fSpecialOperators;
	
	
	public RInfixOperatorRule(IToken userDefinedOpToken, IToken invalidOpToken, IToken predefinedOpToken) {
		
		fDefaultOpToken = userDefinedOpToken;
		fPredefinedOpToken = predefinedOpToken;
		fInvalidOpToken = invalidOpToken;
		
		fBuffer = new StringBuilder();
		fSpecialOperators = new HashMap<String, IToken>();
		
		for (String op : RTokens.PREDIFINED_INFIX_OPERATORS) {
			fSpecialOperators.put(op, fPredefinedOpToken);
		}
	}
	
	public IToken evaluate(ICharacterScanner scanner) {
		
		int c = scanner.read();
		if (c == '%') {
			fBuffer.append('%');
			while (true) {
				c = scanner.read();
				switch (c) {
				case '%':
					fBuffer.append('%');
					return succeed();
				case '\n':
				case '\r':
				case ICharacterScanner.EOF:
					scanner.unread();
					fBuffer.setLength(0);
					return fInvalidOpToken;
				default:
					fBuffer.append((char) c);
				}
			}
		}
		else {
			scanner.unread();
			return Token.UNDEFINED;
		}
	}
	
	private IToken succeed() {
		
		IToken token = fSpecialOperators.get(fBuffer.toString());
		fBuffer.setLength(0);
		return (token != null) ? token : fDefaultOpToken;
	}
}
