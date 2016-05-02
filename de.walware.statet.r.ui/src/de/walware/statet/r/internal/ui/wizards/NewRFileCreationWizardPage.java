/*=============================================================================#
 # Copyright (c) 2005-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;

import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.ltk.buildpaths.core.BuildpathsUtils;
import de.walware.ecommons.ltk.buildpaths.core.IBuildpathElement;
import de.walware.ecommons.ui.dialogs.groups.Layouter;
import de.walware.ecommons.ui.workbench.ContainerSelectionComposite.ContainerFilter;

import de.walware.statet.ext.ui.wizards.NewElementWizardPage;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RProjects;


/**
 * The "New" wizard page allows setting the container for
 * the new file as well as the file name. The page
 * will only accept file name without the extension or
 * with the extension that matches the expected one (r).
 */
public class NewRFileCreationWizardPage extends NewElementWizardPage {
	
	
	private static final String fgDefaultExtension = ".R"; //$NON-NLS-1$
	
	private static class RSourceFolderFilter extends ContainerFilter {
		
		@Override
		public boolean select(final IContainer container) {
			try {
				final IProject project= container.getProject();
				if (container.getType() == IResource.PROJECT) {
					if (project.hasNature(RProjects.R_NATURE_ID)) {
						return true;
					}
				}
				else {
					final IRProject rProject= RProjects.getRProject(project);
					if (rProject != null) {
						final ImList<IBuildpathElement> buildpath= rProject.getRawBuildpath();
						for (final IBuildpathElement sourceContainer : buildpath) {
							if (sourceContainer.getPath().isPrefixOf(container.getFullPath())) {
								return (!BuildpathsUtils.isExcluded(container, sourceContainer));
							}
						}
					}
				}
			}
			catch (final CoreException e) {	}
			
			return false;
		}
		
	}
	
	
	private final ResourceGroup resourceGroup;
	
	/**
	 * Constructor.
	 */
	public NewRFileCreationWizardPage(final IStructuredSelection selection) {
		super("NewRFileCreationWizardPage", selection); //$NON-NLS-1$
		
		setTitle(Messages.NewRScriptFileWizardPage_title);
		setDescription(Messages.NewRScriptFileWizardPage_description);
		
		this.resourceGroup = new ResourceGroup(fgDefaultExtension, new RSourceFolderFilter());
	}
	
	
	@Override
	protected void createContents(final Layouter layouter) {
		this.resourceGroup.createGroup(layouter);
	}
	
	ResourceGroup getResourceGroup() {
		return this.resourceGroup;
	}
	
	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);
		if (visible) {
			this.resourceGroup.setFocus();
		}
	}
	
	public void saveSettings() {
		this.resourceGroup.saveSettings();
	}
	
	@Override
	protected void validatePage() {
		updateStatus(this.resourceGroup.validate());
	}
	
}
