/*=============================================================================#
 # Copyright (c) 2006-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.base.core.preferences;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.walware.ecommons.collections.CollectionUtils;
import de.walware.ecommons.collections.ConstArrayList;
import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.Preference.EnumListPref;
import de.walware.ecommons.preferences.Preference.StringArrayPref;
import de.walware.ecommons.tasklist.TaskPriority;
import de.walware.ecommons.tasklist.TaskTag;


public class TaskTagsPreferences {
	
	public static final String GROUP_ID = "statet.task_tags"; //$NON-NLS-1$
	
	private static final String KEY_TAGS = "task_tags"; //$NON-NLS-1$
	private static final String KEY_PRIORITIES = "task_tags.priority"; //$NON-NLS-1$
	
	public static final StringArrayPref PREF_TAGS = new StringArrayPref(
			StatetCorePreferenceNodes.CAT_MANAGMENT_QUALIFIER, KEY_TAGS);
	public static final EnumListPref<TaskPriority> PREF_PRIORITIES = new EnumListPref<>(
			StatetCorePreferenceNodes.CAT_MANAGMENT_QUALIFIER, KEY_PRIORITIES, TaskPriority.class);
	
	
	public static String[] loadTagsOnly(final IPreferenceAccess prefs) {
		return prefs.getPreferenceValue(PREF_TAGS);
	}
	
	
	private final ConstList<TaskTag> taskTags;
	
	
	/**
	 * Creates preferences with the specified values.
	 * 
	 * @param taskTags
	 */
	public TaskTagsPreferences(final Collection<TaskTag> taskTags) {
		this.taskTags= CollectionUtils.asConstList(taskTags);
	}
	
	/**
	 * Creates preferences with default values.
	 */
	public TaskTagsPreferences() {
		this(new ConstArrayList<>(
				new TaskTag("TODO", TaskPriority.NORMAL), //$NON-NLS-1$
				new TaskTag("FIXME", TaskPriority.NORMAL) )); //$NON-NLS-1$
	}
	
	/**
	 * Creates preferences with values loaded from store.
	 * 
	 * @param prefs
	 */
	public TaskTagsPreferences(final IPreferenceAccess prefs) {
		final String[] keywords = prefs.getPreferenceValue(PREF_TAGS);
		final List<TaskPriority> priorities = prefs.getPreferenceValue(PREF_PRIORITIES);
		
		if (keywords.length == priorities.size()) {
			TaskTag[] array= new TaskTag[keywords.length];
			for (int i= 0; i < array.length; i++) {
				array[i]= new TaskTag(keywords[i], priorities.get(i));
			}
			this.taskTags= new ConstArrayList<>(array);
		}
		else {
			this.taskTags= CollectionUtils.emptyConstList();
		}
	}
	
	
	public String[] getTags() {
		final String[] array= new String[this.taskTags.size()];
		for (int i= 0; i < array.length; i++) {
			array[i]= this.taskTags.get(i).getKeyword();
		}
		return array;
	}
	
	public TaskPriority[] getPriorities() {
		final TaskPriority[] array= new TaskPriority[this.taskTags.size()];
		for (int i= 0; i < array.length; i++) {
			array[i]= this.taskTags.get(i).getPriority();
		}
		return array;
	}
	
	public List<TaskTag> getTaskTags() {
		return this.taskTags;
	}
	
	
	/**
	 * Allows to save the preferences. 
	 * 
	 * <p>Note: Intended to usage in preference/property page only.</p>
	 */
	public Map<Preference<?>, Object> addPreferencesToMap(final Map<Preference<?>, Object> map) {
		map.put(PREF_TAGS, getTags());
		map.put(PREF_PRIORITIES, new ConstArrayList<>(getPriorities()));
		return map;
	}
	
	/**
	 * Allows to save the preferences. 
	 * 
	 * <p>Note: Intended to usage in preference/property page only.</p>
	 */
	public Map<Preference<?>, Object> getPreferencesMap() {
		return addPreferencesToMap(new HashMap<Preference<?>, Object>(2));
	}
	
}
