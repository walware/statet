/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.core;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;


public class StatetCore {

	
	public static final String PLUGIN_ID = "de.walware.statet.base.core";
	
	public static final int STATUSCATEGORY = (2 << 16);
	
	/** Status Code for common errors in (incremental) builders */
	public static final int STATUSCODE_BUILD_ERROR = STATUSCATEGORY | (2 << 8);
	
	
	public static Set<StatetProject> getStatetProjects() {
		
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		Set<StatetProject> collected = new HashSet<StatetProject>();
		
		try {
			for (IProject project : projects) {
				StatetProject sp = (StatetProject) project.getNature(StatetProject.NATURE_ID);
				if (sp != null)
					collected.add(sp);
			}
		} catch (CoreException e) {
			return new HashSet<StatetProject>(0);
		}
		return collected;
	}
	

	private StatetCore() {

		super();
	}
	
}
