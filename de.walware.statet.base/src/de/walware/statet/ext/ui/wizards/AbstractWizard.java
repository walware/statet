/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 *
 */
public abstract class AbstractWizard extends Wizard {

	
	protected IWorkbench fWorkbench;
	protected IStructuredSelection fWorkbenchSelection;


	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		
		fWorkbench = workbench;
		fWorkbenchSelection = currentSelection;
	}

	protected void setDialogSettings(AbstractUIPlugin plugin, String sectionName) {
		
		IDialogSettings master = plugin.getDialogSettings();
		IDialogSettings settings = master.getSection(sectionName);
		if (settings == null) {
			settings = master.addNewSection(sectionName);
		}
		setDialogSettings(settings);
	}

}