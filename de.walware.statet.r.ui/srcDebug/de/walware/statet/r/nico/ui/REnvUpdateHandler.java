/*******************************************************************************
 * Copyright (c) 2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPage;

import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;

import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.nico.IRDataAdapter;
import de.walware.statet.r.nico.REnvUpdater;
import de.walware.statet.r.nico.RProcess;
import de.walware.statet.r.nico.RTool;
import de.walware.statet.r.ui.RUI;


/**
 * 
 */
public class REnvUpdateHandler extends AbstractHandler {
	
	
	private RProcess fProcess;
	private boolean fCompletly;
	
	
	public REnvUpdateHandler() {
	}
	
	public REnvUpdateHandler(final RProcess process, final boolean completly) {
		fProcess = process;
		fCompletly = completly;
	}
	
	
	@Override
	public void setEnabled(final Object evaluationContext) {
		if (fProcess != null) {
			setBaseEnabled(!fProcess.isTerminated());
		}
	}
	
	
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		ToolProcess process = fProcess;
		if (process == null) {
			final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(false);
			process = NicoUI.getToolRegistry().getActiveToolSession(page).getProcess();
		}
		final ToolController controller = NicoUITools.getController("R", RTool.R_DATA_FEATURESET_ID, process);
		final boolean completly = fCompletly;
		controller.submit(new IToolRunnable() {
			public String getTypeId() {
				return "r/workbench/index";
			}
			public SubmitType getSubmitType() {
				return SubmitType.TOOLS;
			}
			public String getLabel() {
				return "Update Help Index";
			}
			public void changed(final int event, final ToolProcess process) {
			}
			public void run(final IToolRunnableControllerAdapter adapter,
					final IProgressMonitor monitor)
					throws InterruptedException, CoreException {
				final IRDataAdapter r = (IRDataAdapter) adapter;
				IREnvConfiguration rEnvConfig = (IREnvConfiguration) r.getProcess().getAdapter(IREnvConfiguration.class);
				if (rEnvConfig != null) {
					rEnvConfig = rEnvConfig.getReference().getConfig();
					if (rEnvConfig != null) {
						final String remoteAddress = r.getWorkspaceData().getRemoteAddress();
						final Map<String, String> properties = new HashMap<String, String>();
						if (remoteAddress != null) {
							properties.put("renv.hostname", remoteAddress);
						}
						
						final REnvUpdater updater = new REnvUpdater(rEnvConfig);
						updater.update(r, completly, properties, monitor);
						r.handleStatus(new Status(IStatus.INFO, RUI.PLUGIN_ID, "Indexing successfully completed."), monitor);
					}
				}
			}
		});
		return null;
	}
	
}
