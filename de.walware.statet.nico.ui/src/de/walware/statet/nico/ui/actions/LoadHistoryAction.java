/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.actions;

import org.eclipse.jface.wizard.WizardDialog;

import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.util.IToolProvider;
import de.walware.statet.nico.ui.NicoUIMessages;


/**
 * 
 */
public class LoadHistoryAction extends ToolAction {
	
	
	public LoadHistoryAction(final IToolProvider support) {
		super(support, false);
		
		setText(NicoUIMessages.LoadHistoryAction_name);
		setToolTipText(NicoUIMessages.LoadHistoryAction_tooltip);
//		setImageDescriptor();
//		setDisabledImageDescriptor();
		
		handleToolChanged();
	}
	
	
	@Override
	public void run() {
		
		final ToolProcess tool = getTool();
		if (tool == null) {
			return;
		}
		
		final WizardDialog dialog = new WizardDialog(UIAccess.getActiveWorkbenchShell(true),
				new LoadHistoryWizard(tool));
		dialog.open();
	}
	
}
