/*******************************************************************************
 * Copyright (c) 2005-2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.text;


import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;

import de.walware.eclipsecommons.ui.util.ColorManager;


/**
 * Single token scanner.
 */
public class SingleTokenScanner extends StatextTextScanner {

	private String fDefaultTokenKey;
	
	public SingleTokenScanner (ColorManager colorManager, IPreferenceStore preferenceStore, 
			String stylesContext, String defaultTokenKey) {

		super(colorManager, preferenceStore, stylesContext);
		
		fDefaultTokenKey = defaultTokenKey;
		initialize();
	}

	@Override
	protected List<IRule> createRules() {
		IToken defaultToken = getToken(fDefaultTokenKey);
		setDefaultReturnToken(defaultToken);
		return null;
	}
}
