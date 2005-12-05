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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;


public class StatetProject implements IProjectNature {

	
	public static final String ID = "de.walware.statet.base.StatetNature";
	
	
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
	 * Helper method for returning one option value only.
	 * Note that it may answer <code>null</code> if this option does not exist, or if there is no custom value for it.
	 * <p>
	 * For a complete description of the configurable options, see <code>StatetCore#getDefaultOptions</code>.
	 * </p>
	 * 
	 * @param optionName the name of an option
	 * @param inheritCoreOptions - boolean indicating whether JavaCore options should be inherited as well
	 * @return the String value of a given option
	 */
	public String getOption(String optionName, boolean inheritCoreOptions) {
		
		IEclipsePreferences projectPreferences = getEclipsePreferences();
		String coreValue = inheritCoreOptions ? StatetCore.getOption(optionName) : null;
		if (projectPreferences == null) 
			return coreValue;
		String value = projectPreferences.get(optionName, coreValue);
		return (value == null) ? 
				null : value.trim();
	}
	
	/**
	 * Returns the project custom preference pool.
	 * Project preferences may include custom encoding.
	 * 
	 * @return IEclipsePreferences
	 */
	public IEclipsePreferences getEclipsePreferences() {

		IScopeContext context = new ProjectScope(getProject());
		return context.getNode(StatetPlugin.ID);
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
		else
			monitor.done();
	}
	
}
