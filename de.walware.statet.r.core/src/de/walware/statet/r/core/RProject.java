/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import de.walware.statet.base.core.StatetProject;
import de.walware.statet.ext.core.StatextProject;
import de.walware.statet.r.core.internal.RInternalBuilder;


public class RProject extends StatextProject {

	
	public static final String NATURE_ID = "de.walware.statet.r.core.RNature";
	
	
	public RProject() {

		super();
	}

	
/* IProjectNature *************************************************************/
	
	public void configure() throws CoreException {

		addBuilders();
	}

	public void deconfigure() throws CoreException {

		removeBuilders();
	}

	
/* **/
	public StatetProject getStatetProject() throws CoreException {
		
		return (StatetProject) fProject.getNature(StatetProject.NATURE_ID);
	}
	
	
	
/* **/
	public void addBuilders() throws CoreException {
		
		String builderId = RInternalBuilder.ID;
		
		IProjectDescription description = fProject.getDescription();
		ICommand[] existingCommands = description.getBuildSpec();
		int builderIndex = getBuilderIndex(existingCommands, builderId);
		
		if (builderIndex == -1) {
			// Add new builder
			ICommand newCommand = description.newCommand();
			newCommand.setBuilderName(builderId);
			
			ICommand[] newCommands = new ICommand[existingCommands.length+1];
			System.arraycopy(existingCommands, 0, newCommands, 0, existingCommands.length);
			newCommands[existingCommands.length] = newCommand;
			
			description.setBuildSpec(newCommands);
			fProject.setDescription(description, null);
		}
	}
	
	public void removeBuilders() throws CoreException {

		String builderId = RInternalBuilder.ID;
		
		IProjectDescription description = getProject().getDescription();
		ICommand[] existingCommands = description.getBuildSpec();
		int builderIndex = getBuilderIndex(existingCommands, builderId);
		
		if (builderIndex >= 0) {
			ICommand[] newCommands = new ICommand[existingCommands.length - 1];
			System.arraycopy(existingCommands, 0, newCommands, 0, builderIndex);
			System.arraycopy(existingCommands, builderIndex+1, newCommands, builderIndex, newCommands.length-builderIndex);
			description.setBuildSpec(newCommands);
		}
	}
	
	/**
	 * Find the specific Java command amongst the given build spec
	 * and return its index or -1 if not found.
	 */
	private int getBuilderIndex(ICommand[] buildSpec, String id) {

		for (int i = 0; i < buildSpec.length; ++i) {
			if (buildSpec[i].getBuilderName().equals(id)) {
				return i;
			}
		}
		return -1;
	}

/* */

	public static void addNature(IProject project, IProgressMonitor monitor) throws CoreException {

		try {
			monitor.beginTask("Config R Project...", 1000);
			
			if (!project.hasNature(NATURE_ID)) {
				StatetProject.addNature(project, new SubProgressMonitor(monitor, 400));
				
				IProjectDescription description = appendNature(project.getDescription(), NATURE_ID);
				project.setDescription(description, new SubProgressMonitor(monitor, 600));
			} 
		}
		finally {
			monitor.done();
		}
	}
}
