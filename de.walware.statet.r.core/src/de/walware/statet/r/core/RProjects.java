/*=============================================================================#
 # Copyright (c) 2014-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.resources.core.ProjectUtils;

import de.walware.statet.base.core.StatetProject;

import de.walware.statet.r.internal.core.Messages;
import de.walware.statet.r.internal.core.RProject;


public class RProjects {
	
	
	// "de.walware.statet.r.natures.R"
	public static final String R_NATURE_ID= "de.walware.statet.r.RNature"; //$NON-NLS-1$
	
	public static final String R_PKG_NATURE_ID = "de.walware.statet.r.RPkgNature"; //$NON-NLS-1$
	
	
	public static IRProject getRProject(final IProject project) {
		return RProject.getRProject(project);
	}
	
	/**
	 * 
	 * @param project the project to setup
	 * @param monitor SubMonitor-recommended
	 * @throws CoreException
	 */
	public static void setupRProject(final IProject project,
			final IProgressMonitor monitor) throws CoreException {
		final SubMonitor progress= SubMonitor.convert(monitor,
				NLS.bind(Messages.RProject_ConfigureTask_label, project.getName()),
				2 + 8 );
		
		final IProjectDescription description= project.getDescription();
		boolean changed= false;
		changed|= ProjectUtils.addNature(description, StatetProject.NATURE_ID);
		changed|= ProjectUtils.addNature(description, R_NATURE_ID);
		progress.worked(2);
		
		if (changed) {
			project.setDescription(description, progress.newChild(8));
		}
	}
	
	/**
	 * 
	 * @param project the project to setup
	 * @param pkgRoot the root folder of the R package structure
	 * @param pkgName the R package name
	 * @param monitor SubMonitor-recommended
	 * @throws CoreException
	 */
	public static void setupRPkgProject(final IProject project,
			final IContainer pkgRoot, final String pkgName,
			final IProgressMonitor monitor) throws CoreException {
		final SubMonitor m= SubMonitor.convert(monitor,
				NLS.bind(Messages.RProject_ConfigureTask_label, project.getName()),
				2 + 8 + 2 );
		
		final IProjectDescription description= project.getDescription();
		boolean changed= false;
		changed|= ProjectUtils.addNature(description, StatetProject.NATURE_ID);
		changed|= ProjectUtils.addNature(description, R_NATURE_ID);
		changed|= ProjectUtils.addNature(description, R_PKG_NATURE_ID);
		m.worked(2);
		
		if (changed) {
			project.setDescription(description, m.newChild(8));
		}
		
		m.setWorkRemaining(2);
		
		final RProject rProject= RProject.getRProject(project);
		
		PreferencesUtil.setPrefValue(rProject.getProjectContext(), IRProject.BASE_FOLDER_PATH_PREF,
				(pkgRoot != null) ? pkgRoot.getProjectRelativePath().toPortableString() : null );
		
		if (pkgName != null) {
			rProject.setPackageConfig(pkgName);
			m.worked(2);
		}
	}
	
}
