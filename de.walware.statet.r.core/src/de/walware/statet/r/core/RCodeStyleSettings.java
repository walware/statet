/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import java.util.concurrent.locks.Lock;

import de.walware.eclipsecommons.preferences.AbstractPreferencesModelObject;
import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.preferences.Preference.EnumPref;
import de.walware.eclipsecommons.preferences.Preference.IntPref;


/**
 * Settings for style of R code.
 */
public class RCodeStyleSettings extends AbstractPreferencesModelObject {
	
	public static final String CONTEXT_ID = "r.codestyle"; //$NON-NLS-1$

	public static enum IndentationType {
		TAB, SPACES,
	}
	public static final IntPref PREF_TAB_SIZE = new IntPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "tab.size"); //$NON-NLS-1$
	public static final String PROP_TAB_SIZE = "tabSize"; //$NON-NLS-1$
	
	private static final EnumPref<IndentationType> PREF_INDENT_DEFAULT_TYPE = new EnumPref<IndentationType>(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.default.type", IndentationType.class); //$NON-NLS-1$
	public static final String PROP_INDENT_DEFAULT_TYPE = "indentDefaultType"; //$NON-NLS-1$
	private static final IntPref PREF_INDENT_SPACES_COUNT = new IntPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.spaces.count"); //$NON-NLS-1$
	public static final String PROP_INDENT_SPACES_COUNT = "indentSpacesCount"; //$NON-NLS-1$
	
	
	private int fTabSize;
	private IndentationType fIndentationDefaultType;
	private int fIndentationSpacesCount;

	
	/**
	 * Creates an instance with default settings.
	 */
	public RCodeStyleSettings() {
		super(false);
		loadDefaults();
		resetDirty();
	}
	
	
	@Override
	public String[] getNodeQualifiers() {
		return new String[] { RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER };
	}
	
	@Override
	public synchronized void loadDefaults() {
		setTabSize(4);
		setIndentDefaultType(IndentationType.SPACES);
		setIndentSpacesCount(4);
	}
	
	@Override
	public synchronized void load(IPreferenceAccess prefs) {
		setTabSize(prefs.getPreferenceValue(PREF_TAB_SIZE));
		setIndentDefaultType(prefs.getPreferenceValue(PREF_INDENT_DEFAULT_TYPE));
		setIndentSpacesCount(prefs.getPreferenceValue(PREF_INDENT_SPACES_COUNT));
	}

	public void load(RCodeStyleSettings source) {
		Lock sourceLock = source.getReadLock();
		Lock writeLock = getWriteLock();
		try {
			sourceLock.lock();
			writeLock.lock();
			
			setTabSize(source.fTabSize);
			setIndentDefaultType(source.fIndentationDefaultType);
			setIndentSpacesCount(source.fIndentationSpacesCount);
		}
		finally {
			sourceLock.unlock();
			writeLock.unlock();
		}
	}

	@Override
	public synchronized Map<Preference, Object> deliverToPreferencesMap(Map<Preference, Object> map) {
		map.put(PREF_TAB_SIZE, getTabSize());
		map.put(PREF_INDENT_DEFAULT_TYPE, getIndentDefaultType());
		map.put(PREF_INDENT_SPACES_COUNT, getIndentSpacesCount());
		return map;
	}

	
/*-- Properties --------------------------------------------------------------*/
	
	public void setTabSize(int size) {
		int oldValue = fTabSize;
		fTabSize = size;
		firePropertyChange(PROP_TAB_SIZE, oldValue, fTabSize);
	}
	public int getTabSize() {
		return fTabSize;
	}

	public void setIndentDefaultType(IndentationType type) {
		IndentationType oldValue = fIndentationDefaultType;
		fIndentationDefaultType = type;
		firePropertyChange(PROP_INDENT_DEFAULT_TYPE, oldValue, fIndentationDefaultType);
	}
	public IndentationType getIndentDefaultType() {
		return fIndentationDefaultType;
	}

	public void setIndentSpacesCount(int count) {
		int oldValue = fIndentationSpacesCount;
		fIndentationSpacesCount = count;
		firePropertyChange(PROP_INDENT_SPACES_COUNT, oldValue, fIndentationSpacesCount);
	}
	public int getIndentSpacesCount() {
		return fIndentationSpacesCount;
	}

}
