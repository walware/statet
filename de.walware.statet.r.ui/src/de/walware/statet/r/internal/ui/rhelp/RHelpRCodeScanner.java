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

package de.walware.statet.r.internal.ui.rhelp;

import org.eclipse.jface.preference.IPreferenceStore;

import de.walware.ecommons.text.HtmlParseInput;
import de.walware.ecommons.text.ui.settings.CssTextStyleManager;

import de.walware.statet.r.ui.RUIPreferenceConstants;
import de.walware.statet.r.ui.text.r.RCodeScanner2;


public class RHelpRCodeScanner extends RCodeScanner2 {
	
	
	public RHelpRCodeScanner(final IPreferenceStore preferenceStore) {
		super(new RTokenScannerLexer(),
				new CssTextStyleManager(preferenceStore, RUIPreferenceConstants.R.TS_GROUP_ID,
						RUIPreferenceConstants.R.TS_DEFAULT_ROOT ) );
	}
	
	
	public void setCode(final String html) {
		getLexer().reset(new HtmlParseInput(html));
	}
	
	public String getDefaultStyle() {
		return (String) getTextStyleManager().getToken(null).getData();
	}
	
}
