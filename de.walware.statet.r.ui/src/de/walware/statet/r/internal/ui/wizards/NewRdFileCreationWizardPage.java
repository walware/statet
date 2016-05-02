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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;

import de.walware.ecommons.ui.dialogs.groups.Layouter;
import de.walware.ecommons.ui.workbench.ContainerSelectionComposite.ContainerFilter;

import de.walware.statet.ext.ui.wizards.NewElementWizardPage;

import de.walware.statet.r.core.RProjects;


/**
 * The "New" wizard page allows setting the container for
 * the new file as well as the file name. The page
 * will only accept file name without the extension or
 * with the extension that matches the expected one (r).
 */
public class NewRdFileCreationWizardPage extends NewElementWizardPage {
	
	
	private static final String fgDefaultExtension = ".Rd"; //$NON-NLS-1$
	
	private static class RProjectFilter extends ContainerFilter {
		
		@Override
		public boolean select(final IContainer container) {
			try {
				final IProject project= container.getProject();
				if (project.hasNature(RProjects.R_NATURE_ID)) {
					return true;
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
	public NewRdFileCreationWizardPage(final IStructuredSelection selection) {
		super("NewRdFileCreationWizardPage", selection); //$NON-NLS-1$
		
		setTitle(Messages.NewRDocFileWizardPage_title);
		setDescription(Messages.NewRDocFileWizardPage_description);
		
		this.resourceGroup= new ResourceGroup(fgDefaultExtension, new RProjectFilter());
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
