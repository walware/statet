/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.text.r;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;

import de.walware.ecommons.ui.text.presentation.AbstractRuleBasedScanner;
import de.walware.ecommons.ui.util.ColorManager;

import de.walware.statet.r.ui.RUIPreferenceConstants;


/**
 * Scanner for infix-operators.
 */
public class RInfixOperatorScanner extends AbstractRuleBasedScanner {
	
	
	/**
	 * @param colorManager
	 * @param preferenceStore
	 */
	public RInfixOperatorScanner(final ColorManager colorManager, final IPreferenceStore preferenceStore) {
		super(colorManager, preferenceStore, RUIPreferenceConstants.R.TS_GROUP_ID);
		
		initialize();
	}
	
	
	@Override
	protected List<IRule> createRules() {
		final List<IRule> list = new ArrayList<IRule>();
		final IToken predefinedOpToken = getToken(IRTextTokens.OP_KEY);
		final IToken userdefinedOpToken = getToken(IRTextTokens.OP_SUB_USERDEFINED_KEY);
		final IToken invalidOpToken = getToken(IRTextTokens.UNDEFINED_KEY);
		list.add(new RInfixOperatorRule(userdefinedOpToken, invalidOpToken, predefinedOpToken));
		return list;
	}
	
}
