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

import java.util.List;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;

import de.walware.ecommons.text.ui.presentation.AbstractRuleBasedScanner;
import de.walware.ecommons.text.ui.settings.TextStyleManager;


/**
 * Scanner for infix-operators.
 */
public class RInfixOperatorScanner extends AbstractRuleBasedScanner {
	
	
	public RInfixOperatorScanner(final TextStyleManager textStyles) {
		super(textStyles);
		
		initRules();
	}
	
	
	@Override
	protected void createRules(final List<IRule> rules) {
		final IToken predefinedOpToken= getToken(IRTextTokens.OP_KEY);
		final IToken userdefinedOpToken= getToken(IRTextTokens.OP_SUB_USERDEFINED_KEY);
		final IToken invalidOpToken= getToken(IRTextTokens.UNDEFINED_KEY);
		
		rules.add(new RInfixOperatorRule(userdefinedOpToken, invalidOpToken, predefinedOpToken));
	}
	
}
