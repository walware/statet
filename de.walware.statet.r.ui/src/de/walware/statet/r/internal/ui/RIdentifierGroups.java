/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui;

import java.util.Arrays;
import java.util.Map;

import de.walware.ecommons.preferences.AbstractPreferencesModelObject;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.Preference.StringArrayPref;

import de.walware.statet.r.core.RSymbolComparator;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.RUIPreferenceConstants;


/**
 * 
 */
public class RIdentifierGroups extends AbstractPreferencesModelObject {
	
	
	public final static String GROUP_ID = "r.editor/identifiergroups"; //$NON-NLS-1$
	
	
	private String[] fIdentifiersItemsAssignment;
	private String[] fIdentifiersItemsLogical;
	private String[] fIdentifiersItemsFlowcontrol;
	private String[] fIdentifiersItemsCustom1;
	private String[] fIdentifiersItemsCustom2;
	
	
	public RIdentifierGroups() {
		installLock();
	}
	
	@Override
	public String[] getNodeQualifiers() {
		return new String[0];
	}
	
	@Override
	public void loadDefaults() {
	}
	
	@Override
	public void load(IPreferenceAccess prefs) {
		RSymbolComparator comparator = new RSymbolComparator();
		fIdentifiersItemsAssignment = loadValues(prefs, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_ASSIGNMENT_ITEMS);
		Arrays.sort(fIdentifiersItemsAssignment, comparator);
		fIdentifiersItemsLogical = loadValues(prefs, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_LOGICAL_ITEMS);
		Arrays.sort(fIdentifiersItemsLogical, comparator);
		fIdentifiersItemsFlowcontrol = loadValues(prefs, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_FLOWCONTROL_ITEMS);
		Arrays.sort(fIdentifiersItemsFlowcontrol, comparator);
		fIdentifiersItemsCustom1 = loadValues(prefs, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_CUSTOM1_ITEMS);
		Arrays.sort(fIdentifiersItemsCustom1, comparator);
		fIdentifiersItemsCustom2 = loadValues(prefs, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_CUSTOM2_ITEMS);
		Arrays.sort(fIdentifiersItemsCustom2, comparator);
	}
	
	@Override
	public Map<Preference, Object> deliverToPreferencesMap(Map<Preference, Object> map) {
		return map;
	}
	
	private final String[] loadValues(IPreferenceAccess prefs, String key) {
		Preference<String[]> pref = new StringArrayPref(RUI.PLUGIN_ID, key);
		return prefs.getPreferenceValue(pref);
	}
	
	
	public String[] getAssignmentIdentifiers() {
		return fIdentifiersItemsAssignment;
	}
	
	public String[] getLogicalIdentifiers() {
		return fIdentifiersItemsLogical;
	}
	
	public String[] getFlowcontrolIdentifiers() {
		return fIdentifiersItemsFlowcontrol;
	}
	
	public String[] getCustom1Identifiers() {
		return fIdentifiersItemsCustom1;
	}
	
	public String[] getCustom2Identifiers() {
		return fIdentifiersItemsCustom2;
	}
	
}
