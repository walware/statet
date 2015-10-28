/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

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
	
	
	private final IToken fDefaultOpToken;
	private final IToken fPredefinedOpToken;
	private final IToken fInvalidOpToken;
	
	private final StringBuilder fBuffer;
	private final Map<String, IToken> fSpecialOperators;
	
	
	public RInfixOperatorRule(final IToken userDefinedOpToken, final IToken invalidOpToken, final IToken predefinedOpToken) {
		fDefaultOpToken = userDefinedOpToken;
		fPredefinedOpToken = predefinedOpToken;
		fInvalidOpToken = invalidOpToken;
		
		fBuffer = new StringBuilder();
		fSpecialOperators= new HashMap<>();
		
		for (final String op : RTokens.PREDIFINED_INFIX_OPERATORS) {
			fSpecialOperators.put(op, fPredefinedOpToken);
		}
	}
	
	@Override
	public IToken evaluate(final ICharacterScanner scanner) {
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
		final IToken token = fSpecialOperators.get(fBuffer.toString());
		fBuffer.setLength(0);
		return (token != null) ? token : fDefaultOpToken;
	}
	
}
