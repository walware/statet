/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.preferences;

import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


public abstract class OverlayStoreConfigurationBlock extends AbstractConfigurationBlock {
	
	
	protected OverlayPreferenceStore fOverlayStore;
	private boolean fIsDirty;
	private boolean fInLoading;
	
	
	public OverlayStoreConfigurationBlock() {
		super();
	}
	
	
	protected void setupOverlayStore(final IPreferenceStore store, final OverlayStorePreference[] keys) {
		fOverlayStore = new OverlayPreferenceStore(store, keys);
		fOverlayStore.load();
		fOverlayStore.start();
		fOverlayStore.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent event) {
				if (!fInLoading) {
					fIsDirty = true;
					handlePropertyChange();
				}
			}
		});
		fIsDirty = false;
	}
	
	protected abstract Set<String> getChangedGroups();
	
	protected void handlePropertyChange() {
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
		if (fOverlayStore != null && fIsDirty) {
			fOverlayStore.propagate();
			fIsDirty = false;
			scheduleChangeNotification(getChangedGroups(), true);
		}
	}
	
	@Override
	public boolean performOk() {
		if (fOverlayStore != null && fIsDirty) {
			fOverlayStore.propagate();
			fIsDirty = false;
			scheduleChangeNotification(getChangedGroups(), false);
			return true;
		}
		return true;
	}
	
	@Override
	public void performDefaults() {
		if (fOverlayStore != null) {
			fInLoading = true;
			fOverlayStore.loadDefaults();
			fInLoading = false;
			fIsDirty = true;
			handlePropertyChange();
			updateControls();
		}
	}
	
	protected void updateControls() {
	}
	
}
