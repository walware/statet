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

package de.walware.statet.r.internal.debug.ui.sourcelookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;

import de.walware.statet.r.core.RProject;
import de.walware.statet.r.debug.core.sourcelookup.RProjectSourceContainer;
import de.walware.statet.r.internal.debug.ui.ProjectSelectionDialog;


public class RProjectSourceContainerBrowser extends AbstractSourceContainerBrowser {
	
	
	/** Created via extension point */
	public RProjectSourceContainerBrowser() {
	}
	
	
	@Override
	public ISourceContainer[] addSourceContainers(final Shell shell,
			final ISourceLookupDirector director) {
		final List<IProject> projects = getPossibleAdditions(director);
		final ProjectSelectionDialog dialog = new ProjectSelectionDialog(shell, projects);
		if (dialog.open() == Dialog.OK) {
			final Set<?> selectedProjects = dialog.getCheckedElements();
			final List<ISourceContainer> containers = new ArrayList<ISourceContainer>();
			for (final IProject project : projects) {
				if (selectedProjects.contains(project)) {
					final RProjectSourceContainer container = new RProjectSourceContainer(project, false);
					container.init(director);
					containers.add(container);
				}
			}
			return containers.toArray(new ISourceContainer[containers.size()]);
		}
		return new ISourceContainer[0];
	}
	
	protected List<IProject> getPossibleAdditions(final ISourceLookupDirector director) {
		final List<IProject> projects = getRResourceProjects();
		final ISourceContainer[] containers = director.getSourceContainers();
		for (final ISourceContainer container : containers) {
			if (container.getType().getId().equals(RProjectSourceContainer.TYPE_ID)) {
				projects.remove(((RProjectSourceContainer) container).getProject());
			}
		}
		return projects;
	}
	
	protected List<IProject> getRResourceProjects() {
		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		final List<IProject> collected = new ArrayList<IProject>();
		for (final IProject project : projects) {
			try {
				if (project.hasNature(RProject.NATURE_ID)) {
					collected.add(project);
				}
			}
			catch (final CoreException e) {
			}
		}
		return collected;
	}
	
}
