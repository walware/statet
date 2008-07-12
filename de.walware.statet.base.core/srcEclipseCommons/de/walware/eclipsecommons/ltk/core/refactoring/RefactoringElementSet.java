/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ltk.core.refactoring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import de.walware.eclipsecommons.ltk.IModelElement;
import de.walware.eclipsecommons.ltk.ISourceUnit;


public class RefactoringElementSet {
	
	
	private static final int POST_PROCESS = 10000;
	
	
	protected Object[] fOrgTargets;
	
	private List<IModelElement> fModelElements;
	private List<IResource> fResources;
	
	private int fProcessState = 0;
	private List<IResource> fResourcesOwnedByElements;
	private List<IFile> fFilesContainingElements;
	
	
	public RefactoringElementSet(final Object[] elements) {
		fOrgTargets = elements;
		init(elements);
		
		if (fModelElements == null) {
			fModelElements = new ArrayList<IModelElement>(0);
		}
		if (fResources == null) {
			fResources = new ArrayList<IResource>(0);
		}
		if (countElements() == fOrgTargets.length) {
			fProcessState = 10;
		}
		else {
			fProcessState = -10;
		}
	}
	
	
	protected void init(final Object[] elements) {
		for (final Object o : elements) {
			add(o);
		}
	}
	
	protected void add(final Object o) {
		if (o instanceof IModelElement) {
			if (fModelElements == null) {
				fModelElements = new ArrayList<IModelElement>();
			}
			fModelElements.add((IModelElement) o);
			return;
		}
		if (o instanceof IResource) {
			if (fResources == null) {
				fResources = new ArrayList<IResource>();
			}
			fResources.add((IResource) o);
			return;
		}
	}
	
	protected int countElements() {
		return fResources.size() + fModelElements.size();
	}
	
	public int getElementCount() {
		return countElements();
	}
	
	public boolean isOK() {
		return (fProcessState > 0);
	}
	
	public Object[] getInitialObjects() {
		return fOrgTargets;
	}
	
	public List<IResource> getResources() {
		return fResources;
	}
	
	public List<IModelElement> getModelElements() {
		return fModelElements;
	}
	
	public List<IResource> getResourcesOwnedByElements() {
		return fResourcesOwnedByElements;
	}
	
	public List<IFile> getFilesContainingElements() {
		return fFilesContainingElements;
	}
	
	public IResource getOwningResource(final IModelElement element) {
		if ((element.getElementType() & IModelElement.MASK_C2) < IModelElement.C2_SOURCE_CHUNK) {
			IResource resource;
			resource = (IResource) element.getAdapter(IResource.class);
			return resource;
		}
		return null;
	}
	
	public IResource getResource(final IModelElement element) {
		final ISourceUnit unit = element.getSourceUnit();
		if (unit != null) {
			return unit.getResource();
		}
		return null;
	}
	
	public IProject getSingleProject() {
		IProject project = null;
		for (final IResource resource : fResources) {
			final IProject p = resource.getProject();
			if (project == null) {
				project = p;
				continue;
			}
			if (!project.equals(p)) {
				return null;
			}
		}
		for (final IModelElement element : fModelElements) {
			final IResource resource = getResource(element);
			if (resource == null) {
				continue;
			}
			final IProject p = resource.getProject();
			if (project == null) {
				project = p;
				continue;
			}
			if (!project.equals(p)) {
				return null;
			}
		}
		return project;
	}
	
	public Set<IProject> getProjects() {
		final Set<IProject> projects = new HashSet<IProject>();
		for (final IResource resource : fResources) {
			projects.add(resource.getProject());
		}
		for (final IModelElement element : fModelElements) {
			final IResource resource = getResource(element);
			if (resource != null) {
				projects.add(resource.getProject());
			}
		}
		return projects;
	}
	
	public Set<IProject> getAffectedProjects() {
		final Set<IProject> projects = getProjects();
		final IProject[] array = projects.toArray(new IProject[projects.size()]);
		for (int i = 0; i < array.length; i++) {
			final IProject[] referencingProjects = array[i].getReferencingProjects();
			if (referencingProjects.length > 0) {
				addAffectedProjects(referencingProjects, projects);
			}
		}
		return projects;
	}
	
