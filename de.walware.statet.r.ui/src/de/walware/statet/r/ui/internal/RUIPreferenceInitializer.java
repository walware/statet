/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.editors.text.EditorsUI;

import de.walware.statet.r.internal.debug.RDebugPreferenceConstants;
import de.walware.statet.r.ui.RUIPreferenceConstants;


public class RUIPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = RUIPlugin.getDefault().getPreferenceStore();
		EditorsUI.useAnnotationsPreferencePage(store);
		EditorsUI.useQuickDiffPreferencePage(store);
		RUIPreferenceConstants.initializeDefaultValues(store);
		
		DefaultScope defaultScope = new DefaultScope();
		RDebugPreferenceConstants.initializeDefaultValues(defaultScope);
	}

}