/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui;

import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import de.walware.eclipsecommons.UpdateableHandler;
import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.preferences.PreferencesUtil;


/**
 * Handler toggling and caching the state of a boolean preference value.
 */
public class TogglePreferenceEnablementHandler extends UpdateableHandler implements IElementUpdater, IPreferenceChangeListener {
	
	
	private Preference<Boolean> fPreferenceKey;
	private IPreferenceAccess fPrefAccess;
	private boolean fPreferenceIsEnabled;
	
	private String fCommandId;
	
	
	public TogglePreferenceEnablementHandler(final Preference<Boolean> pref, final String commandId) {
		this(pref, PreferencesUtil.getInstancePrefs(), commandId);
	}
	
	public TogglePreferenceEnablementHandler(final Preference<Boolean> pref, final IPreferenceAccess access, final String commandId) {
		super(true);
		fPreferenceKey = pref;
		fPrefAccess = access;
		fCommandId = commandId;
		fPrefAccess.addPreferenceNodeListener(fPreferenceKey.getQualifier(), this);
		fPreferenceIsEnabled = fPrefAccess.getPreferenceValue(fPreferenceKey);
	}
	
	
	public void preferenceChange(final PreferenceChangeEvent event) {
		if (fPreferenceKey.getKey().equals(event.getKey())) {
			final Boolean newValue = fPrefAccess.getPreferenceValue(fPreferenceKey);
			if (newValue == null) {
				return;
			}
			final boolean newEnablement = newValue.booleanValue();
			if (newEnablement != fPreferenceIsEnabled) {
				fPreferenceIsEnabled = newEnablement;
				final ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
				if (fCommandId != null && commandService != null) {
					commandService.refreshElements(fCommandId, null);
				}
			}
		}
	}
	
	@Override
	public void dispose() {
		fPrefAccess.removePreferenceNodeListener(fPreferenceKey.getQualifier(), this);
		super.dispose();
	}
	
	public void updateElement(final UIElement element, final Map parameters) {
		element.setChecked(fPreferenceIsEnabled);
	}
	
	@Override
	public Object execute(final ExecutionEvent arg0) throws ExecutionException {
		PreferencesUtil.setPrefValue(fPrefAccess.getPreferenceContexts()[0], fPreferenceKey, !fPreferenceIsEnabled);
		return null;
	}
	
}