	private void addAffectedProjects(final IProject[] projectToAdd, final Set<IProject> projects) {
		for (int i = 0; i < projectToAdd.length; i++) {
			if (projects.add(projectToAdd[i])) {
				final IProject[] referencingProjects = projectToAdd[i].getReferencingProjects();
				if (referencingProjects.length > 0) {
					addAffectedProjects(referencingProjects, projects);
				}
			}
		}
	}
	
	public String[] getAffectedProjectNatures() throws CoreException {
		final Set<IProject> affectedProjects = getAffectedProjects();
		final Set<String> natureIds = new HashSet<String>();
		for (final IProject project : affectedProjects) {
			final String[] ids = project.getDescription().getNatureIds();
			for (final String id : ids) {
				natureIds.add(id);
			}
		}
		return natureIds.toArray(new String[natureIds.size()]);
	}
	
	
	public void removeElementsWithAncestorsOnList() {
		removeResourcesDescendantsOfResources();
		removeResourcesDescendantsOfModelElements();
		removeModelElementsDescendantsOfModelElements();
	}
	
	private void removeResourcesDescendantsOfResources() {
		final Iterator<IResource> iter = fResources.iterator();
		ITER_RESOURCE : while (iter.hasNext()) {
			final IResource subResource = iter.next();
			for (final IResource superResource : fResources) {
				if (isDescendantOf(subResource, superResource)) {
					iter.remove();
					continue ITER_RESOURCE;
				}
			}
		}
	}
	
	private void removeResourcesDescendantsOfModelElements() {
		final Iterator<IResource> iter = fResources.iterator();
		ITER_RESOURCE : while (iter.hasNext()) {
			final IResource subResource = iter.next();
			for (final IModelElement superElement : fModelElements) {
				if (isDescendantOf(subResource, superElement)) {
					iter.remove();
					continue ITER_RESOURCE;
				}
			}
		}
	}
	
	private void removeModelElementsDescendantsOfModelElements() {
		final Iterator<IModelElement> iter = fModelElements.iterator();
		ITER_ELEMENT : while (iter.hasNext()) {
			final IModelElement subElement = iter.next();
			for (final IModelElement superElement : fModelElements) {
				if (isDescendantOf(subElement, superElement)) {
					iter.remove();
					continue ITER_ELEMENT;
				}
			}
		}
	}
	
	protected boolean isDescendantOf(final IResource subResource, final IResource superResource) {
		return !subResource.equals(superResource) && superResource.getFullPath().isPrefixOf(subResource.getFullPath());
	}
	
	protected boolean isDescendantOf(final IResource subResource, final IModelElement superElement) {
		final IResource superResource = getOwningResource(superElement);
		if (superResource != null) {
			return isDescendantOf(subResource, superResource);
		}
		return false;
	}
	
	protected boolean isDescendantOf(final IModelElement subElement, final IModelElement superElement) {
		if (subElement.equals(superElement)) {
			return false;
		}
		IModelElement parent = subElement.getParent();
		while (parent != null){
			if (parent.equals(superElement)) {
				return true;
			}
			parent = parent.getParent();
		}
		return false;
	}
	
	public void postProcess() {
		if (fProcessState < 0) {
			throw new IllegalStateException();
		}
		if (fProcessState < POST_PROCESS) {
			fResourcesOwnedByElements = new ArrayList<IResource>(1);
			fFilesContainingElements = new ArrayList<IFile>(1);
			for (final IModelElement element : fModelElements) {
				IResource resource;
				resource = getOwningResource(element);
				if (resource != null) {
					fResourcesOwnedByElements.add(resource);
					continue;
				}
				resource = getResource(element);
				if (resource != null && resource.getType() == IResource.FILE) {
					fFilesContainingElements.add((IFile) resource);
					continue;
				}
			}
		}
		fProcessState = POST_PROCESS;
	}
	
}
