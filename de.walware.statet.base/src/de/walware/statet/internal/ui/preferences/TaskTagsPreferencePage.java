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

package de.walware.statet.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.ext.ui.preferences.PropertyAndPreferencePage;


/**
 * The page to configure the task tags
 */
public class TaskTagsPreferencePage extends PropertyAndPreferencePage<TaskTagsConfigurationBlock> {

	public static final String PREF_ID = "de.walware.statet.ui.preferences.TaskTagsPage"; //$NON-NLS-1$
	public static final String PROP_ID = "de.walware.statet.ui.propertyPages.TaskTagsPage"; //$NON-NLS-1$
	
	
	public TaskTagsPreferencePage() {
		
		setPreferenceStore(StatetPlugin.getDefault().getPreferenceStore());

		// only used when page is shown programatically
		setTitle(Messages.TaskTags_title); 

		setDescription(Messages.TaskTags_description); 
	}

	@Override
	protected String getPreferencePageID() {
		
		return PREF_ID;
	}

	@Override
	protected String getPropertyPageID() {
		
		return PROP_ID;
	}
	
	@Override
	protected TaskTagsConfigurationBlock createConfigurationBlock() 
			throws CoreException {
		
		return new TaskTagsConfigurationBlock(getProject(), getNewStatusChangedListener());
	}
	
	@Override
	protected boolean hasProjectSpecificSettings(IProject project) {
		
		return fBlock.hasProjectSpecificOptions(project);
	}
}
