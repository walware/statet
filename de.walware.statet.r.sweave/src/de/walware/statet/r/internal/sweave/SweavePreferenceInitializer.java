/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;

import de.walware.ecommons.preferences.PreferencesUtil;

import de.walware.statet.r.internal.sweave.editors.SweaveEditorOptions;


public class SweavePreferenceInitializer extends AbstractPreferenceInitializer {
	
	
	public SweavePreferenceInitializer() {
	}
	
	
	@Override
	public void initializeDefaultPreferences() {
		final DefaultScope defaults = new DefaultScope();
		PreferencesUtil.setPrefValue(defaults, SweaveEditorOptions.PREF_SPELLCHECKING_ENABLED, false);
	}
	
}
