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

package de.walware.statet.r.ui.pkgmanager;

import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.statet.nico.ui.NicoUI;

import de.walware.rj.eclient.AbstractRToolCommandHandler;
import de.walware.rj.eclient.IRToolService;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.ui.RUI;


public class RPkgUICommandHandler extends AbstractRToolCommandHandler {
	
	
	public static final String OPEN_PACKAGE_MANAGER_COMMAND_ID = "openPackageManager"; //$NON-NLS-1$
	
	
	public RPkgUICommandHandler() {
	}
	
	
	@Override
	public IStatus execute(final String id, final IRToolService r, final Map<String, Object> data, final IProgressMonitor monitor) {
		if (id.equals(OPEN_PACKAGE_MANAGER_COMMAND_ID)) {
			final RProcess process = (RProcess) r.getTool();
			final IWorkbenchPage page = NicoUI.getToolRegistry().findWorkbenchPage(process);
			final OpenRPkgManagerHandler handler = new OpenRPkgManagerHandler(process,
					page.getWorkbenchWindow().getShell() );
			try {
				handler.execute(null);
				return Status.OK_STATUS;
			}
			catch (final ExecutionException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
						"An error occurred when opening the R package manager.", null ));
				return new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1, "", null);
			}
		}
		
		throw new UnsupportedOperationException();
	}
	
}
