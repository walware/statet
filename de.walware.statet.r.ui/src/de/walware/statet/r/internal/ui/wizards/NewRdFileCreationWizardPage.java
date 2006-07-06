/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;

import de.walware.eclipsecommons.ui.dialogs.Layouter;

import de.walware.statet.ext.ui.wizards.NewElementWizardPage;


/**
 * The "New" wizard page allows setting the container for
 * the new file as well as the file name. The page
 * will only accept file name without the extension or
 * with the extension that matches the expected one (r).
 */
public class NewRdFileCreationWizardPage extends NewElementWizardPage {

	
	private static final String fgDefaultExtension = ".Rd";
	
	
	ResourceGroup fResourceGroup;

	/**
	 * Constructor.
	 */
	public NewRdFileCreationWizardPage(IStructuredSelection selection) {
		super("NewRdFileCreationWizardPage", selection);
		
		setTitle(Messages.NewRDocFileWizardPage_title);
		setDescription(Messages.NewRDocFileWizardPage_description);

		fResourceGroup = new ResourceGroup(fgDefaultExtension);
	}

	@Override
	protected void createContents(Layouter layouter) {
		
		fResourceGroup.createGroup(layouter);
	}

	public void setVisible(boolean visible) {

		super.setVisible(visible);
	    if (visible)
	        fResourceGroup.setFocus();
	}

    public void saveSettings() {
    	
    	fResourceGroup.saveSettings();
    }
	
    @Override
    protected void validatePage() {
    	
    	updateStatus(fResourceGroup.validate());
    }
	
}