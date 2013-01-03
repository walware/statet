/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.debug.core.sourcelookup;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.CompositeSourceContainer;

import de.walware.statet.r.core.RProject;
import de.walware.statet.r.internal.debug.core.sourcelookup.Messages;


public class AllRProjectsSourceContainer extends CompositeSourceContainer implements IRSourceContainer {
	
	
	public static final String TYPE_ID = "de.walware.statet.r.sourceContainers.AllRProjectsType"; //$NON-NLS-1$
	
	
	public AllRProjectsSourceContainer() {
	}
	
	
	@Override
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}
	
	@Override
	public String getName() {
		return Messages.AllRProjectsSourceContainer_name;
	}
	
	@Override
	protected ISourceContainer[] createSourceContainers() throws CoreException {
		final List<ISourceContainer> list = new ArrayList<ISourceContainer>();
		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (final IProject project : projects) {
			try {
				if (project.isOpen() && project.hasNature(RProject.NATURE_ID)) {
					final ISourceContainer container = new RProjectSourceContainer(project, false);
					container.init(getDirector());
					list.add(container);
				}
			}
			catch (final Exception e) {}
		}
		return list.toArray(new ISourceContainer[list.size()]);
	}
	
	
	@Override
	public Object findSourceElement(final URI fileUri, final IFile[] fileInWorkspace) throws CoreException {
		final ISourceContainer[] containers = getSourceContainers();
		for (int i = 0; i < containers.length; i++) {
			final Object element = ((RProjectSourceContainer) containers[i]).findSourceElement(
					fileUri, fileInWorkspace );
			if (element != null) {
				return element;
			}
		}
		return null;
	}
	
	@Override
	public void findSourceElement(final IPath path, final List<Object> elements) throws CoreException {
		final ISourceContainer[] containers = getSourceContainers();
		for (int i = 0; i < containers.length; i++) {
			((RProjectSourceContainer) containers[i]).findSourceElement(path, elements);
		}
	}
	
	
	@Override
	public int hashCode() {
		return getClass().hashCode() + 1;
	}
	
	@Override
	public boolean equals(final Object obj) {
		return (obj instanceof AllRProjectsSourceContainer);
	}
	
}
