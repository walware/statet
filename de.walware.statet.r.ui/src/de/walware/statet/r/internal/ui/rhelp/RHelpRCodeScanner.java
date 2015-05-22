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

import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;

import de.walware.ecommons.text.core.util.HtmlStripParserInput;
import de.walware.ecommons.text.ui.settings.CssTextStyleManager;

import de.walware.statet.r.ui.RUIPreferenceConstants;
import de.walware.statet.r.ui.text.r.RDefaultTextStyleScanner;


public class RHelpRCodeScanner extends RDefaultTextStyleScanner {
	
	
	public RHelpRCodeScanner(final IPreferenceStore preferenceStore) {
		super(new CssTextStyleManager(preferenceStore,
				RUIPreferenceConstants.R.TS_GROUP_ID, RUIPreferenceConstants.R.TS_DEFAULT_ROOT ));
	}
	
	
	public void setHtml(final String html) {
		getLexer().reset(new HtmlStripParserInput(html).init());
	}
	
	public String getDefaultStyle() {
		return (String) getTextStyles().getToken(null).getData();
	}
	
	@Override
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
		getTextStyles().handleSettingsChanged(groupIds, options);
		super.handleSettingsChanged(groupIds, options);
	}
	
}
