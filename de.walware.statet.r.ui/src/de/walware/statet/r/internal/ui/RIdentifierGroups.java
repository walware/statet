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

package de.walware.statet.r.internal.ui;

import java.util.Arrays;
import java.util.Set;

import de.walware.eclipsecommons.AbstractSettingsModelObject;
import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.preferences.Preference.StringArrayPref;
import de.walware.eclipsecommons.preferences.SettingsChangeNotifier.ManageListener;

import de.walware.statet.base.core.StatetCore;
import de.walware.statet.r.core.RNamesComparator;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.RUIPreferenceConstants;


/**
 *
 */
public class RIdentifierGroups extends AbstractSettingsModelObject {
	
	
	public final static String CONTEXT_ID = "r.editor/identifiergroups"; //$NON-NLS-1$
	
	
	private String[] fIdentifiersItemsAssignment;
	private String[] fIdentifiersItemsLogical;
	private String[] fIdentifiersItemsFlowcontrol;
	private String[] fIdentifiersItemsCustom1;
	private String[] fIdentifiersItemsCustom2;
	
	private IPreferenceAccess fPrefs;
	
	
	public RIdentifierGroups(IPreferenceAccess prefs) {
		installLock();

		fPrefs = prefs;
		StatetCore.getSettingsChangeNotifier().addManageListener(new ManageListener() {
			public void beforeSettingsChangeNotification(Set<String> contexts) {
				if (contexts.contains(CONTEXT_ID)) {
					checkItems();
				}
			}
			public void afterSettingsChangeNotification(Set<String> contexts) {
				if (contexts.contains(CONTEXT_ID)) {
					resetDirty();
				}
			}
		});
		checkItems();
	}
	
	
	private void checkItems() {
		getWriteLock().lock();
		try {
			RNamesComparator comparator = new RNamesComparator();
			fIdentifiersItemsAssignment = loadValues(fPrefs, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_ASSIGNMENT_ITEMS);
			Arrays.sort(fIdentifiersItemsAssignment, comparator);
			fIdentifiersItemsLogical = loadValues(fPrefs, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_LOGICAL_ITEMS);
			Arrays.sort(fIdentifiersItemsLogical, comparator);
			fIdentifiersItemsFlowcontrol = loadValues(fPrefs, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_FLOWCONTROL_ITEMS);
			Arrays.sort(fIdentifiersItemsFlowcontrol, comparator);
			fIdentifiersItemsCustom1 = loadValues(fPrefs, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_CUSTOM1_ITEMS);
			Arrays.sort(fIdentifiersItemsCustom1, comparator);
			fIdentifiersItemsCustom2 = loadValues(fPrefs, RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_CUSTOM2_ITEMS);
			Arrays.sort(fIdentifiersItemsCustom2, comparator);
		}
		finally {
			getWriteLock().unlock();
		}
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
