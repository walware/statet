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

import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;


/**
 * The "New" wizard page allows setting the container for
 * the new file as well as the file name. The page
 * will only accept file name without the extension or
 * with the extension that matches the expected one (r).
 */
public class NewRProjectWizardPage extends WizardNewProjectCreationPage {

	
	/**
	 * Constructor.
	 */
	public NewRProjectWizardPage() {
		super("NewRProjectWizardPage");
		
		setTitle(Messages.NewRProjectWizardPage_title);
		setDescription(Messages.NewRProjectWizardPage_description);
	}

}