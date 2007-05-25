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

import org.eclipse.jface.preference.IPreferenceStore;

import de.walware.eclipsecommons.ui.util.ColorManager;

import de.walware.statet.ext.ui.text.SingleTokenScanner;
import de.walware.statet.r.ui.RUIPreferenceConstants;


/**
 * Scanner for R strings
 */
public class RStringScanner extends SingleTokenScanner {

	public RStringScanner(ColorManager colorManager, IPreferenceStore preferenceStore) {
		super(colorManager, preferenceStore, 
				RUIPreferenceConstants.R.CONTEXT_ID, IRTextTokens.STRING);
	}

}
