/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core;

import java.util.Map;
import java.util.concurrent.locks.Lock;

import de.walware.ecommons.preferences.AbstractPreferencesModelObject;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.Preference.BooleanPref;
import de.walware.ecommons.preferences.Preference.EnumPref;
import de.walware.ecommons.preferences.Preference.IntPref;


/**
 * Settings for style of R code.
 */
public class RCodeStyleSettings extends AbstractPreferencesModelObject {
	
	public static final String INDENT_GROUP_ID = "r.codestyle.indent"; //$NON-NLS-1$
	public static final String WS_GROUP_ID = "r.codestyle.ws"; //$NON-NLS-1$
	
	public static final String[] ALL_GROUP_IDS = new String[] { INDENT_GROUP_ID, WS_GROUP_ID };
	
	
	public static enum IndentationType {
		TAB, SPACES,
	}
	
	public static final IntPref PREF_TAB_SIZE = new IntPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "tab.size"); //$NON-NLS-1$
	public static final String PROP_TAB_SIZE = "tabSize"; //$NON-NLS-1$
	
	public static final EnumPref<IndentationType> PREF_INDENT_DEFAULT_TYPE = new EnumPref<IndentationType>(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.default.type", IndentationType.class); //$NON-NLS-1$
	public static final String PROP_INDENT_DEFAULT_TYPE = "indentDefaultType"; //$NON-NLS-1$
	
	public static final IntPref PREF_INDENT_SPACES_COUNT = new IntPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.spaces.count"); //$NON-NLS-1$
	public static final String PROP_INDENT_SPACES_COUNT = "indentSpacesCount"; //$NON-NLS-1$
	
	public static final BooleanPref PREF_REPLACE_TABS_WITH_SPACES = new BooleanPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.replace_tabs.enable"); //$NON-NLS-1$
	public static final String PROP_REPLACE_TABS_WITH_SPACES = "replaceOtherTabsWithSpaces"; //$NON-NLS-1$
	
	public static final BooleanPref PREF_REPLACE_CONVERSATIVE = new BooleanPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.replace_strategy"); //$NON-NLS-1$
	public static final String PROP_REPLACE_CONVERSATIVE = "replaceConservative"; //$NON-NLS-1$
	
	public static final IntPref PREF_INDENT_BLOCK_DEPTH = new IntPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.block.depth"); //$NON-NLS-1$
	public static final String PROP_INDENT_BLOCK_DEPTH = "indentBlockDepth"; //$NON-NLS-1$
	
	public static final IntPref PREF_INDENT_GROUP_DEPTH = new IntPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.group.depth"); //$NON-NLS-1$
	public static final String PROP_INDENT_GROUP_DEPTH = "indentGroupDepth"; //$NON-NLS-1$
	
	public static final IntPref PREF_INDENT_WRAPPED_COMMAND_DEPTH = new IntPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.wrapped_command.depth"); //$NON-NLS-1$
	public static final String PROP_INDENT_WRAPPED_COMMAND_DEPTH = "indentWrappedCommandDepth"; //$NON-NLS-1$
	
	
	public static final BooleanPref PREF_WS_ARGASSIGN_BEFORE = new BooleanPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "ws.arg_assign.before"); //$NON-NLS-1$
	public static final String PROP_WS_ARGASSIGN_BEFORE = "whitespaceArgAssignBefore"; //$NON-NLS-1$
	
	public static final BooleanPref PREF_WS_ARGASSIGN_BEHIND = new BooleanPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "ws.arg_assign.behind"); //$NON-NLS-1$
	public static final String PROP_WS_ARGASSIGN_BEHIND = "whitespaceArgAssignBehind"; //$NON-NLS-1$
	
	public static final BooleanPref PREF_NL_FDEF_BODYBLOCK_BEFORE = new BooleanPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "nl.fdef_bodyblock.before"); //$NON-NLS-1$
	public static final String PROP_NL_FDEF_BODYBLOCK_BEFORE = "newlineFDefBodyBlockBefore"; //$NON-NLS-1$
	
	
	private boolean fEditMode;
	
	private int fTabSize;
	private IndentationType fIndentDefaultType;
	private int fIndentSpacesCount;
	private int fIndentBlockDepth;
	private int fIndentGroupDepth;
	private int fIndentWrappedCommandDepth;
	private boolean fReplaceOtherTabsWithSpaces;
	private boolean fReplaceConservative;
	
	private boolean fWSArgAssignBefore;
	private boolean fWSArgAssignBehind;
	private boolean fNLFDefBodyBlockBefore;
	
	
	/**
	 * Creates an instance with default settings.
	 */
	public RCodeStyleSettings(final boolean editMode) {
		fEditMode = editMode;
		if (!fEditMode) {
			installLock();
		}
		loadDefaults();
		resetDirty();
	}
	
	/**
	 * Creates an instance with default settings.
	 */
	public RCodeStyleSettings() {
		this(false);
	}
	
	
	@Override
	public String[] getNodeQualifiers() {
		return new String[] { RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER };
	}
	
	@Override
	public void loadDefaults() {
		setTabSize(4);
		setIndentDefaultType(IndentationType.TAB);
		setIndentSpacesCount(4);
		setIndentBlockDepth(1);
		setIndentGroupDepth(1);
		setIndentWrappedCommandDepth(2);
		setReplaceOtherTabsWithSpaces(false);
		setReplaceConservative(false);
		
		setWhitespaceArgAssignBefore(true);
		setWhitespaceArgAssignBehind(true);
		setNewlineFDefBodyBlockBefore(false);
	}
	
	@Override
	public void load(final IPreferenceAccess prefs) {
		setTabSize(prefs.getPreferenceValue(PREF_TAB_SIZE));
		setIndentDefaultType(prefs.getPreferenceValue(PREF_INDENT_DEFAULT_TYPE));
		setIndentSpacesCount(prefs.getPreferenceValue(PREF_INDENT_SPACES_COUNT));
		setReplaceOtherTabsWithSpaces(prefs.getPreferenceValue(PREF_REPLACE_TABS_WITH_SPACES));
		setIndentBlockDepth(prefs.getPreferenceValue(PREF_INDENT_BLOCK_DEPTH));
		setIndentGroupDepth(prefs.getPreferenceValue(PREF_INDENT_GROUP_DEPTH));
		setIndentWrappedCommandDepth(prefs.getPreferenceValue(PREF_INDENT_WRAPPED_COMMAND_DEPTH));
		setReplaceConservative(prefs.getPreferenceValue(PREF_REPLACE_CONVERSATIVE));
		
		setWhitespaceArgAssignBefore(prefs.getPreferenceValue(PREF_WS_ARGASSIGN_BEFORE));
		setWhitespaceArgAssignBehind(prefs.getPreferenceValue(PREF_WS_ARGASSIGN_BEHIND));
		setNewlineFDefBodyBlockBefore(prefs.getPreferenceValue(PREF_NL_FDEF_BODYBLOCK_BEFORE));
	}
	
	public void load(final RCodeStyleSettings source) {
		final Lock writeLock = getWriteLock();
		final Lock sourceLock = source.getReadLock();
		try {
			sourceLock.lock();
			writeLock.lock();
			
			setTabSize(source.fTabSize);
			setIndentDefaultType(source.fIndentDefaultType);
			setIndentSpacesCount(source.fIndentSpacesCount);
			setReplaceOtherTabsWithSpaces(source.fReplaceOtherTabsWithSpaces);
			setIndentBlockDepth(source.fIndentBlockDepth);
			setIndentGroupDepth(source.fIndentGroupDepth);
			setIndentWrappedCommandDepth(source.fIndentWrappedCommandDepth);
			setReplaceConservative(source.fReplaceConservative);
			
			setWhitespaceArgAssignBefore(source.fWSArgAssignBefore);
			setWhitespaceArgAssignBehind(source.fWSArgAssignBehind);
			setNewlineFDefBodyBlockBefore(source.fNLFDefBodyBlockBefore);
		}
		finally {
			sourceLock.unlock();
			writeLock.unlock();
		}
	}
	
	@Override
	public Map<Preference, Object> deliverToPreferencesMap(final Map<Preference, Object> map) {
		map.put(PREF_TAB_SIZE, getTabSize());
		map.put(PREF_INDENT_DEFAULT_TYPE, getIndentDefaultType());
		map.put(PREF_INDENT_SPACES_COUNT, getIndentSpacesCount());
		map.put(PREF_REPLACE_TABS_WITH_SPACES, getReplaceOtherTabsWithSpaces());
		map.put(PREF_INDENT_BLOCK_DEPTH, getIndentBlockDepth());
		map.put(PREF_INDENT_GROUP_DEPTH, getIndentGroupDepth());
		map.put(PREF_INDENT_WRAPPED_COMMAND_DEPTH, getIndentWrappedCommandDepth());
		map.put(PREF_REPLACE_CONVERSATIVE, getReplaceConservative());
		
		map.put(PREF_WS_ARGASSIGN_BEFORE, getWhitespaceArgAssignBefore());
		map.put(PREF_WS_ARGASSIGN_BEHIND, getWhitespaceArgAssignBehind());
		map.put(PREF_NL_FDEF_BODYBLOCK_BEFORE, getNewlineFDefBodyBlockBefore());
		return map;
	}
	
	
