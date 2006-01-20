/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.core.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.preferences.DefaultScope;

import de.walware.eclipsecommon.preferences.IPreferenceAccess;
import de.walware.eclipsecommon.preferences.Preference;
import de.walware.eclipsecommon.preferences.PreferencesUtil;
import de.walware.eclipsecommon.preferences.Preference.StringPref;


public class TaskTagsPreferences {

	private static final String KEY_TAGS = "task_tags";
	private static final String KEY_PRIORITIES = "task_tags.priority";
	
	public static final StringPref PREF_TAGS = new StringPref(
			StatetCorePreferenceNodes.CAT_MANAGMENT_QUALIFIER, KEY_TAGS);
	public static final StringPref PREF_PRIORITIES = new StringPref(
			StatetCorePreferenceNodes.CAT_MANAGMENT_QUALIFIER, KEY_PRIORITIES);

	
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

	
	public static void setDefaultValues(DefaultScope scope) {
		
		TaskTagsPreferences defaultTags = new TaskTagsPreferences(
				new String[] { "TODO", "FIXME" },
				new TaskPriority[] { TaskPriority.NORMAL, TaskPriority.NORMAL }
		);
		Map<Preference, Object> map = defaultTags.getPreferencesMap();
		for (Preference<Object> unit : map.keySet()) {
			PreferencesUtil.setPrefValue(scope, unit, map.get(unit));
		}
	}

	public static TaskTagsPreferences load(IPreferenceAccess prefs) {

		String[] tags = loadTagsOnly(prefs);

		String priosValue = prefs.getPreferenceValue(PREF_PRIORITIES);
		String[] prioStrings = priosValue.split(",");
		
		TaskPriority[] prios = new TaskPriority[tags.length];
		for (int i = 0; i < tags.length; i++) {
			prios[i] = TaskPriority.valueOf(prioStrings[i].trim());
		}
		
		return new TaskTagsPreferences(tags, prios);
	}
	
	public static String[] loadTagsOnly(IPreferenceAccess prefs) {
		
		String tagValue = prefs.getPreferenceValue(PREF_TAGS);
		String[] tags = tagValue.split(",");

		ArrayList<String> tagList = new ArrayList<String>(tags.length);
		for (int i = 0; i < tags.length; i++) {
			String tagName = tags[i]; //.trim();
			if (tagName.length() > 0)
				tagList.add(tagName);
		}
		
		return tagList.toArray(new String[tagList.size()]);
	}
	
	
	private String[] fTags;
	private TaskPriority[] fPrios;
	
	public TaskTagsPreferences(String[] tags, TaskPriority[] priorities) {
		
		assert (tags.length == priorities.length);
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
	 * <p>Note: Intended to usage in preference/property page only.
	 * </p>
	 * 
	 * @param prefs
	 * @param project
	 */
	public Map<Preference, Object> getPreferencesMap() {
		
		StringBuffer tags = new StringBuffer();
		StringBuffer prios = new StringBuffer();
		for (int i = 0; i < fTags.length; i++) {
			tags.append(fTags[i]);
			tags.append(',');
			prios.append(fPrios[i].toString());
			prios.append(',');
		}
			
		Map<Preference, Object> map = new HashMap<Preference, Object>(2);
		map.put(PREF_TAGS, tags.toString());
		map.put(PREF_PRIORITIES, prios.toString());
		return map;
	}

}
