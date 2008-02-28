/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.core.preferences;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Assert;

import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.preferences.Preference.EnumListPref;
import de.walware.eclipsecommons.preferences.Preference.StringArrayPref;


public class TaskTagsPreferences {
	
	public static final String GROUP_ID = "statet.task_tags"; //$NON-NLS-1$
	
	private static final String KEY_TAGS = "task_tags"; //$NON-NLS-1$
	private static final String KEY_PRIORITIES = "task_tags.priority"; //$NON-NLS-1$
	
	public static final StringArrayPref PREF_TAGS = new StringArrayPref(
			StatetCorePreferenceNodes.CAT_MANAGMENT_QUALIFIER, KEY_TAGS);
	public static final EnumListPref<TaskPriority> PREF_PRIORITIES = new EnumListPref<TaskPriority>(
			StatetCorePreferenceNodes.CAT_MANAGMENT_QUALIFIER, KEY_PRIORITIES, TaskPriority.class);
	
	
	public static enum TaskPriority {
		
		HIGH (IMarker.PRIORITY_HIGH), 
		NORMAL (IMarker.PRIORITY_NORMAL), 
		LOW (IMarker.PRIORITY_LOW);
		
		private int fPriority;
		
		TaskPriority(int priority) {
			fPriority = priority;
		}
		
		public int getMarkerPriority() {
			return fPriority;
		}
	};
	
	
	public static String[] loadTagsOnly(IPreferenceAccess prefs) {
		return prefs.getPreferenceValue(PREF_TAGS);
	}
	
	
	private String[] fTags;
	private TaskPriority[] fPrios;
	
	
	/**
	 * Creates preferences with the specified values.
	 * 
	 * @param tags
	 * @param priorities
	 */
	public TaskTagsPreferences(String[] tags, TaskPriority[] priorities) {
		setup(tags, priorities);
	}
	
	/**
	 * Creates preferences with default values.
	 */
	public TaskTagsPreferences() {
		setup(	new String[] { "TODO", "FIXME" }, //$NON-NLS-1$ //$NON-NLS-2$
				new TaskPriority[] { TaskPriority.NORMAL, TaskPriority.NORMAL }
		);
	}
	
	/**
	 * Creates preferences with values loaded from store.
	 * 
	 * @param prefs
	 */
	public TaskTagsPreferences(final IPreferenceAccess prefs) {
		String[] tags = loadTagsOnly(prefs);
		List<TaskPriority> prios = prefs.getPreferenceValue(PREF_PRIORITIES);
		
		setup(tags, prios.toArray(new TaskPriority[prios.size()]));
	}
	
	private void setup(final String[] tags, final TaskPriority[] priorities) {
		
		Assert.isLegal(tags.length == priorities.length, "Invalid preference values for task tags.");  //$NON-NLS-1$
		fTags = tags;
		fPrios = priorities;
	}
	
	
	public String[] getTags() {
		return fTags;
	}
	
	public TaskPriority[] getPriorities() {
		return fPrios;
	}
	
	
	/**
	 * Allows to save the preferences. 
	 * 
	 * <p>Note: Intended to usage in preference/property page only.</p>
	 */
	public Map<Preference, Object> addPreferencesToMap(final Map<Preference, Object> map) {
		map.put(PREF_TAGS, fTags);
		map.put(PREF_PRIORITIES, Arrays.asList(fPrios));
		return map;
	}
	
	/**
	 * Allows to save the preferences. 
	 * 
	 * <p>Note: Intended to usage in preference/property page only.</p>
	 */
	public Map<Preference, Object> getPreferencesMap() {
		return addPreferencesToMap(new HashMap<Preference, Object>(2));
	}
	
}
