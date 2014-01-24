/*=============================================================================#
 # Copyright (c) 2009-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.ui.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.statet.nico.core.runtime.IRemoteEngineController;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.util.IToolProvider;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.actions.ToolRetargetableHandler;


public class DisconnectEngineHandler extends ToolRetargetableHandler {
	
	
	private static class DisconnectJob extends Job {
		
		private static String createLabel(final ToolProcess process) {
			return "Disconnect " + process.getLabel();
		}
		
		
		private final ToolController fController;
		
		
		DisconnectJob(final ToolProcess process, final ToolController controller) {
			super(createLabel(process));
			
			setUser(true);
			setPriority(INTERACTIVE);
			
			fController = controller;
		}
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			try {
				((IRemoteEngineController) fController).disconnect(monitor);
				return Status.OK_STATUS;
			}
			catch (final CoreException e) {
				StatusManager.getManager().handle(e, NicoUI.PLUGIN_ID);
				return e.getStatus();
			}
			finally {
				monitor.done();
			}
		}
	}
	
	
	public DisconnectEngineHandler(final IToolProvider toolProvider, final IServiceLocator serviceLocator) {
		super(toolProvider, serviceLocator);
		init();
	}
	
	
	@Override
	protected boolean evaluateEnabled() {
		final ToolProcess tool = getTool();
		return ((tool != null)
				&& tool.isProvidingFeatureSet(IRemoteEngineController.FEATURE_SET_ID)
				&& !tool.isTerminated() );
	}
	
	@Override
	protected Object doExecute(final ExecutionEvent event) {
		final ToolProcess tool = getCheckedTool();
		final ToolController controller;
		try {
			controller = NicoUITools.accessController(null, tool);
		}
		catch (final CoreException e) {
			StatusManager.getManager().handle(e.getStatus(), StatusManager.SHOW | StatusManager.LOG);
			return null;
		}
		
		final IProgressService progressService = getProgressService();
		final Job job = new DisconnectJob(tool, controller);
		job.schedule();
		progressService.showInDialog(getShell(), job);
		return null;
	}
	
}
