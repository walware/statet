/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;


/**
 * The "New" wizard page allows setting the container for
 * the new file as well as the file name. The page
 * will only accept file name without the extension or
 * with the extension that matches the expected one (r).
 */
public class NewRProjectWizardPage extends WizardNewProjectCreationPage {
	
	
	private IStructuredSelection fSelection;
	
	
	/**
	 * Constructor.
	 */
	public NewRProjectWizardPage(final IStructuredSelection selection) {
		super("NewRProjectWizardPage"); //$NON-NLS-1$
		fSelection = selection;
		
		setTitle(Messages.NewRProjectWizardPage_title);
		setDescription(Messages.NewRProjectWizardPage_description);
	}
	
	
	@Override
	public void createControl(final Composite parent) {
		super.createControl(parent);
		final Composite composite = (Composite) getControl();
		createWorkingSetGroup(composite, fSelection, new String[] {
				"org.eclipse.ui.resourceWorkingSetPage", //$NON-NLS-1$
				});
	}
	
}
