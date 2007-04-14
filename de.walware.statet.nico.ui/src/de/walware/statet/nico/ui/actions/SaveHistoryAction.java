/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.actions;

import org.eclipse.jface.wizard.WizardDialog;

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.NicoUIMessages;


/**
 * 
 */
public class SaveHistoryAction extends ToolAction  {
	
	
	public SaveHistoryAction(IToolActionSupport support) {
		
		super(support, false);
		
		setText(NicoUIMessages.SaveHistoryAction_name);
		setToolTipText(NicoUIMessages.SaveHistoryAction_tooltip);
//		setImageDescriptor();
//		setDisabledImageDescriptor();
		
		handleToolChanged();
	}
	
	
	public void run() {
		
		ToolProcess tool = getTool();
		if (tool == null) {
			return;
		}
		
		WizardDialog dialog = new WizardDialog(UIAccess.getActiveWorkbenchShell(true), 
				new SaveHistoryWizard(tool));
		dialog.open();
	}
	
}
