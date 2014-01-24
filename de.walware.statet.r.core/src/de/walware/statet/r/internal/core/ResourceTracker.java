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

package de.walware.statet.r.internal.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;

import de.walware.statet.r.core.RProject;
import de.walware.statet.r.internal.core.sourcemodel.RModelIndex;
import de.walware.statet.r.internal.core.sourcemodel.RModelManager;


public class ResourceTracker implements IResourceChangeListener {
	
	
	private final RModelManager fManager;
	
	private final Map<IProject, RProject> fProjects = new HashMap<IProject, RProject>();
	
	
	public ResourceTracker(final RModelManager manager) {
		fManager = manager; 
	}
	
	
	public void register(final IProject project, final RProject rProject) {
		synchronized (fProjects) {
			fProjects.put(project, rProject);
		}
	}
	
	public void unregister(final IProject project) {
		synchronized (fProjects) {
			fProjects.remove(project);
		}
	}
	
	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getResource() instanceof IProject) {
			final IProject project = (IProject) event.getResource();
			
			if (project.isOpen()) {
				try {
					if (project.hasNature(RProject.NATURE_ID)) {
						final RModelIndex index = fManager.getIndex();
						if (index != null) {
							switch (event.getType()) {
							case IResourceChangeEvent.PRE_CLOSE:
								index.updateProjectConfigClosed(project);
								break;
							case IResourceChangeEvent.PRE_DELETE:
								index.updateProjectConfigRemoved(project);
								break;
							default:
								break;
							}
						}
					}
				}
				catch (final CoreException e) {}
			}
			
			final RProject rProject;
			synchronized (fProjects) {
				rProject = fProjects.remove(project);
			}
			if (rProject != null) {
				rProject.dispose();
			}
		}
	}
	
}
