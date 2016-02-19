/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.rhelp;

import de.walware.ecommons.preferences.core.Preference;
import de.walware.ecommons.preferences.core.Preference.BooleanPref;
import de.walware.ecommons.preferences.core.Preference.IntPref;
import de.walware.ecommons.preferences.core.Preference.StringPref;

import de.walware.statet.r.ui.RUI;


public class RHelpPreferences {
	
	
	public static final String RHELP_QUALIFIER = RUI.PLUGIN_ID + "/rhelpview"; //$NON-NLS-1$
	
	
	public static final String HOMEPAGE_URL_KEY = "homepage.url"; //$NON-NLS-1$
	
	public static final String SEARCH_REUSE_PAGE_ENABLED_KEY = "search.reuse_page.enabled"; //$NON-NLS-1$
	public static final String SEARCH_PREVIEW_FRAGMENTS_MAX_KEY = "search.preview_fragments.max"; //$NON-NLS-1$
	
	public static final Preference<String> HOMEPAGE_URL_PREF = new StringPref(
			RHELP_QUALIFIER, HOMEPAGE_URL_KEY);
	
	public static final Preference<Boolean> SEARCH_REUSE_PAGE_ENABLED_PREF = new BooleanPref(
			RHELP_QUALIFIER, SEARCH_REUSE_PAGE_ENABLED_KEY);
	
	public static final Preference<Integer> SEARCH_PREVIEW_FRAGMENTS_MAX_PREF = new IntPref(
			RHELP_QUALIFIER, SEARCH_PREVIEW_FRAGMENTS_MAX_KEY);
	
}
