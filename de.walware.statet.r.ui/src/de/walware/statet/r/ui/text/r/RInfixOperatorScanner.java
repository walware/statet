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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;

import de.walware.eclipsecommons.ui.util.ColorManager;

import de.walware.statet.ext.ui.text.StatextTextScanner;


/**
 * Scanner for infix-operators.
 */
public class RInfixOperatorScanner extends StatextTextScanner {

	
	/**
	 * @param colorManager
	 * @param preferenceStore
	 */
	public RInfixOperatorScanner(ColorManager colorManager, IPreferenceStore preferenceStore) {
		super(colorManager, preferenceStore);
		
		initialize();
	}

	@Override
	protected List<IRule> createRules() {
		List<IRule> list = new ArrayList<IRule>();
		IToken predefinedOpToken = getToken(IRTextTokens.OTHER_OPERATORS);
		IToken userdefinedOpToken = getToken(IRTextTokens.OTHER_OPERATORS); // TODO: add text token for user-defined operators
		IToken invalidOpToken = getToken(IRTextTokens.UNDEFINED);
		list.add(new RInfixOperatorRule(userdefinedOpToken, invalidOpToken, predefinedOpToken));
		return list;
	}

}
