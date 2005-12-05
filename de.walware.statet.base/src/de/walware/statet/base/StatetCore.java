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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;


public class StatetCore {

	
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
	
	
	public StatetCore() {

		super();
	}
	
	public static String getOption(String optionName) {
		
		return StatetPlugin.getDefault().getPreferenceStore().getString(optionName);
	}
	
	public static Set<StatetProject> getStatetProjects() {
		
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		Set<StatetProject> collected = new HashSet<StatetProject>();
		
		try {
			for (IProject project : projects) {
				StatetProject sp = (StatetProject) project.getNature(StatetProject.ID);
				if (sp != null)
					collected.add(sp);
			}
		} catch (CoreException e) {
			return new HashSet<StatetProject>(0);
		}
		return collected;
	}
	
}
