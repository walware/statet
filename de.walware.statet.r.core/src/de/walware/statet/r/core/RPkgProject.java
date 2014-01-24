/*=============================================================================#
 # Copyright (c) 2007-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import de.walware.ecommons.resources.ProjectUtil;

import de.walware.statet.base.core.StatetProject;

import de.walware.statet.r.internal.core.Messages;


/**
 * Nature of R package projects
 */
public class RPkgProject implements IProjectNature {
	
	
	public static final String NATURE_ID = "de.walware.statet.r.RPkgNature"; //$NON-NLS-1$
	
	
	public static void addNature(final IProject project, final String pkgName,
			final IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(Messages.RProject_ConfigureTask_label, 1000);
			
			if (!project.hasNature(NATURE_ID)) {
				StatetProject.addNature(project, new SubProgressMonitor(monitor, 300));
				
				IProjectDescription description = project.getDescription();
				description = ProjectUtil.appendNature(description, RProject.NATURE_ID);
				description = ProjectUtil.appendNature(description, NATURE_ID);
				project.setDescription(description, new SubProgressMonitor(monitor, 300));
			}
			
			if (pkgName != null) {
				final RProject rProject = RProject.getRProject(project);
				rProject.setPackageConfig(pkgName);
				monitor.worked(300);
			}
		}
		finally {
			monitor.done();
		}
	}
	
	
	private IProject fProject;
	
	
	public RPkgProject() {
	}
	
	
	@Override
	public void setProject(final IProject project) {
		fProject = project;
	}
	
	@Override
	public void configure() throws CoreException {
	}
	
	@Override
	public void deconfigure() throws CoreException {
	}
	
	@Override
	public IProject getProject() {
		return fProject;
	}
	
}
