/*******************************************************************************
 * Copyright (c) 2005-2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.jface.preference.IPreferenceStore;

import de.walware.statet.base.core.preferences.StatetCorePreferenceNodes;
import de.walware.statet.ui.StatetUiPreferenceConstants;


public class StatetPreferenceInitializer extends AbstractPreferenceInitializer {


	@Override
	public void initializeDefaultPreferences() {
		
		// Core
		DefaultScope defaultScope = new DefaultScope();
		StatetCorePreferenceNodes.initializeDefaultValues(defaultScope);

		// UI
		IPreferenceStore store = StatetPlugin.getDefault().getPreferenceStore();
		StatetUiPreferenceConstants.initializeDefaultValues(store);
	}

}