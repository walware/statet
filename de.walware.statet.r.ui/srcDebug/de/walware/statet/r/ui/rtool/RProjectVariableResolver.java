/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.rtool;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ui.workbench.ResourceVariableResolver;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RProjects;
import de.walware.statet.r.internal.ui.rtools.Messages;
import de.walware.statet.r.ui.RUI;


public class RProjectVariableResolver extends ResourceVariableResolver.ProjectVariableResolver {
	
	
	public static final String R_PKG_BASE_PATH_NAME = "r_pkg_base_path"; //$NON-NLS-1$
	
	public static final String R_PKG_NAME_NAME = "r_pkg_name"; //$NON-NLS-1$
	
	
	public RProjectVariableResolver() {
	}
	
	
	@Override
	public String resolveValue(final IDynamicVariable variable, final String argument) throws CoreException {
		final IRProject rProject = getRProject(variable, argument);
		if (variable.getName().equals(R_PKG_BASE_PATH_NAME)) {
			IContainer container = rProject.getBaseContainer();
			if (container == null) {
				container = rProject.getProject();
			}
			return container.getFullPath().toString();
		}
		if (variable.getName().equals(R_PKG_NAME_NAME)) {
			final String name = rProject.getPackageName();
			if (name == null) {
				throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
						NLS.bind(Messages.Variable_error_InvalidProject_NoPkgName_message, 
								variable.getName(), rProject.getProject().getName() )));
			}
			return name;
		}
		throw new IllegalArgumentException(variable.toString());
	}
	
	
	protected IRProject getRProject(final IDynamicVariable variable, final String argument)
			throws CoreException {
		final IProject project = (IProject) getResource(variable, argument);
		if (!project.exists()) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
					NLS.bind(Messages.Variable_error_InvalidProject_NotExists_message, 
							variable.getName(), project.getName() )));
		}
		final IRProject rProject;
		if (!project.hasNature(RProjects.R_NATURE_ID)
				|| (rProject = RProjects.getRProject(project)) == null) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
					NLS.bind(Messages.Variable_error_InvalidProject_NotExists_message, 
							variable.getName(), project.getName() )));
		}
		return rProject;
	}
	
}
