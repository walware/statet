/*=============================================================================#
 # Copyright (c) 2005-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.builder;

import java.util.List;

import de.walware.ecommons.tasklist.TaskMarkerHandler;
import de.walware.ecommons.tasklist.TaskTag;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;

import de.walware.statet.r.core.IRProject;


public class RTaskMarkerHandler extends TaskMarkerHandler {
	
	
	public static final String TASK_MARKER_ID= "de.walware.statet.r.markers.Tasks"; //$NON-NLS-1$
	
	
	public RTaskMarkerHandler() {
		super(TASK_MARKER_ID);
	}
	
	
	public void init(final IRProject project) {
		final TaskTagsPreferences taskPrefs = new TaskTagsPreferences(project.getPrefs());
		final List<TaskTag> taskTags= taskPrefs.getTaskTags();
		initTaskPattern(taskTags);
	}
	
}
