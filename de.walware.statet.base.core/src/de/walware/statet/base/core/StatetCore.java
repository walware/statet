/*=============================================================================#
 # Copyright (c) 2005-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.base.core;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.debug.core.ISshSessionService;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.core.model.IWorkspaceSourceUnit;

import de.walware.statet.base.internal.core.BaseCorePlugin;


public class StatetCore {
	
	
	public static final String PLUGIN_ID = "de.walware.statet.base.core"; //$NON-NLS-1$
	
	
	public static Set<StatetProject> getStatetProjects() {
		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		final Set<StatetProject> collected = new HashSet<StatetProject>();
		try {
			for (final IProject project : projects) {
				final StatetProject sp = (StatetProject) project.getNature(StatetProject.NATURE_ID);
				if (sp != null) {
					collected.add(sp);
				}
			}
		} catch (final CoreException e) {
			logError(e);
			return new HashSet<StatetProject>(0);
		}
		return collected;
	}
	
	public static StatetProject getStatetProject(final ISourceUnit su) {
		if (su instanceof IWorkspaceSourceUnit) {
			final IProject project = ((IWorkspaceSourceUnit) su).getResource().getProject();
			try {
				if (project.hasNature(StatetProject.NATURE_ID)) {
					return (StatetProject) project.getNature(StatetProject.NATURE_ID);
				}
			}
			catch (final CoreException e) {
				logError(e);
			}
		}
		return null;
	}
	
	public static ISshSessionService getSshSessionManager() {
		return BaseCorePlugin.getDefault().getSshSessionManager();
	}
	
	private static void logError(final CoreException e) {
		BaseCorePlugin.getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, -1, "Error catched", e)); //$NON-NLS-1$
	}
	
	
	private StatetCore() {
	}
	
}
