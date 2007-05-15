/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;


public class OverlayStoreConfigurationBlock extends AbstractConfigurationBlock {

	
	protected OverlayPreferenceStore fOverlayStore;
		
	
	public OverlayStoreConfigurationBlock() {
		super();
	}

	protected void setupOverlayStore(IPreferenceStore store, OverlayStorePreference[] keys) {
		fOverlayStore = new OverlayPreferenceStore(store, keys);
		fOverlayStore.load();
		fOverlayStore.start();
	}
	
	@Override
	public void dispose() {
		if (fOverlayStore != null) {
			fOverlayStore.stop();
			fOverlayStore= null;
		}
		super.dispose();
	}
	
	
	@Override
	public void performApply() {
		performOk();
	}
	
	@Override
	public boolean performOk() {
		if (fOverlayStore != null) {
			fOverlayStore.propagate();
			return true;
		}
		return false;
	}

	public void performDefaults() {
		if (fOverlayStore != null) {
			fOverlayStore.loadDefaults();
			updateControls();
		}
	}

	protected void updateControls() {
	}
	
}
