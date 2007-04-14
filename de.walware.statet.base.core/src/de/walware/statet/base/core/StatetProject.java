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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

import de.walware.statet.ext.core.StatextProject;


public class StatetProject extends StatextProject {

	
	public static final String ID = "de.walware.statet.base.core.StatetNature";
	
	
	private IProject fProject;
	
	
	public StatetProject() {

		super();
	}

	
/* IProjectNature *************************************************************/
	
	public void configure() throws CoreException {

	}

	public void deconfigure() throws CoreException {

	}

	public void setProject(IProject project) {
		
		fProject = project;
	}
	
	public IProject getProject() {

		return fProject;
	}


/* */
	
	/**
	 * Returns the project custom preference pool.
	 * Project preferences may include custom encoding.
	 * 
	 * @return IEclipsePreferences
	 */
	public IEclipsePreferences getEclipsePreferences() {

		IScopeContext context = new ProjectScope(getProject());
		return context.getNode(StatetCore.PLUGIN_ID);
	}

	
	public static void addNature(IProject project, IProgressMonitor monitor) throws CoreException {

		if (!project.hasNature(ID)) {
			IProjectDescription description = project.getDescription();
			String[] prevNatures = description.getNatureIds();
			String[] newNatures = new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length] = ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, monitor);
		} 
		else {
			monitor.done();
		}
	}
	
}
