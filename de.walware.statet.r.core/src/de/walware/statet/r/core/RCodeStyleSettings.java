/*******************************************************************************
 * Copyright (c) 2007 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core;

import java.util.Map;

import de.walware.eclipsecommons.preferences.AbstractPreferencesModelObject;
import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.preferences.Preference.IntPref;


/**
 * Settings for style of R code.
 */
public class RCodeStyleSettings extends AbstractPreferencesModelObject {
	
	public static final IntPref PREF_TAB_SIZE = new IntPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "tab_size"); //$NON-NLS-1$
	public static final String PROP_TAB_SIZE = "tabSize"; //$NON-NLS-1$
	
	/**
	 * @see RCorePreferenceNodes#CAT_R_CODESTYLE_PRESENTATION_QUALIFIER
	 */
	public static class Presentation {
		public static final IntPref PREF_TAB_SIZE = new IntPref(
				RCorePreferenceNodes.CAT_R_CODESTYLE_PRESENTATION_QUALIFIER, "tabWidth"); //$NON-NLS-1$
	}
	
	
	private int fTabSize;

	
	/**
	 * Creates an instance with default settings.
	 */
	public RCodeStyleSettings() {
		
		loadDefaults();
		resetDirty();
	}
	
	/**
	 * Creates an instance with settings from preferences.
	 */
	public RCodeStyleSettings(IPreferenceAccess prefs) {
		
		load(prefs);
		resetDirty();
	}

	@Override
	public String[] getNodeQualifiers() {
		
		return new String[] { RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER };
	}
	
	@Override
	public void loadDefaults() {
		
		setTabSize(4);
	}
	
	@Override
	public void load(IPreferenceAccess prefs) {
		
		setTabSize(prefs.getPreferenceValue(PREF_TAB_SIZE));
	}

	@Override
	public Map<Preference, Object> deliverToPreferencesMap(Map<Preference, Object> map) {
		
		map.put(PREF_TAB_SIZE, getTabSize());

		map.put(Presentation.PREF_TAB_SIZE, getTabSize());
		
		return map;
	}

	
/*-- Properties --------------------------------------------------------------*/
	
	public void setTabSize(int size) {
		
		int oldValue = fTabSize;
		fTabSize = size;
		firePropertyChange(PROP_TAB_SIZE, oldValue, size);
	}
	public int getTabSize() {
		
		return fTabSize;
	}
}
