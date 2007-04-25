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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.statet.ext.core.StatextProject;


public class StatetProject extends StatextProject {

	
	public static final String NATURE_ID = "de.walware.statet.base.StatetNature"; //$NON-NLS-1$
	
	public static void addNature(IProject project, IProgressMonitor monitor) throws CoreException {

		if (!project.hasNature(NATURE_ID)) {
			IProjectDescription description = appendNature(project.getDescription(), NATURE_ID);
			project.setDescription(description, monitor);
		} 
		else {
			monitor.done();
		}
	}

	
	public StatetProject() {

		super();
	}

	
	public void configure() throws CoreException {

	}

	public void deconfigure() throws CoreException {

	}

}