/*-- Properties --------------------------------------------------------------*/
	
	public void setTabSize(final int size) {
		final int oldValue = fTabSize;
		fTabSize = size;
		firePropertyChange(PROP_TAB_SIZE, oldValue, size);
	}
	public int getTabSize() {
		return fTabSize;
	}
	
	public void setIndentDefaultType(final IndentationType type) {
		final IndentationType oldValue = fIndentDefaultType;
		fIndentDefaultType = type;
		firePropertyChange(PROP_INDENT_DEFAULT_TYPE, oldValue, type);
	}
	public IndentationType getIndentDefaultType() {
		return fIndentDefaultType;
	}
	
	public void setIndentSpacesCount(final int count) {
		final int oldValue = fIndentSpacesCount;
		fIndentSpacesCount = count;
		firePropertyChange(PROP_INDENT_SPACES_COUNT, oldValue, count);
	}
	public int getIndentSpacesCount() {
		return fIndentSpacesCount;
	}
	
	public final void setIndentBlockDepth(final int depth) {
		final int oldValue = fIndentBlockDepth;
		fIndentBlockDepth = depth;
		firePropertyChange(PROP_INDENT_BLOCK_DEPTH, oldValue, depth);
	}
	public final int getIndentBlockDepth() {
		return fIndentBlockDepth;
	}
	
	public final void setIndentGroupDepth(final int depth) {
		final int oldValue = fIndentGroupDepth;
		fIndentGroupDepth = depth;
		firePropertyChange(PROP_INDENT_GROUP_DEPTH, oldValue, depth);
	}
	public final int getIndentGroupDepth() {
		return fIndentGroupDepth;
	}
	
	public final void setIndentWrappedCommandDepth(final int depth) {
		final int oldValue = fIndentWrappedCommandDepth;
		fIndentWrappedCommandDepth = depth;
		firePropertyChange(PROP_INDENT_WRAPPED_COMMAND_DEPTH, oldValue, depth);
	}
	public final int getIndentWrappedCommandDepth() {
		return fIndentWrappedCommandDepth;
	}
	
	public void setReplaceOtherTabsWithSpaces(final boolean enable) {
		final boolean oldValue = fReplaceOtherTabsWithSpaces;
		fReplaceOtherTabsWithSpaces = enable;
		firePropertyChange(PROP_REPLACE_TABS_WITH_SPACES, oldValue, getReplaceOtherTabsWithSpaces());
	}
	public boolean getReplaceOtherTabsWithSpaces() {
		if (fEditMode) {
			return fReplaceOtherTabsWithSpaces;
		}
		return (fIndentDefaultType == IndentationType.SPACES) && fReplaceOtherTabsWithSpaces;
	}
	
	public void setReplaceConservative(final boolean enable) {
		final boolean oldValue = fReplaceConservative;
		fReplaceConservative = enable;
		firePropertyChange(PROP_REPLACE_CONVERSATIVE, oldValue, enable);
	}
	public boolean getReplaceConservative() {
		return fReplaceConservative;
	}
	
	
	public void setWhitespaceArgAssignBefore(final boolean enable) {
		final boolean oldValue = fWSArgAssignBefore;
		fWSArgAssignBefore = enable;
		firePropertyChange(PROP_WS_ARGASSIGN_BEFORE, oldValue, enable);
	}
	public boolean getWhitespaceArgAssignBefore() {
		return fWSArgAssignBefore;
	}
	
	public void setWhitespaceArgAssignBehind(final boolean enable) {
		final boolean oldValue = fWSArgAssignBehind;
		fWSArgAssignBehind = enable;
		firePropertyChange(PROP_WS_ARGASSIGN_BEFORE, oldValue, enable);
	}
	public boolean getWhitespaceArgAssignBehind() {
		return fWSArgAssignBehind;
	}
	
	public void setNewlineFDefBodyBlockBefore(final boolean enable) {
		final boolean oldValue = fNLFDefBodyBlockBefore;
		fNLFDefBodyBlockBefore = enable;
		firePropertyChange(PROP_NL_FDEF_BODYBLOCK_BEFORE, oldValue, enable);
	}
	public boolean getNewlineFDefBodyBlockBefore() {
		return fNLFDefBodyBlockBefore;
	}
	
	
	public String getArgAssignString() {
		return (fWSArgAssignBefore ?
				(fWSArgAssignBehind ? " = " : " =") :
				(fWSArgAssignBefore ? "= " : "=") ); 
	}
	
}
