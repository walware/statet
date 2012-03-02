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

package de.walware.statet.r.core;

import java.util.Map;
import java.util.concurrent.locks.Lock;

import de.walware.ecommons.preferences.AbstractPreferencesModelObject;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.Preference.BooleanPref;
import de.walware.ecommons.preferences.Preference.EnumPref;
import de.walware.ecommons.preferences.Preference.IntPref;
import de.walware.ecommons.text.IIndentSettings;


/**
 * Settings for style of R code.
 */
public class RCodeStyleSettings extends AbstractPreferencesModelObject
		implements IIndentSettings {
	
	public static final String INDENT_GROUP_ID = "r/r.codestyle/indent"; //$NON-NLS-1$
	public static final String WS_GROUP_ID = "r/r.codestyle/ws"; //$NON-NLS-1$
	
	public static final String[] ALL_GROUP_IDS = new String[] { INDENT_GROUP_ID, WS_GROUP_ID };
	
	
	public static final IntPref TAB_SIZE_PREF = new IntPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "tab.size"); //$NON-NLS-1$
	
	public static final EnumPref<IndentationType> INDENT_DEFAULT_TYPE_PREF = new EnumPref<IndentationType>(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.default.type", IndentationType.class); //$NON-NLS-1$
	
	public static final IntPref INDENT_SPACES_COUNT_PREF = new IntPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.spaces.count"); //$NON-NLS-1$
	
	public static final BooleanPref REPLACE_TABS_WITH_SPACES_PREF = new BooleanPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.replace_tabs.enable"); //$NON-NLS-1$
	
	public static final BooleanPref REPLACE_CONVERSATIVE_PREF = new BooleanPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.replace_strategy"); //$NON-NLS-1$
	
	
	public static final IntPref INDENT_BLOCK_DEPTH_PREF = new IntPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.block.depth"); //$NON-NLS-1$
	public static final String INDENT_BLOCK_DEPTH_PROP = "indentBlockDepth"; //$NON-NLS-1$
	
	public static final IntPref INDENT_GROUP_DEPTH_PREF = new IntPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.group.depth"); //$NON-NLS-1$
	public static final String INDENT_GROUP_DEPTH_PROP = "indentGroupDepth"; //$NON-NLS-1$
	
	public static final IntPref INDENT_WRAPPED_COMMAND_DEPTH_PREF = new IntPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "indent.wrapped_command.depth"); //$NON-NLS-1$
	public static final String INDENT_WRAPPED_COMMAND_DEPTH_PROP = "indentWrappedCommandDepth"; //$NON-NLS-1$
	
	
	public static final BooleanPref WS_ARGASSIGN_BEFORE_PREF = new BooleanPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "ws.arg_assign.before"); //$NON-NLS-1$
	public static final String WS_ARGASSIGN_BEFORE_PROP = "whitespaceArgAssignBefore"; //$NON-NLS-1$
	
	public static final BooleanPref WS_ARGASSIGN_BEHIND_PREF = new BooleanPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "ws.arg_assign.behind"); //$NON-NLS-1$
	public static final String WS_ARGASSIGN_BEHIND_PROP = "whitespaceArgAssignBehind"; //$NON-NLS-1$
	
	public static final BooleanPref NL_FDEF_BODYBLOCK_BEFORE_PREF = new BooleanPref(
			RCorePreferenceNodes.CAT_R_CODESTYLE_QUALIFIER, "nl.fdef_bodyblock.before"); //$NON-NLS-1$
	public static final String NL_FDEF_BODYBLOCK_BEFOREP_PROP = "newlineFDefBodyBlockBefore"; //$NON-NLS-1$
	
	
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
	public RCodeStyleSettings(final int mode) {
		if (mode >= 1) {
			installLock();
		}
		loadDefaults();
		resetDirty();
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
		setTabSize(prefs.getPreferenceValue(TAB_SIZE_PREF));
		setIndentDefaultType(prefs.getPreferenceValue(INDENT_DEFAULT_TYPE_PREF));
		setIndentSpacesCount(prefs.getPreferenceValue(INDENT_SPACES_COUNT_PREF));
		setReplaceOtherTabsWithSpaces(prefs.getPreferenceValue(REPLACE_TABS_WITH_SPACES_PREF));
		setIndentBlockDepth(prefs.getPreferenceValue(INDENT_BLOCK_DEPTH_PREF));
		setIndentGroupDepth(prefs.getPreferenceValue(INDENT_GROUP_DEPTH_PREF));
		setIndentWrappedCommandDepth(prefs.getPreferenceValue(INDENT_WRAPPED_COMMAND_DEPTH_PREF));
		setReplaceConservative(prefs.getPreferenceValue(REPLACE_CONVERSATIVE_PREF));
		
		setWhitespaceArgAssignBefore(prefs.getPreferenceValue(WS_ARGASSIGN_BEFORE_PREF));
		setWhitespaceArgAssignBehind(prefs.getPreferenceValue(WS_ARGASSIGN_BEHIND_PREF));
		setNewlineFDefBodyBlockBefore(prefs.getPreferenceValue(NL_FDEF_BODYBLOCK_BEFORE_PREF));
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
	public Map<Preference<?>, Object> deliverToPreferencesMap(final Map<Preference<?>, Object> map) {
		map.put(TAB_SIZE_PREF, getTabSize());
		map.put(INDENT_DEFAULT_TYPE_PREF, getIndentDefaultType());
		map.put(INDENT_SPACES_COUNT_PREF, getIndentSpacesCount());
		map.put(REPLACE_TABS_WITH_SPACES_PREF, getReplaceOtherTabsWithSpaces());
		map.put(INDENT_BLOCK_DEPTH_PREF, getIndentBlockDepth());
		map.put(INDENT_GROUP_DEPTH_PREF, getIndentGroupDepth());
		map.put(INDENT_WRAPPED_COMMAND_DEPTH_PREF, getIndentWrappedCommandDepth());
		map.put(REPLACE_CONVERSATIVE_PREF, getReplaceConservative());
		
		map.put(WS_ARGASSIGN_BEFORE_PREF, getWhitespaceArgAssignBefore());
		map.put(WS_ARGASSIGN_BEHIND_PREF, getWhitespaceArgAssignBehind());
		map.put(NL_FDEF_BODYBLOCK_BEFORE_PREF, getNewlineFDefBodyBlockBefore());
		return map;
	}
	
	
/*-- Properties --------------------------------------------------------------*/
	
	public void setTabSize(final int size) {
		final int oldValue = fTabSize;
		fTabSize = size;
		firePropertyChange(TAB_SIZE_PROP, oldValue, size);
	}
	@Override
	public int getTabSize() {
		return fTabSize;
	}
	
	public void setIndentDefaultType(final IndentationType type) {
		final IndentationType oldValue = fIndentDefaultType;
		fIndentDefaultType = type;
		firePropertyChange(INDENT_DEFAULT_TYPE_PROP, oldValue, type);
	}
	@Override
	public IndentationType getIndentDefaultType() {
		return fIndentDefaultType;
	}
	
	public void setIndentSpacesCount(final int count) {
		final int oldValue = fIndentSpacesCount;
		fIndentSpacesCount = count;
		firePropertyChange(INDENT_SPACES_COUNT_PROP, oldValue, count);
	}
	@Override
	public int getIndentSpacesCount() {
		return fIndentSpacesCount;
	}
	
	public final void setIndentBlockDepth(final int depth) {
		final int oldValue = fIndentBlockDepth;
		fIndentBlockDepth = depth;
		firePropertyChange(INDENT_BLOCK_DEPTH_PROP, oldValue, depth);
	}
	public final int getIndentBlockDepth() {
		return fIndentBlockDepth;
	}
	
	public final void setIndentGroupDepth(final int depth) {
		final int oldValue = fIndentGroupDepth;
		fIndentGroupDepth = depth;
		firePropertyChange(INDENT_GROUP_DEPTH_PROP, oldValue, depth);
	}
	public final int getIndentGroupDepth() {
		return fIndentGroupDepth;
	}
	
	public final void setIndentWrappedCommandDepth(final int depth) {
		final int oldValue = fIndentWrappedCommandDepth;
		fIndentWrappedCommandDepth = depth;
		firePropertyChange(INDENT_WRAPPED_COMMAND_DEPTH_PROP, oldValue, depth);
	}
	public final int getIndentWrappedCommandDepth() {
		return fIndentWrappedCommandDepth;
	}
	
	public void setReplaceOtherTabsWithSpaces(final boolean enable) {
		final boolean oldValue = fReplaceOtherTabsWithSpaces;
		fReplaceOtherTabsWithSpaces = enable;
		firePropertyChange(REPLACE_TABS_WITH_SPACES_PROP, oldValue, getReplaceOtherTabsWithSpaces());
	}
	@Override
	public boolean getReplaceOtherTabsWithSpaces() {
		return fReplaceOtherTabsWithSpaces;
	}
	
	public void setReplaceConservative(final boolean enable) {
		final boolean oldValue = fReplaceConservative;
		fReplaceConservative = enable;
		firePropertyChange(REPLACE_CONSERVATIVE_PROP, oldValue, enable);
	}
	@Override
	public boolean getReplaceConservative() {
		return fReplaceConservative;
	}
	
	
	@Override
	public int getLineWidth() {
		return -1;
	}
	
	
	public void setWhitespaceArgAssignBefore(final boolean enable) {
		final boolean oldValue = fWSArgAssignBefore;
		fWSArgAssignBefore = enable;
		firePropertyChange(WS_ARGASSIGN_BEFORE_PROP, oldValue, enable);
	}
	public boolean getWhitespaceArgAssignBefore() {
		return fWSArgAssignBefore;
	}
	
	public void setWhitespaceArgAssignBehind(final boolean enable) {
		final boolean oldValue = fWSArgAssignBehind;
		fWSArgAssignBehind = enable;
		firePropertyChange(WS_ARGASSIGN_BEFORE_PROP, oldValue, enable);
	}
	public boolean getWhitespaceArgAssignBehind() {
		return fWSArgAssignBehind;
	}
	
	public void setNewlineFDefBodyBlockBefore(final boolean enable) {
		final boolean oldValue = fNLFDefBodyBlockBefore;
		fNLFDefBodyBlockBefore = enable;
		firePropertyChange(NL_FDEF_BODYBLOCK_BEFOREP_PROP, oldValue, enable);
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
