/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.workbench;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


/**
 * Provides content for a tree viewer that shows only containers.
 */
public class ContainerContentProvider implements ITreeContentProvider {
	
	
	private boolean fShowClosedProjects = true;
	
	
	/**
	 * Creates a new ContainerContentProvider.
	 */
	public ContainerContentProvider() {
	}
	
	
	public void dispose() {
	}
	
	public Object[] getElements(final Object element) {
		return getChildren(element);
	}
	
	public boolean hasChildren(final Object element) {
		return getChildren(element).length > 0;
	}
	
	public Object[] getChildren(final Object element) {
		if (element instanceof IWorkspace) { // Projects
			final IProject[] allProjects = ((IWorkspace) element).getRoot().getProjects();
			
			// check if closed projects should be shown
			if (fShowClosedProjects)
				return allProjects;
			
			final List<IProject> accessibleProjects = new ArrayList<IProject>();
			for (int i = 0; i < allProjects.length; i++) {
				if (allProjects[i].isOpen())
					accessibleProjects.add(allProjects[i]);
			}
			return accessibleProjects.toArray();
		}
		else if (element instanceof IContainer) { // Folders etc.
			final IContainer container = (IContainer) element;
			if (container.isAccessible()) {
				try {
					final List<IResource> children = new ArrayList<IResource>();
					final IResource[] members = container.members();
					for (int i = 0; i < members.length; i++) {
						if (members[i].getType() != IResource.FILE)
							children.add(members[i]);
					}
					return children.toArray();
				}
				catch (final CoreException e) {
					// this should never happen because we call #isAccessible before invoking #members
				}
			}
		}
		else if (element instanceof IContainer[]) {
			return (IContainer[]) element;
		}
		return new Object[0];
	}
	
	public Object getParent(final Object element) {
		if (element instanceof IResource) {
			return ((IResource) element).getParent();
		}
		return null;
	}
	
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
	}
	
	/**
	 * Specify whether or not to show closed projects in the tree 
	 * viewer.  Default is to show closed projects.
	 * 
	 * @param show boolean if false, do not show closed projects in the tree
	 */
	public void showClosedProjects(final boolean show) {
		fShowClosedProjects = show;
	}
	
}
