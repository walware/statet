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

package de.walware.statet.nico.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;

import de.walware.eclipsecommon.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.NicoUIMessages;


/**
 * 
 */
public class LoadHistoryAction extends Action  {
	
	
	private ToolProcess fProcess;
	

	public LoadHistoryAction() {
		
		setText(NicoUIMessages.LoadHistoryAction_name);
		setToolTipText(NicoUIMessages.LoadHistoryAction_tooltip);
//		setImageDescriptor();
//		setDisabledImageDescriptor();
		setEnabled(false);
	}
	
	public void connect(ToolProcess process) {
		
		fProcess = process;
		setEnabled(fProcess != null);
	}
	
	public void run() {
		
		ToolProcess process = fProcess;
		if (process == null) {
			return;
		}
		
		WizardDialog dialog = new WizardDialog(UIAccess.getActiveWorkbenchShell(true), 
				new LoadHistoryWizard(process));
		dialog.open();
	}
	
}
