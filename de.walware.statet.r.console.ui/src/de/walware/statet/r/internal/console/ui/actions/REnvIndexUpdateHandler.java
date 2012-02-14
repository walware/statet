/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.console.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;

import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.console.core.RTool;
import de.walware.statet.r.console.ui.tools.REnvIndexAutoUpdater;


/**
 * Command handler scheduling the update of an R environment index.
 */
public class REnvIndexUpdateHandler extends AbstractHandler {
	
	
	private RProcess fProcess;
	private boolean fCompletely;
	
	
	public REnvIndexUpdateHandler() {
	}
	
	public REnvIndexUpdateHandler(final RProcess process, final boolean completely) {
		fProcess = process;
		fCompletely = completely;
	}
	
	
	@Override
	public void setEnabled(final Object evaluationContext) {
		if (fProcess != null) {
			setBaseEnabled(!fProcess.isTerminated());
		}
	}
	
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		ToolProcess process = fProcess;
		if (process == null) {
			final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(false);
			process = NicoUI.getToolRegistry().getActiveToolSession(page).getProcess();
		}
		if (!NicoUITools.isToolReady(RTool.TYPE, RTool.R_DATA_FEATURESET_ID, process)) {
			return null;
		}
		process.getQueue().add(new REnvIndexAutoUpdater.UpdateRunnable(fCompletely));
		return null;
	}
	
}
