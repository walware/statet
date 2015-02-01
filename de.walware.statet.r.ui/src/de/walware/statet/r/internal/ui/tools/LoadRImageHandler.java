/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.tools;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;

import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.actions.AbstractToolHandler;
import de.walware.statet.nico.ui.util.NicoWizardDialog;

import de.walware.statet.r.console.core.RConsoleTool;


public class LoadRImageHandler extends AbstractToolHandler {
	
	
	public LoadRImageHandler() {
		super(RConsoleTool.TYPE, LoadRImageRunnable.REQUIRED_FEATURESET_ID);
	}
	
	
	@Override
	protected Object execute(final ITool tool, final ExecutionEvent event) throws ExecutionException {
		final LoadRImageWizard wizard = new LoadRImageWizard((ToolProcess) tool);
		final WizardDialog dialog = new NicoWizardDialog(UIAccess.getActiveWorkbenchShell(true), wizard);
		dialog.setBlockOnOpen(false);
		dialog.open();
		
		return null;
	}
	
}
