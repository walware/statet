/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.debug.core.sourcelookup;

import java.net.URI;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.CompositeSourceContainer;


public class RProjectSourceContainer extends CompositeSourceContainer implements IRSourceContainer {
	
	
	public static final String TYPE_ID = "de.walware.statet.r.sourceContainers.RProjectType"; //$NON-NLS-1$
	
	
	private final IProject fProject;
	
	private boolean fReferenced;
	
	
	public RProjectSourceContainer(final IProject project, final boolean referenced) {
		fProject = project;
	}
	
	
	@Override
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}
	
	@Override
	public String getName() {
		return fProject.getName();
	}
	
	@Override
	public boolean isComposite() {
		return fReferenced;
	}
	
	/**
	 * @return the project
	 */
	public IProject getProject() {
		return fProject;
	}
	
	@Override
	protected ISourceContainer[] createSourceContainers() throws CoreException {
		// TODO: project dependencies
		return new ISourceContainer[0];
	}
	
	
	@Override
	public Object[] findSourceElements(final String name) throws CoreException {
		return null;
	}
	
	@Override
	public IFile findSourceElement(final URI fileUri, final IFile[] fileInWorkspace) {
		if (fProject.isOpen() && fProject.exists()) {
			for (final IFile workspaceFile : fileInWorkspace) {
				if (fProject.equals(workspaceFile.getProject())
						&& workspaceFile.exists() ) {
					return workspaceFile;
				}
			}
		}
		return null;
	}
	
	@Override
	public void findSourceElement(final IPath path, final List<Object> elements) {
		if (fProject.isOpen() && fProject.exists()) {
			final IFile file = fProject.getFile(path);
			if (file.exists()) {
				elements.add(file);
			}
		}
	}
	
	@Override
	public int hashCode() {
		return fProject.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		return (obj instanceof RProjectSourceContainer
				&& fProject.equals(((RProjectSourceContainer) obj).fProject) );
	}
	
}
