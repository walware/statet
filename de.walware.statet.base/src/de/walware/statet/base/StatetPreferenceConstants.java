/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base;

import org.eclipse.jface.preference.IPreferenceStore;



/**
 * @see org.eclipse.jface.resource.StringConverter
 * @see org.eclipse.jface.preference.PreferenceConverter
 *
 */
public class StatetPreferenceConstants {

	public final static String ROOT = "statet";
	
	/**
	 * A named preference that holds the task tags.
	 * <p>
	 * Value is of type <code>String</code>. Single Tasks are separated by ','.
	 * Value: @value
	 */
	public final static String TASK_TAGS = ROOT + ".task_tags";

	public final static String TASK_TAGS_PRIORITIES = TASK_TAGS + ".priority";

	/**
	 * Initializes the given preference store with the default values.
	 * 
	 * @param store the preference store to be initialized
	 */
	public static void initializeDefaultValues(IPreferenceStore store) {

		store.setDefault(TASK_TAGS, "TODO,FIXME");
		store.setDefault(TASK_TAGS_PRIORITIES, "NORMAL,NORMAL");
	}
	
}

